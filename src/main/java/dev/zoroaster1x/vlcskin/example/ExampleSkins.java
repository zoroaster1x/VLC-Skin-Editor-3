package dev.zoroaster1x.vlcskin.example;

import dev.zoroaster1x.vlcskin.format.SkinWriter;
import dev.zoroaster1x.vlcskin.model.SkinLayout;
import dev.zoroaster1x.vlcskin.model.SkinTheme;
import dev.zoroaster1x.vlcskin.model.SkinWindow;
import dev.zoroaster1x.vlcskin.model.ThemeInfo;
import dev.zoroaster1x.vlcskin.model.item.ButtonItem;
import dev.zoroaster1x.vlcskin.model.item.ImageItem;
import dev.zoroaster1x.vlcskin.model.item.SliderItem;
import dev.zoroaster1x.vlcskin.model.item.TextItem;
import dev.zoroaster1x.vlcskin.model.resource.BitmapResource;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Path2D;
import java.awt.geom.RoundRectangle2D;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import javax.imageio.ImageIO;

/**
 * Built in example themes with assets generated at creation time, so the first
 * run has something real to open without downloading a skin.
 */
public final class ExampleSkins {

    public record Example(String id, String name, String description) {
    }

    public static final Example NEON = new Example("neon",
            "Neon player",
            "320x140 player bar with transport buttons, a seek slider and a volume slider");
    public static final Example PANEL = new Example("panel",
            "Flat panel",
            "A plain control panel with a video area and a playlist, a good starting point");
    public static final Example VELOCITY = new Example("velocity",
            "VeLoCity Dark",
            "dmtiir's full four window player theme (MIT), bundled with its license");

    private ExampleSkins() {
    }

    public static java.util.List<Example> catalog() {
        return java.util.List.of(NEON, PANEL, VELOCITY);
    }

    /**
     * Writes the example into a folder and returns the theme.xml path.
     */
    public static Path create(Path folder, Example example) throws IOException {
        Files.createDirectories(folder);
        if (example.id().equals(VELOCITY.id())) {
            return createVelocity(folder);
        }
        SkinTheme theme = example.id().equals(PANEL.id()) ? buildPanel(folder) : buildNeon(folder);
        Path themeFile = folder.resolve("theme.xml");
        Files.writeString(themeFile, SkinWriter.toXml(theme), StandardCharsets.UTF_8);
        return themeFile;
    }

    /**
     * VeLoCity is a real theme rather than a generated one, so its files ship
     * as resources and are copied out together with the MIT license.
     */
    private static Path createVelocity(Path folder) throws IOException {
        java.util.List<String> files = java.util.List.of(
                "theme.xml", "bg.png", "bg2.png", "buttons20.png", "buttons30.png",
                "corners.png", "roboto.ttf", "time.png", "volume.png");
        for (String file : files) {
            copyResource("velocity/" + file, folder.resolve(file));
        }
        copyResource("velocity/LICENSE.txt", folder.resolve("LICENSE-VeLoCity.txt"));
        return folder.resolve("theme.xml");
    }

    private static void copyResource(String name, Path target) throws IOException {
        try (java.io.InputStream in = ExampleSkins.class.getResourceAsStream(name)) {
            if (in == null) {
                throw new IOException("Example resource missing from the jar: " + name);
            }
            Files.write(target, in.readAllBytes());
        }
    }


