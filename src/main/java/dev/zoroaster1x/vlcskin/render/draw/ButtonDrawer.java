package dev.zoroaster1x.vlcskin.render.draw;

import dev.zoroaster1x.vlcskin.model.item.ButtonItem;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;

/**
 * Draws a button in its up, over or down state.
 */
public final class ButtonDrawer implements Drawer<ButtonItem> {

    @Override
    public void draw(Graphics2D g, ButtonItem item, int offsetX, int offsetY, DrawContext context) {
        boolean hovered = context.options().hover() == item;
        boolean pressed = context.options().pressed() == item;
        String sprite;
        if (!hovered || (!pressed && "none".equals(item.getOver())) || (pressed && "none".equals(item.getDown()))) {
            sprite = item.getUp();
        } else if (!pressed) {
            sprite = item.getOver();
        } else {
            sprite = item.getDown();
        }
        BufferedImage image = context.images().image(context.index(), sprite, context.options().frameTick());
        if (image == null) {
            return;
        }
        g.drawImage(image, offsetX + item.getX(), offsetY + item.getY(), null);
    }
}
