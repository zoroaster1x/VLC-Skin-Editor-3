package io.github.zoroaster1x.vlcskin.format;

import io.github.zoroaster1x.vlcskin.model.IncludeFile;
import io.github.zoroaster1x.vlcskin.model.SkinIndex;
import io.github.zoroaster1x.vlcskin.model.SkinLayout;
import io.github.zoroaster1x.vlcskin.model.SkinTheme;
import io.github.zoroaster1x.vlcskin.model.SkinWindow;
import io.github.zoroaster1x.vlcskin.model.item.AnchorItem;
import io.github.zoroaster1x.vlcskin.model.item.Item;
import io.github.zoroaster1x.vlcskin.model.item.PanelItem;
import io.github.zoroaster1x.vlcskin.model.item.PlaytreeItem;
import io.github.zoroaster1x.vlcskin.model.item.RadialSliderItem;
import io.github.zoroaster1x.vlcskin.model.item.SliderItem;
import io.github.zoroaster1x.vlcskin.model.item.TextItem;
import io.github.zoroaster1x.vlcskin.model.item.VideoItem;
import io.github.zoroaster1x.vlcskin.model.resource.BitmapResource;
import io.github.zoroaster1x.vlcskin.model.resource.BitmapFontResource;
import io.github.zoroaster1x.vlcskin.model.resource.FontResource;
import io.github.zoroaster1x.vlcskin.model.resource.IniFileResource;
import io.github.zoroaster1x.vlcskin.model.resource.Resource;
import io.github.zoroaster1x.vlcskin.model.resource.SubBitmap;
import io.github.zoroaster1x.vlcskin.render.BezierPath;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Checks a theme for the mistakes VLC would refuse or draw badly.
 */
public final class SkinValidator {

    private SkinValidator() {
    }

    public static List<ParseIssue> validate(SkinTheme theme) {
        return validate(theme, null);
    }

    public static List<ParseIssue> validate(SkinTheme theme, Path folder) {
        List<ParseIssue> issues = new ArrayList<>();
        SkinIndex index = new SkinIndex(theme);
        checkTheme(theme, issues);
        checkDuplicateIds(theme, issues);
        checkResources(theme, folder, issues);
        checkWindows(theme, issues);
        for (SkinWindow window : theme.getWindows()) {
            for (SkinLayout layout : window.getLayouts()) {
                checkLayout(layout, issues);
                for (Item item : layout.getItems()) {
                    checkItem(item, index, issues, "Window[" + window.getId() + "]/Layout[" + layout.getId() + "]");
                }
            }
        }
        checkReferences(index, issues);
        return issues;
    }

    private static void checkTheme(SkinTheme theme, List<ParseIssue> issues) {
        if (theme.getVersion() == null || !theme.getVersion().startsWith("2.")) {
            issues.add(ParseIssue.error("Theme version must be 2.x for skins2", "Theme"));
        }
        if (theme.getAlpha() < 1 || theme.getAlpha() > 255) {
            issues.add(ParseIssue.warning("Theme alpha should be between 1 and 255", "Theme"));
        }
        if (theme.getMovealpha() < 1 || theme.getMovealpha() > 255) {
            issues.add(ParseIssue.warning("Theme movealpha should be between 1 and 255", "Theme"));
        }
        if (theme.getMagnet() < 0) {
            issues.add(ParseIssue.warning("Theme magnet should not be negative", "Theme"));
        }
        if (theme.getThemeInfo().getName() == null || theme.getThemeInfo().getName().isBlank()) {
            issues.add(ParseIssue.info("ThemeInfo has no name", "ThemeInfo"));
        }
    }