    private static SkinTheme buildNeon(Path folder) throws IOException {
        writePng(folder, "background.png", neonBackground());
        writePng(folder, "prev.png", prevIcon());
        writePng(folder, "play.png", playIcon());
        writePng(folder, "next.png", nextIcon());
        writePng(folder, "close.png", closeIcon());
        writePng(folder, "seek_track.png", track(272, 8, new Color(0x2B, 0x31, 0x3D)));
        writePng(folder, "seek_thumb.png", dot(11, new Color(0x4F, 0xC3, 0xF7)));
        writePng(folder, "volume_track.png", track(80, 6, new Color(0x2B, 0x31, 0x3D)));
        writePng(folder, "volume_thumb.png", dot(9, new Color(0x9A, 0xA4, 0xB2)));

        SkinTheme theme = new SkinTheme();
        ThemeInfo info = new ThemeInfo();
        info.setName("Neon player");
        info.setAuthor("VLC Skin Studio example");
        info.setEmail("none");
        info.setWebpage("https://github.com/zoroaster1x/VLC-Skin-Editor-3");
        theme.setThemeInfo(info);

        theme.getResources().add(bitmap("background", "background.png"));
        theme.getResources().add(bitmap("prev", "prev.png"));
        theme.getResources().add(bitmap("play", "play.png"));
        theme.getResources().add(bitmap("next", "next.png"));
        theme.getResources().add(bitmap("close", "close.png"));
        theme.getResources().add(bitmap("seek_track", "seek_track.png"));
        theme.getResources().add(bitmap("seek_thumb", "seek_thumb.png"));
        theme.getResources().add(bitmap("volume_track", "volume_track.png"));
        theme.getResources().add(bitmap("volume_thumb", "volume_thumb.png"));

        SkinWindow window = new SkinWindow();
        window.setId("main");
        window.setX(120);
        window.setY(120);
        SkinLayout layout = new SkinLayout();
        layout.setId("main");
        layout.setWidth(320);
        layout.setHeight(140);

        layout.getItems().add(image("background_img", "background", 0, 0));
        layout.getItems().add(text("artist", "$N", 24, 16, "#9AA4B2", 180, "left"));
        layout.getItems().add(text("clock", "$T", 176, 16, "#E6E9EF", 100, "right"));

        layout.getItems().add(slider("seek_track", "seek_thumb", "position", 24, 52,
                "(0,0),(272,0)", "time", 10));
        layout.getItems().add(button("prev_btn", "prev", 28, 92, "playlist.previous()"));
        layout.getItems().add(button("play_btn", "play", 68, 88, "vlc.play()"));
        layout.getItems().add(button("next_btn", "next", 108, 92, "playlist.next()"));
        layout.getItems().add(text("volume_label", "$V", 214, 116, "#9AA4B2", 30, "left"));
        layout.getItems().add(slider("volume_track", "volume_thumb", "volume", 244, 108,
                "(0,0),(72,0)", "volume", 7));
        layout.getItems().add(button("close_btn", "close", 294, 8, "vlc.quit()"));

        window.getLayouts().add(layout);
        theme.getWindows().add(window);
        return theme;
    }


    private static SkinTheme buildPanel(Path folder) throws IOException {
        writePng(folder, "panel.png", panelBackground());
        writePng(folder, "play_big.png", playIcon());
        writePng(folder, "slider_track.png", track(240, 8, new Color(0x33, 0x38, 0x42)));
        writePng(folder, "slider_thumb.png", dot(11, new Color(0xE0, 0x6C, 0x38)));

        SkinTheme theme = new SkinTheme();
        ThemeInfo info = new ThemeInfo();
        info.setName("Flat panel");
        info.setAuthor("VLC Skin Studio example");
        info.setWebpage("https://github.com/zoroaster1x/VLC-Skin-Editor-3");
        theme.setThemeInfo(info);

        theme.getResources().add(bitmap("panel", "panel.png"));
        theme.getResources().add(bitmap("play_big", "play_big.png"));
        theme.getResources().add(bitmap("slider_track", "slider_track.png"));
        theme.getResources().add(bitmap("slider_thumb", "slider_thumb.png"));

        SkinWindow window = new SkinWindow();
        window.setId("panel");
        SkinLayout layout = new SkinLayout();
        layout.setId("panel");
        layout.setWidth(420);
        layout.setHeight(220);

        layout.getItems().add(image("panel_img", "panel", 0, 0));
        dev.zoroaster1x.vlcskin.model.item.VideoItem video =
                new dev.zoroaster1x.vlcskin.model.item.VideoItem();
        video.setId("video");
        video.setX(10);
        video.setY(10);
        video.setWidth(400);
        video.setHeight(150);
        layout.getItems().add(video);
        layout.getItems().add(button("play_big_btn", "play_big", 20, 170, "vlc.play()"));
        layout.getItems().add(slider("seek_track", "slider_thumb", "position", 80, 180,
                "(0,0),(320,0)", "time", 8));

        window.getLayouts().add(layout);
        theme.getWindows().add(window);
        return theme;
    }

    private static BitmapResource bitmap(String id, String file) {
        BitmapResource bitmap = new BitmapResource();
        bitmap.setId(id);
        bitmap.setFile(file);
        return bitmap;
    }

    private static ImageItem image(String id, String image, int x, int y) {
        ImageItem item = new ImageItem();
        item.setId(id);
        item.setImage(image);
        item.setX(x);
        item.setY(y);
        return item;
    }

    private static TextItem text(String id, String text, int x, int y, String color, int width, String align) {
        TextItem item = new TextItem();
        item.setId(id);
        item.setText(text);
        item.setX(x);
        item.setY(y);
        item.setColor(color);
        item.setWidth(width);
        item.setAlignment(align);
        return item;
    }

    private static ButtonItem button(String id, String up, int x, int y, String action) {
        ButtonItem item = new ButtonItem();
        item.setId(id);
        item.setUp(up);
        item.setX(x);
        item.setY(y);
        item.setAction(action);
        return item;
    }

