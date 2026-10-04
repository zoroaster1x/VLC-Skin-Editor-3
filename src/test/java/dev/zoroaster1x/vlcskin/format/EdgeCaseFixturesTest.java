package dev.zoroaster1x.vlcskin.format;

import static org.assertj.core.api.Assertions.assertThat;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.stream.Stream;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

/**
 * Opens every VLC edge-case fixture in this repository. The fixtures come from
 * the VLC skins2 source and each one isolates one tolerance or quirk; see
 * docs/skins2-edge-cases.md for the inventory and the VLC references.
 *
 * <p>The point is that no real-world input makes the parser throw. Whatever
 * the theme is missing or gets wrong, the document must still open with
 * issues, because that is what keeps an imported theme editable.
 */
class EdgeCaseFixturesTest {

    private static final Path FIXTURES = Path.of("src", "test", "resources", "edge-cases");

    @Test
    void everyFixtureParsesWithoutThrowing(@TempDir Path work) throws Exception {
        assertThat(FIXTURES).as("fixture folder").isDirectory();
        List<Path> themes;
        try (Stream<Path> stream = Files.list(FIXTURES)) {
            themes = stream
                    .filter(path -> path.getFileName().toString().endsWith(".xml"))
                    .sorted()
                    .toList();
        }
        assertThat(themes).as("fixture count").hasSize(53);

        Path copy = work.resolve("fixtures");
        copyTree(FIXTURES, copy);
        for (Path theme : themes) {
            String name = theme.getFileName().toString();
            Path target = copy.resolve(name);
            try {
                SkinParser.Result result = SkinParser.parse(target);
                assertThat(result.theme()).as("%s produces a theme", name).isNotNull();
            } catch (RuntimeException | java.io.IOException ex) {
                throw new AssertionError("fixture " + name + " failed: " + ex, ex);
            }
        }
    }

    private static void copyTree(Path source, Path target) throws java.io.IOException {
        try (Stream<Path> stream = Files.walk(source)) {
            for (Path path : stream.toList()) {
                Path relative = source.relativize(path);
                Path destination = target.resolve(relative.toString());
                if (Files.isDirectory(path)) {
                    Files.createDirectories(destination);
                } else {
                    Files.createDirectories(destination.getParent());
                    Files.copy(path, destination);
                }
            }
        }
    }
}
