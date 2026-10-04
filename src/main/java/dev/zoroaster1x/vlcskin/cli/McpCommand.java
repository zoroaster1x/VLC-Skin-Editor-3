package dev.zoroaster1x.vlcskin.cli;

import dev.zoroaster1x.vlcskin.Version;
import dev.zoroaster1x.vlcskin.edit.EditorSession;
import dev.zoroaster1x.vlcskin.mcp.EditorService;
import dev.zoroaster1x.vlcskin.mcp.McpServerRunner;
import java.nio.file.Path;
import java.util.concurrent.Callable;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;

/**
 * Serve the editor over MCP stdio, the way OpenCode and Claude expect.
 */
@Command(name = "mcp", description = "Run the MCP server over stdio.")
public final class McpCommand implements Callable<Integer> {

    /**
     * Installed by the desktop entry point so a standalone MCP process can read
     * and write the same preferences file the window uses. Null means no host.
     */
    public static final java.util.concurrent.atomic.AtomicReference<java.util.function.Supplier<
            dev.zoroaster1x.vlcskin.snapshot.UiInspector>> HOST =
            new java.util.concurrent.atomic.AtomicReference<>();

    @Option(names = {"-f", "--file"}, paramLabel = "SKIN", description = "Open this skin before serving.")
    Path skin;

    @Option(names = {"--force"}, description = "Serve even when the MCP server is disabled in preferences.")
    boolean force;

    @Override
    public Integer call() throws Exception {
        var store = new dev.zoroaster1x.vlcskin.app.config.SettingsStore();
        var settings = store.load();
        if (!force && !settings.isMcpEnabled()) {
            System.err.println("The MCP server is disabled in Preferences (AI and MCP, Enable the MCP server).");
            System.err.println("Run with --force to serve anyway.");
            return 3;
        }
        EditorService service = new EditorService();
        var supplier = HOST.get();
        if (supplier != null) {
            var host = supplier.get();
            if (host != null) {
                service.setUi(host);
            }
        }
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
