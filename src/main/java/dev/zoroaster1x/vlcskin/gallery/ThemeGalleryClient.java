package dev.zoroaster1x.vlcskin.gallery;

import com.fasterxml.jackson.core.type.TypeReference;
import dev.zoroaster1x.vlcskin.format.VltCodec;
import dev.zoroaster1x.vlcskin.util.Json;
import java.io.IOException;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.security.MessageDigest;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.HexFormat;
import java.util.List;
import java.util.function.Consumer;

/**
 * Reads the official VideoLAN skins gallery and downloads single themes.
 *
 * <p>The gallery page lists every skin in a JavaScript call per entry. The
 * parsed list, the preview images and the theme archives are cached under the
 * user cache folder; a stale list is still served when the network is down, so
 * the browser keeps working offline.
 */
public final class ThemeGalleryClient {

    public static final String LIST_URL = "https://www.videolan.org/vlc/skins.html";
    public static final String DOWNLOAD_URL = "https://www.videolan.org/vlc/skins2/";

    private static final Duration LIST_TTL = Duration.ofHours(24);

    private final HttpClient http;
    private final String listUrl;
    private final String downloadUrl;
    private final Path cacheRoot;

    public ThemeGalleryClient() {
        this(HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(10))
                .followRedirects(HttpClient.Redirect.NORMAL)
                .build(), LIST_URL, DOWNLOAD_URL, defaultCacheRoot());
    }

    ThemeGalleryClient(HttpClient http, String listUrl, String downloadUrl, Path cacheRoot) {
        this.http = http;
        this.listUrl = listUrl;
        this.downloadUrl = downloadUrl;
        this.cacheRoot = cacheRoot;
    }

    /**
     * The folder cached gallery data lives in, honouring XDG_CACHE_HOME.
     */
    public static Path defaultCacheRoot() {
        String cacheHome = System.getenv("XDG_CACHE_HOME");
        Path base = cacheHome != null && !cacheHome.isBlank()
                ? Path.of(cacheHome)
                : Path.of(System.getProperty("user.home"), ".cache");
        return base.resolve("vlc-skin-studio");
    }

    /**
     * Every theme the gallery page currently lists, from the cache when it is
     * fresh.
     */
    public List<GalleryTheme> fetch() throws IOException, InterruptedException {
        return fetch(false);
    }

    /**
     * @param force true to ignore the cached list and ask the site again
     */
    public List<GalleryTheme> fetch(boolean force) throws IOException, InterruptedException {
        Path listFile = cacheRoot.resolve("gallery").resolve("themes.json");
        if (!force) {
            List<GalleryTheme> cached = readThemes(listFile);
            if (cached != null && isFresh(listFile)) {
                return cached;
            }
        }
        try {
            HttpRequest request = HttpRequest.newBuilder(URI.create(listUrl))
                    .timeout(Duration.ofSeconds(30))
                    .header("User-Agent", "vlc-skin-studio")
                    .GET()
                    .build();
            HttpResponse<String> response = http.send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() >= 400) {
                throw new IOException("The gallery returned HTTP " + response.statusCode());
            }
            List<GalleryTheme> themes = parse(response.body());
            writeThemes(listFile, themes);
            return themes;
        } catch (IOException | InterruptedException ex) {
            List<GalleryTheme> cached = readThemes(listFile);
            if (cached != null) {
                return cached;
            }
            throw ex;
        }
    }

    /**
     * Downloads a theme archive and unpacks it into the folder. The archive is
     * kept in the cache, so importing the same theme again is instant.
     *
     * @return the unpacked theme.xml
     */
    public Path download(GalleryTheme theme, Path folder, Consumer<String> progress)
            throws IOException, InterruptedException {
        Path archive = cachedArchive(theme, progress);
        if (progress != null) {
            progress.accept("Unpacking " + theme.name() + "...");
        }
        return VltCodec.unpack(archive, folder);
    }

    private Path cachedArchive(GalleryTheme theme, Consumer<String> progress)
            throws IOException, InterruptedException {
        Path archive = cacheRoot.resolve("archives").resolve(theme.folderName() + ".vlt");
        if (Files.isRegularFile(archive) && Files.size(archive) > 0) {
            return archive;
        }
        if (progress != null) {
            progress.accept("Downloading " + theme.name() + "...");
        }
        String encoded = URLEncoder.encode(theme.file(), StandardCharsets.UTF_8).replace("+", "%20");
        HttpRequest request = HttpRequest.newBuilder(URI.create(downloadUrl + encoded))
                .timeout(Duration.ofMinutes(2))
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
        writeAtomically(archive, body);
        return archive;
    }

    /**
     * The bytes of a preview image, cached by URL. Returns null when the image
     * cannot be loaded.
     */
    public byte[] image(String url) {
        if (url == null || url.isBlank()) {
            return null;
        }
        Path cached = cacheRoot.resolve("previews").resolve(hash(url) + ".img");
        try {
            if (Files.isRegularFile(cached)) {
                return Files.readAllBytes(cached);
            }
            HttpRequest request = HttpRequest.newBuilder(URI.create(url))
                    .timeout(Duration.ofSeconds(15))
                    .header("User-Agent", "vlc-skin-studio")
                    .GET()
                    .build();
            HttpResponse<byte[]> response = http.send(request, HttpResponse.BodyHandlers.ofByteArray());
            if (response.statusCode() >= 400) {
                return null;
            }
            writeAtomically(cached, response.body());
            return response.body();
        } catch (InterruptedException ex) {
            Thread.currentThread().interrupt();
            return null;
        } catch (IOException ex) {
            return null;
        }
    }

    /**
     * The folder a downloaded theme goes into when the caller has no opinion.
     */
    public static Path themesFolder(GalleryTheme theme) {
        String dataHome = System.getenv("XDG_DATA_HOME");
        Path base = dataHome != null && !dataHome.isBlank()
                ? Path.of(dataHome)
                : Path.of(System.getProperty("user.home"), ".local", "share");
        return base.resolve("vlc-skin-studio").resolve("themes").resolve(theme.folderName());
    }

    private List<GalleryTheme> readThemes(Path file) {
        try {
            if (!Files.isRegularFile(file)) {
                return null;
            }
            return Json.mapper().readValue(Files.readString(file), new TypeReference<List<GalleryTheme>>() {
            });
        } catch (Exception ex) {
            return null;
        }
    }

    private void writeThemes(Path file, List<GalleryTheme> themes) {
        try {
            writeAtomically(file, Json.write(themes).getBytes(StandardCharsets.UTF_8));
        } catch (IOException ex) {
            // The cache is best effort; the list is returned either way.
        }
    }

    private static void writeAtomically(Path file, byte[] bytes) throws IOException {
        Files.createDirectories(file.getParent());
        Path temp = Files.createTempFile(file.getParent(), file.getFileName().toString(), ".tmp");
        Files.write(temp, bytes);
        try {
            Files.move(temp, file, StandardCopyOption.REPLACE_EXISTING, StandardCopyOption.ATOMIC_MOVE);
        } catch (IOException ex) {
            Files.move(temp, file, StandardCopyOption.REPLACE_EXISTING);
        }
    }

    private static boolean isFresh(Path file) {
        try {
            return Instant.now().toEpochMilli() - Files.getLastModifiedTime(file).toMillis()
                    < LIST_TTL.toMillis();
        } catch (IOException ex) {
            return false;
        }
    }

    private static String hash(String text) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            return HexFormat.of().formatHex(digest.digest(text.getBytes(StandardCharsets.UTF_8)));
        } catch (Exception ex) {
            return Integer.toHexString(text.hashCode());
        }
    }

    /**
     * Parses the showSkinBox calls of the gallery page.
     */
    public static List<GalleryTheme> parse(String html) {
        List<GalleryTheme> themes = new ArrayList<>();
        int index = 0;
        while ((index = html.indexOf("showSkinBox(", index)) >= 0) {
            int start = index + "showSkinBox(".length();
            int end = matchingParen(html, start);
            if (end < 0) {
                break;
            }
            List<String> fields = splitArguments(html.substring(start, end));
            index = end;
            if (fields.size() < 12) {
                continue;
            }
            try {
                themes.add(new GalleryTheme(
                        Integer.parseInt(fields.get(0).trim()),
                        fields.get(1),
                        fields.get(2),
                        fields.get(3),
                        parseLong(fields.get(4)),
                        fields.get(5),
                        fields.get(6),
                        fields.get(9),
                        fields.get(11)));
            } catch (RuntimeException ex) {
                // A malformed row must not stop the rest of the list.
            }
        }
        return themes;
    }

    private static int matchingParen(String text, int start) {
        int depth = 1;
        boolean inQuote = false;
        for (int i = start; i < text.length(); i++) {
            char c = text.charAt(i);
            if (inQuote) {
                if (c == '\\') {
                    i++;
                } else if (c == '\'') {
                    inQuote = false;
                }
            } else if (c == '\'') {
                inQuote = true;
            } else if (c == '(') {
                depth++;
            } else if (c == ')') {
                depth--;
                if (depth == 0) {
                    return i;
                }
            }
        }
        return -1;
    }

    private static List<String> splitArguments(String text) {
        List<String> fields = new ArrayList<>();
        StringBuilder current = new StringBuilder();
        boolean inQuote = false;
        for (int i = 0; i < text.length(); i++) {
            char c = text.charAt(i);
            if (inQuote) {
                if (c == '\\' && i + 1 < text.length()) {
                    current.append(text.charAt(++i));
                } else if (c == '\'') {
                    inQuote = false;
                } else {
                    current.append(c);
                }
            } else if (c == '\'') {
                inQuote = true;
            } else if (c == ',') {
                fields.add(current.toString().trim());
                current.setLength(0);
            } else {
                current.append(c);
            }
        }
        fields.add(current.toString().trim());
        return fields;
    }

    private static long parseLong(String text) {
        try {
            return Long.parseLong(text.trim());
        } catch (NumberFormatException ex) {
            return 0;
        }
    }
}
