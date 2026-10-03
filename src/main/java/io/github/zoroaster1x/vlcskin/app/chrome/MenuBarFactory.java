package io.github.zoroaster1x.vlcskin.app.chrome;

import io.github.zoroaster1x.vlcskin.app.Studio;
import io.github.zoroaster1x.vlcskin.app.i18n.Messages;
import io.github.zoroaster1x.vlcskin.app.theme.ThemeManager;
import io.github.zoroaster1x.vlcskin.model.item.Item;
import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.Toolkit;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.util.function.BooleanSupplier;
import javax.swing.BorderFactory;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.KeyStroke;

/**
 * Builds the application menu bar; shared by the desktop window and screenshots.
 */
public final class MenuBarFactory {

    /**
     * Theme switching as the menu sees it.
     */
    public interface ThemeControl {
        void apply(String id);

        boolean isDark();
    }

    private MenuBarFactory() {
    }

    public static JMenuBar build(ChromeActions actions, ThemeControl themes) {
        JMenuBar bar = new JMenuBar();
        bar.setAlignmentX(java.awt.Component.LEFT_ALIGNMENT);

        JMenu file = new JMenu(Messages.get("MENU_FILE", "File"));
        file.setMnemonic(KeyEvent.VK_F);
        file.add(item(Messages.get("MENU_FILE_NEW", "New"), "new", KeyEvent.VK_N, e -> actions.newSkin()));
        file.add(item(Messages.get("MENU_FILE_OPEN", "Open..."), "open", KeyEvent.VK_O, e -> actions.openSkin()));
        file.add(item(Messages.get("MENU_FILE_SAVE", "Save"), "save", KeyEvent.VK_S, e -> actions.save()));
        file.add(item("Save as...", null, 0, e -> actions.saveAs()));
        file.addSeparator();
        file.add(item("Import VLT...", null, 0, e -> actions.importVlt()));
        JMenuItem exportVlt = item(Messages.get("MENU_FILE_VLT", "Export as VLT..."), null, 0, e -> actions.exportVlt());
        exportVlt.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_V,
                Toolkit.getDefaultToolkit().getMenuShortcutKeyMaskEx() | KeyEvent.SHIFT_DOWN_MASK));
        file.add(exportVlt);
        file.add(item(Messages.get("MENU_FILE_PNG", "Save current preview as image..."), "image", 0,
                e -> actions.renderPreview()));
        file.addSeparator();
        JMenuItem testVlc = item(Messages.get("MENU_FILE_TEST", "Test skin in VLC"), "play", 0, e -> actions.testInVlc());
        testVlc.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_T,
                Toolkit.getDefaultToolkit().getMenuShortcutKeyMaskEx() | KeyEvent.SHIFT_DOWN_MASK));
        file.add(testVlc);
        file.addSeparator();
        file.add(item(Messages.get("MENU_FILE_EXIT", "Exit"), null, 0, e -> actions.exit()));
        bar.add(file);

        JMenu edit = new JMenu(Messages.get("MENU_EDIT", "Edit"));
        edit.setMnemonic(KeyEvent.VK_E);
        edit.add(item(Messages.get("MENU_EDIT_UNDO", "Undo"), "undo", KeyEvent.VK_Z, e -> actions.undo()));
        edit.add(item(Messages.get("MENU_EDIT_REDO", "Redo"), "redo", KeyEvent.VK_Y, e -> actions.redo()));
        edit.addSeparator();
        edit.add(item(Messages.get("MENU_EDIT_THEME", "Skin settings"), "layout", KeyEvent.VK_I, e -> actions.openSettings()));
        edit.add(item(Messages.get("MENU_EDIT_VARS", "Global variables"), "checkbox", KeyEvent.VK_G,
                e -> actions.showVariables()));
        edit.add(item(Messages.get("MENU_EDIT_PREFS", "Preferences"), null, 0,
                e -> openPreferences(actions)));
        edit.addSeparator();
        edit.add(item("Duplicate item", "duplicate", KeyEvent.VK_D, e -> actions.duplicate()));
        JMenuItem delete = item("Delete item", "delete", 0, e -> actions.deleteSelected());
        delete.setAccelerator(isMac()
                ? KeyStroke.getKeyStroke(KeyEvent.VK_BACK_SPACE, Toolkit.getDefaultToolkit().getMenuShortcutKeyMaskEx())
                : KeyStroke.getKeyStroke(KeyEvent.VK_DELETE, 0));
        edit.add(delete);
        edit.addSeparator();
        edit.add(item(Messages.get("MENU_EDIT_UP", "Move selected item up"), "up", 0,
                e -> actions.moveSelected(0, -1)));
        edit.add(item(Messages.get("MENU_EDIT_DOWN", "Move selected item down"), "down", 0,
                e -> actions.moveSelected(0, 1)));
        edit.add(item(Messages.get("MENU_EDIT_LEFT", "Move selected item left"), null, 0,
                e -> actions.moveSelected(-1, 0)));
        edit.add(item(Messages.get("MENU_EDIT_RIGHT", "Move selected item right"), null, 0,
                e -> actions.moveSelected(1, 0)));
        bar.add(edit);

        JMenu view = new JMenu("View");
        view.setMnemonic(KeyEvent.VK_V);
        view.add(item("Zoom in", "zoom-in", KeyEvent.VK_EQUALS, e -> actions.zoomIn()));
        view.add(item("Zoom out", "zoom-out", KeyEvent.VK_MINUS, e -> actions.zoomOut()));
        view.add(item("Fit window", "grid", 0, e -> actions.fitToWindow()));
        view.addSeparator();
        JCheckBoxMenuItem checkerboard = new JCheckBoxMenuItem("Checkerboard",
                actions.studio().settings().isCheckerboard());
        checkerboard.addActionListener(e -> {
            actions.toggleCheckerboard();
            checkerboard.setSelected(actions.studio().settings().isCheckerboard());
        });
        view.add(checkerboard);
        view.addSeparator();
        view.add(item(Messages.get("TOOLBAR_MOVE", "Move tool"), "move", 0,
                e -> actions.setTool(io.github.zoroaster1x.vlcskin.app.panel.CanvasPanel.Tool.MOVE)));
        view.add(item(Messages.get("TOOLBAR_PATH", "Path tool"), "path", 0,
                e -> actions.setTool(io.github.zoroaster1x.vlcskin.app.panel.CanvasPanel.Tool.PATH)));
        view.addSeparator();
        JCheckBoxMenuItem dark = new JCheckBoxMenuItem("Dark theme", themes.isDark());
        dark.addActionListener(e -> themes.apply(themes.isDark() ? "light" : "dark"));
        view.add(dark);
        JMenu themeMenu = new JMenu("Theme");
        for (ThemeManager.Theme theme : ThemeManager.themes().values()) {
            JMenuItem themeItem = new JMenuItem(theme.label());
            themeItem.addActionListener(e -> themes.apply(theme.id()));
            themeMenu.add(themeItem);
        }
        view.add(themeMenu);
        bar.add(view);

        JMenu help = new JMenu(Messages.get("MENU_HELP", "Help"));
        help.setMnemonic(KeyEvent.VK_H);
        JMenuItem onlineHelp = item(Messages.get("MENU_HELP_DOC", "Online help"), "help", 0,
                e -> browse("https://www.videolan.org/vlc/skinedhlp/"));
        onlineHelp.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_F1, 0));
        help.add(onlineHelp);
        help.add(item("Check for updates", null, 0,
                e -> browse("https://github.com/zoroaster1x/vlc-skin-editor/releases")));
        JCheckBoxMenuItem autoUpdate = new JCheckBoxMenuItem("Check for updates on startup",
                actions.studio().settings().isAutoUpdate());
        autoUpdate.addActionListener(e -> {
            actions.studio().settings().setAutoUpdate(autoUpdate.isSelected());
            actions.studio().saveSettings();
        });
        help.add(autoUpdate);
        help.add(item(Messages.get("MENU_HELP_ABOUT", "About"), "info", 0,
                e -> new io.github.zoroaster1x.vlcskin.app.dialog.AboutDialog().setVisible(true)));
        bar.add(help);
        return bar;
    }

    public static void refreshUndoLabels(javax.swing.JMenuBar bar, Studio studio, String undoKey, String redoKey) {
        String undo = studio.session().history().undoDescription();
        String redo = studio.session().history().redoDescription();
        for (int menuIndex = 0; menuIndex < bar.getMenuCount(); menuIndex++) {
            JMenu menu = bar.getMenu(menuIndex);
            if (menu == null) {
                continue;
            }
            for (int itemIndex = 0; itemIndex < menu.getItemCount(); itemIndex++) {
                JMenuItem item = menu.getItem(itemIndex);
                if (item == null) {
                    continue;
                }
                String text = item.getText();
                if (text != null && text.startsWith(undoKey)) {
                    item.setText(undo == null ? undoKey : undoKey + ": " + undo);
                    item.setEnabled(undo != null);
                } else if (text != null && text.startsWith(redoKey)) {
                    item.setText(redo == null ? redoKey : redoKey + ": " + redo);
                    item.setEnabled(redo != null);
                }
            }
        }
    }

    private static JMenuItem item(String text, String icon, int acceleratorKey, ActionListener listener) {
        JMenuItem menuItem = new JMenuItem(text);
        if (icon != null) {
            menuItem.setIcon(io.github.zoroaster1x.vlcskin.app.component.Icons.of(icon, 14));
        }
        if (acceleratorKey != 0) {
            menuItem.setAccelerator(KeyStroke.getKeyStroke(acceleratorKey,
                    Toolkit.getDefaultToolkit().getMenuShortcutKeyMaskEx()));
        }
        menuItem.addActionListener(listener);
        return menuItem;
    }

    private static void browse(String url) {
        try {
            java.awt.Desktop.getDesktop().browse(java.net.URI.create(url));
        } catch (Exception ex) {
            // Opening the browser is best effort.
        }
    }

    private static boolean isMac() {
        return System.getProperty("os.name", "").toLowerCase(java.util.Locale.ROOT).contains("mac");
    }

    /**
     * Preferences need the host window for the live toolbar callback; the
     * headless host gets the plain dialog.
     */
    private static void openPreferences(ChromeActions actions) {
        if (actions instanceof io.github.zoroaster1x.vlcskin.app.StudioFrame frame) {
            frame.openPreferences();
        } else {
            new io.github.zoroaster1x.vlcskin.app.dialog.PreferencesDialog(actions.studio()).setVisible(true);
        }
    }
}
