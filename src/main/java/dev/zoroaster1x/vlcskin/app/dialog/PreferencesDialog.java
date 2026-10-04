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
import javax.swing.JSpinner;
import javax.swing.JTextField;

/**
 * Preferences: theme, language, interface scale, checkerboard, toolbar and the
 * keyboard shortcuts.
 */
public final class PreferencesDialog extends JDialog {

    public PreferencesDialog(Studio studio) {
        this(studio, null, null);
    }

    public PreferencesDialog(Studio studio, Consumer<Boolean> toolbarToggle) {
        this(studio, toolbarToggle, null);
    }

    public PreferencesDialog(Studio studio, Consumer<Boolean> toolbarToggle, Runnable keymapChanged) {
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
        JCheckBox mcpEnabled = new JCheckBox(Messages.get("APP_PREFS_MCP_ENABLE",
                "Enable the MCP server"), studio.settings().isMcpEnabled());
        JCheckBox showToolCalls = new JCheckBox(Messages.get("APP_PREFS_MCP_SHOW",
                "Show tool calls in the status bar"), studio.settings().isShowToolCalls());
        JTextField mcpCommand = new JTextField(mcpCommand());
        mcpCommand.setEditable(false);
        mcpCommand.setToolTipText(Messages.get("APP_PREFS_MCP_COMMAND_TIP",
                "Point your AI client at this command. OpenCode config: mcp.servers.vlc-skin-studio "
                        + "with type local and this command array plus the mcp argument."));
        JLabel mcpStatus = new JLabel(mcpStatusText());
        JButton copyCommand = new JButton(Messages.get("APP_PREFS_MCP_COPY", "Copy command"));
        copyCommand.addActionListener(e -> {
            java.awt.Toolkit.getDefaultToolkit().getSystemClipboard().setContents(
                    new java.awt.datatransfer.StringSelection(mcpCommand.getText()), null);
            studio.status(Messages.get("APP_PREFS_MCP_COPIED", "MCP command copied"));
        });
        JButton refreshStatus = new JButton(Messages.get("APP_PREFS_MCP_REFRESH", "Refresh status"));
        refreshStatus.addActionListener(e -> mcpStatus.setText(mcpStatusText()));
        JSpinner fontScale = new JSpinner(new javax.swing.SpinnerNumberModel(
                studio.settings().getFontScale(), 75, 200, 25));
        JButton keys = new JButton(Messages.get("APP_PREFS_KEYS_BUTTON", "Keyboard shortcuts..."));

        content.add(row(Messages.get("WIN_PREFS_LAF_L", "Look and feel"), themeBox));
        content.add(row(Messages.get("WIN_PREFS_LANG_L", "Language"), languageBox));
        content.add(row(Messages.get("APP_PREFS_FONT_SCALE", "Interface size"), fontScale));
        content.add(row(Messages.get("APP_PREFS_KEYS", "Keys"), keys));
        content.add(row(Messages.get("APP_PREFS_CANVAS_BG", "Canvas background"), backgroundBox));
        content.add(Box.createVerticalStrut(8));
        content.add(checkerboard);
        content.add(showToolbar);
        content.add(Box.createVerticalStrut(6));
        JLabel mcpTitle = new JLabel(Messages.get("APP_PREFS_MCP_TITLE", "AI and MCP"));
        mcpTitle.setFont(mcpTitle.getFont().deriveFont(java.awt.Font.BOLD));
        content.add(mcpTitle);
        content.add(mcpEnabled);
        content.add(showToolCalls);
        JPanel commandRow = new JPanel(new java.awt.BorderLayout(6, 0));
        commandRow.add(mcpCommand, java.awt.BorderLayout.CENTER);
        JPanel commandButtons = new JPanel(new FlowLayout(FlowLayout.LEFT, 4, 0));
        commandButtons.add(copyCommand);
        commandButtons.add(refreshStatus);
        commandRow.add(commandButtons, java.awt.BorderLayout.EAST);
        content.add(commandRow);
        JLabel mcpNote = new JLabel(Messages.get("APP_PREFS_MCP_NOTE",
                "<html><span>The server edits a skin file; this window does not share its session, so save "
                        + "here before the AI reads and open the file again after it writes. Its activity is "
                        + "logged with timestamps in the MCP panel and in the cache mcp.log.</span></html>"));
        mcpNote.setForeground(javax.swing.UIManager.getColor("Label.disabledForeground"));
        content.add(mcpNote);
        JPanel statusRow = new JPanel(new java.awt.BorderLayout(6, 0));
        statusRow.add(mcpStatus, java.awt.BorderLayout.CENTER);
        content.add(statusRow);
        content.add(Box.createVerticalStrut(6));
        JLabel note = new JLabel(Messages.get("APP_PREFS_NOTE",
                "<html><span>Menu and dialog labels use the original "
                        + "editor translations where one exists; newer panels stay in English until translated. "
                        + "Interface size scales every panel and dialog without a restart.</span></html>"));
        note.setForeground(javax.swing.UIManager.getColor("Label.disabledForeground"));
        content.add(note);
        content.add(Box.createVerticalStrut(10));

        JPanel footer = new JPanel(new FlowLayout(FlowLayout.RIGHT, 6, 0));
        JButton cancel = new JButton(Messages.get("BUTTON_CANCEL", "Cancel"));
        cancel.addActionListener(e -> dispose());
        keys.addActionListener(e -> {
            KeymapDialog dialog = new KeymapDialog(studio);
            dialog.setVisible(true);
            if (dialog.saved()) {
                studio.settings().getKeys().clear();
                studio.settings().getKeys().putAll(dialog.keys());
                studio.saveSettings();
                if (keymapChanged != null) {
                    keymapChanged.run();
                }
            }
        });
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
            studio.applyFontScale((Integer) fontScale.getValue());
            studio.settings().setCheckerboard(checkerboard.isSelected());
            studio.settings().setShowToolbar(showToolbar.isSelected());
            studio.settings().setMcpEnabled(mcpEnabled.isSelected());
            studio.settings().setShowToolCalls(showToolCalls.isSelected());
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
        pack();
        setLocationRelativeTo(null);
    }

