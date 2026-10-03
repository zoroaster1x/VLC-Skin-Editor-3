package dev.zoroaster1x.vlcskin.model;

import dev.zoroaster1x.vlcskin.model.item.Item;
import java.util.LinkedList;
import java.util.List;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;

/**
 * One resizable arrangement of items inside a window.
 */
@Getter
@Setter
@EqualsAndHashCode(callSuper = false)
public final class SkinLayout extends SkinNode {

    public static final String DEFAULT_ID = "none";
    public static final int DEFAULT_MIN = -1;
    public static final int DEFAULT_MAX = -1;

    private String id = DEFAULT_ID;
    private int width;
    private int height;
    private int minwidth = DEFAULT_MIN;
    private int maxwidth = DEFAULT_MAX;
    private int minheight = DEFAULT_MIN;
    private int maxheight = DEFAULT_MAX;
    private final List<Item> items = new LinkedList<>();
}
