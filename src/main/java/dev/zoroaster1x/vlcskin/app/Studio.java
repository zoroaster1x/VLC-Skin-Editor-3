package dev.zoroaster1x.vlcskin.app;

import dev.zoroaster1x.vlcskin.Version;
import dev.zoroaster1x.vlcskin.app.config.SettingsStore;
import dev.zoroaster1x.vlcskin.app.config.StudioSettings;
import dev.zoroaster1x.vlcskin.app.dialog.ProgressDialog;
import dev.zoroaster1x.vlcskin.app.i18n.Messages;
import dev.zoroaster1x.vlcskin.app.theme.ThemeManager;
import dev.zoroaster1x.vlcskin.edit.EditorSession;
import dev.zoroaster1x.vlcskin.format.VltCodec;
import dev.zoroaster1x.vlcskin.mcp.EditorService;
import dev.zoroaster1x.vlcskin.model.SkinTheme;
import dev.zoroaster1x.vlcskin.util.Json;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import javax.swing.JComponent;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.text.JTextComponent;

/**
 * The application state behind the window: session, settings and operations.
 */
public final class Studio {

    private final EditorService service;
    private final SettingsStore settingsStore;
    private volatile StudioSettings settings;
    private final List<Consumer<String>> statusListeners = new ArrayList<>();
    private boolean darkTheme = true;

    public Studio(EditorService service, SettingsStore settingsStore, StudioSettings settings) {
        this.service = service;
        this.settingsStore = settingsStore;
        this.settings = settings;
        this.darkTheme = ThemeManager.byId(settings.getTheme()).dark();
        // Session changes can arrive from an MCP thread; the panels are Swing.
        service.setDispatcher(runnable -> {
            if (SwingUtilities.isEventDispatchThread()) {
                runnable.run();
            } else {
                SwingUtilities.invokeLater(runnable);
            }
        });
    }

    public EditorService service() {
        return service;
    }

    public EditorSession session() {
        return service.session();
    }

    public StudioSettings settings() {
        return settings;
    }

    public void replaceSettings(StudioSettings newSettings) {
        this.settings = newSettings;
    }

    public boolean isDarkTheme() {
        return darkTheme;
    }

    public void setDarkTheme(boolean dark) {
        this.darkTheme = dark;
        settings.setTheme(dark ? "dark" : "light");
        ThemeManager.apply(settings.getTheme());
        saveSettings();
    }

    public void applyTheme(String id) {
        darkTheme = ThemeManager.byId(id).dark();
        settings.setTheme(id);
        ThemeManager.apply(id);
        saveSettings();
    }

    public void saveSettings() {
        settingsStore.save(settings);
    }

    public void addStatusListener(Consumer<String> listener) {
        synchronized (statusListeners) {
            statusListeners.add(listener);
        }
    }

    public void status(String message) {
        List<Consumer<String>> snapshot;
        synchronized (statusListeners) {
            snapshot = List.copyOf(statusListeners);
        }
        Runnable notify = () -> snapshot.forEach(listener -> listener.accept(message));
        if (SwingUtilities.isEventDispatchThread()) {
            notify.run();
        } else {
            SwingUtilities.invokeLater(notify);
        }
    }


    public void openFile(Path file) {
        var outcome = service.open(file.toAbsolutePath().toString());
        if (outcome.error()) {
            error(outcome.text());
            return;
        }
        settings.remember(file.toAbsolutePath().toString());
        saveSettings();
        status("Opened " + file.getFileName());
        service.session().fireChanged();
    }

    public void openDialog(java.awt.Component parent) {
        JFileChooser chooser = chooser(parent, "Open skin",
                new FileNameExtensionFilter("VLC skins (*.xml, *.vlt)", "xml", "vlt"));
        if (chooser.showOpenDialog(parent) != JFileChooser.APPROVE_OPTION) {
            return;
        }
        Path file = chooser.getSelectedFile().toPath();
        settings.setLastDirectory(chooser.getCurrentDirectory().getAbsolutePath());
        saveSettings();
        if (file.getFileName().toString().toLowerCase().endsWith(".vlt")) {
            importVlt(parent, file);
        } else {
            openFile(file);
        }
    }

    public void newSkin() {
        service.newSkin("Untitled");
        status("New skin");
        service.session().fireChanged();
    }