    private JPanel row(String label, javax.swing.JComponent field) {
        JPanel row = new JPanel(new BorderLayout(8, 0));
        JLabel name = new JLabel(label);
        row.add(name, BorderLayout.WEST);
        row.add(field, BorderLayout.CENTER);
        return row;
    }

    /**
     * The command an MCP client should run. The jar that is running is the
     * honest answer; the classes directory case (tests and IDE runs) has no
     * stable command, so the released name is shown instead.
     */
    private static String mcpCommand() {
        try {
            java.nio.file.Path location = java.nio.file.Path.of(PreferencesDialog.class
                    .getProtectionDomain().getCodeSource().getLocation().toURI());
            if (java.nio.file.Files.isDirectory(location)) {
                return "java -jar vlc-skin-studio.jar mcp";
            }
            return "java -jar " + location + " mcp";
        } catch (Exception ex) {
            return "java -jar vlc-skin-studio.jar mcp";
        }
    }

    /**
     * Whether a server is running, how many calls it made and what it did last.
     */
    private static String mcpStatusText() {
        java.util.Map<String, Object> status = dev.zoroaster1x.vlcskin.mcp.McpLog.readStatus();
        if (status.isEmpty()) {
            return Messages.get("APP_PREFS_MCP_NEVER", "MCP server: no run recorded yet");
        }
        long calls = status.get("calls") instanceof Number number ? number.longValue() : 0;
        String tool = String.valueOf(status.getOrDefault("lastTool", ""));
        long last = status.get("lastCallAt") instanceof Number number ? number.longValue() : 0;
        String when = last == 0 ? Messages.get("APP_PREFS_MCP_NEVER_CALLED", "never")
                : Math.max(0, (System.currentTimeMillis() - last) / 1000) + "s ago";
        return Messages.format("APP_PREFS_MCP_STATUS", "MCP server: %s, %s call(s), last: %s %s",
                dev.zoroaster1x.vlcskin.mcp.McpLog.isRunning() ? "running" : "not running",
                calls, tool.isBlank() ? "-" : tool, when);
    }
}
