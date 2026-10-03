package dev.zoroaster1x.vlcskin.app.dialog;

import dev.zoroaster1x.vlcskin.app.docs.DocumentationPanel;
import dev.zoroaster1x.vlcskin.app.i18n.Messages;
import javax.swing.JDialog;
import javax.swing.JFrame;

/**
 * The bundled documentation in its own window: searchable topics, rendered
 * markdown, images and links.
 */
public final class DocumentationDialog extends JDialog {

    private final DocumentationPanel panel = new DocumentationPanel();

    public DocumentationDialog(JFrame owner) {
        super(owner, Messages.get("APP_DOCS_TITLE", "Documentation"), false);
        setSize(1180, 780);
        setLocationRelativeTo(owner);
        setContentPane(panel);
    }

    public DocumentationPanel panel() {
        return panel;
    }
}
