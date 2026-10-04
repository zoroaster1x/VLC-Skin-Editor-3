package dev.zoroaster1x.vlcskin.model.item;

import dev.zoroaster1x.vlcskin.model.SkinNode;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;

/**
 * Shared attributes of every layout element.
 */
@Getter
@Setter
@EqualsAndHashCode(callSuper = false)
public abstract sealed class AbstractItem extends SkinNode implements Item
        permits AnchorItem, ButtonItem, CheckboxItem, GroupItem, ImageItem, PanelItem, PlaytreeItem,
                RadialSliderItem, SliderItem, SliderBackground, TextItem, VideoItem {

    public static final String DEFAULT_ID = "Unnamed";
    public static final String DEFAULT_VISIBLE = "true";
    public static final String DEFAULT_LEFTTOP = "lefttop";
    public static final String DEFAULT_RIGHTBOTTOM = "lefttop";
    public static final String DEFAULT_HELP = "";

    private String id = DEFAULT_ID;
    private String visible = DEFAULT_VISIBLE;
    private int x;
    private int y;
    private String lefttop = DEFAULT_LEFTTOP;
    private String rightbottom = DEFAULT_RIGHTBOTTOM;
    private boolean xkeepratio;
    private boolean ykeepratio;
    private String help = DEFAULT_HELP;

    @Override
    public boolean usesResource(String resourceId) {
        return false;
    }

    /**
     * True when an attribute names the resource. VLC accepts "id1;id2" fallback
     * lists, so a resource is in use when any segment names it.
     */
    protected static boolean eq(String value, String other) {
        if (value == null) {
            return other == null;
        }
        if (value.equals(other)) {
            return true;
        }
        if (other == null || value.indexOf(';') < 0) {
            return false;
        }
        for (String part : value.split(";")) {
            if (part.strip().equals(other)) {
                return true;
            }
        }
        return false;
    }
}
