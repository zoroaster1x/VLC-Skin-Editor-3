package dev.zoroaster1x.vlcskin.app.panel;

/**
 * What a tree node points at.
 */
public record TreeRef(Kind kind, String id, String label, String itemType) {

    public enum Kind {
        THEME,
        RESOURCE,
        SUB_BITMAP,
        WINDOW,
        LAYOUT,
        ITEM,
        SLIDER_BACKGROUND
    }

    public TreeRef(Kind kind, String id, String label) {
        this(kind, id, label, null);
    }

    @Override
    public String toString() {
        return label;
    }
}
