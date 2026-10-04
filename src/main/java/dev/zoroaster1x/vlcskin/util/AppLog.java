package dev.zoroaster1x.vlcskin.util;

import dev.zoroaster1x.vlcskin.app.config.AppPaths;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.nio.file.StandardOpenOption;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.zip.Deflater;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

/**
 * The long lived log: one shared text file per day plus a session archive.
 *
 * <p>Everything the app logs (CLI, TUI, GUI, MCP) is appended to
 * {@code <config>/logs/YYYY-MM-DD.log}, so all sessions of that user end up in
 * one place and can be read while they run. Each application start also opens a
 * zip named with the start time and the timezone offset,
 * {@code YYYY-MM-DD-HHMMSS+ZZZZ.zip}, which carries every log file known at
 * that moment; a background virtual thread refreshes it every 30 seconds when
 * new lines arrived, so the archive is live without recompressing on every
 * line. The archive is written again on close.
 */
public final class AppLog {

    private static final DateTimeFormatter TIME =
            DateTimeFormatter.ofPattern("HH:mm:ss.SSS").withZone(ZoneId.systemDefault());
    private static final DateTimeFormatter STAMP =
            DateTimeFormatter.ofPattern("yyyy-MM-dd-HHmmssZ").withZone(ZoneId.systemDefault());
    private static final long FLUSH_MILLIS = 30_000;

    private static final Object LOCK = new Object();
    private static boolean started;
    private static Path sessionArchive;
    private static long archivedBytes = -1;
    private static Thread flusher;
    private static boolean hookInstalled;

    private AppLog() {
    }

    /**
     * Opens the session archive and starts the live refresh. Safe to call more
     * than once; only the first call does the work.
     */
    public static void start(String application, String version) {
        synchronized (LOCK) {
            if (started) {
                return;
            }
            started = true;
            try {
                Path folder = AppPaths.logsDir();
                Files.createDirectories(folder);
                line("SYSTEM", application + " " + version + " started");
                sessionArchive = folder.resolve(STAMP.format(Instant.now()) + ".zip");
                rewriteArchive();
                flusher = Thread.ofVirtual()
                        .name("vlc-skin-log-flusher")
                        .unstarted(() -> {
                            while (true) {
                                try {
                                    Thread.sleep(FLUSH_MILLIS);
                                } catch (InterruptedException ex) {
                                    Thread.currentThread().interrupt();
                                    return;
                                }
                                synchronized (LOCK) {
                                    if (!started) {
                                        return;
                                    }
                                }
                                flush();
                            }
                        });
                flusher.start();
                if (!hookInstalled) {
                    hookInstalled = true;
                    Runtime.getRuntime().addShutdownHook(
                            new Thread(AppLog::close, "vlc-skin-log-close"));
                }
            } catch (IOException | RuntimeException ex) {
                // Logging must never stop the application.
            }
        }
    }

    /**
     * Appends one timestamped line to the day file. Also used before
     * {@link #start} so even short CLI runs leave a trace.
     */
    public static void line(String category, String message) {
        String text = TIME.format(Instant.now()) + " [" + category + "] ["
                + Thread.currentThread().getName() + "] " + message;
        try {
            Path file = dailyFile();
            Files.createDirectories(file.getParent());
            Files.writeString(file, text + System.lineSeparator(), StandardCharsets.UTF_8,
                    StandardOpenOption.CREATE, StandardOpenOption.APPEND);
        } catch (IOException | RuntimeException ex) {
            // Logging must never throw.
        }
    }

    /**
     * Writes the archive again when any log file grew. Cheap when idle: the
     * total byte count is compared first, so a quiet session does no work.
     */
    public static void flush() {
        synchronized (LOCK) {
            if (sessionArchive == null) {
                return;
            }
            try {
                if (totalLogBytes() != archivedBytes) {
                    rewriteArchive();
                }
            } catch (IOException | RuntimeException ex) {
                // Keep the previous archive rather than failing.
            }
        }
    }

    /**
     * Flushes and stops the live refresh. Called from a shutdown hook as well.
     */
    public static void close() {
        Thread toStop;
        synchronized (LOCK) {
            if (!started) {
                return;
            }
            started = false;
            toStop = flusher;
            flusher = null;
            try {
                line("SYSTEM", "log session closed");
                if (sessionArchive != null) {
                    rewriteArchive();
                }
            } catch (IOException | RuntimeException ex) {
                // Nothing useful to do while closing.
            }
        }
        if (toStop != null) {
            toStop.interrupt();
        }
    }

    /**
     * The archive of this session, or null when none was opened.
     */
    public static Path sessionArchive() {
        synchronized (LOCK) {
            return sessionArchive;
        }
    }

    public static Path dailyFile() {
        return AppPaths.logsDir().resolve(LocalDate.now(ZoneId.systemDefault()) + ".log");
    }

    /**
     * Forgets all session state, for tests.
     */
    public static void resetForTests() {
        synchronized (LOCK) {
            started = false;
            sessionArchive = null;
            archivedBytes = -1;
            flusher = null;
        }
    }

    private static long totalLogBytes() throws IOException {
        long total = 0;
        for (Path file : logFiles()) {
            total += Files.size(file);
        }
        return total;
    }

    /**
     * Every log this user has: the daily text files plus the MCP activity log.
     */
    private static List<Path> logFiles() throws IOException {
        List<Path> files = new ArrayList<>();
        Path folder = AppPaths.logsDir();
        if (Files.isDirectory(folder)) {
            try (var stream = Files.list(folder)) {
                stream.filter(path -> path.getFileName().toString().endsWith(".log"))
                        .sorted(Comparator.comparing(Path::getFileName))
                        .forEach(files::add);
            }
        }
        Path mcp = AppPaths.cacheDir().resolve("mcp.log");
        if (Files.isRegularFile(mcp)) {
            files.add(mcp);
        }
        return files;
    }

    private static void rewriteArchive() throws IOException {
        List<Path> files = logFiles();
        long total = 0;
        for (Path file : files) {
            total += Files.size(file);
        }
        if (files.isEmpty()) {
            archivedBytes = -1;
            return;
        }
        Path temp = sessionArchive.resolveSibling(sessionArchive.getFileName() + ".tmp");
        try (ZipOutputStream zip = new ZipOutputStream(Files.newOutputStream(temp))) {
            zip.setLevel(Deflater.BEST_SPEED);
            for (Path file : files) {
                zip.putNextEntry(new ZipEntry(file.getFileName().toString()));
                Files.copy(file, zip);
                zip.closeEntry();
            }
        }
        Files.move(temp, sessionArchive, StandardCopyOption.REPLACE_EXISTING,
                StandardCopyOption.ATOMIC_MOVE);
        archivedBytes = total;
    }
}
