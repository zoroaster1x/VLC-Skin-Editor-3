package dev.zoroaster1x.vlcskin.mcp;

import static org.assertj.core.api.Assertions.assertThat;

import java.nio.file.Path;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

/**
 * The MCP status file and the activity log the desktop panel tails.
 */
class McpLogTest {

    @Test
    void writesAndReadsStatusAndTail(@TempDir Path root) {
        McpLog.useRoot(root);
        try {
            McpLog.started("9.9", "/tmp/example.xml");
            assertThat(McpLog.tail(20)).anyMatch(line -> line.contains("server started"));
            Map<String, Object> status = McpLog.readStatus();
            assertThat(status).containsEntry("version", "9.9").containsEntry("lastTool", "");
            assertThat(((Number) status.get("calls")).longValue()).isZero();

            McpLog.call("add_item", 12, false, "/tmp/example.xml", null);
            status = McpLog.readStatus();
            assertThat(status).containsEntry("lastTool", "add_item");
            assertThat(((Number) status.get("calls")).longValue()).isEqualTo(1);
            assertThat(McpLog.tail(20)).anyMatch(line -> line.contains("add_item") && line.contains("12 ms"));
            assertThat(McpLog.isRunning()).as("our own pid is alive").isTrue();

            McpLog.stopped();
            assertThat(McpLog.tail(20)).anyMatch(line -> line.contains("server stopped"));
        } finally {
            McpLog.resetRoot();
        }
    }
}
