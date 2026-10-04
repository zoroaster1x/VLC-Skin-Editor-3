package dev.zoroaster1x.vlcskin.format;

import dev.zoroaster1x.vlcskin.model.item.AbstractItem;
import dev.zoroaster1x.vlcskin.model.item.AnchorItem;
import dev.zoroaster1x.vlcskin.model.item.ButtonItem;
import dev.zoroaster1x.vlcskin.model.item.CheckboxItem;
import dev.zoroaster1x.vlcskin.model.item.GroupItem;
import dev.zoroaster1x.vlcskin.model.item.ImageItem;
import dev.zoroaster1x.vlcskin.model.item.Item;
import dev.zoroaster1x.vlcskin.model.item.PanelItem;
import dev.zoroaster1x.vlcskin.model.item.PlaytreeItem;
import dev.zoroaster1x.vlcskin.model.item.RadialSliderItem;
import dev.zoroaster1x.vlcskin.model.item.SliderBackground;
import dev.zoroaster1x.vlcskin.model.item.SliderItem;
import dev.zoroaster1x.vlcskin.model.item.TextItem;
import dev.zoroaster1x.vlcskin.model.item.VideoItem;
import dev.zoroaster1x.vlcskin.util.XmlWriter;

/**
 * Writes one layout element. Attribute order is canonical: identity first,
 * type attributes next, resize metadata last. Unknown attributes and children
 * are appended unchanged so nothing a file carried is lost.
 */
final class ItemWriter {

    private ItemWriter() {
    }

    private static String text(String value) {
        return value == null ? "" : value;
    }

    static void write(Item item, XmlWriter writer) {
        switch (item) {
            case AnchorItem anchor -> write(anchor, writer);
            case ButtonItem button -> write(button, writer);
            case CheckboxItem checkbox -> write(checkbox, writer);
            case GroupItem group -> write(group, writer);
            case ImageItem image -> write(image, writer);
            case PanelItem panel -> write(panel, writer);
            case PlaytreeItem playtree -> write(playtree, writer);
            case RadialSliderItem radial -> write(radial, writer);
            case SliderItem slider -> write(slider, writer);
            case SliderBackground background -> write(background, writer);
            case TextItem textItem -> write(textItem, writer);
            case VideoItem video -> write(video, writer);
        }
    }

    private static void head(AbstractItem item, XmlWriter writer) {
        writer.start(item.elementName());
        writer.attrIf("id", item.getId(), "Unnamed");
        writer.attrIf("x", item.getX(), 0);
        writer.attrIf("y", item.getY(), 0);
    }

    private static void tailAttributes(AbstractItem item, XmlWriter writer) {
        writer.attrIf("lefttop", item.getLefttop(), "lefttop");
        writer.attrIf("rightbottom", item.getRightbottom(), "lefttop");
        writer.attrIf("xkeepratio", item.isXkeepratio(), false);
        writer.attrIf("ykeepratio", item.isYkeepratio(), false);
        writer.attrIf("help", item.getHelp(), "");
        writer.attrIf("visible", item.getVisible(), "true");
    }

    private static void preserve(AbstractItem item, XmlWriter writer) {
        item.foreignAttributes().forEach(writer::attr);
        item.unknownChildren().forEach(writer::line);
    }

    private static void write(AnchorItem item, XmlWriter writer) {
        head(item, writer);
        writer.attrIf("points", item.getPoints(), AnchorItem.DEFAULT_POINTS);
        writer.attr("priority", item.getPriority());
        writer.attrIf("range", item.getRange(), AnchorItem.DEFAULT_RANGE);
        writer.attrIf("lefttop", item.getLefttop(), "lefttop");
        preserve(item, writer);
        writer.end(item.elementName());
    }

    private static void write(ButtonItem item, XmlWriter writer) {
        head(item, writer);
        writer.attr("up", text(item.getUp()));
        writer.attrIf("down", item.getDown(), "none");
        writer.attrIf("over", item.getOver(), "none");
        writer.attrIf("action", item.getAction(), "none");
        writer.attrIf("tooltiptext", item.getTooltiptext(), "");
        tailAttributes(item, writer);
        preserve(item, writer);
        writer.end(item.elementName());
    }

    private static void write(CheckboxItem item, XmlWriter writer) {
        head(item, writer);
        writer.attr("state", text(item.getState()));
        writer.attr("up1", text(item.getUp1()));
        writer.attrIf("down1", item.getDown1(), "none");
        writer.attrIf("over1", item.getOver1(), "none");
        writer.attrIf("action1", item.getAction1(), "none");
        writer.attrIf("tooltiptext1", item.getTooltiptext1(), "");
        writer.attr("up2", text(item.getUp2()));
        writer.attrIf("down2", item.getDown2(), "none");
        writer.attrIf("over2", item.getOver2(), "none");
        writer.attrIf("action2", item.getAction2(), "none");
        writer.attrIf("tooltiptext2", item.getTooltiptext2(), "");
        tailAttributes(item, writer);
        preserve(item, writer);
        writer.end(item.elementName());
    }

    private static void write(GroupItem item, XmlWriter writer) {
        head(item, writer);
        item.foreignAttributes().forEach(writer::attr);
        for (Item child : item.getItems()) {
            write(child, writer);
        }
        item.unknownChildren().forEach(writer::line);
        writer.end(item.elementName());
    }

