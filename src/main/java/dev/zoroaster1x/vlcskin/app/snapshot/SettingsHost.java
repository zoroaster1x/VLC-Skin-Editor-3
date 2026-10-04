package dev.zoroaster1x.vlcskin.app.snapshot;

import dev.zoroaster1x.vlcskin.app.config.SettingsStore;
import dev.zoroaster1x.vlcskin.app.config.StudioSettings;
import dev.zoroaster1x.vlcskin.app.i18n.Messages;
import dev.zoroaster1x.vlcskin.snapshot.UiInspector;
import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;

/**
 * A host for CLI and MCP runs with no window: preferences read and write the
 * same settings file the desktop app uses, while screenshots and canvas state
 * stay unavailable.
 */
public final class SettingsHost implements UiInspector {

    private final SettingsStore store;
    private final StudioSettings settings;

    public SettingsHost(SettingsStore store, StudioSettings settings) {
        this.store = store;
        this.settings = settings;
        Messages.setLanguage(settings.getLanguage());
    }

    @Override
    public boolean available() {
        return false;
    }

    @Override
    public String describeUi() {
        return "{\"host\":\"cli\",\"window\":false}";
    }

    @Override
    public byte[] screenshotPng() {
        return null;
    }

    @Override
    public String summary() {
        return "CLI host without a window";
    }

    @Override
    public boolean applyTheme(String themeId) {
        if (!dev.zoroaster1x.vlcskin.app.theme.ThemeManager.themes().containsKey(themeId)) {
            return false;
        }
        settings.setTheme(themeId);
        store.save(settings);
        return true;
    }

    @Override
    public Map<String, String> preferences() {
        Map<String, String> values = new LinkedHashMap<>();
        values.put("theme", settings.getTheme());
        values.put("language", settings.getLanguage());
        values.put("checkerboard", Boolean.toString(settings.isCheckerboard()));
        values.put("showToolbar", Boolean.toString(settings.isShowToolbar()));
        values.put("canvasZoom", Integer.toString(settings.getCanvasZoom()));
        values.put("canvasBackground", settings.getCanvasBackground());
        values.put("autoUpdate", Boolean.toString(settings.isAutoUpdate()));
        values.put("recentFiles", String.join("\n", settings.getRecentFiles()));
        return values;
    }

    @Override
    public boolean setPreference(String key, String value) {
        switch (key.toLowerCase(Locale.ROOT)) {
            case "theme" -> settings.setTheme(value);
            case "language" -> {
                Messages.setLanguage(value);
                settings.setLanguage(value);
            }
            case "checkerboard" -> settings.setCheckerboard(Boolean.parseBoolean(value));
            case "showtoolbar" -> settings.setShowToolbar(Boolean.parseBoolean(value));
            case "canvaszoom" -> settings.setCanvasZoom(Math.max(1, Math.min(16, Integer.parseInt(value))));
            case "canvasbackground" -> settings.setCanvasBackground(
                    dev.zoroaster1x.vlcskin.app.theme.ThemeManager.normalizeCanvasBackground(value));
            case "autoupdate" -> settings.setAutoUpdate(Boolean.parseBoolean(value));
            default -> {
                return false;
            }
        }
        store.save(settings);
        return true;
    }

    @Override
    public boolean setCanvas(Integer zoom, String tool, Boolean checkerboard) {
        if (zoom != null) {
            settings.setCanvasZoom(Math.max(1, Math.min(16, zoom)));
        }
        if (checkerboard != null) {
            settings.setCheckerboard(checkerboard);
        }
        if (zoom == null && checkerboard == null) {
            return false;
        }
        store.save(settings);
        return true;
    }
}
