package dev.zoroaster1x.vlcskin.format.parse;

import dev.zoroaster1x.vlcskin.format.ParseIssue;
import dev.zoroaster1x.vlcskin.format.XmlSupport;
import dev.zoroaster1x.vlcskin.model.SkinNode;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;

/**
 * Reads attributes and remembers which ones were claimed. Whatever is left
 * becomes a preserved foreign attribute so a save cannot drop it.
 */
public final class Attributes {

    private final Element element;
    private final List<ParseIssue> issues;
    private final Set<String> consumed = new HashSet<>();

    public Attributes(Element element, List<ParseIssue> issues) {
        this.element = element;
        this.issues = issues;
    }

    public String str(String name, String defaultValue) {
        consumed.add(name);
        if (element.hasAttribute(name)) {
            return element.getAttribute(name);
        }
        return defaultValue;
    }

    /**
     * Reads an id attribute. The DTD defaults an absent id to "none", and VLC
     * treats that as no id at all, so both cases fall through to the generated
     * unique id the editor needs for its trees.
     */
    public String id(String generated) {
        consumed.add("id");
        if (element.hasAttribute("id")) {
            String value = element.getAttribute("id");
            if (!value.isEmpty() && !"none".equals(value)) {
                return value;
            }
        }
        return generated;
    }

    public String required(String name, String fallback) {
        String value = str(name, null);
        if (value == null || value.isEmpty()) {
            issues.add(ParseIssue.warning(
                    "Missing required attribute \"" + name + "\" on <" + element.getNodeName() + ">",
                    XmlSupport.path(element)));
            return fallback;
        }
        return value;
    }

    public int integer(String name, int defaultValue) {
        String raw = str(name, null);
        if (raw == null) {
            return defaultValue;
        }
        try {
            return Integer.parseInt(raw.trim());
        } catch (NumberFormatException ex) {
            issues.add(ParseIssue.warning(
                    "Attribute \"" + name + "\" is not an integer: \"" + raw + "\"",
                    XmlSupport.path(element)));
            return defaultValue;
        }
    }

    public boolean bool(String name, boolean defaultValue) {
        String raw = str(name, null);
        return raw == null ? defaultValue : Boolean.parseBoolean(raw.trim());
    }

    /**
     * Copies unclaimed attributes onto the node and returns it.
     */
    public <T extends SkinNode> T finish(T node) {
        NamedNodeMap attributes = element.getAttributes();
        for (int i = 0; i < attributes.getLength(); i++) {
            Node attribute = attributes.item(i);
            if (!consumed.contains(attribute.getNodeName())) {
                node.preserveAttribute(attribute.getNodeName(), attribute.getNodeValue());
            }
        }
        return node;
    }

    public Element element() {
        return element;
    }

    public List<ParseIssue> issues() {
        return issues;
    }
}
