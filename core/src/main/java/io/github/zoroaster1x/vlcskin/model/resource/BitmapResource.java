package io.github.zoroaster1x.vlcskin.model.resource;

import io.github.zoroaster1x.vlcskin.model.SkinNode;
import java.util.LinkedList;
import java.util.List;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;

/**
 * A PNG that can be cut into sub bitmaps and frame strips.
 */
@Getter
@Setter
@EqualsAndHashCode(callSuper = false)
public final class BitmapResource extends AbstractResource {

    public static final String DEFAULT_ALPHACOLOR = "#FF00FF";

    private String file;
    private String alphacolor = DEFAULT_ALPHACOLOR;
    private int nbframes = 1;
    private int fps;
    private final List<SubBitmap> subBitmaps = new LinkedList<>();

    @Override
    public String elementName() {
        return "Bitmap";
    }

    @Override
    public String typeName() {
        return "Bitmap";
    }

    /**
     * A copy of this resource including its sub bitmaps.
     */
    public BitmapResource copy() {
        BitmapResource copy = new BitmapResource();
        copy.setId(getId());
        copy.setFile(file);
        copy.setAlphacolor(alphacolor);
        copy.setNbframes(nbframes);
        copy.setFps(fps);
        for (SubBitmap sub : subBitmaps) {
            copy.subBitmaps.add(sub.copy());
        }
        foreignAttributes().forEach(copy::preserveAttribute);
        return copy;
    }
}
