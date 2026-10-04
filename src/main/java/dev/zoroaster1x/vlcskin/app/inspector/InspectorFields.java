package dev.zoroaster1x.vlcskin.app.inspector;

import dev.zoroaster1x.vlcskin.app.Studio;
import dev.zoroaster1x.vlcskin.app.component.Icons;
import dev.zoroaster1x.vlcskin.app.i18n.Messages;
import dev.zoroaster1x.vlcskin.model.resource.BitmapResource;
import dev.zoroaster1x.vlcskin.model.resource.FontResource;
import dev.zoroaster1x.vlcskin.model.resource.Resource;
import dev.zoroaster1x.vlcskin.model.resource.SubBitmap;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JColorChooser;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.JTextField;
import javax.swing.SpinnerNumberModel;
import net.miginfocom.swing.MigLayout;

/**
 * Reusable inspector rows that commit every change through the studio.
 */
public final class InspectorFields {

    private final Studio studio;
    private final JPanel form;

    public InspectorFields(Studio studio, JPanel form) {
        this.studio = studio;
        this.form = form;
        // Right aligned labels, one growing column. Editors are capped so a
        // wide inspector does not stretch a two digit field across the panel,
        // and their minimums are small enough to fit a narrow dock without
        // clipping (there is no horizontal scrollbar on purpose).
        form.setLayout(new MigLayout("insets 8, fillx, wrap 2, hidemode 3", "[right,pref!]8[grow,fill]"));
    }

    public void section(String title) {
        JLabel label = new JLabel(title);
        label.setFont(label.getFont().deriveFont(java.awt.Font.BOLD));
        label.setForeground(javax.swing.UIManager.getColor("Label.disabledForeground"));
        form.add(label, "growx, span 2, gaptop 8, gapbottom 2");
    }

    public void note(String text) {
        NoteArea label = new NoteArea(text);
        form.add(label, "span 2, growx, width 40:200:10000, gapbottom 4");
    }

    /**
     * A note that wraps with the inspector width instead of forcing it wider.
     * Backed by a JTextArea so the height is the real wrapped height, at any
     * font scale.
     */
    private static final class NoteArea extends javax.swing.JTextArea
            implements javax.swing.Scrollable {
        NoteArea(String text) {
            super(stripHtml(text));
            setLineWrap(true);
            setWrapStyleWord(true);
            setEditable(false);
            setOpaque(false);
            setBorder(null);
            setFocusable(false);
            setRows(1);
            setForeground(javax.swing.UIManager.getColor("Label.disabledForeground"));
        }

        private static String stripHtml(String text) {
            return text == null ? "" : text.replaceAll("<[^>]+>", "");
        }

        @Override
        public Dimension getPreferredSize() {
            int width = getWidth();
            if (width <= 0) {
                width = getParent() == null ? 200 : Math.max(120, getParent().getWidth() - 16);
            }
            setSize(width, Integer.MAX_VALUE);
            Dimension size = super.getPreferredSize();
            return new Dimension(width, Math.max(size.height, getFontMetrics(getFont()).getHeight()));
        }

        @Override
        public Dimension getPreferredScrollableViewportSize() {
            return getPreferredSize();
        }

        @Override
        public int getScrollableUnitIncrement(java.awt.Rectangle visible, int orientation, int direction) {
            return 16;
        }

        @Override
        public int getScrollableBlockIncrement(java.awt.Rectangle visible, int orientation, int direction) {
            return 64;
        }

        @Override
        public boolean getScrollableTracksViewportWidth() {
            return true;
        }

        @Override
        public boolean getScrollableTracksViewportHeight() {
            return false;
        }
    }

    public void row(String label, JComponent editor) {
        form.add(new JLabel(label));
        form.add(editor, "growx, width 40:180:320");
    }

    public JTextField text(String value, Consumer<String> commit) {
        JTextField field = new JTextField(value == null ? "" : value);
        field.setMinimumSize(new Dimension(40, field.getPreferredSize().height));
        commitOn(field, commit);
        return field;
    }

    public JSpinner integer(int value, Consumer<Integer> commit) {
        JSpinner spinner = new JSpinner(safeModel(value, -32000, 32000, 1));
        spinner.setMinimumSize(new Dimension(50, spinner.getPreferredSize().height));
        spinner.addChangeListener(e -> commit.accept((Integer) spinner.getValue()));
        return spinner;
    }

    public JSpinner integer(int value, int min, int max, Consumer<Integer> commit) {
        JSpinner spinner = new JSpinner(safeModel(value, min, max, 1));
        spinner.setMinimumSize(new Dimension(50, spinner.getPreferredSize().height));
        spinner.addChangeListener(e -> commit.accept((Integer) spinner.getValue()));
        return spinner;
    }

    /**
     * A spinner model whose range always contains the current value. Real
     * themes carry values outside the editor's sensible ranges (a maxwidth of
     * 99999, a zero opacity), and opening such a value must not throw.
     */
    public static SpinnerNumberModel safeModel(int value, int min, int max, int step) {
        return new SpinnerNumberModel(value, Math.min(min, value), Math.max(max, value), step);
    }

    public JCheckBox bool(boolean value, Consumer<Boolean> commit) {
        JCheckBox box = new JCheckBox();
        box.setSelected(value);
        box.addActionListener(e -> commit.accept(box.isSelected()));
        return box;
    }