    /**
     * Asks for a target file first, creates or replaces it, and only then
     * starts the in-memory skin pointed at that file.
     */
    public void newSkin(java.awt.Component parent) {
        JFileChooser chooser = chooser(parent, "New skin",
                new FileNameExtensionFilter("VLC skin (*.xml)", "xml"));
        if (chooser.showSaveDialog(parent) != JFileChooser.APPROVE_OPTION) {
            return;
        }
        Path target = chooser.getSelectedFile().toPath().toAbsolutePath();
        if (!target.getFileName().toString().toLowerCase().endsWith(".xml")) {
            target = target.resolveSibling(target.getFileName() + ".xml");
        }
        if (Files.exists(target)) {
            int replace = JOptionPane.showConfirmDialog(parent,
                    "Replace the existing file \"" + target.getFileName() + "\"?", "New skin",
                    JOptionPane.YES_NO_OPTION);
            if (replace != JOptionPane.YES_OPTION) {
                return;
            }
        }
        service.newSkin("Untitled");
        try {
            session().saveAs(target);
        } catch (IOException ex) {
            error(ex.getMessage());
            return;
        }
        settings.remember(target.toString());
        settings.setLastDirectory(target.getParent().toString());
        saveSettings();
        status("New skin");
        service.session().fireChanged();
    }

    public void save() {
        EditorSession session = session();
        if (session.file() == null) {
            saveAsDialog(null);
            return;
        }
        var outcome = service.save(session.file().toString());
        if (outcome.error()) {
            error(outcome.text());
        } else {
            settings.remember(session.file().toString());
            saveSettings();
            status("Saved " + session.file().getFileName());
        }
    }

    public void saveAsDialog(java.awt.Component parent) {
        JFileChooser chooser = chooser(parent, "Save skin as",
                new FileNameExtensionFilter("VLC skin (*.xml)", "xml"));
        if (chooser.showSaveDialog(parent) != JFileChooser.APPROVE_OPTION) {
            return;
        }
        Path target = chooser.getSelectedFile().toPath();
        if (!target.getFileName().toString().toLowerCase().endsWith(".xml")) {
            target = target.resolveSibling(target.getFileName() + ".xml");
        }
        var outcome = service.save(target.toString());
        if (outcome.error()) {
            error(outcome.text());
        } else {
            settings.remember(target.toString());
            settings.setLastDirectory(target.getParent().toString());
            saveSettings();
            status("Saved " + target.getFileName());
            service.session().fireChanged();
        }
    }

    /**
     * Imports a VLT next to itself, after the original confirmation.
     */
    public void importVlt(java.awt.Component parent, Path archive) {
        String base = archive.getFileName().toString().replaceAll("(?i)\\.(vlt|zip|tar|gz)$", "");
        Path folder = archive.toAbsolutePath().getParent().resolve(base + "_unpacked");
        int confirm = JOptionPane.showConfirmDialog(parent,
                Messages.get("VLT_EX_MSG",
                        "The VLT file will be unpacked to a subfolder called \"%f\".\nDo you want to continue?")
                        .replace("%f", folder.getFileName().toString()),
                Messages.get("VLT_EX_TITLE", "Importing a VLT file"),
                JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);
        if (confirm != JOptionPane.YES_OPTION) {
            return;
        }
        if (Files.exists(folder.resolve("theme.xml"))) {
            if (JOptionPane.showConfirmDialog(parent,
                    "The folder already exists. Replace it?", "Import VLT",
                    JOptionPane.YES_NO_OPTION) != JOptionPane.YES_OPTION) {
                return;
            }
        }
        try {
            var outcome = new ProgressDialog(parent).run(
                    Messages.get("VLT_EX_TITLE", "Importing a VLT file"),
                    Messages.get("VLT_EX_PROGRESS", "Unpacking VLT file..."),
                    () -> service.importVlt(archive.toAbsolutePath().toString(), folder.toString()));
            if (outcome.error()) {
                error(outcome.text());
                return;
            }
            settings.remember(folder.resolve("theme.xml").toString());
            saveSettings();
            status("Imported " + archive.getFileName());
            service.session().fireChanged();
            JOptionPane.showMessageDialog(parent, "Imported " + archive.getFileName(),
                    Messages.get("VLT_EX_TITLE", "Importing a VLT file"), JOptionPane.INFORMATION_MESSAGE);
        } catch (Exception ex) {
            error(ex.getMessage());
        }
    }

