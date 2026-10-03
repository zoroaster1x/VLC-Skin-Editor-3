package dev.zoroaster1x.vlcskin.model;

/**
 * The layout element kinds a VLC skins2 theme can contain.
 */
public enum ItemType {
    ANCHOR("Anchor", "Anchor"),
    BUTTON("Button", "Button"),
    CHECKBOX("Checkbox", "Checkbox"),
    GROUP("Group", "Group"),
    IMAGE("Image", "Image"),
    PANEL("Panel", "Panel"),
    PLAYLIST("Playlist", "Playlist"),
    PLAYTREE("Playtree", "Playtree"),
    RADIAL_SLIDER("RadialSlider", "Radial slider"),
    SLIDER("Slider", "Slider"),
    SLIDER_BACKGROUND("SliderBackground", "Slider background"),
    TEXT("Text", "Text"),
    VIDEO("Video", "Video");

    private final String xmlName;
    private final String displayName;

    ItemType(String xmlName, String displayName) {
        this.xmlName = xmlName;
        this.displayName = displayName;
    }

    public String xmlName() {
        return xmlName;
    }

    public String displayName() {
        return displayName;
    }
}
