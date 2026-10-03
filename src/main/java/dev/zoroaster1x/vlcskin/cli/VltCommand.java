package dev.zoroaster1x.vlcskin.cli;

import dev.zoroaster1x.vlcskin.edit.EditorSession;
import dev.zoroaster1x.vlcskin.mcp.EditorService;
import java.nio.file.Path;
import java.util.concurrent.Callable;
import picocli.CommandLine.Command;
import picocli.CommandLine.Parameters;

/**
 * Import and export VLT theme archives.
 */
@Command(name = "vlt", description = "Import or export a .vlt theme archive.",
        subcommands = {VltCommand.Import.class, VltCommand.Export.class})
public final class VltCommand implements Callable<Integer> {

    @Override
    public Integer call() {
        System.err.println("Use vlt import <archive> [folder] or vlt export <skin.xml> <out.vlt>");
        return 1;
    }

    @Command(name = "import", description = "Unpack a .vlt archive and validate it.")
    public static final class Import implements Callable<Integer> {

        @Parameters(index = "0", paramLabel = "ARCHIVE", description = "The .vlt file.")
        Path archive;

        @Parameters(index = "1", paramLabel = "FOLDER", arity = "0..1",
                description = "Target folder, defaults next to the archive.")
        Path folder;

        @Override
        public Integer call() throws Exception {
            EditorService service = new EditorService();
            var outcome = service.importVlt(archive.toString(), folder == null ? null : folder.toString());
            System.out.println(outcome.text());
            return outcome.error() ? 1 : 0;
        }
    }

    @Command(name = "export", description = "Write a skin and its assets as a .vlt archive.")
    public static final class Export implements Callable<Integer> {

        @Parameters(index = "0", paramLabel = "SKIN", description = "The skin XML file.")
        Path skin;

        @Parameters(index = "1", paramLabel = "OUT", description = "Target .vlt path.")
        Path out;

        @Override
        public Integer call() throws Exception {
            EditorService service = new EditorService(EditorSession.open(skin));
            var outcome = service.exportVlt(out.toString());
            System.out.println(outcome.text());
            return outcome.error() ? 1 : 0;
        }
    }
}
