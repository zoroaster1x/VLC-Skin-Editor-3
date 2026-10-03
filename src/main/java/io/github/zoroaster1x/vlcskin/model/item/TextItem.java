package io.github.zoroaster1x.vlcskin.model.item;

import lombok.Getter;
import lombok.Setter;

import io.github.zoroaster1x.vlcskin.model.ItemType;

/**
 * A text line drawn in a font resource.
 */
@Getter
@Setter
public final class TextItem extends AbstractItem {

    public static final String DEFAULT_COLOR = "#000000";
    public static final String DEFAULT_ALIGNMENT = "left";
    public static final String DEFAULT_SCROLLING = "auto";

    private String text = "";
    private String font = "defaultfont";
    private String color = DEFAULT_COLOR;
    private int width;
    private String alignment = DEFAULT_ALIGNMENT;
    private String scrolling = DEFAULT_SCROLLING;

    @Override
    public String elementName() {
        return "Text";
    }

    @Override
    public ItemType type() {
        return ItemType.TEXT;
    }

    @Override
    public boolean usesResource(String resourceId) {
        return eq(font, resourceId);
    }
}
