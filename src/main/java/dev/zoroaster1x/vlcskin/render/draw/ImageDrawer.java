package dev.zoroaster1x.vlcskin.render.draw;

import dev.zoroaster1x.vlcskin.model.item.ImageItem;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;

/**
 * Draws an image at its intrinsic size.
 */
public final class ImageDrawer implements Drawer<ImageItem> {

    @Override
    public void draw(Graphics2D g, ImageItem item, int offsetX, int offsetY, DrawContext context) {
        BufferedImage image = context.images().image(context.index(), item.getImage(), context.options().frameTick());
        if (image == null) {
            return;
        }
        g.drawImage(image, offsetX + item.getX(), offsetY + item.getY(), null);
    }
}
