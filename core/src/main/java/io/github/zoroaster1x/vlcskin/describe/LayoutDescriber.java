package io.github.zoroaster1x.vlcskin.describe;

import io.github.zoroaster1x.vlcskin.model.SkinIndex;
import io.github.zoroaster1x.vlcskin.model.SkinLayout;
import io.github.zoroaster1x.vlcskin.model.SkinWindow;
import io.github.zoroaster1x.vlcskin.model.item.AbstractItem;
import io.github.zoroaster1x.vlcskin.model.item.AnchorItem;
import io.github.zoroaster1x.vlcskin.model.item.ButtonItem;
import io.github.zoroaster1x.vlcskin.model.item.CheckboxItem;
import io.github.zoroaster1x.vlcskin.model.item.ImageItem;
import io.github.zoroaster1x.vlcskin.model.item.Item;
import io.github.zoroaster1x.vlcskin.model.item.PanelItem;
import io.github.zoroaster1x.vlcskin.model.item.PlaytreeItem;
import io.github.zoroaster1x.vlcskin.model.item.RadialSliderItem;
import io.github.zoroaster1x.vlcskin.model.item.SliderItem;
import io.github.zoroaster1x.vlcskin.model.item.TextItem;
import io.github.zoroaster1x.vlcskin.model.item.VideoItem;
import io.github.zoroaster1x.vlcskin.render.Bounds;
import io.github.zoroaster1x.vlcskin.render.ImageStore;
import io.github.zoroaster1x.vlcskin.render.PreviewVariables;
import java.awt.Rectangle;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Builds the geometry description a model can reason about without pixels.
 */
public final class LayoutDescriber {

    private LayoutDescriber() {
    }

    public static LayoutDescription describe(SkinWindow window, SkinLayout layout, SkinIndex index,
                                             ImageStore images, PreviewVariables variables, int zoom) {
        List<GeometryNode> nodes = new ArrayList<>();
        int[] zOrder = {0};
        String parentId = null;
        for (Item item : layout.getItems()) {
            collect(item, parentId, 0, zOrder, nodes, index, images, variables);
        }
        return new LayoutDescription(
                window == null ? null : window.getId(),
                layout.getId(),
                layout.getWidth(),
                layout.getHeight(),
                zoom,
                nodes,
                new LinkedHashMap<>(variables.booleans()),
                new LinkedHashMap<>(variables.texts()),
                variables.sliderValue(),
                List.copyOf(images.problems().entrySet().stream()
                        .map(entry -> entry.getKey() + ": " + entry.getValue()).toList()));
    }

    private static void collect(Item item, String parentId, int depth, int[] zOrder, List<GeometryNode> nodes,
                                SkinIndex index, ImageStore images, PreviewVariables variables) {
        Rectangle bounds = Bounds.of(item, index, images, variables);
        boolean visible = true;
        if (item instanceof AbstractItem abstractItem) {
            visible = variables.evaluate(abstractItem.getVisible());
        }
        nodes.add(new GeometryNode(
                item.getId(),
                item.type().displayName(),
                item.elementName(),
                parentId,
                depth,
                zOrder[0]++,
                bounds.x,
                bounds.y,
                bounds.width,
                bounds.height,
                visible,
                textOf(item, variables),
                attributesOf(item)));
        for (Item child : item.children()) {
            collect(child, item.getId(), depth + 1, zOrder, nodes, index, images, variables);
        }
    }

    private static String textOf(Item item, PreviewVariables variables) {
        return switch (item) {
            case TextItem text -> variables.substitute(text.getText());
            case ButtonItem button -> button.getTooltiptext();
            case ImageItem image -> image.getAction();
            case SliderItem slider -> slider.getValue();
            default -> null;
        };
    }

    private static Map<String, String> attributesOf(Item item) {
        Map<String, String> attributes = new LinkedHashMap<>();
        switch (item) {
            case ImageItem image -> {
                attributes.put("image", safe(image.getImage()));
                attributes.put("resize", safe(image.getResize()));
                if (!"none".equals(image.getAction())) {
                    attributes.put("action", safe(image.getAction()));
                }
            }
            case ButtonItem button -> {
                attributes.put("up", safe(button.getUp()));
                attributes.put("down", safe(button.getDown()));
                attributes.put("over", safe(button.getOver()));
                if (!"none".equals(button.getAction())) {
                    attributes.put("action", safe(button.getAction()));
                }
            }
            case CheckboxItem checkbox -> {
                attributes.put("state", safe(checkbox.getState()));
                attributes.put("up1", safe(checkbox.getUp1()));
                attributes.put("up2", safe(checkbox.getUp2()));
            }
            case TextItem text -> {
                attributes.put("font", safe(text.getFont()));
                attributes.put("color", safe(text.getColor()));
                attributes.put("alignment", safe(text.getAlignment()));
            }
            case SliderItem slider -> {
                attributes.put("up", safe(slider.getUp()));
                attributes.put("points", safe(slider.getPoints()));
                attributes.put("value", safe(slider.getValue()));
                attributes.put("thickness", Integer.toString(slider.getThickness()));
            }
            case AnchorItem anchor -> {
                attributes.put("points", safe(anchor.getPoints()));
                attributes.put("priority", Integer.toString(anchor.getPriority()));
                attributes.put("range", Integer.toString(anchor.getRange()));
            }
            case PanelItem panel -> {
                attributes.put("lefttop", safe(panel.getLefttop()));
                attributes.put("rightbottom", safe(panel.getRightbottom()));
            }
            case PlaytreeItem playtree -> {
                attributes.put("font", safe(playtree.getFont()));
                attributes.put("flat", Boolean.toString(playtree.isFlat()));
            }
            case RadialSliderItem radial -> {
                attributes.put("sequence", safe(radial.getSequence()));
                attributes.put("nbimages", Integer.toString(radial.getNbimages()));
            }
            case VideoItem video -> attributes.put("autoresize", Boolean.toString(video.isAutoresize()));
            default -> {
            }
        }
        if (item instanceof AbstractItem abstractItem) {
            if (!"true".equals(abstractItem.getVisible())) {
                attributes.put("visible", safe(abstractItem.getVisible()));
            }
            if (!abstractItem.foreignAttributes().isEmpty()) {
                attributes.put("foreignAttributes", String.join(",", abstractItem.foreignAttributes().keySet()));
            }
        }
        return attributes;
    }

    private static String safe(String value) {
        return value == null ? "" : value;
    }
}
