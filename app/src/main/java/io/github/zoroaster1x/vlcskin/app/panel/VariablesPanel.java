package io.github.zoroaster1x.vlcskin.app.panel;

import io.github.zoroaster1x.vlcskin.action.GlobalVariableCatalog;
import io.github.zoroaster1x.vlcskin.app.Studio;
import io.github.zoroaster1x.vlcskin.app.i18n.Messages;
import java.awt.BorderLayout;
import java.awt.GridLayout;
import java.util.LinkedHashMap;
import java.util.Map;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSlider;
import javax.swing.JTextField;

/**
 * Simulates the player state the preview renders against.
 */
public final class VariablesPanel extends JPanel {

    private final Studio studio;
    private final JSlider slider = new JSlider(0, 100);
    private final Map<String, JCheckBox> booleans = new LinkedHashMap<>();
    private final Map<String, JTextField> texts = new LinkedHashMap<>();
    private boolean updating;

    public VariablesPanel(Studio studio) {
        this.studio = studio;
        setLayout(new BorderLayout());
        JPanel content = new JPanel();
        content.setLayout(new BoxLayout(content, BoxLayout.Y_AXIS));
        content.setBorder(BorderFactory.createEmptyBorder(8, 10, 8, 10));

        JLabel sliderLabel = new JLabel(Messages.get("WIN_VARS_SLIDER", "Slider position"));
        sliderLabel.setAlignmentX(LEFT_ALIGNMENT);
        slider.setAlignmentX(LEFT_ALIGNMENT);
        slider.addChangeListener(e -> {
            if (!updating) {
                studio.session().variables().setSliderValue(slider.getValue() / 100f);
                studio.session().fireChanged();
            }
        });
        content.add(sliderLabel);
        content.add(slider);
        content.add(Box.createVerticalStrut(8));

        JPanel grid = new JPanel(new GridLayout(0, 1, 2, 2));
        grid.setAlignmentX(LEFT_ALIGNMENT);
        for (GlobalVariableCatalog.BooleanVariable variable : GlobalVariableCatalog.BOOLEANS) {
            JCheckBox box = new JCheckBox(variable.label());
            box.setToolTipText(variable.name());
            box.addActionListener(e -> {
                if (!updating) {
                    studio.session().variables().setBoolean(variable.name(), box.isSelected());
                    studio.session().fireChanged();
                }
            });
            booleans.put(variable.name(), box);
            grid.add(box);
        }
        content.add(grid);
        content.add(Box.createVerticalStrut(8));

        JPanel textGrid = new JPanel(new GridLayout(0, 2, 4, 2));
        textGrid.setAlignmentX(LEFT_ALIGNMENT);
        for (GlobalVariableCatalog.TextVariable variable : GlobalVariableCatalog.TEXTS) {
            JLabel label = new JLabel(variable.token() + "  " + variable.label());
            JTextField field = new JTextField(variable.sample());
            field.addActionListener(e -> {
                studio.session().variables().setText(variable.token(), field.getText());
                studio.session().fireChanged();
            });
            texts.put(variable.token(), field);
            textGrid.add(label);
            textGrid.add(field);
        }
        content.add(textGrid);
        content.add(Box.createVerticalStrut(8));
        JLabel note = new JLabel(Messages.get("WIN_VARS_NOTE",
                "These variables only affect the preview. They simulate the state of VLC."));
        note.setForeground(javax.swing.UIManager.getColor("Label.disabledForeground"));
        note.setAlignmentX(LEFT_ALIGNMENT);
        content.add(note);
        content.add(Box.createVerticalGlue());
        add(new JScrollPane(content), BorderLayout.CENTER);
    }

    /**
     * Pulls the current session variables into the widgets.
     */
    public void refresh() {
        updating = true;
        try {
            slider.setValue(Math.round(studio.session().variables().sliderValue() * 100));
            booleans.forEach((name, box) -> box.setSelected(studio.session().variables().getBoolean(name)));
            texts.forEach((name, field) -> {
                String value = studio.session().variables().getText(name);
                if (value != null) {
                    field.setText(value);
                }
            });
        } finally {
            updating = false;
        }
    }
}
