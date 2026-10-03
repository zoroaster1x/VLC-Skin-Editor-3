package io.github.zoroaster1x.vlcskin.render.draw;

import io.github.zoroaster1x.vlcskin.model.item.AnchorItem;
import io.github.zoroaster1x.vlcskin.render.BezierPath;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;

/**
 * Draws the anchor path and its control points while the anchor is selected.
 */
public final class AnchorDrawer implements Drawer<AnchorItem> {

    public static final Color GUIDE = new Color(0xE0, 0x30, 0x30);
    public static final Color KNOB = new Color(0xF5, 0xC2, 0x11);

    @Override
    public void draw(Graphics2D g, AnchorItem item, int offsetX, int offsetY, DrawContext context) {
        if (context.options().selection() != item && !context.options().anchorHelpers()) {
            return;
        }
        BezierPath path;
        try {
            path = io.github.zoroaster1x.vlcskin.render.SliderGeometry.sharedPath(item.getPoints());
        } catch (IllegalArgumentException ex) {
            return;
        }
        int x = offsetX + item.getX();
        int y = offsetY + item.getY();
        g.setColor(GUIDE);
        g.setStroke(new BasicStroke(1f));
        java.util.List<java.awt.geom.Point2D.Float> samples = path.samplePath(48);
        for (int i = 0; i < samples.size() - 1; i++) {
            g.drawLine(x + (int) samples.get(i).x, y + (int) samples.get(i).y,
                    x + (int) samples.get(i + 1).x, y + (int) samples.get(i + 1).y);
        }
        g.setColor(KNOB);
        for (int i = 0; i < path.controlCount(); i++) {
            g.fillRect(x + path.controlX(i) - 2, y + path.controlY(i) - 2, 5, 5);
        }
    }
}
