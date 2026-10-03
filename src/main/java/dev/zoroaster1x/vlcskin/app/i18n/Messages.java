package dev.zoroaster1x.vlcskin.app.i18n;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.MissingResourceException;
import java.util.PropertyResourceBundle;
import java.util.ResourceBundle;

/**
 * UI strings with the original editor's translations. The bundle files are
 * converted from the original language files (GPL-2.0-or-later), so the keys
 * match the keys the original editor used for the same concepts. Newer
 * surfaces fall back to English until a translation exists.
 */
public final class Messages {

    public static final String DEFAULT_LANGUAGE = "en";
    private static final String RESOURCE_ROOT = "/dev/zoroaster1x/vlcskin/app/messages/";
    private static final Map<String, ResourceBundle> BUNDLES = new java.util.HashMap<>();
    private static volatile String language = DEFAULT_LANGUAGE;

    private Messages() {
    }

    /**
     * The folder a user translation can be dropped into, one properties file
     * per language, without rebuilding.
     */
    private static java.nio.file.Path userLanguageFolder() {
        return dev.zoroaster1x.vlcskin.app.config.SettingsStore.defaultPath()
                .resolveSibling("lang");
    }

    public static void setLanguage(String code) {
        language = code == null || code.isBlank() ? DEFAULT_LANGUAGE : code;
    }

    public static String language() {
        return language;
    }

    public static String get(String key, String fallback) {
        ResourceBundle bundle = bundle(language);
        if (bundle != null && bundle.containsKey(key)) {
            return bundle.getString(key);
        }
        return fallback;
    }

    /**
     * Replaces the original placeholders %t, %i, %n and %s in order.
     */
    public static String format(String key, String fallback, Object... values) {
        String text = get(key, fallback);
        for (Object value : values) {
            int index = firstPlaceholder(text);
            if (index < 0) {
                break;
            }
            text = text.substring(0, index) + value + text.substring(index + 2);
        }
        return text;
    }

    private static int firstPlaceholder(String text) {
        for (int i = 0; i < text.length() - 1; i++) {
            if (text.charAt(i) == '%' && "tinsp".indexOf(text.charAt(i + 1)) >= 0) {
                return i;
            }
        }
        return -1;
    }

    /**
     * The languages that actually have a bundle on the classpath.
     */
    public static List<Language> available() {
        List<Language> languages = new ArrayList<>();
        try (InputStream in = Messages.class.getResourceAsStream(RESOURCE_ROOT + "languages.properties")) {
            if (in == null) {
                languages.add(new Language(DEFAULT_LANGUAGE, "English"));
                return languages;
            }
            BufferedReader reader = new BufferedReader(new InputStreamReader(in, StandardCharsets.UTF_8));
            String line;
            while ((line = reader.readLine()) != null) {
                if (line.isBlank() || line.startsWith("#") || !line.contains("=")) {
                    continue;
                }
                int split = line.indexOf('=');
                languages.add(new Language(line.substring(0, split).trim(), line.substring(split + 1).trim()));
            }
        } catch (IOException ex) {
            languages.add(new Language(DEFAULT_LANGUAGE, "English"));
        }
        if (languages.stream().noneMatch(item -> item.code().equals(DEFAULT_LANGUAGE))) {
            languages.add(0, new Language(DEFAULT_LANGUAGE, "English"));
        }
        java.nio.file.Path userFolder = userLanguageFolder();
        try {
            if (java.nio.file.Files.isDirectory(userFolder)) {
                try (var files = java.nio.file.Files.list(userFolder)) {
                    files.filter(java.nio.file.Files::isRegularFile).forEach(path -> {
                        String name = path.getFileName().toString();
                        if (!name.startsWith("messages_") || !name.endsWith(".properties")) {
                            return;
                        }
                        String code = name.substring("messages_".length(), name.length() - ".properties".length());
                        if (languages.stream().noneMatch(item -> item.code().equalsIgnoreCase(code))) {
                            languages.add(new Language(code, code));
                        }
                    });
                }
            }
        } catch (IOException ex) {
            // User translations are a bonus; the classpath list is enough.
        }
        return languages;
    }

    private static synchronized ResourceBundle bundle(String code) {
        if (BUNDLES.containsKey(code)) {
            return BUNDLES.get(code);
        }
        ResourceBundle bundle = null;
        java.nio.file.Path userFile = userLanguageFolder()
                .resolve("messages_" + code + ".properties");
        try {
            if (java.nio.file.Files.isRegularFile(userFile)) {
                try (InputStream in = java.nio.file.Files.newInputStream(userFile)) {
                    bundle = new PropertyResourceBundle(new InputStreamReader(in, StandardCharsets.UTF_8));
                }
            }
        } catch (IOException ex) {
            bundle = null;
        }
        if (bundle == null) {
            try (InputStream in = Messages.class.getResourceAsStream(RESOURCE_ROOT + "messages_" + code + ".properties")) {
                if (in != null) {
                    bundle = new PropertyResourceBundle(new InputStreamReader(in, StandardCharsets.UTF_8));
                }
            } catch (IOException | MissingResourceException ex) {
                bundle = null;
            }
        }
        BUNDLES.put(code, bundle);
        return bundle;
    }

    public record Language(String code, String name) {
        @Override
        public String toString() {
            return name;
        }
    }
}