    private static void checkDuplicateIds(SkinTheme theme, List<ParseIssue> issues) {
        Set<String> resourceIds = new HashSet<>();
        for (Resource resource : theme.getResources()) {
            if (!resourceIds.add(resource.getId())) {
                issues.add(ParseIssue.error("Duplicate resource id \"" + resource.getId() + "\"",
                        resource.elementName() + "[" + resource.getId() + "]"));
            }
            if (resource instanceof BitmapResource bitmap) {
                Set<String> subIds = new HashSet<>();
                for (SubBitmap sub : bitmap.getSubBitmaps()) {
                    if (!subIds.add(sub.getId())) {
                        issues.add(ParseIssue.error("Duplicate SubBitmap id \"" + sub.getId() + "\"",
                                "Bitmap[" + bitmap.getId() + "]"));
                    }
                    if (!resourceIds.add(sub.getId())) {
                        issues.add(ParseIssue.error(
                                "SubBitmap id \"" + sub.getId() + "\" is already used by a resource",
                                "Bitmap[" + bitmap.getId() + "]"));
                    }
                }
            }
        }
        Set<String> windowIds = new HashSet<>();
        for (SkinWindow window : theme.getWindows()) {
            if (!windowIds.add(window.getId())) {
                issues.add(ParseIssue.error("Duplicate window id \"" + window.getId() + "\"",
                        "Window[" + window.getId() + "]"));
            }
            Set<String> layoutIds = new HashSet<>();
            for (SkinLayout layout : window.getLayouts()) {
                if (!layoutIds.add(layout.getId())) {
                    issues.add(ParseIssue.error("Duplicate layout id \"" + layout.getId()
                            + "\" in window \"" + window.getId() + "\"", "Window[" + window.getId() + "]"));
                }
            }
        }
        Map<String, Integer> itemIds = new HashMap<>();
        new SkinIndex(theme).forEachItem(item -> itemIds.merge(item.getId(), 1, Integer::sum));
        itemIds.forEach((id, count) -> {
            if (count > 1) {
                issues.add(ParseIssue.error("The item id \"" + id + "\" is used " + count + " times", "Theme"));
            }
        });
    }

    private static void checkResources(SkinTheme theme, Path folder, List<ParseIssue> issues) {
        for (Resource resource : theme.getResources()) {
            String file = switch (resource) {
                case BitmapResource bitmap -> bitmap.getFile();
                case FontResource font -> font.getFile();
                case BitmapFontResource font -> font.getFile();
                case IniFileResource ini -> ini.getFile();
                default -> null;
            };
            if (file == null || file.isBlank()) {
                issues.add(ParseIssue.error(resource.typeName() + " \"" + resource.getId() + "\" has no file",
                        resource.elementName() + "[" + resource.getId() + "]"));
            } else if (folder != null && !Files.isRegularFile(folder.resolve(file.replace('\\', '/')))) {
                issues.add(ParseIssue.warning("File not found: " + file,
                        resource.elementName() + "[" + resource.getId() + "]"));
            }
            if (resource instanceof BitmapResource bitmap) {
                if (bitmap.getNbframes() < 1) {
                    issues.add(ParseIssue.error("Bitmap \"" + bitmap.getId() + "\" needs at least one frame",
                            "Bitmap[" + bitmap.getId() + "]"));
                }
                for (SubBitmap sub : bitmap.getSubBitmaps()) {
                    if (sub.getWidth() <= 0 || sub.getHeight() <= 0) {
                        issues.add(ParseIssue.error("SubBitmap \"" + sub.getId() + "\" has no size",
                                "SubBitmap[" + sub.getId() + "]"));
                    }
                }
            }
            if (resource instanceof FontResource font && font.getSize() <= 0) {
                issues.add(ParseIssue.error("Font \"" + font.getId() + "\" needs a positive size",
                        "Font[" + font.getId() + "]"));
            }
        }
        for (IncludeFile include : theme.getIncludes()) {
            String file = include.getFile();
            if (file == null || file.isBlank()) {
                issues.add(ParseIssue.error("Include has no file", "Include"));
            } else if (folder != null && !Files.isRegularFile(folder.resolve(file.replace('\\', '/')))) {
                issues.add(ParseIssue.warning("Include file not found: " + file, "Include"));
            }
        }
    }

    private static void checkWindows(SkinTheme theme, List<ParseIssue> issues) {
        if (theme.getWindows().isEmpty()) {
            issues.add(ParseIssue.error("The theme has no windows", "Theme"));
        }
        for (SkinWindow window : theme.getWindows()) {
            if (window.getLayouts().isEmpty()) {
                issues.add(ParseIssue.error("Window \"" + window.getId() + "\" has no layouts",
                        "Window[" + window.getId() + "]"));
            }
        }
    }

