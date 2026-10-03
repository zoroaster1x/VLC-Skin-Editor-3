package dev.zoroaster1x.vlcskin.render;

import static org.assertj.core.api.Assertions.assertThat;

import dev.zoroaster1x.vlcskin.edit.EditorSession;
import dev.zoroaster1x.vlcskin.example.ExampleSkins;
import dev.zoroaster1x.vlcskin.model.SkinLayout;
import dev.zoroaster1x.vlcskin.model.item.ButtonItem;
import dev.zoroaster1x.vlcskin.model.item.Item;
import java.awt.Rectangle;
import java.awt.image.BufferedImage;
import java.nio.file.Path;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class RendererTest {

    private EditorSession openNeon(Path folder) throws Exception {
        Path theme = ExampleSkins.create(folder, ExampleSkins.NEON);
        return EditorSession.open(theme);
    }

    @Test
    void rendersTheExampleAtZoom(@TempDir Path folder) throws Exception {
        EditorSession session = openNeon(folder);
        SkinLayout layout = session.currentLayout();
        RenderOptions options = session.renderOptions();
        SkinRenderer renderer = session.renderer();

        // The rounded background paints a pixel near the center and leaves the corner transparent
        // when the checkerboard is off; the default options paint the checkerboard instead.
        BufferedImage one = renderer.render(layout, options.withZoom(1).withoutCheckerboard());
        assertThat(one.getWidth()).isEqualTo(320);
        assertThat(one.getHeight()).isEqualTo(140);
        BufferedImage two = renderer.render(layout, options.withZoom(2).withoutCheckerboard());
        assertThat(two.getWidth()).isEqualTo(640);
        assertThat(two.getHeight()).isEqualTo(280);
        assertThat(alphaAt(one, 160, 70)).isGreaterThan(0);
        assertThat(alphaAt(one, 1, 1)).isEqualTo(0);
        assertThat(alphaAt(renderer.render(layout, options.withZoom(1)), 1, 1)).isEqualTo(255);
    }

    @Test
    void clickingSelectsTheTopmostItem(@TempDir Path folder) throws Exception {
        EditorSession session = openNeon(folder);
        SkinLayout layout = session.currentLayout();
        HitTester hitTester = new HitTester(session.index(), session.images(), session.variables());
        Item play = session.index().findItem("play_btn");
        assertThat(play).isInstanceOf(ButtonItem.class);
        Rectangle bounds = Bounds.of(play, session.index(), session.images(), session.variables());
        Item found = hitTester.topmost(layout, bounds.x + bounds.width / 2, bounds.y + bounds.height / 2);
        assertThat(found.getId()).isEqualTo("play_btn");

        // Clicking the background away from every control hits the background image.
        Item background = hitTester.topmost(layout, 300, 130);
        assertThat(background.getId()).isEqualTo("background_img");
    }

    @Test
    void selectionOverlayDrawsRed(@TempDir Path folder) throws Exception {
        EditorSession session = openNeon(folder);
        SkinLayout layout = session.currentLayout();
        Item button = session.index().findItem("play_btn");
        RenderOptions options = session.renderOptions().withSelection(button).withZoom(1);
        BufferedImage image = session.renderer().render(layout, options);
        Rectangle bounds = Bounds.of(button, session.index(), session.images(), session.variables());
        boolean red = false;
        for (int x = bounds.x; x < bounds.x + bounds.width && !red; x++) {
            int rgb = image.getRGB(x, bounds.y);
            if (((rgb >> 16) & 0xFF) > 180 && ((rgb >> 8) & 0xFF) < 90 && (rgb & 0xFF) < 90) {
                red = true;
            }
        }
        assertThat(red).as("selection rectangle is red").isTrue();
    }

    @Test
    void sliderThumbTracksTheValue(@TempDir Path folder) throws Exception {
        EditorSession session = openNeon(folder);
        var slider = (dev.zoroaster1x.vlcskin.model.item.SliderItem) session.index().findItem("position");
        session.variables().setSliderValue(0f);
        var left = SliderGeometry.thumbPosition(slider, session.variables());
        session.variables().setSliderValue(1f);
        var right = SliderGeometry.thumbPosition(slider, session.variables());
        assertThat(left.x).isLessThan(right.x);
        assertThat(right.x).isEqualTo(272f);
    }

    private int alphaAt(BufferedImage image, int x, int y) {
        return (image.getRGB(x, y) >>> 24) & 0xFF;
    }
}
