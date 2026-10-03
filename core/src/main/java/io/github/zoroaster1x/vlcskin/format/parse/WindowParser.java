package io.github.zoroaster1x.vlcskin.format.parse;

import io.github.zoroaster1x.vlcskin.format.XmlSupport;
import io.github.zoroaster1x.vlcskin.model.SkinLayout;
import io.github.zoroaster1x.vlcskin.model.SkinWindow;
import io.github.zoroaster1x.vlcskin.model.item.Item;
import org.w3c.dom.Element;

/**
 * Builds windows and layouts from XML.
 */
public final class WindowParser {

    private final ParseContext context;
    private final ItemParser itemParser;

    public WindowParser(ParseContext context) {
        this.context = context;
        this.itemParser = new ItemParser(context);
    }

    public SkinWindow parseWindow(Element element) {
        Attributes attrs = new Attributes(element, context.issues());
        SkinWindow window = new SkinWindow();
        String explicit = attrs.id(null);
        window.setId(explicit != null ? explicit : context.uniqueId("Window"));
        window.setVisible(attrs.str("visible", SkinWindow.DEFAULT_VISIBLE));
        window.setX(attrs.integer("x", 0));
        window.setY(attrs.integer("y", 0));
        window.setDragdrop(attrs.bool("dragdrop", true));
        window.setPlayondrop(attrs.bool("playondrop", true));
        for (Element child : XmlSupport.childElements(element)) {
            if ("Layout".equals(child.getNodeName())) {
                window.getLayouts().add(parseLayout(child));
            } else {
                window.preserveChild(XmlSupport.elementToXml(child));
            }
        }
        return attrs.finish(window);
    }

    private SkinLayout parseLayout(Element element) {
        Attributes attrs = new Attributes(element, context.issues());
        SkinLayout layout = new SkinLayout();
        String explicit = attrs.id(null);
        layout.setId(explicit != null ? explicit : context.uniqueId("Layout"));
        layout.setWidth(attrs.integer("width", 0));
        layout.setHeight(attrs.integer("height", 0));
        layout.setMinwidth(attrs.integer("minwidth", SkinLayout.DEFAULT_MIN));
        layout.setMaxwidth(attrs.integer("maxwidth", SkinLayout.DEFAULT_MAX));
        layout.setMinheight(attrs.integer("minheight", SkinLayout.DEFAULT_MIN));
        layout.setMaxheight(attrs.integer("maxheight", SkinLayout.DEFAULT_MAX));
        if (layout.getWidth() <= 0 || layout.getHeight() <= 0) {
            context.issue(io.github.zoroaster1x.vlcskin.format.ParseIssue.warning(
                    "Layout \"" + layout.getId() + "\" has a non positive size",
                    XmlSupport.path(element)));
        }
        for (Element child : XmlSupport.childElements(element)) {
            Item item = itemParser.parse(child);
            if (item != null) {
                layout.getItems().add(item);
            } else {
                layout.preserveChild(XmlSupport.elementToXml(child));
            }
        }
        return attrs.finish(layout);
    }
}