    public JComboBox<String> combo(String value, List<String> options, Consumer<String> commit) {
        JComboBox<String> box = new JComboBox<>(options.toArray(String[]::new));
        box.setMinimumSize(new Dimension(50, box.getPreferredSize().height));
        if (value != null && options.contains(value)) {
            box.setSelectedItem(value);
        } else if (value != null) {
            box.addItem(value);
            box.setSelectedItem(value);
        }
        box.addActionListener(e -> {
            Object selected = box.getSelectedItem();
            commit.accept(selected == null ? "none" : selected.toString());
        });
        return box;
    }

    /**
     * A color field with a swatch button that opens a chooser.
     */
    public JComponent color(String value, Consumer<String> commit) {
        JPanel panel = new JPanel(new java.awt.BorderLayout(4, 0));
        panel.setOpaque(false);
        JTextField field = new JTextField(value == null ? "#000000" : value);
        panel.add(field, java.awt.BorderLayout.CENTER);
        JButton swatch = new JButton();
        swatch.setPreferredSize(new Dimension(28, 22));
        swatch.setToolTipText(Messages.get("WIN_PLAYTREE_CHOOSER_TITLE", "Choose color"));
        paintSwatch(swatch, value);
        swatch.addActionListener(e -> {
            Color chosen = JColorChooser.showDialog(studio.service().session().currentLayout() == null
                    ? null : swatch, Messages.get("WIN_PLAYTREE_CHOOSER_TITLE", "Choose color"),
                    parseColor(field.getText()));
            if (chosen != null) {
                String hex = String.format("#%02X%02X%02X", chosen.getRed(), chosen.getGreen(), chosen.getBlue());
                field.setText(hex);
                paintSwatch(swatch, hex);
                commit.accept(hex);
            }
        });
        panel.add(swatch, java.awt.BorderLayout.EAST);
        commitOn(field, text -> {
            paintSwatch(swatch, text);
            commit.accept(text);
        });
        return panel;
    }

    private void paintSwatch(JButton button, String value) {
        button.setBackground(parseColor(value));
        button.setOpaque(true);
        button.setBorderPainted(false);
    }

    private Color parseColor(String value) {
        try {
            return Color.decode(value.startsWith("#") ? value : "#" + value);
        } catch (RuntimeException ex) {
            return Color.GRAY;
        }
    }

    /**
     * A combo of image (or font) resource ids plus none/defaultfont.
     */
    public JComboBox<String> resourceCombo(String value, boolean fonts, Consumer<String> commit) {
        List<String> options = new ArrayList<>();
        options.add("none");
        if (fonts) {
            options.add("defaultfont");
        }
        for (Resource resource : studio.session().theme().getResources()) {
            if (fonts && resource instanceof FontResource) {
                options.add(resource.getId());
            } else if (!fonts && resource instanceof BitmapResource bitmap) {
                options.add(bitmap.getId());
                for (SubBitmap sub : bitmap.getSubBitmaps()) {
                    options.add(sub.getId());
                }
            }
        }
        return combo(value, options, commit);
    }

    /**
     * An action attribute with an editor button.
     */
    public JComponent action(String value, Consumer<String> commit) {
        return actionPanel(actionText(value, commit));
    }

    /**
     * The raw action text field, for rows that want to sync a preset combo
     * with the same attribute.
     */
    public JTextField actionText(String value, Consumer<String> commit) {
        JTextField field = new JTextField(value == null ? "none" : value);
        commitOn(field, commit);
        return field;
    }

    /**
     * Wraps an action text field with the chain editor button.
     */
    public JComponent actionPanel(JTextField field) {
        JPanel panel = new JPanel(new java.awt.BorderLayout(4, 0));
        panel.setOpaque(false);
        JButton edit = new JButton(Icons.of("playlist", 14));
        edit.setToolTipText(Messages.get("APP_INSPECTOR_EDIT_ACTIONS", "Edit actions"));
        edit.addActionListener(e -> {
            String result = dev.zoroaster1x.vlcskin.app.dialog.ActionEditorDialog.edit(studio, field.getText());
            if (result != null) {
                field.setText(result);
                for (java.awt.event.ActionListener listener : field.getActionListeners()) {
                    listener.actionPerformed(new java.awt.event.ActionEvent(field,
                            java.awt.event.ActionEvent.ACTION_PERFORMED, result));
                }
            }
        });
        panel.add(field, java.awt.BorderLayout.CENTER);
        panel.add(edit, java.awt.BorderLayout.EAST);
        return panel;
    }

    /**
     * A points field with a hint about the path tool.
     */
    public JComponent points(String value, Consumer<String> commit) {
        JTextField field = new JTextField(value == null ? "(0,0)" : value);
        field.setToolTipText(Messages.get("APP_INSPECTOR_POINTS_TIP",
                "Bezier control points as (x,y),(x,y). Use the path tool on the canvas to drag them."));
        commitOn(field, commit);
        return field;
    }

    private void commitOn(JTextField field, Consumer<String> commit) {
        field.addActionListener(e -> commit.accept(field.getText()));
        field.addFocusListener(new FocusAdapter() {
            @Override
            public void focusLost(FocusEvent e) {
                commit.accept(field.getText());
            }
        });
    }
}
