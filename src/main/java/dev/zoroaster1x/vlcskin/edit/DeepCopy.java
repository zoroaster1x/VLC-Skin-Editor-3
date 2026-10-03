package dev.zoroaster1x.vlcskin.edit;

import dev.zoroaster1x.vlcskin.model.SkinIndex;
import dev.zoroaster1x.vlcskin.model.SkinLayout;
import dev.zoroaster1x.vlcskin.model.SkinWindow;
import dev.zoroaster1x.vlcskin.model.item.AbstractItem;
import dev.zoroaster1x.vlcskin.model.item.AnchorItem;
import dev.zoroaster1x.vlcskin.model.item.ButtonItem;
import dev.zoroaster1x.vlcskin.model.item.CheckboxItem;
import dev.zoroaster1x.vlcskin.model.item.GroupItem;
import dev.zoroaster1x.vlcskin.model.item.ImageItem;
import dev.zoroaster1x.vlcskin.model.item.Item;
import dev.zoroaster1x.vlcskin.model.item.PanelItem;
import dev.zoroaster1x.vlcskin.model.item.PlaytreeItem;
import dev.zoroaster1x.vlcskin.model.item.RadialSliderItem;
import dev.zoroaster1x.vlcskin.model.item.SliderBackground;
import dev.zoroaster1x.vlcskin.model.item.SliderItem;
import dev.zoroaster1x.vlcskin.model.item.TextItem;
import dev.zoroaster1x.vlcskin.model.item.VideoItem;
import dev.zoroaster1x.vlcskin.model.resource.BitmapResource;
import dev.zoroaster1x.vlcskin.model.resource.BitmapFontResource;
import dev.zoroaster1x.vlcskin.model.resource.FontResource;
import dev.zoroaster1x.vlcskin.model.resource.IniFileResource;
import dev.zoroaster1x.vlcskin.model.resource.MenuItemEntry;
import dev.zoroaster1x.vlcskin.model.resource.MenuSeparatorEntry;
import dev.zoroaster1x.vlcskin.model.resource.PopupMenuResource;
import dev.zoroaster1x.vlcskin.model.resource.Resource;

/**
 * Deep copies with fresh ids, used by duplicate and drag-copy.
 */
public final class DeepCopy {

    public static final String COPY_PATTERN = "%oldid%_copy";

    private DeepCopy() {
    }

    public static Item item(Item source, SkinIndex index) {
        return item(source, index, COPY_PATTERN);
    }

    /**
     * Deep copies an item with a rename pattern such as {@code %oldid%_copy}.
     */
    public static Item item(Item source, SkinIndex index, String pattern) {
        AbstractItem copy = copyShallow(source, index, pattern);
        if (copy instanceof GroupItem group && source instanceof GroupItem sourceGroup) {
            for (Item child : sourceGroup.getItems()) {
                group.getItems().add(item(child, index, pattern));
            }
        } else if (copy instanceof PanelItem panel && source instanceof PanelItem sourcePanel) {
            for (Item child : sourcePanel.getItems()) {
                panel.getItems().add(item(child, index, pattern));
            }
        } else if (copy instanceof PlaytreeItem playtree && source instanceof PlaytreeItem sourcePlaytree) {
            if (sourcePlaytree.getSlider() != null) {
                playtree.setSlider((SliderItem) item(sourcePlaytree.getSlider(), index, pattern));
            }
        } else if (copy instanceof SliderItem slider && source instanceof SliderItem sourceSlider) {
            if (sourceSlider.getBackground() != null) {
                slider.setBackground((SliderBackground) item(sourceSlider.getBackground(), index, pattern));
            }
        }
        AbstractItem abstractSource = (AbstractItem) source;
        abstractSource.foreignAttributes().forEach(copy::preserveAttribute);
        abstractSource.unknownChildren().forEach(copy::preserveChild);
        return copy;
    }

