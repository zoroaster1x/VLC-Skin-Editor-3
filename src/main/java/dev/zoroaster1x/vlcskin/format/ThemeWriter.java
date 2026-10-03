package dev.zoroaster1x.vlcskin.format;

import dev.zoroaster1x.vlcskin.model.IncludeFile;
import dev.zoroaster1x.vlcskin.model.SkinLayout;
import dev.zoroaster1x.vlcskin.model.SkinTheme;
import dev.zoroaster1x.vlcskin.model.SkinWindow;
import dev.zoroaster1x.vlcskin.model.ThemeInfo;
import dev.zoroaster1x.vlcskin.model.item.Item;
import dev.zoroaster1x.vlcskin.model.resource.Resource;
import dev.zoroaster1x.vlcskin.util.XmlWriter;

/**
 * Writes the theme, its windows and layouts.
 */
final class ThemeWriter {

    private ThemeWriter() {
    }

    static void write(SkinTheme theme, XmlWriter writer) {
        writer.start("Theme");
        writer.attr("version", theme.getVersion());
        writer.attrIf("tooltipfont", theme.getTooltipfont(), "defaultfont");
        writer.attrIf("magnet", theme.getMagnet(), SkinTheme.DEFAULT_MAGNET);
        writer.attrIf("alpha", theme.getAlpha(), SkinTheme.DEFAULT_ALPHA);
        writer.attrIf("movealpha", theme.getMovealpha(), SkinTheme.DEFAULT_ALPHA);
        theme.foreignAttributes().forEach(writer::attr);

        writeThemeInfo(theme.getThemeInfo(), writer);
        writer.line("<!-- Created with VLC Skin Studio (https://github.com/zoroaster1x/vlc-skin-editor) -->");

        for (IncludeFile include : theme.getIncludes()) {
            writer.start("Include").attr("file", include.getFile());
            include.foreignAttributes().forEach(writer::attr);
            writer.end("Include");
        }
        for (Resource resource : theme.getResources()) {
            ResourceWriter.write(resource, writer);
        }
        for (SkinWindow window : theme.getWindows()) {
            writeWindow(window, writer);
        }
        for (String child : theme.unknownChildren()) {
            writer.line(child);
        }
        writer.end("Theme");
    }

    private static void writeThemeInfo(ThemeInfo info, XmlWriter writer) {
        writer.start("ThemeInfo");
        writer.attr("name", info.getName());
        writer.attr("author", info.getAuthor());
        writer.attr("email", info.getEmail());
        writer.attr("webpage", info.getWebpage());
        info.foreignAttributes().forEach(writer::attr);
        writer.end("ThemeInfo");
    }

    private static void writeWindow(SkinWindow window, XmlWriter writer) {
        writer.start("Window");
        writer.attrIf("id", window.getId(), SkinWindow.DEFAULT_ID);
        writer.attrIf("visible", window.getVisible(), SkinWindow.DEFAULT_VISIBLE);
        writer.attrIf("x", window.getX(), 0);
        writer.attrIf("y", window.getY(), 0);
        writer.attrIf("dragdrop", window.isDragdrop(), true);
        writer.attrIf("playondrop", window.isPlayondrop(), true);
        window.foreignAttributes().forEach(writer::attr);
        for (SkinLayout layout : window.getLayouts()) {
            writeLayout(layout, writer);
        }
        window.unknownChildren().forEach(writer::line);
        writer.end("Window");
    }

    private static void writeLayout(SkinLayout layout, XmlWriter writer) {
        writer.start("Layout");
        writer.attrIf("id", layout.getId(), SkinLayout.DEFAULT_ID);
        writer.attr("width", layout.getWidth());
        writer.attr("height", layout.getHeight());
        writer.attrIf("minwidth", layout.getMinwidth(), SkinLayout.DEFAULT_MIN);
        writer.attrIf("maxwidth", layout.getMaxwidth(), SkinLayout.DEFAULT_MAX);
        writer.attrIf("minheight", layout.getMinheight(), SkinLayout.DEFAULT_MIN);
        writer.attrIf("maxheight", layout.getMaxheight(), SkinLayout.DEFAULT_MAX);
        layout.foreignAttributes().forEach(writer::attr);
        for (Item item : layout.getItems()) {
            ItemWriter.write(item, writer);
        }
        layout.unknownChildren().forEach(writer::line);
        writer.end("Layout");
    }
}
