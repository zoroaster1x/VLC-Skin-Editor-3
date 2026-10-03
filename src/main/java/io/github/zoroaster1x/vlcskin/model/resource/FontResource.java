package io.github.zoroaster1x.vlcskin.model.resource;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;

/**
 * A TrueType or OpenType font file used by text items.
 */
@Getter
@Setter
@EqualsAndHashCode(callSuper = false)
public final class FontResource extends AbstractResource {

    public static final int DEFAULT_SIZE = 12;

    private String file;
    private int size = DEFAULT_SIZE;

    @Override
    public String elementName() {
        return "Font";
    }

    @Override
    public String typeName() {
        return "Font";
    }

    public FontResource copy() {
        FontResource copy = new FontResource();
        copy.setId(getId());
        copy.setFile(file);
        copy.setSize(size);
        foreignAttributes().forEach(copy::preserveAttribute);
        return copy;
    }
}
