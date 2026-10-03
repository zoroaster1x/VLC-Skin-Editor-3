package dev.zoroaster1x.vlcskin.render;

import static org.assertj.core.api.Assertions.assertThat;

import dev.zoroaster1x.vlcskin.model.SkinIndex;
import dev.zoroaster1x.vlcskin.model.SkinTheme;
import dev.zoroaster1x.vlcskin.model.resource.BitmapResource;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.nio.file.Files;
import java.nio.file.Path;
import javax.imageio.ImageIO;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

/** Bitmap strips animate: frames cycle, fps is read, sub bitmaps do not animate. */
class ImageStoreTest {

    @Test
    void framesCycleForAnAnimatedBitmap(@TempDir Path folder) throws Exception {
        BufferedImage strip = new BufferedImage(2, 8, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = strip.createGraphics();
        for (int frame = 0; frame < 4; frame++) {
            g.setColor(new Color(20 + frame * 50, 0, 0));
            g.fillRect(0, frame * 2, 2, 2);
        }
        g.dispose();
        ImageIO.write(strip, "png", folder.resolve("strip.png").toFile());

        SkinTheme theme = new SkinTheme();
        BitmapResource bitmap = new BitmapResource();
        bitmap.setId("strip");
        bitmap.setFile("strip.png");
        bitmap.setNbframes(4);
        bitmap.setFps(12);
        theme.getResources().add(bitmap);
        SkinIndex index = new SkinIndex(theme);
        ImageStore store = new ImageStore(folder);

        int first = store.image(index, "strip", 0).getRGB(0, 0);
        int second = store.image(index, "strip", 1).getRGB(0, 0);
        int wrapped = store.image(index, "strip", 4).getRGB(0, 0);
        assertThat(second).isNotEqualTo(first);
        assertThat(wrapped).isEqualTo(first);
        assertThat(store.image(index, "strip", 0).getHeight()).isEqualTo(2);
    }

    @Test
    void animationRateComesFromTheFastestBitmap(@TempDir Path folder) throws Exception {
        SkinTheme theme = new SkinTheme();
        BitmapResource slow = new BitmapResource();
        slow.setId("slow");
        slow.setNbframes(2);
        slow.setFps(5);
        BitmapResource fast = new BitmapResource();
        fast.setId("fast");
        fast.setNbframes(2);
        fast.setFps(25);
        BitmapResource still = new BitmapResource();
        still.setId("still");
        theme.getResources().add(slow);
        theme.getResources().add(fast);
        theme.getResources().add(still);
        SkinIndex index = new SkinIndex(theme);
        assertThat(ImageStore.hasAnimation(index)).isTrue();
        assertThat(ImageStore.animationFps(index)).isEqualTo(25);

        ImageStore store = new ImageStore(folder);
        assertThat(Files.isDirectory(folder)).isTrue();
        assertThat(store).isNotNull();
    }
}
