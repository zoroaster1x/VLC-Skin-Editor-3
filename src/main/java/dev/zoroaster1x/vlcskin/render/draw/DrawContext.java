package dev.zoroaster1x.vlcskin.render.draw;

import dev.zoroaster1x.vlcskin.model.SkinIndex;
import dev.zoroaster1x.vlcskin.render.ImageStore;
import dev.zoroaster1x.vlcskin.render.RenderOptions;

/**
 * What every item drawer needs.
 */
public record DrawContext(SkinIndex index, ImageStore images, RenderOptions options) {

    public float zoom() {
        return options.zoom();
    }
}
