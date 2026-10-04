package dev.zoroaster1x.vlcskin.app;

import dev.zoroaster1x.vlcskin.Version;
import dev.zoroaster1x.vlcskin.app.chrome.ChromeActions;
import dev.zoroaster1x.vlcskin.app.chrome.MenuBarFactory;
import dev.zoroaster1x.vlcskin.app.chrome.StatusBar;
import dev.zoroaster1x.vlcskin.app.chrome.ToolBarFactory;
import dev.zoroaster1x.vlcskin.app.config.StudioSettings;
import dev.zoroaster1x.vlcskin.app.dialog.AboutDialog;
import dev.zoroaster1x.vlcskin.app.dialog.PreferencesDialog;
import dev.zoroaster1x.vlcskin.app.dialog.ThemeSettingsDialog;
import dev.zoroaster1x.vlcskin.app.i18n.Messages;
import dev.zoroaster1x.vlcskin.app.panel.CanvasPanel;
import dev.zoroaster1x.vlcskin.app.panel.Panels;
import dev.zoroaster1x.vlcskin.app.snapshot.SwingUiInspector;
import dev.zoroaster1x.vlcskin.edit.ValueCommand;
import dev.zoroaster1x.vlcskin.model.SkinLayout;
import dev.zoroaster1x.vlcskin.model.SkinWindow;
import dev.zoroaster1x.vlcskin.model.item.Item;
import dev.zoroaster1x.vlcskin.model.resource.Resource;
import java.awt.BorderLayout;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.function.Consumer;
import javax.swing.AbstractAction;
import javax.swing.JComponent;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JMenuBar;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JToolBar;
import javax.swing.KeyStroke;
import javax.swing.SwingUtilities;
import javax.swing.filechooser.FileNameExtensionFilter;
import io.github.andrewauclair.moderndocking.DockableStyle;
import io.github.andrewauclair.moderndocking.DockingRegion;
import io.github.andrewauclair.moderndocking.app.Docking;
import io.github.andrewauclair.moderndocking.app.DockingState;
import io.github.andrewauclair.moderndocking.app.LayoutPersistence;
import io.github.andrewauclair.moderndocking.app.RootDockingPanel;
import io.github.andrewauclair.moderndocking.ext.ui.DockingUI;
import io.github.andrewauclair.moderndocking.ui.DefaultDockingPanel;

/**
 * The desktop window: chrome, dockable panels and the status bar.
 */
public final class StudioFrame extends JFrame implements ChromeActions {

    private final Studio studio;
    private final Panels panels;
    private final JToolBar toolbar;
    private JFrame toolbarHolder;
    private final StatusBar statusBar = new StatusBar();
    private final MenuBarFactory.ThemeControl themeControl = new MenuBarFactory.ThemeControl() {
        @Override
        public void apply(String id) {
            studio.applyTheme(id);
            SwingUtilities.updateComponentTreeUI(StudioFrame.this);
            setJMenuBar(MenuBarFactory.build(StudioFrame.this, this));
            applyChromeState();
            studio.session().fireChanged();
        }

        @Override
        public boolean isDark() {
            return studio.isDarkTheme();
        }
    };

    public StudioFrame(Studio studio) {
        super(Version.NAME);
        this.studio = studio;
        Messages.setLanguage(studio.settings().getLanguage());
        this.panels = new Panels(studio);
        setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);

        setJMenuBar(MenuBarFactory.build(this, themeControl));
        toolbar = ToolBarFactory.build(this);
        restoreToolbar(studio.settings());
        // ModernDocking computes the split sizes from the window size, so the
        // geometry has to be in place before the panels are docked.
        applyWindowGeometry();

        Docking.initialize(this);
        DockingUI.initialize();
        RootDockingPanel root = new RootDockingPanel(this);
        // ModernDocking tracks the frame's content pane, so the panels are added
        // to it instead of replacing it with a wrapper.
        java.awt.Container content = getContentPane();
        content.setLayout(new java.awt.BorderLayout());
        content.add(root, java.awt.BorderLayout.CENTER);
        if (toolbar.getParent() == null) {
            content.add(toolbar, studio.settings().getToolbarOrientation() == JToolBar.VERTICAL
                    ? java.awt.BorderLayout.WEST : java.awt.BorderLayout.NORTH);
        }
        content.add(statusBar, java.awt.BorderLayout.SOUTH);