    private static AbstractItem copyShallow(Item source, SkinIndex index, String pattern) {
        return switch (source) {
            case AnchorItem item -> {
                AnchorItem copy = new AnchorItem();
                base(item, copy, index, pattern);
                copy.setPoints(item.getPoints());
                copy.setPriority(item.getPriority());
                copy.setRange(item.getRange());
                yield copy;
            }
            case ButtonItem item -> {
                ButtonItem copy = new ButtonItem();
                base(item, copy, index, pattern);
                copy.setUp(item.getUp());
                copy.setDown(item.getDown());
                copy.setOver(item.getOver());
                copy.setAction(item.getAction());
                copy.setTooltiptext(item.getTooltiptext());
                yield copy;
            }
            case CheckboxItem item -> {
                CheckboxItem copy = new CheckboxItem();
                base(item, copy, index, pattern);
                copy.setState(item.getState());
                copy.setUp1(item.getUp1());
                copy.setDown1(item.getDown1());
                copy.setOver1(item.getOver1());
                copy.setAction1(item.getAction1());
                copy.setTooltiptext1(item.getTooltiptext1());
                copy.setUp2(item.getUp2());
                copy.setDown2(item.getDown2());
                copy.setOver2(item.getOver2());
                copy.setAction2(item.getAction2());
                copy.setTooltiptext2(item.getTooltiptext2());
                yield copy;
            }
            case GroupItem item -> {
                GroupItem copy = new GroupItem();
                base(item, copy, index, pattern);
                yield copy;
            }
            case ImageItem item -> {
                ImageItem copy = new ImageItem();
                base(item, copy, index, pattern);
                copy.setImage(item.getImage());
                copy.setAction(item.getAction());
                copy.setAction2(item.getAction2());
                copy.setResize(item.getResize());
                copy.setArt(item.isArt());
                yield copy;
            }
            case PanelItem item -> {
                PanelItem copy = new PanelItem();
                base(item, copy, index, pattern);
                copy.setWidth(item.getWidth());
                copy.setHeight(item.getHeight());
                yield copy;
            }
            case PlaytreeItem item -> {
                PlaytreeItem copy = new PlaytreeItem();
                base(item, copy, index, pattern);
                copy.setWidth(item.getWidth());
                copy.setHeight(item.getHeight());
                copy.setFont(item.getFont());
                copy.setBgimage(item.getBgimage());
                copy.setItemimage(item.getItemimage());
                copy.setOpenimage(item.getOpenimage());
                copy.setClosedimage(item.getClosedimage());
                copy.setFgcolor(item.getFgcolor());
                copy.setPlaycolor(item.getPlaycolor());
                copy.setSelcolor(item.getSelcolor());
                copy.setBgcolor1(item.getBgcolor1());
                copy.setBgcolor2(item.getBgcolor2());
                copy.setFlat(item.isFlat());
                copy.setPlaylistSyntax(item.isPlaylistSyntax());
                yield copy;
            }
            case RadialSliderItem item -> {
                RadialSliderItem copy = new RadialSliderItem();
                base(item, copy, index, pattern);
                copy.setSequence(item.getSequence());
                copy.setNbimages(item.getNbimages());
                copy.setMinangle(item.getMinangle());
                copy.setMaxangle(item.getMaxangle());
                copy.setValue(item.getValue());
                copy.setTooltiptext(item.getTooltiptext());
                yield copy;
            }
            case SliderItem item -> {
                SliderItem copy = new SliderItem();
                base(item, copy, index, pattern);
                copy.setUp(item.getUp());
                copy.setDown(item.getDown());
                copy.setOver(item.getOver());
                copy.setPoints(item.getPoints());
                copy.setThickness(item.getThickness());
                copy.setValue(item.getValue());
                copy.setTooltiptext(item.getTooltiptext());
                copy.setInPlaytree(item.isInPlaytree());
                yield copy;
            }
            case SliderBackground item -> {
                SliderBackground copy = new SliderBackground();
                base(item, copy, index, pattern);
                copy.setImage(item.getImage());
                copy.setNbhoriz(item.getNbhoriz());
                copy.setNbvert(item.getNbvert());
                copy.setPadhoriz(item.getPadhoriz());
                copy.setPadvert(item.getPadvert());
                yield copy;
            }
            case TextItem item -> {
                TextItem copy = new TextItem();
                base(item, copy, index, pattern);
                copy.setText(item.getText());
                copy.setFont(item.getFont());
                copy.setColor(item.getColor());
                copy.setWidth(item.getWidth());
                copy.setAlignment(item.getAlignment());
                copy.setScrolling(item.getScrolling());
                yield copy;
            }
            case VideoItem item -> {
                VideoItem copy = new VideoItem();
                base(item, copy, index, pattern);
                copy.setWidth(item.getWidth());
                copy.setHeight(item.getHeight());
                copy.setAutoresize(item.isAutoresize());
                yield copy;
            }
        };
    }

    private static void base(Item source, dev.zoroaster1x.vlcskin.model.item.AbstractItem copy,
                             SkinIndex index, String pattern) {
        copy.setId(index.uniqueCopy(pattern, source.getId()));
        copy.setVisible(source instanceof dev.zoroaster1x.vlcskin.model.item.AbstractItem abstractItem
                ? abstractItem.getVisible() : "true");
        copy.setX(source.getX());
        copy.setY(source.getY());
        if (source instanceof dev.zoroaster1x.vlcskin.model.item.AbstractItem abstractItem) {
            copy.setLefttop(abstractItem.getLefttop());
            copy.setRightbottom(abstractItem.getRightbottom());
            copy.setXkeepratio(abstractItem.isXkeepratio());
            copy.setYkeepratio(abstractItem.isYkeepratio());
            copy.setHelp(abstractItem.getHelp());
        }
    }

