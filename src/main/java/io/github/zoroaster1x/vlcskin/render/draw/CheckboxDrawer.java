package io.github.zoroaster1x.vlcskin.render.draw;

import io.github.zoroaster1x.vlcskin.model.item.CheckboxItem;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;

/**
 * Draws a checkbox in the sprite triple its state expression selects.
 */
public final class CheckboxDrawer implements Drawer<CheckboxItem> {

    @Override
    public void draw(Graphics2D g, CheckboxItem item, int offsetX, int offsetY, DrawContext context) {
        boolean state = context.options().variables().evaluate(item.getState());
        String up = state ? item.getUp2() : item.getUp1();
        String over = state ? item.getOver2() : item.getOver1();
        String down = state ? item.getDown2() : item.getDown1();
        boolean hovered = context.options().hover() == item;
        boolean pressed = context.options().pressed() == item;
        String sprite;
        if (!hovered || (!pressed && "none".equals(over)) || (pressed && "none".equals(down))) {
            sprite = up;
        } else if (!pressed) {
            sprite = over;
        } else {
            sprite = down;
        }
        BufferedImage image = context.images().image(context.index(), sprite, context.options().frameTick());
        if (image == null) {
            return;
        }
        g.drawImage(image, offsetX + item.getX(), offsetY + item.getY(), null);
    }
}