    private static void checkLayout(SkinLayout layout, List<ParseIssue> issues) {
        if (layout.getWidth() <= 0 || layout.getHeight() <= 0) {
            issues.add(ParseIssue.error("Layout \"" + layout.getId() + "\" needs a positive size",
                    "Layout[" + layout.getId() + "]"));
        }
        if (layout.getMinwidth() > 0 && layout.getMaxwidth() > 0 && layout.getMinwidth() > layout.getMaxwidth()) {
            issues.add(ParseIssue.warning("Layout \"" + layout.getId() + "\" minwidth is larger than maxwidth",
                    "Layout[" + layout.getId() + "]"));
        }
        if (layout.getMinheight() > 0 && layout.getMaxheight() > 0 && layout.getMinheight() > layout.getMaxheight()) {
            issues.add(ParseIssue.warning("Layout \"" + layout.getId() + "\" minheight is larger than maxheight",
                    "Layout[" + layout.getId() + "]"));
        }
    }

    private static void checkItem(Item item, SkinIndex index, List<ParseIssue> issues, String owner) {
        String location = owner + "/" + item.elementName() + "[" + item.getId() + "]";
        switch (item) {
            case SliderItem slider -> {
                checkPoints(slider.getPoints(), location, issues);
                if (slider.getThickness() <= 0) {
                    issues.add(ParseIssue.warning("Slider \"" + slider.getId() + "\" thickness should be positive",
                            location));
                }
                if (isMissing(index, slider.getUp())) {
                    issues.add(ParseIssue.error("Slider \"" + slider.getId() + "\" up image is missing",
                            location));
                }
                if (slider.getBackground() != null && isMissing(index, slider.getBackground().getImage())) {
                    issues.add(ParseIssue.error("Slider \"" + slider.getId() + "\" background image is missing",
                            location));
                }
                if (slider.getBackground() != null && slider.getBackground().getNbhoriz() > 0
                        && slider.getBackground().getNbvert() > 0
                        && slider.getBackground().getNbhoriz() * slider.getBackground().getNbvert() < 1) {
                    issues.add(ParseIssue.warning("Slider \"" + slider.getId() + "\" background grid is empty",
                            location));
                }
            }
            case AnchorItem anchor -> checkPoints(anchor.getPoints(), location, issues);
            case TextItem text -> {
                if (text.getWidth() < 0) {
                    issues.add(ParseIssue.warning("Text \"" + text.getId() + "\" width should not be negative",
                            location));
                }
                checkColor(text.getColor(), location, issues);
            }
            case PanelItem panel -> {
                if (panel.getWidth() <= 0 || panel.getHeight() <= 0) {
                    issues.add(ParseIssue.warning("Panel \"" + panel.getId() + "\" has a non positive size",
                            location));
                }
            }
            case VideoItem video -> {
                if (video.getWidth() <= 0 || video.getHeight() <= 0) {
                    issues.add(ParseIssue.warning("Video \"" + video.getId() + "\" has a non positive size",
                            location));
                }
            }
            case PlaytreeItem playtree -> {
                if (playtree.getSlider() == null) {
                    issues.add(ParseIssue.warning("Playtree \"" + playtree.getId() + "\" has no slider",
                            location));
                }
                checkColor(playtree.getFgcolor(), location, issues);
                checkColor(playtree.getBgcolor1(), location, issues);
                checkColor(playtree.getBgcolor2(), location, issues);
                checkColor(playtree.getSelcolor(), location, issues);
                checkColor(playtree.getPlaycolor(), location, issues);
            }
            case RadialSliderItem radial -> {
                if (radial.getNbimages() <= 0) {
                    issues.add(ParseIssue.warning("Radial slider \"" + radial.getId() + "\" needs images",
                            location));
                }
            }
            case io.github.zoroaster1x.vlcskin.model.item.ImageItem image -> {
                if (isMissing(index, image.getImage())) {
                    issues.add(ParseIssue.error("Image \"" + image.getId() + "\" image is missing", location));
                }
                if (!List.of("mosaic", "scale", "scale2").contains(image.getResize())) {
                    issues.add(ParseIssue.warning("Image \"" + image.getId() + "\" has an unknown resize mode: "
                            + image.getResize(), location));
                }
            }
            case io.github.zoroaster1x.vlcskin.model.item.ButtonItem button -> {
                if (isMissing(index, button.getUp())) {
                    issues.add(ParseIssue.error("Button \"" + button.getId() + "\" up image is missing", location));
                }
            }
            case io.github.zoroaster1x.vlcskin.model.item.CheckboxItem checkbox -> {
                if (isMissing(index, checkbox.getUp1()) || isMissing(index, checkbox.getUp2())) {
                    issues.add(ParseIssue.error("Checkbox \"" + checkbox.getId() + "\" up images are missing",
                            location));
                }
                if (checkbox.getState() == null || checkbox.getState().isBlank()) {
                    issues.add(ParseIssue.error("Checkbox \"" + checkbox.getId() + "\" has no state condition",
                            location));
                }
            }
            default -> {
            }
        }
        for (Item child : item.children()) {
            checkItem(child, index, issues, location);
        }
    }

