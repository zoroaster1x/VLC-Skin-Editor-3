package dev.zoroaster1x.vlcskin.util;

import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

/**
 * The CLI/TUI log. Debug lines are off unless {@code --verbose} is given and
 * always go to stderr, so machine readable stdout stays untouched. Every line
 * carries a timestamp and the thread name, because the interesting bugs are
 * the ones that happen on another thread.
 */
public final class Log {

    private static final DateTimeFormatter TIME =
            DateTimeFormatter.ofPattern("HH:mm:ss.SSS").withZone(ZoneId.systemDefault());

    private static volatile boolean verbose;

    private Log() {
    }

    public static void setVerbose(boolean enabled) {
        verbose = enabled;
    }

    public static boolean isVerbose() {
        return verbose;
    }

    public static void debug(String message, Object... arguments) {
        if (!verbose) {
            return;
        }
        emit("DEBUG", message, arguments);
    }

    public static void info(String message, Object... arguments) {
        emit("INFO", message, arguments);
    }

    public static void warn(String message, Object... arguments) {
        emit("WARN", message, arguments);
    }

    public static void error(String message, Object... arguments) {
        emit("ERROR", message, arguments);
    }

    private static void emit(String level, String message, Object... arguments) {
        String text = arguments == null || arguments.length == 0
                ? message
                : String.format(message, arguments);
        System.err.println(TIME.format(Instant.now()) + " [" + level + "] ["
                + Thread.currentThread().getName() + "] " + text);
        AppLog.line(level, text);
    }
}
