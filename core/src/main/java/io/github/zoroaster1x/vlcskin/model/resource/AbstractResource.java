package io.github.zoroaster1x.vlcskin.model.resource;

import io.github.zoroaster1x.vlcskin.model.SkinNode;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;

/**
 * Shared identity of a resource.
 */
@Getter
@Setter
@EqualsAndHashCode(callSuper = false)
public abstract sealed class AbstractResource extends SkinNode implements Resource
        permits BitmapResource, FontResource, BitmapFontResource, PopupMenuResource, IniFileResource {

    private String id;
}
