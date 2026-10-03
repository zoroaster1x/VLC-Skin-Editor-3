package dev.zoroaster1x.vlcskin.model.resource;

import dev.zoroaster1x.vlcskin.model.SkinNode;
import java.util.List;
import java.util.Map;

/**
 * A top level entry of a theme.
 */
public sealed interface Resource permits AbstractResource {

    String getId();

    void setId(String id);

    /**
     * The XML element name.
     */
    String elementName();

    /**
     * A human readable type name for the tree and the inspector.
     */
    String typeName();

    /**
     * Attributes this editor does not know, preserved for the next save.
     */
    Map<String, String> foreignAttributes();

    /**
     * Unknown child elements kept as raw XML.
     */
    List<String> unknownChildren();
}
