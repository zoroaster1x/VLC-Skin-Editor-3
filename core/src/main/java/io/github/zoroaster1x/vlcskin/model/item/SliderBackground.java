package io.github.zoroaster1x.vlcskin.model.item;

import lombok.Getter;
import lombok.Setter;

import io.github.zoroaster1x.vlcskin.model.ItemType;

/**
 * The background of a slider: one bitmap split into a grid of frames, one frame
 * per fill level. Only the id and image are used by VLC for identity.
 */
@Getter
@Setter
public final class SliderBackground extends AbstractItem {

    private String image;
    private int nbhoriz = 1;
    private int nbvert = 1;
    private int padhoriz;
    private int padvert;

    @Override
    public String elementName() {
        return "SliderBackground";
    }

    @Override
    public ItemType type() {
        return ItemType.SLIDER_BACKGROUND;
    }

    @Override
    public boolean usesResource(String resourceId) {
        return eq(image, resourceId);
    }
}
