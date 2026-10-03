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
    private boolean autoUpdate;
    private String language = "en";
    private int canvasZoom = 2;
    private String aiBaseUrl = "";
    private String aiModel = "gpt-4o-mini";
    private String aiKeyEnv = "OPENAI_API_KEY";
    private boolean aiShowToolCalls;
    private String lastExample = "neon";

    public void remember(String file) {
        recentFiles.remove(file);
        recentFiles.add(0, file);
        while (recentFiles.size() > 12) {
            recentFiles.remove(recentFiles.size() - 1);
        }
    }
}
