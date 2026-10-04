package dev.zoroaster1x.vlcskin.util;

import java.time.Instant;

/**
 * How long the process needed to become usable. The clock starts when this
 * class is first touched, which is the earliest point any entry point can
 * share, and the ready line is printed once the window, the TUI prompt or the
 * MCP server is actually up.
 */
public final class Startup {

    /**
     * The JVM process start, so the ready line measures the whole launch. The
     * class-load time is only a fallback when the platform does not provide it.
     */
    private static final Instant PROCESS_START = ProcessHandle.current().info()
            .startInstant().orElse(null);
    private static final long CLASS_LOAD = System.nanoTime();

    private Startup() {
    }

    public static long elapsedMillis() {
        if (PROCESS_START != null) {
            return Math.max(0, java.time.Duration.between(PROCESS_START, Instant.now()).toMillis());
        }
        return (System.nanoTime() - CLASS_LOAD) / 1_000_000;
    }

    /**
     * Seconds with millisecond precision, for the ready line.
     */
    public static String elapsedText() {
        return String.format(java.util.Locale.ROOT, "%.3f s", elapsedMillis() / 1000.0);
    }
}
