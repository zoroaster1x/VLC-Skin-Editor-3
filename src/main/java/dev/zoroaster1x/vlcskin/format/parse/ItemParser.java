package dev.zoroaster1x.vlcskin.format.parse;

import dev.zoroaster1x.vlcskin.format.XmlSupport;
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
import java.util.List;
import java.util.function.Consumer;
import org.w3c.dom.Element;

/**
 * Builds layout elements from XML. Returns null for unknown tags.
 */
public final class ItemParser {

    private final ParseContext context;

    public ItemParser(ParseContext context) {
        this.context = context;
    }

    public Item parse(Element element) {
        return switch (element.getNodeName()) {
            case "Anchor" -> parseAnchor(element);
            case "Button" -> parseButton(element);
            case "Checkbox" -> parseCheckbox(element);
            case "Group" -> parseGroup(element);
            case "Image" -> parseImage(element);
            case "Panel" -> parsePanel(element);
            case "Playlist" -> parsePlaytree(element, true);
            case "Playtree" -> parsePlaytree(element, false);
            case "RadialSlider" -> parseRadialSlider(element);
            case "Slider" -> parseSlider(element, false);
            case "Text" -> parseText(element);
            case "Video" -> parseVideo(element);
            default -> null;
        };
    }

    /**
     * Parses the item children of a container, preserving unknown elements.
     */
    public void parseChildren(Element parent, List<Item> target, dev.zoroaster1x.vlcskin.model.SkinNode owner) {
        for (Element child : XmlSupport.childElements(parent)) {
            Item item = parse(child);
            if (item != null) {
                target.add(item);
            } else {
                owner.preserveChild(XmlSupport.elementToXml(child));
            }
        }
    }

    private void common(AbstractItem item, Attributes attrs) {
        String explicit = attrs.id(null);
        item.setId(explicit != null ? explicit : context.uniqueId(item.type().displayName()));
        item.setX(attrs.integer("x", 0));
        item.setY(attrs.integer("y", 0));
    }

    private void generalTail(AbstractItem item, Attributes attrs) {
        item.setLefttop(attrs.str("lefttop", AbstractItem.DEFAULT_LEFTTOP));
        item.setRightbottom(attrs.str("rightbottom", AbstractItem.DEFAULT_RIGHTBOTTOM));
        item.setXkeepratio(attrs.bool("xkeepratio", false));
        item.setYkeepratio(attrs.bool("ykeepratio", false));
        item.setHelp(attrs.str("help", AbstractItem.DEFAULT_HELP));
        item.setVisible(attrs.str("visible", AbstractItem.DEFAULT_VISIBLE));
    }

    private AnchorItem parseAnchor(Element element) {
        Attributes attrs = new Attributes(element, context.issues());
        AnchorItem item = new AnchorItem();
        common(item, attrs);
        item.setPoints(attrs.str("points", AnchorItem.DEFAULT_POINTS));
        item.setPriority(attrs.integer("priority", 0));
        item.setRange(attrs.integer("range", AnchorItem.DEFAULT_RANGE));
        item.setLefttop(attrs.str("lefttop", AbstractItem.DEFAULT_LEFTTOP));
        return attrs.finish(item);
    }

    private ButtonItem parseButton(Element element) {
        Attributes attrs = new Attributes(element, context.issues());
        ButtonItem item = new ButtonItem();
        common(item, attrs);
        item.setUp(attrs.required("up", ""));
        item.setDown(attrs.str("down", ButtonItem.DEFAULT_DOWN));
        item.setOver(attrs.str("over", ButtonItem.DEFAULT_OVER));
        item.setAction(attrs.str("action", ButtonItem.DEFAULT_ACTION));
        item.setTooltiptext(attrs.str("tooltiptext", ButtonItem.DEFAULT_TOOLTIPTEXT));
        generalTail(item, attrs);
        return attrs.finish(item);
    }