    private static boolean isMissing(SkinIndex index, String id) {
        return id == null || id.isBlank() || (!"none".equals(id) && index.findImage(id) == null);
    }

    private static void checkPoints(String points, String location, List<ParseIssue> issues) {
        if (points == null || points.isBlank()) {
            issues.add(ParseIssue.error("The points attribute is empty", location));
            return;
        }
        try {
            BezierPath.parse(points);
        } catch (IllegalArgumentException ex) {
            issues.add(ParseIssue.error("Invalid points: " + ex.getMessage(), location));
        }
    }

    private static void checkColor(String color, String location, List<ParseIssue> issues) {
        if (color == null || !color.matches("#[0-9a-fA-F]{6}")) {
            issues.add(ParseIssue.warning("Color \"" + color + "\" is not #RRGGBB", location));
        }
    }

    private static void checkReferences(SkinIndex index, List<ParseIssue> issues) {
        index.forEachItem(item -> {
            for (String resourceId : referencedIds(item)) {
                if (resourceId == null || resourceId.isBlank() || "none".equals(resourceId)
                        || "defaultfont".equals(resourceId)) {
                    continue;
                }
                if (index.findResource(resourceId) == null && index.findImage(resourceId) == null) {
                    issues.add(ParseIssue.error("Item \"" + item.getId() + "\" references missing resource \""
                            + resourceId + "\"", item.elementName() + "[" + item.getId() + "]"));
                }
            }
        });
    }

    private static List<String> referencedIds(Item item) {
        List<String> ids = new ArrayList<>();
        switch (item) {
            case io.github.zoroaster1x.vlcskin.model.item.ImageItem image -> ids.add(image.getImage());
            case io.github.zoroaster1x.vlcskin.model.item.ButtonItem button -> {
                ids.add(button.getUp());
                ids.add(button.getDown());
                ids.add(button.getOver());
            }
            case io.github.zoroaster1x.vlcskin.model.item.CheckboxItem checkbox -> {
                ids.add(checkbox.getUp1());
                ids.add(checkbox.getDown1());
                ids.add(checkbox.getOver1());
                ids.add(checkbox.getUp2());
                ids.add(checkbox.getDown2());
                ids.add(checkbox.getOver2());
            }
            case SliderItem slider -> {
                ids.add(slider.getUp());
                ids.add(slider.getDown());
                ids.add(slider.getOver());
                if (slider.getBackground() != null) {
                    ids.add(slider.getBackground().getImage());
                }
            }
            case TextItem text -> ids.add(text.getFont());
            case PlaytreeItem playtree -> {
                ids.add(playtree.getFont());
                ids.add(playtree.getBgimage());
                ids.add(playtree.getItemimage());
                ids.add(playtree.getOpenimage());
                ids.add(playtree.getClosedimage());
            }
            case RadialSliderItem radial -> ids.add(radial.getSequence());
            default -> {
            }
        }
        return ids;
    }
}
