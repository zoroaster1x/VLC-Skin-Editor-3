package dev.zoroaster1x.vlcskin.render;

import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * A port of VLC's bezier utility: the curve is sampled at 1024 percentages and
 * only pixel changes are stored, so lookups and hit tests need no polynomial
 * evaluation at draw time. Coordinates round the same way VLC rounds them.
 */
public final class BezierPath {

    public static final int MAX_SAMPLES = 1023;

    private static final Pattern POINT_PATTERN =
            Pattern.compile("\\(\\s*(-?\\d+)\\s*,\\s*(-?\\d+)\\s*\\)");

    private final int[] controlX;
    private final int[] controlY;
    private final float[] percents;
    private final int[] pathX;
    private final int[] pathY;
    private final int pathCount;

    public BezierPath(int[] controlX, int[] controlY) {
        if (controlX.length == 0 || controlX.length != controlY.length) {
            throw new IllegalArgumentException("control points must be non empty and paired");
        }
        this.controlX = controlX.clone();
        this.controlY = controlY.clone();

        int max = MAX_SAMPLES + 2;
        float[] samples = new float[max];
        int[] xs = new int[max];
        int[] ys = new int[max];
        int count = 0;

        float[] factorials = new float[controlX.length];
        factorials[0] = 1;
        for (int i = 1; i < controlX.length; i++) {
            factorials[i] = i * factorials[i - 1];
        }

        Point2D.Float first = computePoint(0f, factorials);
        xs[count] = (int) Math.rint(first.x);
        ys[count] = (int) Math.rint(first.y);
        samples[count] = 0f;
        count++;

        int oldX = xs[0];
        int oldY = ys[0];
        for (int j = 1; j <= MAX_SAMPLES; j++) {
            float percentage = (float) j / MAX_SAMPLES;
            Point2D.Float point = computePoint(percentage, factorials);
            int cx = (int) Math.rint(point.x);
            int cy = (int) Math.rint(point.y);
            if (cx != oldX || cy != oldY) {
                samples[count] = percentage;
                xs[count] = cx;
                ys[count] = cy;
                count++;
                oldX = cx;
                oldY = cy;
            }
        }
        if (count == 1) {
            samples[count] = 1f;
            xs[count] = xs[0];
            ys[count] = ys[0];
            count++;
        }
        samples[count - 1] = 1f;

        this.percents = new float[count];
        this.pathX = new int[count];
        this.pathY = new int[count];
        System.arraycopy(samples, 0, percents, 0, count);
        System.arraycopy(xs, 0, pathX, 0, count);
        System.arraycopy(ys, 0, pathY, 0, count);
        this.pathCount = count;
    }

    private Point2D.Float computePoint(float t, float[] factorials) {
        float x = 0f;
        float y = 0f;
        int n = controlX.length - 1;
        for (int i = 0; i < controlX.length; i++) {
            float coefficient = (float) (Math.pow(t, i) * Math.pow(1 - t, n - i))
                    * (factorials[n] / factorials[i] / factorials[n - i]);
            x += controlX[i] * coefficient;
            y += controlY[i] * coefficient;
        }
        return new Point2D.Float(x, y);
    }

    /**
     * The sample nearest to the given percentage, like VLC's getPoint.
     */
    public Point2D.Float pointAt(float t) {
        int reference = 0;
        float minDiff = Math.abs(percents[0] - t);
        while (reference < pathCount) {
            float diff = Math.abs(percents[reference] - t);
            if (diff > minDiff) {
                break;
            }
            reference++;
            minDiff = diff;
        }
        int index = Math.min(Math.max(reference - 1, 0), pathCount - 1);
        return new Point2D.Float(pathX[index], pathY[index]);
    }

    /**
     * Nearest stored sample to a point, used for slider clicking.
     */
    public int nearestIndex(int x, int y) {
        int reference = 0;
        long minDist = Long.MAX_VALUE;
        for (int i = 0; i < pathCount; i++) {
            long dx = pathX[i] - x;
            long dy = pathY[i] - y;
            long dist = dx * dx + dy * dy;
            if (dist < minDist) {
                minDist = dist;
                reference = i;
            }
        }
        return reference;
    }

    public float percentAtIndex(int index) {
        return percents[Math.max(0, Math.min(index, pathCount - 1))];
    }

    public int width() {
        int width = 0;
        for (int i = 0; i < pathCount; i++) {
            if (pathX[i] >= width) {
                width = pathX[i] + 1;
            }
        }
        return width;
    }

    public int height() {
        int height = 0;
        for (int i = 0; i < pathCount; i++) {
            if (pathY[i] >= height) {
                height = pathY[i] + 1;
            }
        }
        return height;
    }

    public int controlCount() {
        return controlX.length;
    }

    public int controlX(int index) {
        return controlX[index];
    }

    public int controlY(int index) {
        return controlY[index];
    }

    public int sampleCount() {
        return pathCount;
    }

    /**
     * The stored path as segments for drawing the selection guide.
     */
    public List<Point2D.Float> samplePath(int wantedSamples) {
        List<Point2D.Float> points = new ArrayList<>();
        int samples = Math.max(2, wantedSamples);
        for (int i = 0; i <= samples; i++) {
            points.add(pointAt((float) i / samples));
        }
        return points;
    }

    /**
     * Parses "(x,y),(x,y)" and tolerates extra whitespace or separators.
     */
    public static BezierPath parse(String text) {
        if (text == null || text.isBlank()) {
            throw new IllegalArgumentException("empty points attribute");
        }
        Matcher matcher = POINT_PATTERN.matcher(text);
        List<Integer> xs = new ArrayList<>();
        List<Integer> ys = new ArrayList<>();
        while (matcher.find()) {
            xs.add(Integer.parseInt(matcher.group(1)));
            ys.add(Integer.parseInt(matcher.group(2)));
        }
        if (xs.isEmpty()) {
            throw new IllegalArgumentException("no points found in \"" + text + "\"");
        }
        int[] x = new int[xs.size()];
        int[] y = new int[ys.size()];
        for (int i = 0; i < x.length; i++) {
            x[i] = xs.get(i);
            y[i] = ys.get(i);
        }
        return new BezierPath(x, y);
    }

    public static String format(int[] xs, int[] ys) {
        StringBuilder builder = new StringBuilder();
        for (int i = 0; i < xs.length; i++) {
            if (i > 0) {
                builder.append(',');
            }
            builder.append('(').append(xs[i]).append(',').append(ys[i]).append(')');
        }
        return builder.toString();
    }
}
