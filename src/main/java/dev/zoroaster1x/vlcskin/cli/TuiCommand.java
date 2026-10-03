package dev.zoroaster1x.vlcskin.cli;

import dev.zoroaster1x.vlcskin.edit.EditorSession;
import dev.zoroaster1x.vlcskin.mcp.EditorService;
import dev.zoroaster1x.vlcskin.tui.TuiLoop;
import java.nio.file.Path;
import java.util.concurrent.Callable;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;
import picocli.CommandLine.Parameters;

/**
 * Open the terminal UI, optionally with a skin.
 */
@Command(name = "tui", description = "Browse and edit a skin in the terminal.")
public final class TuiCommand implements Callable<Integer> {

    @Parameters(index = "0", paramLabel = "SKIN", arity = "0..1", description = "Optional skin to open.")
    Path skin;

    @Option(names = "--no-color", description = "Disable ANSI colors in the preview.")
    boolean noColor;

    @Override
    public Integer call() throws Exception {
        EditorService service = new EditorService();
        if (skin != null) {
            var outcome = service.open(skin.toAbsolutePath().toString());
            if (outcome.error()) {
                System.err.println(outcome.text());
                return 1;
            }
        }
        TuiLoop.run(service, !noColor);
        return 0;
    }
}
