package dev.zoroaster1x.vlcskin.update;

import static org.assertj.core.api.Assertions.assertThat;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.http.HttpClient;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.MessageDigest;
import java.util.HexFormat;
import org.junit.jupiter.api.Assumptions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class UpdateServiceTest {

    @Test
    void comparesDottedVersionsLikeAUserWould() {
        assertThat(UpdateService.compareVersions("1.0.10", "1.0.9")).isPositive();
        assertThat(UpdateService.compareVersions("1.0.1", "1.0.1")).isZero();
        assertThat(UpdateService.compareVersions("v1.2.0", "1.1.9")).isPositive();
        assertThat(UpdateService.compareVersions("1.0.0", "2.0.0")).isNegative();
    }

    @Test
    void collectsEveryMissedReleaseOldestFirst() throws Exception {
        HttpServer server = HttpServer.create(new InetSocketAddress("127.0.0.1", 0), 0);
        String base = "http://127.0.0.1:" + server.getAddress().getPort();
        String json = """
                [
                  {"tag_name":"1.0.2","name":"VLC Skin Studio 1.0.2","body":"Second notes",
                   "html_url":"https://example/2","published_at":"2026-10-04T00:00:00Z","draft":false,
                   "assets":[{"name":"vlc-skin-studio.jar","browser_download_url":"%s/jar"},
                             {"name":"SHA256SUMS","browser_download_url":"%s/sums"}]},
                  {"tag_name":"1.0.1","name":"VLC Skin Studio 1.0.1","body":"First notes",
                   "html_url":"https://example/1","published_at":"2026-10-03T00:00:00Z","draft":false,
                   "assets":[{"name":"vlc-skin-studio.jar","browser_download_url":"%s/jar"},
                             {"name":"SHA256SUMS","browser_download_url":"%s/sums"}]}
                ]
                """.formatted(base, base, base, base);
        server.createContext("/releases", exchange -> respond(exchange, json.getBytes(StandardCharsets.UTF_8)));
        server.start();
        try {
            var info = new UpdateService(HttpClient.newHttpClient(), base + "/releases", "1.0.0").check();

            assertThat(info.updateAvailable()).isTrue();
            assertThat(info.current()).isEqualTo("1.0.0");
            assertThat(info.latest().version()).isEqualTo("1.0.2");
            assertThat(info.missed()).extracting(UpdateService.Release::version)
                    .containsExactly("1.0.1", "1.0.2");
        } finally {
            server.stop(0);
        }
    }

    @Test
    void downloadsTheJarAndVerifiesItsChecksum(@TempDir Path work) throws Exception {
        byte[] bytes = "pretend jar".getBytes(StandardCharsets.UTF_8);
        String hash = hashOf(bytes);
        HttpServer server = HttpServer.create(new InetSocketAddress("127.0.0.1", 0), 0);
        String base = "http://127.0.0.1:" + server.getAddress().getPort();
        server.createContext("/jar", exchange -> respond(exchange, bytes));
        server.createContext("/sums", exchange -> respond(exchange,
                (hash + "  vlc-skin-studio.jar\n").getBytes(StandardCharsets.UTF_8)));
        server.start();
        try {
            var release = new UpdateService.Release("1.0.2", "VLC Skin Studio 1.0.2", "notes",
                    "https://example", "2026-10-04T00:00:00Z", base + "/jar", base + "/sums");
            var service = new UpdateService(HttpClient.newHttpClient(), base + "/releases", "1.0.0");

            Path jar = service.downloadJar(release, work.resolve("out/vlc-skin-studio.jar"), null);
            Path sums = service.downloadChecksums(release, work.resolve("out/SHA256SUMS"));

            assertThat(jar).exists();
            assertThat(UpdateService.expectedChecksum(sums)).isEqualTo(hash);
            assertThat(UpdateService.verify(jar, hash)).isTrue();
            assertThat(UpdateService.verify(jar, "0".repeat(64))).isFalse();
        } finally {
            server.stop(0);
        }
    }

    @Test
    void replacesTheRunningJarOnUnix(@TempDir Path work) throws Exception {
        Assumptions.assumeFalse(UpdateService.isWindows());
        Path target = work.resolve("vlc-skin-studio.jar");
        Files.writeString(target, "old build");
        Path source = work.resolve("downloaded.jar");
        Files.writeString(source, "new build");

        var result = UpdateService.install(source, target);

        assertThat(Files.readString(target)).isEqualTo("new build");
        assertThat(result.restartHandled()).isFalse();
        assertThat(work.resolve("vlc-skin-studio.jar.new")).doesNotExist();
    }

    @Test
    void theWindowsHelperWaitsForTheProcessAndSwapsTheJar() {
        String helper = UpdateService.windowsHelper();
        assertThat(helper).contains("tasklist");
        assertThat(helper).contains("move /Y");
        assertThat(helper).contains("javaw -jar");
        assertThat(helper).contains("%~1").contains("%~2").contains("%~3");
    }

    private static String hashOf(byte[] bytes) throws Exception {
        return HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256").digest(bytes));
    }

    private static void respond(HttpExchange exchange, byte[] body) throws IOException {
        exchange.sendResponseHeaders(200, body.length);
        try (var out = exchange.getResponseBody()) {
            out.write(body);
        }
    }
}
