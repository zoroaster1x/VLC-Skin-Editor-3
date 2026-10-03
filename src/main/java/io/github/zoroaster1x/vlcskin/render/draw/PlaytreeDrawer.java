package io.github.zoroaster1x.vlcskin.render.draw;

import io.github.zoroaster1x.vlcskin.model.item.PlaytreeItem;
import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;

/**
 * Draws the sample rows the editor always showed: one folder row (unless the
 * playlist is flat), a normal item, a playing item and a selected item, plus
 * whatever background the skin configures.
 */
public final class PlaytreeDrawer implements Drawer<PlaytreeItem> {

    @Override
    public void draw(Graphics2D g, PlaytreeItem item, int offsetX, int offsetY, DrawContext context) {
        if (item.getWidth() <= 0 || item.getHeight() <= 0) {
            return;
        }
        int x = offsetX + item.getX();
        int y = offsetY + item.getY();
        int width = item.getWidth();
        int height = item.getHeight();
        Font font = context.images().font(context.index(), item.getFont());
        g.setFont(font);
        FontMetrics metrics = g.getFontMetrics();
        int lineHeight = metrics.getHeight();

        drawBackground(g, item, x, y, width, height, lineHeight, context);
        BufferedImage closed = context.images().image(context.index(), item.getClosedimage(), context.options().frameTick());
        BufferedImage open = context.images().image(context.index(), item.getOpenimage(), context.options().frameTick());
        BufferedImage itemImage = context.images().image(context.index(), item.getItemimage(), context.options().frameTick());

        int cursor = y;
        int rowHeight = lineHeight;
        if (!item.isFlat() && closed != null) {
            rowHeight = Math.max(lineHeight, closed.getHeight());
        }
        if (!item.isFlat() && open != null) {
            rowHeight = Math.max(rowHeight, open.getHeight());
        }
        if (itemImage != null) {
            rowHeight = Math.max(rowHeight, itemImage.getHeight());
        }

        if (!item.isFlat() && closed != null && cursor + rowHeight <= y + height) {
            g.drawImage(closed, x, cursor, null);
            g.setColor(TextDrawer.parseColor(item.getFgcolor(), Color.BLACK));
            g.drawString("Closed folder", x + closed.getWidth() + 2,
                    cursor + rowHeight - (rowHeight - metrics.getAscent()) / 2);
            cursor += rowHeight;
        }
        if (!item.isFlat() && open != null && cursor + rowHeight <= y + height) {
            g.drawImage(open, x, cursor, null);
            g.setColor(TextDrawer.parseColor(item.getFgcolor(), Color.BLACK));
            g.drawString("Open folder", x + open.getWidth() + 2,
                    cursor + rowHeight - (rowHeight - metrics.getAscent()) / 2);
            cursor += rowHeight;
        }
        if (cursor + rowHeight <= y + height) {
            drawItemRow(g, item, itemImage, x, cursor, width, rowHeight, metrics, "Normal item",
                    TextDrawer.parseColor(item.getFgcolor(), Color.BLACK), context);
            cursor += rowHeight;
        }
        if (cursor + rowHeight <= y + height) {
            drawItemRow(g, item, itemImage, x, cursor, width, rowHeight, metrics, "Playing item",
                    TextDrawer.parseColor(item.getPlaycolor(), Color.RED), context);
            cursor += rowHeight;
        }
        if (cursor + rowHeight <= y + height) {
            g.setColor(TextDrawer.parseColor(item.getSelcolor(), Color.BLUE));
            g.fillRect(x, cursor, width, rowHeight);
            drawItemRow(g, item, itemImage, x, cursor, width, rowHeight, metrics, "Selected item",
                    TextDrawer.parseColor(item.getFgcolor(), Color.BLACK), context);
        }
        if (item.getSlider() != null) {
            context.options();
            new SliderDrawer().draw(g, item.getSlider(), x, y, context);
        }
    }

    private void drawItemRow(Graphics2D g, PlaytreeItem item, BufferedImage image, int x, int y, int width,
                             int rowHeight, FontMetrics metrics, String label, Color color, DrawContext context) {
        g.setColor(color);
        int textX = x;
        if (image != null) {
            g.drawImage(image, x, y, null);
            textX = x + image.getWidth() + 4;
        }
        g.drawString(label, textX, y + rowHeight - (rowHeight - metrics.getAscent()) / 2);
    }

    private void drawBackground(Graphics2D g, PlaytreeItem item, int x, int y, int width, int height,
                                int lineHeight, DrawContext context) {
        BufferedImage background = "none".equals(item.getBgimage())
                ? null : context.images().image(context.index(), item.getBgimage());
        if (background != null) {
            int imageWidth = Math.min(width, background.getWidth());
            int imageHeight = Math.min(height, background.getHeight());
            for (int ty = 0; ty < height; ty += Math.max(1, imageHeight)) {
                for (int tx = 0; tx < width; tx += Math.max(1, imageWidth)) {
                    g.drawImage(background, x + tx, y + ty, null);
                }
            }
            return;
        }
        g.setColor(TextDrawer.parseColor(item.getBgcolor1(), Color.WHITE));
        g.fillRect(x, y, width, height);
        g.setColor(TextDrawer.parseColor(item.getBgcolor2(), Color.WHITE));
        for (int stripe = lineHeight; stripe < height; stripe += lineHeight * 2) {
            g.fillRect(x, y + stripe, width, lineHeight);
        }
    }
}
