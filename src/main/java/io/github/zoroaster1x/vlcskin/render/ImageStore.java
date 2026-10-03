package io.github.zoroaster1x.vlcskin.render;

import io.github.zoroaster1x.vlcskin.model.SkinIndex;
import io.github.zoroaster1x.vlcskin.model.resource.BitmapResource;
import io.github.zoroaster1x.vlcskin.model.resource.FontResource;
import io.github.zoroaster1x.vlcskin.model.resource.SubBitmap;
import java.awt.Color;
import java.awt.Font;
import java.awt.FontFormatException;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;
import javax.imageio.ImageIO;

/**
 * Loads and caches bitmap and font resources for the preview.
 *
 * <p>The bitmap pipeline mirrors VLC's skins2 FileBitmap: the whole PNG is
 * decoded to straight ARGB, every pixel whose RGB equals the alphacolor
 * becomes fully transparent, and a multi frame strip is cut into equal frames.
 * Semi transparent pixels keep their alpha, so a preview composites the same
 * way VLC does.
 */
public final class ImageStore {

    private static final java.util.concurrent.atomic.AtomicReference<BufferedImage> BROKEN_REF =
            new java.util.concurrent.atomic.AtomicReference<>();

    /**
     * The placeholder is created lazily so a CLI or MCP run that never draws an
     * image does not have to initialize AWT (which matters inside a native
     * image).
     */
    private static BufferedImage broken() {
        BufferedImage broken = BROKEN_REF.get();
        if (broken == null) {
            broken = brokenPlaceholder();
            BROKEN_REF.set(broken);
        }
        return broken;
    }

    private final Path skinFolder;
    private final Map<String, BufferedImage> images = new HashMap<>();
    private final Map<String, BufferedImage> wholeImages = new HashMap<>();
    private final Map<String, Font> fonts = new HashMap<>();
    private final Map<String, String> problems = new HashMap<>();

    public ImageStore(Path skinFolder) {
        this.skinFolder = skinFolder;
    }

    public Path skinFolder() {
        return skinFolder;
    }

    public String problem(String id) {
        return problems.get(id);
    }

    public Map<String, String> problems() {
        return Map.copyOf(problems);
    }

    public void invalidate() {
        images.clear();
        wholeImages.clear();
        fonts.clear();
        problems.clear();
    }

    public void invalidate(String id) {
        images.keySet().removeIf(key -> key.equals(id) || key.startsWith(id + "#"));
        wholeImages.remove(id);
        problems.remove(id);
    }

    /**
     * The first frame of a bitmap, or the cropped image of a sub bitmap.
     */
    public BufferedImage image(SkinIndex index, String id) {
        return image(index, id, 0);
    }

    /**
     * The frame of a bitmap strip for the current animation tick. Static images
     * always return frame 0; a bitmap with nbframes greater than one cycles.
     */
    public BufferedImage image(SkinIndex index, String id, int frameTick) {
        if (id == null || id.isEmpty()) {
            return null;
        }
        SkinIndex.ImageRef ref = index.findImage(id);
        if (ref == null) {
            problems.put(id, "No bitmap or sub bitmap is named \"" + id + "\"");
            return null;
        }
        int frames = ref.sub() != null ? 1 : Math.max(1, ref.bitmap().getNbframes());
        int frame = frames > 1 ? Math.floorMod(frameTick, frames) : 0;
        String key = id + "#" + frame;
        BufferedImage cached = images.get(key);
        if (cached != null) {
            return cached;
        }
        BufferedImage whole = wholeImage(index, ref.bitmap());
        if (whole == null) {
            images.put(key, broken());
            return broken();
        }
        BufferedImage result;
        if (ref.sub() != null) {
            result = crop(whole, ref.sub(), id);
        } else {
            int frameHeight = Math.max(1, whole.getHeight() / frames);
            result = whole.getSubimage(0, frame * frameHeight, whole.getWidth(), frameHeight);
        }
        images.put(key, result);
        return result;
    }

    /**
     * True when any bitmap in the theme has more than one frame.
     */
    public static boolean hasAnimation(SkinIndex index) {
        for (io.github.zoroaster1x.vlcskin.model.resource.Resource resource : index.theme().getResources()) {
            if (resource instanceof BitmapResource bitmap && bitmap.getNbframes() > 1) {
                return true;
            }
        }
        return false;
    }

