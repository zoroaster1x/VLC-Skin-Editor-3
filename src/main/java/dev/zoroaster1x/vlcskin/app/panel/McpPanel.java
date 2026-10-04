package dev.zoroaster1x.vlcskin.app.panel;

import dev.zoroaster1x.vlcskin.app.Studio;
import dev.zoroaster1x.vlcskin.app.i18n.Messages;
import dev.zoroaster1x.vlcskin.mcp.McpLog;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Font;
import java.util.List;
import java.util.Map;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JToolBar;
import javax.swing.Timer;

/**
 * The MCP activity view: a timestamped tail of the server log plus its status.
 *
 * <p>The AI's server is normally a separate process, so this panel reads the
 * log file the server writes under the cache directory instead of trying to
 * share memory with it. When a tool call happens, the newest line appears
 * within a second.
 */
public final class McpPanel extends JPanel {

    private static final int TAIL = 400;

    private final JLabel status = new JLabel();
    private final JTextArea lines = new JTextArea();
    private final Timer timer;

    public McpPanel(Studio studio) {
        setLayout(new BorderLayout());

        JToolBar bar = new JToolBar();
        bar.setFloatable(false);
        JButton refresh = new JButton(Messages.get("APP_XML_REFRESH", "Refresh"),
                dev.zoroaster1x.vlcskin.app.component.Icons.of("refresh", 14));
        refresh.setToolTipText(Messages.get("APP_MCP_REFRESH_TIP", "Read the log again"));
        refresh.addActionListener(e -> refresh());
        JButton copy = new JButton(Messages.get("APP_MCP_COPY", "Copy"));
        copy.addActionListener(e -> {
            lines.selectAll();
            lines.copy();
            lines.setCaretPosition(lines.getText().length());
        });
        bar.add(refresh);
        bar.add(copy);
        bar.add(Box.createHorizontalGlue());
        status.setBorder(BorderFactory.createEmptyBorder(0, 8, 0, 8));
        bar.add(status);

        lines.setEditable(false);
        lines.setLineWrap(false);
        lines.setFont(new Font(Font.MONOSPACED, Font.PLAIN,
                lines.getFont() != null ? lines.getFont().getSize() : 12));
        JScrollPane scroll = new JScrollPane(lines);
        scroll.setBorder(null);

        add(bar, BorderLayout.NORTH);
        add(scroll, BorderLayout.CENTER);

        // Only poll while the panel is visible; the log file is tiny.
        timer = new Timer(1000, e -> refresh());
        timer.setInitialDelay(0);
        refresh();
    }

    @Override
    public void addNotify() {
        super.addNotify();
        timer.start();
    }

    @Override
    public void removeNotify() {
        timer.stop();
        super.removeNotify();
    }

    /**
     * Re-reads the status and the log tail.
     */
    public void refresh() {
        Map<String, Object> state = McpLog.readStatus();
        if (state.isEmpty()) {
            status.setText(Messages.get("APP_MCP_STATUS_NONE", "MCP server: no run recorded yet"));
        } else {
            long calls = state.get("calls") instanceof Number number ? number.longValue() : 0;
            long last = state.get("lastCallAt") instanceof Number number ? number.longValue() : 0;
            String when = last == 0 ? "never"
                    : Math.max(0, (System.currentTimeMillis() - last) / 1000) + "s ago";
            status.setText(Messages.format("APP_MCP_STATUS", "MCP server: %s, %s call(s), last %s",
                    McpLog.isRunning() ? "running" : "not running", calls, when));
        }
        List<String> tail = McpLog.tail(TAIL);
        String text = String.join("\n", tail);
        if (!text.equals(lines.getText())) {
            lines.setText(text);
            if (text.length() > 0) {
                lines.setCaretPosition(text.length());
            }
        }
    }

    public JTextArea textArea() {
        return lines;
    }

    public String statusText() {
        return status.getText();
    }
}
