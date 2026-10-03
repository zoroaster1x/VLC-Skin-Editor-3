package io.github.zoroaster1x.vlcskin.model.resource;

import io.github.zoroaster1x.vlcskin.model.SkinNode;

/**
 * A Separator line between popup menu items.
 */
public final class MenuSeparatorEntry extends SkinNode implements PopupMenuResource.MenuEntry {

    @Override
    public PopupMenuResource.MenuEntry copy() {
        MenuSeparatorEntry copy = new MenuSeparatorEntry();
        foreignAttributes().forEach(copy::preserveAttribute);
        return copy;
    }

    @Override
    public SkinNode node() {
        return this;
    }
}
