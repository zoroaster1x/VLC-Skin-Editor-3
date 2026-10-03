package dev.zoroaster1x.vlcskin.model.item;

import lombok.Getter;
import lombok.Setter;

import dev.zoroaster1x.vlcskin.model.ItemType;

/**
 * A knob that rotates through a sequence of images between two angles.
 */
@Getter
@Setter
public final class RadialSliderItem extends AbstractItem {

    public static final String DEFAULT_VALUE = "none";
    public static final String DEFAULT_TOOLTIPTEXT = "";
    public static final int DEFAULT_MAX_ANGLE = 360;

    private String sequence;
    private int nbimages;
    private int minangle;
    private int maxangle = DEFAULT_MAX_ANGLE;
    private String value = DEFAULT_VALUE;
    private String tooltiptext = DEFAULT_TOOLTIPTEXT;

    @Override
    public String elementName() {
        return "RadialSlider";
    }

    @Override
    public ItemType type() {
        return ItemType.RADIAL_SLIDER;
    }

    @Override
    public boolean usesResource(String resourceId) {
        return eq(sequence, resourceId);
    }
}
