package dev.zoroaster1x.vlcskin.app.dialog;

import dev.zoroaster1x.vlcskin.app.Studio;
import dev.zoroaster1x.vlcskin.app.config.AppPaths;
import dev.zoroaster1x.vlcskin.app.i18n.Messages;
import dev.zoroaster1x.vlcskin.edit.ValueCommand;
import dev.zoroaster1x.vlcskin.model.resource.BitmapResource;
import dev.zoroaster1x.vlcskin.model.item.SliderBackground;
import dev.zoroaster1x.vlcskin.render.SliderBackgroundGenerator;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.image.BufferedImage;
import java.nio.file.Files;
import java.nio.file.Path;
import javax.imageio.ImageIO;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JSpinner;
import javax.swing.SpinnerNumberModel;
import javax.swing.filechooser.FileNameExtensionFilter;

/**
 * The slider background wizard: compose a strip and register it as a bitmap.
 */
public final class SliderBackgroundGeneratorDialog extends JDialog {

    private final Studio studio;
    private final SliderBackground background;
    private final JSpinner width = new JSpinner(new SpinnerNumberModel(180, 8, 4000, 1));
    private final JSpinner height = new JSpinner(new SpinnerNumberModel(10, 2, 4000, 1));
    private final JSpinner marginLeft = new JSpinner(new SpinnerNumberModel(0, 0, 1000, 1));
    private final JSpinner marginRight = new JSpinner(new SpinnerNumberModel(0, 0, 1000, 1));
    private final JSpinner marginTop = new JSpinner(new SpinnerNumberModel(0, 0, 1000, 1));
    private final JSpinner marginBottom = new JSpinner(new SpinnerNumberModel(0, 0, 1000, 1));
    private final JRadioButton horizontal = new JRadioButton(Messages.get("APP_SBG_LEFT_RIGHT", "Left to right"), true);
    private final JRadioButton vertical = new JRadioButton(Messages.get("APP_SBG_BOTTOM_TOP", "Bottom to top"));
    private final JCheckBox tileBackground = new JCheckBox(Messages.get("APP_SBG_TILE_BACKGROUND", "Tile background"), true);
    private final JCheckBox tileMiddle = new JCheckBox(Messages.get("APP_SBG_TILE_MIDDLE", "Tile middle"), true);
    private Path backgroundFile;
    private Path edge1File;
    private Path middleFile;
    private Path edge2File;
    private Path overlayFile;

