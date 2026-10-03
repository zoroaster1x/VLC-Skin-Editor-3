package dev.zoroaster1x.vlcskin.model.item;

import lombok.Getter;
import lombok.Setter;

import dev.zoroaster1x.vlcskin.model.ItemType;

/**
 * A clickable image.
 */
@Getter
@Setter
public final class ImageItem extends AbstractItem {

    public static final String DEFAULT_RESIZE = "mosaic";
    public static final String DEFAULT_ACTION = "none";

    private String image;
    private String action = DEFAULT_ACTION;
    private String action2 = DEFAULT_ACTION;
    private String resize = DEFAULT_RESIZE;
    private boolean art;

    @Override
    public String elementName() {
        return "Image";
    }

    @Override
    public ItemType type() {
        return ItemType.IMAGE;
    }

    @Override
    public boolean usesResource(String resourceId) {
        return eq(image, resourceId);
    }
}
