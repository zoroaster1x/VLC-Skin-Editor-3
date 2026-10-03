package dev.zoroaster1x.vlcskin.tui;

import dev.zoroaster1x.vlcskin.mcp.EditorService;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import org.jline.reader.Candidate;
import org.jline.reader.Completer;
import org.jline.reader.LineReader;
import org.jline.reader.LineReaderBuilder;
import org.jline.reader.impl.completer.StringsCompleter;
import org.jline.terminal.Terminal;
import org.jline.terminal.TerminalBuilder;

/** The interactive terminal loop on top of {@link TuiShell}, with completion. */
public final class TuiLoop {

    private static final List<String> COMMANDS = List.of(
            "help", "open", "new", "info", "tree", "items", "show", "render", "render!",
            "vars", "set", "validate", "save", "actions", "examples", "example", "quit");

    private TuiLoop() {
    }

    public static void run(EditorService service, boolean color) throws IOException {
        Terminal terminal = TerminalBuilder.builder().system(true).dumb(false).build();
        TuiShell shell = new TuiShell(service, color);
        LineReader reader = LineReaderBuilder.builder()
                .terminal(terminal)
                .completer(completer(shell))
                .variable(LineReader.HISTORY_FILE, historyFile())
                .build();
        boolean useColor = color && terminal.getType() != null && !"dumb".equals(terminal.getType());
        if (!useColor) {
            shell = new TuiShell(service, false);
        }
        terminal.writer().print(shell.banner());
        terminal.writer().flush();
        while (true) {
            String line = reader.readLine("vlcskin> ");
            if (line == null) {
                break;
            }
            String output = shell.execute(line);
            terminal.writer().print(output);
            terminal.writer().flush();
            String trimmed = line.trim().toLowerCase();
            if ("quit".equals(trimmed) || "exit".equals(trimmed)) {
                break;
            }
        }
    }

    private static Path historyFile() {
        String configHome = System.getenv("XDG_CONFIG_HOME");
        Path base = configHome != null && !configHome.isBlank()
                ? Path.of(configHome) : Path.of(System.getProperty("user.home"), ".config");
        Path folder = base.resolve("vlc-skin-studio");
        try {
            Files.createDirectories(folder);
        } catch (IOException ex) {
            return Path.of(System.getProperty("java.io.tmpdir"), "vlc-skin-studio-history");
        }
        return folder.resolve("tui_history");
    }

    private static Completer completer(TuiShell shell) {
        StringsCompleter commands = new StringsCompleter(COMMANDS);
        return (reader, line, candidates) -> {
            if (line.wordIndex() == 0) {
                commands.complete(reader, line, candidates);
                return;
            }
            String command = line.words().isEmpty() ? "" : line.words().get(0);
            List<String> pool = switch (command) {
                case "show", "set" -> shell.itemIds();
                case "open" -> shell.resourceIds();
                case "tree" -> shell.layoutIds();
                default -> List.of();
            };
            String word = line.word() == null ? "" : line.word();
            for (String value : pool) {
                if (value.startsWith(word)) {
                    candidates.add(new Candidate(value, value, null, null, null, null, true));
                }
            }
        };
    }
}
