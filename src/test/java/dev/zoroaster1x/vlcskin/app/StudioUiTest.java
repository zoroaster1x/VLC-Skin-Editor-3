package dev.zoroaster1x.vlcskin.app;

import static org.assertj.core.api.Assertions.assertThat;

import dev.zoroaster1x.vlcskin.app.panel.CanvasPanel;
import dev.zoroaster1x.vlcskin.app.theme.ThemeManager;
import dev.zoroaster1x.vlcskin.edit.EditorSession;
import dev.zoroaster1x.vlcskin.example.ExampleSkins;
import dev.zoroaster1x.vlcskin.model.ItemType;
import dev.zoroaster1x.vlcskin.model.item.Item;
import dev.zoroaster1x.vlcskin.render.Bounds;
import java.awt.Color;
import java.awt.Rectangle;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.nio.file.Files;
import java.nio.file.Path;
import javax.imageio.ImageIO;
import javax.swing.JSpinner;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

/**
 * Drives the whole UI without a window: lays it out, dispatches real mouse
 * events, and writes screenshots to build/reports/screenshots for review.
 */
class StudioUiTest {

    private static Path outputDir;

    @BeforeAll
    static void prepare() throws Exception {
        outputDir = Path.of("build", "reports", "screenshots");
        Files.createDirectories(outputDir);
    }

    private HeadlessStudio studio(Path folder) throws Exception {
        return studio(folder, "dark");
    }

    private HeadlessStudio studio(Path folder, String themeId) throws Exception {
        Path theme = ExampleSkins.create(folder, ExampleSkins.NEON);
        Studio studio = VlcSkinStudio.headlessStudio();
        dev.zoroaster1x.vlcskin.app.theme.ThemeManager.apply(themeId);
        studio.settings().setTheme(themeId);
        studio.openFile(theme);
        HeadlessStudio headless = new HeadlessStudio(studio);
        headless.render(1440, 900);
        headless.panels().canvas.setTool(CanvasPanel.Tool.MOVE);
        return headless;
    }

    @Test
    void theWholeWindowPaintsRealPixels(@TempDir Path folder) throws Exception {
        HeadlessStudio headless = studio(folder);
        BufferedImage image = headless.render(1440, 900);
        assertThat(image.getWidth()).isEqualTo(1440);
        int distinct = 0;
        int previous = image.getRGB(0, 0);
        for (int x = 0; x < image.getWidth(); x += 7) {
            for (int y = 0; y < image.getHeight(); y += 7) {
                int rgb = image.getRGB(x, y);
                if (rgb != previous) {
                    distinct++;
                    previous = rgb;
                }
            }
        }
        assertThat(distinct).as("the window is not a flat rectangle").isGreaterThan(50);
        ImageIO.write(image, "png", outputDir.resolve("studio-dark.png").toFile());
        assertThat(Files.size(outputDir.resolve("studio-dark.png"))).isGreaterThan(20_000);
    }

    @Test
    void lightThemeAlsoPaints(@TempDir Path folder) throws Exception {
        HeadlessStudio headless = studio(folder, "light");
        BufferedImage image = headless.render(1440, 900);
        ImageIO.write(image, "png", outputDir.resolve("studio-light.png").toFile());
        assertThat(Files.size(outputDir.resolve("studio-light.png"))).isGreaterThan(20_000);
    }

    @Test
    void theCanvasBackdropFollowsTheTheme(@TempDir Path folder) throws Exception {
        HeadlessStudio light = studio(folder, "light");
        Color lightBackdrop = light.canvas().canvasComponent().getBackground();
        assertThat(lightBackdrop).isEqualTo(ThemeManager.canvasBackground());

        HeadlessStudio dark = studio(folder.resolve("dark"), "dark");
        Color darkBackdrop = dark.canvas().canvasComponent().getBackground();
        assertThat(darkBackdrop).isEqualTo(ThemeManager.canvasBackground());
        assertThat(lightBackdrop).isNotEqualTo(darkBackdrop);

        // A dark window can still pin a light stage, and the change is live.
        dark.studio().settings().setCanvasBackground("light");
        dark.canvas().refreshBackdrop();
        assertThat(dark.canvas().canvasComponent().getBackground()).isEqualTo(lightBackdrop);
    }

