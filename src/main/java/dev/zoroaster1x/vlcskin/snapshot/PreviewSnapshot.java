package dev.zoroaster1x.vlcskin.snapshot;

import dev.zoroaster1x.vlcskin.describe.LayoutDescription;
import dev.zoroaster1x.vlcskin.describe.LayoutDescriber;
import dev.zoroaster1x.vlcskin.edit.EditorSession;
import dev.zoroaster1x.vlcskin.model.SkinIndex;
import dev.zoroaster1x.vlcskin.model.SkinLayout;
import dev.zoroaster1x.vlcskin.model.SkinWindow;
import dev.zoroaster1x.vlcskin.render.ImageStore;
import dev.zoroaster1x.vlcskin.render.RenderOptions;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import javax.imageio.ImageIO;

/**
 * Renders a layout to PNG bytes plus the geometry description, for people and models.
 */
public final class PreviewSnapshot {

    public record Result(byte[] png, LayoutDescription description) {
    }

    private PreviewSnapshot() {
    }

    /**
     * Captures the layout currently selected in a session.
     */
    public static Result capture(EditorSession session, int zoom) throws IOException {
        SkinLayout layout = session.currentLayout();
        if (layout == null) {
            throw new IllegalStateException("The skin has no layout to render");
        }
        SkinWindow window = session.index().windowOf(layout);
        RenderOptions options = session.renderOptions();
        options = options.withZoom(Math.max(1, zoom));
        return capture(session.index(), session.images(), window, layout, options, session.variables());
    }

    public static Result capture(SkinIndex index, ImageStore images, SkinWindow window, SkinLayout layout,
                                 RenderOptions options, dev.zoroaster1x.vlcskin.render.PreviewVariables variables)
            throws IOException {
        BufferedImage image = new dev.zoroaster1x.vlcskin.render.SkinRenderer(index, images)
                .render(layout, options.withoutCheckerboard().withoutOverlays());
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        ImageIO.write(image, "png", out);
        LayoutDescription description = LayoutDescriber.describe(window, layout, index, images, variables,
                options.zoom());
        return new Result(out.toByteArray(), description);
    }

    public static void writePng(Result result, Path target) throws IOException {
        Files.write(target, result.png());
    }
}