    private static void write(ImageItem item, XmlWriter writer) {
        head(item, writer);
        writer.attr("image", text(item.getImage()));
        writer.attrIf("action", item.getAction(), "none");
        writer.attrIf("action2", item.getAction2(), "none");
        writer.attrIf("resize", item.getResize(), "mosaic");
        writer.attrIf("art", item.isArt(), false);
        tailAttributes(item, writer);
        preserve(item, writer);
        writer.end(item.elementName());
    }

    private static void write(PanelItem item, XmlWriter writer) {
        head(item, writer);
        writer.attr("width", item.getWidth());
        writer.attr("height", item.getHeight());
        writer.attrIf("lefttop", item.getLefttop(), "lefttop");
        writer.attrIf("rightbottom", item.getRightbottom(), "lefttop");
        writer.attrIf("xkeepratio", item.isXkeepratio(), false);
        writer.attrIf("ykeepratio", item.isYkeepratio(), false);
        item.foreignAttributes().forEach(writer::attr);
        for (Item child : item.getItems()) {
            write(child, writer);
        }
        item.unknownChildren().forEach(writer::line);
        writer.end(item.elementName());
    }

    private static void write(PlaytreeItem item, XmlWriter writer) {
        head(item, writer);
        writer.attr("width", item.getWidth());
        writer.attr("height", item.getHeight());
        writer.attr("font", text(item.getFont()));
        if (!item.isPlaylistSyntax()) {
            writer.attrIf("itemimage", item.getItemimage(), "none");
            writer.attrIf("openimage", item.getOpenimage(), "none");
            writer.attrIf("closedimage", item.getClosedimage(), "none");
            writer.attrIf("flat", item.isFlat(), false);
        }
        writer.attrIf("bgimage", item.getBgimage(), "none");
        writer.attrIf("fgcolor", item.getFgcolor(), PlaytreeItem.DEFAULT_FGCOLOR);
        writer.attrIf("playcolor", item.getPlaycolor(), PlaytreeItem.DEFAULT_PLAYCOLOR);
        writer.attrIf("selcolor", item.getSelcolor(), PlaytreeItem.DEFAULT_SELCOLOR);
        writer.attrIf("bgcolor1", item.getBgcolor1(), PlaytreeItem.DEFAULT_BGCOLOR);
        writer.attrIf("bgcolor2", item.getBgcolor2(), PlaytreeItem.DEFAULT_BGCOLOR);
        tailAttributes(item, writer);
        item.foreignAttributes().forEach(writer::attr);
        if (item.getSlider() != null) {
            write(item.getSlider(), writer);
        }
        item.unknownChildren().forEach(writer::line);
        writer.end(item.elementName());
    }

    private static void write(RadialSliderItem item, XmlWriter writer) {
        head(item, writer);
        writer.attr("sequence", text(item.getSequence()));
        writer.attr("nbimages", item.getNbimages());
        writer.attrIf("minangle", item.getMinangle(), 0);
        writer.attrIf("maxangle", item.getMaxangle(), RadialSliderItem.DEFAULT_MAX_ANGLE);
        writer.attrIf("value", item.getValue(), "none");
        writer.attrIf("tooltiptext", item.getTooltiptext(), "");
        tailAttributes(item, writer);
        preserve(item, writer);
        writer.end(item.elementName());
    }

    private static void write(SliderItem item, XmlWriter writer) {
        head(item, writer);
        writer.attr("up", text(item.getUp()));
        writer.attrIf("down", item.getDown(), "none");
        writer.attrIf("over", item.getOver(), "none");
        writer.attr("points", item.getPoints());
        writer.attrIf("thickness", item.getThickness(), SliderItem.DEFAULT_THICKNESS);
        if (!item.isInPlaytree()) {
            writer.attrIf("value", item.getValue(), "none");
        }
        writer.attrIf("tooltiptext", item.getTooltiptext(), "");
        tailAttributes(item, writer);
        item.foreignAttributes().forEach(writer::attr);
        if (item.getBackground() != null) {
            write(item.getBackground(), writer);
        }
        item.unknownChildren().forEach(writer::line);
        writer.end(item.elementName());
    }

    private static void write(SliderBackground item, XmlWriter writer) {
        writer.start(item.elementName());
        writer.attrIf("id", item.getId(), "Unnamed");
        writer.attr("image", text(item.getImage()));
        writer.attrIf("nbhoriz", item.getNbhoriz(), 1);
        writer.attrIf("nbvert", item.getNbvert(), 1);
        writer.attrIf("padhoriz", item.getPadhoriz(), 0);
        writer.attrIf("padvert", item.getPadvert(), 0);
        item.foreignAttributes().forEach(writer::attr);
        item.unknownChildren().forEach(writer::line);
        writer.end(item.elementName());
    }

    private static void write(TextItem item, XmlWriter writer) {
        head(item, writer);
        writer.attrIf("text", item.getText(), "");
        writer.attr("font", text(item.getFont()));
        writer.attrIf("color", item.getColor(), "#000000");
        writer.attrIf("width", item.getWidth(), 0);
        writer.attrIf("alignment", item.getAlignment(), "left");
        writer.attrIf("scrolling", item.getScrolling(), "auto");
        tailAttributes(item, writer);
        preserve(item, writer);
        writer.end(item.elementName());
    }

    private static void write(VideoItem item, XmlWriter writer) {
        head(item, writer);
        writer.attrIf("width", item.getWidth(), 0);
        writer.attrIf("height", item.getHeight(), 0);
        writer.attrIf("autoresize", item.isAutoresize(), true);
        tailAttributes(item, writer);
        preserve(item, writer);
        writer.end(item.elementName());
    }
}
