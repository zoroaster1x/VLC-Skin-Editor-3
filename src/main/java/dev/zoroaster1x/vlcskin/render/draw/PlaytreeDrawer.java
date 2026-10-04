package dev.zoroaster1x.vlcskin.render.draw;

import dev.zoroaster1x.vlcskin.model.item.PlaytreeItem;
import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;

/**
 * Draws the playlist control the way VLC shows it with no media loaded: the
 * two tree nodes "Playlist" and "Media Library", plus whatever background and
 * slider the skin configures.
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
        BufferedImage itemImage = context.images().image(context.index(), item.getItemimage(), context.options().frameTick());

        // VLC with no media shows its two playlist tree nodes, "Playlist" and
        // "Media Library"; the editor previews exactly that instead of sample
        // rows, so a VLC screenshot comparison is meaningful.
        java.util.List<String> rows = java.util.List.of(
                dev.zoroaster1x.vlcskin.app.i18n.Messages.get("APP_PLAYLIST_ROOT", "Playlist"),
                dev.zoroaster1x.vlcskin.app.i18n.Messages.get("APP_PLAYLIST_LIBRARY", "Media Library"));
        // Row placement follows CtrlTree::makeImage: itemHeight is the font
        // size grown by the icons, the text image is font metrics tall, and
        // each row advances by the text height after a possible top clip.
        int itemImageWidth = 5;
        if (itemImage != null) {
            itemImageWidth = Math.max(itemImageWidth, itemImage.getWidth());
        }
        int textHeight = metrics.getHeight();
        int itemHeight = Math.max(font.getSize(), textHeight);
        int rowY = 0;
        int textX = x + itemImageWidth;
        for (String label : rows) {
            rowY += itemHeight - textHeight;
            int sourceY = 0;
            if (rowY < 0) {
                sourceY = -rowY;
                rowY = 0;
            }
            if (y + rowY < y + height) {
                if (itemImage != null) {
                    int iconY = rowY + (itemHeight - itemImage.getHeight() + 1) / 2;
                    g.drawImage(itemImage, x, y + iconY, null);
                }
                g.setColor(Colors.parse(context.index(), item.getFgcolor(), Color.BLACK));
                g.drawString(label, textX, y + rowY + metrics.getAscent());
            }
            rowY += textHeight - sourceY;
        }
        if (item.getSlider() != null) {
            // The nested slider's x and y are layout absolute (VLC adds it to
            // the layout as its own control), so pass the parent offset only.
            // Its value is the playlist scroll position, which VLC starts at
            // 1.0 for a tree with no content (var_tree.cpp:38-77).
            new SliderDrawer().draw(g, item.getSlider(), offsetX, offsetY, context, 1f);
        }
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
        g.setColor(Colors.parse(context.index(), item.getBgcolor1(), Color.WHITE));
        g.fillRect(x, y, width, height);
        g.setColor(Colors.parse(context.index(), item.getBgcolor2(), Color.WHITE));
        for (int stripe = lineHeight; stripe < height; stripe += lineHeight * 2) {
            g.fillRect(x, y + stripe, width, lineHeight);
        }
    }
}
