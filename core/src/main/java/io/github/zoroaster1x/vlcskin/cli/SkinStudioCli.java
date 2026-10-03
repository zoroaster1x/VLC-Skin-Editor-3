package io.github.zoroaster1x.vlcskin.cli;

import io.github.zoroaster1x.vlcskin.Version;
import java.util.concurrent.Callable;
import picocli.CommandLine.Command;

/**
 * Root command; gui is handled by the app module, everything else lives here.
 */
@Command(name = "vlc-skin-studio",
        mixinStandardHelpOptions = true,
        versionProvider = SkinStudioCli.VersionProvider.class,
        description = "A modern VLC skins2 editor, with a desktop UI, a TUI, a CLI and an MCP server.",
        subcommands = {
                McpCommand.class,
                RenderCommand.class,
                InspectCommand.class,
                ValidateCommand.class,
                NewCommand.class,
                VltCommand.class,
                TuiCommand.class,
                ExamplesCommand.class
        })
public final class SkinStudioCli implements Callable<Integer> {

    @Override
    public Integer call() {
        System.out.println("VLC Skin Studio " + Version.VERSION);
        System.out.println("Run with gui to open the desktop window, or use --help for commands.");
        return 0;
    }

    public static void main(String[] args) {
        System.exit(new picocli.CommandLine(new SkinStudioCli()).execute(args));
    }

    /**
     * picocli version provider.
     */
    public static final class VersionProvider implements picocli.CommandLine.IVersionProvider {
        @Override
        public String[] getVersion() {
            return new String[] {Version.NAME + " " + Version.VERSION};
        }
    }
}
