package dev.zoroaster1x.vlcskin.app.config;

import java.util.ArrayList;
import java.util.List;
import lombok.Getter;
import lombok.Setter;

/**
 * Everything the desktop app remembers between runs.
 */
@Getter
@Setter
public final class StudioSettings {

    private String theme = "dark";
    private String lastDirectory;
    private List<String> recentFiles = new ArrayList<>();
    private int windowX = -1;
    private int windowY = -1;
    private int windowWidth = 1440;
    private int windowHeight = 900;
    private boolean windowMaximized;
    private boolean showWelcome = true;
    private boolean checkerboard = true;
    private boolean showToolbar = true;
    private boolean toolbarFloating;
    private int toolbarOrientation;
    private int toolbarX = -1;
    private int toolbarY = -1;
    private boolean autoUpdate = true;
    private String language = "en";
    private int canvasZoom = 2;
    private String canvasBackground = "theme";
    private boolean showToolCalls;
    private boolean mcpEnabled = true;
    private String lastExample = "neon";

    /**
     * Interface font scale in percent; 100 uses the look and feel default.
     */
    private int fontScale = 100;

    /**
     * User overrides for {@link dev.zoroaster1x.vlcskin.app.config.Keymap}
     * action ids; an empty value means deliberately unbound.
     */
    private java.util.Map<String, String> keys = new java.util.LinkedHashMap<>();

    public void remember(String file) {
        recentFiles.remove(file);
        recentFiles.addFirst(file);
        while (recentFiles.size() > 12) {
            recentFiles.removeLast();
        }
    }
}
