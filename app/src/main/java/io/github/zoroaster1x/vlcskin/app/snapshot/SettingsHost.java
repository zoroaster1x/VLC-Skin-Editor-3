package io.github.zoroaster1x.vlcskin.app.snapshot;

import io.github.zoroaster1x.vlcskin.app.config.SettingsStore;
import io.github.zoroaster1x.vlcskin.app.config.StudioSettings;
import io.github.zoroaster1x.vlcskin.app.i18n.Messages;
import io.github.zoroaster1x.vlcskin.snapshot.UiInspector;
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
        if (!io.github.zoroaster1x.vlcskin.app.theme.ThemeManager.themes().containsKey(themeId)) {
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
        values.put("aiBaseUrl", settings.getAiBaseUrl());
        values.put("aiModel", settings.getAiModel());
        values.put("aiKeyEnv", settings.getAiKeyEnv());
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
            case "aibaseurl" -> settings.setAiBaseUrl(value);
            case "aimodel" -> settings.setAiModel(value);
            case "aikeyenv" -> settings.setAiKeyEnv(value);
            default -> {
                return false;
            }
        }
        store.save(settings);
        return true;
    }
}
