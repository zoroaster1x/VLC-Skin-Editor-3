package io.github.zoroaster1x.vlcskin.model.item;

import io.github.zoroaster1x.vlcskin.model.ItemType;
import java.util.List;
import java.util.Map;

/**
 * One element inside a layout.
 */
public sealed interface Item permits AbstractItem {

    String elementName();

    ItemType type();

    String getId();

    void setId(String id);

    int getX();

    void setX(int x);

    int getY();

    void setY(int y);

    /**
     * Attributes this editor does not know, preserved for the next save.
     */
    Map<String, String> foreignAttributes();

    /**
     * Unknown child elements kept as raw XML.
     */
    List<String> unknownChildren();

    /**
     * Direct children, empty for leaf items.
     */
    default List<Item> children() {
        return List.of();
    }

    default boolean isContainer() {
        return !children().isEmpty();
    }

    boolean usesResource(String resourceId);
}
