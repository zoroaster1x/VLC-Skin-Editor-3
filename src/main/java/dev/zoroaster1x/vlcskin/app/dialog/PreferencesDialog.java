package dev.zoroaster1x.vlcskin.app.dialog;

import dev.zoroaster1x.vlcskin.app.Studio;
import dev.zoroaster1x.vlcskin.app.i18n.Messages;
import dev.zoroaster1x.vlcskin.app.theme.ThemeManager;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.util.function.Consumer;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;

/**
 * Preferences: theme, language, checkerboard and the toolbar, as the original had.
 */
public final class PreferencesDialog extends JDialog {

    public PreferencesDialog(Studio studio) {
        this(studio, null);
    }

    public PreferencesDialog(Studio studio, Consumer<Boolean> toolbarToggle) {
        super((java.awt.Frame) null, Messages.get("MENU_EDIT_PREFS", "Preferences"), true);
        JPanel content = new JPanel();
        content.setLayout(new BoxLayout(content, BoxLayout.Y_AXIS));
        content.setBorder(BorderFactory.createEmptyBorder(14, 14, 14, 14));

        JComboBox<String> themeBox = new JComboBox<>();
        for (ThemeManager.Theme theme : ThemeManager.themes().values()) {
            themeBox.addItem(theme.label());
        }
        themeBox.setSelectedItem(ThemeManager.byId(studio.settings().getTheme()).label());

        JComboBox<Messages.Language> languageBox = new JComboBox<>();
        for (Messages.Language language : Messages.available()) {
            languageBox.addItem(language);
        }
        for (int i = 0; i < languageBox.getItemCount(); i++) {
            if (languageBox.getItemAt(i).code().equals(studio.settings().getLanguage())) {
                languageBox.setSelectedIndex(i);
            }
        }

        JComboBox<String> backgroundBox = new JComboBox<>(new String[] {
                Messages.get("APP_PREFS_CANVAS_THEME", "Follow the theme"),
                Messages.get("APP_PREFS_CANVAS_LIGHT", "Light"),
                Messages.get("APP_PREFS_CANVAS_DARK", "Dark")});
        backgroundBox.setSelectedIndex(switch (studio.settings().getCanvasBackground()) {
            case "light" -> 1;
            case "dark" -> 2;
            default -> 0;
        });

        JCheckBox checkerboard = new JCheckBox(Messages.get("APP_PREFS_CHECKERBOARD", "Checkerboard behind the preview"),
                studio.settings().isCheckerboard());
        JCheckBox showToolbar = new JCheckBox(Messages.get("WIN_PREFS_TBAR_L", "Show the toolbar"),
                studio.settings().isShowToolbar());

        content.add(row(Messages.get("WIN_PREFS_LAF_L", "Look and feel"), themeBox));
        content.add(row(Messages.get("WIN_PREFS_LANG_L", "Language"), languageBox));
        content.add(row(Messages.get("APP_PREFS_CANVAS_BG", "Canvas background"), backgroundBox));
        content.add(Box.createVerticalStrut(8));
        content.add(checkerboard);
        content.add(showToolbar);
        content.add(Box.createVerticalStrut(6));
        JLabel note = new JLabel(Messages.get("APP_PREFS_NOTE",
                "<html><span style='font-size:9px'>Menu and dialog labels use the original "
                        + "editor translations where one exists; newer panels stay in English until translated.</span></html>"));
        note.setForeground(javax.swing.UIManager.getColor("Label.disabledForeground"));
        content.add(note);
        content.add(Box.createVerticalStrut(10));

        JPanel footer = new JPanel(new FlowLayout(FlowLayout.RIGHT, 6, 0));
        JButton cancel = new JButton(Messages.get("BUTTON_CANCEL", "Cancel"));
        cancel.addActionListener(e -> dispose());
        JButton ok = new JButton(Messages.get("BUTTON_OK", "OK"));
        ok.addActionListener(e -> {
            ThemeManager.Theme theme = ThemeManager.themes().get(studio.settings().getTheme());
            String selectedThemeId = theme == null ? "dark" : theme.id();
            for (ThemeManager.Theme candidate : ThemeManager.themes().values()) {
                if (candidate.label().equals(themeBox.getSelectedItem())) {
                    selectedThemeId = candidate.id();
                }
            }
            studio.applyTheme(selectedThemeId);
            Messages.Language language = (Messages.Language) languageBox.getSelectedItem();
            if (language != null) {
                Messages.setLanguage(language.code());
                studio.settings().setLanguage(language.code());
            }
            studio.settings().setCanvasBackground(switch (backgroundBox.getSelectedIndex()) {
                case 1 -> "light";
                case 2 -> "dark";
                default -> "theme";
            });
            studio.settings().setCheckerboard(checkerboard.isSelected());
            studio.settings().setShowToolbar(showToolbar.isSelected());
            if (toolbarToggle != null) {
                toolbarToggle.accept(showToolbar.isSelected());
            }
            studio.saveSettings();
            studio.session().fireChanged();
            studio.status(Messages.get("APP_PREFS_SAVED", "Preferences saved"));
            dispose();
        });
        footer.add(cancel);
        footer.add(ok);
        content.add(footer);

        setContentPane(content);
        setPreferredSize(new Dimension(460, 360));
        pack();
        setLocationRelativeTo(null);
    }

    private JPanel row(String label, javax.swing.JComponent field) {
        JPanel row = new JPanel(new BorderLayout(8, 0));
        JLabel name = new JLabel(label);
        name.setPreferredSize(new Dimension(150, 24));
        row.add(name, BorderLayout.WEST);
        row.add(field, BorderLayout.CENTER);
        row.setMaximumSize(new Dimension(Integer.MAX_VALUE, 30));
        return row;
    }
}
