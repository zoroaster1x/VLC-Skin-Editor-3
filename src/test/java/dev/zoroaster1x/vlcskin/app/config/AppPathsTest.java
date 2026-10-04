package dev.zoroaster1x.vlcskin.app.config;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

/**
 * The one place that decides where the application writes. The platform root
 * differs per OS; the folder name and the derived folders must not.
 */
class AppPathsTest {

    @Test
    void everyOwnedFolderHangsOffThePlatformRoots() {
        assertThat(AppPaths.configDir().getFileName().toString()).isEqualTo("vlc-skin-studio");
        assertThat(AppPaths.cacheDir().getFileName().toString()).isEqualTo("vlc-skin-studio");
        assertThat(AppPaths.dataDir().getFileName().toString()).isEqualTo("vlc-skin-studio");
        assertThat(AppPaths.examplesDir()).isEqualTo(AppPaths.configDir().resolve("examples"));
        assertThat(AppPaths.exportsDir()).isEqualTo(AppPaths.configDir().resolve("exports"));
        assertThat(AppPaths.updatesDir()).isEqualTo(AppPaths.cacheDir().resolve("updates"));
    }

    @Test
    void cacheAndConfigRootsAreDistinct() {
        assertThat(AppPaths.cacheDir()).isNotEqualTo(AppPaths.configDir());
        assertThat(AppPaths.cacheDir()).isNotEqualTo(AppPaths.dataDir().resolve("vlc-skin-studio"));
    }
}
