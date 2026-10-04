package dev.zoroaster1x.vlcskin.app.i18n;

import dev.zoroaster1x.vlcskin.model.ItemType;
import dev.zoroaster1x.vlcskin.model.resource.BitmapFontResource;
import dev.zoroaster1x.vlcskin.model.resource.BitmapResource;
import dev.zoroaster1x.vlcskin.model.resource.FontResource;
import dev.zoroaster1x.vlcskin.model.resource.IniFileResource;
import dev.zoroaster1x.vlcskin.model.resource.PopupMenuResource;
import dev.zoroaster1x.vlcskin.model.resource.Resource;

/**
 * Translated names for item and resource kinds. One place, because the items
 * tree, the inspector title and the resource tree all label the same kinds.
 */
public final class TypeNames {

    private TypeNames() {
    }

    public static String item(ItemType type) {
        return switch (type) {
            case ANCHOR -> Messages.get("ANCHOR", "Anchor");
            case BUTTON -> Messages.get("BUTTON", "Button");
            case CHECKBOX -> Messages.get("CHECKBOX", "Checkbox");
            case GROUP -> Messages.get("GROUP", "Group");
            case IMAGE -> Messages.get("IMAGE", "Image");
            case PANEL -> Messages.get("PANEL", "Panel");
            case PLAYLIST -> Messages.get("APP_TYPE_PLAYLIST", "Playlist");
            case PLAYTREE -> Messages.get("PLAYTREE", "Playtree");
            case RADIAL_SLIDER -> Messages.get("RADIALSLIDER", "Radial slider");
            case SLIDER -> Messages.get("SLIDER", "Slider");
            case SLIDER_BACKGROUND -> Messages.get("SLIDERBG", "SliderBackground");
            case TEXT -> Messages.get("TEXT", "Text");
            case VIDEO -> Messages.get("VIDEO", "Video");
        };
    }

    public static String resource(Resource resource) {
        return switch (resource) {
            case BitmapResource ignored -> Messages.get("BITMAP", "Bitmap");
            case FontResource ignored -> Messages.get("FONT", "Font");
            case BitmapFontResource ignored -> Messages.get("APP_TYPE_BITMAP_FONT", "Bitmap font");
            case PopupMenuResource ignored -> Messages.get("APP_TYPE_POPUP_MENU", "Popup menu");
            case IniFileResource ignored -> Messages.get("APP_TYPE_INI_FILE", "Ini file");
            default -> resource.typeName();
        };
    }
}
