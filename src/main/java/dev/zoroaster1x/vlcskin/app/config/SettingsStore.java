package dev.zoroaster1x.vlcskin.app.config;

import dev.zoroaster1x.vlcskin.util.Json;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;

/**
 * Loads and saves the studio settings as JSON under the user config folder.
 *
 * <p>Saving is atomic and keeps one backup: a crash while writing can never
 * leave a half written settings file, and a corrupt file falls back to the
 * backup before defaults. Reads and writes are synchronized because the window
 * and an MCP server can use the same file at the same time.
 */
public final class SettingsStore {

    private static final Object LOCK = new Object();

    private final Path file;

    public SettingsStore() {
        this(defaultPath());
    }

    public SettingsStore(Path file) {
        this.file = file;
    }

    public static Path defaultPath() {
        return AppPaths.configDir().resolve("settings.json");
    }

    public Path file() {
        return file;
    }

    public StudioSettings load() {
        synchronized (LOCK) {
            StudioSettings loaded = read(file);
            if (loaded == null) {
                loaded = read(backupFile());
            }
            if (loaded != null) {
                return loaded;
            }
        }
        StudioSettings settings = new StudioSettings();
        settings.setLanguage(defaultLanguage());
        return settings;
    }

    public void save(StudioSettings settings) {
        synchronized (LOCK) {
            try {
                Files.createDirectories(file.getParent());
                if (Files.isRegularFile(file)) {
                    Files.copy(file, backupFile(), StandardCopyOption.REPLACE_EXISTING);
                }
                Path temp = Files.createTempFile(file.getParent(), "settings", ".json");
                Files.writeString(temp, Json.write(settings));
                try {
                    Files.move(temp, file, StandardCopyOption.REPLACE_EXISTING,
                            StandardCopyOption.ATOMIC_MOVE);
                } catch (IOException ex) {
                    Files.move(temp, file, StandardCopyOption.REPLACE_EXISTING);
                }
            } catch (IOException ex) {
                // Saving preferences is best effort.
            }
        }
    }

    private Path backupFile() {
        return file.resolveSibling(file.getFileName() + ".bak");
    }

    private static StudioSettings read(Path path) {
        try {
            if (Files.isRegularFile(path)) {
                return Json.mapper().readValue(Files.readString(path), StudioSettings.class);
            }
        } catch (Exception ex) {
            // A broken settings file must never stop the editor from starting.
        }
        return null;
    }

    /**
     * First run: follow the system locale when the editor speaks it.
     */
    private static String defaultLanguage() {
        String code = java.util.Locale.getDefault().getLanguage();
        boolean known = dev.zoroaster1x.vlcskin.app.i18n.Messages.available().stream()
                .anyMatch(language -> language.code().equalsIgnoreCase(code));
        return known ? code.toLowerCase(java.util.Locale.ROOT) : "en";
    }
}
