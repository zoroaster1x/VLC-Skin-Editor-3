package dev.zoroaster1x.vlcskin.app;

import dev.zoroaster1x.vlcskin.app.chrome.ChromeActions;
import dev.zoroaster1x.vlcskin.app.chrome.MenuBarFactory;
import dev.zoroaster1x.vlcskin.app.chrome.StatusBar;
import dev.zoroaster1x.vlcskin.app.chrome.ToolBarFactory;
import dev.zoroaster1x.vlcskin.app.i18n.Messages;
import dev.zoroaster1x.vlcskin.app.panel.CanvasPanel;
import dev.zoroaster1x.vlcskin.app.panel.Panels;
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import javax.imageio.ImageIO;
import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JSplitPane;
import javax.swing.JTabbedPane;
import javax.swing.JToolBar;
import javax.swing.SwingUtilities;

/**
 * The whole UI assembled without a window, for tests, screenshots and the MCP
 * UI description. The same panels and the same chrome the desktop window uses
 * are laid out with split panes here, so what is verified is what ships and
 * nothing pops up on screen.
 */
public final class HeadlessStudio extends JPanel implements ChromeActions {

    private final Studio studio;
    private final Panels panels;
    private final JTabbedPane leftTabs = new JTabbedPane();
    private final JTabbedPane rightTabs = new JTabbedPane();
    private final JTabbedPane bottomTabs = new JTabbedPane();
    private final JPanel canvasHost = new JPanel(new java.awt.BorderLayout());
    private final JToolBar toolbar;
    private final StatusBar statusBar = new StatusBar();
    private final MenuBarFactory.ThemeControl themeControl = new MenuBarFactory.ThemeControl() {
        @Override
        public void apply(String id) {
            applyTheme(id);
        }

        @Override
        public boolean isDark() {
            return studio.isDarkTheme();
        }
    };

