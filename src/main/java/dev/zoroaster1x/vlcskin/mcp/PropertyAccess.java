package dev.zoroaster1x.vlcskin.mcp;

import dev.zoroaster1x.vlcskin.model.item.AbstractItem;
import dev.zoroaster1x.vlcskin.model.item.AnchorItem;
import dev.zoroaster1x.vlcskin.model.item.ButtonItem;
import dev.zoroaster1x.vlcskin.model.item.CheckboxItem;
import dev.zoroaster1x.vlcskin.model.item.ImageItem;
import dev.zoroaster1x.vlcskin.model.item.PanelItem;
import dev.zoroaster1x.vlcskin.model.item.PlaytreeItem;
import dev.zoroaster1x.vlcskin.model.item.RadialSliderItem;
import dev.zoroaster1x.vlcskin.model.item.SliderItem;
import dev.zoroaster1x.vlcskin.model.item.TextItem;
import dev.zoroaster1x.vlcskin.model.item.VideoItem;
import dev.zoroaster1x.vlcskin.model.resource.BitmapFontResource;
import dev.zoroaster1x.vlcskin.model.resource.BitmapResource;
import dev.zoroaster1x.vlcskin.model.resource.FontResource;
import dev.zoroaster1x.vlcskin.model.resource.IniFileResource;
import dev.zoroaster1x.vlcskin.model.resource.PopupMenuResource;
import dev.zoroaster1x.vlcskin.model.resource.Resource;
import dev.zoroaster1x.vlcskin.model.resource.SubBitmap;
import java.util.List;
import java.util.Locale;

/**
 * Reads and writes item and resource attributes by name, for AI and the inspector.
 */
public final class PropertyAccess {

    private PropertyAccess() {
    }

    /**
     * Applies one attribute; returns the previous value or null when unknown.
     */
    public static String setItem(AbstractItem item, String name, String value) {
        String key = name.toLowerCase(Locale.ROOT);
        String previous = getItem(item, key);
        if (previous == null) {
            return null;
        }
        switch (key) {
            case "id" -> item.setId(value);
            case "x" -> item.setX(integer(value));
            case "y" -> item.setY(integer(value));
            case "lefttop" -> item.setLefttop(value);
            case "rightbottom" -> item.setRightbottom(value);
            case "xkeepratio" -> item.setXkeepratio(bool(value));
            case "ykeepratio" -> item.setYkeepratio(bool(value));
            case "help" -> item.setHelp(value);
            case "visible" -> item.setVisible(value);
            default -> setTypedItem(item, key, value);
        }
        return previous;
    }

