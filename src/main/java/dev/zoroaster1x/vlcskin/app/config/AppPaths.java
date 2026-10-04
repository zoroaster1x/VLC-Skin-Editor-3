package dev.zoroaster1x.vlcskin.app.config;

import java.nio.file.Path;

/**
 * The folders the editor owns under the user config directory. Everything the
 * application writes on its own lives here so the home folder stays clean:
 * settings and the window layout in the root, example skins under
 * {@code examples/}, generated images for untitled themes under
 * {@code exports/}, and user translations under {@code lang/}.
 */
public final class AppPaths {

    private AppPaths() {
    }

    /**
     * {@code $XDG_CONFIG_HOME/vlc-skin-studio}, or
     * {@code ~/.config/vlc-skin-studio} when the variable is not set.
     */
    public static Path configDir() {
        String configHome = System.getenv("XDG_CONFIG_HOME");
        Path base = configHome != null && !configHome.isBlank()
                ? Path.of(configHome)
                : Path.of(System.getProperty("user.home"), ".config");
        return base.resolve("vlc-skin-studio");
    }

    /**
     * The folder example skins are created in when the user has no last folder.
     */
    public static Path examplesDir() {
        return configDir().resolve("examples");
    }

    /**
     * The folder generated images go to while a theme has no file yet.
     */
    public static Path exportsDir() {
        return configDir().resolve("exports");
    }
}
