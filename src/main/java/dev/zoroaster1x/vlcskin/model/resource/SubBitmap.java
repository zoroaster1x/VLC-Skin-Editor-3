package dev.zoroaster1x.vlcskin.model.resource;

import dev.zoroaster1x.vlcskin.model.SkinNode;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;

/**
 * A rectangular cut out of a parent bitmap.
 */
@Getter
@Setter
@EqualsAndHashCode(callSuper = false)
public final class SubBitmap extends SkinNode {

    private String id;
    private int x;
    private int y;
    private int width;
    private int height;
    private int nbframes = 1;
    private int fps;

    public String elementName() {
        return "SubBitmap";
    }

    public SubBitmap copy() {
        SubBitmap copy = new SubBitmap();
        copy.id = id;
        copy.x = x;
        copy.y = y;
        copy.width = width;
        copy.height = height;
        copy.nbframes = nbframes;
        copy.fps = fps;
        foreignAttributes().forEach(copy::preserveAttribute);
        return copy;
    }
}
