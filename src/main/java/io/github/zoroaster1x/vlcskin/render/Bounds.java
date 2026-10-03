package io.github.zoroaster1x.vlcskin.render;

import io.github.zoroaster1x.vlcskin.model.SkinIndex;
import io.github.zoroaster1x.vlcskin.model.item.AnchorItem;
import io.github.zoroaster1x.vlcskin.model.item.ButtonItem;
import io.github.zoroaster1x.vlcskin.model.item.CheckboxItem;
import io.github.zoroaster1x.vlcskin.model.item.GroupItem;
import io.github.zoroaster1x.vlcskin.model.item.ImageItem;
import io.github.zoroaster1x.vlcskin.model.item.Item;
import io.github.zoroaster1x.vlcskin.model.item.PanelItem;
import io.github.zoroaster1x.vlcskin.model.item.PlaytreeItem;
import io.github.zoroaster1x.vlcskin.model.item.RadialSliderItem;
import io.github.zoroaster1x.vlcskin.model.item.SliderItem;
import io.github.zoroaster1x.vlcskin.model.item.TextItem;
import io.github.zoroaster1x.vlcskin.model.item.VideoItem;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.image.BufferedImage;

/**
 * Computes the drawn bounds of an item, in layout coordinates.
 */
public final class Bounds {

    private static final BufferedImage SCRATCH = new BufferedImage(1, 1, BufferedImage.TYPE_INT_ARGB);

    private Bounds() {
    }

    public static Rectangle of(Item item, SkinIndex index, ImageStore images, PreviewVariables variables) {
        return of(item, 0, 0, index, images, variables);
    }

    public static Rectangle of(Item item, int offsetX, int offsetY, SkinIndex index, ImageStore images,
                               PreviewVariables variables) {
        return switch (item) {
            case AnchorItem anchor -> anchor(anchor, offsetX, offsetY);
            case ButtonItem button -> sized(item, offsetX, offsetY, images.image(index, button.getUp()));
            case CheckboxItem checkbox -> sized(item, offsetX, offsetY,
                    images.image(index, variables.evaluate(checkbox.getState())
                            ? checkbox.getUp2() : checkbox.getUp1()));
            case GroupItem group -> group(group, offsetX, offsetY, index, images, variables);
            case ImageItem image -> sized(item, offsetX, offsetY, images.image(index, image.getImage()));
            case PanelItem panel -> new Rectangle(offsetX + panel.getX(), offsetY + panel.getY(),
                    Math.max(0, panel.getWidth()), Math.max(0, panel.getHeight()));
            case PlaytreeItem playtree -> new Rectangle(offsetX + playtree.getX(), offsetY + playtree.getY(),
                    Math.max(0, playtree.getWidth()), Math.max(0, playtree.getHeight()));
            case RadialSliderItem radial -> radial(radial, offsetX, offsetY, index, images);
            case SliderItem slider -> slider(slider, offsetX, offsetY);
            case io.github.zoroaster1x.vlcskin.model.item.SliderBackground background ->
                    background(background, offsetX, offsetY, index, images);
            case TextItem text -> text(text, offsetX, offsetY, index, images, variables);
            case VideoItem video -> new Rectangle(offsetX + video.getX(), offsetY + video.getY(),
                    Math.max(0, video.getWidth()), Math.max(0, video.getHeight()));
        };
    }

    private static Rectangle sized(Item item, int offsetX, int offsetY, BufferedImage image) {
        int width = image == null ? 0 : image.getWidth();
        int height = image == null ? 0 : image.getHeight();
        return new Rectangle(offsetX + item.getX(), offsetY + item.getY(), width, height);
    }

    private static Rectangle group(GroupItem group, int offsetX, int offsetY, SkinIndex index,
                                   ImageStore images, PreviewVariables variables) {
        Rectangle union = null;
        int x = offsetX + group.getX();
        int y = offsetY + group.getY();
        for (Item child : group.getItems()) {
            Rectangle childBounds = of(child, x, y, index, images, variables);
            union = union == null ? childBounds : union.union(childBounds);
        }
        return union == null ? new Rectangle(x, y, 0, 0) : union;
    }

    private static Rectangle anchor(AnchorItem anchor, int offsetX, int offsetY) {
        try {
            BezierPath path = SliderGeometry.sharedPath(anchor.getPoints());
            int width = path.controlCount() == 0 ? 0 : path.width();
            int height = path.controlCount() == 0 ? 0 : path.height();
            return new Rectangle(offsetX + anchor.getX(), offsetY + anchor.getY(), width, height);
        } catch (IllegalArgumentException ex) {
            return new Rectangle(offsetX + anchor.getX(), offsetY + anchor.getY(), 0, 0);
        }
    }

    private static Rectangle slider(SliderItem slider, int offsetX, int offsetY) {
        try {
            BezierPath path = SliderGeometry.sharedPath(slider.getPoints());
            return new Rectangle(offsetX + slider.getX(), offsetY + slider.getY(), path.width(), path.height());
        } catch (IllegalArgumentException ex) {
            return new Rectangle(offsetX + slider.getX(), offsetY + slider.getY(), 0, 0);
        }
    }

    private static Rectangle background(io.github.zoroaster1x.vlcskin.model.item.SliderBackground background,
                                        int offsetX, int offsetY, SkinIndex index, ImageStore images) {
        Rectangle frame = SliderGeometry.backgroundFrame(background, 0.5f, index, images);
        return new Rectangle(offsetX + background.getX(), offsetY + background.getY(),
                frame == null ? 0 : frame.width, frame == null ? 0 : frame.height);
    }

    private static Rectangle radial(RadialSliderItem radial, int offsetX, int offsetY, SkinIndex index,
                                    ImageStore images) {
        BufferedImage image = images.image(index, radial.getSequence());
        if (image == null) {
            return new Rectangle(offsetX + radial.getX(), offsetY + radial.getY(), 0, 0);
        }
        int frames = Math.max(1, radial.getNbimages());
        return new Rectangle(offsetX + radial.getX(), offsetY + radial.getY(),
                image.getWidth(), Math.max(1, image.getHeight() / frames));
    }

    private static Rectangle text(TextItem text, int offsetX, int offsetY, SkinIndex index, ImageStore images,
                                  PreviewVariables variables) {
        Graphics2D g = SCRATCH.createGraphics();
        try {
            g.setFont(images.font(index, text.getFont()));
            String content = variables.substitute(text.getText());
            FontMetrics metrics = g.getFontMetrics();
            int width = text.getWidth() > 0 ? text.getWidth() : metrics.stringWidth(content);
            return new Rectangle(offsetX + text.getX(), offsetY + text.getY(), Math.max(1, width),
                    Math.max(1, metrics.getHeight()));
        } finally {
            g.dispose();
        }
    }
}
