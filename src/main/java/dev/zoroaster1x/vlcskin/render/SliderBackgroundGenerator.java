package dev.zoroaster1x.vlcskin.render;

import java.awt.Graphics2D;
import java.awt.image.BufferedImage;

/**
 * Builds a slider background strip: one frame per fill level, the way the
 * original editor's wizard did. The image is laid out as one column of frames
 * for a horizontal slider and one row for a vertical slider, matching VLC's
 * nbhoriz/nbvert frame grid when the generated picture is saved with
 * nbvert = frames for horizontal and nbhoriz = frames for vertical.
 */
public final class SliderBackgroundGenerator {

    /**
     * Everything the generator needs; images may be null except the middle one.
     */
    public record Spec(
            int width,
            int height,
            int marginLeft,
            int marginRight,
            int marginTop,
            int marginBottom,
            boolean horizontal,
            boolean tileBackground,
            boolean tileMiddle,
            BufferedImage background,
            BufferedImage edge1,
            BufferedImage middle,
            BufferedImage edge2,
            BufferedImage overlay) {
    }

    private SliderBackgroundGenerator() {
    }

    public static BufferedImage generate(Spec spec) {
        if (spec.middle() == null) {
            throw new IllegalArgumentException("The middle image is required");
        }
        return spec.horizontal() ? horizontal(spec) : vertical(spec);
    }

