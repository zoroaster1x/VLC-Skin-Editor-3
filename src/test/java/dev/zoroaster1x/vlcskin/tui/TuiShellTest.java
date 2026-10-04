package dev.zoroaster1x.vlcskin.tui;

import static org.assertj.core.api.Assertions.assertThat;

import dev.zoroaster1x.vlcskin.example.ExampleSkins;
import dev.zoroaster1x.vlcskin.mcp.EditorService;
import java.nio.file.Path;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

/**
 * The terminal UI runs without an event thread or a window; every command is a
 * string in and a string out, which is what makes it testable.
 */
class TuiShellTest {

    @Test
    void bannerNamesTheDocumentAndCommandsAnswer(@TempDir Path folder) throws Exception {
        Path theme = ExampleSkins.create(folder, ExampleSkins.NEON);
        EditorService service = new EditorService();
        assertThat(service.open(theme.toString()).error()).isFalse();
        TuiShell shell = new TuiShell(service, false);

        assertThat(shell.banner()).contains("theme.xml");
        assertThat(shell.execute("help")).contains("render").contains("validate");
        assertThat(shell.execute("info")).contains("Neon player").contains("windows:").contains("layouts:");
        assertThat(shell.execute("items")).isNotBlank();
        assertThat(shell.execute("tree")).contains("play_btn");
        assertThat(shell.execute("validate")).isNotNull();
        assertThat(shell.execute("show play_btn")).contains("Button");
        assertThat(shell.execute("nonsense")).contains("Unknown");
    }

    @Test
    void renderProducesTerminalArt(@TempDir Path folder) throws Exception {
        Path theme = ExampleSkins.create(folder, ExampleSkins.PANEL);
        EditorService service = new EditorService();
        assertThat(service.open(theme.toString()).error()).isFalse();
        TuiShell shell = new TuiShell(service, false);

        String art = shell.execute("render 1");
        assertThat(art).isNotBlank();
        assertThat(art.lines().count()).isGreaterThan(5);
    }
}
