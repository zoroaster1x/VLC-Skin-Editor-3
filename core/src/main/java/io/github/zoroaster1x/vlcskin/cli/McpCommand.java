package io.github.zoroaster1x.vlcskin.cli;

import io.github.zoroaster1x.vlcskin.Version;
import io.github.zoroaster1x.vlcskin.edit.EditorSession;
import io.github.zoroaster1x.vlcskin.mcp.EditorService;
import io.github.zoroaster1x.vlcskin.mcp.McpServerRunner;
import java.nio.file.Path;
import java.util.concurrent.Callable;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;

/**
 * Serve the editor over MCP stdio, the way OpenCode and Claude expect.
 */
@Command(name = "mcp", description = "Run the MCP server over stdio.")
public final class McpCommand implements Callable<Integer> {

    @Option(names = {"-f", "--file"}, paramLabel = "SKIN", description = "Open this skin before serving.")
    Path skin;

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
        McpServerRunner.serveStdio(service, Version.VERSION);
        return 0;
    }
}
