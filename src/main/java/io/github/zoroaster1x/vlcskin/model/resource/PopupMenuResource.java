package io.github.zoroaster1x.vlcskin.model.resource;

import io.github.zoroaster1x.vlcskin.model.SkinNode;
import java.util.LinkedList;
import java.util.List;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;

/**
 * A popup menu definition with entries and separators.
 */
@Getter
@Setter
@EqualsAndHashCode(callSuper = false)
public final class PopupMenuResource extends AbstractResource {

    private final List<MenuEntry> entries = new LinkedList<>();

    @Override
    public String elementName() {
        return "PopupMenu";
    }

    @Override
    public String typeName() {
        return "Popup menu";
    }

    public PopupMenuResource copy() {
        PopupMenuResource copy = new PopupMenuResource();
        copy.setId(getId());
        for (MenuEntry entry : entries) {
            copy.entries.add(entry.copy());
        }
        foreignAttributes().forEach(copy::preserveAttribute);
        return copy;
    }

    /**
     * One line of a popup menu.
     */
    public sealed interface MenuEntry permits MenuItemEntry, MenuSeparatorEntry {

        MenuEntry copy();

        SkinNode node();
    }
}
