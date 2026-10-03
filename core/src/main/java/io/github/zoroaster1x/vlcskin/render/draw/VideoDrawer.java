package io.github.zoroaster1x.vlcskin.render.draw;

import io.github.zoroaster1x.vlcskin.model.item.VideoItem;
import java.awt.Color;
import java.awt.Graphics2D;

/**
 * Draws the video output rectangle as VLC leaves it without a video.
 */
public final class VideoDrawer implements Drawer<VideoItem> {

    @Override
    public void draw(Graphics2D g, VideoItem item, int offsetX, int offsetY, DrawContext context) {
        if (item.getWidth() <= 0 || item.getHeight() <= 0) {
            return;
        }
        g.setColor(Color.BLACK);
        g.fillRect(offsetX + item.getX(), offsetY + item.getY(), item.getWidth(), item.getHeight());
    }
}
