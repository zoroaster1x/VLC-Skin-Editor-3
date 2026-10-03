package io.github.zoroaster1x.vlcskin.render.draw;

import io.github.zoroaster1x.vlcskin.model.item.AbstractItem;
import java.awt.Graphics2D;

/**
 * Draws one kind of layout element.
 */
public interface Drawer<T extends AbstractItem> {

    void draw(Graphics2D g, T item, int offsetX, int offsetY, DrawContext context);
}
