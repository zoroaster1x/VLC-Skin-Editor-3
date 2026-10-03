package dev.zoroaster1x.vlcskin.model.item;

import lombok.Getter;
import lombok.Setter;

import dev.zoroaster1x.vlcskin.model.ItemType;

/**
 * A track slider whose thumb follows a bezier path of control points.
 */
@Getter
@Setter
public final class SliderItem extends AbstractItem {

    public static final String DEFAULT_DOWN = "none";
    public static final String DEFAULT_OVER = "none";
    public static final String DEFAULT_POINTS = "(0,0)";
    public static final int DEFAULT_THICKNESS = 10;
    public static final String DEFAULT_VALUE = "none";
    public static final String DEFAULT_TOOLTIPTEXT = "";

    private String up;
    private String down = DEFAULT_DOWN;
    private String over = DEFAULT_OVER;
    private String points = DEFAULT_POINTS;
    private int thickness = DEFAULT_THICKNESS;
    private String value = DEFAULT_VALUE;
    private String tooltiptext = DEFAULT_TOOLTIPTEXT;
    private SliderBackground background;
    private boolean inPlaytree;

    @Override
    public String elementName() {
        return "Slider";
    }

    /**
     * The background is the one child a slider can have, shown in the items tree.
     */
    @Override
    public java.util.List<Item> children() {
        return background == null ? java.util.List.of() : java.util.List.of(background);
    }

    @Override
    public ItemType type() {
        return ItemType.SLIDER;
    }

    @Override
    public boolean usesResource(String resourceId) {
        return eq(up, resourceId) || eq(over, resourceId) || eq(down, resourceId)
                || (background != null && background.usesResource(resourceId));
    }
}
