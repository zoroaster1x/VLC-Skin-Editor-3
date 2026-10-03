package dev.zoroaster1x.vlcskin.app.chrome;

import dev.zoroaster1x.vlcskin.app.panel.CanvasPanel;

/**
 * What the menu bar and toolbar can ask the host window to do.
 */
public interface ChromeActions {

    /**
     * The studio the chrome acts on.
     */
    dev.zoroaster1x.vlcskin.app.Studio studio();

    void newSkin();

    void openSkin();

    void save();

    void saveAs();

    void importVlt();

    void exportVlt();

    void renderPreview();

    void testInVlc();

    void undo();

    void redo();

    void duplicate();

    void deleteSelected();

    void moveSelected(int dx, int dy);

    void zoomIn();

    void zoomOut();

    void fitToWindow();

    void setTool(CanvasPanel.Tool tool);

    void validateSkin();

    /**
     * Restores the default docking arrangement.
     */
    void resetLayout();

    /**
     * Opens the official theme gallery browser.
     */
    void browseThemes();

    /**
     * Brings a dockable panel to the front, showing it again when it was
     * hidden.
     */
    void showPanel(String id);

    /**
     * Opens the bundled documentation viewer.
     */
    void browseDocumentation();

    /**
     * The most recent files, newest first.
     */
    java.util.List<String> recentFiles();

    /**
     * Opens one entry of the recent files list.
     */
    void openRecent(String path);

    void openSettings();

    void showVariables();

    void toggleCheckerboard();

    void exit();
}
