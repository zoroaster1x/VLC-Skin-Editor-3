package dev.zoroaster1x.vlcskin.mcp;

import dev.zoroaster1x.vlcskin.util.Json;
import io.modelcontextprotocol.json.jackson2.JacksonMcpJsonMapper;
import io.modelcontextprotocol.server.McpServer;
import io.modelcontextprotocol.server.McpServerFeatures;
import io.modelcontextprotocol.server.McpSyncServer;
import io.modelcontextprotocol.server.transport.StdioServerTransportProvider;
import io.modelcontextprotocol.spec.McpSchema;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Base64;
import java.util.concurrent.CountDownLatch;

/**
 * Hosts the toolset over MCP stdio for OpenCode, Claude or any MCP client.
 */
public final class McpServerRunner {

    public static final String SERVER_NAME = "vlc-skin-studio";
    private static final String INSTRUCTIONS =
            "Edit VLC skins2 themes. render_layout returns a PNG plus geometry; layout_tree is enough "
                    + "when no image is needed. describe_editor_ui and screenshot_editor show the desktop "
                    + "window when it is running. "
                    + "The user shares the skin file and may edit or save it in the desktop window or with "
                    + "other tools while you work. Tool results can therefore start with a [NOTICE] line "
                    + "explaining that the document or the file on disk changed outside your calls. "
                    + "When the file changed, call disk_diff to see the changed lines, then sync_from_disk "
                    + "to merge those changes into your document; your own edits are kept and conflicts "
                    + "are reported so you can keep working instead of reloading or overwriting.";

    private McpServerRunner() {
    }

    private static McpSyncServer build(StdioServerTransportProvider transport, EditorService service,
                                       String version) {
        McpSyncServer server = McpServer.sync(transport)
                .serverInfo(SERVER_NAME, version)
                // listChanged stays false: the tool set is fixed at startup, and
                // emitting change notifications before a client connects fails
                // inside a native image.
                .capabilities(McpSchema.ServerCapabilities.builder().tools(false).build())
                .instructions(INSTRUCTIONS)
                .build();
        for (ToolSpec spec : new McpToolset(service).tools()) {
            server.addTool(toSdkTool(spec, service));
        }
        return server;
    }

    /**
     * Starts a server on the given streams; returns it for tests and embedding.
     */
    public static McpSyncServer start(EditorService service, InputStream in, OutputStream out, String version) {
        JacksonMcpJsonMapper mapper = new JacksonMcpJsonMapper(Json.mapper());
        return build(new StdioServerTransportProvider(mapper, in, out), service, version);
    }

    private static McpServerFeatures.SyncToolSpecification toSdkTool(ToolSpec spec, EditorService service) {
        McpSchema.Tool tool = McpSchema.Tool.builder()
                .name(spec.name())
                .title(spec.title())
                .description(spec.description())
                .inputSchema(spec.inputSchema())
                .build();
        return new McpServerFeatures.SyncToolSpecification(tool, (exchange, request) -> {
            long started = System.nanoTime();
            String notice = service.beginToolCall(spec.name());
            ToolOutcome outcome;
            try {
                outcome = spec.call(request.arguments());
            } catch (RuntimeException ex) {
                McpLog.line(spec.name() + " threw " + ex);
                outcome = ToolOutcome.error(ex.getMessage() == null ? ex.toString() : ex.getMessage());
            }
            long millis = (System.nanoTime() - started) / 1_000_000;
            service.endToolCall(spec.name(), millis, outcome.error());
            McpLog.call(spec.name(), millis, outcome.error(),
                    service.session().file() == null ? null : service.session().file().toString(), notice);
            String text = outcome.text() == null ? "" : outcome.text();
            if (notice != null) {
                text = notice + "\n" + text;
            }
            McpSchema.CallToolResult.Builder builder = McpSchema.CallToolResult.builder();
            builder.addTextContent(text);
            if (outcome.hasPng()) {
                builder.addContent(McpSchema.ImageContent.builder(
                        Base64.getEncoder().encodeToString(outcome.png()), "image/png").build());
            }
            if (outcome.structured() != null) {
                builder.structuredContent(outcome.structured());
            }
            builder.isError(outcome.error());
            return builder.build();
        });
    }

    /**
     * Blocks on stdio until the client disconnects. The input stream wrapper
     * notices the end of stdin and releases the latch, so a scripted client
     * that closes the pipe does not leave the process behind.
     */
    public static void serveStdio(EditorService service, String version) {
        JacksonMcpJsonMapper mapper = new JacksonMcpJsonMapper(Json.mapper());
        dev.zoroaster1x.vlcskin.util.AppLog.start("MCP", version);
        dev.zoroaster1x.vlcskin.util.Banner.startup(System.err);
        McpLog.started(version, service.session().file() == null
                ? null : service.session().file().toString());
        CountDownLatch latch = new CountDownLatch(1);
        InputStream in = new InputStream() {
            @Override
            public int read() throws IOException {
                int next = System.in.read();
                if (next < 0) {
                    latch.countDown();
                }
                return next;
            }

            @Override
            public int read(byte[] buffer, int offset, int length) throws IOException {
                int count = System.in.read(buffer, offset, length);
                if (count < 0) {
                    latch.countDown();
                }
                return count;
            }
        };
        McpSyncServer server = build(new StdioServerTransportProvider(mapper, in, System.out), service, version);
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            server.closeGracefully();
            latch.countDown();
        }, "vlc-skin-mcp-shutdown"));
        try {
            latch.await();
        } catch (InterruptedException ex) {
            Thread.currentThread().interrupt();
        }
        McpLog.stopped();
        server.closeGracefully();
    }
}
