package dev.zoroaster1x.vlcskin.update;

import com.fasterxml.jackson.databind.JsonNode;
import dev.zoroaster1x.vlcskin.Version;
import dev.zoroaster1x.vlcskin.util.Json;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.nio.file.AtomicMoveNotSupportedException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.security.MessageDigest;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HexFormat;
import java.util.List;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.function.Consumer;

/**
 * Talks to the GitHub releases: which version is newest, the notes of every
 * release the user missed, and the download of the jar with its SHA-256 check
 * before it replaces the running file.
 *
 * <p>Replacement is platform aware. Unix swaps the jar in place, which is safe
 * while the JVM runs because it keeps the old inode open. Windows locks a
 * running jar, so a generated batch helper waits for this process to exit,
 * swaps the file and starts the app again.
 */
public final class UpdateService {

    public static final String RELEASES_PAGE = "https://github.com/zoroaster1x/VLC-Skin-Editor-3/releases";
    private static final String RELEASES_API =
            "https://api.github.com/repos/zoroaster1x/VLC-Skin-Editor-3/releases?per_page=30";
    private static final String JAR_ASSET = "vlc-skin-studio.jar";
    private static final String CHECKSUMS_ASSET = "SHA256SUMS";

    /**
     * One GitHub release with the asset URLs the updater needs.
     */
    public record Release(String tag, String name, String notes, String pageUrl, String publishedAt,
                          String jarUrl, String checksumsUrl) {

        /**
         * The numeric part of the tag: {@code v1.0.2} becomes {@code 1.0.2}.
         */
        public String version() {
            Matcher matcher = Pattern.compile("\\d+(?:\\.\\d+)+").matcher(tag == null ? "" : tag);
            return matcher.find() ? matcher.group() : (tag == null ? "" : tag);
        }
    }

    /**
     * The outcome of a check: the newest release, and every release newer than
     * the running version, oldest first, so the notes read in upgrade order.
     */
    public record UpdateInfo(String current, Release latest, List<Release> missed) {

        public boolean updateAvailable() {
            return latest != null && !missed.isEmpty();
        }
    }

    /**
     * What {@link #install} did: the target file, whether a restart helper was
     * started, and where that helper lives.
     */
    public record InstallResult(Path target, boolean restartHandled, Path helper) {
    }

    private final HttpClient http;
    private final String releasesUrl;
    private final String currentVersion;

