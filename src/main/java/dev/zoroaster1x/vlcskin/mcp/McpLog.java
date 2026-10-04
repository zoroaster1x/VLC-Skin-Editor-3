package dev.zoroaster1x.vlcskin.mcp;

import dev.zoroaster1x.vlcskin.app.config.AppPaths;
import dev.zoroaster1x.vlcskin.util.Json;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * The MCP server's diary: a timestamped call log the desktop window can tail,
 * and a small status file it reads to say whether a server is running and what
 * it did last.
 *
 * <p>The server is usually a separate process, so an in-memory view would be
 * empty in the window. Both files live under the cache directory and are safe
 * to delete at any time.
 */
public final class McpLog {

    private static final DateTimeFormatter TIME =
            DateTimeFormatter.ofPattern("HH:mm:ss.SSS").withZone(ZoneId.systemDefault());

    private static volatile Path rootOverride;

    private McpLog() {
    }

    /**
     * Redirects the log and status files, for tests.
     */
    public static void useRoot(Path root) {
        rootOverride = root;
    }

    public static void resetRoot() {
        rootOverride = null;
    }

    private static Path root() {
        return rootOverride != null ? rootOverride : AppPaths.cacheDir();
    }

    public static Path logFile() {
        return root().resolve("mcp.log");
    }

    public static Path statusFile() {
        return root().resolve("mcp-status.json");
    }

    /**
     * Records the server start and resets the status counters.
     */
    public static void started(String version, String file) {
        line("server started, version " + version + (file == null ? "" : ", file " + file));
        writeStatus(Map.of(
                "pid", ProcessHandle.current().pid(),
                "version", version == null ? "" : version,
                "startedAt", System.currentTimeMillis(),
                "lastCallAt", 0,
                "calls", 0,
                "lastTool", "",
                "lastError", false,
                "file", file == null ? "" : file));
    }

    /**
     * One completed tool call, into both the log and the status.
     */
    public static void call(String tool, long millis, boolean error, String file, String notice) {
        line(String.format("%-22s %5d ms%s%s", tool, millis, error ? " ERROR" : "",
                notice == null ? "" : "  " + notice));
        Map<String, Object> status = new LinkedHashMap<>();
        Map<String, Object> previous = readStatus();
        status.put("pid", ProcessHandle.current().pid());
        status.put("version", previous.getOrDefault("version", ""));
        status.put("startedAt", previous.getOrDefault("startedAt", System.currentTimeMillis()));
        status.put("lastCallAt", System.currentTimeMillis());
        status.put("calls", ((Number) previous.getOrDefault("calls", 0)).longValue() + 1);
        status.put("lastTool", tool);
        status.put("lastError", error);
        status.put("file", file == null ? "" : file);
        writeStatus(status);
    }

    public static void stopped() {
        line("server stopped");
    }

    /**
     * Appends one timestamped line to the log.
     */
    public static void line(String message) {
        try {
            Path file = logFile();
            Files.createDirectories(file.getParent());
            Files.writeString(file,
                    TIME.format(Instant.now()) + "  " + message + System.lineSeparator(),
                    StandardCharsets.UTF_8, StandardOpenOption.CREATE, StandardOpenOption.APPEND);
        } catch (IOException | RuntimeException ex) {
            // Logging must never break the server.
        }
    }

    /**
     * The last lines of the log, oldest first, for the activity panel.
     */
    public static List<String> tail(int lines) {
        try {
            Path file = logFile();
            if (!Files.isRegularFile(file)) {
                return List.of();
            }
            List<String> all = Files.readAllLines(file, StandardCharsets.UTF_8);
            int from = Math.max(0, all.size() - lines);
            return new ArrayList<>(all.subList(from, all.size()));
        } catch (IOException | RuntimeException ex) {
            return List.of();
        }
    }

    /**
     * The status file as a map, or an empty map when no server has run.
     */
    public static Map<String, Object> readStatus() {
        try {
            Path file = statusFile();
            if (!Files.isRegularFile(file)) {
                return Map.of();
            }
            return Json.mapper().readValue(Files.readString(file), Map.class);
        } catch (IOException | RuntimeException ex) {
            return Map.of();
        }
    }

    /**
     * True when a server wrote a status recently. The pid is checked first, so
     * a stale file from a crashed server does not look active forever.
     */
    public static boolean isRunning() {
        Map<String, Object> status = readStatus();
        if (status.isEmpty()) {
            return false;
        }
        Object pid = status.get("pid");
        if (pid instanceof Number number) {
            boolean alive = ProcessHandle.of(number.longValue()).map(ProcessHandle::isAlive).orElse(false);
            if (alive) {
                return true;
            }
        }
        Object last = status.get("lastCallAt");
        long lastMillis = last instanceof Number number ? number.longValue() : 0;
        return false;
    }

    private static void writeStatus(Map<String, Object> status) {
        try {
            Path file = statusFile();
            Files.createDirectories(file.getParent());
            Path temp = Files.createTempFile(file.getParent(), "mcp-status", ".json");
            Files.writeString(temp, Json.write(status), StandardCharsets.UTF_8);
            Files.move(temp, file, java.nio.file.StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException | RuntimeException ex) {
            // Status reporting is best effort.
        }
    }
}
