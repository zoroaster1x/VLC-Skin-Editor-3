package io.github.zoroaster1x.vlcskin.render;

import io.github.zoroaster1x.vlcskin.model.SkinIndex;
import io.github.zoroaster1x.vlcskin.model.item.PlaytreeItem;
import io.github.zoroaster1x.vlcskin.model.item.SliderBackground;
import io.github.zoroaster1x.vlcskin.model.item.SliderItem;
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
        return sharedPath(slider.getPoints()).pointAt(variables.sliderValue());
    }

    /**
     * The frame of the background grid for a fill level. Frames run left to
     * right, then top to bottom, exactly like VLC's slider background.
     */
    public static Rectangle backgroundFrame(SliderBackground background, float value, SkinIndex index,
                                            ImageStore images) {
        BufferedImage whole = wholeBackground(index, images, background);
        if (whole == null) {
            return null;
        }
        int nbhoriz = Math.max(1, background.getNbhoriz());
        int nbvert = Math.max(1, background.getNbvert());
        int frameWidth = Math.max(1, (whole.getWidth() - background.getPadhoriz() * (nbhoriz - 1)) / nbhoriz);
        int frameHeight = Math.max(1, (whole.getHeight() - background.getPadvert() * (nbvert - 1)) / nbvert);
        int fields = nbhoriz * nbvert;
        int n = (int) (fields * value);
        n = Math.max(0, Math.min(fields - 1, n));
        int fx = n % nbhoriz;
        int fy = n / nbhoriz;
        return new Rectangle(
                fx * (frameWidth + background.getPadhoriz()),
                fy * (frameHeight + background.getPadvert()),
                frameWidth, frameHeight);
    }

    private static BufferedImage wholeBackground(SkinIndex index, ImageStore images, SliderBackground background) {
        if (background.getImage() == null) {
            return null;
        }
        SkinIndex.ImageRef ref = index.findImage(background.getImage());
        if (ref == null) {
            return null;
        }
        return images.wholeImage(index, ref.bitmap());
    }

    /**
     * How many rows of the playtree sample the preview draws.
     */
    public static int playtreeSampleRows(PlaytreeItem playtree) {
        return playtree.isFlat() ? 3 : 5;
    }
}
