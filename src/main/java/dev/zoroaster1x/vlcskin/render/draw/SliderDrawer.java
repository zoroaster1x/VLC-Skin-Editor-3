package dev.zoroaster1x.vlcskin.render.draw;

import dev.zoroaster1x.vlcskin.model.item.SliderBackground;
import dev.zoroaster1x.vlcskin.model.item.SliderItem;
import dev.zoroaster1x.vlcskin.render.SliderGeometry;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.image.BufferedImage;

/**
 * Draws a slider: background frame first, then the thumb on the bezier path.
 */
public final class SliderDrawer implements Drawer<SliderItem> {

    @Override
    public void draw(Graphics2D g, SliderItem item, int offsetX, int offsetY, DrawContext context) {
        draw(g, item, offsetX, offsetY, context, context.options().variables().sliderValue());
    }

    /**
     * Draws the slider at an explicit value. The nested playlist slider always
     * follows the playlist scroll position, which VLC initialises to 1.0.
     */
    public void draw(Graphics2D g, SliderItem item, int offsetX, int offsetY,
                     DrawContext context, float value) {
        if (item.getBackground() != null) {
            drawBackground(g, item.getBackground(), offsetX + item.getX(), offsetY + item.getY(), context);
        }
        BufferedImage thumb = context.images().image(context.index(), item.getUp(), context.options().frameTick());
        if (thumb == null) {
            return;
        }
        java.awt.geom.Point2D.Float position = SliderGeometry.thumbPosition(item, value);
        // VLC: x = pos.left + xPos - imgWidth / 2, integer division on both.
        int x = offsetX + item.getX() + (int) position.getX() - thumb.getWidth() / 2;
        int y = offsetY + item.getY() + (int) position.getY() - thumb.getHeight() / 2;
        g.drawImage(thumb, x, y, null);
    }

    /**
     * Draws one frame of a slider background grid.
     */
    public void drawBackground(Graphics2D g, SliderBackground item, int offsetX, int offsetY, DrawContext context) {
        BufferedImage whole = SliderGeometry.backgroundImage(context.index(), context.images(), item);
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
