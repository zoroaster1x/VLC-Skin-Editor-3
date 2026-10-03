package io.github.zoroaster1x.vlcskin.app.config;

import io.github.zoroaster1x.vlcskin.util.Json;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * Loads and saves the studio settings as JSON under the user config folder.
 */
public final class SettingsStore {

    private final Path file;

    public SettingsStore() {
        this(defaultPath());
    }

    public SettingsStore(Path file) {
        this.file = file;
    }

    public static Path defaultPath() {
        String configHome = System.getenv("XDG_CONFIG_HOME");
        Path base = configHome != null && !configHome.isBlank()
                ? Path.of(configHome)
                : Path.of(System.getProperty("user.home"), ".config");
        return base.resolve("vlc-skin-studio").resolve("settings.json");
    }

    public Path file() {
        return file;
    }

    public StudioSettings load() {
        try {
            if (Files.exists(file)) {
                return Json.mapper().readValue(Files.readString(file), StudioSettings.class);
            }
        } catch (IOException ex) {
            // A broken settings file must never stop the editor from starting.
        }
        return new StudioSettings();
    }

    public void save(StudioSettings settings) {
        try {
            Files.createDirectories(file.getParent());
            Files.writeString(file, Json.write(settings));
        } catch (IOException ex) {
            // Saving preferences is best effort.
        }
    }
}
