package dev.zoroaster1x.vlcskin.render;

import dev.zoroaster1x.vlcskin.model.SkinIndex;
import dev.zoroaster1x.vlcskin.model.item.PlaytreeItem;
import dev.zoroaster1x.vlcskin.model.item.SliderBackground;
import dev.zoroaster1x.vlcskin.model.item.SliderItem;
import java.awt.Rectangle;
import java.awt.image.BufferedImage;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Slider path and background frame maths shared by the renderer and hit tests.
 */
public final class SliderGeometry {

    private static final int PATH_CACHE_LIMIT = 256;
    private static final Map<String, BezierPath> PATH_CACHE =
            new LinkedHashMap<>(64, 0.75f, true) {
                @Override
                protected boolean removeEldestEntry(Map.Entry<String, BezierPath> eldest) {
                    return size() > PATH_CACHE_LIMIT;
                }
            };

    private SliderGeometry() {
    }

    public static synchronized BezierPath sharedPath(String points) {
        return PATH_CACHE.computeIfAbsent(points, BezierPath::parse);
    }

    /**
     * The thumb centre for the current slider value.
     */
    public static java.awt.geom.Point2D.Float thumbPosition(SliderItem slider, PreviewVariables variables) {
        return thumbPosition(slider, variables.sliderValue());
    }

    /**
     * The thumb position for an explicit value, used by the nested playlist
     * slider whose position VLC initialises to 1.0 (var_tree.cpp:38-77).
     */
    public static java.awt.geom.Point2D.Float thumbPosition(SliderItem slider, float value) {
        return sharedPath(slider.getPoints()).pointAt(value);
    }

    /**
     * The frame of the background grid for a fill level. Frames run left to
     * right, then top to bottom, exactly like VLC's slider background.
     */
    public static Rectangle backgroundFrame(SliderBackground background, float value, SkinIndex index,
                                            ImageStore images) {
        BufferedImage whole = backgroundImage(index, images, background);
        if (whole == null) {
            return null;
        }
        int nbhoriz = Math.max(1, background.getNbhoriz());
        int nbvert = Math.max(1, background.getNbvert());
        int frameWidth = Math.max(1, (whole.getWidth() - background.getPadhoriz() * (nbhoriz - 1)) / nbhoriz);
        int frameHeight = Math.max(1, (whole.getHeight() - background.getPadvert() * (nbvert - 1)) / nbvert);
        int fields = nbhoriz * nbvert;
        // VLC: position = (int)(value * (fields - 1)).
        int n = (int) (value * (fields - 1));
        n = Math.max(0, Math.min(fields - 1, n));
        int fx = n % nbhoriz;
        int fy = n / nbhoriz;
        return new Rectangle(
                fx * (frameWidth + background.getPadhoriz()),
                fy * (frameHeight + background.getPadvert()),
                frameWidth, frameHeight);
    }

    /**
     * The bitmap a slider background cuts frames from, with the sub bitmap
     * region applied. VLC resolves an image id to a SubBitmap and scales and
     * cuts that rectangle; using the whole parent image is how a 204 wide time
     * strip used to turn into a 380 wide grey bar.
     */
    public static BufferedImage backgroundImage(SkinIndex index, ImageStore images,
                                                SliderBackground background) {
        if (background.getImage() == null) {
            return null;
        }
        SkinIndex.ImageRef ref = index.findImage(background.getImage());
        if (ref == null) {
            return null;
        }
        BufferedImage whole = images.wholeImage(index, ref.bitmap());
        if (whole == null || ref.sub() == null) {
            return whole;
        }
        int x = Math.max(0, Math.min(ref.sub().getX(), whole.getWidth() - 1));
        int y = Math.max(0, Math.min(ref.sub().getY(), whole.getHeight() - 1));
        int width = Math.max(1, Math.min(ref.sub().getWidth(), whole.getWidth() - x));
        int height = Math.max(1, Math.min(ref.sub().getHeight(), whole.getHeight() - y));
        return whole.getSubimage(x, y, width, height);
    }

    /**
     * How many rows of the playtree sample the preview draws.
     */
    public static int playtreeSampleRows(PlaytreeItem playtree) {
        return playtree.isFlat() ? 3 : 5;
    }
}
