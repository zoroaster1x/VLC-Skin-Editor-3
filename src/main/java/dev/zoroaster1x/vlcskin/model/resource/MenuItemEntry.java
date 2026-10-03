package dev.zoroaster1x.vlcskin.model.resource;

import dev.zoroaster1x.vlcskin.model.SkinNode;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;

/**
 * A clickable entry of a popup menu.
 */
@Getter
@Setter
@EqualsAndHashCode(callSuper = false)
public final class MenuItemEntry extends SkinNode implements PopupMenuResource.MenuEntry {

    public static final String DEFAULT_ACTION = "none";

    private String label = "";
    private String action = DEFAULT_ACTION;

    @Override
    public PopupMenuResource.MenuEntry copy() {
        MenuItemEntry copy = new MenuItemEntry();
        copy.label = label;
        copy.action = action;
        foreignAttributes().forEach(copy::preserveAttribute);
        return copy;
    }

    @Override
    public SkinNode node() {
        return this;
    }
}
