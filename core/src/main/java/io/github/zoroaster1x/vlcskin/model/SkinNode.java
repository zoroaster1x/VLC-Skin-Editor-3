package io.github.zoroaster1x.vlcskin.model;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * Base for every model node. Attributes and child elements the editor does not
 * know about survive a load and save cycle instead of being silently dropped.
 */
public abstract class SkinNode {

    private final Map<String, String> foreignAttributes = new LinkedHashMap<>();
    private final List<String> unknownChildren = new LinkedList<>();

    /**
     * Records an attribute the schema does not claim, so it can be written back.
     */
    public void preserveAttribute(String name, String value) {
        foreignAttributes.put(name, value);
    }

    public Map<String, String> foreignAttributes() {
        return Collections.unmodifiableMap(foreignAttributes);
    }

    public boolean hasForeignAttributes() {
        return !foreignAttributes.isEmpty();
    }

    /**
     * Records an unknown child element as raw XML, so it survives a save.
     */
    public void preserveChild(String rawXml) {
        if (rawXml != null && !rawXml.isBlank()) {
            unknownChildren.add(rawXml);
        }
    }

    public List<String> unknownChildren() {
        return Collections.unmodifiableList(unknownChildren);
    }

    public boolean hasUnknownChildren() {
        return !unknownChildren.isEmpty();
    }

    /**
     * Appends preserved children, each on its own line.
     */
    protected void writeUnknownChildren(io.github.zoroaster1x.vlcskin.util.XmlWriter writer) {
        for (String child : unknownChildren) {
            writer.line(child);
        }
    }

    /**
     * Appends the preserved attributes in their original order.
     */
    protected void writeForeignAttributes(io.github.zoroaster1x.vlcskin.util.XmlWriter writer) {
        foreignAttributes.forEach(writer::attr);
    }
}
