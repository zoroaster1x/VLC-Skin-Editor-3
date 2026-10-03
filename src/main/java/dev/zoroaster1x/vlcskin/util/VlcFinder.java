package dev.zoroaster1x.vlcskin.util;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Optional;

/**
 * Finds VLC and starts it on a skin. Handles native installs and the Flatpak
 * build, whose sandbox keeps its own VLC data folder under
 * ~/.var/app/org.videolan.VLC. A theme is shipped as a .vlt in the skins2
 * folder, which VLC's skin chooser expects.
 */
public final class VlcFinder {

    public static final String FLATPAK_ID = "org.videolan.VLC";

    private VlcFinder() {
    }

    /**
     * A native VLC binary, empty when only the Flatpak build is installed.
     */
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
            candidates.add(Path.of(System.getProperty("user.home"), "Applications/VLC.app/Contents/MacOS/VLC"));
        } else {
            candidates.add(Path.of("/usr/bin/vlc"));
            candidates.add(Path.of("/usr/local/bin/vlc"));
            candidates.add(Path.of(System.getProperty("user.home"), ".local/bin/vlc"));
        }
        for (Path candidate : candidates) {
            if (Files.isExecutable(candidate)) {
                return Optional.of(candidate);
            }
        }
        return onPath();
    }

    /**
     * True when the Flatpak VLC application is installed.
     */
    public static boolean isFlatpak() {
        if (find().isPresent()) {
            return false;
        }
        Optional<Path> flatpak = onPathNamed("flatpak");
        if (flatpak.isEmpty()) {
            return false;
        }
        try {
            Process process = new ProcessBuilder(flatpak.get().toString(), "info", FLATPAK_ID)
                    .redirectErrorStream(true).start();
            process.getInputStream().readAllBytes();
            return process.waitFor() == 0;
        } catch (IOException | InterruptedException ex) {
            return false;
        }
    }

    /**
     * The folder VLC reads skins from, native or Flatpak.
     */
    public static Path skinsFolder() {
        Path home = Path.of(System.getProperty("user.home"));
        String os = System.getProperty("os.name", "").toLowerCase(Locale.ROOT);
        if (os.contains("win")) {
            String programFiles = System.getenv("ProgramFiles");
            if (programFiles != null) {
                return Path.of(programFiles, "VideoLAN", "VLC", "skins");
            }
            return home.resolve("AppData/Roaming/vlc/skins2");
        }
        if (os.contains("mac")) {
            return home.resolve("Library/Application Support/org.videolan.vlc/skins2");
        }
        if (isFlatpak()) {
            return home.resolve(".var/app/" + FLATPAK_ID + "/data/vlc/skins2");
        }
        return home.resolve(".local/share/vlc/skins2");
    }

    /**
     * Copies a theme archive into VLC's skins folder, creating it if needed.
     */
    public static Path install(Path archive) throws IOException {
        Path folder = skinsFolder();
        Files.createDirectories(folder);
        Path target = folder.resolve(archive.getFileName());
        Files.copy(archive, target, java.nio.file.StandardCopyOption.REPLACE_EXISTING);
        return target;
    }

    /**
     * Starts VLC on an installed theme archive.
     */
    public static void launch(Path installedArchive) throws IOException {
        String sandboxPath = "$HOME/.local/share/vlc/skins2/" + installedArchive.getFileName();
        Optional<Path> nativeVlc = find();
        if (nativeVlc.isPresent()) {
            new ProcessBuilder(nativeVlc.get().toString(), "-I", "skins2",
                    "--skins2-last=" + installedArchive, "--skins2-systray").start();
            return;
        }
        Optional<Path> flatpak = onPathNamed("flatpak");
        if (flatpak.isEmpty()) {
            throw new IOException("VLC was not found");
        }
        new ProcessBuilder(flatpak.get().toString(), "run", FLATPAK_ID, "-I", "skins2",
                "--skins2-last=" + sandboxPath, "--skins2-systray").start();
    }

    /**
     * The exact command a user could paste when launching fails.
     */
    public static String describeLaunch(Path installedArchive) {
        if (find().isPresent()) {
            return "vlc -I skins2 --skins2-last=" + installedArchive;
        }
        return "flatpak run " + FLATPAK_ID + " -I skins2 --skins2-last=$HOME/.local/share/vlc/skins2/"
                + installedArchive.getFileName();
    }

    private static Optional<Path> onPath() {
        String os = System.getProperty("os.name", "").toLowerCase(Locale.ROOT);
        return onPathNamed(os.contains("win") ? "vlc.exe" : "vlc");
    }

    private static Optional<Path> onPathNamed(String name) {
        String path = System.getenv("PATH");
        if (path == null) {
            return Optional.empty();
        }
        for (String entry : path.split(java.io.File.pathSeparator)) {
            Path candidate = Path.of(entry, name);
            if (Files.isExecutable(candidate)) {
                return Optional.of(candidate);
            }
        }
        return Optional.empty();
    }
}
