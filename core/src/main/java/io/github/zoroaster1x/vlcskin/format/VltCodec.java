package io.github.zoroaster1x.vlcskin.format;

import io.github.zoroaster1x.vlcskin.model.SkinTheme;
import io.github.zoroaster1x.vlcskin.model.resource.BitmapResource;
import io.github.zoroaster1x.vlcskin.model.resource.BitmapFontResource;
import io.github.zoroaster1x.vlcskin.model.resource.FontResource;
import io.github.zoroaster1x.vlcskin.model.resource.IniFileResource;
import io.github.zoroaster1x.vlcskin.model.resource.Resource;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import org.apache.commons.compress.archivers.tar.TarArchiveEntry;
import org.apache.commons.compress.archivers.tar.TarArchiveInputStream;
import org.apache.commons.compress.archivers.tar.TarArchiveOutputStream;

/**
 * Reads and writes VLT theme archives. VLC bundles a theme as a gzipped tar
 * with theme.xml and every referenced asset. Some archives in the wild are
 * plain zip, so both are accepted.
 */
public final class VltCodec {

    public record Contents(String themeXml, Map<String, byte[]> assets) {

        public String themeEntryName() {
            return "theme.xml";
        }
    }

    private VltCodec() {
    }

    public static Contents read(Path archive) throws IOException {
        byte[] bytes = Files.readAllBytes(archive);
        if (bytes.length > 2 && bytes[0] == 'P' && bytes[1] == 'K') {
            return readZip(bytes);
        }
        return readTarGz(bytes);
    }

    private static Contents readZip(byte[] bytes) throws IOException {
        String themeXml = null;
        Map<String, byte[]> assets = new LinkedHashMap<>();
        try (ZipInputStream zip = new ZipInputStream(new java.io.ByteArrayInputStream(bytes))) {
            ZipEntry entry;
            while ((entry = zip.getNextEntry()) != null) {
                if (entry.isDirectory()) {
                    continue;
                }
                byte[] data = zip.readAllBytes();
                String name = entry.getName();
                if (isThemeXml(name)) {
                    themeXml = new String(data, StandardCharsets.UTF_8);
                } else {
                    assets.put(name, data);
                }
            }
        }
        return new Contents(themeXml, assets);
    }

    private static Contents readTarGz(byte[] bytes) throws IOException {
        String themeXml = null;
        Map<String, byte[]> assets = new LinkedHashMap<>();
        try (InputStream in = new BufferedInputStream(new java.io.ByteArrayInputStream(bytes));
             TarArchiveInputStream tar = new TarArchiveInputStream(new GZIPInputStream(in))) {
            TarArchiveEntry entry;
            while ((entry = tar.getNextEntry()) != null) {
                if (entry.isDirectory()) {
                    continue;
                }
                byte[] data = tar.readAllBytes();
                String name = entry.getName();
                if (isThemeXml(name)) {
                    themeXml = new String(data, StandardCharsets.UTF_8);
                } else {
                    assets.put(name, data);
                }
            }
        }
        return new Contents(themeXml, assets);
    }

    private static boolean isThemeXml(String name) {
        String base = name.contains("/") ? name.substring(name.lastIndexOf('/') + 1) : name;
        return base.equalsIgnoreCase("theme.xml");
    }

    /**
     * Writes a gzipped tar with theme.xml first and every referenced asset.
     */
    public static void write(SkinTheme theme, Path skinFile, Path target) throws IOException {
        Path folder = skinFile == null ? Path.of(".") : skinFile.getParent();
        if (folder == null) {
            folder = Path.of(".");
        }
        String themeXml = SkinWriter.toXml(theme);
        Set<String> written = new LinkedHashSet<>();
        try (OutputStream fileOut = Files.newOutputStream(target);
             BufferedOutputStream buffered = new BufferedOutputStream(fileOut);
             TarArchiveOutputStream tar = new TarArchiveOutputStream(new GZIPOutputStream(buffered))) {
            tar.setLongFileMode(TarArchiveOutputStream.LONGFILE_POSIX);
            writeEntry(tar, "theme.xml", themeXml.getBytes(StandardCharsets.UTF_8));
            for (String relative : referencedFiles(theme)) {
                if (!written.add(relative)) {
                    continue;
                }
                Path asset = folder.resolve(relative.replace('\\', '/')).normalize();
                if (!Files.isRegularFile(asset)) {
                    continue;
                }
                byte[] data = Files.readAllBytes(asset);
                writeEntry(tar, relative.replace('\\', '/'), data);
            }
        }
    }

    /**
     * Writes a theme archive into memory, for tests and MCP replies.
     */
    public static byte[] toBytes(SkinTheme theme, Path skinFile) throws IOException {
        Path temp = Files.createTempFile("vlcskin-vlt", ".tar.gz");
        try {
            write(theme, skinFile, temp);
            return Files.readAllBytes(temp);
        } finally {
            Files.deleteIfExists(temp);
        }
    }

    private static void writeEntry(TarArchiveOutputStream tar, String name, byte[] data) throws IOException {
        TarArchiveEntry entry = new TarArchiveEntry(name);
        entry.setSize(data.length);
        entry.setModTime(System.currentTimeMillis());
        tar.putArchiveEntry(entry);
        tar.write(data);
        tar.closeArchiveEntry();
    }

    /**
     * Every file a theme references, main skin folder relative.
     */
    public static Set<String> referencedFiles(SkinTheme theme) {
        Set<String> files = new LinkedHashSet<>();
        if (theme.getSourcePath() != null) {
            files.add(Path.of(theme.getSourcePath()).getFileName().toString());
        }
        for (Resource resource : theme.getResources()) {
            switch (resource) {
                case BitmapResource bitmap -> add(files, bitmap.getFile());
                case FontResource font -> add(files, font.getFile());
                case BitmapFontResource font -> add(files, font.getFile());
                case IniFileResource ini -> add(files, ini.getFile());
                default -> {
                }
            }
        }
        theme.getIncludes().forEach(include -> add(files, include.getFile()));
        return files;
    }

    private static void add(Set<String> files, String file) {
        if (file != null && !file.isBlank() && !"none".equals(file)) {
            files.add(file);
        }
    }

    /**
     * Extracts an archive next to itself into a folder, for VLT import.
     */
    public static Path unpack(Path archive, Path targetFolder) throws IOException {
        Contents contents = read(archive);
        if (contents.themeXml() == null) {
            throw new IOException("The archive contains no theme.xml");
        }
        Files.createDirectories(targetFolder);
        Files.writeString(targetFolder.resolve("theme.xml"), contents.themeXml(), StandardCharsets.UTF_8);
        for (Map.Entry<String, byte[]> asset : contents.assets().entrySet()) {
            Path file = targetFolder.resolve(asset.getKey().replace('\\', '/')).normalize();
            if (!file.startsWith(targetFolder.normalize())) {
                throw new IOException("The archive tries to write outside the target folder: " + asset.getKey());
            }
            Files.createDirectories(file.getParent());
            Files.write(file, asset.getValue());
        }
        return targetFolder.resolve("theme.xml");
    }

    /**
     * Convenience for tests: reads bytes instead of a file.
     */
    public static Contents readBytes(byte[] bytes) throws IOException {
        if (bytes.length > 2 && bytes[0] == 'P' && bytes[1] == 'K') {
            return readZip(bytes);
        }
        return readTarGz(bytes);
    }
}
