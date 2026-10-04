package dev.zoroaster1x.vlcskin.app.chrome;

import dev.zoroaster1x.vlcskin.app.Studio;
import dev.zoroaster1x.vlcskin.app.i18n.Messages;
import dev.zoroaster1x.vlcskin.app.theme.ThemeManager;
import dev.zoroaster1x.vlcskin.util.Platform;
import dev.zoroaster1x.vlcskin.model.item.Item;
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
        file.add(shortcut(actions, item(Messages.get("MENU_FILE_NEW", "New"), "new", 0,
                e -> actions.newSkin()), "file.new"));
        file.add(shortcut(actions, item(Messages.get("MENU_FILE_OPEN", "Open..."), "open", 0,
                e -> actions.openSkin()), "file.open"));
        JMenu recent = new JMenu(Messages.get("MENU_FILE_RECENT", "Recent files"));
        java.util.List<String> recents = actions.recentFiles();
        if (recents == null || recents.isEmpty()) {
            JMenuItem none = new JMenuItem(Messages.get("APP_WELCOME_NO_RECENT", "No recent files yet"));
            none.setEnabled(false);
            recent.add(none);
        } else {
            for (String path : recents) {
                JMenuItem entry = new JMenuItem(new java.io.File(path).getName());
                entry.setToolTipText(path);
                entry.addActionListener(e -> actions.openRecent(path));
                recent.add(entry);
            }
        }
        file.add(recent);
        file.add(shortcut(actions, item(Messages.get("MENU_FILE_SAVE", "Save"), "save", 0,
                e -> actions.save()), "file.save"));
        file.add(item("Save as...", null, 0, e -> actions.saveAs()));
        file.addSeparator();
        file.add(item("Import VLT...", null, 0, e -> actions.importVlt()));
        file.add(shortcut(actions, item(Messages.get("APP_GALLERY_MENU", "Browse themes..."), "open", 0,
                e -> actions.browseThemes()), "file.browse"));
        file.add(shortcut(actions, item(Messages.get("MENU_FILE_VLT", "Export as VLT..."), null, 0,
                e -> actions.exportVlt()), "file.exportVlt"));
        file.add(item(Messages.get("MENU_FILE_PNG", "Save current preview as image..."), "image", 0,
                e -> actions.renderPreview()));
        file.addSeparator();
        file.add(shortcut(actions, item(Messages.get("MENU_FILE_TEST", "Test skin in VLC"), "play", 0,
                e -> actions.testInVlc()), "file.testVlc"));
        file.addSeparator();
        file.add(item(Messages.get("MENU_FILE_EXIT", "Exit"), null, 0, e -> actions.exit()));
        bar.add(file);

        JMenu edit = new JMenu(Messages.get("MENU_EDIT", "Edit"));
        edit.setMnemonic(KeyEvent.VK_E);
        edit.add(shortcut(actions, item(Messages.get("MENU_EDIT_UNDO", "Undo"), "undo", 0,
                e -> actions.undo()), "edit.undo"));
        edit.add(shortcut(actions, item(Messages.get("MENU_EDIT_REDO", "Redo"), "redo", 0,
                e -> actions.redo()), "edit.redo"));
        edit.addSeparator();
        edit.add(shortcut(actions, item(Messages.get("MENU_EDIT_THEME", "Skin settings"), "layout", 0,
                e -> actions.openSettings()), "edit.skinSettings"));
        edit.add(shortcut(actions, item(Messages.get("MENU_EDIT_VARS", "Global variables"), "checkbox", 0,
                e -> actions.showVariables()), "edit.variables"));
        edit.add(item(Messages.get("MENU_EDIT_PREFS", "Preferences"), null, 0,
                e -> openPreferences(actions)));
        edit.addSeparator();
        edit.add(shortcut(actions, item("Duplicate item", "duplicate", 0,
                e -> actions.duplicate()), "edit.duplicate"));
        edit.add(shortcut(actions, item("Delete item", "delete", 0,
                e -> actions.deleteSelected()), "edit.delete"));
        edit.addSeparator();
        edit.add(shortcut(actions, item(Messages.get("MENU_EDIT_UP", "Move selected item up"), "up", 0,
                e -> actions.moveSelected(0, -1)), "item.moveUp"));
        edit.add(shortcut(actions, item(Messages.get("MENU_EDIT_DOWN", "Move selected item down"), "down", 0,
                e -> actions.moveSelected(0, 1)), "item.moveDown"));
        edit.add(shortcut(actions, item(Messages.get("MENU_EDIT_LEFT", "Move selected item left"), null, 0,
                e -> actions.moveSelected(-1, 0)), "item.moveLeft"));
        edit.add(shortcut(actions, item(Messages.get("MENU_EDIT_RIGHT", "Move selected item right"), null, 0,
                e -> actions.moveSelected(1, 0)), "item.moveRight"));
        bar.add(edit);

        JMenu view = new JMenu("View");
        view.setMnemonic(KeyEvent.VK_V);
        view.add(shortcut(actions, item("Zoom in", "zoom-in", 0, e -> actions.zoomIn()), "view.zoomIn"));
        view.add(shortcut(actions, item("Zoom out", "zoom-out", 0, e -> actions.zoomOut()), "view.zoomOut"));
        view.add(shortcut(actions, item("Fit window", "grid", 0, e -> actions.fitToWindow()), "view.fit"));
        view.addSeparator();
        JCheckBoxMenuItem checkerboard = new JCheckBoxMenuItem("Checkerboard",
                actions.studio().settings().isCheckerboard());
        checkerboard.addActionListener(e -> {
            actions.toggleCheckerboard();
            checkerboard.setSelected(actions.studio().settings().isCheckerboard());
        });
        view.add(checkerboard);
        JMenu canvasBackground = new JMenu(Messages.get("APP_PREFS_CANVAS_BG", "Canvas background"));
        java.util.Map<String, String> backgrounds = new java.util.LinkedHashMap<>();
        backgrounds.put("theme", Messages.get("APP_PREFS_CANVAS_THEME", "Follow the theme"));
        backgrounds.put("light", Messages.get("APP_PREFS_CANVAS_LIGHT", "Light"));
        backgrounds.put("dark", Messages.get("APP_PREFS_CANVAS_DARK", "Dark"));
        for (var entry : backgrounds.entrySet()) {
            JCheckBoxMenuItem choice = new JCheckBoxMenuItem(entry.getValue(),
                    entry.getKey().equals(actions.studio().settings().getCanvasBackground()));
            choice.addActionListener(e -> {
                setCanvasBackground(actions, entry.getKey());
                for (int index = 0; index < canvasBackground.getItemCount(); index++) {
                    JMenuItem item = canvasBackground.getItem(index);
                    if (item instanceof JCheckBoxMenuItem box) {
                        box.setSelected(box == choice);
                    }
                }
            });
            canvasBackground.add(choice);
        }
        view.add(canvasBackground);
        JMenu panels = new JMenu("Panels");
        for (var entry : java.util.List.of(
                java.util.Map.entry("resources", dev.zoroaster1x.vlcskin.app.i18n.PanelTitles.resources()),
                java.util.Map.entry("structure", dev.zoroaster1x.vlcskin.app.i18n.PanelTitles.windows()),
                java.util.Map.entry("items", dev.zoroaster1x.vlcskin.app.i18n.PanelTitles.items()),
                java.util.Map.entry("canvas", dev.zoroaster1x.vlcskin.app.i18n.PanelTitles.canvas()),
                java.util.Map.entry("inspector", dev.zoroaster1x.vlcskin.app.i18n.PanelTitles.inspector()),
                java.util.Map.entry("variables", dev.zoroaster1x.vlcskin.app.i18n.PanelTitles.variables()),
                java.util.Map.entry("problems", dev.zoroaster1x.vlcskin.app.i18n.PanelTitles.problems()),
                java.util.Map.entry("mcp", dev.zoroaster1x.vlcskin.app.i18n.PanelTitles.mcp()),
                java.util.Map.entry("xml", dev.zoroaster1x.vlcskin.app.i18n.PanelTitles.xml()))) {
            JMenuItem panelItem = new JMenuItem(entry.getValue());
            panelItem.addActionListener(e -> actions.showPanel(entry.getKey()));
            panels.add(panelItem);
        }
        view.add(panels);
        view.add(item("Reset panel layout", null, 0, e -> actions.resetLayout()));
        view.addSeparator();
        view.add(item(Messages.get("TOOLBAR_MOVE", "Move tool"), "move", 0,
                e -> actions.setTool(dev.zoroaster1x.vlcskin.app.panel.CanvasPanel.Tool.MOVE)));
        view.add(item(Messages.get("TOOLBAR_PATH", "Path tool"), "path", 0,
                e -> actions.setTool(dev.zoroaster1x.vlcskin.app.panel.CanvasPanel.Tool.PATH)));
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
        JMenuItem documentation = item(Messages.get("MENU_HELP_DOCS", "Documentation"), "help", 0,
                e -> actions.browseDocumentation());
        documentation.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_F1, 0));
        help.add(documentation);
        help.add(item(Messages.get("MENU_HELP_DOC", "Online help"), "help", 0,
                e -> browse("https://www.videolan.org/vlc/skinedhlp/")));
        help.add(item("Check for updates", null, 0, e -> openUpdateCheck(actions)));
        JCheckBoxMenuItem autoUpdate = new JCheckBoxMenuItem("Check for updates on startup",
                actions.studio().settings().isAutoUpdate());
        autoUpdate.addActionListener(e -> {
            actions.studio().settings().setAutoUpdate(autoUpdate.isSelected());
            actions.studio().saveSettings();
        });
        help.add(autoUpdate);
        help.add(item(Messages.get("MENU_HELP_ABOUT", "About"), "info", 0,
                e -> new dev.zoroaster1x.vlcskin.app.dialog.AboutDialog().setVisible(true)));
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

    private static JMenuItem shortcut(ChromeActions actions, JMenuItem menuItem, String id) {
        menuItem.setAccelerator(dev.zoroaster1x.vlcskin.app.config.Keymap.keyStroke(
                actions.keybindings(), id));
        return menuItem;
    }

    /**
     * Applies and stores a canvas backdrop choice, then repaints the canvas.
     */
    private static void setCanvasBackground(ChromeActions actions, String value) {
        actions.studio().settings().setCanvasBackground(value);
        actions.studio().saveSettings();
        actions.studio().session().fireChanged();
    }

    private static JMenuItem item(String text, String icon, int acceleratorKey, ActionListener listener) {
        JMenuItem menuItem = new JMenuItem(text);
        if (icon != null) {
            menuItem.setIcon(dev.zoroaster1x.vlcskin.app.component.Icons.of(icon, 14));
        }
        if (acceleratorKey != 0) {
            menuItem.setAccelerator(KeyStroke.getKeyStroke(acceleratorKey,
                    menuShortcutMask()));
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

    /**
     * The platform menu shortcut. A headless toolkit throws instead of
     * answering, and the tests build the same menu bar offscreen.
     */
    private static int menuShortcutMask() {
        if (java.awt.GraphicsEnvironment.isHeadless()) {
            return Platform.isMac() ? KeyEvent.META_DOWN_MASK : KeyEvent.CTRL_DOWN_MASK;
        }
        return Toolkit.getDefaultToolkit().getMenuShortcutKeyMaskEx();
    }

    /**
     * The update check needs the window for the dialog and its exit hook; the
     * headless host only gets the background request.
     */
    private static void openUpdateCheck(ChromeActions actions) {
        if (actions instanceof dev.zoroaster1x.vlcskin.app.StudioFrame frame) {
            frame.openUpdateCheck();
        } else {
            actions.studio().checkForUpdates(null, null, true);
        }
    }

    /**
     * Preferences need the host window for the live toolbar callback; the
     * headless host gets the plain dialog.
     */
    private static void openPreferences(ChromeActions actions) {
        if (actions instanceof dev.zoroaster1x.vlcskin.app.StudioFrame frame) {
            frame.openPreferences();
        } else {
            new dev.zoroaster1x.vlcskin.app.dialog.PreferencesDialog(actions.studio()).setVisible(true);
        }
    }
}