    private static void setTypedItem(AbstractItem item, String key, String value) {
        switch (item) {
            case AnchorItem anchor -> {
                switch (key) {
                    case "points" -> anchor.setPoints(value);
                    case "priority" -> anchor.setPriority(integer(value));
                    case "range" -> anchor.setRange(integer(value));
                    default -> {
                    }
                }
            }
            case ButtonItem button -> {
                switch (key) {
                    case "up" -> button.setUp(value);
                    case "down" -> button.setDown(value);
                    case "over" -> button.setOver(value);
                    case "action" -> button.setAction(value);
                    case "tooltiptext" -> button.setTooltiptext(value);
                    default -> {
                    }
                }
            }
            case CheckboxItem checkbox -> {
                switch (key) {
                    case "state" -> checkbox.setState(value);
                    case "up1" -> checkbox.setUp1(value);
                    case "down1" -> checkbox.setDown1(value);
                    case "over1" -> checkbox.setOver1(value);
                    case "action1" -> checkbox.setAction1(value);
                    case "tooltiptext1" -> checkbox.setTooltiptext1(value);
                    case "up2" -> checkbox.setUp2(value);
                    case "down2" -> checkbox.setDown2(value);
                    case "over2" -> checkbox.setOver2(value);
                    case "action2" -> checkbox.setAction2(value);
                    case "tooltiptext2" -> checkbox.setTooltiptext2(value);
                    default -> {
                    }
                }
            }
            case ImageItem image -> {
                switch (key) {
                    case "image" -> image.setImage(value);
                    case "action" -> image.setAction(value);
                    case "action2" -> image.setAction2(value);
                    case "resize" -> image.setResize(value);
                    case "art" -> image.setArt(bool(value));
                    default -> {
                    }
                }
            }
            case PanelItem panel -> {
                switch (key) {
                    case "width" -> panel.setWidth(integer(value));
                    case "height" -> panel.setHeight(integer(value));
                    default -> {
                    }
                }
            }
            case PlaytreeItem playtree -> {
                switch (key) {
                    case "width" -> playtree.setWidth(integer(value));
                    case "height" -> playtree.setHeight(integer(value));
                    case "font" -> playtree.setFont(value);
                    case "bgimage" -> playtree.setBgimage(value);
                    case "itemimage" -> playtree.setItemimage(value);
                    case "openimage" -> playtree.setOpenimage(value);
                    case "closedimage" -> playtree.setClosedimage(value);
                    case "fgcolor" -> playtree.setFgcolor(value);
                    case "playcolor" -> playtree.setPlaycolor(value);
                    case "selcolor" -> playtree.setSelcolor(value);
                    case "bgcolor1" -> playtree.setBgcolor1(value);
                    case "bgcolor2" -> playtree.setBgcolor2(value);
                    case "flat" -> playtree.setFlat(bool(value));
                    default -> {
                    }
                }
            }
            case RadialSliderItem radial -> {
                switch (key) {
                    case "sequence" -> radial.setSequence(value);
                    case "nbimages" -> radial.setNbimages(integer(value));
                    case "minangle" -> radial.setMinangle(integer(value));
                    case "maxangle" -> radial.setMaxangle(integer(value));
                    case "value" -> radial.setValue(value);
                    case "tooltiptext" -> radial.setTooltiptext(value);
                    default -> {
                    }
                }
            }
            case SliderItem slider -> {
                switch (key) {
                    case "up" -> slider.setUp(value);
                    case "down" -> slider.setDown(value);
                    case "over" -> slider.setOver(value);
                    case "points" -> slider.setPoints(value);
                    case "thickness" -> slider.setThickness(integer(value));
                    case "value" -> slider.setValue(value);
                    case "tooltiptext" -> slider.setTooltiptext(value);
                    default -> {
                    }
                }
            }
            case dev.zoroaster1x.vlcskin.model.item.SliderBackground background -> {
                switch (key) {
                    case "image" -> background.setImage(value);
                    case "nbhoriz" -> background.setNbhoriz(integer(value));
                    case "nbvert" -> background.setNbvert(integer(value));
                    case "padhoriz" -> background.setPadhoriz(integer(value));
                    case "padvert" -> background.setPadvert(integer(value));
                    default -> {
                    }
                }
            }
            case TextItem text -> {
                switch (key) {
                    case "text" -> text.setText(value);
                    case "font" -> text.setFont(value);
                    case "color" -> text.setColor(value);
                    case "width" -> text.setWidth(integer(value));
                    case "alignment" -> text.setAlignment(value);
                    case "scrolling" -> text.setScrolling(value);
                    default -> {
                    }
                }
            }
            case VideoItem video -> {
                switch (key) {
                    case "width" -> video.setWidth(integer(value));
                    case "height" -> video.setHeight(integer(value));
                    case "autoresize" -> video.setAutoresize(bool(value));
                    default -> {
                    }
                }
            }
            default -> {
            }
        }
    }

    /**
     * The current value of an attribute, or null when the name is unknown.
     */
    public static String getItem(AbstractItem item, String name) {
        String key = name.toLowerCase(Locale.ROOT);
        return switch (key) {
            case "id" -> item.getId();
            case "x" -> Integer.toString(item.getX());
            case "y" -> Integer.toString(item.getY());
            case "lefttop" -> item.getLefttop();
            case "rightbottom" -> item.getRightbottom();
            case "xkeepratio" -> Boolean.toString(item.isXkeepratio());
            case "ykeepratio" -> Boolean.toString(item.isYkeepratio());
            case "help" -> item.getHelp();
            case "visible" -> item.getVisible();
            default -> typedItem(item, key);
        };
    }

