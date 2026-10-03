package dev.zoroaster1x.vlcskin.model;

import java.util.LinkedList;
import java.util.List;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;

/**
 * A top level window of a theme.
 */
@Getter
@Setter
@EqualsAndHashCode(callSuper = false)
public final class SkinWindow extends SkinNode {

    public static final String DEFAULT_ID = "none";
    public static final String DEFAULT_VISIBLE = "true";

    private String id = DEFAULT_ID;
    private String visible = DEFAULT_VISIBLE;
    private int x;
    private int y;
    private boolean dragdrop = true;
    private boolean playondrop = true;
    private final List<SkinLayout> layouts = new LinkedList<>();
}
