package dev.zoroaster1x.vlcskin.app.dialog;

import dev.zoroaster1x.vlcskin.app.Studio;
import dev.zoroaster1x.vlcskin.app.config.Keymap;
import dev.zoroaster1x.vlcskin.app.i18n.Messages;
import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.KeyEventDispatcher;
import java.awt.KeyboardFocusManager;
import java.awt.event.KeyEvent;
import java.util.LinkedHashMap;
import java.util.Map;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.KeyStroke;
import javax.swing.ListSelectionModel;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableColumnModel;

/**
 * Editor for the configurable keyboard shortcuts. Rows come from
 * {@link Keymap#BINDINGS}; the capture dialog records one chord at a time and
 * warns about conflicts before saving.
 */
public final class KeymapDialog extends JDialog {

    private final Map<String, String> working;
    private final DefaultTableModel model = new DefaultTableModel(
            new Object[] {Messages.get("APP_KEYS_ACTION", "Action"),
                    Messages.get("APP_KEYS_SHORTCUT", "Shortcut")}, 0) {
        @Override
        public boolean isCellEditable(int row, int column) {
            return false;
        }
    };
    private final JTable table = new JTable(model);
    private boolean saved;

    public KeymapDialog(Studio studio) {
        super((java.awt.Frame) null, Messages.get("APP_KEYS_TITLE", "Keyboard shortcuts"), true);
        working = Keymap.copyOf(studio.settings().getKeys());
        for (Keymap.Binding binding : Keymap.BINDINGS) {
            model.addRow(new Object[] {label(binding), Keymap.describe(Keymap.stroke(working, binding.id()))});
        }
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        table.setRowHeight(Math.max(22, table.getFont().getSize() + 8));
        TableColumnModel columns = table.getColumnModel();
        columns.getColumn(0).setPreferredWidth(280);
        columns.getColumn(1).setPreferredWidth(180);
        JScrollPane scroll = new JScrollPane(table);
        scroll.setPreferredSize(new Dimension(480, 380));
        scroll.setBorder(BorderFactory.createEmptyBorder());

        JPanel footer = new JPanel(new FlowLayout(FlowLayout.RIGHT, 6, 0));
        JButton change = new JButton(Messages.get("APP_KEYS_CHANGE", "Change..."));
        change.addActionListener(e -> changeSelected());
        JButton clear = new JButton(Messages.get("APP_KEYS_CLEAR", "Clear"));
        clear.addActionListener(e -> {
            int row = table.getSelectedRow();
            if (row >= 0) {
                working.put(Keymap.BINDINGS.get(row).id(), "");
                refreshRow(row);
            }
        });
        JButton reset = new JButton(Messages.get("APP_KEYS_RESET", "Reset all"));
        reset.addActionListener(e -> {
            Keymap.resetAll(working);
            for (int row = 0; row < Keymap.BINDINGS.size(); row++) {
                refreshRow(row);
            }
        });
        JButton cancel = new JButton(Messages.get("BUTTON_CANCEL", "Cancel"));
        cancel.addActionListener(e -> dispose());
        JButton ok = new JButton(Messages.get("BUTTON_OK", "OK"));
        ok.addActionListener(e -> {
            if (confirmConflicts()) {
                saved = true;
                dispose();
            }
        });
        footer.add(change);
        footer.add(clear);
        footer.add(reset);
        footer.add(cancel);
        footer.add(ok);
        table.getSelectionModel().setSelectionInterval(0, 0);
        table.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseClicked(java.awt.event.MouseEvent e) {
                if (e.getClickCount() == 2) {
                    changeSelected();
                }
            }
        });

        JPanel content = new JPanel(new BorderLayout(0, 10));
        content.setBorder(BorderFactory.createEmptyBorder(12, 12, 12, 12));
        JLabel note = new JLabel(Messages.get("APP_KEYS_NOTE",
                "<html>Select a row and press Change, or double click it. Backspace clears a "
                        + "shortcut, Esc cancels the capture.</html>"));
        content.add(note, BorderLayout.NORTH);
        content.add(scroll, BorderLayout.CENTER);
        content.add(footer, BorderLayout.SOUTH);
        setContentPane(content);
        pack();
        setLocationRelativeTo(null);
    }

    /**
     * True when the user pressed OK; the keys are in {@link #keys()}.
     */
    public boolean saved() {
        return saved;
    }

    public Map<String, String> keys() {
        return new LinkedHashMap<>(working);
    }

    private String label(Keymap.Binding binding) {
        return binding.labelKey() == null
                ? binding.fallback()
                : Messages.get(binding.labelKey(), binding.fallback());
    }

    private void changeSelected() {
        int row = table.getSelectedRow();
        if (row < 0) {
            return;
        }
        Keymap.Binding binding = Keymap.BINDINGS.get(row);
        String captured = capture(binding);
        if (captured != null) {
            Keymap.set(working, binding.id(), captured);
            refreshRow(row);
        }
    }

    private void refreshRow(int row) {
        Keymap.Binding binding = Keymap.BINDINGS.get(row);
        model.setValueAt(Keymap.describe(Keymap.stroke(working, binding.id())), row, 1);
    }

    private boolean confirmConflicts() {
        Map<String, String> seen = new LinkedHashMap<>();
        for (Keymap.Binding binding : Keymap.BINDINGS) {
            String stroke = Keymap.stroke(working, binding.id());
            if (stroke == null || stroke.isBlank()) {
                continue;
            }
            String previous = seen.put(Keymap.normalize(stroke), label(binding));
            if (previous != null) {
                int choice = JOptionPane.showConfirmDialog(this,
                        Messages.format("APP_KEYS_CONFLICT_MSG",
                                "%1$s is used by both \"%2$s\" and \"%3$s\". Keep both?",
                                Keymap.describe(stroke), previous, label(binding)),
                        Messages.get("APP_KEYS_CONFLICT_TITLE", "Shortcut conflict"),
                        JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
                return choice == JOptionPane.YES_OPTION;
            }
        }
        return true;
    }

    /**
     * Records one chord. Returns the stroke text, "" for a cleared shortcut, or
     * null when the user cancelled.
     */
    private String capture(Keymap.Binding binding) {
        JDialog dialog = new JDialog(this, Messages.get("APP_KEYS_PRESS_TITLE", "Press a shortcut"), true);
        JLabel label = new JLabel(Messages.format("APP_KEYS_PRESS_MSG",
                "<html>Press the new shortcut for <b>%s</b>.<br>Backspace clears it, Esc cancels.</html>",
                label(binding)));
        label.setBorder(BorderFactory.createEmptyBorder(20, 24, 20, 24));
        dialog.setContentPane(label);
        dialog.setUndecorated(true);
        dialog.pack();
        dialog.setLocationRelativeTo(this);

        String[] result = {null};
        KeyEventDispatcher dispatcher = event -> {
            if (event.getID() != KeyEvent.KEY_PRESSED) {
                return false;
            }
            KeyStroke stroke = KeyStroke.getKeyStrokeForEvent(event);
            if (stroke.getKeyCode() == KeyEvent.VK_ESCAPE) {
                dialog.dispose();
                return true;
            }
            if (stroke.getKeyCode() == KeyEvent.VK_BACK_SPACE) {
                result[0] = "";
                dialog.dispose();
                return true;
            }
            if (stroke.getKeyCode() == 0 || isModifierOnly(stroke.getKeyCode())) {
                return true;
            }
            result[0] = strokeText(stroke);
            dialog.dispose();
            return true;
        };
        KeyboardFocusManager.getCurrentKeyboardFocusManager().addKeyEventDispatcher(dispatcher);
        try {
            dialog.setVisible(true);
        } finally {
            KeyboardFocusManager.getCurrentKeyboardFocusManager().removeKeyEventDispatcher(dispatcher);
        }
        return result[0];
    }

    private static boolean isModifierOnly(int keyCode) {
        return switch (keyCode) {
            case KeyEvent.VK_SHIFT, KeyEvent.VK_CONTROL, KeyEvent.VK_ALT,
                    KeyEvent.VK_META, KeyEvent.VK_ALT_GRAPH -> true;
            default -> false;
        };
    }

    private static String strokeText(KeyStroke stroke) {
        StringBuilder text = new StringBuilder();
        int modifiers = stroke.getModifiers();
        if ((modifiers & KeyEvent.SHIFT_DOWN_MASK) != 0) {
            text.append("shift ");
        }
        if ((modifiers & KeyEvent.CTRL_DOWN_MASK) != 0) {
            text.append("control ");
        }
        if ((modifiers & KeyEvent.ALT_DOWN_MASK) != 0) {
            text.append("alt ");
        }
        if ((modifiers & KeyEvent.META_DOWN_MASK) != 0) {
            text.append("meta ");
        }
        text.append(KeyEvent.getKeyText(stroke.getKeyCode()));
        return text.toString().strip();
    }
}
