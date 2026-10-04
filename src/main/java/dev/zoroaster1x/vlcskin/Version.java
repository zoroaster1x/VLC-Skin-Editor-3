package dev.zoroaster1x.vlcskin;

import java.io.InputStream;
import java.util.Properties;

/**
 * The application version. The value is generated from {@code gradle.properties}
 * into a resource at build time, so the release workflow, the About box, the CLI
 * and the update check can never disagree about it.
 */
public final class Version {

    public static final String VERSION = load();
    public static final String NAME = "VLC Skin Studio";

    private Version() {
    }

    private static String load() {
        try (InputStream in = Version.class.getResourceAsStream("/dev/zoroaster1x/vlcskin/version.properties")) {
            if (in != null) {
                Properties properties = new Properties();
                properties.load(in);
                String value = properties.getProperty("version");
                if (value != null && !value.isBlank()) {
                    return value.trim();
                }
            }
        } catch (Exception ex) {
            // A missing version resource must never stop the editor.
        }
        return "0.0.0";
    }
}
