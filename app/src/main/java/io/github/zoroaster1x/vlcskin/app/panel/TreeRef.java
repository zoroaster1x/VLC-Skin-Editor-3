package io.github.zoroaster1x.vlcskin.app.panel;

/**
 * What a tree node points at.
 */
public record TreeRef(Kind kind, String id, String label) {

    public enum Kind {
        THEME,
        RESOURCE,
        SUB_BITMAP,
        WINDOW,
        LAYOUT,
        ITEM,
        SLIDER_BACKGROUND
    }

    @Override
    public String toString() {
        return label;
    }
}
