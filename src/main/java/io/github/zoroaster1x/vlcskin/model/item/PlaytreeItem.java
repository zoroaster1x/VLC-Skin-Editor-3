package io.github.zoroaster1x.vlcskin.model.item;

import lombok.Getter;
import lombok.Setter;

import io.github.zoroaster1x.vlcskin.model.ItemType;
import java.util.List;

/**
 * The playlist control. The format has two spellings of the same control:
 * {@code Playtree} with folder icons, and the flat {@code Playlist} which only
 * keeps the plain variant. One class models both; {@link #playlistSyntax}
 * remembers which element name to write back.
 */
@Getter
@Setter
public final class PlaytreeItem extends AbstractItem {

    public static final String DEFAULT_FONT = "defaultfont";
    public static final String DEFAULT_IMAGE = "none";
    public static final String DEFAULT_FGCOLOR = "#000000";
    public static final String DEFAULT_PLAYCOLOR = "#FF0000";
    public static final String DEFAULT_SELCOLOR = "#0000FF";
    public static final String DEFAULT_BGCOLOR = "#FFFFFF";

    private int width;
    private int height;
    private String font = DEFAULT_FONT;
    private String bgimage = DEFAULT_IMAGE;
    private String itemimage = DEFAULT_IMAGE;
    private String openimage = DEFAULT_IMAGE;
    private String closedimage = DEFAULT_IMAGE;
    private String fgcolor = DEFAULT_FGCOLOR;
    private String playcolor = DEFAULT_PLAYCOLOR;
    private String selcolor = DEFAULT_SELCOLOR;
    private String bgcolor1 = DEFAULT_BGCOLOR;
    private String bgcolor2 = DEFAULT_BGCOLOR;
    private boolean flat;
    private SliderItem slider;
    private boolean playlistSyntax;

    @Override
    public String elementName() {
        return playlistSyntax ? "Playlist" : "Playtree";
    }

    @Override
    public ItemType type() {
        return playlistSyntax ? ItemType.PLAYLIST : ItemType.PLAYTREE;
    }

    /**
     * A flat playlist has no folder rows.
     */
    public boolean isFlat() {
        return flat || playlistSyntax;
    }

    @Override
    public List<Item> children() {
        return slider == null ? List.of() : List.of(slider);
    }

    @Override
    public boolean usesResource(String resourceId) {
        return eq(font, resourceId) || eq(bgimage, resourceId) || eq(itemimage, resourceId)
                || eq(openimage, resourceId) || eq(closedimage, resourceId)
                || (slider != null && slider.usesResource(resourceId));
    }
}