    public void importVlt(Path archive) {
        importVlt(null, archive);
    }

    public void exportVltDialog(java.awt.Component parent) {
        JFileChooser chooser = chooser(parent, "Export VLT",
                new FileNameExtensionFilter("VLC theme (*.vlt)", "vlt"));
        if (chooser.showSaveDialog(parent) != JFileChooser.APPROVE_OPTION) {
            return;
        }
        Path target = chooser.getSelectedFile().toPath();
        if (!target.getFileName().toString().toLowerCase().endsWith(".vlt")) {
            target = target.resolveSibling(target.getFileName() + ".vlt");
        }
        Path exportTarget = target;
        List<String> missing = missingReferencedFiles();
        try {
            var outcome = new ProgressDialog(parent).run(
                    "Export VLT",
                    Messages.get("VLT_PROGRESS", "Creating VLT file..."),
                    () -> service.exportVlt(exportTarget.toString()));
            if (outcome.error()) {
                error(outcome.text());
                return;
            }
            status("Exported " + exportTarget.getFileName());
            if (!missing.isEmpty()) {
                status("Warning: files that do not exist were skipped: " + String.join(", ", missing));
            }
            JOptionPane.showMessageDialog(parent, Messages.get("VLT_SUCCESS_MSG", "VLT file has been successfully created."),
                    Messages.get("VLT_SUCCESS_TITLE", "VLT file created"), JOptionPane.INFORMATION_MESSAGE);
        } catch (Exception ex) {
            error(ex.getMessage());
        }
    }

    /**
     * Files the theme references that the exporter will skip because they are absent.
     */
    private List<String> missingReferencedFiles() {
        Path folder = session().file() == null ? Path.of(".") : session().file().getParent();
        if (folder == null) {
            folder = Path.of(".");
        }
        List<String> missing = new ArrayList<>();
        for (String relative : VltCodec.referencedFiles(session().theme())) {
            if (relative == null || relative.isBlank()) {
                continue;
            }
            Path asset = folder.resolve(relative.replace('\\', '/')).normalize();
            if (!Files.isRegularFile(asset)) {
                missing.add(relative);
            }
        }
        return missing;
    }

    public void renderPreviewDialog(java.awt.Component parent) {
        JFileChooser chooser = chooser(parent, "Save preview",
                new FileNameExtensionFilter("PNG image (*.png)", "png"));
        if (chooser.showSaveDialog(parent) != JFileChooser.APPROVE_OPTION) {
            return;
        }
        try {
            dev.zoroaster1x.vlcskin.snapshot.PreviewSnapshot.Result result =
                    dev.zoroaster1x.vlcskin.snapshot.PreviewSnapshot.capture(session(), 1);
            Files.write(chooser.getSelectedFile().toPath(), result.png());
            status("Preview written");
        } catch (IOException | IllegalStateException ex) {
            error(ex.getMessage());
        }
    }

    public JFileChooser chooser(java.awt.Component parent, String title, FileNameExtensionFilter filter) {
        JFileChooser chooser = new JFileChooser();
        chooser.setDialogTitle(title);
        chooser.setFileFilter(filter);
        if (settings.getLastDirectory() != null) {
            chooser.setCurrentDirectory(Path.of(settings.getLastDirectory()).toFile());
        } else {
            Path fallback = defaultDirectory();
            if (fallback != null) {
                chooser.setCurrentDirectory(fallback.toFile());
            }
        }
        if (settings.getLastDirectory() == null && chooser.getCurrentDirectory() != null) {
            settings.setLastDirectory(chooser.getCurrentDirectory().getAbsolutePath());
        }
        return chooser;
    }

    /**
     * The VLC skins folder, used when the user has no last directory yet.
     * Only an existing directory is returned; nothing is created.
     */
    private Path defaultDirectory() {
        Path candidate = null;
        Path skins = dev.zoroaster1x.vlcskin.util.VlcFinder.skinsFolder();
        if (Files.isDirectory(skins)) {
            candidate = skins;
        }
        if (candidate == null || !Files.isDirectory(candidate)) {
            candidate = Path.of(System.getProperty("user.home"), "vlc-skins");
            if (!Files.isDirectory(candidate)) {
                candidate = Path.of(System.getProperty("user.home"));
            }
        }
        return Files.isDirectory(candidate) ? candidate : null;
    }

