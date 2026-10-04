package dev.zoroaster1x.vlcskin.app.chrome;

import dev.zoroaster1x.vlcskin.app.i18n.Messages;
import java.awt.Component;
import java.awt.event.ActionListener;
import javax.swing.JButton;
import javax.swing.JToggleButton;
import javax.swing.JToolBar;

/**
 * Builds the application toolbar; shared by the desktop window and screenshots.
 * Buttons carry a tool id client property so enabling and checked state never
 * depend on comparing localized tooltip strings.
 */
public final class ToolBarFactory {

    public static final String TOOL_ID = "vlcskin.toolId";

    private ToolBarFactory() {
    }

    public static JToolBar build(ChromeActions actions) {
        JToolBar bar = new JToolBar();
        bar.setName("Main toolbar");
        bar.setFloatable(true);
        bar.setAlignmentX(java.awt.Component.LEFT_ALIGNMENT);
        bar.setBorder(javax.swing.BorderFactory.createEmptyBorder(4, 6, 4, 6));
        bar.add(tool("open", Messages.get("TOOLBAR_OPEN", "Open a skin..."), "open", e -> actions.openSkin()));
        bar.add(tool("save", Messages.get("TOOLBAR_SAVE", "Save skin modifications"), "save", e -> actions.save()));
        bar.addSeparator();
        bar.add(tool("undo", Messages.get("TOOLBAR_UNDO", "Undo"), "undo", e -> actions.undo()));
        bar.add(tool("redo", Messages.get("TOOLBAR_REDO", "Redo"), "redo", e -> actions.redo()));
        bar.addSeparator();
        bar.add(toggle("move", Messages.get("TOOLBAR_MOVE", "Item moving tool"), "move",
                e -> actions.setTool(dev.zoroaster1x.vlcskin.app.panel.CanvasPanel.Tool.MOVE)));
        bar.add(toggle("path", Messages.get("TOOLBAR_PATH", "Slider editing tool"), "path",
                e -> actions.setTool(dev.zoroaster1x.vlcskin.app.panel.CanvasPanel.Tool.PATH)));
        bar.addSeparator();
        bar.add(tool("zoom-out", "Zoom out", "zoom-out", e -> actions.zoomOut()));
        bar.add(tool("zoom-in", "Zoom in", "zoom-in", e -> actions.zoomIn()));
        bar.add(tool("fit", "Fit window", "grid", e -> actions.fitToWindow()));
        bar.addSeparator();
        bar.add(tool("validate", "Validate the skin", "validate", e -> actions.validateSkin()));
        bar.add(tool("render", "Render the preview to PNG", "image", e -> actions.renderPreview()));
        bar.addSeparator();
        bar.add(tool("settings", Messages.get("MENU_EDIT_THEME", "Skin settings"), "layout", e -> actions.openSettings()));
        bar.add(tool("variables", "Global variables", "checkbox", e -> actions.showVariables()));
        return bar;
    }

    private static JButton tool(String id, String tooltip, String icon, ActionListener listener) {
        JButton button = new JButton(dev.zoroaster1x.vlcskin.app.component.Icons.of(icon, 16));
        button.setToolTipText(tooltip);
        button.setFocusable(true);
        button.putClientProperty(TOOL_ID, id);
        button.putClientProperty("JButton.buttonType", "toolBarButton");
        button.addActionListener(listener);
        return button;
    }

    private static JToggleButton toggle(String id, String tooltip, String icon, ActionListener listener) {
        JToggleButton button = new JToggleButton(dev.zoroaster1x.vlcskin.app.component.Icons.of(icon, 16));
        button.setToolTipText(tooltip);
        button.setFocusable(true);
        button.putClientProperty(TOOL_ID, id);
        button.putClientProperty("JButton.buttonType", "toolBarButton");
        button.addActionListener(listener);
        return button;
    }

    /**
     * Applies the checked state of the move/path tool buttons after a tool change.
     */
    public static void syncToolButtons(Component toolbar, dev.zoroaster1x.vlcskin.app.panel.CanvasPanel.Tool tool) {
        if (!(toolbar instanceof JToolBar bar)) {
            return;
        }
        for (Component component : bar.getComponents()) {
            if (component instanceof JToggleButton button) {
                Object id = button.getClientProperty(TOOL_ID);
                if ("move".equals(id)) {
                    button.setSelected(tool == dev.zoroaster1x.vlcskin.app.panel.CanvasPanel.Tool.MOVE);
                } else if ("path".equals(id)) {
                    button.setSelected(tool == dev.zoroaster1x.vlcskin.app.panel.CanvasPanel.Tool.PATH);
                }
            }
        }
    }

    /**
     * Applies undo and redo availability to the toolbar buttons.
     */
    public static void syncUndoButtons(Component toolbar, boolean canUndo, boolean canRedo) {
        if (!(toolbar instanceof JToolBar bar)) {
            return;
        }
        for (Component component : bar.getComponents()) {
            if (component instanceof JButton button) {
                Object id = button.getClientProperty(TOOL_ID);
                if ("undo".equals(id)) {
                    button.setEnabled(canUndo);
                } else if ("redo".equals(id)) {
                    button.setEnabled(canRedo);
                }
            }
        }
    }
}
