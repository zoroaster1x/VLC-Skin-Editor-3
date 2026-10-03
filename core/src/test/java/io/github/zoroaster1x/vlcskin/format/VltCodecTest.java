package io.github.zoroaster1x.vlcskin.format;

import static org.assertj.core.api.Assertions.assertThat;

import io.github.zoroaster1x.vlcskin.example.ExampleSkins;
import java.nio.file.Files;
import java.nio.file.Path;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class VltCodecTest {

    @Test
    void exampleRoundTripsThroughAVlt(@TempDir Path folder) throws Exception {
        Path skinFolder = folder.resolve("skin");
        Path themeFile = ExampleSkins.create(skinFolder, ExampleSkins.NEON);
        var session = io.github.zoroaster1x.vlcskin.edit.EditorSession.open(themeFile);

        Path vlt = folder.resolve("theme.vlt");
        VltCodec.write(session.theme(), themeFile, vlt);
        assertThat(Files.size(vlt)).isGreaterThan(1000);

        VltCodec.Contents contents = VltCodec.read(vlt);
        assertThat(contents.themeXml()).contains("<Theme");
        assertThat(contents.assets()).containsKeys("background.png", "play.png");

        Path unpacked = folder.resolve("unpacked");
        Path unpackedTheme = VltCodec.unpack(vlt, unpacked);
        assertThat(unpackedTheme).exists();
        assertThat(unpacked.resolve("seek_thumb.png")).exists();

        SkinParser.Result parsed = SkinParser.parse(unpackedTheme);
        assertThat(parsed.issues()).noneMatch(issue -> issue.severity() == ParseIssue.Severity.ERROR);
        assertThat(parsed.theme().getWindows()).hasSize(1);
    }

    @Test
    void zipArchivesAreAccepted(@TempDir Path folder) throws Exception {
        Path zip = folder.resolve("theme.zip");
        try (var out = new java.util.zip.ZipOutputStream(Files.newOutputStream(zip))) {
            out.putNextEntry(new java.util.zip.ZipEntry("theme.xml"));
            out.write("<Theme version=\"2.0\"><Window id=\"w\"><Layout id=\"l\" width=\"10\" height=\"10\"/>"
                    .getBytes(java.nio.charset.StandardCharsets.UTF_8));
            out.closeEntry();
            out.putNextEntry(new java.util.zip.ZipEntry("image.png"));
            out.write(new byte[] {1, 2, 3});
            out.closeEntry();
        }
        VltCodec.Contents contents = VltCodec.read(zip);
        assertThat(contents.themeXml()).contains("Window");
        assertThat(contents.assets()).containsKey("image.png");
    }
}
