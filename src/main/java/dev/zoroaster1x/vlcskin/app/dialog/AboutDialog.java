package dev.zoroaster1x.vlcskin.app.dialog;

import dev.zoroaster1x.vlcskin.Version;
import dev.zoroaster1x.vlcskin.app.i18n.Messages;
import java.awt.BorderLayout;
import java.awt.Cursor;
import java.awt.Desktop;
import java.awt.FlowLayout;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.net.URI;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;

/**
 * About box with the honest version and ecosystem notes.
 */
public final class AboutDialog extends JDialog {

    private static final String WEBSITE = "https://github.com/zoroaster1x/vlc-skin-editor";

    public AboutDialog() {
        super((java.awt.Frame) null,
                Messages.get("ABOUT_TITLE", "About VLC Skin Studio").replace("%v", Version.VERSION), true);
        JPanel content = new JPanel(new BorderLayout(8, 8));
        content.setBorder(BorderFactory.createEmptyBorder(16, 18, 12, 18));
        JLabel title = new JLabel(Version.NAME + " " + Version.VERSION);
        title.setFont(title.getFont().deriveFont(java.awt.Font.BOLD, 18f));
        JLabel body = new JLabel("<html>A modern editor for VLC skins2 themes.<br><br>"
                + "Copyright 2007-2026 The VideoLAN Team and contributors.<br>"
                + "GPL-3.0-or-later, derivative of the original VLC Skin Editor 0.8.6 "
                + "by Daniel Dreibrodt (GPL-2.0-or-later).<br><br>"
                + "Desktop UI with dockable panels, a terminal UI, a CLI and an MCP server.<br>"
                + "Built with Java 25, FlatLaf, ModernDocking and the official MCP Java SDK.<br>"
                + "Skin format: VLC skins2 V2.0.<br><br>"
                + "Website: <a href=\"" + WEBSITE + "\">github.com/zoroaster1x/vlc-skin-editor</a></html>");
        body.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        body.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                browse(WEBSITE);
            }
        });
        JPanel footer = new JPanel(new FlowLayout(FlowLayout.RIGHT, 6, 0));
        JButton close = new JButton("Close");
        close.addActionListener(e -> dispose());
        footer.add(close);
        content.add(title, BorderLayout.NORTH);
        content.add(body, BorderLayout.CENTER);
        content.add(footer, BorderLayout.SOUTH);
        setContentPane(content);
        setSize(540, 300);
        setLocationRelativeTo(null);
    }

    private static void browse(String url) {
        try {
            Desktop.getDesktop().browse(URI.create(url));
        } catch (Exception ex) {
            // Opening the browser is best effort.
        }
    }
}
