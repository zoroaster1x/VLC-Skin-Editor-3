package io.github.zoroaster1x.vlcskin.app.dialog;

import io.github.zoroaster1x.vlcskin.action.ActionCatalog;
import io.github.zoroaster1x.vlcskin.action.ActionChain;
import io.github.zoroaster1x.vlcskin.app.Studio;
import io.github.zoroaster1x.vlcskin.app.i18n.Messages;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.util.List;
import javax.swing.BorderFactory;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.ListSelectionModel;
import javax.swing.SwingUtilities;

/**
 * Builds a semicolon separated VLC action chain.
 */
public final class ActionEditorDialog {

    private ActionEditorDialog() {
    }

    /**
     * Returns the new action attribute, or null when cancelled.
     */
    public static String edit(Studio studio, String current) {
        JDialog dialog = new JDialog((java.awt.Frame) null, Messages.get("ACTIONS_PU", "Actions"), true);
        dialog.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
        DefaultListModel<String> model = new DefaultListModel<>();
        ActionChain chain = ActionChain.parse(current);
        chain.codes().forEach(model::addElement);
        JList<String> list = new JList<>(model);
        list.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        list.setCellRenderer(new javax.swing.DefaultListCellRenderer() {
            @Override
            public java.awt.Component getListCellRendererComponent(JList<?> l, Object value, int index,
                                                                   boolean selected, boolean focus) {
                JLabel label = (JLabel) super.getListCellRendererComponent(l, value, index, selected, focus);
                label.setText(ActionCatalog.describe(value.toString()) + "   [" + value + "]");
                return label;
            }
        });

        JPanel content = new JPanel(new BorderLayout(8, 8));
        content.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        list.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseClicked(java.awt.event.MouseEvent e) {
                if (e.getClickCount() != 2) {
                    return;
                }
                int selected = list.getSelectedIndex();
                if (selected < 0) {
                    return;
                }
                String edited = JOptionPane.showInputDialog(dialog, "Action code:", model.get(selected));
                if (edited != null && !edited.isBlank()) {
                    model.set(selected, edited.trim());
                }
            }
        });

        JPanel buttons = new JPanel(new FlowLayout(FlowLayout.LEFT, 4, 0));
        JButton add = new JButton(Messages.get("APP_ACTION_ADD", "Add action"));
        add.addActionListener(e -> showAddMenu(studio, add, model));
        JButton remove = new JButton(Messages.get("APP_ACTION_REMOVE", "Remove"));
        remove.addActionListener(e -> {
            int index = list.getSelectedIndex();
            if (index >= 0) {
                model.remove(index);
            }
        });
        JButton up = new JButton(Messages.get("APP_ACTION_UP", "Up"));
        up.addActionListener(e -> move(model, list.getSelectedIndex(), -1));
        JButton down = new JButton(Messages.get("APP_ACTION_DOWN", "Down"));
        down.addActionListener(e -> move(model, list.getSelectedIndex(), 1));
        buttons.add(add);
        buttons.add(remove);
        buttons.add(up);
        buttons.add(down);

        JPanel actions = new JPanel(new BorderLayout());
        actions.add(new JLabel(Messages.get("APP_ACTION_ORDER_NOTE", "The actions run in order, left to right.")),
                BorderLayout.NORTH);
        actions.add(new JScrollPane(list), BorderLayout.CENTER);
        actions.add(buttons, BorderLayout.SOUTH);

        boolean[] cancelled = {true};
        JPanel footer = new JPanel(new FlowLayout(FlowLayout.RIGHT, 6, 0));
        JButton none = new JButton(Messages.get("ACTION_DESC_NONE", "Do nothing"));
        none.addActionListener(e -> {
            model.clear();
            cancelled[0] = false;
            dialog.dispose();
        });
        JButton cancel = new JButton(Messages.get("BUTTON_CANCEL", "Cancel"));
        cancel.addActionListener(e -> dialog.dispose());
        JButton ok = new JButton(Messages.get("BUTTON_OK", "OK"));
        ok.addActionListener(e -> {
            cancelled[0] = false;
            dialog.dispose();
        });
        footer.add(none);
        footer.add(cancel);
        footer.add(ok);

        content.add(actions, BorderLayout.CENTER);
        content.add(footer, BorderLayout.SOUTH);
        dialog.setContentPane(content);
        dialog.setSize(new Dimension(460, 380));
        dialog.setLocationRelativeTo(null);
        dialog.setVisible(true);

        if (cancelled[0]) {
            return null;
        }
        List<String> codes = java.util.Collections.list(model.elements());
        return ActionChain.format(codes);
    }

    private static void showAddMenu(Studio studio, JButton anchor, DefaultListModel<String> model) {
        JPopupMenu menu = new JPopupMenu();
        JMenu vlcMenu = new JMenu(Messages.get("GROUP_VLC", "VLC"));
        JMenu dialogsMenu = new JMenu(Messages.get("GROUP_DIALOGS", "Dialogs"));
        JMenu playlistMenu = new JMenu(Messages.get("GROUP_PLAYLIST", "Playlist"));
        JMenu dvdMenu = new JMenu(Messages.get("GROUP_DVD", "DVD"));
        JMenu skinMenu = new JMenu(Messages.get("GROUP_SKIN", "Skin windows"));
        for (ActionCatalog.Action action : ActionCatalog.actions()) {
            JMenuItem item = new JMenuItem(action.display());
            item.setToolTipText(action.code());
            item.addActionListener(e -> add(studio, action, model));
            String code = action.code();
            if (code.startsWith("dialogs.")) {
                dialogsMenu.add(item);
            } else if (code.startsWith("playlist.")) {
                playlistMenu.add(item);
            } else if (code.startsWith("dvd.")) {
                dvdMenu.add(item);
            } else if (code.startsWith(".")) {
                skinMenu.add(item);
            } else {
                vlcMenu.add(item);
            }
        }
        menu.add(vlcMenu);
        menu.add(dialogsMenu);
        menu.add(playlistMenu);
        menu.add(dvdMenu);
        menu.add(skinMenu);
        menu.show(anchor, 0, anchor.getHeight());
    }

    private static void add(Studio studio, ActionCatalog.Action action, DefaultListModel<String> model) {
        switch (action.kind()) {
            case STATIC -> model.addElement(action.code());
            case BOOLEAN -> {
                String[] options = {Messages.get("ACTION_ACTIVATE", "Activate"),
                        Messages.get("ACTION_DEACTIVATE", "Deactivate")};
                int choice = JOptionPane.showOptionDialog(null, action.display(),
                        Messages.get("APP_ACTION_PARAMETER_TITLE", "Action parameter"),
                        JOptionPane.DEFAULT_OPTION, JOptionPane.QUESTION_MESSAGE, null, options, options[0]);
                if (choice >= 0) {
                    model.addElement(action.code().replace("()", "(" + (choice == 0) + ")"));
                }
            }
            case WINDOW -> {
                String window = pickWindow(studio, action.display());
                if (window != null) {
                    model.addElement(window + action.code());
                }
            }
            case WINDOW_STATE -> {
                String window = pickWindow(studio, action.display());
                if (window != null) {
                    model.addElement(window + action.code());
                }
            }
            case WINDOW_LAYOUT -> {
                String window = pickWindow(studio, action.display());
                if (window == null) {
                    return;
                }
                JComboBox<String> layouts = new JComboBox<>();
                var found = studio.session().index().findWindow(window);
                if (found != null) {
                    found.getLayouts().forEach(layout -> layouts.addItem(layout.getId()));
                }
                int result = JOptionPane.showConfirmDialog(null, layouts,
                        Messages.format("APP_ACTION_LAYOUT_TITLE", "Layout for %s", window),
                        JOptionPane.OK_CANCEL_OPTION);
                if (result == JOptionPane.OK_OPTION) {
                    model.addElement(window + ".setLayout(" + layouts.getSelectedItem() + ")");
                }
            }
        }
    }

    private static String pickWindow(Studio studio, String title) {
        var windows = studio.session().theme().getWindows();
        if (windows.isEmpty()) {
            return null;
        }
        JComboBox<String> box = new JComboBox<>();
        windows.forEach(window -> box.addItem(window.getId()));
        int result = JOptionPane.showConfirmDialog(null, box, title, JOptionPane.OK_CANCEL_OPTION);
        return result == JOptionPane.OK_OPTION ? (String) box.getSelectedItem() : null;
    }

    private static void move(DefaultListModel<String> model, int index, int delta) {
        int to = index + delta;
        if (index < 0 || to < 0 || to >= model.size()) {
            return;
        }
        String value = model.remove(index);
        model.add(to, value);
    }
}
