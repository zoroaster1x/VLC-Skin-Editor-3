package io.github.zoroaster1x.vlcskin.cli;

import io.github.zoroaster1x.vlcskin.edit.EditorSession;
import io.github.zoroaster1x.vlcskin.mcp.EditorService;
import io.github.zoroaster1x.vlcskin.util.Json;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.concurrent.Callable;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;
import picocli.CommandLine.Parameters;

/**
 * Render a layout to a PNG and optionally write the geometry JSON.
 */
@Command(name = "render", description = "Render a layout to PNG and print or write its geometry.")
public final class RenderCommand implements Callable<Integer> {

    @Parameters(index = "0", paramLabel = "SKIN", description = "The skin XML file.")
    Path skin;

    @Option(names = {"-w", "--window"}, description = "Window id, defaults to the first.")
    String window;

    @Option(names = {"-l", "--layout"}, description = "Layout id, defaults to the first.")
    String layout;

    @Option(names = {"-z", "--zoom"}, description = "Zoom 1 to 16.", defaultValue = "1")
    int zoom;

    @Option(names = {"-o", "--out"}, description = "PNG output path.", defaultValue = "preview.png")
    Path out;

    @Option(names = "--json", description = "Write the layout description to this JSON file.")
    Path json;

    @Option(names = "--quiet", description = "Do not print the description.")
    boolean quiet;

    @Override
    public Integer call() throws Exception {
        EditorService service = new EditorService(EditorSession.open(skin));
        var outcome = service.renderLayout(window, layout, Math.max(1, Math.min(16, zoom)), null);
        if (outcome.error()) {
            System.err.println(outcome.text());
            return 1;
        }
        Files.write(out, outcome.png());
        if (json != null) {
            Files.writeString(json, Json.write(outcome.structured()));
        }
        if (!quiet) {
            System.out.println(outcome.text());
            System.out.println(Json.write(outcome.structured()));
        }
        System.out.println("Wrote " + out);
        return 0;
    }
}