    public HeadlessStudio(Studio studio) {
        super(new java.awt.BorderLayout());
        this.studio = studio;
        Messages.setLanguage(studio.settings().getLanguage());
        this.panels = new Panels(studio);
        panels.canvas.putClientProperty("panelName", "Canvas");
        panels.resources.putClientProperty("panelName", "Resources");
        panels.structure.putClientProperty("panelName", "Windows and layouts");
        panels.items.putClientProperty("panelName", "Items");
        panels.inspector.putClientProperty("panelName", "Inspector");
        panels.variables.putClientProperty("panelName", "Variables");
        panels.problems.putClientProperty("panelName", "Problems");
        panels.xml.putClientProperty("panelName", "Skin XML");
        panels.ai.putClientProperty("panelName", "AI assistant");

        leftTabs.addTab(dev.zoroaster1x.vlcskin.app.i18n.PanelTitles.resources(), panels.resources);
        leftTabs.addTab(dev.zoroaster1x.vlcskin.app.i18n.PanelTitles.windows(), panels.structure);
        leftTabs.addTab(dev.zoroaster1x.vlcskin.app.i18n.PanelTitles.items(), panels.items);

        rightTabs.addTab(dev.zoroaster1x.vlcskin.app.i18n.PanelTitles.inspector(), panels.inspector);
        rightTabs.addTab(dev.zoroaster1x.vlcskin.app.i18n.PanelTitles.variables(), panels.variables);
        rightTabs.addTab(dev.zoroaster1x.vlcskin.app.i18n.PanelTitles.ai(), panels.ai);

        JTabbedPane bottom = bottomTabs;
        bottom.addTab(dev.zoroaster1x.vlcskin.app.i18n.PanelTitles.problems(), panels.problems);
        bottom.addTab(dev.zoroaster1x.vlcskin.app.i18n.PanelTitles.xml(), panels.xml);

        canvasHost.add(panels.canvas, java.awt.BorderLayout.CENTER);

        JSplitPane middleRight = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, canvasHost, rightTabs);
        middleRight.setDividerLocation(720);
        middleRight.setResizeWeight(0.75);
        JSplitPane centerBottom = new JSplitPane(JSplitPane.VERTICAL_SPLIT, middleRight, bottom);
        centerBottom.setDividerLocation(430);
        centerBottom.setResizeWeight(0.8);
        JSplitPane leftCenter = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, leftTabs, centerBottom);
        leftCenter.setDividerLocation(210);
        leftCenter.setResizeWeight(0.2);
        add(leftCenter, java.awt.BorderLayout.CENTER);

        javax.swing.JMenuBar menuBar = MenuBarFactory.build(this, themeControl);
        toolbar = ToolBarFactory.build(this);
        menuBar.setMaximumSize(new java.awt.Dimension(Integer.MAX_VALUE, menuBar.getPreferredSize().height));
        toolbar.setMaximumSize(new java.awt.Dimension(Integer.MAX_VALUE, toolbar.getPreferredSize().height));
        JPanel chrome = new JPanel();
        chrome.setLayout(new BoxLayout(chrome, BoxLayout.Y_AXIS));
        chrome.add(menuBar);
        chrome.add(toolbar);
        chrome.setBorder(BorderFactory.createEmptyBorder());
        add(chrome, java.awt.BorderLayout.NORTH);
        add(statusBar, java.awt.BorderLayout.SOUTH);

        studio.addStatusListener(statusBar::setMessage);
        studio.session().addListener(() -> {
            statusBar.update(studio);
            MenuBarFactory.refreshUndoLabels(menuBar, studio, "Undo", "Redo");
            ToolBarFactory.syncToolButtons(toolbar, panels.canvas.tool());
            ToolBarFactory.syncUndoButtons(toolbar, studio.session().history().canUndo(),
                    studio.session().history().canRedo());
        });
        studio.service().setUi(new dev.zoroaster1x.vlcskin.app.snapshot.SwingUiInspector(
                () -> this, studio, "Headless studio", () -> panels.canvas, this::showNamedPanel));
        statusBar.update(studio);
    }

    public Panels panels() {
        return panels;
    }

    @Override
    public Studio studio() {
        return studio;
    }

    public CanvasPanel canvas() {
        return panels.canvas;
    }

    /**
     * Lays out and paints the whole UI at the given size.
     */
    public BufferedImage render(int width, int height) {
        setSize(width, height);
        doLayout();
        layoutDeep(this);
        setSize(width, height);
        doLayout();
        BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        Graphics2D g = image.createGraphics();
        try {
            paint(g);
        } finally {
            g.dispose();
        }
        return image;
    }

    public byte[] renderPng(int width, int height) throws Exception {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        ImageIO.write(render(width, height), "png", out);
        return out.toByteArray();
    }

    private void layoutDeep(java.awt.Container container) {
        container.doLayout();
        for (java.awt.Component child : container.getComponents()) {
            if (child instanceof java.awt.Container nested) {
                layoutDeep(nested);
            }
        }
    }

    /**
     * Runs a task on the EDT and waits, so tests can drive Swing safely.
     */
    public void onEdt(Runnable task) throws Exception {
        if (SwingUtilities.isEventDispatchThread()) {
            task.run();
        } else {
            SwingUtilities.invokeAndWait(task);
        }
    }

    public Dimension size() {
        return getSize();
    }

    /**
     * Applies a FlatLaf theme to the already built panel tree.
     */
    public void applyTheme(String themeId) {
        dev.zoroaster1x.vlcskin.app.theme.ThemeManager.apply(themeId);
        studio.settings().setTheme(themeId);
        SwingUtilities.updateComponentTreeUI(this);
        panels.refresh();
        repaint();
    }

    /**
     * Selects a tab in the right column by title.
     */
    public void showRightTab(String title) {
        for (int i = 0; i < rightTabs.getTabCount(); i++) {
            if (title.equals(rightTabs.getTitleAt(i))) {
                rightTabs.setSelectedIndex(i);
                return;
            }
        }
    }

    /**
     * Selects a tab in the left column by title.
     */
    public void showLeftTab(String title) {
        for (int i = 0; i < leftTabs.getTabCount(); i++) {
            if (title.equals(leftTabs.getTitleAt(i))) {
                leftTabs.setSelectedIndex(i);
                return;
            }
        }
    }

    /**
     * Brings a named panel to the front, by translated or English name.
     */
    public void showNamedPanel(String name) {
        if (name == null) {
            return;
        }
        for (JTabbedPane tabs : java.util.List.of(leftTabs, rightTabs, bottomTabs)) {
            for (int i = 0; i < tabs.getTabCount(); i++) {
                String title = tabs.getTitleAt(i);
                java.awt.Component component = tabs.getComponentAt(i);
                String panelName = component instanceof javax.swing.JComponent jComponent
                        ? String.valueOf(jComponent.getClientProperty("panelName")) : "";
                if (name.equalsIgnoreCase(title) || name.equalsIgnoreCase(panelName)) {
                    tabs.setSelectedIndex(i);
                    return;
                }
            }
        }
    }

    @Override
    public void newSkin() {
        studio.newSkin();
    }

    @Override
    public void openSkin() {
        statusBar.setMessage("Open a skin from the CLI or the desktop window in headless mode");
    }

    @Override
    public void save() {
        studio.save();
    }

    @Override
    public void saveAs() {
        statusBar.setMessage("Save as needs the desktop window");
    }

    @Override
    public void importVlt() {
        statusBar.setMessage("Import needs the desktop window or the vlt CLI command");
    }

    @Override
    public void exportVlt() {
        statusBar.setMessage("Export needs the desktop window or the vlt CLI command");
    }

    @Override
    public void renderPreview() {
        statusBar.setMessage("Use the render CLI command for PNG output");
    }

    @Override
    public void testInVlc() {
        statusBar.setMessage("VLC launching is available in the desktop window");
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
        var item = studio.session().selection().item(studio.session().index());
        if (item != null) {
            studio.service().duplicateItem(item.getId());
        }
    }

    @Override
    public void deleteSelected() {
        var item = studio.session().selection().item(studio.session().index());
        if (item != null) {
            studio.service().deleteItem(item.getId());
        }
    }

    @Override
    public void moveSelected(int dx, int dy) {
        var item = studio.session().selection().item(studio.session().index());
        if (item == null) {
            return;
        }
        studio.session().apply(dev.zoroaster1x.vlcskin.edit.ValueCommand
                .builder("Move " + item.type().displayName())
                .set(item.getX(), item.getX() + dx, item::setX)
                .set(item.getY(), item.getY() + dy, item::setY)
                .build());
    }

    @Override
    public void zoomIn() {
        panels.canvas.zoomIn();
        ToolBarFactory.syncToolButtons(toolbar, panels.canvas.tool());
    }

    @Override
    public void zoomOut() {
        panels.canvas.zoomOut();
        ToolBarFactory.syncToolButtons(toolbar, panels.canvas.tool());
    }

    @Override
    public void fitToWindow() {
        panels.canvas.fitToWindow();
    }

    @Override
    public void setTool(CanvasPanel.Tool tool) {
        panels.canvas.setTool(tool);
        ToolBarFactory.syncToolButtons(toolbar, tool);
    }

    @Override
    public void validateSkin() {
        panels.problems.validate();
        statusBar.setMessage("Validation finished");
    }

    @Override
    public void resetLayout() {
        statusBar.setMessage("Panel layout reset");
    }

    @Override
    public void browseThemes() {
        statusBar.setMessage("The theme browser needs the desktop window");
    }

    @Override
    public java.util.List<String> recentFiles() {
        return java.util.List.copyOf(studio.settings().getRecentFiles());
    }

    @Override
    public void openRecent(String path) {
        studio.openFile(java.nio.file.Path.of(path));
    }

    @Override
    public void openSettings() {
        statusBar.setMessage("Skin settings open in the desktop window");
    }

    @Override
    public void showVariables() {
        showRightTab(dev.zoroaster1x.vlcskin.app.i18n.PanelTitles.variables());
    }

    @Override
    public void showAi() {
        showRightTab(dev.zoroaster1x.vlcskin.app.i18n.PanelTitles.ai());
    }

    @Override
    public void toggleCheckerboard() {
        studio.settings().setCheckerboard(!studio.settings().isCheckerboard());
        studio.session().fireChanged();
    }

    @Override
    public void exit() {
        statusBar.setMessage("Exit is available in the desktop window");
    }

    /**
     * The toolbar, so tests can inspect its state.
     */
    public JComponent toolbarComponent() {
        return toolbar;
    }
}
