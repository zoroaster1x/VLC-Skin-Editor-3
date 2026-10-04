package dev.zoroaster1x.vlcskin.app.config;

import java.nio.file.Path;
import java.util.Locale;

/**
 * The folders the editor owns under the platform convention. Everything the
 * application writes on its own lives here so the home folder stays clean:
 * settings, the window layout and the keymap in the config root, example skins
 * under {@code examples/}, generated images for untitled themes under
 * {@code exports/}, and user translations under {@code lang/}.
 *
 * <p>Platform roots:
 *
 * <ul>
 *   <li>Linux: {@code $XDG_CONFIG_HOME/vlc-skin-studio} and
 *       {@code $XDG_CACHE_HOME/vlc-skin-studio}, falling back to
 *       {@code ~/.config} and {@code ~/.cache}.</li>
 *   <li>macOS: {@code ~/Library/Application Support/vlc-skin-studio} and
 *       {@code ~/Library/Caches/vlc-skin-studio}.</li>
 *   <li>Windows: {@code %APPDATA%\vlc-skin-studio} and
 *       {@code %LOCALAPPDATA%\vlc-skin-studio\cache}.</li>
 * </ul>
 */
public final class AppPaths {

    private AppPaths() {
    }

    /**
     * The persistent settings, layout, keymap and language root.
     */
    public static Path configDir() {
        Path home = Path.of(System.getProperty("user.home", "."));
        String os = osName();
        if (os.contains("win")) {
            String appData = System.getenv("APPDATA");
            Path base = appData != null && !appData.isBlank()
                    ? Path.of(appData)
                    : home.resolve("AppData").resolve("Roaming");
            return base.resolve("vlc-skin-studio");
        }
        if (os.contains("mac")) {
            return home.resolve("Library").resolve("Application Support").resolve("vlc-skin-studio");
        }
        String configHome = System.getenv("XDG_CONFIG_HOME");
        Path base = configHome != null && !configHome.isBlank()
                ? Path.of(configHome)
                : home.resolve(".config");
        return base.resolve("vlc-skin-studio");
    }

    /**
     * The disposable cache root: gallery lists, previews, archives and update
     * downloads. Losing it never loses user work.
     */
    public static Path cacheDir() {
        Path home = Path.of(System.getProperty("user.home", "."));
        String os = osName();
        if (os.contains("win")) {
            String localAppData = System.getenv("LOCALAPPDATA");
            if (localAppData == null || localAppData.isBlank()) {
                return configDir().resolve("cache");
            }
            return Path.of(localAppData).resolve("vlc-skin-studio").resolve("cache");
        }
        if (os.contains("mac")) {
            return home.resolve("Library").resolve("Caches").resolve("vlc-skin-studio");
        }
        String cacheHome = System.getenv("XDG_CACHE_HOME");
        Path base = cacheHome != null && !cacheHome.isBlank()
                ? Path.of(cacheHome)
                : home.resolve(".cache");
        return base.resolve("vlc-skin-studio");
    }

    /**
     * Bulk data the user would expect to keep: downloaded gallery themes.
     */
    public static Path dataDir() {
        Path home = Path.of(System.getProperty("user.home", "."));
        String os = osName();
        if (os.contains("win")) {
            return configDir();
        }
        if (os.contains("mac")) {
            return home.resolve("Library").resolve("Application Support").resolve("vlc-skin-studio");
        }
        String dataHome = System.getenv("XDG_DATA_HOME");
        Path base = dataHome != null && !dataHome.isBlank()
                ? Path.of(dataHome)
                : home.resolve(".local").resolve("share");
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

    /**
     * Staging folder for update downloads, under the cache.
     */
    public static Path updatesDir() {
        return cacheDir().resolve("updates");
    }

    private static String osName() {
        return System.getProperty("os.name", "").toLowerCase(Locale.ROOT);
    }
}
