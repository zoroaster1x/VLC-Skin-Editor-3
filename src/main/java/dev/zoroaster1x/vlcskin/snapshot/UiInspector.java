package dev.zoroaster1x.vlcskin.snapshot;

/**
 * Implemented by the running desktop UI so the MCP server can report what the
 * user sees and hand a screenshot back. Core only talks to this interface.
 */
public interface UiInspector {

    /**
     * True when a desktop UI is up and can answer.
     */
    boolean available();

    /**
     * JSON describing the current window, panels, sizes and focused control.
     */
    String describeUi();

    /**
     * PNG of the whole application window, or null when not available.
     */
    byte[] screenshotPng();

    /**
     * A short human sentence about the current state, for logs.
     */
    String summary();

    /**
     * Applies a theme id when a UI is attached.
     */
    default boolean applyTheme(String themeId) {
        return false;
    }

    /**
     * Brings a named panel to the front when a UI is attached.
     */
    default boolean showPanel(String panelName) {
        return false;
    }

    /**
     * Sets zoom, tool or checkerboard on the canvas when a UI is attached.
     */
    default boolean setCanvas(Integer zoom, String tool, Boolean checkerboard) {
        return false;
    }

    /**
     * Opens the skin settings dialog when a UI is attached.
     */
    default boolean openSettings() {
        return false;
    }

    /**
     * The host preferences as string key/value pairs, empty when no host.
     */
    default java.util.Map<String, String> preferences() {
        return java.util.Map.of();
    }

    /**
     * Writes one host preference; false when no host can persist it.
     */
    default boolean setPreference(String key, String value) {
        return false;
    }
}
