package io.github.zoroaster1x.vlcskin.app.inspector;

import io.github.zoroaster1x.vlcskin.app.Studio;
import io.github.zoroaster1x.vlcskin.app.component.Icons;
import io.github.zoroaster1x.vlcskin.model.resource.BitmapResource;
import io.github.zoroaster1x.vlcskin.model.resource.FontResource;
import io.github.zoroaster1x.vlcskin.model.resource.Resource;
import io.github.zoroaster1x.vlcskin.model.resource.SubBitmap;
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
        form.setLayout(new MigLayout("insets 10, fillx, wrap 2", "[right]10[grow,fill]"));
    }

    public void section(String title) {
        JLabel label = new JLabel(title);
        label.setFont(label.getFont().deriveFont(java.awt.Font.BOLD, 11f));
        label.setForeground(javax.swing.UIManager.getColor("Label.disabledForeground"));
        form.add(label, "growx, span 2, gaptop 8, gapbottom 2");
    }

    public void note(String text) {
        JLabel label = new JLabel("<html><span style='font-size:9px'>" + text + "</span></html>");
        label.setForeground(javax.swing.UIManager.getColor("Label.disabledForeground"));
        form.add(label, "span 2, growx, gapbottom 4");
    }

    public void row(String label, JComponent editor) {
        form.add(new JLabel(label));
        form.add(editor, "growx");
    }

    public JTextField text(String value, Consumer<String> commit) {
        JTextField field = new JTextField(value == null ? "" : value);
        commitOn(field, commit);
        return field;
    }

    public JSpinner integer(int value, Consumer<Integer> commit) {
        JSpinner spinner = new JSpinner(new SpinnerNumberModel(value, -32000, 32000, 1));
        spinner.addChangeListener(e -> commit.accept((Integer) spinner.getValue()));
        return spinner;
    }

    public JSpinner integer(int value, int min, int max, Consumer<Integer> commit) {
        JSpinner spinner = new JSpinner(new SpinnerNumberModel(value, min, max, 1));
        spinner.addChangeListener(e -> commit.accept((Integer) spinner.getValue()));
        return spinner;
    }

    public JCheckBox bool(boolean value, Consumer<Boolean> commit) {
        JCheckBox box = new JCheckBox();
        box.setSelected(value);
        box.addActionListener(e -> commit.accept(box.isSelected()));
        return box;
    }

    public JComboBox<String> combo(String value, List<String> options, Consumer<String> commit) {
        JComboBox<String> box = new JComboBox<>(options.toArray(String[]::new));
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
        swatch.setToolTipText("Choose color");
        paintSwatch(swatch, value);
        swatch.addActionListener(e -> {
            Color chosen = JColorChooser.showDialog(studio.service().session().currentLayout() == null
                    ? null : swatch, "Choose color", parseColor(field.getText()));
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
        JPanel panel = new JPanel(new java.awt.BorderLayout(4, 0));
        panel.setOpaque(false);
        JTextField field = new JTextField(value == null ? "none" : value);
        JButton edit = new JButton(Icons.of("playlist", 14));
        edit.setToolTipText("Edit actions");
        edit.addActionListener(e -> {
            String result = io.github.zoroaster1x.vlcskin.app.dialog.ActionEditorDialog.edit(studio, field.getText());
            if (result != null) {
                field.setText(result);
                commit.accept(result);
            }
        });
        panel.add(field, java.awt.BorderLayout.CENTER);
        panel.add(edit, java.awt.BorderLayout.EAST);
        commitOn(field, commit);
        return panel;
    }

    /**
     * A points field with a hint about the path tool.
     */
    public JComponent points(String value, Consumer<String> commit) {
        JTextField field = new JTextField(value == null ? "(0,0)" : value);
        field.setToolTipText("Bezier control points as (x,y),(x,y). Use the path tool on the canvas to drag them.");
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