    public UpdateService() {
        this(HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(10))
                .followRedirects(HttpClient.Redirect.NORMAL)
                .build(), RELEASES_API, Version.VERSION);
    }

    UpdateService(HttpClient http, String releasesUrl, String currentVersion) {
        this.http = http;
        this.releasesUrl = releasesUrl;
        this.currentVersion = currentVersion;
    }

    /**
     * Asks GitHub which releases exist and sorts them against the running
     * version.
     */
    public UpdateInfo check() throws IOException, InterruptedException {
        List<Release> newer = new ArrayList<>();
        for (Release release : releases()) {
            if (compareVersions(release.version(), currentVersion) > 0) {
                newer.add(release);
            }
        }
        newer.sort(Comparator.comparing(Release::version, UpdateService::compareVersions));
        return new UpdateInfo(currentVersion, newer.isEmpty() ? null : newer.getLast(), newer);
    }

    List<Release> releases() throws IOException, InterruptedException {
        HttpRequest request = HttpRequest.newBuilder(URI.create(releasesUrl))
                .timeout(Duration.ofSeconds(15))
                .header("Accept", "application/vnd.github+json")
                .header("User-Agent", "vlc-skin-studio")
                .GET()
                .build();
        HttpResponse<String> response = http.send(request, HttpResponse.BodyHandlers.ofString());
        if (response.statusCode() >= 400) {
            throw new IOException("GitHub returned HTTP " + response.statusCode());
        }
        return parseReleases(response.body());
    }

    static List<Release> parseReleases(String json) throws IOException {
        List<Release> releases = new ArrayList<>();
        JsonNode root = Json.mapper().readTree(json);
        if (root == null || !root.isArray()) {
            return releases;
        }
        for (JsonNode node : root) {
            if (node.path("draft").asBoolean(false)) {
                continue;
            }
            releases.add(new Release(
                    node.path("tag_name").asText(""),
                    node.path("name").asText(""),
                    node.path("body").asText(""),
                    node.path("html_url").asText(""),
                    node.path("published_at").asText(""),
                    assetUrl(node, JAR_ASSET),
                    assetUrl(node, CHECKSUMS_ASSET)));
        }
        return releases;
    }

    private static String assetUrl(JsonNode release, String name) {
        for (JsonNode asset : release.path("assets")) {
            if (name.equals(asset.path("name").asText())) {
                return asset.path("browser_download_url").asText("");
            }
        }
        return "";
    }

    /**
     * Downloads the release jar to the target path, replacing it atomically.
     */
    public Path downloadJar(Release release, Path target, Consumer<String> progress)
            throws IOException, InterruptedException {
        if (release.jarUrl() == null || release.jarUrl().isBlank()) {
            throw new IOException("Release " + release.tag() + " has no " + JAR_ASSET + " asset");
        }
        if (progress != null) {
            progress.accept("Downloading " + release.version() + "...");
        }
        writeAtomically(target, get(release.jarUrl(), Duration.ofMinutes(5)));
        return target;
    }

    /**
     * Downloads the checksum file that belongs to the release jar.
     */
    public Path downloadChecksums(Release release, Path target) throws IOException, InterruptedException {
        if (release.checksumsUrl() == null || release.checksumsUrl().isBlank()) {
            throw new IOException("Release " + release.tag() + " has no " + CHECKSUMS_ASSET + " asset");
        }
        writeAtomically(target, get(release.checksumsUrl(), Duration.ofMinutes(2)));
        return target;
    }

    private byte[] get(String url, Duration timeout) throws IOException, InterruptedException {
        HttpRequest request = HttpRequest.newBuilder(URI.create(url))
                .timeout(timeout)
                .header("User-Agent", "vlc-skin-studio")
                .GET()
                .build();
        HttpResponse<byte[]> response = http.send(request, HttpResponse.BodyHandlers.ofByteArray());
        if (response.statusCode() >= 400) {
            throw new IOException("The download returned HTTP " + response.statusCode());
        }
        byte[] body = response.body();
        if (body.length == 0) {
            throw new IOException("The download was empty");
        }
        return body;
    }

    /**
     * The first SHA-256 in a {@code sha256sum} file, or null when the file has
     * no usable line.
     */
    public static String expectedChecksum(Path checksumsFile) throws IOException {
        for (String line : Files.readAllLines(checksumsFile, StandardCharsets.UTF_8)) {
            String trimmed = line.strip();
            if (trimmed.isEmpty()) {
                continue;
            }
            String hash = trimmed.split("\\s+")[0];
            if (hash.matches("[0-9a-fA-F]{64}")) {
                return hash.toLowerCase(Locale.ROOT);
            }
        }
        return null;
    }

    /**
     * The SHA-256 of a file, hex encoded, lower case.
     */
    public static String checksum(Path file) throws IOException {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            try (InputStream in = Files.newInputStream(file)) {
                byte[] buffer = new byte[65536];
                int read;
                while ((read = in.read(buffer)) > 0) {
                    digest.update(buffer, 0, read);
                }
            }
            return HexFormat.of().formatHex(digest.digest());
        } catch (java.security.NoSuchAlgorithmException ex) {
            throw new IOException("SHA-256 is not available", ex);
        }
    }

    public static boolean verify(Path file, String expectedSha256) throws IOException {
        return expectedSha256 != null && !expectedSha256.isBlank()
                && checksum(file).equalsIgnoreCase(expectedSha256);
    }

    /**
     * The jar this JVM runs from, or null for a class directory or a native
     * image, where self replacement is impossible.
     */
    public static Path runningJar() {
        try {
            var source = UpdateService.class.getProtectionDomain().getCodeSource();
            if (source == null || source.getLocation() == null) {
                return null;
            }
            Path path = Path.of(source.getLocation().toURI());
            return Files.isRegularFile(path) && path.getFileName().toString().endsWith(".jar") ? path : null;
        } catch (Exception ex) {
            return null;
        }
    }

    /**
     * Installs a verified jar over the running one. On Unix the file is swapped
     * in place and the caller should restart when convenient. On Windows a
     * helper waits for this process to exit, swaps the file and starts the app
     * again, so the caller quits after this returns.
     */
    public static InstallResult install(Path verifiedJar, Path target) throws IOException {
        if (!Files.isRegularFile(verifiedJar)) {
            throw new IOException("The verified update jar is missing: " + verifiedJar);
        }
        Path absoluteTarget = target.toAbsolutePath();
        if (isWindows()) {
            Path helper = Files.createTempFile("vlc-skin-studio-update", ".bat");
            Files.writeString(helper, windowsHelper(), StandardCharsets.UTF_8);
            new ProcessBuilder("cmd.exe", "/c", helper.toString(),
                    Long.toString(ProcessHandle.current().pid()),
                    verifiedJar.toAbsolutePath().toString(),
                    absoluteTarget.toString()).start();
            return new InstallResult(absoluteTarget, true, helper);
        }
        Path temp = absoluteTarget.resolveSibling(absoluteTarget.getFileName() + ".new");
        Files.copy(verifiedJar, temp, StandardCopyOption.REPLACE_EXISTING);
        try {
            Files.move(temp, absoluteTarget, StandardCopyOption.REPLACE_EXISTING,
                    StandardCopyOption.ATOMIC_MOVE);
        } catch (AtomicMoveNotSupportedException ex) {
            Files.move(temp, absoluteTarget, StandardCopyOption.REPLACE_EXISTING);
        }
        return new InstallResult(absoluteTarget, false, null);
    }

    public static boolean isWindows() {
        return System.getProperty("os.name", "").toLowerCase(Locale.ROOT).contains("win");
    }

    /**
     * The Windows swap helper. It takes the pid, the downloaded jar and the
     * target jar as arguments: it polls tasklist until the pid is gone, moves
     * the file into place and starts the app again. It has no parenthesized
     * blocks on purpose, so paths with brackets or spaces stay safe.
     */
    static String windowsHelper() {
        return "@echo off\r\n"
                + "setlocal\r\n"
                + ":wait\r\n"
                + "tasklist /FI \"PID eq %~1\" | find \"%~1\" >NUL\r\n"
                + "if errorlevel 1 goto replace\r\n"
                + "ping -n 2 127.0.0.1 >NUL\r\n"
                + "goto wait\r\n"
                + ":replace\r\n"
                + "move /Y \"%~2\" \"%~3\" >NUL 2>&1\r\n"
                + "if not errorlevel 1 goto restart\r\n"
                + "set /a TRIES+=1\r\n"
                + "if %TRIES% GEQ 60 goto giveup\r\n"
                + "ping -n 2 127.0.0.1 >NUL\r\n"
                + "goto replace\r\n"
                + ":giveup\r\n"
                + "exit /b 1\r\n"
                + ":restart\r\n"
                + "start \"\" javaw -jar \"%~3\"\r\n"
                + "del \"%~f0\"\r\n";
    }

    /**
     * Compares dotted numeric versions: 1.0.10 is newer than 1.0.9, a leading
     * v or a suffix still compares by the numbers.
     */
    public static int compareVersions(String left, String right) {
        int[] a = versionParts(left);
        int[] b = versionParts(right);
        for (int i = 0; i < Math.max(a.length, b.length); i++) {
            int x = i < a.length ? a[i] : 0;
            int y = i < b.length ? b[i] : 0;
            if (x != y) {
                return Integer.compare(x, y);
            }
        }
        return 0;
    }

    private static int[] versionParts(String version) {
        if (version == null) {
            return new int[0];
        }
        Matcher matcher = Pattern.compile("\\d+(?:\\.\\d+)*").matcher(version);
        if (!matcher.find()) {
            return new int[0];
        }
        String[] pieces = matcher.group().split("\\.");
        int[] parts = new int[pieces.length];
        for (int i = 0; i < pieces.length; i++) {
            try {
                parts[i] = Integer.parseInt(pieces[i]);
            } catch (NumberFormatException ex) {
                parts[i] = 0;
            }
        }
        return parts;
    }

    private static void writeAtomically(Path file, byte[] bytes) throws IOException {
        Files.createDirectories(file.toAbsolutePath().getParent());
        Path temp = Files.createTempFile(file.toAbsolutePath().getParent(),
                file.getFileName().toString(), ".part");
        Files.write(temp, bytes);
        try {
            Files.move(temp, file, StandardCopyOption.REPLACE_EXISTING, StandardCopyOption.ATOMIC_MOVE);
        } catch (AtomicMoveNotSupportedException ex) {
            Files.move(temp, file, StandardCopyOption.REPLACE_EXISTING);
        }
    }
}
