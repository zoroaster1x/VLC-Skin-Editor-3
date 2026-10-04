package dev.zoroaster1x.vlcskin.format;

import static org.assertj.core.api.Assertions.assertThat;

import dev.zoroaster1x.vlcskin.example.ExampleSkins;
import java.nio.file.Files;
import java.nio.file.Path;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class VltCodecTest {

    @Test
    void exampleRoundTripsThroughAVlt(@TempDir Path folder) throws Exception {
        Path skinFolder = folder.resolve("skin");
        Path themeFile = ExampleSkins.create(skinFolder, ExampleSkins.NEON);
        var session = dev.zoroaster1x.vlcskin.edit.EditorSession.open(themeFile);
        session.theme().getThemeInfo().setName("Changed in memory");

        Path vlt = folder.resolve("theme.vlt");
        VltCodec.write(session.theme(), themeFile, vlt);
        assertThat(Files.size(vlt)).isGreaterThan(1000);

        // The in-memory model wins; the archive must not carry the stale file on top.
        VltCodec.Contents contents = VltCodec.read(vlt);
        assertThat(contents.themeXml()).contains("Changed in memory");
        assertThat(contents.themeXml()).contains("<Theme");
        assertThat(contents.assets()).containsKeys("background.png", "play.png");

        int themeEntries = 0;
        try (var tar = new org.apache.commons.compress.archivers.tar.TarArchiveInputStream(
                new java.util.zip.GZIPInputStream(Files.newInputStream(vlt)))) {
            var entry = tar.getNextEntry();
            while (entry != null) {
                if (entry.getName().equals("theme.xml")) {
                    themeEntries++;
                }
                entry = tar.getNextEntry();
            }
        }
        assertThat(themeEntries).as("exactly one theme.xml entry").isEqualTo(1);

        Path unpacked = folder.resolve("unpacked");
        Path unpackedTheme = VltCodec.unpack(vlt, unpacked);
        assertThat(unpackedTheme).exists();
        assertThat(unpacked.resolve("seek_thumb.png")).exists();

        SkinParser.Result parsed = SkinParser.parse(unpackedTheme);
        assertThat(parsed.issues()).noneMatch(issue -> issue.severity() == ParseIssue.Severity.ERROR);
        assertThat(parsed.theme().getWindows()).hasSize(1);
    }

    @Test
    void winamp2ArchivesGainTheBundledTemplate(@TempDir Path folder) throws Exception {
        Path zip = folder.resolve("winamp.zip");
        try (var out = new java.util.zip.ZipOutputStream(Files.newOutputStream(zip))) {
            out.putNextEntry(new java.util.zip.ZipEntry("main.bmp"));
            out.write(new byte[] {'B', 'M', 0, 0, 0, 0});
            out.closeEntry();
            out.putNextEntry(new java.util.zip.ZipEntry("cbuttons.bmp"));
            out.write(new byte[] {'B', 'M', 1, 0, 0, 0});
            out.closeEntry();
        }
        Path unpacked = folder.resolve("winamp-out");
        Path theme = VltCodec.unpack(zip, unpacked);
        assertThat(Files.readString(theme)).contains("Winamp2");
        assertThat(unpacked.resolve("main.bmp")).exists();
        assertThat(unpacked.resolve("cbuttons.bmp")).exists();

        // A Winamp2 archive in a folder keeps VLC's rule: paths resolve next to main.bmp.
        Path nested = folder.resolve("nested.zip");
        try (var out = new java.util.zip.ZipOutputStream(Files.newOutputStream(nested))) {
            out.putNextEntry(new java.util.zip.ZipEntry("CoolSkin/main.bmp"));
            out.write(new byte[] {'B', 'M', 0, 0, 0, 0});
            out.closeEntry();
            out.putNextEntry(new java.util.zip.ZipEntry("CoolSkin/volume.bmp"));
            out.write(new byte[] {'B', 'M', 1, 0, 0, 0});
            out.closeEntry();
        }
        Path nestedOut = folder.resolve("nested-out");
        assertThat(VltCodec.unpack(nested, nestedOut)).exists();
        assertThat(nestedOut.resolve("main.bmp")).exists();
        assertThat(nestedOut.resolve("volume.bmp")).exists();

        // Without theme.xml and without main.bmp the error stands.
        Path empty = folder.resolve("empty.zip");
        try (var out = new java.util.zip.ZipOutputStream(Files.newOutputStream(empty))) {
            out.putNextEntry(new java.util.zip.ZipEntry("readme.txt"));
            out.write("nothing".getBytes(java.nio.charset.StandardCharsets.UTF_8));
            out.closeEntry();
        }
        org.assertj.core.api.Assertions
                .assertThatThrownBy(() -> VltCodec.unpack(empty, folder.resolve("empty-out")))
                .isInstanceOf(java.io.IOException.class);
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

    @Test
    void zipWithAFolderPrefixUnpacksFlat(@TempDir Path folder) throws Exception {
        Path zip = folder.resolve("skin.zip");
        try (var out = new java.util.zip.ZipOutputStream(Files.newOutputStream(zip))) {
            out.putNextEntry(new java.util.zip.ZipEntry("CoolSkin/theme.xml"));
            out.write("<Theme version=\"2.0\"><Window id=\"w\"><Layout id=\"l\" width=\"10\" height=\"10\"/></Theme>"
                    .getBytes(java.nio.charset.StandardCharsets.UTF_8));
            out.closeEntry();
            out.putNextEntry(new java.util.zip.ZipEntry("CoolSkin/image.png"));
            out.write(new byte[] {1, 2, 3});
            out.closeEntry();
        }
        Path unpacked = folder.resolve("unpacked");
        VltCodec.unpack(zip, unpacked);
        assertThat(unpacked.resolve("theme.xml")).exists();
        assertThat(unpacked.resolve("image.png")).exists();
        assertThat(unpacked.resolve("CoolSkin")).doesNotExist();
    }

    @Test
    void aZipBundlingVltFilesUnpacksEveryTheme(@TempDir Path folder) throws Exception {
        Path skinFolder = folder.resolve("skin");
        Path themeFile = ExampleSkins.create(skinFolder, ExampleSkins.NEON);
        var session = dev.zoroaster1x.vlcskin.edit.EditorSession.open(themeFile);
        byte[] inner = VltCodec.toBytes(session.theme(), themeFile);

        Path bundle = folder.resolve("bundle.vlt");
        try (var out = new java.util.zip.ZipOutputStream(Files.newOutputStream(bundle))) {
            out.putNextEntry(new java.util.zip.ZipEntry("ColdBlue.vlt"));
            out.write(inner);
            out.closeEntry();
            out.putNextEntry(new java.util.zip.ZipEntry("FreshGreen.vlt"));
            out.write(inner);
            out.closeEntry();
        }

        Path unpacked = folder.resolve("unpacked");
        Path first = VltCodec.unpack(bundle, unpacked);
        assertThat(first).exists();
        assertThat(unpacked.resolve("ColdBlue").resolve("theme.xml")).exists();
        assertThat(unpacked.resolve("FreshGreen").resolve("theme.xml")).exists();
        assertThat(unpacked.resolve("ColdBlue").resolve("background.png")).exists();
    }
}
