package dev.zoroaster1x.vlcskin.render;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.awt.Color;
import java.awt.image.BufferedImage;
import org.junit.jupiter.api.Test;

class SliderBackgroundGeneratorTest {

    private BufferedImage image(int width, int height, Color color) {
        BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
        java.awt.Graphics2D g = image.createGraphics();
        g.setColor(color);
        g.fillRect(0, 0, width, height);
        g.dispose();
        return image;
    }

    @Test
    void horizontalStripHasOneFramePerFillLevel() {
        BufferedImage middle = image(10, 8, Color.RED);
        var spec = new SliderBackgroundGenerator.Spec(100, 12, 0, 0, 2, 2, true, true, true,
                image(100, 12, Color.DARK_GRAY), null, middle, null, null);
        BufferedImage out = SliderBackgroundGenerator.generate(spec);
        assertThat(SliderBackgroundGenerator.frameCount(spec)).isEqualTo(101);
        assertThat(out.getWidth()).isEqualTo(100);
        assertThat(out.getHeight()).isEqualTo(101 * 12);
        // Frame 0 is background only; the middle color does not appear.
        assertThat(out.getRGB(50, 6) & 0x00FFFFFF).isEqualTo(0x404040);
        // The last frame has the middle color somewhere in its row.
        int lastRowY = 100 * 12 + 6;
        boolean foundRed = false;
        for (int x = 0; x < out.getWidth(); x++) {
            if ((out.getRGB(x, lastRowY) & 0x00FF0000) != 0) {
                foundRed = true;
                break;
            }
        }
        assertThat(foundRed).as("middle pixels fill the last frame").isTrue();
    }

    @Test
    void verticalStripIsTransposed() {
        var spec = new SliderBackgroundGenerator.Spec(20, 50, 0, 0, 0, 0, false, true, true,
                null, null, image(8, 4, Color.BLUE), null, null);
        BufferedImage out = SliderBackgroundGenerator.generate(spec);
        assertThat(out.getWidth()).isEqualTo(51 * 20);
        assertThat(out.getHeight()).isEqualTo(50);
    }

    @Test
    void middleImageIsRequired() {
        var spec = new SliderBackgroundGenerator.Spec(10, 10, 0, 0, 0, 0, true, true, true,
                null, null, null, null, null);
        assertThatThrownBy(() -> SliderBackgroundGenerator.generate(spec))
                .isInstanceOf(IllegalArgumentException.class);
    }
}
