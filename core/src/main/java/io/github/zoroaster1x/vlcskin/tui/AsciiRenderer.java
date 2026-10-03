package io.github.zoroaster1x.vlcskin.tui;

import java.awt.image.BufferedImage;

/**
 * Turns a rendered skin into terminal art, with truecolor half blocks.
 */
public final class AsciiRenderer {

    private static final String RAMP = " .:-=+*#%@";
    private static final char HALF_BLOCK = '\u2580';

    private AsciiRenderer() {
    }

    /**
     * Renders with ANSI colors when the terminal supports them.
     */
    public static String renderColor(BufferedImage image, int columns) {
        int rows = Math.max(1, (int) Math.round((double) image.getHeight() / image.getWidth() * columns) / 2);
        StringBuilder out = new StringBuilder();
        for (int row = 0; row < rows; row++) {
            for (int col = 0; col < columns; col++) {
                int x = Math.min(image.getWidth() - 1, col * image.getWidth() / columns);
                int topY = Math.min(image.getHeight() - 1, row * 2 * image.getHeight() / (rows * 2));
                int bottomY = Math.min(image.getHeight() - 1, (row * 2 + 1) * image.getHeight() / (rows * 2));
                int top = image.getRGB(x, topY);
                int bottom = image.getRGB(x, bottomY);
                out.append("\u001b[38;2;").append(red(top)).append(';').append(green(top)).append(';')
                        .append(blue(top)).append("m");
                out.append("\u001b[48;2;").append(red(bottom)).append(';').append(green(bottom)).append(';')
                        .append(blue(bottom)).append("m");
                out.append(HALF_BLOCK);
            }
            out.append("\u001b[0m\n");
        }
        return out.toString();
    }

    /**
     * Renders without color, using a density ramp.
     */
    public static String renderMono(BufferedImage image, int columns) {
        int rows = Math.max(1, (int) Math.round((double) image.getHeight() / image.getWidth() * columns) / 2);
        StringBuilder out = new StringBuilder();
        for (int row = 0; row < rows; row++) {
            for (int col = 0; col < columns; col++) {
                int x = Math.min(image.getWidth() - 1, col * image.getWidth() / columns);
                int topY = Math.min(image.getHeight() - 1, row * 2 * image.getHeight() / (rows * 2));
                int bottomY = Math.min(image.getHeight() - 1, (row * 2 + 1) * image.getHeight() / (rows * 2));
                double luminance = (luminance(image.getRGB(x, topY)) + luminance(image.getRGB(x, bottomY))) / 2.0;
                int index = (int) Math.round(luminance * (RAMP.length() - 1));
                out.append(RAMP.charAt(Math.max(0, Math.min(RAMP.length() - 1, index))));
            }
            out.append('\n');
        }
        return out.toString();
    }

    private static double luminance(int argb) {
        int alpha = (argb >>> 24) & 0xFF;
        double r = ((argb >> 16) & 0xFF) / 255.0;
        double g = ((argb >> 8) & 0xFF) / 255.0;
        double b = (argb & 0xFF) / 255.0;
        return (0.2126 * r + 0.7152 * g + 0.0722 * b) * (alpha / 255.0);
    }

    private static int red(int argb) {
        return (argb >> 16) & 0xFF;
    }

    private static int green(int argb) {
        return (argb >> 8) & 0xFF;
    }

    private static int blue(int argb) {
        return argb & 0xFF;
    }
}
