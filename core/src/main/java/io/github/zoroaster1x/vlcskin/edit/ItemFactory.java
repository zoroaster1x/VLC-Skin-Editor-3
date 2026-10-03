package io.github.zoroaster1x.vlcskin.edit;

import io.github.zoroaster1x.vlcskin.model.ItemType;
import io.github.zoroaster1x.vlcskin.model.SkinIndex;
import io.github.zoroaster1x.vlcskin.model.item.AbstractItem;
import io.github.zoroaster1x.vlcskin.model.item.AnchorItem;
import io.github.zoroaster1x.vlcskin.model.item.ButtonItem;
import io.github.zoroaster1x.vlcskin.model.item.CheckboxItem;
import io.github.zoroaster1x.vlcskin.model.item.GroupItem;
import io.github.zoroaster1x.vlcskin.model.item.ImageItem;
import io.github.zoroaster1x.vlcskin.model.item.PanelItem;
import io.github.zoroaster1x.vlcskin.model.item.PlaytreeItem;
import io.github.zoroaster1x.vlcskin.model.item.RadialSliderItem;
import io.github.zoroaster1x.vlcskin.model.item.SliderBackground;
import io.github.zoroaster1x.vlcskin.model.item.SliderItem;
import io.github.zoroaster1x.vlcskin.model.item.TextItem;
import io.github.zoroaster1x.vlcskin.model.item.VideoItem;

/**
 * Creates new items with sane defaults and unique ids.
 */
public final class ItemFactory {

    private ItemFactory() {
    }

    public static AbstractItem create(ItemType type, SkinIndex index) {
        AbstractItem item = switch (type) {
            case ANCHOR -> {
                AnchorItem anchor = new AnchorItem();
                anchor.setRange(10);
                yield anchor;
            }
            case BUTTON -> {
                ButtonItem button = new ButtonItem();
                button.setUp("none");
                yield button;
            }
            case CHECKBOX -> {
                CheckboxItem checkbox = new CheckboxItem();
                checkbox.setUp1("none");
                checkbox.setUp2("none");
                yield checkbox;
            }
            case GROUP -> new GroupItem();
            case IMAGE -> {
                ImageItem image = new ImageItem();
                image.setImage("none");
                yield image;
            }
            case PANEL -> {
                PanelItem panel = new PanelItem();
                panel.setWidth(100);
                panel.setHeight(50);
                yield panel;
            }
            case PLAYLIST, PLAYTREE -> {
                PlaytreeItem playtree = new PlaytreeItem();
                playtree.setPlaylistSyntax(type == ItemType.PLAYLIST);
                playtree.setWidth(150);
                playtree.setHeight(100);
                SliderItem slider = new SliderItem();
                slider.setUp("none");
                slider.setInPlaytree(true);
                slider.setId(index.uniqueUnnamed("Slider"));
                playtree.setSlider(slider);
                yield playtree;
            }
            case RADIAL_SLIDER -> {
                RadialSliderItem radial = new RadialSliderItem();
                radial.setSequence("none");
                radial.setNbimages(1);
                yield radial;
            }
            case SLIDER -> {
                SliderItem slider = new SliderItem();
                slider.setUp("none");
                yield slider;
            }
            case SLIDER_BACKGROUND -> {
                SliderBackground background = new SliderBackground();
                background.setImage("none");
                yield background;
            }
            case TEXT -> {
                TextItem text = new TextItem();
                text.setText("Text");
                text.setFont("defaultfont");
                yield text;
            }
            case VIDEO -> {
                VideoItem video = new VideoItem();
                video.setWidth(320);
                video.setHeight(240);
                yield video;
            }
        };
        item.setId(index.uniqueUnnamed(type.displayName()));
        return item;
    }
}
