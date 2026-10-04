package dev.zoroaster1x.vlcskin.render.draw;

import dev.zoroaster1x.vlcskin.model.item.TextItem;
import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.Shape;

/**
 * Draws a text item with the global variables substituted.
 *
 * <p>Matches VLC's CtrlText static frame: a text shorter than the control
 * width is aligned inside it; a longer text is clipped to the width and the
 * alignment decides which part is visible (left shows the start, right the
 * end, center the middle).
 */
public final class TextDrawer implements Drawer<TextItem> {

    @Override
    public void draw(Graphics2D g, TextItem item, int offsetX, int offsetY, DrawContext context) {
        Font font = context.images().font(context.index(), item.getFont());
        g.setFont(font);
        g.setColor(Colors.parse(context.index(), item.getColor(), Color.BLACK));
        String text = context.options().variables().substitute(item.getText());
        FontMetrics metrics = g.getFontMetrics();
        int textWidth = metrics.stringWidth(text);
        int left = offsetX + item.getX();
        int top = offsetY + item.getY();
        int x = left;
        int y = top + metrics.getAscent();
        int width = item.getWidth();
        if (width > 0 && textWidth < width) {
            switch (item.getAlignment()) {
                case "right" -> x += width - textWidth;
                case "center" -> x += (width - textWidth) / 2;
                default -> {
                }
            }
        } else if (width > 0) {
            Shape oldClip = g.getClip();
            switch (item.getAlignment()) {
                case "right" -> x -= textWidth - width;
                case "center" -> x -= (textWidth - width) / 2;
                default -> {
                }
            }
            g.clipRect(left, top, width, metrics.getHeight());
            g.drawString(text, x, y);
            g.setClip(oldClip);
            return;
        }
        g.drawString(text, x, y);
    }
}