    @Test
    void draggingOnTheCanvasMovesTheItemAndUndoRestoresIt(@TempDir Path folder) throws Exception {
        HeadlessStudio headless = studio(folder);
        EditorSession session = headless.studio().session();
        Item play = session.index().findItem("play_btn");
        int originalX = play.getX();
        Rectangle bounds = Bounds.of(play, session.index(), session.images(), session.variables());
        Rectangle canvasBounds = headless.canvas().canvasBounds();
        int zoom = headless.studio().settings().getCanvasZoom();
        int startX = canvasBounds.x + (bounds.x + bounds.width / 2) * zoom;
        int startY = canvasBounds.y + (bounds.y + bounds.height / 2) * zoom;
        var canvas = headless.canvas().canvasComponent();

        headless.onEdt(() -> {
            canvas.dispatchEvent(mouse(canvas, MouseEvent.MOUSE_PRESSED, startX, startY));
            canvas.dispatchEvent(mouse(canvas, MouseEvent.MOUSE_DRAGGED, startX + 20 * zoom, startY + 5 * zoom));
            canvas.dispatchEvent(mouse(canvas, MouseEvent.MOUSE_RELEASED, startX + 20 * zoom, startY + 5 * zoom));
        });

        assertThat(session.index().findItem("play_btn").getX()).isEqualTo(originalX + 20);
        assertThat(session.history().canUndo()).isTrue();
        session.undo();
        assertThat(session.index().findItem("play_btn").getX()).isEqualTo(originalX);

        // Selection follows the click.
        assertThat(session.selection().itemId()).isEqualTo("play_btn");
    }

    @Test
    void addingAnItemShowsUpInTheTreeAndTheDescription(@TempDir Path folder) throws Exception {
        HeadlessStudio headless = studio(folder);
        headless.onEdt(() -> headless.panels().items.addToRoot(ItemType.TEXT));
        EditorSession session = headless.studio().session();
        String id = session.selection().itemId();
        assertThat(id).isNotBlank();
        assertThat(session.index().findItem(id)).isNotNull();
        String description = headless.studio().service().describeUi().text();
        assertThat(description).contains("Canvas");
        assertThat(description).contains("Inspector");
        headless.render(1440, 900);
        ImageIO.write(headless.render(1440, 900), "png", outputDir.resolve("studio-with-new-text.png").toFile());
    }

    @Test
    void canvasAloneAtZoomTwoPaintsTheSkin(@TempDir Path folder) throws Exception {
        HeadlessStudio headless = studio(folder);
        headless.onEdt(() -> headless.canvas().zoomIn());
        BufferedImage image = headless.render(1440, 900);
        ImageIO.write(image, "png", outputDir.resolve("studio-zoom2.png").toFile());
        assertThat(Files.size(outputDir.resolve("studio-zoom2.png"))).isGreaterThan(20_000);
    }

    @Test
    void inspectorShowsTheSelectedItemFields(@TempDir Path folder) throws Exception {
        HeadlessStudio headless = studio(folder);
        EditorSession session = headless.studio().session();
        session.selection().selectItem("position");
        session.fireChanged();
        BufferedImage image = headless.render(1440, 900);
        ImageIO.write(image, "png", outputDir.resolve("studio-slider-selected.png").toFile());
        assertThat(session.index().findItem("position")).isNotNull();
    }