    private static String typedItem(AbstractItem item, String key) {
        return switch (item) {
            case AnchorItem anchor -> switch (key) {
                case "points" -> anchor.getPoints();
                case "priority" -> Integer.toString(anchor.getPriority());
                case "range" -> Integer.toString(anchor.getRange());
                default -> null;
            };
            case ButtonItem button -> switch (key) {
                case "up" -> button.getUp();
                case "down" -> button.getDown();
                case "over" -> button.getOver();
                case "action" -> button.getAction();
                case "tooltiptext" -> button.getTooltiptext();
                default -> null;
            };
            case CheckboxItem checkbox -> switch (key) {
                case "state" -> checkbox.getState();
                case "up1" -> checkbox.getUp1();
                case "down1" -> checkbox.getDown1();
                case "over1" -> checkbox.getOver1();
                case "action1" -> checkbox.getAction1();
                case "tooltiptext1" -> checkbox.getTooltiptext1();
                case "up2" -> checkbox.getUp2();
                case "down2" -> checkbox.getDown2();
                case "over2" -> checkbox.getOver2();
                case "action2" -> checkbox.getAction2();
                case "tooltiptext2" -> checkbox.getTooltiptext2();
                default -> null;
            };
            case ImageItem image -> switch (key) {
                case "image" -> image.getImage();
                case "action" -> image.getAction();
                case "action2" -> image.getAction2();
                case "resize" -> image.getResize();
                case "art" -> Boolean.toString(image.isArt());
                default -> null;
            };
            case PanelItem panel -> switch (key) {
                case "width" -> Integer.toString(panel.getWidth());
                case "height" -> Integer.toString(panel.getHeight());
                default -> null;
            };
            case PlaytreeItem playtree -> switch (key) {
                case "width" -> Integer.toString(playtree.getWidth());
                case "height" -> Integer.toString(playtree.getHeight());
                case "font" -> playtree.getFont();
                case "bgimage" -> playtree.getBgimage();
                case "itemimage" -> playtree.getItemimage();
                case "openimage" -> playtree.getOpenimage();
                case "closedimage" -> playtree.getClosedimage();
                case "fgcolor" -> playtree.getFgcolor();
                case "playcolor" -> playtree.getPlaycolor();
                case "selcolor" -> playtree.getSelcolor();
                case "bgcolor1" -> playtree.getBgcolor1();
                case "bgcolor2" -> playtree.getBgcolor2();
                case "flat" -> Boolean.toString(playtree.isFlat());
                default -> null;
            };
            case RadialSliderItem radial -> switch (key) {
                case "sequence" -> radial.getSequence();
                case "nbimages" -> Integer.toString(radial.getNbimages());
                case "minangle" -> Integer.toString(radial.getMinangle());
                case "maxangle" -> Integer.toString(radial.getMaxangle());
                case "value" -> radial.getValue();
                case "tooltiptext" -> radial.getTooltiptext();
                default -> null;
            };
            case SliderItem slider -> switch (key) {
                case "up" -> slider.getUp();
                case "down" -> slider.getDown();
                case "over" -> slider.getOver();
                case "points" -> slider.getPoints();
                case "thickness" -> Integer.toString(slider.getThickness());
                case "value" -> slider.getValue();
                case "tooltiptext" -> slider.getTooltiptext();
                default -> null;
            };
            case dev.zoroaster1x.vlcskin.model.item.SliderBackground background -> switch (key) {
                case "image" -> background.getImage();
                case "nbhoriz" -> Integer.toString(background.getNbhoriz());
                case "nbvert" -> Integer.toString(background.getNbvert());
                case "padhoriz" -> Integer.toString(background.getPadhoriz());
                case "padvert" -> Integer.toString(background.getPadvert());
                default -> null;
            };
            case TextItem text -> switch (key) {
                case "text" -> text.getText();
                case "font" -> text.getFont();
                case "color" -> text.getColor();
                case "width" -> Integer.toString(text.getWidth());
                case "alignment" -> text.getAlignment();
                case "scrolling" -> text.getScrolling();
                default -> null;
            };
            case VideoItem video -> switch (key) {
                case "width" -> Integer.toString(video.getWidth());
                case "height" -> Integer.toString(video.getHeight());
                case "autoresize" -> Boolean.toString(video.isAutoresize());
                default -> null;
            };
            default -> null;
        };
    }

    /**
     * Known attribute names for an item, used in error messages and docs.
     */
    public static List<String> itemNames(AbstractItem item) {
        List<String> common = List.of("id", "x", "y", "lefttop", "rightbottom", "xkeepratio", "ykeepratio",
                "help", "visible");
        List<String> typed = switch (item) {
            case AnchorItem ignored -> List.of("points", "priority", "range");
            case ButtonItem ignored -> List.of("up", "down", "over", "action", "tooltiptext");
            case CheckboxItem ignored -> List.of("state", "up1", "down1", "over1", "action1", "tooltiptext1",
                    "up2", "down2", "over2", "action2", "tooltiptext2");
            case ImageItem ignored -> List.of("image", "action", "action2", "resize", "art");
            case PanelItem ignored -> List.of("width", "height");
            case PlaytreeItem ignored -> List.of("width", "height", "font", "bgimage", "itemimage", "openimage",
                    "closedimage", "fgcolor", "playcolor", "selcolor", "bgcolor1", "bgcolor2", "flat");
            case RadialSliderItem ignored -> List.of("sequence", "nbimages", "minangle", "maxangle", "value",
                    "tooltiptext");
            case SliderItem ignored -> List.of("up", "down", "over", "points", "thickness", "value", "tooltiptext");
            case dev.zoroaster1x.vlcskin.model.item.SliderBackground ignored -> List.of("image", "nbhoriz",
                    "nbvert", "padhoriz", "padvert");
            case TextItem ignored -> List.of("text", "font", "color", "width", "alignment", "scrolling");
            case VideoItem ignored -> List.of("width", "height", "autoresize");
            default -> List.of();
        };
        List<String> all = new java.util.ArrayList<>(common);
        all.addAll(typed);
        return all;
    }