    /**
     * The animation rate to use, taken from the fastest animated bitmap.
     */
    public static int animationFps(SkinIndex index) {
        int fps = 0;
        for (io.github.zoroaster1x.vlcskin.model.resource.Resource resource : index.theme().getResources()) {
            if (resource instanceof BitmapResource bitmap && bitmap.getNbframes() > 1) {
                fps = Math.max(fps, bitmap.getFps() > 0 ? bitmap.getFps() : 10);
            }
        }
        return Math.max(0, Math.min(60, fps));
    }

    /**
     * The complete bitmap without frame splitting, for slider backgrounds.
     */
    public BufferedImage wholeImage(SkinIndex index, BitmapResource bitmap) {
        BufferedImage cached = wholeImages.get(bitmap.getId());
        if (cached != null) {
            return cached;
        }
        if (bitmap.getFile() == null || bitmap.getFile().isBlank()) {
            problems.put(bitmap.getId(), "Bitmap \"" + bitmap.getId() + "\" has no file");
            wholeImages.put(bitmap.getId(), broken());
            return broken();
        }
        File file = skinFolder.resolve(bitmap.getFile().replace('\\', '/')).toFile();
        try {
            BufferedImage decoded = ImageIO.read(file);
            if (decoded == null) {
                throw new IOException("unsupported image format");
            }
            BufferedImage keyed = applyAlphaColor(decoded, bitmap.getAlphacolor());
            wholeImages.put(bitmap.getId(), keyed);
            return keyed;
        } catch (IOException ex) {
            problems.put(bitmap.getId(), "Could not load " + bitmap.getFile() + ": " + ex.getMessage());
            wholeImages.put(bitmap.getId(), broken());
            return broken();
        }
    }

    private BufferedImage crop(BufferedImage whole, SubBitmap sub, String id) {
        int x = Math.max(0, sub.getX());
        int y = Math.max(0, sub.getY());
        int width = Math.min(sub.getWidth(), whole.getWidth() - x);
        int height = Math.min(sub.getHeight(), whole.getHeight() - y);
        if (width <= 0 || height <= 0) {
            problems.put(id, "SubBitmap \"" + id + "\" is outside its parent bitmap");
            return broken();
        }
        if (x + width > whole.getWidth() || y + height > whole.getHeight()) {
            problems.put(id, "SubBitmap \"" + id + "\" is clipped by its parent bitmap");
        }
        return whole.getSubimage(x, y, width, height);
    }

    /**
     * VLC keys out any pixel whose RGB equals alphacolor, ignoring its alpha.
     */
    private BufferedImage applyAlphaColor(BufferedImage source, String alphacolor) {
        int key;
        try {
            key = Color.decode(alphacolor).getRGB() & 0x00FFFFFF;
        } catch (NumberFormatException ex) {
            key = 0x00FF00FF;
        }
        int width = source.getWidth();
        int height = source.getHeight();
        BufferedImage out = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                int argb = source.getRGB(x, y);
                int alpha = (argb >>> 24) & 0xFF;
                int rgb = argb & 0x00FFFFFF;
                out.setRGB(x, y, rgb == key ? 0 : ((alpha << 24) | rgb));
            }
        }
        return out;
    }

    /**
     * Resolves a font resource, falling back to a sane sans serif.
     */
    public Font font(SkinIndex index, String id) {
        if (id == null || id.isEmpty() || "defaultfont".equals(id)) {
            return new Font(Font.SANS_SERIF, Font.PLAIN, 12);
        }
        Font cached = fonts.get(id);
        if (cached != null) {
            return cached;
        }
        FontResource resource = index.findFont(id);
        int size = resource != null && resource.getSize() > 0 ? resource.getSize() : 12;
        Font result = new Font(Font.SANS_SERIF, Font.PLAIN, size);
        if (resource != null && resource.getFile() != null && !resource.getFile().isBlank()) {
            File file = skinFolder.resolve(resource.getFile().replace('\\', '/')).toFile();
            try {
                result = Font.createFont(Font.TRUETYPE_FONT, file).deriveFont((float) size);
            } catch (FontFormatException | IOException ex) {
                problems.put(id, "Could not load font " + resource.getFile() + ": " + ex.getMessage());
            }
        }
        fonts.put(id, result);
        return result;
    }

    private static BufferedImage brokenPlaceholder() {
        BufferedImage image = new BufferedImage(32, 32, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = image.createGraphics();
        g.setColor(new Color(255, 0, 0, 100));
        g.fillRect(0, 0, 32, 32);
        g.setColor(new Color(255, 255, 255, 200));
        g.drawLine(0, 0, 31, 31);
        g.drawLine(31, 0, 0, 31);
        g.dispose();
        return image;
    }
}
