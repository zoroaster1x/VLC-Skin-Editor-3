package dev.zoroaster1x.vlcskin.format;

import dev.zoroaster1x.vlcskin.model.SkinTheme;
import dev.zoroaster1x.vlcskin.model.resource.BitmapResource;
import dev.zoroaster1x.vlcskin.model.resource.BitmapFontResource;
import dev.zoroaster1x.vlcskin.model.resource.FontResource;
import dev.zoroaster1x.vlcskin.model.resource.IniFileResource;
import dev.zoroaster1x.vlcskin.model.resource.Resource;
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

    public record Contents(String themeXml, String themeEntryName, Map<String, byte[]> assets) {
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
        String themeEntry = null;
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
                    themeEntry = name;
                } else {
                    assets.put(name, data);
                }
            }
        }
        return new Contents(themeXml, themeEntry, assets);
    }

    private static Contents readTarGz(byte[] bytes) throws IOException {
        String themeXml = null;
        String themeEntry = null;
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
                    themeEntry = name;
                } else {
                    assets.put(name, data);
                }
            }
        }
        return new Contents(themeXml, themeEntry, assets);
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
     * Extracts an archive next to itself into a folder, for VLT import. Some
     * gallery entries are a plain zip that bundles further .vlt archives; those
     * are unpacked too, one folder per bundled theme.
     */
    public static Path unpack(Path archive, Path targetFolder) throws IOException {
        Contents contents = read(archive);
        if (contents.themeXml() != null) {
            return extract(contents, targetFolder);
        }
        java.util.List<Map.Entry<String, byte[]>> nested = contents.assets().entrySet().stream()
                .filter(entry -> entry.getKey().toLowerCase(java.util.Locale.ROOT).endsWith(".vlt"))
                .filter(entry -> looksLikeArchive(entry.getValue()))
                .toList();
        if (nested.isEmpty()) {
            throw new IOException("The archive contains no theme.xml");
        }
        Path first = null;
        for (Map.Entry<String, byte[]> entry : nested) {
            Contents inner = readBytes(entry.getValue());
            if (inner.themeXml() == null) {
                continue;
            }
            String base = entry.getKey();
            int slash = base.lastIndexOf('/');
            base = slash >= 0 ? base.substring(slash + 1) : base;
            base = base.substring(0, base.length() - 4);
            Path folder = nested.size() == 1 ? targetFolder : targetFolder.resolve(base);
            Path themeFile = extract(inner, folder);
            if (first == null) {
                first = themeFile;
            }
        }
        if (first == null) {
            throw new IOException("The bundled .vlt files contain no theme.xml");
        }
        return first;
    }

    private static Path extract(Contents contents, Path targetFolder) throws IOException {
        Files.createDirectories(targetFolder);
        Files.writeString(targetFolder.resolve("theme.xml"), contents.themeXml(), StandardCharsets.UTF_8);
        String prefix = folderPrefix(contents.themeEntryName());
        Path normalizedTarget = targetFolder.normalize();
        for (Map.Entry<String, byte[]> asset : contents.assets().entrySet()) {
            String relative = stripPrefix(asset.getKey(), prefix);
            Path file = targetFolder.resolve(relative.replace('\\', '/')).normalize();
            if (!file.startsWith(normalizedTarget)) {
                throw new IOException("The archive tries to write outside the target folder: " + asset.getKey());
            }
            Files.createDirectories(file.getParent());
            Files.write(file, asset.getValue());
        }
        return targetFolder.resolve("theme.xml");
    }

    /**
     * Many gallery zips keep the whole theme under one folder, for example
     * {@code CoolSkin/theme.xml} and {@code CoolSkin/image.png}. The folder is
     * dropped so the theme lands flat in the target.
     */
    private static String folderPrefix(String themeEntryName) {
        if (themeEntryName == null) {
            return "";
        }
        int slash = themeEntryName.lastIndexOf('/');
        return slash >= 0 ? themeEntryName.substring(0, slash + 1) : "";
    }

    private static String stripPrefix(String name, String prefix) {
        if (!prefix.isEmpty() && name.startsWith(prefix)) {
            return name.substring(prefix.length());
        }
        return name;
    }

    private static boolean looksLikeArchive(byte[] bytes) {
        if (bytes.length > 2 && bytes[0] == 'P' && bytes[1] == 'K') {
            return true;
        }
        return bytes.length > 2 && (bytes[0] & 0xFF) == 0x1F && (bytes[1] & 0xFF) == 0x8B;
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
