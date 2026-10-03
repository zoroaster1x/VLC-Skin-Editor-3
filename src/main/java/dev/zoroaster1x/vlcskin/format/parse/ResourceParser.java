package dev.zoroaster1x.vlcskin.format.parse;

import dev.zoroaster1x.vlcskin.format.XmlSupport;
import dev.zoroaster1x.vlcskin.model.IncludeFile;
import dev.zoroaster1x.vlcskin.model.resource.BitmapFontResource;
import dev.zoroaster1x.vlcskin.model.resource.BitmapResource;
import dev.zoroaster1x.vlcskin.model.resource.FontResource;
import dev.zoroaster1x.vlcskin.model.resource.IniFileResource;
import dev.zoroaster1x.vlcskin.model.resource.MenuItemEntry;
import dev.zoroaster1x.vlcskin.model.resource.MenuSeparatorEntry;
import dev.zoroaster1x.vlcskin.model.resource.PopupMenuResource;
import dev.zoroaster1x.vlcskin.model.resource.Resource;
import dev.zoroaster1x.vlcskin.model.resource.SubBitmap;
import org.w3c.dom.Element;

/**
 * Builds resources from XML.
 */
public final class ResourceParser {

    private final ParseContext context;

    public ResourceParser(ParseContext context) {
        this.context = context;
    }

    public Resource parse(Element element) {
        return switch (element.getNodeName()) {
            case "Bitmap" -> parseBitmap(element);
            case "Font" -> parseFont(element);
            case "BitmapFont" -> parseBitmapFont(element);
            case "PopupMenu" -> parsePopupMenu(element);
            case "IniFile" -> parseIniFile(element);
            default -> null;
        };
    }

    public IncludeFile parseInclude(Element element) {
        Attributes attrs = new Attributes(element, context.issues());
        IncludeFile include = new IncludeFile();
        include.setFile(attrs.required("file", ""));
        return attrs.finish(include);
    }

    private BitmapResource parseBitmap(Element element) {
        Attributes attrs = new Attributes(element, context.issues());
        BitmapResource bitmap = new BitmapResource();
        bitmap.setId(attrs.required("id", context.uniqueId("Bitmap")));
        bitmap.setFile(attrs.required("file", ""));
        bitmap.setAlphacolor(attrs.str("alphacolor", BitmapResource.DEFAULT_ALPHACOLOR));
        bitmap.setNbframes(attrs.integer("nbframes", 1));
        bitmap.setFps(attrs.integer("fps", 0));
        for (Element child : XmlSupport.childElements(element)) {
            if ("SubBitmap".equals(child.getNodeName())) {
                bitmap.getSubBitmaps().add(parseSubBitmap(child));
            } else {
                bitmap.preserveChild(XmlSupport.elementToXml(child));
            }
        }
        if (bitmap.getNbframes() < 1) {
            context.issue(dev.zoroaster1x.vlcskin.format.ParseIssue.warning(
                    "Bitmap \"" + bitmap.getId() + "\" has nbframes < 1; using 1",
                    XmlSupport.path(element)));
            bitmap.setNbframes(1);
        }
        return attrs.finish(bitmap);
    }

    private SubBitmap parseSubBitmap(Element element) {
        Attributes attrs = new Attributes(element, context.issues());
        SubBitmap sub = new SubBitmap();
        sub.setId(attrs.required("id", context.uniqueId("SubBitmap")));
        sub.setX(attrs.integer("x", 0));
        sub.setY(attrs.integer("y", 0));
        sub.setWidth(attrs.integer("width", 0));
        sub.setHeight(attrs.integer("height", 0));
        sub.setNbframes(attrs.integer("nbframes", 1));
        sub.setFps(attrs.integer("fps", 0));
        return attrs.finish(sub);
    }

    private FontResource parseFont(Element element) {
        Attributes attrs = new Attributes(element, context.issues());
        FontResource font = new FontResource();
        font.setId(attrs.required("id", context.uniqueId("Font")));
        font.setFile(attrs.required("file", ""));
        font.setSize(attrs.integer("size", FontResource.DEFAULT_SIZE));
        return attrs.finish(font);
    }

    private BitmapFontResource parseBitmapFont(Element element) {
        Attributes attrs = new Attributes(element, context.issues());
        BitmapFontResource font = new BitmapFontResource();
        font.setId(attrs.required("id", context.uniqueId("Bitmap font")));
        font.setFile(attrs.required("file", ""));
        font.setType(attrs.str("type", BitmapFontResource.DEFAULT_TYPE));
        return attrs.finish(font);
    }

    private PopupMenuResource parsePopupMenu(Element element) {
        Attributes attrs = new Attributes(element, context.issues());
        PopupMenuResource menu = new PopupMenuResource();
        menu.setId(attrs.required("id", context.uniqueId("Popup menu")));
        for (Element child : XmlSupport.childElements(element)) {
            switch (child.getNodeName()) {
                case "MenuItem" -> {
                    Attributes itemAttrs = new Attributes(child, context.issues());
                    MenuItemEntry item = new MenuItemEntry();
                    item.setLabel(itemAttrs.required("label", ""));
                    item.setAction(itemAttrs.str("action", MenuItemEntry.DEFAULT_ACTION));
                    menu.getEntries().add(itemAttrs.finish(item));
                }
                case "MenuSeparator" -> {
                    Attributes separatorAttrs = new Attributes(child, context.issues());
                    menu.getEntries().add(separatorAttrs.finish(new MenuSeparatorEntry()));
                }
                default -> menu.preserveChild(XmlSupport.elementToXml(child));
            }
        }
        return attrs.finish(menu);
    }

    private IniFileResource parseIniFile(Element element) {
        Attributes attrs = new Attributes(element, context.issues());
        IniFileResource ini = new IniFileResource();
        ini.setId(attrs.required("id", context.uniqueId("Ini file")));
        ini.setFile(attrs.required("file", ""));
        return attrs.finish(ini);
    }
}
