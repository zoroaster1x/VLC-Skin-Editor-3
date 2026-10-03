package dev.zoroaster1x.vlcskin.render.draw;

import dev.zoroaster1x.vlcskin.model.item.AbstractItem;
import dev.zoroaster1x.vlcskin.model.item.GroupItem;
import dev.zoroaster1x.vlcskin.model.item.PanelItem;
import java.awt.Graphics2D;

/**
 * Draws groups and panels: children move with the container origin.
 */
public final class ContainerDrawer {

    public void draw(Graphics2D g, GroupItem group, int offsetX, int offsetY, DrawContext context) {
        paintChildren(g, group, offsetX + group.getX(), offsetY + group.getY(), context);
    }

    public void draw(Graphics2D g, PanelItem panel, int offsetX, int offsetY, DrawContext context) {
        paintChildren(g, panel, offsetX + panel.getX(), offsetY + panel.getY(), context);
    }

    private void paintChildren(Graphics2D g, AbstractItem container, int x, int y, DrawContext context) {
        for (dev.zoroaster1x.vlcskin.model.item.Item child : container.children()) {
            ItemPainters.draw(g, child, x, y, context);
        }
    }
}
