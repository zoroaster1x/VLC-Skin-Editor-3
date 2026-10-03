package io.github.zoroaster1x.vlcskin.app;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Optional;

/**
 * Finds a VLC installation and starts it on a skin, the way the original editor did.
 */
public final class VlcLauncher {

    private VlcLauncher() {
    }

    public static Optional<Path> find() {
        List<Path> candidates = new ArrayList<>();
        String os = System.getProperty("os.name", "").toLowerCase(Locale.ROOT);
        if (os.contains("win")) {
            String programFiles = System.getenv("ProgramFiles");
            if (programFiles != null) {
                candidates.add(Path.of(programFiles, "VideoLAN", "VLC", "vlc.exe"));
            }
            String programFilesX86 = System.getenv("ProgramFiles(x86)");
            if (programFilesX86 != null) {
                candidates.add(Path.of(programFilesX86, "VideoLAN", "VLC", "vlc.exe"));
            }
        } else if (os.contains("mac")) {
            candidates.add(Path.of("/Applications/VLC.app/Contents/MacOS/VLC"));
            Path home = Path.of(System.getProperty("user.home"));
            candidates.add(home.resolve("Applications/VLC.app/Contents/MacOS/VLC"));
        } else {
            candidates.add(Path.of("/usr/bin/vlc"));
            candidates.add(Path.of("/usr/local/bin/vlc"));
            candidates.add(Path.of("/var/lib/flatpak/exports/bin/org.videolan.VLC"));
            Path home = Path.of(System.getProperty("user.home"));
            candidates.add(home.resolve(".local/bin/vlc"));
        }
        for (Path candidate : candidates) {
            if (Files.isExecutable(candidate)) {
                return Optional.of(candidate);
            }
        }
        return onPath();
    }

    private static Optional<Path> onPath() {
        String path = System.getenv("PATH");
        if (path == null) {
            return Optional.empty();
        }
        String name = System.getProperty("os.name", "").toLowerCase(Locale.ROOT).contains("win")
                ? "vlc.exe" : "vlc";
        for (String entry : path.split(java.io.File.pathSeparator)) {
            Path candidate = Path.of(entry, name);
            if (Files.isExecutable(candidate)) {
                return Optional.of(candidate);
            }
        }
        return Optional.empty();
    }

    /**
     * Starts VLC in skins2 mode with the given theme.
     */
    public static void launch(Path vlc, Path skinFile) throws IOException {
        new ProcessBuilder(vlc.toString(), "-I", "skins2", "--skins2-last=" + skinFile,
                "--skins2-systray").start();
    }
}
