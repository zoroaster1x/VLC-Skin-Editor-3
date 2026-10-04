package dev.zoroaster1x.vlcskin.app.panel;

import static org.assertj.core.api.Assertions.assertThat;

import dev.zoroaster1x.vlcskin.app.config.AppPaths;
import dev.zoroaster1x.vlcskin.app.config.StudioSettings;
import java.nio.file.Files;
import java.nio.file.Path;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

/**
 * The welcome card's folder choice for new example skins: the user's last
 * folder when it is usable, the config directory otherwise, never the home
 * folder.
 */
class WelcomeCardTest {

    @Test
    void theLastWritableFolderWins(@TempDir Path folder) {
        StudioSettings settings = new StudioSettings();
        settings.setLastDirectory(folder.toString());
        assertThat(WelcomeCard.exampleFolder(settings, "neon"))
                .isEqualTo(folder.resolve("vlc-skin-neon"));
    }

    @Test
    void flatpakPathsAreSkipped(@TempDir Path folder) throws Exception {
        Path flatpak = folder.resolve("flatpak").resolve("exports");
        Files.createDirectories(flatpak);
        StudioSettings settings = new StudioSettings();
        settings.setLastDirectory(flatpak.toString());
        assertThat(WelcomeCard.exampleFolder(settings, "neon"))
                .isEqualTo(AppPaths.examplesDir().resolve("vlc-skin-neon"));
    }

    @Test
    void withoutALastFolderTheConfigDirectoryIsUsed() {
        StudioSettings settings = new StudioSettings();
        assertThat(WelcomeCard.exampleFolder(settings, "panel"))
                .isEqualTo(AppPaths.examplesDir().resolve("vlc-skin-panel"));
    }

    @Test
    void aBlankLastFolderFallsBackToTheConfigDirectory() {
        StudioSettings settings = new StudioSettings();
        settings.setLastDirectory("  ");
        assertThat(WelcomeCard.exampleFolder(settings, "neon"))
                .startsWith(AppPaths.configDir());
    }
}
