package io.github.zoroaster1x.vlcskin.mcp;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Small JSON schema builder for MCP tool input schemas.
 */
public final class Schema {

    private final Map<String, Object> properties = new LinkedHashMap<>();
    private final List<String> required = new ArrayList<>();

    public static Schema object() {
        return new Schema();
    }

    public Schema string(String name, String description) {
        properties.put(name, Map.of("type", "string", "description", description));
        return this;
    }

    public Schema integer(String name, String description) {
        properties.put(name, Map.of("type", "integer", "description", description));
        return this;
    }

    public Schema number(String name, String description) {
        properties.put(name, Map.of("type", "number", "description", description));
        return this;
    }

    public Schema bool(String name, String description) {
        properties.put(name, Map.of("type", "boolean", "description", description));
        return this;
    }

    public Schema object(String name, String description, Map<String, Object> schema) {
        properties.put(name, Map.of("type", "object", "description", description, "properties", schema));
        return this;
    }

    public Schema array(String name, String itemType, String description) {
        properties.put(name, Map.of("type", "array", "description", description,
                "items", Map.of("type", itemType)));
        return this;
    }

    public Schema required(String... names) {
        required.addAll(List.of(names));
        return this;
    }

    public Map<String, Object> build() {
        Map<String, Object> schema = new LinkedHashMap<>();
        schema.put("type", "object");
        schema.put("properties", properties);
        if (!required.isEmpty()) {
            schema.put("required", List.copyOf(required));
        }
        schema.put("additionalProperties", false);
        return schema;
    }
}
