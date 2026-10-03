package io.github.zoroaster1x.vlcskin.mcp;

import io.github.zoroaster1x.vlcskin.util.Json;

/**
 * What a tool call returns: text for the model, optionally a PNG, and
 * optionally structured JSON for a client that prefers data over prose.
 */
public record ToolOutcome(String text, byte[] png, Object structured, boolean error) {

    public static ToolOutcome text(String text) {
        return new ToolOutcome(text, null, null, false);
    }

    public static ToolOutcome text(Object structured) {
        return new ToolOutcome(Json.write(structured), null, structured, false);
    }

    public static ToolOutcome text(String text, Object structured) {
        return new ToolOutcome(text, null, structured, false);
    }

    public static ToolOutcome image(byte[] png, String text, Object structured) {
        return new ToolOutcome(text, png, structured, false);
    }

    public static ToolOutcome error(String message) {
        return new ToolOutcome("Error: " + message, null, null, true);
    }

    public boolean hasPng() {
        return png != null && png.length > 0;
    }
}
