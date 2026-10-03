package io.github.zoroaster1x.vlcskin.model.item;

import lombok.Getter;
import lombok.Setter;

import io.github.zoroaster1x.vlcskin.model.ItemType;

/**
 * A two or three state image button.
 */
@Getter
@Setter
public final class ButtonItem extends AbstractItem {

    public static final String DEFAULT_DOWN = "none";
    public static final String DEFAULT_OVER = "none";
    public static final String DEFAULT_ACTION = "none";
    public static final String DEFAULT_TOOLTIPTEXT = "";

    private String up;
    private String down = DEFAULT_DOWN;
    private String over = DEFAULT_OVER;
    private String action = DEFAULT_ACTION;
    private String tooltiptext = DEFAULT_TOOLTIPTEXT;

    @Override
    public String elementName() {
        return "Button";
    }

    @Override
    public ItemType type() {
        return ItemType.BUTTON;
    }

    @Override
    public boolean usesResource(String resourceId) {
        return eq(up, resourceId) || eq(over, resourceId) || eq(down, resourceId);
    }
}