    @Test
    void aThemeWithExtremeNumbersOpensAndBuildsBothForms(@TempDir Path folder) throws Exception {
        Path theme = folder.resolve("theme.xml");
        Files.writeString(theme, """
                <?xml version="1.0" encoding="UTF-8"?>
                <!DOCTYPE Theme PUBLIC "-//VideoLAN//DTD VLC Skins V2.0//EN" "skin.dtd">
                <Theme version="2.0" magnet="9999" alpha="0">
                  <ThemeInfo name="ranges"/>
                  <Window id="w">
                    <Layout id="l" width="100" height="50" minwidth="5" maxwidth="99999"
                            minheight="5" maxheight="99999">
                      <Text id="t" x="0" y="0" width="10" text="hi" font="defaultfont"/>
                    </Layout>
                  </Window>
                </Theme>
                """, java.nio.charset.StandardCharsets.UTF_8);

        Studio studio = VlcSkinStudio.headlessStudio();
        ThemeManager.apply("dark");
        studio.settings().setTheme("dark");
        studio.openFile(theme);
        HeadlessStudio headless = new HeadlessStudio(studio);
        headless.render(900, 640);

        // The layout form used to throw on maxwidth="99999"; the theme form on magnet="9999".
        assertThat(findSpinner(headless, 99999)).as("layout maxwidth").isNotNull();
        headless.onEdt(() -> {
            studio.session().selection().selectWindow(null);
            studio.session().fireChanged();
        });
        assertThat(findSpinner(headless, 9999)).as("theme magnet").isNotNull();
        assertThat(findSpinner(headless, 0)).as("theme alpha").isNotNull();
        headless.render(900, 640);
    }

    @Test
    void interfaceScaleGrowsFontsAndRows(@TempDir Path folder) throws Exception {
        HeadlessStudio headless = studio(folder);
        int baseSize = javax.swing.UIManager.getFont("Label.font").getSize();
        int baseRow = javax.swing.UIManager.getInt("Tree.rowHeight");
        headless.onEdt(() -> headless.studio().applyFontScale(150));
        assertThat(javax.swing.UIManager.getFont("Label.font").getSize()).isGreaterThan(baseSize);
        assertThat(javax.swing.UIManager.getInt("Tree.rowHeight")).isGreaterThan(baseRow);
        BufferedImage image = headless.render(1440, 900);
        ImageIO.write(image, "png", outputDir.resolve("studio-scale150.png").toFile());
        assertThat(Files.size(outputDir.resolve("studio-scale150.png"))).isGreaterThan(20_000);
        headless.onEdt(() -> headless.studio().applyFontScale(100));
        assertThat(javax.swing.UIManager.getFont("Label.font").getSize()).isEqualTo(baseSize);
    }

    @Test
    void ctrlWheelZoomsTheCanvas(@TempDir Path folder) throws Exception {
        HeadlessStudio headless = studio(folder);
        var canvas = headless.canvas().canvasComponent();
        int before = headless.studio().settings().getCanvasZoom();
        headless.onEdt(() -> canvas.dispatchEvent(new java.awt.event.MouseWheelEvent(canvas,
                java.awt.event.MouseEvent.MOUSE_WHEEL, System.currentTimeMillis(),
                java.awt.event.InputEvent.CTRL_DOWN_MASK, 100, 100, 0, false,
                java.awt.event.MouseWheelEvent.WHEEL_UNIT_SCROLL, 1, -1)));
        assertThat(headless.studio().settings().getCanvasZoom()).isGreaterThan(before);

        // A trackpad pinch arrives as a ctrl-wheel on X11 and follows the same path.
        headless.onEdt(() -> canvas.dispatchEvent(new java.awt.event.MouseWheelEvent(canvas,
                java.awt.event.MouseEvent.MOUSE_WHEEL, System.currentTimeMillis(),
                java.awt.event.InputEvent.CTRL_DOWN_MASK, 100, 100, 0, false,
                java.awt.event.MouseWheelEvent.WHEEL_UNIT_SCROLL, 1, 1)));
        assertThat(headless.studio().settings().getCanvasZoom()).isEqualTo(before);
    }

    private JSpinner findSpinner(java.awt.Container container, int value) {
        for (java.awt.Component child : container.getComponents()) {
            if (child instanceof JSpinner spinner && Integer.valueOf(value).equals(spinner.getValue())) {
                return spinner;
            }
            if (child instanceof java.awt.Container nested) {
                JSpinner found = findSpinner(nested, value);
                if (found != null) {
                    return found;
                }
            }
        }
        return null;
    }

    private MouseEvent mouse(java.awt.Component target, int id, int x, int y) {
        return new MouseEvent(target, id, System.currentTimeMillis(), 0, x, y, 1, false);
    }
}
