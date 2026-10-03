package dev.zoroaster1x.vlcskin.gallery;

import static org.assertj.core.api.Assertions.assertThat;

import com.sun.net.httpserver.HttpServer;
import dev.zoroaster1x.vlcskin.example.ExampleSkins;
import dev.zoroaster1x.vlcskin.format.VltCodec;
import java.net.InetSocketAddress;
import java.net.http.HttpClient;
import java.nio.file.Path;
import java.util.Base64;
import java.util.concurrent.atomic.AtomicInteger;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class ThemeGalleryCacheTest {

    private static final byte[] TINY_PNG = Base64.getDecoder().decode(
            "iVBORw0KGgoAAAANSUhEUgAAAAEAAAABCAYAAAAfFcSJAAAADUlEQVR42mP8z8BQDwAEhQGAhKmMIQAAAABJRU5ErkJggg==");

    @Test
    void cachesTheListPreviewsAndArchives(@TempDir Path cache, @TempDir Path work) throws Exception {
        Path skinFolder = work.resolve("skin");
        Path themeFile = ExampleSkins.create(skinFolder, ExampleSkins.NEON);
        var session = dev.zoroaster1x.vlcskin.edit.EditorSession.open(themeFile);
        byte[] archive = VltCodec.toBytes(session.theme(), themeFile);

        HttpServer server = HttpServer.create(new InetSocketAddress("127.0.0.1", 0), 0);
        int port = server.getAddress().getPort();
        String base = "http://127.0.0.1:" + port;
        String page = "showSkinBox(1,'Cached','Me','2024-01-01','1','Cached.vlt','1KiB',5.0,9,"
                + "'" + base + "/preview.png','0','1.0')";
        AtomicInteger listHits = new AtomicInteger();
        AtomicInteger imageHits = new AtomicInteger();
        AtomicInteger archiveHits = new AtomicInteger();
        server.createContext("/skins.html", exchange -> {
            listHits.incrementAndGet();
            respond(exchange, page.getBytes());
        });
        server.createContext("/preview.png", exchange -> {
            imageHits.incrementAndGet();
            respond(exchange, TINY_PNG);
        });
        server.createContext("/vlc/skins2/Cached.vlt", exchange -> {
            archiveHits.incrementAndGet();
            respond(exchange, archive);
        });
        server.start();
        try {
            var client = new ThemeGalleryClient(HttpClient.newHttpClient(),
                    base + "/skins.html", base + "/vlc/skins2/", cache);

            assertThat(client.fetch()).hasSize(1);
            assertThat(client.fetch()).hasSize(1);
            assertThat(listHits).hasValue(1);

            assertThat(client.fetch(true)).hasSize(1);
            assertThat(listHits).hasValue(2);

            assertThat(client.image(base + "/preview.png")).isNotEmpty();
            assertThat(client.image(base + "/preview.png")).isNotEmpty();
            assertThat(imageHits).hasValue(1);

            GalleryTheme theme = client.fetch().get(0);
            Path first = client.download(theme, work.resolve("one"), null);
            Path second = client.download(theme, work.resolve("two"), null);
            assertThat(first).exists();
            assertThat(second).exists();
            assertThat(archiveHits).hasValue(1);
        } finally {
            server.stop(0);
        }
    }

    private static void respond(com.sun.net.httpserver.HttpExchange exchange, byte[] body)
            throws java.io.IOException {
        exchange.getResponseHeaders().add("Content-Type", "application/octet-stream");
        exchange.sendResponseHeaders(200, body.length);
        try (var out = exchange.getResponseBody()) {
            out.write(body);
        }
    }
}
