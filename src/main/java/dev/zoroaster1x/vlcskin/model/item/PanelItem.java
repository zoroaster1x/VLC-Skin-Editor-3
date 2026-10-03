package dev.zoroaster1x.vlcskin.model.item;

import lombok.Getter;
import lombok.Setter;

import dev.zoroaster1x.vlcskin.model.ItemType;
import java.util.LinkedList;
import java.util.List;

/**
 * A resizable container with anchoring semantics defined by its attributes.
 */
@Getter
@Setter
public final class PanelItem extends AbstractItem {

    private int width;
    private int height;
    private final List<Item> items = new LinkedList<>();

    @Override
    public String elementName() {
        return "Panel";
    }

    @Override
    public ItemType type() {
        return ItemType.PANEL;
    }

    @Override
    public List<Item> children() {
        return items;
    }

    @Override
    public boolean usesResource(String resourceId) {
        for (Item item : items) {
            if (item.usesResource(resourceId)) {
                return true;
            }
        }
        return false;
    }
}
