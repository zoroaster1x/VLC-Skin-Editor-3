package dev.zoroaster1x.vlcskin.app.dialog;

import dev.zoroaster1x.vlcskin.app.Studio;
import dev.zoroaster1x.vlcskin.app.i18n.Messages;
import dev.zoroaster1x.vlcskin.edit.ValueCommand;
import dev.zoroaster1x.vlcskin.model.resource.BitmapResource;
import dev.zoroaster1x.vlcskin.model.resource.SubBitmap;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.SpinnerNumberModel;

/**
 * Visual cutter for a sub bitmap: drag the frame over the parent image.
 */
public final class SubBitmapEditorDialog extends JDialog {

    private final Studio studio;
    private final BitmapResource bitmap;
    private final SubBitmap sub;
    private final ImageCanvas canvas;
    private final JSpinner xField;
    private final JSpinner yField;
    private final JSpinner widthField;
    private final JSpinner heightField;
    private final JSpinner framesSpinner;
    private final JSpinner fpsSpinner;
    private final BufferedImage parentImage;

    public SubBitmapEditorDialog(Studio studio, BitmapResource bitmap, SubBitmap sub) {
        super((java.awt.Frame) null, Messages.get("WIN_SBMP_EDIT_TITLE", "Edit SubBitmap") + " " + sub.getId(), true);
        this.studio = studio;
        this.bitmap = bitmap;
        this.sub = sub;
        setLayout(new BorderLayout(8, 8));
        ((JPanel) getContentPane()).setBorder(javax.swing.BorderFactory.createEmptyBorder(10, 10, 10, 10));

        BufferedImage image = studio.session().images().image(studio.session().index(), bitmap.getId());
        parentImage = image;
        canvas = new ImageCanvas(image);
        canvas.setPreferredSize(new Dimension(480, 360));
        add(canvas, BorderLayout.CENTER);
        xField = spinner(sub.getX(), 0, 10000);
        yField = spinner(sub.getY(), 0, 10000);
        widthField = spinner(sub.getWidth(), 1, 10000);
        heightField = spinner(sub.getHeight(), 1, 10000);
        JSpinner framesField = spinner(Math.max(1, sub.getNbframes()), 1, 100);
        JSpinner fpsField = spinner(Math.max(0, sub.getFps()), 0, 240);
        JPanel fields = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 0));
        fields.add(new JLabel(Messages.get("WIN_ITEM_X", "X")));
        fields.add(xField);
        fields.add(new JLabel(Messages.get("WIN_ITEM_Y", "Y")));
        fields.add(yField);
        fields.add(new JLabel(Messages.get("WIN_ITEM_WIDTH", "Width")));
        fields.add(widthField);
        fields.add(new JLabel(Messages.get("WIN_ITEM_HEIGHT", "Height")));
        fields.add(heightField);
        fields.add(new JLabel(Messages.get("WIN_BITMAP_NBFRAMES", "Frames")));
        fields.add(framesField);
        fields.add(new JLabel(Messages.get("WIN_BITMAP_FPS", "FPS")));
        fields.add(fpsField);
        add(fields, BorderLayout.NORTH);
        framesSpinner = framesField;
        fpsSpinner = fpsField;

        Runnable sync = () -> canvas.setFrame((Integer) xField.getValue(), (Integer) yField.getValue(),
                (Integer) widthField.getValue(), (Integer) heightField.getValue());
        xField.addChangeListener(e -> sync.run());
        yField.addChangeListener(e -> sync.run());
        widthField.addChangeListener(e -> sync.run());
        heightField.addChangeListener(e -> sync.run());
        canvas.setOnFrameMoved(rect -> {
            xField.setValue(rect.x);
            yField.setValue(rect.y);
            widthField.setValue(rect.width);
            heightField.setValue(rect.height);
        });

        JPanel footer = new JPanel(new FlowLayout(FlowLayout.RIGHT, 6, 0));
        JButton cancel = new JButton(Messages.get("BUTTON_CANCEL", "Cancel"));
        cancel.addActionListener(e -> dispose());
        JButton ok = new JButton(Messages.get("BUTTON_OK", "OK"));
        ok.addActionListener(e -> {
            apply();
            dispose();
        });
        footer.add(cancel);
        footer.add(ok);
        add(footer, BorderLayout.SOUTH);
        pack();
        setLocationRelativeTo(null);
    }

    private JSpinner spinner(int value, int min, int max) {
        JSpinner spinner = new JSpinner(new SpinnerNumberModel(value, min, max, 1));
        spinner.setPreferredSize(new Dimension(70, 24));
        return spinner;
    }

    private void apply() {
        int x = (Integer) xField.getValue();
        int y = (Integer) yField.getValue();
        int width = (Integer) widthField.getValue();
        int height = (Integer) heightField.getValue();
        int frames = (Integer) framesSpinner.getValue();
        int fps = (Integer) fpsSpinner.getValue();
        if (parentImage != null && (x + width > parentImage.getWidth() || y + height > parentImage.getHeight())) {
            JOptionPane.showMessageDialog(this,
                    Messages.format("APP_SBMP_OUTSIDE_MSG",
                            "The rectangle extends beyond the parent bitmap (%s).",
                            parentImage.getWidth() + "x" + parentImage.getHeight()),
                    Messages.get("APP_SBMP_OUTSIDE_TITLE", "SubBitmap outside its parent"),
                    JOptionPane.WARNING_MESSAGE);
            return;
        }
        int oldX = sub.getX();
        int oldY = sub.getY();
        int oldWidth = sub.getWidth();
        int oldHeight = sub.getHeight();
        int oldFrames = sub.getNbframes();
        int oldFps = sub.getFps();
        if (x == oldX && y == oldY && width == oldWidth && height == oldHeight
                && frames == oldFrames && fps == oldFps) {
            return;
        }
        studio.session().apply(ValueCommand.builder("Edit SubBitmap")
                .step(() -> set(x, y, width, height, frames, fps),
                        () -> set(oldX, oldY, oldWidth, oldHeight, oldFrames, oldFps))
                .build());
        studio.session().images().invalidate(bitmap.getId());
        studio.session().images().invalidate(sub.getId());
        studio.session().fireChanged();
    }

    private void set(int x, int y, int width, int height, int frames, int fps) {
        sub.setX(x);
        sub.setY(y);
        sub.setWidth(width);
        sub.setHeight(height);
        sub.setNbframes(frames);
        sub.setFps(fps);
    }

    /**
     * The image with the draggable frame.
     */
    private static final class ImageCanvas extends JPanel {

        private final BufferedImage image;
        private Rectangle frame = new Rectangle();
        private Point dragOffset;
        private java.util.function.Consumer<Rectangle> onFrameMoved;

        ImageCanvas(BufferedImage image) {
            this.image = image;
            setBackground(new Color(0x20, 0x22, 0x28));
        }

        void setOnFrameMoved(java.util.function.Consumer<Rectangle> listener) {
            this.onFrameMoved = listener;
        }

        void setFrame(int x, int y, int width, int height) {
            frame = new Rectangle(x, y, width, height);
            repaint();
        }

        private double scale() {
            if (image == null) {
                return 1;
            }
            return Math.min((double) getWidth() / image.getWidth(), (double) getHeight() / image.getHeight());
        }

        private Point toImage(Point point) {
            double scale = scale();
            int offsetX = (int) ((getWidth() - image.getWidth() * scale) / 2);
            int offsetY = (int) ((getHeight() - image.getHeight() * scale) / 2);
            return new Point((int) ((point.x - offsetX) / scale), (int) ((point.y - offsetY) / scale));
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            if (image == null) {
                return;
            }
            Graphics2D g2 = (Graphics2D) g.create();
            try {
                g2.setRenderingHint(java.awt.RenderingHints.KEY_INTERPOLATION,
                        java.awt.RenderingHints.VALUE_INTERPOLATION_NEAREST_NEIGHBOR);
                double scale = scale();
                int width = (int) (image.getWidth() * scale);
                int height = (int) (image.getHeight() * scale);
                int offsetX = (getWidth() - width) / 2;
                int offsetY = (getHeight() - height) / 2;
                g2.drawImage(image, offsetX, offsetY, width, height, null);
                g2.setColor(new Color(0xE0, 0x6C, 0x38));
                g2.setStroke(new java.awt.BasicStroke(1.5f));
                g2.drawRect(offsetX + (int) (frame.x * scale), offsetY + (int) (frame.y * scale),
                        (int) (frame.width * scale), (int) (frame.height * scale));
                g2.setColor(new Color(0xE0, 0x6C, 0x38, 40));
                g2.fillRect(offsetX + (int) (frame.x * scale), offsetY + (int) (frame.y * scale),
                        (int) (frame.width * scale), (int) (frame.height * scale));
            } finally {
                g2.dispose();
            }
        }

        {
            MouseAdapter adapter = new MouseAdapter() {
                @Override
                public void mousePressed(MouseEvent e) {
                    if (image == null) {
                        return;
                    }
                    Point point = toImage(e.getPoint());
                    if (frame.contains(point)) {
                        dragOffset = new Point(point.x - frame.x, point.y - frame.y);
                        setCursor(Cursor.getPredefinedCursor(Cursor.MOVE_CURSOR));
                    } else {
                        dragOffset = null;
                    }
                }

                @Override
                public void mouseDragged(MouseEvent e) {
                    if (dragOffset == null || image == null) {
                        return;
                    }
                    Point point = toImage(e.getPoint());
                    int x = Math.max(0, Math.min(image.getWidth() - frame.width, point.x - dragOffset.x));
                    int y = Math.max(0, Math.min(image.getHeight() - frame.height, point.y - dragOffset.y));
                    frame.setLocation(x, y);
                    repaint();
                }
                @Override
                public void mouseReleased(MouseEvent e) {
                    if (onFrameMoved != null && dragOffset != null) {
                        onFrameMoved.accept(new Rectangle(frame));
                    }
                    dragOffset = null;
                    setCursor(Cursor.getDefaultCursor());
                }
            };
            addMouseListener(adapter);
            addMouseMotionListener(adapter);
        }
    }
}
