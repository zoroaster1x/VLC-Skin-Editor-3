package dev.zoroaster1x.vlcskin.app;

import static org.assertj.core.api.Assertions.assertThat;

import dev.zoroaster1x.vlcskin.app.panel.CanvasPanel;
import dev.zoroaster1x.vlcskin.edit.EditorSession;
import dev.zoroaster1x.vlcskin.example.ExampleSkins;
import dev.zoroaster1x.vlcskin.model.ItemType;
import dev.zoroaster1x.vlcskin.model.item.Item;
import dev.zoroaster1x.vlcskin.render.Bounds;
import java.awt.Rectangle;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.nio.file.Files;
import java.nio.file.Path;
import javax.imageio.ImageIO;
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

    private MouseEvent mouse(java.awt.Component target, int id, int x, int y) {
        return new MouseEvent(target, id, System.currentTimeMillis(), 0, x, y, 1, false);
    }
}
