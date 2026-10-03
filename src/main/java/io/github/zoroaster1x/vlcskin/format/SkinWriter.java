package io.github.zoroaster1x.vlcskin.format;

import io.github.zoroaster1x.vlcskin.model.SkinTheme;
import io.github.zoroaster1x.vlcskin.util.XmlWriter;

/**
 * Serializes a theme to skin XML text.
 */
public final class SkinWriter {

    public static final String DOCTYPE =
            "<!DOCTYPE Theme PUBLIC \"-//VideoLAN//DTD VLC Skins V2.0//EN\" \"skin.dtd\">";
    public static final String INDENT = "  ";

    private SkinWriter() {
    }

    public static String toXml(SkinTheme theme) {
        XmlWriter writer = new XmlWriter(INDENT);
        ThemeWriter.write(theme, writer);
        return DOCTYPE + "\n" + writer.toXml() + "\n";
    }
}