    public static Resource resource(Resource source, SkinIndex index) {
        return resource(source, index, COPY_PATTERN);
    }

    /**
     * Deep copies one sub bitmap with a rename pattern.
     */
    public static dev.zoroaster1x.vlcskin.model.resource.SubBitmap subBitmap(
            dev.zoroaster1x.vlcskin.model.resource.SubBitmap source, SkinIndex index, String pattern) {
        var copy = source.copy();
        copy.setId(index.uniqueCopy(pattern, source.getId()));
        return copy;
    }

    /**
     * Deep copies a resource with a rename pattern.
     */
    public static Resource resource(Resource source, SkinIndex index, String pattern) {
        Resource copy = switch (source) {
            case BitmapResource bitmap -> {
                BitmapResource clone = bitmap.copy();
                clone.setId(index.uniqueCopy(pattern, bitmap.getId()));
                for (int i = 0; i < clone.getSubBitmaps().size(); i++) {
                    clone.getSubBitmaps().get(i).setId(
                            index.uniqueCopy(pattern, bitmap.getSubBitmaps().get(i).getId()));
                }
                yield clone;
            }
            case FontResource font -> {
                FontResource clone = font.copy();
                clone.setId(index.uniqueCopy(pattern, font.getId()));
                yield clone;
            }
            case BitmapFontResource font -> {
                BitmapFontResource clone = font.copy();
                clone.setId(index.uniqueCopy(pattern, font.getId()));
                yield clone;
            }
            case PopupMenuResource menu -> {
                PopupMenuResource clone = menu.copy();
                clone.setId(index.uniqueCopy(pattern, menu.getId()));
                yield clone;
            }
            case IniFileResource ini -> {
                IniFileResource clone = ini.copy();
                clone.setId(index.uniqueCopy(pattern, ini.getId()));
                yield clone;
            }
        };
        return copy;
    }

    public static SkinLayout layout(SkinLayout source, SkinIndex index) {
        return layout(source, index, COPY_PATTERN);
    }

    /**
     * Deep copies a layout with a rename pattern.
     */
    public static SkinLayout layout(SkinLayout source, SkinIndex index, String pattern) {
        SkinLayout copy = new SkinLayout();
        copy.setId(index.uniqueCopy(pattern, source.getId()));
        copy.setWidth(source.getWidth());
        copy.setHeight(source.getHeight());
        copy.setMinwidth(source.getMinwidth());
        copy.setMaxwidth(source.getMaxwidth());
        copy.setMinheight(source.getMinheight());
        copy.setMaxheight(source.getMaxheight());
        for (Item item : source.getItems()) {
            copy.getItems().add(item(item, index, pattern));
        }
        source.foreignAttributes().forEach(copy::preserveAttribute);
        source.unknownChildren().forEach(copy::preserveChild);
        return copy;
    }

    public static SkinWindow window(SkinWindow source, SkinIndex index) {
        return window(source, index, COPY_PATTERN);
    }

    /**
     * Deep copies a window with a rename pattern.
     */
    public static SkinWindow window(SkinWindow source, SkinIndex index, String pattern) {
        SkinWindow copy = new SkinWindow();
        copy.setId(index.uniqueCopy(pattern, source.getId()));
        copy.setVisible(source.getVisible());
        copy.setX(source.getX());
        copy.setY(source.getY());
        copy.setDragdrop(source.isDragdrop());
        copy.setPlayondrop(source.isPlayondrop());
        for (SkinLayout layout : source.getLayouts()) {
            copy.getLayouts().add(layout(layout, index, pattern));
        }
        source.foreignAttributes().forEach(copy::preserveAttribute);
        source.unknownChildren().forEach(copy::preserveChild);
        return copy;
    }

    /**
     * Copies a menu entry; ids of menu items are not part of the format.
     */
    public static PopupMenuResource.MenuEntry menuEntry(PopupMenuResource.MenuEntry source) {
        return switch (source) {
            case MenuItemEntry item -> {
                MenuItemEntry copy = new MenuItemEntry();
                copy.setLabel(item.getLabel());
                copy.setAction(item.getAction());
                yield copy;
            }
            case MenuSeparatorEntry separator -> new MenuSeparatorEntry();
        };
    }
}
