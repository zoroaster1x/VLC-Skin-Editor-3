package io.github.zoroaster1x.vlcskin.render.draw;

import io.github.zoroaster1x.vlcskin.model.SkinIndex;
import io.github.zoroaster1x.vlcskin.render.ImageStore;
import io.github.zoroaster1x.vlcskin.render.RenderOptions;

/**
 * What every item drawer needs.
 */
public record DrawContext(SkinIndex index, ImageStore images, RenderOptions options) {

    public float zoom() {
        return options.zoom();
    }
}
