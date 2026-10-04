/*
 * VLC Skin Studio, a modern editor for VLC skins2 themes.
 * Copyright (C) 2026 Zoroaster1x and contributors.
 *
 * This program is free software: you can redistribute it and/or modify it
 * under the terms of the GNU General Public License as published by the Free
 * Software Foundation, either version 3 of the License, or (at your option)
 * any later version. It is a derivative of the original VLC Skin Editor 0.8.6
 * by Daniel Dreibrodt.
 */

package dev.zoroaster1x.vlcskin.app;

import dev.zoroaster1x.vlcskin.app.config.SettingsStore;
import dev.zoroaster1x.vlcskin.app.config.StudioSettings;
import dev.zoroaster1x.vlcskin.app.theme.ThemeManager;
import dev.zoroaster1x.vlcskin.cli.SkinStudioCli;
import dev.zoroaster1x.vlcskin.edit.EditorSession;
import dev.zoroaster1x.vlcskin.util.Platform;
import dev.zoroaster1x.vlcskin.mcp.EditorService;
import java.nio.file.Path;
import java.util.Arrays;
import javax.swing.SwingUtilities;
import picocli.CommandLine;

/**
 * Entry point. Any known CLI subcommand runs the CLI; otherwise the desktop
 * window opens. gui <skin.xml> opens a file with the window.
 */
public final class VlcSkinStudio {

    private VlcSkinStudio() {
    }

    public static void main(String[] args) {
        Thread.setDefaultUncaughtExceptionHandler((thread, throwable) -> {
            System.err.println("Uncaught exception on " + thread.getName());
            throwable.printStackTrace();
        });
        // Inside a native image java.home is not set, and AWT's font configuration
        // looks for it before it falls back to the platform fonts.
        if (System.getProperty("java.home") == null) {
            System.setProperty("java.home", executableFolder());
        }
        // The image does not carry the JVM's fontconfig data; use the file the
        // native build writes next to the executable when it is there.
        if (isNativeImage()) {
            java.nio.file.Path fontConfig = Path.of(executableFolder(), "fontconfig.properties");
            if (java.nio.file.Files.exists(fontConfig)) {
                System.setProperty("sun.awt.fontconfig", fontConfig.toString());
            }
        }
        if (Platform.isMac()) {
            System.setProperty("apple.laf.useScreenMenuBar", "true");
        }
        if (isNativeImage() && (args.length == 0 || "gui".equals(args[0]))) {
            System.err.println("This native build serves the CLI, the TUI, the MCP server and PNG rendering.");
            System.err.println("The desktop window runs on the JVM build:");
            System.err.println("  java -jar vlc-skin-studio.jar");
            System.exit(2);
        }
        if (args.length > 0 && isCliInvocation(args[0])) {
            // CLI, TUI and MCP never need a display; keep AWT headless.
            System.setProperty("java.awt.headless", "true");
            SettingsStore store = new SettingsStore();
            StudioSettings settings = store.load();
            dev.zoroaster1x.vlcskin.cli.McpCommand.HOST.set(
                    () -> new dev.zoroaster1x.vlcskin.app.snapshot.SettingsHost(store, settings));
            int code;
            long started = System.nanoTime();
            dev.zoroaster1x.vlcskin.util.Log.info("command: %s", String.join(" ", args));
            try {
                code = dev.zoroaster1x.vlcskin.cli.SkinStudioCli.commandLine().execute(args);
            } catch (RuntimeException | Error ex) {
                String message = ex.getMessage() == null ? ex.toString() : ex.getMessage();
                dev.zoroaster1x.vlcskin.util.Log.error("%s", message);
                code = 3;
            }
            dev.zoroaster1x.vlcskin.util.Log.info("command finished with %d in %d ms",
                    code, (System.nanoTime() - started) / 1_000_000);
            System.exit(code);
        }
        startGui(args);
    }

    /**
     * True when this class runs inside a GraalVM native image rather than a JVM.
     */
    private static boolean isNativeImage() {
        return System.getProperty("org.graalvm.nativeimage.imagecode") != null;
    }

    private static boolean isCliInvocation(String first) {
        if (first.startsWith("-")) {
            return true;
        }
        return Arrays.stream(new CommandLine(new SkinStudioCli()).getSubcommands().keySet().toArray(String[]::new))
                .anyMatch(name -> name.equals(first));    }

    /**
     * The folder the running executable lives in; a stand-in for java.home
     * inside a native image, where the property is not set.
     */
    private static String executableFolder() {
        return ProcessHandle.current().info().command()
                .map(command -> Path.of(command).toAbsolutePath().getParent())
                .orElseGet(() -> Path.of(System.getProperty("user.dir")))
                .toString();
    }

