package io.github.zoroaster1x.vlcskin.render.draw;

import io.github.zoroaster1x.vlcskin.model.item.SliderBackground;
import io.github.zoroaster1x.vlcskin.model.item.SliderItem;
import io.github.zoroaster1x.vlcskin.render.SliderGeometry;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.image.BufferedImage;

/**
 * Draws a slider: background frame first, then the thumb on the bezier path.
 */
public final class SliderDrawer implements Drawer<SliderItem> {

    @Override
    public void draw(Graphics2D g, SliderItem item, int offsetX, int offsetY, DrawContext context) {
        if (item.getBackground() != null) {
            drawBackground(g, item.getBackground(), offsetX + item.getX(), offsetY + item.getY(), context);
        }
        BufferedImage thumb = context.images().image(context.index(), item.getUp(), context.options().frameTick());
        if (thumb == null) {
            return;
        }
        java.awt.geom.Point2D.Float position = SliderGeometry.thumbPosition(item, context.options().variables());
        int x = (int) Math.round(offsetX + item.getX() + position.getX() - thumb.getWidth() / 2.0);
        int y = (int) Math.round(offsetY + item.getY() + position.getY() - thumb.getHeight() / 2.0);
        g.drawImage(thumb, x, y, null);
    }

    /**
     * Draws one frame of a slider background grid.
     */
    public void drawBackground(Graphics2D g, SliderBackground item, int offsetX, int offsetY, DrawContext context) {
        io.github.zoroaster1x.vlcskin.model.SkinIndex.ImageRef ref =
                context.index().findImage(item.getImage());
        if (ref == null) {
            return;
        }
        BufferedImage whole = context.images().wholeImage(context.index(), ref.bitmap());
        Rectangle frame = SliderGeometry.backgroundFrame(item, context.options().variables().sliderValue(),
                context.index(), context.images());
        if (whole == null || frame == null) {
            return;
        }
        g.drawImage(whole, offsetX + item.getX(), offsetY + item.getY(),
                offsetX + item.getX() + frame.width, offsetY + item.getY() + frame.height,
                frame.x, frame.y, frame.x + frame.width, frame.y + frame.height, null);
    }
}
