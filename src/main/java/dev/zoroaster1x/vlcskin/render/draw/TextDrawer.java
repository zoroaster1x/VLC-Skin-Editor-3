package dev.zoroaster1x.vlcskin.render.draw;

import dev.zoroaster1x.vlcskin.model.item.TextItem;
import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics2D;

/**
 * Draws a text item with the global variables substituted.
 */
public final class TextDrawer implements Drawer<TextItem> {

    @Override
    public void draw(Graphics2D g, TextItem item, int offsetX, int offsetY, DrawContext context) {
        Font font = context.images().font(context.index(), item.getFont());
        g.setFont(font);
        g.setColor(parseColor(item.getColor(), Color.BLACK));
        String text = context.options().variables().substitute(item.getText());
        FontMetrics metrics = g.getFontMetrics();
        int x = offsetX + item.getX();
        int y = offsetY + item.getY() + metrics.getAscent();
        if (item.getWidth() > 0) {
            int textWidth = metrics.stringWidth(text);
            switch (item.getAlignment()) {
                case "right" -> x += item.getWidth() - textWidth;
                case "center" -> x += (item.getWidth() - textWidth) / 2;
                default -> {
                }
            }
        }
        g.drawString(text, x, y);
    }

    static Color parseColor(String value, Color fallback) {
        if (value == null) {
            return fallback;
        }
        try {
            return Color.decode(value.startsWith("#") ? value : "#" + value);
        } catch (NumberFormatException ex) {
            return fallback;
        }
    }
}
