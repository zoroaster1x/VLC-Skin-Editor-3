package dev.zoroaster1x.vlcskin.app.panel;

import dev.zoroaster1x.vlcskin.app.Studio;
import dev.zoroaster1x.vlcskin.app.i18n.Messages;
import java.awt.BorderLayout;
import java.awt.Font;
import javax.swing.JButton;
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
    private boolean updating;

    public XmlPanel(Studio studio) {
        this.studio = studio;
        setLayout(new BorderLayout());
        area.setSyntaxEditingStyle(SyntaxConstants.SYNTAX_STYLE_XML);
        area.setCodeFoldingEnabled(true);
        area.setAntiAliasingEnabled(true);
        area.setTabSize(2);
        area.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 12));
        area.setEditable(true);

        JToolBar bar = new JToolBar();
        bar.setFloatable(false);
        JButton refresh = new JButton(Messages.get("APP_XML_REFRESH", "Refresh"),
                dev.zoroaster1x.vlcskin.app.component.Icons.of("refresh", 14));
        refresh.addActionListener(e -> refreshText());
        JButton apply = new JButton(Messages.get("APP_XML_APPLY", "Apply XML"),
                dev.zoroaster1x.vlcskin.app.component.Icons.of("validate", 14));
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
     */
    public void refresh() {
        if (updating) {
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

    private void refreshText() {
        updating = false;
        refresh();
    }

    public RSyntaxTextArea textArea() {
        return area;
    }
}
