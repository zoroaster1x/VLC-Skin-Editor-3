package dev.zoroaster1x.vlcskin.gallery;

import dev.zoroaster1x.vlcskin.format.VltCodec;
import java.io.IOException;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

/**
 * Reads the official VideoLAN skins gallery and downloads single themes.
 *
 * <p>The gallery page lists every skin in a JavaScript call per entry; the
 * download link behind the page redirects to the real archive, which is then
 * unpacked with {@link VltCodec}.
 */
public final class ThemeGalleryClient {

    public static final String LIST_URL = "https://www.videolan.org/vlc/skins.html";
    public static final String DOWNLOAD_URL = "https://www.videolan.org/vlc/skins2/";

    private final HttpClient http;
    private final String listUrl;
    private final String downloadUrl;

    public ThemeGalleryClient() {
        this(HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(10))
                .followRedirects(HttpClient.Redirect.NORMAL)
                .build(), LIST_URL, DOWNLOAD_URL);
    }

    ThemeGalleryClient(HttpClient http, String listUrl, String downloadUrl) {
        this.http = http;
        this.listUrl = listUrl;
        this.downloadUrl = downloadUrl;
    }

    /**
     * Every theme the gallery page currently lists.
     */
    public List<GalleryTheme> fetch() throws IOException, InterruptedException {
        HttpRequest request = HttpRequest.newBuilder(URI.create(listUrl))
                .timeout(Duration.ofSeconds(30))
                .header("User-Agent", "vlc-skin-studio")
                .GET()
                .build();
        HttpResponse<String> response = http.send(request, HttpResponse.BodyHandlers.ofString());
        if (response.statusCode() >= 400) {
            throw new IOException("The gallery returned HTTP " + response.statusCode());
        }
        return parse(response.body());
    }

    /**
     * Downloads a theme archive and unpacks it into the folder.
     *
     * @return the unpacked theme.xml
     */
    public Path download(GalleryTheme theme, Path folder, Consumer<String> progress)
            throws IOException, InterruptedException {
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
        Files.createDirectories(folder);
        Path archive = Files.createTempFile(folder, "gallery-", ".vlt");
        try {
            Files.write(archive, body);
            if (progress != null) {
                progress.accept("Unpacking " + theme.name() + "...");
            }
            return VltCodec.unpack(archive, folder);
        } finally {
            Files.deleteIfExists(archive);
        }
    }

    /**
     * The bytes of a preview image, or null when it cannot be loaded.
     */
    public byte[] image(String url) {
        if (url == null || url.isBlank()) {
            return null;
        }
        try {
            HttpRequest request = HttpRequest.newBuilder(URI.create(url))
                    .timeout(Duration.ofSeconds(15))
                    .header("User-Agent", "vlc-skin-studio")
                    .GET()
                    .build();
            HttpResponse<byte[]> response = http.send(request, HttpResponse.BodyHandlers.ofByteArray());
            return response.statusCode() < 400 ? response.body() : null;
        } catch (IOException | InterruptedException ex) {
            if (ex instanceof InterruptedException) {
                Thread.currentThread().interrupt();
            }
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