    public SliderBackgroundGeneratorDialog(Studio studio, SliderBackground background) {
        super((java.awt.Frame) null, Messages.get("SBGGEN_TITLE", "Slider background generator"), true);
        this.studio = studio;
        this.background = background;
        setLayout(new BorderLayout(8, 8));
        JPanel content = new JPanel();
        content.setLayout(new BoxLayout(content, BoxLayout.Y_AXIS));
        content.setBorder(BorderFactory.createEmptyBorder(12, 12, 12, 12));

        JPanel orientation = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 0));
        ButtonGroup group = new ButtonGroup();
        group.add(horizontal);
        group.add(vertical);
        orientation.add(new JLabel(Messages.get("APP_SBG_DIRECTION", "Direction")));
        orientation.add(horizontal);
        orientation.add(vertical);
        content.add(orientation);
        content.add(Box.createVerticalStrut(6));
        content.add(row(Messages.get("APP_SBG_SIZE", "Width / height"), width, height));
        content.add(row(Messages.get("APP_SBG_MARGIN_H", "Left / right margin"), marginLeft, marginRight));
        content.add(row(Messages.get("APP_SBG_MARGIN_V", "Top / bottom margin"), marginTop, marginBottom));
        content.add(Box.createVerticalStrut(6));
        content.add(fileRow(Messages.get("SBGGEN_BG", "Background"), file -> backgroundFile = file));
        content.add(fileRow(Messages.get("APP_SBG_START_EDGE", "Start edge"), file -> edge1File = file));
        content.add(fileRow(Messages.get("SBGGEN_MIDDLE", "Middle (required)"), file -> middleFile = file));
        content.add(fileRow(Messages.get("APP_SBG_END_EDGE", "End edge"), file -> edge2File = file));
        content.add(fileRow(Messages.get("SBGGEN_OVERLAY", "Overlay"), file -> overlayFile = file));
        content.add(Box.createVerticalStrut(6));
        JPanel options = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 0));
        options.add(tileBackground);
        options.add(tileMiddle);
        content.add(options);
        JLabel note = new JLabel(Messages.get("APP_SBG_NOTE",
                "<html><span style='font-size:9px'>The strip gets one frame per pixel of "
                        + "travel, which is how VLC picks the filled part.</span></html>"));
        content.add(note);

        JPanel footer = new JPanel(new FlowLayout(FlowLayout.RIGHT, 6, 0));
        JButton cancel = new JButton(Messages.get("BUTTON_CANCEL", "Cancel"));
        cancel.addActionListener(e -> dispose());
        JButton generate = new JButton(Messages.get("SBGGEN_FINISH", "Generate and use"));
        generate.addActionListener(e -> generate());
        footer.add(cancel);
        footer.add(generate);

        add(content, BorderLayout.CENTER);
        add(footer, BorderLayout.SOUTH);
        setPreferredSize(new Dimension(520, 420));
        pack();
        setLocationRelativeTo(null);
    }

    private JPanel row(String label, JSpinner first, JSpinner second) {
        JPanel row = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 0));
        JLabel name = new JLabel(label);
        name.setPreferredSize(new Dimension(150, 22));
        row.add(name);
        first.setPreferredSize(new Dimension(80, 24));
        second.setPreferredSize(new Dimension(80, 24));
        row.add(first);
        row.add(second);
        row.setMaximumSize(new Dimension(Integer.MAX_VALUE, 30));
        return row;
    }

    private JPanel fileRow(String label, java.util.function.Consumer<Path> setter) {
        JPanel row = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 0));
        JLabel name = new JLabel(label);
        name.setPreferredSize(new Dimension(150, 22));
        JLabel value = new JLabel(Messages.get("APP_SBG_NO_FILE", "(none)"));
        value.setPreferredSize(new Dimension(220, 22));
        JButton pick = new JButton(Messages.get("WIN_BITMAP_CHOOSE", "Choose..."));
        pick.addActionListener(e -> {
            JFileChooser chooser = new JFileChooser();
            chooser.setFileFilter(new FileNameExtensionFilter(
                    Messages.get("ADD_BMP_FILE_FILTER_DESC", "PNG image (*.png)"), "png"));
            Path start = studio.session().file() == null ? null : studio.session().file().getParent();
            if (start != null) {
                chooser.setCurrentDirectory(start.toFile());
            }
            if (chooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
                Path file = chooser.getSelectedFile().toPath();
                setter.accept(file);
                value.setText(file.getFileName().toString());
            }
        });
        row.add(name);
        row.add(pick);
        row.add(value);
        row.setMaximumSize(new Dimension(Integer.MAX_VALUE, 30));
        return row;
    }

    private void generate() {
        if (middleFile == null) {
            JOptionPane.showMessageDialog(this, Messages.get("ERROR_SBGGEN_MIDDLE_MSG",
                            "Could not load middle image!\nAt least the middle part of the slider is needed "
                                    + "to generate a slider background."),
                    Messages.get("SBGGEN_TITLE", "Slider background generator"), JOptionPane.WARNING_MESSAGE);
            return;
        }
        try {
            BufferedImage middle = ImageIO.read(middleFile.toFile());
            var spec = new SliderBackgroundGenerator.Spec(
                    (Integer) width.getValue(), (Integer) height.getValue(),
                    (Integer) marginLeft.getValue(), (Integer) marginRight.getValue(),
                    (Integer) marginTop.getValue(), (Integer) marginBottom.getValue(),
                    horizontal.isSelected(), tileBackground.isSelected(), tileMiddle.isSelected(),
                    readOrNull(backgroundFile), readOrNull(edge1File), middle, readOrNull(edge2File),
                    readOrNull(overlayFile));
            BufferedImage strip = SliderBackgroundGenerator.generate(spec);
            Path folder = studio.session().file() == null
                    ? AppPaths.exportsDir()
                    : studio.session().file().getParent();
            Files.createDirectories(folder);
            Path target = folder.resolve(background.getId() + "_bg.png");
            ImageIO.write(strip, "png", target.toFile());
            if (studio.session().file() != null) {
                String relative = folder.relativize(target).toString().replace('\\', '/');
                registerBitmap(relative, strip);
            }
            dispose();
            JOptionPane.showMessageDialog(null,
                    Messages.format("APP_SBG_GENERATED", "Generated %s", target.getFileName()),
                    Messages.get("SBGGEN_TITLE", "Slider background generator"), JOptionPane.INFORMATION_MESSAGE);
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this,
                    Messages.format("APP_SBG_FAILED", "Could not generate the background: %s", ex.getMessage()),
                    Messages.get("SBGGEN_TITLE", "Slider background generator"), JOptionPane.ERROR_MESSAGE);
        }
    }

    private BufferedImage readOrNull(Path file) throws java.io.IOException {
        return file == null ? null : ImageIO.read(file.toFile());
    }

    private void registerBitmap(String relative, BufferedImage strip) {
        BitmapResource bitmap = new BitmapResource();
        bitmap.setId(studio.session().index().uniqueUnnamed(background.getId() + " bg"));
        bitmap.setFile(relative);
        int frames = orientationFrames(strip);
        studio.session().apply(ValueCommand.builder("Generate slider background")
                .step(() -> {
                    studio.session().theme().getResources().add(bitmap);
                    applyBackground(bitmap.getId(), frames);
                }, () -> {
                    studio.session().theme().getResources().remove(bitmap);
                    applyBackground("none", 1);
                })
                .build());
        studio.session().images().invalidate(bitmap.getId());
        studio.session().fireChanged();
    }

    private int orientationFrames(BufferedImage strip) {
        if (horizontal.isSelected()) {
            return Math.max(1, strip.getHeight() / Math.max(1, (Integer) height.getValue()));
        }
        return Math.max(1, strip.getWidth() / Math.max(1, (Integer) width.getValue()));
    }

    private void applyBackground(String imageId, int frames) {
        background.setImage(imageId);
        if (horizontal.isSelected()) {
            background.setNbhoriz(1);
            background.setNbvert(Math.max(1, frames));
        } else {
            background.setNbhoriz(Math.max(1, frames));
            background.setNbvert(1);
        }
        background.setPadhoriz(0);
        background.setPadvert(0);
    }
}