    /**
     * Asks GitHub for the newest release tag in the background. A newer version
     * is announced with a dialog; failures only reach the status bar.
     */
    public void checkForUpdates(java.awt.Component parent) {
        Thread.ofVirtual().name("vlc-skin-studio-update-check").start(() -> {
            String tag;
            try {
                tag = latestReleaseTag();
            } catch (Exception ex) {
                status("Update check failed: " + ex.getMessage());
                return;
            }
            if (tag == null) {
                status("Update check failed: the release reply had no tag");
                return;
            }
            if (isNewer(tag, Version.VERSION)) {
                SwingUtilities.invokeLater(() -> JOptionPane.showMessageDialog(parent,
                        "A newer version is available: " + tag
                                + "\nhttps://github.com/zoroaster1x/vlc-skin-editor/releases",
                        "Check for updates", JOptionPane.INFORMATION_MESSAGE));
            } else {
                status(Version.NAME + " " + Version.VERSION + " is up to date");
            }
        });
    }

    @SuppressWarnings("unchecked")
    private static String latestReleaseTag() throws IOException, InterruptedException {
        var client = java.net.http.HttpClient.newHttpClient();
        var request = java.net.http.HttpRequest.newBuilder(java.net.URI.create(
                        "https://api.github.com/repos/zoroaster1x/vlc-skin-editor/releases/latest"))
                .header("Accept", "application/vnd.github+json")
                .timeout(java.time.Duration.ofSeconds(10))
                .GET()
                .build();
        var response = client.send(request, java.net.http.HttpResponse.BodyHandlers.ofString());
        if (response.statusCode() != 200) {
            throw new IOException("HTTP " + response.statusCode());
        }
        Map<String, Object> release = Json.read(response.body(), Map.class);
        Object tag = release.get("tag_name");
        return tag == null ? null : tag.toString();
    }

    private static boolean isNewer(String candidate, String current) {
        int[] newer = versionParts(candidate);
        int[] older = versionParts(current);
        for (int i = 0; i < Math.max(newer.length, older.length); i++) {
            int left = i < newer.length ? newer[i] : 0;
            int right = i < older.length ? older[i] : 0;
            if (left != right) {
                return left > right;
            }
        }
        return false;
    }

    private static int[] versionParts(String version) {
        java.util.regex.Matcher matcher = java.util.regex.Pattern
                .compile("(\\d+)(?:\\.(\\d+))?(?:\\.(\\d+))?").matcher(version);
        if (!matcher.find()) {
            return new int[0];
        }
        int[] parts = new int[3];
        for (int i = 0; i < parts.length; i++) {
            String group = matcher.group(i + 1);
            parts[i] = group == null ? 0 : Integer.parseInt(group);
        }
        return parts;
    }

    public void error(String message) {
        status(message);
        JOptionPane.showMessageDialog(null, message, "VLC Skin Studio", JOptionPane.ERROR_MESSAGE);
    }

    public void runOnEdt(Runnable runnable) {
        if (SwingUtilities.isEventDispatchThread()) {
            runnable.run();
        } else {
            SwingUtilities.invokeLater(runnable);
        }
    }

    /**
     * Replaces the whole document from an edited XML text, with validation.
     */
    public void applyXml(String xml, java.awt.Component parent) {
        var result = dev.zoroaster1x.vlcskin.format.SkinParser.parse(
                xml.getBytes(java.nio.charset.StandardCharsets.UTF_8),
                session().file() == null ? "untitled.xml" : session().file().toString());
        if (result.hasErrors()) {
            StringBuilder message = new StringBuilder("The XML has errors:\n");
            result.issues().stream()
                    .filter(issue -> issue.severity() == dev.zoroaster1x.vlcskin.format.ParseIssue.Severity.ERROR)
                    .limit(8)
                    .forEach(issue -> message.append("  ").append(issue).append('\n'));
            error(message.toString());
            return;
        }
        SkinTheme theme = result.theme();
        session().replace(theme, session().file(), result.issues());
        status("Applied XML");
    }
}