    private static void startGui(String[] args) {
        SettingsStore store = new SettingsStore();
        StudioSettings settings = store.load();
        System.err.println("VLC Skin Studio " + dev.zoroaster1x.vlcskin.Version.VERSION);
        System.err.println("Settings: " + store.file());
        try {
            ThemeManager.apply(settings.getTheme());
        } catch (Exception ex) {
            ThemeManager.apply("dark");
        }
        EditorService service = new EditorService();
        Studio studio = new Studio(service, store, settings);
        SwingUtilities.invokeLater(() -> {
            StudioFrame frame = new StudioFrame(studio);
            java.awt.Image icon = appIcon();
            if (icon != null) {
                frame.setIconImage(icon);
                setTaskbarIcon(icon);
            }
            frame.setVisible(true);
            dev.zoroaster1x.vlcskin.util.AppLog.start("GUI", dev.zoroaster1x.vlcskin.Version.VERSION);
            dev.zoroaster1x.vlcskin.util.Banner.startup();
            if (args.length > 0 && !"gui".equals(args[0])) {
                Path file = Path.of(args[0]);
                if (java.nio.file.Files.exists(file)) {
                    studio.openFile(file);
                }
            } else if (args.length > 1 && "gui".equals(args[0])) {
                Path file = Path.of(args[1]);
                if (java.nio.file.Files.exists(file)) {
                    studio.openFile(file);
                }
            }
            if (settings.isAutoUpdate()) {
                studio.checkForUpdates(frame, frame::exit, false);
            }
            if (System.getProperty("vlcskin.browser") != null) {
                new dev.zoroaster1x.vlcskin.app.dialog.ThemeBrowserDialog(studio, frame).setVisible(true);
            }
            if (System.getProperty("vlcskin.docs") != null) {
                var documentation = new dev.zoroaster1x.vlcskin.app.dialog.DocumentationDialog(frame);
                String topic = System.getProperty("vlcskin.docs.topic");
                String docsSearch = System.getProperty("vlcskin.docs.search");
                if (topic != null && !topic.isBlank()) {
                    documentation.panel().selectTopic(topic);
                }
                if (docsSearch != null && !docsSearch.isBlank()) {
                    documentation.panel().searchAll(docsSearch);
                }
                documentation.setVisible(true);
            }
            if (System.getProperty("vlcskin.debug") != null) {
                javax.swing.Timer dumpTimer = new javax.swing.Timer(3000, event -> {
                    System.err.println("frame " + frame.getWidth() + "x" + frame.getHeight()
                            + ", displayable=" + frame.isDisplayable()
                            + ", valid=" + frame.isValid()
                            + ", rootPane " + frame.getRootPane().getWidth() + "x" + frame.getRootPane().getHeight()
                            + ", rootPaneLayout=" + frame.getRootPane().getLayout()
                            + ", content " + frame.getContentPane().getWidth()
                            + "x" + frame.getContentPane().getHeight()
                            + ", valid=" + frame.getContentPane().isValid()
                            + ", layout=" + frame.getContentPane().getLayout());
                    dump(frame.getContentPane(), 0);
                    if (Boolean.getBoolean("vlcskin.debug.exit")) {
                        System.exit(0);
                    }
                });
                dumpTimer.setRepeats(false);
                dumpTimer.start();
            }
        });
    }

    /**
     * Prints the panel tree with bounds, for the vlcskin.debug flag.
     */
    private static void dump(java.awt.Component component, int depth) {
        String name = component instanceof javax.swing.JComponent jComponent
                ? String.valueOf(jComponent.getClientProperty("panelName")) : "null";
        if (component instanceof javax.swing.JLabel label && label.getText() != null
                && !label.getText().isBlank()) {
            name = name + " \"" + label.getText().substring(0, Math.min(20, label.getText().length())) + "\"";
        }
        System.err.println("  ".repeat(depth) + component.getClass().getSimpleName()
                + " " + name
                + " " + component.getX() + "," + component.getY()
                + " " + component.getWidth() + "x" + component.getHeight()
                + (component.isVisible() ? "" : " hidden"));
        if (component instanceof java.awt.Container container) {
            for (java.awt.Component child : container.getComponents()) {
                dump(child, depth + 1);
            }
        }
    }

    /**
     * A small generated icon, since the app ships no image assets.
     */
    private static java.awt.Image appIcon() {
        try {
            java.awt.image.BufferedImage image =
                    new java.awt.image.BufferedImage(64, 64, java.awt.image.BufferedImage.TYPE_INT_ARGB);
            java.awt.Graphics2D g = image.createGraphics();
            try {
                g.setRenderingHint(java.awt.RenderingHints.KEY_ANTIALIASING,
                        java.awt.RenderingHints.VALUE_ANTIALIAS_ON);
                g.setColor(new java.awt.Color(0xF2, 0x7C, 0x1E));
                g.fillRoundRect(2, 2, 60, 60, 18, 18);
                g.setColor(java.awt.Color.WHITE);
                java.awt.geom.Path2D play = new java.awt.geom.Path2D.Float();
                play.moveTo(22, 16);
                play.lineTo(50, 32);
                play.lineTo(22, 48);
                play.closePath();
                g.fill(play);
            } finally {
                g.dispose();
            }
            return image;
        } catch (Exception ex) {
            return null;
        }
    }

    private static void setTaskbarIcon(java.awt.Image icon) {
        try {
            if (java.awt.Taskbar.isTaskbarSupported()) {
                java.awt.Taskbar taskbar = java.awt.Taskbar.getTaskbar();
                if (taskbar.isSupported(java.awt.Taskbar.Feature.ICON_IMAGE)) {
                    taskbar.setIconImage(icon);
                }
            }
        } catch (Exception ex) {
            // The taskbar icon is best effort; some platforms throw here.
        }
    }

    /**
     * Opens a session without a UI, used by tests and embedding.
     */
    public static Studio headlessStudio() {
        SettingsStore store = new SettingsStore(Path.of(System.getProperty("java.io.tmpdir"),
                "vlc-skin-studio-test-settings.json"));
        StudioSettings settings = store.load();
        settings.setTheme("dark");
        try {
            ThemeManager.apply(settings.getTheme());
        } catch (Exception ex) {
            // Keep the default look if FlatLaf cannot start.
        }
        return new Studio(new EditorService(), store, settings);
    }

    /**
     * Convenience for callers that only need a session.
     */
    public static EditorSession open(Path file) throws java.io.IOException {
        return EditorSession.open(file);
    }
}
