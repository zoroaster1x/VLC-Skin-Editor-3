package dev.zoroaster1x.vlcskin.format;

import dev.zoroaster1x.vlcskin.model.resource.BitmapFontResource;
import dev.zoroaster1x.vlcskin.model.resource.BitmapResource;
import dev.zoroaster1x.vlcskin.model.resource.FontResource;
import dev.zoroaster1x.vlcskin.model.resource.IniFileResource;
import dev.zoroaster1x.vlcskin.model.resource.MenuItemEntry;
import dev.zoroaster1x.vlcskin.model.resource.MenuSeparatorEntry;
import dev.zoroaster1x.vlcskin.model.resource.PopupMenuResource;
import dev.zoroaster1x.vlcskin.model.resource.Resource;
import dev.zoroaster1x.vlcskin.model.resource.SubBitmap;
import dev.zoroaster1x.vlcskin.util.XmlWriter;

/**
 * Writes bitmap, font, popup menu and ini resources.
 */
final class ResourceWriter {

    private ResourceWriter() {
    }

    private static String text(String value) {
        return value == null ? "" : value;
    }

    static void write(Resource resource, XmlWriter writer) {
        switch (resource) {
            case BitmapResource bitmap -> write(bitmap, writer);
            case FontResource font -> write(font, writer);
            case BitmapFontResource bitmapFont -> write(bitmapFont, writer);
            case PopupMenuResource popupMenu -> write(popupMenu, writer);
            case IniFileResource iniFile -> write(iniFile, writer);
        }
    }

    static void write(BitmapResource bitmap, XmlWriter writer) {
        writer.start("Bitmap");
        writer.attr("id", text(bitmap.getId()));
        writer.attr("file", text(bitmap.getFile()));
        writer.attr("alphacolor", text(bitmap.getAlphacolor()));
        writer.attrIf("nbframes", bitmap.getNbframes(), 1);
        writer.attrIf("fps", bitmap.getFps(), 0);
        bitmap.foreignAttributes().forEach(writer::attr);
        for (SubBitmap sub : bitmap.getSubBitmaps()) {
            write(sub, writer);
        }
        bitmap.unknownChildren().forEach(writer::line);
        writer.end("Bitmap");
    }

    private static void write(SubBitmap sub, XmlWriter writer) {
        writer.start("SubBitmap");
        writer.attr("id", text(sub.getId()));
        writer.attr("x", sub.getX());
        writer.attr("y", sub.getY());
        writer.attr("height", sub.getHeight());
        writer.attr("width", sub.getWidth());
        writer.attrIf("nbframes", sub.getNbframes(), 1);
        writer.attrIf("fps", sub.getFps(), 0);
        sub.foreignAttributes().forEach(writer::attr);
        sub.unknownChildren().forEach(writer::line);
        writer.end("SubBitmap");
    }

    private static void write(FontResource font, XmlWriter writer) {
        writer.start("Font");
        writer.attr("id", text(font.getId()));
        writer.attr("file", text(font.getFile()));
        writer.attrIf("size", font.getSize(), FontResource.DEFAULT_SIZE);
        font.foreignAttributes().forEach(writer::attr);
        font.unknownChildren().forEach(writer::line);
        writer.end("Font");
    }

    private static void write(BitmapFontResource font, XmlWriter writer) {
        writer.start("BitmapFont");
        writer.attr("id", text(font.getId()));
        writer.attr("file", text(font.getFile()));
        writer.attrIf("type", font.getType(), BitmapFontResource.DEFAULT_TYPE);
        font.foreignAttributes().forEach(writer::attr);
        font.unknownChildren().forEach(writer::line);
        writer.end("BitmapFont");
    }

    private static void write(PopupMenuResource menu, XmlWriter writer) {
        writer.start("PopupMenu");
        writer.attr("id", text(menu.getId()));
        menu.foreignAttributes().forEach(writer::attr);
        for (PopupMenuResource.MenuEntry entry : menu.getEntries()) {
            switch (entry) {
                case MenuItemEntry item -> {
                    writer.start("MenuItem");
                    writer.attr("label", item.getLabel());
                    writer.attrIf("action", item.getAction(), "none");
                    item.foreignAttributes().forEach(writer::attr);
                    writer.end("MenuItem");
                }
                case MenuSeparatorEntry separator -> {
                    writer.start("MenuSeparator");
                    separator.foreignAttributes().forEach(writer::attr);
                    writer.end("MenuSeparator");
                }
            }
        }
        menu.unknownChildren().forEach(writer::line);
        writer.end("PopupMenu");
    }

    private static void write(IniFileResource ini, XmlWriter writer) {
        writer.start("IniFile");
        writer.attr("id", text(ini.getId()));
        writer.attr("file", text(ini.getFile()));
        ini.foreignAttributes().forEach(writer::attr);
        ini.unknownChildren().forEach(writer::line);
        writer.end("IniFile");
    }
}
