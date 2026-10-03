package dev.zoroaster1x.vlcskin.describe;

import java.util.Map;

/**
 * One item of a layout as data. A language model without vision can read the
 * positions, sizes, order and attributes and reason about the layout.
 */
public record GeometryNode(
        String id,
        String type,
        String element,
        String parentId,
        int depth,
        int z,
        int x,
        int y,
        int width,
        int height,
        boolean visible,
        String text,
        Map<String, String> attributes) {
}
