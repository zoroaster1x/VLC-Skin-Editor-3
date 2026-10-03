package io.github.zoroaster1x.vlcskin.mcp;

import io.github.zoroaster1x.vlcskin.util.Json;
import io.modelcontextprotocol.json.jackson2.JacksonMcpJsonMapper;
import io.modelcontextprotocol.server.McpServer;
import io.modelcontextprotocol.server.McpServerFeatures;
import io.modelcontextprotocol.server.McpSyncServer;
import io.modelcontextprotocol.server.transport.StdioServerTransportProvider;
import io.modelcontextprotocol.spec.McpSchema;
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
                    + "window when it is running.";

    private McpServerRunner() {
    }

    private static McpSyncServer build(StdioServerTransportProvider transport, EditorService service,
                                       String version) {
        McpSyncServer server = McpServer.sync(transport)
                .serverInfo(SERVER_NAME, version)
                .capabilities(McpSchema.ServerCapabilities.builder().tools(true).build())
                .instructions(INSTRUCTIONS)
                .build();
        for (ToolSpec spec : new McpToolset(service).tools()) {
            server.addTool(toSdkTool(spec));
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

    private static McpServerFeatures.SyncToolSpecification toSdkTool(ToolSpec spec) {
        McpSchema.Tool tool = McpSchema.Tool.builder()
                .name(spec.name())
                .title(spec.title())
                .description(spec.description())
                .inputSchema(spec.inputSchema())
                .build();
        return new McpServerFeatures.SyncToolSpecification(tool, (exchange, request) -> {
            ToolOutcome outcome = spec.call(request.arguments());
            McpSchema.CallToolResult.Builder builder = McpSchema.CallToolResult.builder();
            builder.addTextContent(outcome.text() == null ? "" : outcome.text());
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
     * Blocks on stdio until the client disconnects.
     */
    public static void serveStdio(EditorService service, String version) {
        JacksonMcpJsonMapper mapper = new JacksonMcpJsonMapper(Json.mapper());
        McpSyncServer server = build(new StdioServerTransportProvider(mapper), service, version);
        CountDownLatch latch = new CountDownLatch(1);
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            server.closeGracefully();
            latch.countDown();
        }, "vlc-skin-mcp-shutdown"));
        try {
            latch.await();
        } catch (InterruptedException ex) {
            Thread.currentThread().interrupt();
            server.close();
        }
    }
}
