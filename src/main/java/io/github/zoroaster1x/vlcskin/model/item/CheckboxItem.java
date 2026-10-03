package io.github.zoroaster1x.vlcskin.model.item;

import lombok.Getter;
import lombok.Setter;

import io.github.zoroaster1x.vlcskin.model.ItemType;

/**
 * A two state checkbox with its own sprite triple per state.
 */
@Getter
@Setter
public final class CheckboxItem extends AbstractItem {

    public static final String DEFAULT_STATE = "false";
    public static final String DEFAULT_DOWN = "none";
    public static final String DEFAULT_OVER = "none";
    public static final String DEFAULT_ACTION = "none";
    public static final String DEFAULT_TOOLTIPTEXT = "";

    private String state = DEFAULT_STATE;
    private String up1;
    private String down1 = DEFAULT_DOWN;
    private String over1 = DEFAULT_OVER;
    private String action1 = DEFAULT_ACTION;
    private String tooltiptext1 = DEFAULT_TOOLTIPTEXT;
    private String up2;
    private String down2 = DEFAULT_DOWN;
    private String over2 = DEFAULT_OVER;
    private String action2 = DEFAULT_ACTION;
    private String tooltiptext2 = DEFAULT_TOOLTIPTEXT;

    @Override
    public String elementName() {
        return "Checkbox";
    }

    @Override
    public ItemType type() {
        return ItemType.CHECKBOX;
    }

    @Override
    public boolean usesResource(String resourceId) {
        return eq(up1, resourceId) || eq(down1, resourceId) || eq(over1, resourceId)
                || eq(up2, resourceId) || eq(down2, resourceId) || eq(over2, resourceId);
    }
}
