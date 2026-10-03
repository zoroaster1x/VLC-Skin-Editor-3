package dev.zoroaster1x.vlcskin.util;

/**
 * Builds indented XML text. Small and predictable on purpose: the writer needs
 * full control over attribute order and default omission, which a generic XML
 * library does not give.
 */
public final class XmlWriter {

    private final StringBuilder out = new StringBuilder(4096);
    private final String indent;
    private int depth;
    private boolean attributesOpen;

    public XmlWriter(String indent) {
        this.indent = indent;
    }

    /**
     * Starts an element. Attributes must follow before any child or end call.
     */
    public XmlWriter start(String name) {
        if (attributesOpen) {
            out.append('>');
            attributesOpen = false;
        }
        out.append('\n').append(indent.repeat(depth)).append('<').append(name);
        depth++;
        attributesOpen = true;
        return this;
    }

    public XmlWriter attr(String name, String value) {
        if (value != null) {
            out.append(' ').append(name).append("=\"").append(XmlEscape.attribute(value)).append('"');
        }
        return this;
    }

    public XmlWriter attr(String name, int value) {
        return attr(name, Integer.toString(value));
    }

    public XmlWriter attr(String name, boolean value) {
        return attr(name, Boolean.toString(value));
    }

    public XmlWriter attrIf(String name, String value, String defaultValue) {
        return value == null || value.equals(defaultValue) ? this : attr(name, value);
    }

    public XmlWriter attrIf(String name, int value, int defaultValue) {
        return value == defaultValue ? this : attr(name, value);
    }

    public XmlWriter attrIf(String name, boolean value, boolean defaultValue) {
        return value == defaultValue ? this : attr(name, value);
    }

    /**
     * Writes one raw child line, for preserved unknown content.
     */
    public XmlWriter line(String text) {
        if (attributesOpen) {
            out.append('>');
            attributesOpen = false;
        }
        out.append('\n').append(indent.repeat(depth)).append(text);
        return this;
    }

    /**
     * Closes the current element; self closing when it wrote nothing.
     */
    public XmlWriter end(String name) {
        depth--;
        if (attributesOpen) {
            out.append("/>");
            attributesOpen = false;
        } else {
            out.append('\n').append(indent.repeat(depth)).append("</").append(name).append('>');
        }
        return this;
    }

    public String toXml() {
        String text = out.toString();
        return text.startsWith("\n") ? text.substring(1) : text;
    }

    @Override
    public String toString() {
        return toXml();
    }
}
