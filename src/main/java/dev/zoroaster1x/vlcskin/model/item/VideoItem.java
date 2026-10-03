package dev.zoroaster1x.vlcskin.model.item;

import lombok.Getter;
import lombok.Setter;

import dev.zoroaster1x.vlcskin.model.ItemType;

/**
 * The video output rectangle.
 */
@Getter
@Setter
public final class VideoItem extends AbstractItem {

    private int width;
    private int height;
    private boolean autoresize = true;

    @Override
    public String elementName() {
        return "Video";
    }

    @Override
    public ItemType type() {
        return ItemType.VIDEO;
    }
}
