package dev.zoroaster1x.vlcskin.gallery;

/**
 * One theme from the official VideoLAN skins gallery.
 *
 * @param id         gallery row id
 * @param name       display name
 * @param author     author name
 * @param date       upload or update date
 * @param downloads  download count
 * @param file       archive file name, usually the display name plus .vlt
 * @param size       human readable size
 * @param previewUrl preview image URL
 * @param version    skin version
 */
public record GalleryTheme(int id, String name, String author, String date, long downloads,
                           String file, String size, String previewUrl, String version) {

    /**
     * A file name safe for a folder name.
     */
    public String folderName() {
        String base = file == null || file.isBlank() ? name : file;
        if (base.toLowerCase(java.util.Locale.ROOT).endsWith(".vlt")) {
            base = base.substring(0, base.length() - 4);
        }
        return base.replaceAll("[^A-Za-z0-9._-]+", "_");
    }
}
