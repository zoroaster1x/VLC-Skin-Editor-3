package dev.zoroaster1x.vlcskin.app.theme;

import com.formdev.flatlaf.FlatDarkLaf;
import com.formdev.flatlaf.FlatLaf;
import com.formdev.flatlaf.FlatLightLaf;
import com.formdev.flatlaf.intellijthemes.FlatArcDarkIJTheme;
import com.formdev.flatlaf.intellijthemes.FlatArcIJTheme;
import com.formdev.flatlaf.intellijthemes.FlatOneDarkIJTheme;
import java.awt.Color;
import java.awt.Font;
import java.awt.Window;
import java.util.LinkedHashMap;
import java.util.Map;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;

/**
 * The theme catalog. The accent is VLC orange, which gives the tool its own
 * identity instead of the default blue every editor ships.
 */
public final class ThemeManager {

    public static final Color ACCENT = new Color(0xE0, 0x6C, 0x38);
    public static final Color ACCENT_DARK = new Color(0xB8, 0x53, 0x24);

    private static final Map<String, Theme> THEMES = new LinkedHashMap<>();

    /**
     * One selectable look.
     */
    public record Theme(String id, String label, boolean dark, java.util.function.Supplier<FlatLaf> factory) {
    }

    static {
        THEMES.put("light", new Theme("light", "Light", false, FlatLightLaf::new));
        THEMES.put("dark", new Theme("dark", "Dark", true, FlatDarkLaf::new));
        THEMES.put("intellij", new Theme("intellij", "IntelliJ", false,
                com.formdev.flatlaf.FlatIntelliJLaf::new));
        THEMES.put("darcula", new Theme("darcula", "Darcula", true,
                com.formdev.flatlaf.FlatDarculaLaf::new));
        THEMES.put("arc", new Theme("arc", "Arc", false, FlatArcIJTheme::new));
        THEMES.put("arc-dark", new Theme("arc-dark", "Arc dark", true, FlatArcDarkIJTheme::new));
        THEMES.put("one-dark", new Theme("one-dark", "One dark", true, FlatOneDarkIJTheme::new));
    }

    private ThemeManager() {
    }

    public static Map<String, Theme> themes() {
        return THEMES;
    }

    public static Theme byId(String id) {
        return THEMES.getOrDefault(id, THEMES.get("dark"));
    }

    public static boolean currentIsDark() {
        return FlatLaf.isLafDark();
    }

    private static final Color CANVAS_DARK = new Color(0x16, 0x18, 0x1D);
    private static final Color CANVAS_LIGHT = new Color(0xEC, 0xEE, 0xF1);

    /**
     * The backdrop behind the preview. Dark looks keep the near black stage;
     * light looks get a light neutral so the canvas is not a dark hole in an
     * otherwise white window.
     */
    public static Color canvasBackground() {
        return currentIsDark() ? CANVAS_DARK : CANVAS_LIGHT;
    }

    /**
     * The backdrop for the stored preference: {@code light} and {@code dark}
     * pin the stage regardless of the window theme, anything else follows it.
     */
    public static Color canvasBackground(String preference) {
        if ("light".equalsIgnoreCase(preference)) {
            return CANVAS_LIGHT;
        }
        if ("dark".equalsIgnoreCase(preference)) {
            return CANVAS_DARK;
        }
        return canvasBackground();
    }

    /**
     * The storable form of a canvas backdrop preference; unknown values fall
     * back to following the theme.
     */
    public static String normalizeCanvasBackground(String value) {
        if ("light".equalsIgnoreCase(value) || "dark".equalsIgnoreCase(value)) {
            return value.toLowerCase(java.util.Locale.ROOT);
        }
        return "theme";
    }

    /**
     * Applies a theme and the studio UI defaults.
     */
    public static void apply(String themeId) {
        Theme theme = byId(themeId);
        FlatLaf laf = theme.factory().get();
        FlatLaf.setup(laf);

        Color accent = FlatLaf.isLafDark() ? ACCENT : ACCENT;
        UIManager.put("Component.accentColor", accent);
        UIManager.put("Component.arc", 10);
        UIManager.put("Button.arc", 12);
        UIManager.put("TextComponent.arc", 8);
        UIManager.put("CheckBox.arc", 6);
        UIManager.put("ProgressBar.arc", 8);
        UIManager.put("Component.focusWidth", 1);
        UIManager.put("Component.innerFocusWidth", 1);
        UIManager.put("ScrollBar.thumbArc", 999);
        UIManager.put("ScrollBar.thumbInsets", new java.awt.Insets(2, 2, 2, 2));
        UIManager.put("ScrollBar.width", 12);
        UIManager.put("TabbedPane.tabHeight", 32);
        UIManager.put("TabbedPane.showTabSeparators", true);
        UIManager.put("TabbedPane.tabSelectionHeight", 3);
        UIManager.put("Table.showHorizontalLines", true);
        UIManager.put("Table.showVerticalLines", false);
        baseFont = null;
        applyFontScale(fontScalePercent);
        UIManager.put("ToolTip.background", UIManager.getColor("Panel.background"));
        UIManager.put("ToolTip.border", javax.swing.BorderFactory.createLineBorder(
                UIManager.getColor("Component.borderColor")));
        UIManager.put("TitlePane.unifiedBackground", true);
        FlatLaf.updateUI();
    }

    private static Font baseFont;
    private static int fontScalePercent = 100;

    /**
     * Scales the whole interface by changing the look and feel default font.
     * Row heights and our own derived fonts follow, so 125 percent and 150
     * percent grow panels and text together instead of clipping.
     */
    public static void applyFontScale(int percent) {
        fontScalePercent = Math.max(75, Math.min(200, percent));
        if (baseFont == null) {
            baseFont = UIManager.getFont("Label.font");
        }
        if (baseFont == null) {
            return;
        }
        float size = Math.max(9f, Math.round(baseFont.getSize2D() * fontScalePercent / 100f));
        Font font = baseFont.deriveFont(size);
        UIManager.put("defaultFont", font);
        UIManager.put("Tree.rowHeight", Math.max(22, font.getSize() + 10));
        UIManager.put("Table.rowHeight", Math.max(22, font.getSize() + 8));
        for (Window window : Window.getWindows()) {
            SwingUtilities.updateComponentTreeUI(window);
        }
    }
}
