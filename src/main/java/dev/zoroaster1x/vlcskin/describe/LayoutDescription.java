package dev.zoroaster1x.vlcskin.describe;

import java.util.List;
import java.util.Map;

/**
 * A complete machine readable description of one rendered layout.
 */
public record LayoutDescription(
        String windowId,
        String layoutId,
        int layoutWidth,
        int layoutHeight,
        int zoom,
        List<GeometryNode> items,
        Map<String, Boolean> variables,
        Map<String, String> texts,
        float sliderValue,
        List<String> problems) {
}
