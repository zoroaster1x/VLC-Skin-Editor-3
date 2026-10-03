package io.github.zoroaster1x.vlcskin.render;

import io.github.zoroaster1x.vlcskin.model.SkinIndex;
import io.github.zoroaster1x.vlcskin.model.SkinLayout;
import io.github.zoroaster1x.vlcskin.model.item.Item;
import java.awt.Rectangle;
import java.util.List;

/**
 * Finds the topmost item under a point, honouring draw order and nesting.
 */
public final class HitTester {

    private final SkinIndex index;
    private final ImageStore images;
    private final PreviewVariables variables;

    public HitTester(SkinIndex index, ImageStore images, PreviewVariables variables) {
        this.index = index;
        this.images = images;
        this.variables = variables;
    }

    public Item topmost(SkinLayout layout, int x, int y) {
        return topmost(layout.getItems(), x, y);
    }

    private Item topmost(List<Item> items, int x, int y) {
        for (int i = items.size() - 1; i >= 0; i--) {
            Item item = items.get(i);
            Item child = topmost(item.children(), x, y);
            if (child != null && Bounds.of(child, index, images, variables).contains(x, y)) {
                return child;
            }
            if (contains(item, x, y)) {
                return item;
            }
        }
        return null;
    }

    public boolean contains(Item item, int x, int y) {
        if (item == null) {
            return false;
        }
        Rectangle bounds = Bounds.of(item, index, images, variables);
        if (!bounds.contains(x, y)) {
            return false;
        }
        return !(item instanceof io.github.zoroaster1x.vlcskin.model.item.GroupItem);
    }

    /**
     * All items whose bounds contain the point, topmost first.
     */
    public List<Item> stack(SkinLayout layout, int x, int y) {
        java.util.ArrayList<Item> found = new java.util.ArrayList<>();
        collect(layout.getItems(), x, y, found);
        java.util.Collections.reverse(found);
        return found;
    }

    private void collect(List<Item> items, int x, int y, List<Item> found) {
        for (Item item : items) {
            if (contains(item, x, y)) {
                found.add(item);
            }
            collect(item.children(), x, y, found);
        }
    }
}
