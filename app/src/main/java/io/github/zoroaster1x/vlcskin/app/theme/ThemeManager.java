package io.github.zoroaster1x.vlcskin.app.theme;

import com.formdev.flatlaf.FlatDarkLaf;
import com.formdev.flatlaf.FlatLaf;
import com.formdev.flatlaf.FlatLightLaf;
import com.formdev.flatlaf.intellijthemes.FlatArcDarkIJTheme;
import com.formdev.flatlaf.intellijthemes.FlatArcIJTheme;
import com.formdev.flatlaf.intellijthemes.FlatOneDarkIJTheme;
import java.awt.Color;
import java.util.LinkedHashMap;
import java.util.Map;
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
        UIManager.put("Tree.rowHeight", 22);
        UIManager.put("ToolTip.background", UIManager.getColor("Panel.background"));
        UIManager.put("ToolTip.border", javax.swing.BorderFactory.createLineBorder(
                UIManager.getColor("Component.borderColor")));
        UIManager.put("TitlePane.unifiedBackground", true);
        FlatLaf.updateUI();
    }
}
