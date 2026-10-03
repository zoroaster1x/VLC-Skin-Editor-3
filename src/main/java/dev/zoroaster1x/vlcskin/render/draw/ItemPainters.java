package dev.zoroaster1x.vlcskin.render.draw;

import dev.zoroaster1x.vlcskin.model.item.AnchorItem;
import dev.zoroaster1x.vlcskin.model.item.ButtonItem;
import dev.zoroaster1x.vlcskin.model.item.CheckboxItem;
import dev.zoroaster1x.vlcskin.model.item.GroupItem;
import dev.zoroaster1x.vlcskin.model.item.ImageItem;
import dev.zoroaster1x.vlcskin.model.item.PanelItem;
import dev.zoroaster1x.vlcskin.model.item.PlaytreeItem;
import dev.zoroaster1x.vlcskin.model.item.RadialSliderItem;
import dev.zoroaster1x.vlcskin.model.item.SliderItem;
import dev.zoroaster1x.vlcskin.model.item.TextItem;
import dev.zoroaster1x.vlcskin.model.item.VideoItem;
import java.awt.Graphics2D;

/**
 * Dispatch from an item to its drawer.
 */
public final class ItemPainters {

    private static final ImageDrawer IMAGE = new ImageDrawer();
    private static final ButtonDrawer BUTTON = new ButtonDrawer();
    private static final CheckboxDrawer CHECKBOX = new CheckboxDrawer();
    private static final TextDrawer TEXT = new TextDrawer();
    private static final SliderDrawer SLIDER = new SliderDrawer();
    private static final RadialSliderDrawer RADIAL = new RadialSliderDrawer();
    private static final PlaytreeDrawer PLAYTREE = new PlaytreeDrawer();
    private static final VideoDrawer VIDEO = new VideoDrawer();
    private static final AnchorDrawer ANCHOR = new AnchorDrawer();
    private static final ContainerDrawer CONTAINER = new ContainerDrawer();

    private ItemPainters() {
    }

    public static void draw(Graphics2D g, dev.zoroaster1x.vlcskin.model.item.Item item,
                            int offsetX, int offsetY, DrawContext context) {
        switch (item) {
            case ImageItem image -> IMAGE.draw(g, image, offsetX, offsetY, context);
            case ButtonItem button -> BUTTON.draw(g, button, offsetX, offsetY, context);
            case CheckboxItem checkbox -> CHECKBOX.draw(g, checkbox, offsetX, offsetY, context);
            case TextItem text -> TEXT.draw(g, text, offsetX, offsetY, context);
            case SliderItem slider -> SLIDER.draw(g, slider, offsetX, offsetY, context);
            case RadialSliderItem radial -> RADIAL.draw(g, radial, offsetX, offsetY, context);
            case PlaytreeItem playtree -> PLAYTREE.draw(g, playtree, offsetX, offsetY, context);
            case VideoItem video -> VIDEO.draw(g, video, offsetX, offsetY, context);
            case AnchorItem anchor -> ANCHOR.draw(g, anchor, offsetX, offsetY, context);
            case GroupItem group -> CONTAINER.draw(g, group, offsetX, offsetY, context);
            case PanelItem panel -> CONTAINER.draw(g, panel, offsetX, offsetY, context);
            case dev.zoroaster1x.vlcskin.model.item.SliderBackground background ->
                    SLIDER.drawBackground(g, background, offsetX, offsetY, context);
        }
    }
}
