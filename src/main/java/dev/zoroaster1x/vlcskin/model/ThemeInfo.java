package dev.zoroaster1x.vlcskin.model;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;

/**
 * The ThemeInfo element: who made the theme.
 */
@Getter
@Setter
@EqualsAndHashCode(callSuper = false)
public final class ThemeInfo extends SkinNode {

    private String name = "Unnamed theme";
    private String author = "Unknown author";
    private String email = "Unknown";
    private String webpage = "http://www.videolan.org/vlc/";
}
