package dev.zoroaster1x.vlcskin.model.item;

import lombok.Getter;
import lombok.Setter;

import dev.zoroaster1x.vlcskin.model.ItemType;
import java.util.LinkedList;
import java.util.List;

/**
 * An invisible group that moves its children with it.
 */
@Getter
@Setter
public final class GroupItem extends AbstractItem {

    private final List<Item> items = new LinkedList<>();

    @Override
    public String elementName() {
        return "Group";
    }

    @Override
    public ItemType type() {
        return ItemType.GROUP;
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