    private static BufferedImage horizontal(Spec spec) {
        int frames = spec.width() - spec.marginLeft() - spec.marginRight() + 1;
        frames = Math.max(1, frames);
        BufferedImage out = new BufferedImage(spec.width(), frames * spec.height(), BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = out.createGraphics();
        try {
            for (int frame = 0; frame < frames; frame++) {
                int y = frame * spec.height();
                drawBackground(g, spec, 0, y, spec.width(), spec.height());
                if (frame > 0) {
                    drawProgressHorizontal(g, spec, frame, y);
                }
                if (spec.overlay() != null) {
                    g.drawImage(spec.overlay(), 0, y, null);
                }
            }
        } finally {
            g.dispose();
        }
        return out;
    }

    private static void drawProgressHorizontal(Graphics2D g, Spec spec, int progress, int y) {
        int x = spec.marginLeft();
        int innerHeight = Math.max(1, spec.height() - spec.marginTop() - spec.marginBottom());
        int edge1 = width(spec.edge1());
        int middle = width(spec.middle());
        int edge2 = width(spec.edge2());
        int drawn = 0;
        if (spec.edge1() != null && drawn < progress) {
            int part = Math.min(edge1, progress - drawn);
            g.drawImage(spec.edge1(), x, y + spec.marginTop(), x + part, y + spec.marginTop() + innerHeight,
                    0, 0, part, spec.edge1().getHeight(), null);
            x += part;
            drawn += part;
        }
        if (drawn < progress) {
            int remainingForEdge2 = spec.edge2() != null ? edge2 : 0;
            int shrink = Math.min(remainingForEdge2, Math.max(0, edge2 - (progress - drawn - middle)));
            int middleNeeded = Math.max(0, progress - drawn - shrink);
            if (middleNeeded > 0) {
                x = drawMiddleHorizontal(g, spec, x, y, middleNeeded, innerHeight);
                drawn += middleNeeded;
            }
        }
        if (spec.edge2() != null && drawn < progress) {
            int part = Math.min(edge2, progress - drawn);
            g.drawImage(spec.edge2(), x, y + spec.marginTop(), x + part, y + spec.marginTop() + innerHeight,
                    0, 0, part, spec.edge2().getHeight(), null);
        }
    }

    private static int drawMiddleHorizontal(Graphics2D g, Spec spec, int x, int y, int needed, int innerHeight) {
        int middleWidth = width(spec.middle());
        int sourceHeight = spec.middle().getHeight();
        int drawn = 0;
        if (spec.tileMiddle()) {
            while (drawn < needed) {
                int part = Math.min(middleWidth, needed - drawn);
                g.drawImage(spec.middle(), x + drawn, y + spec.marginTop(),
                        x + drawn + part, y + spec.marginTop() + innerHeight,
                        0, 0, part, sourceHeight, null);
                drawn += part;
            }
        } else {
            g.drawImage(spec.middle(), x, y + spec.marginTop(), x + needed, y + spec.marginTop() + innerHeight,
                    0, 0, middleWidth, sourceHeight, null);
            drawn = needed;
        }
        return x + drawn;
    }

    private static BufferedImage vertical(Spec spec) {
        int frames = spec.height() - spec.marginTop() - spec.marginBottom() + 1;
        frames = Math.max(1, frames);
        BufferedImage out = new BufferedImage(frames * spec.width(), spec.height(), BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = out.createGraphics();
        try {
            for (int frame = 0; frame < frames; frame++) {
                int x = frame * spec.width();
                drawBackground(g, spec, x, 0, spec.width(), spec.height());
                if (frame > 0) {
                    drawProgressVertical(g, spec, frame, x);
                }
                if (spec.overlay() != null) {
                    g.drawImage(spec.overlay(), x, 0, null);
                }
            }
        } finally {
            g.dispose();
        }
        return out;
    }

    private static void drawProgressVertical(Graphics2D g, Spec spec, int progress, int x) {
        int y = spec.height() - spec.marginBottom();
        int innerWidth = Math.max(1, spec.width() - spec.marginLeft() - spec.marginRight());
        int edge1 = height(spec.edge1());
        int middle = height(spec.middle());
        int edge2 = height(spec.edge2());
        int drawn = 0;
        if (spec.edge1() != null && drawn < progress) {
            int part = Math.min(edge1, progress - drawn);
            g.drawImage(spec.edge1(), x + spec.marginLeft(), y - part,
                    x + spec.marginLeft() + innerWidth, y,
                    0, spec.edge1().getHeight() - part, spec.edge1().getWidth(), spec.edge1().getHeight(), null);
            y -= part;
            drawn += part;
        }
        if (drawn < progress) {
            int remainingForEdge2 = spec.edge2() != null ? edge2 : 0;
            int shrink = Math.min(remainingForEdge2, Math.max(0, edge2 - (progress - drawn - middle)));
            int middleNeeded = Math.max(0, progress - drawn - shrink);
            if (middleNeeded > 0) {
                y = drawMiddleVertical(g, spec, x, y, middleNeeded, innerWidth);
                drawn += middleNeeded;
            }
        }
        if (spec.edge2() != null && drawn < progress) {
            int part = Math.min(edge2, progress - drawn);
            g.drawImage(spec.edge2(), x + spec.marginLeft(), y - part,
                    x + spec.marginLeft() + innerWidth, y,
                    0, spec.edge2().getHeight() - part, spec.edge2().getWidth(), spec.edge2().getHeight(), null);
        }
    }

    private static int drawMiddleVertical(Graphics2D g, Spec spec, int x, int y, int needed, int innerWidth) {
        int middleHeight = height(spec.middle());
        int sourceWidth = spec.middle().getWidth();
        int drawn = 0;
        if (spec.tileMiddle()) {
            while (drawn < needed) {
                int part = Math.min(middleHeight, needed - drawn);
                g.drawImage(spec.middle(), x + spec.marginLeft(), y - drawn - part,
                        x + spec.marginLeft() + innerWidth, y - drawn,
                        0, middleHeight - part, sourceWidth, middleHeight, null);
                drawn += part;
            }
        } else {
            g.drawImage(spec.middle(), x + spec.marginLeft(), y - needed,
                    x + spec.marginLeft() + innerWidth, y,
                    0, 0, sourceWidth, middleHeight, null);
            drawn = needed;
        }
        return y - drawn;
    }

    private static void drawBackground(Graphics2D g, Spec spec, int x, int y, int width, int height) {
        if (spec.background() == null) {
            return;
        }
        BufferedImage background = spec.background();
        if (!spec.tileBackground()) {
            g.drawImage(background, x, y, x + width, y + height, 0, 0, background.getWidth(),
                    background.getHeight(), null);
            return;
        }
        for (int ty = 0; ty < height; ty += background.getHeight()) {
            for (int tx = 0; tx < width; tx += background.getWidth()) {
                g.drawImage(background, x + tx, y + ty, null);
            }
        }
    }

    private static int width(BufferedImage image) {
        return image == null ? 0 : image.getWidth();
    }

    private static int height(BufferedImage image) {
        return image == null ? 0 : image.getHeight();
    }

    /**
     * Frames produced for a spec, used to set the grid on the slider background.
     */
    public static int frameCount(Spec spec) {
        return Math.max(1, (spec.horizontal()
                ? spec.width() - spec.marginLeft() - spec.marginRight()
                : spec.height() - spec.marginTop() - spec.marginBottom()) + 1);
    }
}
