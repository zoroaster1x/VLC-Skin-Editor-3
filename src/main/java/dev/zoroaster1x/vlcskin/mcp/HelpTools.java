package dev.zoroaster1x.vlcskin.mcp;

import dev.zoroaster1x.vlcskin.Version;
import dev.zoroaster1x.vlcskin.app.docs.DocumentationBundle;
import dev.zoroaster1x.vlcskin.app.docs.DocumentationSearch;
import dev.zoroaster1x.vlcskin.util.Json;
import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Everything the Help menu shows, as MCP tools: the version and links, the
 * bundled documentation topics, a ranked search over every page and the full
 * markdown of one topic. The skin format reference and the MCP guide ship in
 * the bundle, so a model can read the same API reference the viewer shows.
 */
public final class HelpTools {

    private final DocumentationBundle bundle = new DocumentationBundle();
    private DocumentationSearch search;

    /**
     * The About box and the Help links in one call, plus the topic ids a model
     * should read first.
     */
    public ToolOutcome appInfo() {
        Map<String, Object> info = new LinkedHashMap<>();
        info.put("name", Version.NAME);
        info.put("version", Version.VERSION);
        info.put("project", "https://github.com/zoroaster1x/VLC-Skin-Editor-3");
        info.put("releases", "https://github.com/zoroaster1x/VLC-Skin-Editor-3/releases");
        info.put("onlineHelp", "https://www.videolan.org/vlc/skinedhlp/");
        info.put("documentationTopics", bundle.topics().size());
        putTopic(info, "mcpGuide", "handbook-mcp-for-automation");
        putTopic(info, "mcpToolReference", "handbook-mcp-and-ai");
        putTopic(info, "formatReference", "handbook-format-reference");
        return ToolOutcome.text(Json.write(info), info);
    }

    /**
     * Every bundled topic grouped by section: the handbook, the format
     * reference and the archived original help.
     */
    public ToolOutcome list() {
        if (!bundle.available()) {
            return ToolOutcome.error("The documentation bundle is empty");
        }
        Map<String, List<Map<String, Object>>> grouped = new LinkedHashMap<>();
        for (DocumentationBundle.Topic topic : bundle.topics()) {
            Map<String, Object> entry = new LinkedHashMap<>();
            entry.put("id", topic.id());
            entry.put("title", topic.title());
            entry.put("sourceUrl", topic.sourceUrl());
            grouped.computeIfAbsent(topic.section(), key -> new ArrayList<>()).add(entry);
        }
        List<Map<String, Object>> sections = new ArrayList<>();
        grouped.forEach((section, topics) -> {
            Map<String, Object> group = new LinkedHashMap<>();
            group.put("section", section);
            group.put("topics", topics);
            sections.add(group);
        });
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("count", bundle.topics().size());
        result.put("sections", sections);
        return ToolOutcome.text(Json.write(result), result);
    }

    /**
     * Ranked search across the whole bundle; each hit names its topic and the
     * heading path inside the page.
     */
    public ToolOutcome search(String query, Integer limit) {
        if (query == null || query.isBlank()) {
            return ToolOutcome.error("A search query is required");
        }
        int max = limit == null || limit <= 0 ? 20 : Math.min(limit, 100);
        List<Map<String, Object>> hits = new ArrayList<>();
        for (DocumentationSearch.Hit hit : searcher().search(query, max)) {
            Map<String, Object> entry = new LinkedHashMap<>();
            entry.put("topic", hit.topic().id());
            entry.put("title", hit.topic().title());
            entry.put("section", hit.topic().section());
            entry.put("line", hit.line());
            entry.put("path", hit.path().stream().map(DocumentationSearch.Heading::text).toList());
            entry.put("snippet", hit.snippet());
            entry.put("score", hit.score());
            entry.put("occurrences", hit.occurrences());
            hits.add(entry);
        }
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("query", query);
        result.put("count", hits.size());
        result.put("hits", hits);
        return ToolOutcome.text(Json.write(result), result);
    }

    /**
     * The full markdown of one topic by id. The format reference and the MCP
     * guide are the two that describe the whole API surface.
     */
    public ToolOutcome read(String topicId, Integer maxChars) {
        DocumentationBundle.Topic topic = bundle.topic(topicId);
        if (topic == null) {
            return ToolOutcome.error("No documentation topic \"" + topicId
                    + "\"; call list_documentation for the ids");
        }
        try {
            String markdown = stripFrontMatter(bundle.markdown(topic));
            boolean truncated = maxChars != null && maxChars > 0 && markdown.length() > maxChars;
            if (truncated) {
                markdown = markdown.substring(0, maxChars) + "\n\n[Truncated at " + maxChars + " characters]";
            }
            Map<String, Object> result = new LinkedHashMap<>();
            result.put("id", topic.id());
            result.put("title", topic.title());
            result.put("section", topic.section());
            result.put("sourceUrl", topic.sourceUrl());
            result.put("truncated", truncated);
            result.put("markdown", markdown);
            return ToolOutcome.text(markdown, result);
        } catch (IOException ex) {
            return ToolOutcome.error("Could not read the topic: " + ex.getMessage());
        }
    }

    private void putTopic(Map<String, Object> info, String key, String id) {
        DocumentationBundle.Topic topic = bundle.topic(id);
        if (topic != null) {
            info.put(key, topic.id());
        }
    }

    private synchronized DocumentationSearch searcher() {
        if (search == null) {
            search = new DocumentationSearch(bundle);
        }
        return search;
    }

    /**
     * The viewer hides the YAML header; the model gets the body text.
     */
    private static String stripFrontMatter(String markdown) {
        if (markdown.startsWith("---")) {
            int end = markdown.indexOf("\n---", 3);
            if (end >= 0) {
                int body = markdown.indexOf('\n', end + 1);
                if (body >= 0) {
                    return markdown.substring(body + 1);
                }
            }
        }
        return markdown;
    }
}
