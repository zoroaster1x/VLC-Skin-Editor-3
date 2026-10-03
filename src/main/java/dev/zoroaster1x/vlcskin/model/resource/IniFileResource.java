package dev.zoroaster1x.vlcskin.model.resource;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;

/**
 * An INI file VLC reads for defaults; kept in the model for round trips.
 */
@Getter
@Setter
@EqualsAndHashCode(callSuper = false)
public final class IniFileResource extends AbstractResource {

    private String file;

    @Override
    public String elementName() {
        return "IniFile";
    }

    @Override
    public String typeName() {
        return "Ini file";
    }

    public IniFileResource copy() {
        IniFileResource copy = new IniFileResource();
        copy.setId(getId());
        copy.setFile(file);
        foreignAttributes().forEach(copy::preserveAttribute);
        return copy;
    }
}
