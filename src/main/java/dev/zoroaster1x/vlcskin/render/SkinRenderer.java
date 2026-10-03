package dev.zoroaster1x.vlcskin.render;

import dev.zoroaster1x.vlcskin.model.SkinIndex;
import dev.zoroaster1x.vlcskin.model.SkinLayout;
import dev.zoroaster1x.vlcskin.model.item.AbstractItem;
import dev.zoroaster1x.vlcskin.model.item.AnchorItem;
import dev.zoroaster1x.vlcskin.model.item.Item;
import dev.zoroaster1x.vlcskin.model.item.SliderItem;
import dev.zoroaster1x.vlcskin.render.draw.DrawContext;
import dev.zoroaster1x.vlcskin.render.draw.ItemPainters;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.geom.Point2D;
import java.awt.image.BufferedImage;

/**
 * Paints a layout the way VLC's skins2 would, with editing overlays on top.
 */
public final class SkinRenderer {

    public static final Color SELECTION = new Color(0xE0, 0x30, 0x30);
    public static final Color HOVER = new Color(0x2D, 0x8C, 0xFF);
    private static final Color CHECKER_LIGHT = new Color(0xF2, 0xF2, 0xF2);
    private static final Color CHECKER_DARK = new Color(0xDA, 0xDA, 0xDA);

    private final SkinIndex index;
    private final ImageStore images;

    public SkinRenderer(SkinIndex index, ImageStore images) {
        this.index = index;
        this.images = images;
    }

    public ImageStore images() {
        return images;
    }

    public SkinIndex index() {
        return index;
    }

    public Dimension sizeOf(SkinLayout layout, int zoom) {
        return new Dimension(Math.max(1, layout.getWidth() * zoom), Math.max(1, layout.getHeight() * zoom));
    }

    /**
     * Renders a layout to a new ARGB image.
     */
    public BufferedImage render(SkinLayout layout, RenderOptions options) {
        Dimension size = sizeOf(layout, options.zoom());
        BufferedImage image = new BufferedImage(size.width, size.height, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = image.createGraphics();
        try {
            paint(g, layout, options);
        } finally {
            g.dispose();
        }
        return image;
    }

    /**
     * Paints a layout into an existing graphics context.
     */
    public void paint(Graphics2D g, SkinLayout layout, RenderOptions options) {
        int zoom = Math.max(1, options.zoom());
        Object oldInterpolation = g.getRenderingHint(RenderingHints.KEY_INTERPOLATION);
        g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_NEAREST_NEIGHBOR);
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        if (options.checkerboard()) {
            paintCheckerboard(g, layout.getWidth() * zoom, layout.getHeight() * zoom);
        }
        java.awt.geom.AffineTransform old = g.getTransform();
        g.scale(zoom, zoom);
        DrawContext context = new DrawContext(index, images, options);
        for (Item item : layout.getItems()) {
            ItemPainters.draw(g, item, 0, 0, context);
        }
        if (options.selectionOverlays()) {
            paintOverlays(g, layout, options);
        }
        g.setTransform(old);
        if (oldInterpolation != null) {
            g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, oldInterpolation);
        }
    }

    private void paintCheckerboard(Graphics2D g, int width, int height) {
        int cell = 10;
        for (int y = 0; y < height; y += cell) {
            for (int x = 0; x < width; x += cell) {
                boolean light = ((x / cell) + (y / cell)) % 2 == 0;
                g.setColor(light ? CHECKER_LIGHT : CHECKER_DARK);
                g.fillRect(x, y, cell, cell);
            }
        }
    }

    private void paintOverlays(Graphics2D g, SkinLayout layout, RenderOptions options) {
        float lineWidth = 1f / Math.max(1, options.zoom());
        g.setStroke(new BasicStroke(lineWidth));
        Item hover = options.hover();
        if (hover != null && hover != options.selection()) {
            Rectangle bounds = Bounds.of(hover, index, images, options.variables());
            g.setColor(HOVER);
            g.drawRect(bounds.x, bounds.y, Math.max(1, bounds.width) - 1, Math.max(1, bounds.height) - 1);
        }
        Item selection = options.selection();
        if (selection == null) {
            return;
        }
        if (selection instanceof SliderItem slider) {
            paintSliderOverlay(g, slider);
        } else if (selection instanceof AnchorItem anchor) {
            paintAnchorOverlay(g, anchor);
        }
        Rectangle bounds = Bounds.of(selection, index, images, options.variables());
        g.setColor(SELECTION);
        g.drawRect(bounds.x, bounds.y, Math.max(1, bounds.width) - 1, Math.max(1, bounds.height) - 1);
    }

    private void paintSliderOverlay(Graphics2D g, SliderItem slider) {
        BezierPath path;
        try {
            path = SliderGeometry.sharedPath(slider.getPoints());
        } catch (IllegalArgumentException ex) {
            return;
        }
        int x = slider.getX();
        int y = slider.getY();
        g.setColor(SELECTION);
        java.util.List<Point2D.Float> samples = path.samplePath(10);
        for (int i = 0; i < samples.size() - 1; i++) {
            g.drawLine(x + (int) samples.get(i).x, y + (int) samples.get(i).y,
                    x + (int) samples.get(i + 1).x, y + (int) samples.get(i + 1).y);
        }
        for (int i = 0; i < path.controlCount(); i++) {
            int cx = x + path.controlX(i);
            int cy = y + path.controlY(i);
            g.setColor(Color.BLACK);
            g.fillOval(cx - 3, cy - 3, 7, 7);
            g.setColor(Color.YELLOW);
            g.fillOval(cx - 2, cy - 2, 5, 5);
        }
    }

    private void paintAnchorOverlay(Graphics2D g, AnchorItem anchor) {
        BezierPath path;
        try {
            path = SliderGeometry.sharedPath(anchor.getPoints());
        } catch (IllegalArgumentException ex) {
            return;
        }
        int x = anchor.getX();
        int y = anchor.getY();
        g.setColor(SELECTION);
        java.util.List<Point2D.Float> samples = path.samplePath(48);
        for (int i = 0; i < samples.size() - 1; i++) {
            g.drawLine(x + (int) samples.get(i).x, y + (int) samples.get(i).y,
                    x + (int) samples.get(i + 1).x, y + (int) samples.get(i + 1).y);
        }
        g.setColor(Color.YELLOW);
        for (int i = 0; i < path.controlCount(); i++) {
            g.fillRect(x + path.controlX(i) - 1, y + path.controlY(i) - 1, 3, 3);
        }
    }

    /**
     * Bounds of every item in draw order, for the geometry description.
     */
    public java.util.List<Item> flatten(SkinLayout layout) {
        java.util.List<Item> items = new java.util.ArrayList<>();
        for (Item item : layout.getItems()) {
            collect(item, items);
        }
        return items;
    }

    private void collect(Item item, java.util.List<Item> items) {
        items.add(item);
        for (Item child : item.children()) {
            collect(child, items);
        }
    }

    public static boolean isAnchored(AbstractItem item) {
        return item instanceof AnchorItem;
    }
}
