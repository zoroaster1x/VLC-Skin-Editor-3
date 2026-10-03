package dev.zoroaster1x.vlcskin.app.dialog;

import dev.zoroaster1x.vlcskin.app.Studio;
import dev.zoroaster1x.vlcskin.app.i18n.Messages;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.JTextField;
import javax.swing.SpinnerNumberModel;

/**
 * Theme metadata and window attributes.
 */
public final class ThemeSettingsDialog extends JDialog {

    public ThemeSettingsDialog(Studio studio) {
        super((java.awt.Frame) null, Messages.get("WIN_THEME_TITLE", "Skin settings"), true);
        var theme = studio.session().theme();
        var info = theme.getThemeInfo();

        JPanel content = new JPanel();
        content.setLayout(new BoxLayout(content, BoxLayout.Y_AXIS));
        content.setBorder(BorderFactory.createEmptyBorder(12, 12, 12, 12));

        JTextField name = field(info.getName());
        JTextField author = field(info.getAuthor());
        JTextField email = field(info.getEmail());
        JTextField webpage = field(info.getWebpage());
        JSpinner magnet = new JSpinner(new SpinnerNumberModel(theme.getMagnet(), 0, 500, 1));
        JSpinner alpha = new JSpinner(new SpinnerNumberModel(theme.getAlpha(), 1, 255, 1));
        JSpinner movealpha = new JSpinner(new SpinnerNumberModel(theme.getMovealpha(), 1, 255, 1));

        content.add(row(Messages.get("WIN_THEME_NAME", "Name"), name));
        content.add(row(Messages.get("WIN_THEME_AUTHOR", "Author"), author));
        content.add(row(Messages.get("WIN_THEME_EMAIL", "Email"), email));
        content.add(row(Messages.get("WIN_THEME_WEB", "Webpage"), webpage));
        content.add(Box.createVerticalStrut(8));
        content.add(row(Messages.get("WIN_THEME_MAGNET", "Magnet"), magnet));
        content.add(row(Messages.get("WIN_THEME_ALPHA", "Opacity"), alpha));
        content.add(row(Messages.get("WIN_THEME_MOVEALPHA", "Opacity while moving"), movealpha));
        content.add(Box.createVerticalStrut(8));
        JLabel note = new JLabel(Messages.get("APP_THEME_MAGNET_NOTE", "Magnet is the snapping distance in pixels."));
        note.setForeground(javax.swing.UIManager.getColor("Label.disabledForeground"));
        content.add(note);

        JPanel footer = new JPanel(new FlowLayout(FlowLayout.RIGHT, 6, 0));
        JButton cancel = new JButton(Messages.get("BUTTON_CANCEL", "Cancel"));
        cancel.addActionListener(e -> dispose());
        JButton ok = new JButton(Messages.get("BUTTON_OK", "OK"));
        ok.addActionListener(e -> {
            var command = dev.zoroaster1x.vlcskin.edit.ValueCommand
                    .builder("Edit theme")
                    .set(info.getName(), name.getText(), info::setName)
                    .set(info.getAuthor(), author.getText(), info::setAuthor)
                    .set(info.getEmail(), email.getText(), info::setEmail)
                    .set(info.getWebpage(), webpage.getText(), info::setWebpage)
                    .set(theme.getMagnet(), (Integer) magnet.getValue(), theme::setMagnet)
                    .set(theme.getAlpha(), (Integer) alpha.getValue(), theme::setAlpha)
                    .set(theme.getMovealpha(), (Integer) movealpha.getValue(), theme::setMovealpha)
                    .build();
            studio.session().apply(command);
            dispose();
        });
        footer.add(cancel);
        footer.add(ok);
        JButton help = new JButton(Messages.get("BUTTON_HELP", "Help"));
        help.addActionListener(e -> dev.zoroaster1x.vlcskin.app.dialog.DocumentationDialog
                .openTopic(this, "handbook-step-2-new-theme"));
        footer.add(help);

        content.add(Box.createVerticalStrut(10));
        content.add(footer);
        setContentPane(content);
        setPreferredSize(new Dimension(420, 320));
        pack();
        setLocationRelativeTo(null);
    }

    private JTextField field(String value) {
        JTextField field = new JTextField(value == null ? "" : value);
        field.setPreferredSize(new Dimension(240, 26));
        return field;
    }

    private JPanel row(String label, javax.swing.JComponent component) {
        JPanel row = new JPanel(new BorderLayout(8, 0));
        JLabel name = new JLabel(label);
        name.setPreferredSize(new Dimension(140, 24));
        row.add(name, BorderLayout.WEST);
        row.add(component, BorderLayout.CENTER);
        row.setMaximumSize(new Dimension(Integer.MAX_VALUE, 30));
        return row;
    }
}
