package dev.zoroaster1x.vlcskin.util;

import static org.assertj.core.api.Assertions.assertThat;

import dev.zoroaster1x.vlcskin.app.config.AppPaths;
import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

/**
 * The startup and closing lines print once, no matter how many times a path
 * calls them. This matters because the GUI close path and the shutdown hook
 * both run, and a TUI close can be followed by a normal process exit.
 */
class BannerTest {

    @TempDir
    Path root;

    @BeforeEach
    void redirect() {
        AppPaths.useRoots(root.resolve("config"), root.resolve("cache"), root.resolve("data"));
        AppLog.resetForTests();
        Banner.resetForTests();
    }

    @AfterEach
    void restore() {
        Banner.resetForTests();
        AppLog.resetForTests();
        AppPaths.resetRoots();
    }

    @Test
    void startupAndGoodbyeEachPrintOnce() {
        ByteArrayOutputStream buffer = new ByteArrayOutputStream();
        PrintStream stream = new PrintStream(buffer, true, StandardCharsets.UTF_8);

        Banner.startup(stream);
        Banner.startup(stream);
        Banner.goodbye();
        Banner.goodbye();

        String text = buffer.toString(StandardCharsets.UTF_8);
        assertThat(count(text, "Biji Kurdistan")).isEqualTo(1);
        assertThat(count(text, "Ready in ")).isEqualTo(1);
        assertThat(count(text, "Made with love in Kurdistan")).isEqualTo(1);
        assertThat(count(text, "Good bye!")).isEqualTo(1);
    }

    private static int count(String text, String needle) {
        int total = 0;
        int index = 0;
        while ((index = text.indexOf(needle, index)) >= 0) {
            total++;
            index += needle.length();
        }
        return total;
    }
}