        registerDockable("resources", dev.zoroaster1x.vlcskin.app.i18n.PanelTitles.resources(), panels.resources);
        registerDockable("structure", dev.zoroaster1x.vlcskin.app.i18n.PanelTitles.windows(), panels.structure);
        registerDockable("items", dev.zoroaster1x.vlcskin.app.i18n.PanelTitles.items(), panels.items);
        registerDockable("canvas", dev.zoroaster1x.vlcskin.app.i18n.PanelTitles.canvas(), panels.canvas);
        registerDockable("inspector", dev.zoroaster1x.vlcskin.app.i18n.PanelTitles.inspector(), panels.inspector);
        registerDockable("variables", dev.zoroaster1x.vlcskin.app.i18n.PanelTitles.variables(), panels.variables);
        registerDockable("problems", dev.zoroaster1x.vlcskin.app.i18n.PanelTitles.problems(), panels.problems);
        registerDockable("xml", dev.zoroaster1x.vlcskin.app.i18n.PanelTitles.xml(), panels.xml);

        dockDefaultLayout();
        restoreLayout();
        installShortcuts();

        studio.addStatusListener(statusBar::setMessage);
        studio.service().setUi(new SwingUiInspector(this::getContentPane, studio, "Desktop window",
                () -> panels.canvas, this::showPanelByName));
        studio.session().addListener(this::onSessionChanged);

        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                exit();
            }
        });
        onSessionChanged();
    }

    private void onSessionChanged() {
        panels.canvas.refreshBackdrop();
        statusBar.update(studio);
        MenuBarFactory.refreshUndoLabels(getJMenuBar(), studio, "Undo", "Redo");
        applyToolbarVisibility();
        ToolBarFactory.syncUndoButtons(toolbar,
                studio.session().history().canUndo(), studio.session().history().canRedo());
        String name = studio.session().file() == null ? "Untitled"
                : studio.session().file().getFileName().toString();
        setTitle((studio.session().isDirty() ? "*" : "") + name + " - " + Version.NAME);
    }

    private void applyChromeState() {
        applyToolbarVisibility();
        ToolBarFactory.syncToolButtons(toolbar, panels.canvas.tool());
    }

    private void applyToolbarVisibility() {
        boolean visible = studio.settings().isShowToolbar();
        toolbar.setVisible(visible);
        if (toolbarHolder != null) {
            toolbarHolder.setVisible(visible);
        }
    }

    /**
     * Restores the toolbar orientation, visibility and floating state.
     */
    private void restoreToolbar(StudioSettings settings) {
        toolbar.setFloatable(true);
        toolbar.setOrientation(settings.getToolbarOrientation() == 1
                ? JToolBar.VERTICAL : JToolBar.HORIZONTAL);
        toolbar.setVisible(settings.isShowToolbar());
        if (settings.isToolbarFloating()) {
            toolbarHolder = new JFrame(Version.NAME + " toolbar");
            toolbarHolder.setIconImage(getIconImage());
            toolbarHolder.add(toolbar, BorderLayout.CENTER);
            toolbarHolder.pack();
            if (settings.getToolbarX() >= 0 && settings.getToolbarY() >= 0) {
                toolbarHolder.setLocation(settings.getToolbarX(), settings.getToolbarY());
            } else {
                toolbarHolder.setLocationRelativeTo(null);
            }
            toolbarHolder.setVisible(settings.isShowToolbar());
        }
    }

    private void registerDockable(String id, String title, JComponent content) {
        DefaultDockingPanel dockable = new DockablePanel(id, title);
        dockable.setStyle(DockableStyle.BOTH);
        dockable.setMinMaxAllowed(true);
        dockable.setLayout(new BorderLayout());
        content.putClientProperty("panelName", title);
        dockable.add(content, BorderLayout.CENTER);
        dockable.addMoreOptions(panelMenu(id, title));
        javax.swing.JPopupMenu contextMenu = panelMenu(id, title);
        dockable.setComponentPopupMenu(contextMenu);
        Docking.registerDockable(dockable);
    }

    /**
     * The right click menu of a panel: hide it, float it, or restore the
     * default layout. The View menu can always bring a hidden panel back.
     */
    private javax.swing.JPopupMenu panelMenu(String id, String title) {
        javax.swing.JPopupMenu menu = new javax.swing.JPopupMenu();
        javax.swing.JMenuItem hide = new javax.swing.JMenuItem("Hide " + title);
        hide.addActionListener(e -> {
            io.github.andrewauclair.moderndocking.Dockable dockable = dockable(id);
            if (dockable != null) {
                Docking.minimize(dockable);
            }
        });
        javax.swing.JMenuItem floatWindow = new javax.swing.JMenuItem("Float " + title);
        floatWindow.addActionListener(e -> Docking.newWindow(id));
        javax.swing.JMenuItem show = new javax.swing.JMenuItem("Show " + title);
        show.addActionListener(e -> {
            Docking.display(id);
            Docking.bringToFront(id);
        });
        javax.swing.JMenuItem reset = new javax.swing.JMenuItem("Reset panel layout");
        reset.addActionListener(e -> resetLayout());
        menu.add(show);
        menu.add(hide);
        menu.add(floatWindow);
        menu.addSeparator();
        menu.add(reset);
        return menu;
    }

    /**
     * The registered dockable with this id, or null.
     */
    private static io.github.andrewauclair.moderndocking.Dockable dockable(String id) {
        return Docking.getDockables().stream()
                .filter(dockable -> id.equals(dockable.getPersistentID()))
                .findFirst()
                .orElse(null);
    }

    /**
     * ModernDocking puts every panel in a scroll pane; tracking the viewport
     * stops the outer scrollbars and lets the trees scroll themselves.
     */
    private static final class DockablePanel extends DefaultDockingPanel
            implements javax.swing.Scrollable {

        DockablePanel(String id, String title) {
            super(id, title);
        }

        @Override
        public java.awt.Dimension getPreferredScrollableViewportSize() {
            return getPreferredSize();
        }

        @Override
        public int getScrollableUnitIncrement(java.awt.Rectangle visible, int orientation, int direction) {
            return 16;
        }

        @Override
        public int getScrollableBlockIncrement(java.awt.Rectangle visible, int orientation, int direction) {
            return Math.max(16, orientation == javax.swing.SwingConstants.VERTICAL
                    ? visible.height : visible.width);
        }

        @Override
        public boolean getScrollableTracksViewportWidth() {
            return true;
        }

        @Override
        public boolean getScrollableTracksViewportHeight() {
            return true;
        }
    }


    private void installShortcuts() {
        JComponent root = getRootPane();
        bind(root, "DELETE", "delete-item", e -> deleteSelected());
        if (isMac()) {
            bind(root, "meta BACK_SPACE", "delete-item-backspace", e -> deleteSelected());
        }
        bind(root, "control UP", "move-up", e -> moveSelected(0, -1));
        bind(root, "control DOWN", "move-down", e -> moveSelected(0, 1));
        bind(root, "control LEFT", "move-left", e -> moveSelected(-1, 0));
        bind(root, "control RIGHT", "move-right", e -> moveSelected(1, 0));
        bind(root, "control EQUALS", "zoom-in", e -> zoomIn());
        bind(root, "control MINUS", "zoom-out", e -> zoomOut());
        bind(root, "control 0", "fit", e -> fitToWindow());
        bind(root, "control D", "duplicate", e -> duplicate());
    }

    private static boolean isMac() {
        return System.getProperty("os.name", "").toLowerCase(java.util.Locale.ROOT).contains("mac");
    }

    private void bind(JComponent component, String keyStroke, String name, Consumer<ActionEvent> action) {
        component.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke(keyStroke), name);
        component.getActionMap().put(name, new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                action.accept(e);
            }
        });
    }


    @Override
    public void newSkin() {
        studio.newSkin(getContentPane());
    }

    @Override
    public void openSkin() {
        studio.openDialog(getContentPane());
    }

    @Override
    public void save() {
        studio.save();
    }

    @Override
    public void saveAs() {
        studio.saveAsDialog(getContentPane());
    }

    @Override
    public void importVlt() {
        var chooser = studio.chooser(getContentPane(), "Import VLT",
                new FileNameExtensionFilter("VLC theme (*.vlt, *.zip)", "vlt", "zip"));
        if (chooser.showOpenDialog(getContentPane()) == JFileChooser.APPROVE_OPTION) {
            studio.importVlt(getContentPane(), chooser.getSelectedFile().toPath());
        }
    }

    @Override
    public void exportVlt() {
        studio.exportVltDialog(getContentPane());
    }

    @Override
    public void renderPreview() {
        studio.renderPreviewDialog(getContentPane());
    }

    @Override
    public void testInVlc() {
        if (studio.session().file() == null) {
            studio.saveAsDialog(getContentPane());
        } else {
            studio.save();
        }
        Path skin = studio.session().file();
        if (skin == null) {
            return;
        }
        try {
            Path archive = Files.createTempFile("vlc-skin-", ".vlt");
            dev.zoroaster1x.vlcskin.format.VltCodec.write(
                    studio.session().theme(), skin, archive);
            Path installed = dev.zoroaster1x.vlcskin.util.VlcFinder.install(archive);
            Files.deleteIfExists(archive);
            dev.zoroaster1x.vlcskin.util.VlcFinder.launch(installed);
            studio.status("Started VLC with " + installed.getFileName());
        } catch (Exception ex) {
            studio.error("Could not start VLC: " + ex.getMessage()
                    + "\nTry: " + dev.zoroaster1x.vlcskin.util.VlcFinder.describeLaunch(skin));
        }
    }

    @Override
    public void undo() {
        studio.session().undo();
    }

    @Override
    public void redo() {
        studio.session().redo();
    }

    @Override
    public void duplicate() {
        Item item = studio.session().selection().item(studio.session().index());
        if (item == null) {
            return;
        }
        String pattern = (String) JOptionPane.showInputDialog(this,
                Messages.get("DUPLICATE_MSG",
                        "Please enter the rename pattern for the duplicated objects.\n"
                                + "%oldid% will be replaced by the old ID of the object."),
                "%oldid%_copy");
        if (pattern == null) {
            return;
        }
        var outcome = studio.service().duplicateItem(item.getId(), pattern);
        if (outcome.error()) {
            studio.error(outcome.text());
        }
        studio.session().fireChanged();
    }

    @Override
    public void deleteSelected() {
        String focus = focusedArea();
        boolean itemsFocused = "items".equals(focus);
        boolean resourcesFocused = "resources".equals(focus);
        boolean structureFocused = "structure".equals(focus);
        Item item = studio.session().selection().item(studio.session().index());
        if (item != null && (itemsFocused || (!resourcesFocused && !structureFocused))) {
            if (!confirmDelete(item.getId())) {
                return;
            }
            var outcome = studio.service().deleteItem(item.getId());
            if (outcome.error()) {
                studio.error(outcome.text());
            } else {
                studio.session().selection().clearItem();
                studio.session().fireChanged();
            }
            return;
        }
        Resource resource = studio.session().selection().resource(studio.session().index());
        if (resource != null && (resourcesFocused || (!itemsFocused && !structureFocused))) {
            if (studio.session().index().isResourceUsed(resource.getId())) {
                JOptionPane.showMessageDialog(this,
                        Messages.get("ERROR_RES_DEL_INUSE",
                                "Resource is still used in the skin, thus it cannot be deleted."),
                        Messages.get("ERROR_RES_DEL_TITLE", "Could not delete resource"),
                        JOptionPane.INFORMATION_MESSAGE);
                return;
            }
            if (!confirmDelete(resource.getId())) {
                return;
            }
            var outcome = studio.service().deleteResource(resource.getId());
            if (outcome.error()) {
                studio.error(outcome.text());
            } else {
                studio.session().selection().selectResource(null);
                studio.session().fireChanged();
            }
            return;
        }
        SkinLayout layout = studio.session().selection().layout(studio.session().index());
        if (layout != null && (structureFocused || (!itemsFocused && !resourcesFocused))) {
            SkinWindow window = studio.session().selection().window(studio.session().index());
            if (window == null) {
                return;
            }
            if (window.getLayouts().size() <= 1) {
                studio.error("A window must keep at least one layout.");
                return;
            }
            if (!confirmDelete(layout.getId())) {
                return;
            }
            int index = window.getLayouts().indexOf(layout);
            studio.session().apply(ValueCommand.builder("Delete layout")
                    .step(() -> window.getLayouts().remove(layout),
                            () -> window.getLayouts().add(index, layout))
                    .build());
            studio.session().selection().selectWindow(window.getId());
            return;
        }
        SkinWindow window = studio.session().selection().window(studio.session().index());
        if (window == null || !(structureFocused || (!itemsFocused && !resourcesFocused))) {
            return;
        }
        if (studio.session().theme().getWindows().size() <= 1) {
            studio.error("A theme must keep at least one window.");
            return;
        }
        if (!confirmDelete(window.getId())) {
            return;
        }
        int index = studio.session().theme().getWindows().indexOf(window);
        studio.session().apply(ValueCommand.builder("Delete window")
                .step(() -> studio.session().theme().getWindows().remove(window),
                        () -> studio.session().theme().getWindows().add(index, window))
                .build());
        studio.session().selection().selectWindow(null);
    }

    private boolean confirmDelete(String id) {
        return JOptionPane.showConfirmDialog(this, "Delete \"" + id + "\"?",
                Messages.get("DEL_CONFIRM_TITLE", "Deletion confirmation"),
                JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION;
    }

    /**
     * Which tree has keyboard focus, so Delete acts on that area.
     */
    private String focusedArea() {
        java.awt.Component focus = java.awt.KeyboardFocusManager.getCurrentKeyboardFocusManager().getFocusOwner();
        if (focus == null) {
            return "";
        }
        if (javax.swing.SwingUtilities.isDescendingFrom(focus, panels.resources)) {
            return "resources";
        }
        if (javax.swing.SwingUtilities.isDescendingFrom(focus, panels.items)) {
            return "items";
        }
        if (javax.swing.SwingUtilities.isDescendingFrom(focus, panels.structure)) {
            return "structure";
        }
        return "";
    }

    @Override
    public void moveSelected(int dx, int dy) {
        Item item = studio.session().selection().item(studio.session().index());
        if (item == null) {
            return;
        }
        int x = item.getX() + dx;
        int y = item.getY() + dy;
        studio.session().apply(ValueCommand.builder("Move " + item.type().displayName())
                .set(item.getX(), x, item::setX)
                .set(item.getY(), y, item::setY)
                .build());
    }

    @Override
    public void zoomIn() {
        panels.canvas.zoomIn();
        applyChromeState();
    }

    @Override
    public void zoomOut() {
        panels.canvas.zoomOut();
        applyChromeState();
    }

    @Override
    public void fitToWindow() {
        panels.canvas.fitToWindow();
        applyChromeState();
    }

    @Override
    public void setTool(CanvasPanel.Tool tool) {
        panels.canvas.setTool(tool);
        ToolBarFactory.syncToolButtons(toolbar, tool);
    }

    @Override
    public void validateSkin() {
        panels.problems.validate();
        studio.status("Validation finished");
    }

    @Override
    public void openSettings() {
        new ThemeSettingsDialog(studio).setVisible(true);
    }

    /**
     * Preferences with a live toolbar visibility callback.
     */
    public void openPreferences() {
        new PreferencesDialog(studio, this::setToolbarVisible).setVisible(true);
    }

    private void setToolbarVisible(boolean visible) {
        toolbar.setVisible(visible);
        if (toolbarHolder != null) {
            toolbarHolder.setVisible(visible);
        }
    }

    @Override
    public void showVariables() {
        Docking.bringToFront("variables");
    }

    /**
     * Brings a dockable panel to the front by its id, showing it again when it
     * was hidden. Menu panels use this.
     */
    @Override
    public void showPanel(String id) {
        if (id == null || id.isBlank()) {
            return;
        }
        Docking.display(id);
        Docking.bringToFront(id);
    }

    /**
     * Focuses a dockable panel by its tool name, for the MCP show_panel tool.
     */
    private boolean showPanelByName(String name) {
        String key = name == null ? "" : name.toLowerCase(java.util.Locale.ROOT).trim();
        String id = switch (key) {
            case "resources", "resource" -> "resources";
            case "structure", "windows", "window", "layouts" -> "structure";
            case "items", "item" -> "items";
            case "canvas", "preview" -> "canvas";
            case "inspector", "properties" -> "inspector";
            case "variables", "globals" -> "variables";
            case "problems", "validation" -> "problems";
            case "xml", "source" -> "xml";
            default -> null;
        };
        if (id == null) {
            return false;
        }
        Docking.bringToFront(id);
        return true;
    }

    @Override
    public void toggleCheckerboard() {
        studio.settings().setCheckerboard(!studio.settings().isCheckerboard());
        studio.session().fireChanged();
    }


    private Path layoutFile() {
        return dev.zoroaster1x.vlcskin.app.config.AppPaths.configDir().resolve("layout.xml");
    }

    private void restoreLayout() {
        try {
            if (Files.exists(layoutFile())) {
                var layout = LayoutPersistence.loadWindowLayoutFromFile(layoutFile().toFile());
                if (layout != null) {
                    DockingState.restoreWindowLayout(this, layout);
                }
            }
        } catch (Throwable ex) {
            // A stale layout file must never stop the window from opening.
        }
    }

    private void saveLayout() {
        try {
            Files.createDirectories(layoutFile().getParent());
            LayoutPersistence.saveWindowLayoutToFile(layoutFile().toFile(),
                    DockingState.getWindowLayout(this));
        } catch (Throwable ex) {
            // Best effort, including a missing docking implementation.
        }
    }

    private void applyWindowGeometry() {
        var settings = studio.settings();
        setSize(settings.getWindowWidth(), settings.getWindowHeight());
        if (settings.getWindowX() >= 0 && settings.getWindowY() >= 0) {
            setLocation(settings.getWindowX(), settings.getWindowY());
        } else {
            setLocationRelativeTo(null);
        }
        if (settings.isWindowMaximized()) {
            setExtendedState(MAXIMIZED_BOTH);
        }
    }

    private void dockDefaultLayout() {
        // The canvas is docked first, so the side panels split around it in the
        // order users expect: resources left, inspector right, canvas centre.
        Docking.dock("canvas", this);
        Docking.dock("resources", "canvas", DockingRegion.WEST, 0.18);
        Docking.dock("inspector", "canvas", DockingRegion.EAST, 0.23);
        Docking.dock("structure", "resources", DockingRegion.SOUTH, 0.35);
        Docking.dock("items", "structure", DockingRegion.SOUTH, 0.55);
        Docking.dock("problems", "canvas", DockingRegion.SOUTH, 0.25);
        Docking.dock("xml", "problems", DockingRegion.CENTER);
        Docking.dock("variables", "inspector", DockingRegion.SOUTH, 0.4);
    }

    @Override
    public void resetLayout() {
        try {
            Files.deleteIfExists(layoutFile());
        } catch (Exception ex) {
            // Best effort; the default layout is applied either way.
        }
        dockDefaultLayout();
        revalidate();
        repaint();
        studio.status("Panel layout reset");
    }

    @Override
    public void browseThemes() {
        new dev.zoroaster1x.vlcskin.app.dialog.ThemeBrowserDialog(studio, this).setVisible(true);
    }

    @Override
    public void browseDocumentation() {
        new dev.zoroaster1x.vlcskin.app.dialog.DocumentationDialog(this).setVisible(true);
    }

    @Override
    public java.util.List<String> recentFiles() {
        return java.util.List.copyOf(studio.settings().getRecentFiles());
    }

    @Override
    public void openRecent(String path) {
        studio.openFile(Path.of(path));
    }

    @Override
    public void exit() {
        if (studio.session().isDirty()) {
            int choice = JOptionPane.showConfirmDialog(this, "Save changes before closing?", Version.NAME,
                    JOptionPane.YES_NO_CANCEL_OPTION);
            if (choice == JOptionPane.CANCEL_OPTION || choice == JOptionPane.CLOSED_OPTION) {
                return;
            }
            if (choice == JOptionPane.YES_OPTION) {
                studio.save();
            }
        }
        var settings = studio.settings();
        settings.setWindowMaximized((getExtendedState() & MAXIMIZED_BOTH) == MAXIMIZED_BOTH);
        if (!settings.isWindowMaximized()) {
            settings.setWindowX(getX());
            settings.setWindowY(getY());
            settings.setWindowWidth(getWidth());
            settings.setWindowHeight(getHeight());
        }
        settings.setToolbarFloating(toolbar.getTopLevelAncestor() != this);
        settings.setToolbarOrientation(toolbar.getOrientation());
        if (toolbarHolder != null && toolbarHolder.isVisible()) {
            settings.setToolbarX(toolbarHolder.getX());
            settings.setToolbarY(toolbarHolder.getY());
        }
        studio.saveSettings();
        try {
            saveLayout();
        } catch (Throwable ex) {
            // A persistence problem must never keep the window from closing.
        }
        dispose();
        System.exit(0);
    }

    public Studio studio() {
        return studio;
    }

    public Panels panels() {
        return panels;
    }
}
