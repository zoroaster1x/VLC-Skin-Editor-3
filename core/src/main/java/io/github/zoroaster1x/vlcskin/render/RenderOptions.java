package io.github.zoroaster1x.vlcskin.render;

import io.github.zoroaster1x.vlcskin.model.item.Item;

/**
 * What the preview should draw and highlight.
 */
public record RenderOptions(
        int zoom,
        PreviewVariables variables,
        Item selection,
        Item hover,
        Item pressed,
        boolean checkerboard,
        boolean selectionOverlays,
        boolean anchorHelpers,
        int frameTick) {

    public static RenderOptions of(PreviewVariables variables) {
        return new RenderOptions(1, variables, null, null, null, true, true, true, 0);
    }

    public RenderOptions withZoom(int newZoom) {
        return new RenderOptions(newZoom, variables, selection, hover, pressed, checkerboard, selectionOverlays,
                anchorHelpers, frameTick);
    }

    public RenderOptions withSelection(Item newSelection) {
        return new RenderOptions(zoom, variables, newSelection, hover, pressed, checkerboard, selectionOverlays,
                anchorHelpers, frameTick);
    }

    public RenderOptions withHover(Item newHover) {
        return new RenderOptions(zoom, variables, selection, newHover, pressed, checkerboard, selectionOverlays,
                anchorHelpers, frameTick);
    }

    public RenderOptions withPressed(Item newPressed) {
        return new RenderOptions(zoom, variables, selection, hover, newPressed, checkerboard, selectionOverlays,
                anchorHelpers, frameTick);
    }

    public RenderOptions withCheckerboard(boolean enabled) {
        return new RenderOptions(zoom, variables, selection, hover, pressed, enabled, selectionOverlays,
                anchorHelpers, frameTick);
    }

    public RenderOptions withFrameTick(int newTick) {
        return new RenderOptions(zoom, variables, selection, hover, pressed, checkerboard, selectionOverlays,
                anchorHelpers, newTick);
    }

    public RenderOptions withoutCheckerboard() {
        return new RenderOptions(zoom, variables, selection, hover, pressed, false, selectionOverlays, anchorHelpers,
                frameTick);
    }

    public RenderOptions withoutOverlays() {
        return new RenderOptions(zoom, variables, selection, hover, pressed, checkerboard, false, anchorHelpers,
                frameTick);
    }
}
