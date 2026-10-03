package io.github.zoroaster1x.vlcskin.app;

import io.github.zoroaster1x.vlcskin.app.config.SettingsStore;
import io.github.zoroaster1x.vlcskin.app.config.StudioSettings;
import io.github.zoroaster1x.vlcskin.app.theme.ThemeManager;
import io.github.zoroaster1x.vlcskin.cli.SkinStudioCli;
import io.github.zoroaster1x.vlcskin.edit.EditorSession;
import io.github.zoroaster1x.vlcskin.mcp.EditorService;
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
        if (isMac()) {
            System.setProperty("apple.laf.useScreenMenuBar", "true");
        }
        if (args.length > 0 && isCliInvocation(args[0])) {
            // CLI, TUI and MCP never need a display; keep AWT headless.
            System.setProperty("java.awt.headless", "true");
            SettingsStore store = new SettingsStore();
            StudioSettings settings = store.load();
            io.github.zoroaster1x.vlcskin.cli.McpCommand.HOST.set(
                    () -> new io.github.zoroaster1x.vlcskin.app.snapshot.SettingsHost(store, settings));
            int code = new CommandLine(new SkinStudioCli()).execute(args);
            System.exit(code);
        }
        startGui(args);
    }

    private static boolean isCliInvocation(String first) {
        if (first.startsWith("-")) {
            return true;
        }
        return Arrays.stream(new CommandLine(new SkinStudioCli()).getSubcommands().keySet().toArray(String[]::new))
                .anyMatch(name -> name.equals(first));
    }

    private static void startGui(String[] args) {
        SettingsStore store = new SettingsStore();
        StudioSettings settings = store.load();
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
                studio.checkForUpdates(frame);
            }
        });
    }

    private static boolean isMac() {
        return System.getProperty("os.name", "").toLowerCase(java.util.Locale.ROOT).contains("mac");
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
