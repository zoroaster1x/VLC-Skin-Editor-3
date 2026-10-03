package dev.zoroaster1x.vlcskin.cli;

import dev.zoroaster1x.vlcskin.Version;
import dev.zoroaster1x.vlcskin.example.ExampleSkins;
import dev.zoroaster1x.vlcskin.format.SkinParser;
import dev.zoroaster1x.vlcskin.format.SkinWriter;
import dev.zoroaster1x.vlcskin.mcp.EditorService;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.concurrent.Callable;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;
import picocli.CommandLine.Parameters;

/**
 * Create a new skin, optionally from a built in example.
 */
@Command(name = "new", description = "Create a new skin file, empty or from an example.")
public final class NewCommand implements Callable<Integer> {

    @Parameters(index = "0", paramLabel = "FILE", description = "Target skin XML file.")
    Path target;

    @Option(names = "--example", paramLabel = "ID", description = "Example id, see the examples command.")
    String exampleId;

    @Option(names = "--width", description = "Layout width for an empty skin.", defaultValue = "320")
    int width;

    @Option(names = "--height", description = "Layout height for an empty skin.", defaultValue = "140")
    int height;

    @Override
    public Integer call() throws Exception {
        if (exampleId != null) {
            ExampleSkins.Example example = ExampleSkins.catalog().stream()
                    .filter(candidate -> candidate.id().equals(exampleId))
                    .findFirst().orElse(null);
            if (example == null) {
                System.err.println("Unknown example \"" + exampleId + "\"; known: "
                        + ExampleSkins.catalog().stream().map(ExampleSkins.Example::id).toList());
                return 1;
            }
            Path folder = target.toAbsolutePath().getParent();
            Files.createDirectories(folder);
            ExampleSkins.create(folder, example);
            // The example writes theme.xml; move it to the requested name.
            Path generated = folder.resolve("theme.xml");
            Files.move(generated, target, java.nio.file.StandardCopyOption.REPLACE_EXISTING);
            System.out.println("Created " + target + " from example \"" + example.name() + "\"");
            return 0;
        }
        var service = new EditorService();
        service.newSkin("Untitled");
        var layout = service.session().currentLayout();
        layout.setWidth(width);
        layout.setHeight(height);
        Files.writeString(target, service.session().toXml(), StandardCharsets.UTF_8);
        System.out.println("Created " + target + " (" + width + "x" + height + ")");
        return 0;
    }

    /**
     * Lists the examples, used by the examples subcommand and the CLI help.
     */
    public static void printVersion(java.io.PrintWriter out) {
        out.println(Version.NAME + " " + Version.VERSION);
    }
}
