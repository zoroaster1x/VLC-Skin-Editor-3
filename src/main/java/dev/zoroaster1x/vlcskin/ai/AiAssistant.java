package dev.zoroaster1x.vlcskin.ai;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import dev.zoroaster1x.vlcskin.mcp.EditorService;
import dev.zoroaster1x.vlcskin.mcp.McpToolset;
import dev.zoroaster1x.vlcskin.mcp.ToolOutcome;
import dev.zoroaster1x.vlcskin.mcp.ToolSpec;
import dev.zoroaster1x.vlcskin.util.Json;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.Map;

/**
 * A small OpenAI compatible chat client that can call the same tools the MCP
 * server exposes. When the configured endpoint returns an image tool result
 * and the model accepts images, the PNG is attached to the conversation.
 */
public final class AiAssistant {

    /**
     * Endpoint settings; the key is never written to disk by this class.
     */
    public record Config(String baseUrl, String apiKey, String model, boolean attachImages) {

        public boolean usable() {
            return baseUrl != null && !baseUrl.isBlank() && model != null && !model.isBlank();
        }
    }

    private final EditorService service;
    private final Config config;
    private final ObjectNode root;
    private final ArrayNode messages;
    private final McpToolset toolset;

    public AiAssistant(EditorService service, Config config) {
        this.service = service;
        this.config = config;
        this.toolset = new McpToolset(service);
        this.root = Json.mapper().createObjectNode();
        this.messages = root.putArray("messages");
        messages.addObject().put("role", "system").put("content",
                "You are the assistant inside VLC Skin Studio, a VLC skins2 theme editor. "
                        + "Use the tools to inspect and change the open skin. Prefer layout_tree over guessing; "
                        + "use render_layout when a visual check helps. Keep answers short and concrete.");
    }

    public List<String> transcript() {
        List<String> lines = new ArrayList<>();
        for (JsonNode message : messages) {
            String role = message.path("role").asText();
            if ("tool".equals(role) || "assistant".equals(role) && message.has("tool_calls")) {
                continue;
            }
            lines.add(role + ": " + message.path("content").asText());
        }
        return lines;
    }

    /**
     * Sends a user message and runs the tool loop until the model finishes.
     */
    public String send(String userMessage) throws Exception {
        ObjectNode user = messages.addObject();
        user.put("role", "user").put("content", userMessage);
        for (int round = 0; round < 8; round++) {
            JsonNode response = chat();
            JsonNode choice = response.path("choices").path(0).path("message");
            if (choice.isMissingNode()) {
                return "The endpoint returned no choices.";
            }
            messages.add(choice.deepCopy());
            JsonNode toolCalls = choice.path("tool_calls");
            if (toolCalls.isArray() && !toolCalls.isEmpty()) {
                for (JsonNode call : toolCalls) {
                    String name = call.path("function").path("name").asText();
                    String arguments = call.path("function").path("arguments").asText("{}");
                    @SuppressWarnings("unchecked")
                    Map<String, Object> args = Json.read(arguments, Map.class);
                    ToolOutcome outcome = call(name, args);
                    messages.addObject().put("role", "tool").put("tool_call_id", call.path("id").asText())
                            .put("content", outcome.text() == null ? "" : outcome.text());
                    if (outcome.hasPng() && config.attachImages()) {
                        String data = Base64.getEncoder().encodeToString(outcome.png());
                        ObjectNode imageMessage = messages.addObject();
                        imageMessage.put("role", "user");
                        ArrayNode content = imageMessage.putArray("content");
                        content.addObject().put("type", "text")
                                .put("text", "Screenshot after calling " + name + ":");
                        content.addObject().put("type", "image_url").putObject("image_url")
                                .put("url", "data:image/png;base64," + data);
                    }
                }
                continue;
            }
            JsonNode content = choice.path("content");
            return content.isMissingNode() || content.isNull() ? "(no answer)" : content.asText();
        }
        return "The tool loop ran too long; stopping.";
    }

    private ToolOutcome call(String name, Map<String, Object> arguments) {
        for (ToolSpec spec : toolset.tools()) {
            if (spec.name().equals(name)) {
                return spec.call(arguments);
            }
        }
        return ToolOutcome.error("Unknown tool " + name);
    }

    private JsonNode chat() throws Exception {
        ObjectNode body = Json.mapper().createObjectNode();
        body.put("model", config.model());
        body.set("messages", messages);
        ArrayNode tools = body.putArray("tools");
        for (ToolSpec spec : toolset.tools()) {
            ObjectNode function = tools.addObject().put("type", "function").putObject("function");
            function.put("name", spec.name());
            function.put("description", spec.description());
            function.set("parameters", Json.mapper().valueToTree(spec.inputSchema()));
        }
        body.put("tool_choice", "auto");
        HttpClient client = HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(20)).build();
        HttpRequest.Builder request = HttpRequest.newBuilder()
                .uri(URI.create(normalize(config.baseUrl())))
                .timeout(Duration.ofMinutes(3))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(Json.write(body)));
        if (config.apiKey() != null && !config.apiKey().isBlank()) {
            request.header("Authorization", "Bearer " + config.apiKey());
        }
        HttpResponse<String> response = client.send(request.build(), HttpResponse.BodyHandlers.ofString());
        if (response.statusCode() >= 400) {
            throw new IllegalStateException("The AI endpoint returned " + response.statusCode() + ": "
                    + response.body());
        }
        return Json.mapper().readTree(response.body());
    }

    private static String normalize(String baseUrl) {
        String url = baseUrl.trim();
        if (url.endsWith("/chat/completions")) {
            return url;
        }
        return url.endsWith("/") ? url + "chat/completions" : url + "/chat/completions";
    }
}