    /**
     * Applies one resource attribute; returns the previous value or null.
     */
    public static String setResource(Resource resource, String name, String value) {
        String key = name.toLowerCase(Locale.ROOT);
        return switch (resource) {
            case BitmapResource bitmap -> switch (key) {
                case "id" -> swap(bitmap.getId(), value, bitmap::setId);
                case "file" -> swap(bitmap.getFile(), value, bitmap::setFile);
                case "alphacolor" -> swap(bitmap.getAlphacolor(), value, bitmap::setAlphacolor);
                case "nbframes" -> swap(Integer.toString(bitmap.getNbframes()), value, v -> bitmap.setNbframes(integer(v)));
                case "fps" -> swap(Integer.toString(bitmap.getFps()), value, v -> bitmap.setFps(integer(v)));
                default -> null;
            };
            case FontResource font -> switch (key) {
                case "id" -> swap(font.getId(), value, font::setId);
                case "file" -> swap(font.getFile(), value, font::setFile);
                case "size" -> swap(Integer.toString(font.getSize()), value, v -> font.setSize(integer(v)));
                default -> null;
            };
            case BitmapFontResource font -> switch (key) {
                case "id" -> swap(font.getId(), value, font::setId);
                case "file" -> swap(font.getFile(), value, font::setFile);
                case "type" -> swap(font.getType(), value, font::setType);
                default -> null;
            };
            case PopupMenuResource menu -> switch (key) {
                case "id" -> swap(menu.getId(), value, menu::setId);
                default -> null;
            };
            case IniFileResource ini -> switch (key) {
                case "id" -> swap(ini.getId(), value, ini::setId);
                case "file" -> swap(ini.getFile(), value, ini::setFile);
                default -> null;
            };
        };
    }

    public static String getResource(Resource resource, String name) {
        String key = name.toLowerCase(Locale.ROOT);
        return switch (resource) {
            case BitmapResource bitmap -> switch (key) {
                case "id" -> bitmap.getId();
                case "file" -> bitmap.getFile();
                case "alphacolor" -> bitmap.getAlphacolor();
                case "nbframes" -> Integer.toString(bitmap.getNbframes());
                case "fps" -> Integer.toString(bitmap.getFps());
                default -> null;
            };
            case FontResource font -> switch (key) {
                case "id" -> font.getId();
                case "file" -> font.getFile();
                case "size" -> Integer.toString(font.getSize());
                default -> null;
            };
            case BitmapFontResource font -> switch (key) {
                case "id" -> font.getId();
                case "file" -> font.getFile();
                case "type" -> font.getType();
                default -> null;
            };
            case PopupMenuResource menu -> "id".equals(key) ? menu.getId() : null;
            case IniFileResource ini -> switch (key) {
                case "id" -> ini.getId();
                case "file" -> ini.getFile();
                default -> null;
            };
        };
    }

    /**
     * Known attribute names for a resource.
     */
    public static boolean isKnownResourceAttribute(Resource resource, String name) {
        String key = name.toLowerCase(Locale.ROOT);
        return switch (resource) {
            case BitmapResource ignored -> List.of("id", "file", "alphacolor", "nbframes", "fps").contains(key);
            case FontResource ignored -> List.of("id", "file", "size").contains(key);
            case BitmapFontResource ignored -> List.of("id", "file", "type").contains(key);
            case PopupMenuResource ignored -> "id".equals(key);
            case IniFileResource ignored -> List.of("id", "file").contains(key);
        };
    }

    public static String setSubBitmap(SubBitmap sub, String name, String value) {
        String key = name.toLowerCase(Locale.ROOT);
        return switch (key) {
            case "id" -> swap(sub.getId(), value, sub::setId);
            case "x" -> swap(Integer.toString(sub.getX()), value, v -> sub.setX(integer(v)));
            case "y" -> swap(Integer.toString(sub.getY()), value, v -> sub.setY(integer(v)));
            case "width" -> swap(Integer.toString(sub.getWidth()), value, v -> sub.setWidth(integer(v)));
            case "height" -> swap(Integer.toString(sub.getHeight()), value, v -> sub.setHeight(integer(v)));
            case "nbframes" -> swap(Integer.toString(sub.getNbframes()), value, v -> sub.setNbframes(integer(v)));
            case "fps" -> swap(Integer.toString(sub.getFps()), value, v -> sub.setFps(integer(v)));
            default -> null;
        };
    }

    private interface StringSetter {
        void set(String value);
    }

    private static String swap(String previous, String value, StringSetter setter) {
        setter.set(value);
        return previous;
    }

    public static int integer(String value) {
        return Integer.parseInt(value.trim());
    }

    public static boolean bool(String value) {
        return Boolean.parseBoolean(value.trim());
    }
}
