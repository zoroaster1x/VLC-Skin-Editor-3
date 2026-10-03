package io.github.zoroaster1x.vlcskin.edit;

import io.github.zoroaster1x.vlcskin.model.SkinIndex;
import io.github.zoroaster1x.vlcskin.model.SkinLayout;
import io.github.zoroaster1x.vlcskin.model.SkinWindow;
import io.github.zoroaster1x.vlcskin.model.item.Item;
import io.github.zoroaster1x.vlcskin.model.resource.Resource;

/**
 * What the user currently has focused. Ids survive document rebuilds.
 */
public final class SelectionState {

    private String windowId;
    private String layoutId;
    private String itemId;
    private String resourceId;

    public String windowId() {
        return windowId;
    }

    public String layoutId() {
        return layoutId;
    }

    public String itemId() {
        return itemId;
    }

    public String resourceId() {
        return resourceId;
    }

    public void selectWindow(String id) {
        windowId = id;
        layoutId = null;
        itemId = null;
    }

    public void selectLayout(String windowId, String layoutId) {
        if (!java.util.Objects.equals(this.windowId, windowId)
                || !java.util.Objects.equals(this.layoutId, layoutId)) {
            this.windowId = windowId;
            this.layoutId = layoutId;
            this.itemId = null;
        }
    }

    public void selectItem(String id) {
        itemId = id;
        resourceId = null;
    }

    public void selectResource(String id) {
        resourceId = id;
        itemId = null;
    }

    public void clearItem() {
        itemId = null;
    }

    public SkinWindow window(SkinIndex index) {
        return index.findWindow(windowId);
    }

    public SkinLayout layout(SkinIndex index) {
        SkinWindow window = window(index);
        return window == null ? null : index.findLayout(window, layoutId);
    }

    public Item item(SkinIndex index) {
        return index.findItem(itemId);
    }

    public Resource resource(SkinIndex index) {
        return index.findResource(resourceId);
    }
}