    private static SliderItem slider(String background, String thumb, String id, int x, int y, String points,
                                     String value, int thickness) {
        SliderItem item = new SliderItem();
        item.setId(id);
        item.setUp(thumb);
        item.setPoints(points);
        item.setValue(value);
        item.setThickness(thickness);
        item.setX(x);
        item.setY(y);
        dev.zoroaster1x.vlcskin.model.item.SliderBackground bg =
                new dev.zoroaster1x.vlcskin.model.item.SliderBackground();
        bg.setId(background + "_bg");
        bg.setImage(background);
        item.setBackground(bg);
        return item;
    }


    private static void writePng(Path folder, String name, BufferedImage image) throws IOException {
        ImageIO.write(image, "png", folder.resolve(name).toFile());
    }

    private static BufferedImage canvas(int width, int height) {
        BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = image.createGraphics();
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g.dispose();
        return image;
    }

    private static BufferedImage neonBackground() {
        BufferedImage image = canvas(320, 140);
        Graphics2D g = image.createGraphics();
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g.setColor(new Color(0x1E, 0x22, 0x2B, 240));
        g.fill(new RoundRectangle2D.Float(0, 0, 320, 140, 18, 18));
        g.setColor(new Color(0x4F, 0xC3, 0xF7, 90));
        g.setStroke(new BasicStroke(1.2f));
        g.draw(new RoundRectangle2D.Float(0.5f, 0.5f, 319, 139, 18, 18));
        g.dispose();
        return image;
    }

    private static BufferedImage panelBackground() {
        BufferedImage image = canvas(420, 220);
        Graphics2D g = image.createGraphics();
        g.setColor(new Color(0x25, 0x2A, 0x33, 250));
        g.fill(new RoundRectangle2D.Float(0, 0, 420, 220, 14, 14));
        g.setColor(new Color(0xE0, 0x6C, 0x38, 120));
        g.setStroke(new BasicStroke(1.4f));
        g.draw(new RoundRectangle2D.Float(1, 1, 417, 217, 14, 14));
        g.dispose();
        return image;
    }

    private static BufferedImage playIcon() {
        BufferedImage image = canvas(28, 28);
        Graphics2D g = image.createGraphics();
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g.setColor(new Color(0xE6, 0xE9, 0xEF));
        Path2D triangle = new Path2D.Float();
        triangle.moveTo(10, 7);
        triangle.lineTo(21, 14);
        triangle.lineTo(10, 21);
        triangle.closePath();
        g.fill(triangle);
        g.dispose();
        return image;
    }

    private static BufferedImage prevIcon() {
        BufferedImage image = canvas(28, 28);
        Graphics2D g = image.createGraphics();
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g.setColor(new Color(0xE6, 0xE9, 0xEF));
        Path2D triangle = new Path2D.Float();
        triangle.moveTo(19, 7);
        triangle.lineTo(8, 14);
        triangle.lineTo(19, 21);
        triangle.closePath();
        g.fill(triangle);
        g.fillRect(5, 7, 3, 14);
        g.dispose();
        return image;
    }

    private static BufferedImage nextIcon() {
        BufferedImage image = canvas(28, 28);
        Graphics2D g = image.createGraphics();
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g.setColor(new Color(0xE6, 0xE9, 0xEF));
        Path2D triangle = new Path2D.Float();
        triangle.moveTo(9, 7);
        triangle.lineTo(20, 14);
        triangle.lineTo(9, 21);
        triangle.closePath();
        g.fill(triangle);
        g.fillRect(20, 7, 3, 14);
        g.dispose();
        return image;
    }

    private static BufferedImage closeIcon() {
        BufferedImage image = canvas(18, 18);
        Graphics2D g = image.createGraphics();
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g.setColor(new Color(0x9A, 0xA4, 0xB2));
        g.setStroke(new BasicStroke(2f));
        g.drawLine(4, 4, 14, 14);
        g.drawLine(14, 4, 4, 14);
        g.dispose();
        return image;
    }

    private static BufferedImage track(int width, int height, Color color) {
        BufferedImage image = canvas(width, height);
        Graphics2D g = image.createGraphics();
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g.setColor(color);
        g.fill(new RoundRectangle2D.Float(0, 0, width, height, height, height));
        g.dispose();
        return image;
    }

    private static BufferedImage dot(int size, Color color) {
        BufferedImage image = canvas(size, size);
        Graphics2D g = image.createGraphics();
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g.setColor(color);
        g.fill(new Ellipse2D.Float(0, 0, size, size));
        g.dispose();
        return image;
    }
}
