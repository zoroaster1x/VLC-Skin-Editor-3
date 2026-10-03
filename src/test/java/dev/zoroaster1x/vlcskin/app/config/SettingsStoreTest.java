package dev.zoroaster1x.vlcskin.app.config;

import static org.assertj.core.api.Assertions.assertThat;

import java.nio.file.Files;
import java.nio.file.Path;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class SettingsStoreTest {

    @Test
    void savesAtomicallyAndKeepsABackup(@TempDir Path folder) {
        Path file = folder.resolve("settings.json");
        SettingsStore store = new SettingsStore(file);
        StudioSettings settings = new StudioSettings();
        settings.setTheme("arc");
        store.save(settings);
        settings.setTheme("one-dark");
        store.save(settings);

        assertThat(store.load().getTheme()).isEqualTo("one-dark");
        assertThat(folder.resolve("settings.json.bak")).exists();
        assertThat(folder.resolve("settings.json")).exists();
    }

    @Test
    void aCorruptFileFallsBackToTheBackup(@TempDir Path folder) throws Exception {
        Path file = folder.resolve("settings.json");
        SettingsStore store = new SettingsStore(file);
        StudioSettings settings = new StudioSettings();
        settings.setTheme("darcula");
        store.save(settings);
        settings.setTheme("light");
        store.save(settings);

        Files.writeString(file, "{not json");
        assertThat(store.load().getTheme()).isEqualTo("darcula");
    }

    @Test
    void defaultsFollowTheSystemLocaleWhenKnown(@TempDir Path folder) {
        SettingsStore store = new SettingsStore(folder.resolve("settings.json"));
        String language = store.load().getLanguage();
        assertThat(language).isNotBlank();
    }
}
