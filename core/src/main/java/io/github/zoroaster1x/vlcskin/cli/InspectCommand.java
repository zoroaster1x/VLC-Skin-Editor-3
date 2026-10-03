package io.github.zoroaster1x.vlcskin.cli;

import io.github.zoroaster1x.vlcskin.edit.EditorSession;
import io.github.zoroaster1x.vlcskin.mcp.EditorService;
import io.github.zoroaster1x.vlcskin.util.Json;
import java.nio.file.Path;
import java.util.concurrent.Callable;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;
import picocli.CommandLine.Parameters;

/**
 * Print theme metadata, counts and problems as JSON.
 */
@Command(name = "inspect", description = "Print everything known about a skin as JSON.")
public final class InspectCommand implements Callable<Integer> {

    @Parameters(index = "0", paramLabel = "SKIN", description = "The skin XML file.")
    Path skin;

    @Option(names = "--pretty", description = "Pretty print the JSON.")
    boolean pretty;

    @Override
    public Integer call() throws Exception {
        EditorService service = new EditorService(EditorSession.open(skin));
        System.out.println(Json.write(service.documentInfo()));
        return 0;
    }
}
