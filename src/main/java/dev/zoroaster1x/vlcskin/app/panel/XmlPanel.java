package dev.zoroaster1x.vlcskin.app.panel;

import dev.zoroaster1x.vlcskin.app.Studio;
import dev.zoroaster1x.vlcskin.app.i18n.Messages;
import java.awt.BorderLayout;
import java.awt.Font;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JToolBar;
import org.fife.ui.rsyntaxtextarea.RSyntaxTextArea;
import org.fife.ui.rsyntaxtextarea.SyntaxConstants;
import org.fife.ui.rtextarea.RTextScrollPane;

/**
 * The generated skin XML, editable with an apply step.
 */
public final class XmlPanel extends JPanel {

    private final Studio studio;
    private final RSyntaxTextArea area = new RSyntaxTextArea();
    private final JLabel pending = new JLabel();
    private boolean updating;
    private boolean localEdits;

    public XmlPanel(Studio studio) {
        this.studio = studio;
        setLayout(new BorderLayout());
        area.setSyntaxEditingStyle(SyntaxConstants.SYNTAX_STYLE_XML);
        area.setCodeFoldingEnabled(true);
        area.setAntiAliasingEnabled(true);
        area.setTabSize(2);
        int codeFontSize = area.getFont() != null ? area.getFont().getSize() : 12;
        area.setFont(new Font(Font.MONOSPACED, Font.PLAIN, codeFontSize));
        area.setEditable(true);
        area.getDocument().addDocumentListener(new javax.swing.event.DocumentListener() {
            @Override
            public void insertUpdate(javax.swing.event.DocumentEvent e) {
                edited();
            }

            @Override
            public void removeUpdate(javax.swing.event.DocumentEvent e) {
                edited();
            }

            @Override
            public void changedUpdate(javax.swing.event.DocumentEvent e) {
                edited();
            }

            private void edited() {
                if (!updating) {
                    setLocalEdits(true);
                }
            }
        });

        JToolBar bar = new JToolBar();
        bar.setFloatable(false);
        JButton refresh = new JButton(Messages.get("APP_XML_REFRESH", "Refresh"),
                dev.zoroaster1x.vlcskin.app.component.Icons.of("refresh", 14));
        refresh.setToolTipText(Messages.get("APP_XML_REFRESH_TIP",
                "Discard local edits and show the model again"));
        refresh.addActionListener(e -> refreshText());
        JButton apply = new JButton(Messages.get("APP_XML_APPLY", "Apply XML"),
                dev.zoroaster1x.vlcskin.app.component.Icons.of("validate", 14));
        apply.setToolTipText(Messages.get("APP_XML_APPLY_TIP",
                "Parse the text and replace the document when it has no errors"));
        apply.addActionListener(e -> studio.applyXml(area.getText(), this));
        JButton copy = new JButton(Messages.get("APP_XML_COPY", "Copy"));
        copy.addActionListener(e -> {
            area.selectAll();
            area.copy();
            area.setCaretPosition(0);
        });
        bar.add(refresh);
        bar.add(apply);
        bar.add(copy);
        bar.add(javax.swing.Box.createHorizontalGlue());
        pending.setForeground(dev.zoroaster1x.vlcskin.app.theme.ThemeManager.ACCENT);
        pending.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 8, 0, 8));
        bar.add(pending);

        add(bar, BorderLayout.NORTH);
        add(new RTextScrollPane(area), BorderLayout.CENTER);

        javax.swing.JPopupMenu editorMenu = new javax.swing.JPopupMenu();
        javax.swing.JMenuItem popupApply = new javax.swing.JMenuItem(Messages.get("APP_XML_APPLY", "Apply XML"));
        popupApply.addActionListener(e -> studio.applyXml(area.getText(), this));
        javax.swing.JMenuItem popupRefresh = new javax.swing.JMenuItem(
                Messages.get("APP_XML_REFRESH_MODEL", "Refresh from the model"));
        popupRefresh.addActionListener(e -> refreshText());
        javax.swing.JMenuItem popupCopy = new javax.swing.JMenuItem(Messages.get("APP_XML_COPY", "Copy"));
        popupCopy.addActionListener(e -> area.copy());
        javax.swing.JMenuItem popupSelectAll = new javax.swing.JMenuItem(Messages.get("APP_XML_SELECT_ALL", "Select all"));
        popupSelectAll.addActionListener(e -> area.selectAll());
        editorMenu.add(popupApply);
        editorMenu.add(popupRefresh);
        editorMenu.addSeparator();
        editorMenu.add(popupCopy);
        editorMenu.add(popupSelectAll);
        area.setPopupMenu(editorMenu);
    }

    /**
     * Rewrites the text from the model unless the user has local edits.
     * Unapplied text is never discarded silently; the toolbar says so and
     * Refresh is the explicit way to drop it.
     */
    public void refresh() {
        if (updating || localEdits) {
            return;
        }
        updating = true;
        try {
            String text = studio.session().toXml();
            if (!text.equals(area.getText())) {
                area.setText(text);
                area.setCaretPosition(0);
            }
        } finally {
            updating = false;
        }
    }

    /**
     * Called after the text was parsed into the document successfully.
     */
    public void markSynced() {
        setLocalEdits(false);
    }

    private void setLocalEdits(boolean edits) {
        localEdits = edits;
        pending.setText(edits
                ? Messages.get("APP_XML_PENDING", "Unapplied edits")
                : "");
    }

    private void refreshText() {
        updating = true;
        setLocalEdits(false);
        try {
            area.setText(studio.session().toXml());
            area.setCaretPosition(0);
        } finally {
            updating = false;
        }
    }

    public RSyntaxTextArea textArea() {
        return area;
    }
}