    private CheckboxItem parseCheckbox(Element element) {
        Attributes attrs = new Attributes(element, context.issues());
        CheckboxItem item = new CheckboxItem();
        common(item, attrs);
        item.setState(attrs.str("state", CheckboxItem.DEFAULT_STATE));
        item.setUp1(attrs.required("up1", ""));
        item.setDown1(attrs.str("down1", CheckboxItem.DEFAULT_DOWN));
        item.setOver1(attrs.str("over1", CheckboxItem.DEFAULT_OVER));
        item.setAction1(attrs.str("action1", CheckboxItem.DEFAULT_ACTION));
        item.setTooltiptext1(attrs.str("tooltiptext1", CheckboxItem.DEFAULT_TOOLTIPTEXT));
        item.setUp2(attrs.required("up2", ""));
        item.setDown2(attrs.str("down2", CheckboxItem.DEFAULT_DOWN));
        item.setOver2(attrs.str("over2", CheckboxItem.DEFAULT_OVER));
        item.setAction2(attrs.str("action2", CheckboxItem.DEFAULT_ACTION));
        item.setTooltiptext2(attrs.str("tooltiptext2", CheckboxItem.DEFAULT_TOOLTIPTEXT));
        generalTail(item, attrs);
        return attrs.finish(item);
    }

    private GroupItem parseGroup(Element element) {
        Attributes attrs = new Attributes(element, context.issues());
        GroupItem item = new GroupItem();
        common(item, attrs);
        parseChildren(element, item.getItems(), item);
        return attrs.finish(item);
    }

    private ImageItem parseImage(Element element) {
        Attributes attrs = new Attributes(element, context.issues());
        ImageItem item = new ImageItem();
        common(item, attrs);
        item.setImage(attrs.required("image", ""));
        item.setAction(attrs.str("action", ImageItem.DEFAULT_ACTION));
        item.setAction2(attrs.str("action2", ImageItem.DEFAULT_ACTION));
        item.setResize(attrs.str("resize", ImageItem.DEFAULT_RESIZE));
        item.setArt(attrs.bool("art", false));
        generalTail(item, attrs);
        return attrs.finish(item);
    }

    private PanelItem parsePanel(Element element) {
        Attributes attrs = new Attributes(element, context.issues());
        PanelItem item = new PanelItem();
        common(item, attrs);
        item.setWidth(attrs.integer("width", 0));
        item.setHeight(attrs.integer("height", 0));
        item.setLefttop(attrs.str("lefttop", AbstractItem.DEFAULT_LEFTTOP));
        item.setRightbottom(attrs.str("rightbottom", AbstractItem.DEFAULT_RIGHTBOTTOM));
        item.setXkeepratio(attrs.bool("xkeepratio", false));
        item.setYkeepratio(attrs.bool("ykeepratio", false));
        parseChildren(element, item.getItems(), item);
        return attrs.finish(item);
    }

    private PlaytreeItem parsePlaytree(Element element, boolean playlistSyntax) {
        Attributes attrs = new Attributes(element, context.issues());
        PlaytreeItem item = new PlaytreeItem();
        item.setPlaylistSyntax(playlistSyntax);
        common(item, attrs);
        item.setWidth(attrs.integer("width", 0));
        item.setHeight(attrs.integer("height", 0));
        item.setFont(attrs.required("font", PlaytreeItem.DEFAULT_FONT));
        if (!playlistSyntax) {
            item.setItemimage(attrs.str("itemimage", PlaytreeItem.DEFAULT_IMAGE));
            item.setOpenimage(attrs.str("openimage", PlaytreeItem.DEFAULT_IMAGE));
            item.setClosedimage(attrs.str("closedimage", PlaytreeItem.DEFAULT_IMAGE));
            item.setFlat(attrs.bool("flat", false));
        }
        item.setBgimage(attrs.str("bgimage", PlaytreeItem.DEFAULT_IMAGE));
        item.setFgcolor(attrs.str("fgcolor", PlaytreeItem.DEFAULT_FGCOLOR));
        item.setPlaycolor(attrs.str("playcolor", PlaytreeItem.DEFAULT_PLAYCOLOR));
        item.setSelcolor(attrs.str("selcolor", PlaytreeItem.DEFAULT_SELCOLOR));
        item.setBgcolor1(attrs.str("bgcolor1", PlaytreeItem.DEFAULT_BGCOLOR));
        item.setBgcolor2(attrs.str("bgcolor2", PlaytreeItem.DEFAULT_BGCOLOR));
        generalTail(item, attrs);
        for (Element child : XmlSupport.childElements(element)) {
            if ("Slider".equals(child.getNodeName())) {
                item.setSlider(parseSlider(child, true));
            } else {
                item.preserveChild(XmlSupport.elementToXml(child));
            }
        }
        return attrs.finish(item);
    }

