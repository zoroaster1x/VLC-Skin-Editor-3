package dev.zoroaster1x.vlcskin.util;

/**
 * Escapes and validates text for XML attributes and element content.
 */
public final class XmlEscape {

    private XmlEscape() {
    }

    public static String attribute(String value) {
        if (value == null) {
            return "";
        }
        StringBuilder out = new StringBuilder(value.length() + 8);
        for (int i = 0; i < value.length(); i++) {
            char c = value.charAt(i);
            switch (c) {
                case '&' -> out.append("&amp;");
                case '<' -> out.append("&lt;");
                case '>' -> out.append("&gt;");
                case '"' -> out.append("&quot;");
                case '\'' -> out.append("&apos;");
                case '\n' -> out.append("&#10;");
                case '\r' -> out.append("&#13;");
                case '\t' -> out.append("&#9;");
                default -> {
                    if (c < 0x20 && c != '\n' && c != '\r' && c != '\t') {
                        out.append("&#").append((int) c).append(';');
                    } else {
                        out.append(c);
                    }
                }
            }
        }
        return out.toString();
    }

    public static String text(String value) {
        if (value == null) {
            return "";
        }
        StringBuilder out = new StringBuilder(value.length() + 8);
        for (int i = 0; i < value.length(); i++) {
            char c = value.charAt(i);
            switch (c) {
                case '&' -> out.append("&amp;");
                case '<' -> out.append("&lt;");
                case '>' -> out.append("&gt;");
                default -> out.append(c);
            }
        }
        return out.toString();
    }

    /**
     * True when the id is safe to emit as an XML attribute value without entity noise.
     */
    public static boolean isPlainId(String id) {
        if (id == null || id.isEmpty()) {
            return false;
        }
        for (int i = 0; i < id.length(); i++) {
            char c = id.charAt(i);
            if (c == '<' || c == '>' || c == '&' || c == '"' || c == '\'') {
                return false;
            }
        }
        return true;
    }
}
