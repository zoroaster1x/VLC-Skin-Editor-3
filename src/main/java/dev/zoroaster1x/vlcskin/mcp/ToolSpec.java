package dev.zoroaster1x.vlcskin.mcp;

import java.util.Map;
import java.util.function.Function;

/**
 * One MCP tool: name, description, JSON schema and handler.
 */
public record ToolSpec(String name, String title, String description, Map<String, Object> inputSchema,
                       Function<Map<String, Object>, ToolOutcome> handler) {

    public ToolOutcome call(Map<String, Object> arguments) {
        return handler.apply(arguments == null ? Map.of() : arguments);
    }
}