    private RadialSliderItem parseRadialSlider(Element element) {
        Attributes attrs = new Attributes(element, context.issues());
        RadialSliderItem item = new RadialSliderItem();
        common(item, attrs);
        item.setSequence(attrs.required("sequence", ""));
        item.setNbimages(attrs.integer("nbimages", 0));
        item.setMinangle(attrs.integer("minangle", 0));
        item.setMaxangle(attrs.integer("maxangle", RadialSliderItem.DEFAULT_MAX_ANGLE));
        item.setValue(attrs.str("value", RadialSliderItem.DEFAULT_VALUE));
        item.setTooltiptext(attrs.str("tooltiptext", RadialSliderItem.DEFAULT_TOOLTIPTEXT));
        generalTail(item, attrs);
        return attrs.finish(item);
    }

    private SliderItem parseSlider(Element element, boolean inPlaytree) {
        Attributes attrs = new Attributes(element, context.issues());
        SliderItem item = new SliderItem();
        item.setInPlaytree(inPlaytree);
        common(item, attrs);
        item.setUp(attrs.required("up", ""));
        item.setDown(attrs.str("down", SliderItem.DEFAULT_DOWN));
        item.setOver(attrs.str("over", SliderItem.DEFAULT_OVER));
        item.setPoints(attrs.str("points", SliderItem.DEFAULT_POINTS));
        item.setThickness(attrs.integer("thickness", SliderItem.DEFAULT_THICKNESS));
        item.setValue(attrs.str("value", SliderItem.DEFAULT_VALUE));
        item.setTooltiptext(attrs.str("tooltiptext", SliderItem.DEFAULT_TOOLTIPTEXT));
        generalTail(item, attrs);
        for (Element child : XmlSupport.childElements(element)) {
            if ("SliderBackground".equals(child.getNodeName())) {
                item.setBackground(parseSliderBackground(child));
            } else {
                item.preserveChild(XmlSupport.elementToXml(child));
            }
        }
        return attrs.finish(item);
    }

    private SliderBackground parseSliderBackground(Element element) {
        Attributes attrs = new Attributes(element, context.issues());
        SliderBackground background = new SliderBackground();
        String explicit = attrs.id(null);
        background.setId(explicit != null ? explicit : context.uniqueId("Slider background"));
        background.setImage(attrs.required("image", ""));
        background.setNbhoriz(attrs.integer("nbhoriz", 1));
        background.setNbvert(attrs.integer("nbvert", 1));
        background.setPadhoriz(attrs.integer("padhoriz", 0));
        background.setPadvert(attrs.integer("padvert", 0));
        return attrs.finish(background);
    }

    private TextItem parseText(Element element) {
        Attributes attrs = new Attributes(element, context.issues());
        TextItem item = new TextItem();
        common(item, attrs);
        item.setText(attrs.str("text", ""));
        item.setFont(attrs.required("font", "defaultfont"));
        item.setColor(attrs.str("color", TextItem.DEFAULT_COLOR));
        item.setWidth(attrs.integer("width", 0));
        item.setAlignment(attrs.str("alignment", TextItem.DEFAULT_ALIGNMENT));
        item.setScrolling(attrs.str("scrolling", TextItem.DEFAULT_SCROLLING));
        generalTail(item, attrs);
        return attrs.finish(item);
    }

    private VideoItem parseVideo(Element element) {
        Attributes attrs = new Attributes(element, context.issues());
        VideoItem item = new VideoItem();
        common(item, attrs);
        item.setWidth(attrs.integer("width", 0));
        item.setHeight(attrs.integer("height", 0));
        item.setAutoresize(attrs.bool("autoresize", true));
        generalTail(item, attrs);
        return attrs.finish(item);
    }

    /**
     * Applies a consumer to every parsed item, including nested ones.
     */
    public void walk(Item item, Consumer<Item> consumer) {
        consumer.accept(item);
        for (Item child : item.children()) {
            walk(child, consumer);
        }
    }
}
