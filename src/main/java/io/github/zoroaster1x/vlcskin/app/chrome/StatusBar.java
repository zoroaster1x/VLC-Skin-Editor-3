package io.github.zoroaster1x.vlcskin.app.chrome;

import io.github.zoroaster1x.vlcskin.app.Studio;
import io.github.zoroaster1x.vlcskin.app.theme.ThemeManager;
import io.github.zoroaster1x.vlcskin.model.item.Item;
import java.awt.BorderLayout;
import java.awt.FlowLayout;
import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.UIManager;

/**
 * The bottom status bar: message, selection, zoom and dirty state.
 */
public final class StatusBar extends JPanel {

    private final JLabel message = new JLabel("Ready");
    private final JLabel selection = new JLabel("");
    private final JLabel zoom = new JLabel("");
    private final JLabel dirty = new JLabel("");

    public StatusBar() {
        super(new BorderLayout());
        setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(1, 0, 0, 0, UIManager.getColor("Component.borderColor")),
                BorderFactory.createEmptyBorder(2, 8, 2, 8)));
        JPanel left = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        left.setOpaque(false);
        left.add(message);
        JPanel right = new JPanel(new FlowLayout(FlowLayout.RIGHT, 12, 0));
        right.setOpaque(false);
        right.add(selection);
        right.add(zoom);
        dirty.setForeground(ThemeManager.ACCENT);
        right.add(dirty);
        add(left, BorderLayout.WEST);
        add(right, BorderLayout.EAST);
    }

    public JLabel messageLabel() {
        return message;
    }

    public void setMessage(String text) {
        message.setText(text);
    }

    /**
     * Pulls selection, zoom and dirty state out of the session.
     */
    public void update(Studio studio) {
        var session = studio.session();
        Item item = session.selection().item(session.index());
        selection.setText(item == null ? "" : item.type().displayName() + ": " + item.getId());
        zoom.setText("Zoom " + studio.settings().getCanvasZoom() + "x");
        dirty.setText(session.isDirty() ? "Unsaved changes" : "");
    }
}
