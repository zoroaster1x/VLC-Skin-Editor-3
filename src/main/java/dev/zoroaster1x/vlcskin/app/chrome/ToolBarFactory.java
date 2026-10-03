package dev.zoroaster1x.vlcskin.app.chrome;

import dev.zoroaster1x.vlcskin.app.i18n.Messages;
import java.awt.Component;
import java.awt.event.ActionListener;
import javax.swing.JToolBar;

/**
 * Builds the application toolbar; shared by the desktop window and screenshots.
 */
public final class ToolBarFactory {

    private ToolBarFactory() {
    }

    public static JToolBar build(ChromeActions actions) {
        JToolBar bar = new JToolBar();
        bar.setName("Main toolbar");
        bar.setFloatable(true);
        bar.setAlignmentX(java.awt.Component.LEFT_ALIGNMENT);
        bar.setBorder(javax.swing.BorderFactory.createEmptyBorder(4, 6, 4, 6));
        bar.add(tool(Messages.get("TOOLBAR_OPEN", "Open a skin..."), "open", e -> actions.openSkin()));
        bar.add(tool(Messages.get("TOOLBAR_SAVE", "Save skin modifications"), "save", e -> actions.save()));
        bar.addSeparator();
        bar.add(tool(Messages.get("TOOLBAR_UNDO", "Undo"), "undo", e -> actions.undo()));
        bar.add(tool(Messages.get("TOOLBAR_REDO", "Redo"), "redo", e -> actions.redo()));
        bar.addSeparator();
        bar.add(tool(Messages.get("TOOLBAR_MOVE", "Item moving tool"), "move",
                e -> actions.setTool(dev.zoroaster1x.vlcskin.app.panel.CanvasPanel.Tool.MOVE)));
        bar.add(tool(Messages.get("TOOLBAR_PATH", "Slider editing tool"), "path",
                e -> actions.setTool(dev.zoroaster1x.vlcskin.app.panel.CanvasPanel.Tool.PATH)));
        bar.addSeparator();
        bar.add(tool("Zoom out", "zoom-out", e -> actions.zoomOut()));
        bar.add(tool("Zoom in", "zoom-in", e -> actions.zoomIn()));
        bar.add(tool("Fit window", "grid", e -> actions.fitToWindow()));
        bar.addSeparator();
        bar.add(tool("Validate the skin", "validate", e -> actions.validateSkin()));
        bar.add(tool("Render the preview to PNG", "image", e -> actions.renderPreview()));
        bar.addSeparator();
        bar.add(tool(Messages.get("MENU_EDIT_THEME", "Skin settings"), "layout", e -> actions.openSettings()));
        bar.add(tool("Global variables", "checkbox", e -> actions.showVariables()));
        bar.add(tool("AI assistant", "chat", e -> actions.showAi()));
        return bar;
    }

    private static javax.swing.JButton tool(String tooltip, String icon, ActionListener listener) {
        javax.swing.JButton button = new javax.swing.JButton(dev.zoroaster1x.vlcskin.app.component.Icons.of(icon, 16));
        button.setToolTipText(tooltip);
        button.setFocusable(true);
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
            if (component instanceof javax.swing.JButton button) {
                String tip = button.getToolTipText();
                if (Messages.get("TOOLBAR_MOVE", "Item moving tool").equals(tip)) {
                    button.setSelected(tool == dev.zoroaster1x.vlcskin.app.panel.CanvasPanel.Tool.MOVE);
                } else if (Messages.get("TOOLBAR_PATH", "Slider editing tool").equals(tip)) {
                    button.setSelected(tool == dev.zoroaster1x.vlcskin.app.panel.CanvasPanel.Tool.PATH);
                }
            }
        }
    }

    /**
     * Applies undo and redo availability to the toolbar buttons, found by tooltip.
     */
    public static void syncUndoButtons(Component toolbar, boolean canUndo, boolean canRedo) {
        if (!(toolbar instanceof JToolBar bar)) {
            return;
        }
        String undoTip = Messages.get("TOOLBAR_UNDO", "Undo");
        String redoTip = Messages.get("TOOLBAR_REDO", "Redo");
        for (Component component : bar.getComponents()) {
            if (component instanceof javax.swing.JButton button) {
                String tip = button.getToolTipText();
                if (undoTip.equals(tip)) {
                    button.setEnabled(canUndo);
                } else if (redoTip.equals(tip)) {
                    button.setEnabled(canRedo);
                }
            }
        }
    }
}
