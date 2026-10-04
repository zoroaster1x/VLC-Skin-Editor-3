package dev.zoroaster1x.vlcskin.render.draw;

import dev.zoroaster1x.vlcskin.model.SkinIndex;
import java.awt.Color;

/**
 * Parses a skins2 color value, resolving {@code id.section.key} constants an
 * IniFile resource registered first, the way VLC's getColor does.
 */
public final class Colors {

    private Colors() {
    }

    public static Color parse(SkinIndex index, String value, Color fallback) {
        if (value == null) {
            return fallback;
        }
        String resolved = index == null ? null : index.constant(value);
        if (resolved == null || resolved.isEmpty()) {
            resolved = value;
        }
        try {
            return Color.decode(resolved.startsWith("#") ? resolved : "#" + resolved);
        } catch (NumberFormatException ex) {
            return fallback;
        }
    }
}
