package dev.zoroaster1x.vlcskin.model.resource;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;

/**
 * A digit sheet font, used by VLC for counters.
 */
@Getter
@Setter
@EqualsAndHashCode(callSuper = false)
public final class BitmapFontResource extends AbstractResource {

    public static final String DEFAULT_TYPE = "digits";

    private String file;
    private String type = DEFAULT_TYPE;

    @Override
    public String elementName() {
        return "BitmapFont";
    }

    @Override
    public String typeName() {
        return "Bitmap font";
    }

    public BitmapFontResource copy() {
        BitmapFontResource copy = new BitmapFontResource();
        copy.setId(getId());
        copy.setFile(file);
        copy.setType(type);
        foreignAttributes().forEach(copy::preserveAttribute);
        return copy;
    }
}
