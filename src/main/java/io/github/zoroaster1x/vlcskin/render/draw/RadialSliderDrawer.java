package io.github.zoroaster1x.vlcskin.render.draw;

import io.github.zoroaster1x.vlcskin.model.item.RadialSliderItem;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;

/**
 * Draws the frame of a radial slider sequence for the current value.
 */
public final class RadialSliderDrawer implements Drawer<RadialSliderItem> {

    @Override
    public void draw(Graphics2D g, RadialSliderItem item, int offsetX, int offsetY, DrawContext context) {
        BufferedImage sequence = context.images().image(context.index(), item.getSequence());
        if (sequence == null) {
            return;
        }
        int frames = Math.max(1, item.getNbimages());
        int frameHeight = Math.max(1, sequence.getHeight() / frames);
        int index = Math.min(frames - 1, Math.max(0, (int) Math.floor(context.options().variables().sliderValue()
                * frames)));
        g.drawImage(sequence, offsetX + item.getX(), offsetY + item.getY(),
                offsetX + item.getX() + sequence.getWidth(), offsetY + item.getY() + frameHeight,
                0, index * frameHeight, sequence.getWidth(), index * frameHeight + frameHeight, null);
    }
}
