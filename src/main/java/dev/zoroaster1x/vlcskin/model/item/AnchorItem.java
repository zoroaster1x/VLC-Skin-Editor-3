package dev.zoroaster1x.vlcskin.model.item;

import lombok.Getter;
import lombok.Setter;

import dev.zoroaster1x.vlcskin.model.ItemType;

/**
 * A point of the window that sticks while the window is resized.
 */
@Getter
@Setter
public final class AnchorItem extends AbstractItem {

    public static final String DEFAULT_POINTS = "(0,0)";
    public static final int DEFAULT_RANGE = 10;

    private String points = DEFAULT_POINTS;
    private int priority;
    private int range = DEFAULT_RANGE;

    @Override
    public String elementName() {
        return "Anchor";
    }

    @Override
    public ItemType type() {
        return ItemType.ANCHOR;
    }
}
