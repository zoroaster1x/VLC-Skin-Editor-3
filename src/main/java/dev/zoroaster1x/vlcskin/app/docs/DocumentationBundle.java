package dev.zoroaster1x.vlcskin.app.docs;

import com.fasterxml.jackson.core.type.TypeReference;
import dev.zoroaster1x.vlcskin.util.Json;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * The bundled documentation: a table of contents plus markdown pages and
 * images under one resource folder. The folder is materialized to the user
 * cache directory so the viewer can reference images with file URLs, and every
 * page is searchable as text.
 */
public final class DocumentationBundle {

    private static final String ROOT = "/dev/zoroaster1x/vlcskin/app/docs/";

    public record Topic(String id, String section, String title, String file, String sourceUrl) {
    }

    public record Match(Topic topic, int line, String snippet) {
    }

    private final List<Topic> topics;
    private final String version;

    public DocumentationBundle() {
        this.topics = loadToc();
        this.version = loadVersion();
    }

    public List<Topic> topics() {
        return topics;
    }

    public boolean available() {
        return !topics.isEmpty();
    }

    public Topic topic(String id) {
        if (id == null) {
            return null;
        }
        Topic exact = topics.stream().filter(topic -> topic.id().equals(id)).findFirst().orElse(null);
        if (exact != null) {
            return exact;
        }
        // Links inside the handbook use plain page names; ids carry the area.
        Topic handbook = topics.stream().filter(topic -> topic.id().equals("handbook-" + id)).findFirst().orElse(null);
        if (handbook != null) {
            return handbook;
        }
        return topics.stream().filter(topic -> topic.id().endsWith("-" + id)).findFirst().orElse(null);
    }

    /**
     * The markdown of one page.
     */
    public String markdown(Topic topic) throws IOException {
        String path = ROOT + topic.file();
        try (InputStream in = DocumentationBundle.class.getResourceAsStream(path)) {
            if (in == null) {
                throw new IOException("Missing documentation page " + topic.file());
            }
            return new String(in.readAllBytes(), StandardCharsets.UTF_8);
        }
    }

    /**
     * Copies the whole bundle into the cache folder when the version changed and
     * returns the folder. Images and local links resolve against it.
     */
    public Path materialize() throws IOException {
        Path target = cacheFolder().resolve(version == null || version.isBlank() ? "current" : version);
        Path marker = target.resolve(".complete");
        if (Files.isRegularFile(marker)) {
            return target;
        }
        Files.createDirectories(target);
        List<String> files = new ArrayList<>();
        try (InputStream in = DocumentationBundle.class.getResourceAsStream(ROOT + "files.txt")) {
            if (in != null) {
                new String(in.readAllBytes(), StandardCharsets.UTF_8).lines()
                        .map(String::trim)
                        .filter(line -> !line.isEmpty())
                        .forEach(files::add);
            }
        }
        for (String file : files) {
            Path destination = target.resolve(file).normalize();
            if (!destination.startsWith(target)) {
                continue;
            }
            try (InputStream in = DocumentationBundle.class.getResourceAsStream(ROOT + file)) {
                if (in == null) {
                    continue;
                }
                Files.createDirectories(destination.getParent());
                Files.write(destination, in.readAllBytes());
            }
        }
        Files.writeString(marker, "ok");
        return target;
    }

    /**
     * Case-insensitive full text search over every page.
     */
    public List<Match> search(String query, int limit) {
        List<Match> matches = new ArrayList<>();
        if (query == null || query.isBlank()) {
            return matches;
        }
        String needle = query.toLowerCase(Locale.ROOT);
        for (Topic topic : topics) {
            String text;
            try {
                text = markdown(topic);
            } catch (IOException ex) {
                continue;
            }
            String[] lines = text.split("\n", -1);
            for (int i = 0; i < lines.length && matches.size() < limit; i++) {
                if (lines[i].toLowerCase(Locale.ROOT).contains(needle)) {
                    matches.add(new Match(topic, i + 1, lines[i].strip()));
                }
            }
            if (matches.size() >= limit) {
                break;
            }
        }
        return matches;
    }

    /**
     * Topics grouped by section, in first appearance order.
     */
    public Map<String, List<Topic>> bySection() {
        Map<String, List<Topic>> sections = new LinkedHashMap<>();
        for (Topic topic : topics) {
            sections.computeIfAbsent(topic.section(), key -> new ArrayList<>()).add(topic);
        }
        return sections;
    }

    private static List<Topic> loadToc() {
        try (InputStream in = DocumentationBundle.class.getResourceAsStream(ROOT + "toc.json")) {
            if (in == null) {
                return List.of();
            }
            return Json.mapper().readValue(in, new TypeReference<List<Topic>>() {
            });
        } catch (Exception ex) {
            return List.of();
        }
    }

    private static String loadVersion() {
        try (InputStream in = DocumentationBundle.class.getResourceAsStream(ROOT + "version.txt")) {
            return in == null ? "" : new String(in.readAllBytes(), StandardCharsets.UTF_8).strip();
        } catch (IOException ex) {
            return "";
        }
    }

    private static Path cacheFolder() {
        String cacheHome = System.getenv("XDG_CACHE_HOME");
        Path base = cacheHome != null && !cacheHome.isBlank()
                ? Path.of(cacheHome)
                : Path.of(System.getProperty("user.home"), ".cache");
        return base.resolve("vlc-skin-studio").resolve("docs");
    }
}
