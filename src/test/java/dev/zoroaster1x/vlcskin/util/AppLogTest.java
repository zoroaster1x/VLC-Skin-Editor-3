package dev.zoroaster1x.vlcskin.util;

import static org.assertj.core.api.Assertions.assertThat;

import dev.zoroaster1x.vlcskin.app.config.AppPaths;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.zip.ZipFile;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

/**
 * The daily text log and the live session archive. Every application writes
 * the same daily file, and each start opens a zip with every log this user
 * has; the zip is refreshed while the session runs.
 */
class AppLogTest {

    @TempDir
    Path root;

    @BeforeEach
    void redirect() {
        AppPaths.useRoots(root.resolve("config"), root.resolve("cache"), root.resolve("data"));
        AppLog.resetForTests();
    }

    @AfterEach
    void restore() {
        AppLog.resetForTests();
        AppPaths.resetRoots();
    }

    @Test
    void linesLandInTheDailyFile() {
        AppLog.line("TEST", "hello log");
        assertThat(AppLog.dailyFile()).exists();
        assertThat(read(AppLog.dailyFile()))
                .contains("[TEST]")
                .contains("hello log")
                .contains(Thread.currentThread().getName());
    }

    @Test
    void startOpensAnArchiveWithEveryLogAndFlushesOnClose() throws Exception {
        AppLog.line("TEST", "before start");
        AppLog.start("test-app", "9.9");
        AppLog.line("TEST", "while running");
        Path archive = AppLog.sessionArchive();
        assertThat(archive).isNotNull().exists();
        AppLog.flush();
        AppLog.close();

        assertThat(archive.getFileName().toString())
                .as("named with date, time and timezone offset")
                .matches("\\d{4}-\\d{2}-\\d{2}-\\d{6}[+-]\\d{4}\\.zip");
        try (ZipFile zip = new ZipFile(archive.toFile())) {
            assertThat(zip.getEntry(AppLog.dailyFile().getFileName().toString())).isNotNull();
            String content = new String(zip.getInputStream(
                    zip.getEntry(AppLog.dailyFile().getFileName().toString())).readAllBytes());
            assertThat(content).contains("before start").contains("while running");
            assertThat(content).contains("log session closed");
        }
    }

    @Test
    void theCacheMcpLogIsArchivedToo() throws Exception {
        Files.createDirectories(AppPaths.cacheDir());
        Files.writeString(AppPaths.cacheDir().resolve("mcp.log"), "mcp says hi\n");
        AppLog.start("test-app", "9.9");
        Path archive = AppLog.sessionArchive();
        AppLog.close();
        try (ZipFile zip = new ZipFile(archive.toFile())) {
            assertThat(zip.getEntry("mcp.log")).isNotNull();
            assertThat(new String(zip.getInputStream(zip.getEntry("mcp.log")).readAllBytes()))
                    .contains("mcp says hi");
        }
    }

    private static String read(Path file) {
        try {
            return Files.readString(file);
        } catch (Exception ex) {
            throw new AssertionError(ex);
        }
    }
}
