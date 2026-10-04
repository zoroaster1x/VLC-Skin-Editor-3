package dev.zoroaster1x.vlcskin.util;

import java.util.Locale;

/**
 * One place for the operating system checks that decide behaviour, so the
 * window chrome, the VLC lookup, the updater and the path utility all agree.
 */
public final class Platform {

    private Platform() {
    }

    public static String osName() {
        return System.getProperty("os.name", "").toLowerCase(Locale.ROOT);
    }

    public static boolean isWindows() {
        return osName().contains("win");
    }

    public static boolean isMac() {
        return osName().contains("mac");
    }

    public static boolean isLinux() {
        return osName().contains("linux");
    }
}
