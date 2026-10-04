package dev.zoroaster1x.vlcskin.app;

import dev.zoroaster1x.vlcskin.app.panel.CanvasPanel;
import dev.zoroaster1x.vlcskin.edit.EditorSession;
import dev.zoroaster1x.vlcskin.example.ExampleSkins;
import dev.zoroaster1x.vlcskin.model.ItemType;
import dev.zoroaster1x.vlcskin.model.SkinLayout;
import dev.zoroaster1x.vlcskin.model.item.Item;
import dev.zoroaster1x.vlcskin.render.Bounds;
import dev.zoroaster1x.vlcskin.render.RenderOptions;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import javax.imageio.ImageIO;
import javax.swing.SwingUtilities;

/**
 * Generates the artifacts under screenshots/: stills and animated GIFs of the
 * headless harness doing real UI work. Action frames run at 30fps; key frames
 * hold. Run with:
 *
 * <pre>./gradlew :app:uiScreenshots</pre>
 */
public final class UiShots {

    private static final int WIDTH = 1120;
    private static final int HEIGHT = 720;
    private static final int FRAME_MS = 33;
    private static final int HOLD_MS = 1400;

    private final Path outputDir;
    private final Path velocityTheme;
    private final List<String> written = new ArrayList<>();
    private HeadlessStudio headless;
    private Studio studio;
    private EditorSession session;

    private UiShots(Path outputDir, Path velocityTheme) {
        this.outputDir = outputDir;
        this.velocityTheme = velocityTheme;
    }

    public static void main(String[] args) throws Exception {
        Path output = Path.of(args.length > 0 ? args[0] : "screenshots").toAbsolutePath();
        Path velocity = args.length > 1 && !args[1].isBlank() ? Path.of(args[1]) : null;
        Files.createDirectories(output);
        new UiShots(output, velocity).run();
        try (var files = Files.list(output)) {
            System.out.println("Wrote " + files.count() + " files to " + output);
        }
        System.exit(0);
    }

    private void start() throws Exception {
        Path work = Files.createTempDirectory("vlc-skin-ui-shots");
        mcpRoot = work.resolve("mcp-cache");
        Path theme = ExampleSkins.create(work, ExampleSkins.NEON);
        dev.zoroaster1x.vlcskin.app.theme.ThemeManager.apply("dark");
        studio = VlcSkinStudio.headlessStudio();
        studio.openFile(theme);
        headless = new HeadlessStudio(studio);
        session = studio.session();
        headless.render(WIDTH, HEIGHT);
    }

    private Path mcpRoot;

    private void run() throws Exception {
        start();
        still("studio-dark-overview.png");
        still("example-neon.png", this::renderExampleStill);

        recordSelectDragUndo();
        recordSliderPath();
        recordPreviewVariables();
        recordCanvasZoom();
        recordThemeSwitch();
        recordItemsAndInspector();
        recordValidation();
        recordFullTour();

        onEdt(() -> headless.applyTheme("light"));
        still("studio-light-overview.png");
        onEdt(() -> headless.applyTheme("dark"));

        // The MCP activity panel, with a realistic log written to a temp cache.
        dev.zoroaster1x.vlcskin.mcp.McpLog.useRoot(mcpRoot);
        dev.zoroaster1x.vlcskin.mcp.McpLog.started(dev.zoroaster1x.vlcskin.Version.VERSION,
                session.file() == null ? null : session.file().toString());
        dev.zoroaster1x.vlcskin.mcp.McpLog.call("document_info", 9, false, null, null);
        dev.zoroaster1x.vlcskin.mcp.McpLog.call("layout_tree", 21, false, null, null);
        dev.zoroaster1x.vlcskin.mcp.McpLog.call("render_layout", 48, false, null, null);
        dev.zoroaster1x.vlcskin.mcp.McpLog.call("add_item", 6, false, null,
                "[NOTICE] the file changed on disk outside this server");
        onEdt(() -> {
            headless.showNamedPanel("mcp");
            headless.panels().mcp.refresh();
        });
        still("studio-mcp-activity.png");
        onEdt(() -> headless.showNamedPanel("problems"));
        dev.zoroaster1x.vlcskin.mcp.McpLog.resetRoot();

        if (velocityTheme != null && Files.exists(velocityTheme)) {
            stillVelocity();
        }
        writeContactSheet();
    }

    /**
     * One animated scenario with per-frame delays and duplicate collapsing.
     */
    private static final class Recorder {

        private final List<BufferedImage> frames = new ArrayList<>();
        private final List<Integer> delays = new ArrayList<>();
        private String caption = "";

        void caption(String text) {
            caption = text;
        }

        void add(BufferedImage image, int delayMs) {
            BufferedImage captioned = withCaption(image, caption);
            if (!frames.isEmpty() && similar(frames.get(frames.size() - 1), captioned)) {
                delays.set(delays.size() - 1, delays.get(delays.size() - 1) + delayMs);
                return;
            }
            frames.add(captioned);
            delays.add(delayMs);
        }

        void write(Path target) throws Exception {
            GifWriter.write(frames, delays, target);
        }
    }

    private void record(String fileName, Scenario scenario) throws Exception {
        Recorder recorder = new Recorder();
        scenario.run(recorder);
        recorder.write(outputDir.resolve(fileName));
        written.add(fileName);
    }

    private interface Scenario {
        void run(Recorder recorder) throws Exception;
    }

    private void recordSelectDragUndo() throws Exception {
        record("select-drag-undo.gif", recorder -> {
            recorder.caption("Click the Play button on the canvas");
            recorder.add(frame(), HOLD_MS);
            Item play = session.index().findItem("play_btn");
            Rectangle start = canvasPoint(play, 0.5, 0.5);
            drag(recorder, play, start, 28, 8, 10);
            recorder.caption("The move is one undo step");
            recorder.add(frame(), HOLD_MS);
            onEdt(session::undo);
            recorder.caption("Undo restores it exactly");
            recorder.add(frame(), HOLD_MS);
        });
    }

    private void recordSliderPath() throws Exception {
        record("slider-path-editing.gif", recorder -> {
            onEdt(() -> {
                session.selection().selectItem("position");
                headless.panels().canvas.setTool(CanvasPanel.Tool.PATH);
                session.fireChanged();
            });
            recorder.caption("Path tool: drag a slider control point");
            recorder.add(frame(), HOLD_MS);
            Item slider = session.index().findItem("position");
            Rectangle point = canvasPoint(slider, 0.0, 0.0);
            int zoom = studio.settings().getCanvasZoom();
            var surface = headless.canvas().canvasComponent();
            onEdt(() -> surface.dispatchEvent(mouse(surface, MouseEvent.MOUSE_PRESSED, point.x, point.y)));
            for (int i = 1; i <= 8; i++) {
                int y = point.y - 22 * zoom * i / 8;
                onEdt(() -> surface.dispatchEvent(mouse(surface, MouseEvent.MOUSE_DRAGGED, point.x, y)));
                recorder.caption("The bezier path follows; VLC previews it the same way");
                recorder.add(frame(), FRAME_MS);
            }
            onEdt(() -> surface.dispatchEvent(mouse(surface, MouseEvent.MOUSE_RELEASED, point.x, point.y - 22 * zoom)));
            recorder.add(frame(), HOLD_MS);
            onEdt(session::undo);
            onEdt(() -> headless.panels().canvas.setTool(CanvasPanel.Tool.MOVE));
            recorder.caption("Undo restores the original path");
            recorder.add(frame(), HOLD_MS);
        });
    }

    private void recordPreviewVariables() throws Exception {
        record("preview-variables.gif", recorder -> {
            onEdt(() -> {
                headless.showRightTab(dev.zoroaster1x.vlcskin.app.i18n.PanelTitles.variables());
                session.variables().setBoolean("vlc.isPlaying", true);
                session.variables().setBoolean("vlc.isPaused", false);
                session.fireChanged();
            });
            recorder.caption("Variables panel: simulate the player state");
            recorder.add(frame(), HOLD_MS);
            for (int i = 0; i <= 12; i++) {
                float value = 0.5f + 0.5f * i / 12f;
                onEdt(() -> {
                    session.variables().setSliderValue(value);
                    session.fireChanged();
                });
                recorder.caption("Drag the slider position and the preview follows");
                recorder.add(frame(), FRAME_MS);
            }
            recorder.add(frame(), HOLD_MS);
            onEdt(() -> {
                session.variables().setSliderValue(0.5f);
                session.fireChanged();
                headless.showRightTab(dev.zoroaster1x.vlcskin.app.i18n.PanelTitles.inspector());
            });
        });
    }

    private void recordCanvasZoom() throws Exception {
        record("canvas-zoom.gif", recorder -> {
            recorder.caption("Zoom from 1x to 3x, then fit");
            for (int i = 0; i < 6; i++) {
                onEdt(i % 2 == 0 ? () -> headless.canvas().zoomOut() : () -> headless.canvas().zoomIn());
                recorder.add(frame(), FRAME_MS);
            }
            recorder.add(frame(), HOLD_MS);
            onEdt(() -> studio.settings().setCanvasZoom(1));
            onEdt(() -> headless.canvas().refresh());
            recorder.add(frame(), HOLD_MS);
            onEdt(() -> headless.canvas().fitToWindow());
            recorder.caption("Fit window sizes the canvas to the panel");
            recorder.add(frame(), HOLD_MS);
        });
    }

    private void recordThemeSwitch() throws Exception {
        record("theme-switch.gif", recorder -> {
            recorder.caption("Dark theme");
            recorder.add(frame(), HOLD_MS);
            for (String theme : List.of("intellij", "arc", "one-dark", "light")) {
                onEdt(() -> headless.applyTheme(theme));
                recorder.caption("Theme: " + theme);
                recorder.add(frame(), HOLD_MS);
            }
            onEdt(() -> headless.applyTheme("dark"));
        });
    }

    private void recordItemsAndInspector() throws Exception {
        record("items-and-inspector.gif", recorder -> {
            for (String id : List.of("background_img", "artist", "close_btn", "position")) {
                Item item = session.index().findItem(id);
                onEdt(() -> {
                    session.selection().selectItem(id);
                    session.fireChanged();
                });
                recorder.caption("Select " + item.type().displayName() + ": " + id);
                recorder.add(frame(), HOLD_MS);
            }
            recorder.caption("Add a Text item from the Items panel");
            onEdt(() -> headless.panels().items.addToRoot(ItemType.TEXT));
            recorder.add(frame(), HOLD_MS);
            onEdt(session::undo);
            recorder.caption("Undo removes it again");
            recorder.add(frame(), HOLD_MS);
        });
    }

    private void recordValidation() throws Exception {
        record("problems-validation.gif", recorder -> {
            onEdt(() -> headless.panels().problems.validate());
            recorder.caption("Validate: the Problems panel lists real issues");
            recorder.add(frame(), HOLD_MS);
            EditorSession target = session;
            onEdt(() -> studio.service().setItemProperty("play_btn", "up", "not_a_bitmap"));
            onEdt(() -> headless.panels().problems.validate());
            recorder.caption("Break a reference and validate again");
            recorder.add(frame(), HOLD_MS);
            onEdt(target::undo);
            onEdt(() -> headless.panels().problems.validate());
            recorder.caption("Undo and the problem disappears");
            recorder.add(frame(), HOLD_MS);
        });
    }

    private void recordFullTour() throws Exception {
        Recorder tour = new Recorder();
        tour.caption("VLC Skin Studio: the neon example open in the editor");
        tour.add(frame(), HOLD_MS);
        tour.caption("Drag items on the canvas");
        drag(tour, session.index().findItem("play_btn"), canvasPoint(session.index().findItem("play_btn"), 0.5, 0.5),
                20, 6, 8);
        tour.add(frame(), HOLD_MS);
        onEdt(session::undo);
        tour.caption("Undo everything, it is all command based");
        tour.add(frame(), HOLD_MS);
        onEdt(() -> session.selection().selectItem("volume"));
        onEdt(() -> studio.setDarkTheme(false));
        tour.caption("Light theme");
        tour.add(frame(), HOLD_MS);
        onEdt(() -> studio.setDarkTheme(true));
        tour.caption("And back to dark");
        tour.add(frame(), HOLD_MS);
        onEdt(() -> headless.showRightTab(dev.zoroaster1x.vlcskin.app.i18n.PanelTitles.variables()));
        tour.caption("Global variables drive the live preview");
        tour.add(frame(), HOLD_MS);
        onEdt(() -> headless.showRightTab(dev.zoroaster1x.vlcskin.app.i18n.PanelTitles.inspector()));
        tour.caption("Ready for the next skin");
        tour.add(frame(), HOLD_MS);
        tour.write(outputDir.resolve("full-tour.gif"));
        written.add("full-tour.gif");
        this.tourFrames = tour.frames;
    }

    private List<BufferedImage> tourFrames = List.of();

    private void drag(Recorder recorder, Item item, Rectangle start, int dx, int dy, int steps) throws Exception {
        int zoom = studio.settings().getCanvasZoom();
        var surface = headless.canvas().canvasComponent();
        onEdt(() -> {
            session.selection().selectItem(item.getId());
            session.fireChanged();
            surface.dispatchEvent(mouse(surface, MouseEvent.MOUSE_PRESSED, start.x, start.y));
        });
        for (int i = 1; i <= steps; i++) {
            int x = start.x + dx * zoom * i / steps;
            int y = start.y + dy * zoom * i / steps;
            onEdt(() -> surface.dispatchEvent(mouse(surface, MouseEvent.MOUSE_DRAGGED, x, y)));
            recorder.add(frame(), FRAME_MS);
        }
        onEdt(() -> surface.dispatchEvent(mouse(surface, MouseEvent.MOUSE_RELEASED,
                start.x + dx * zoom, start.y + dy * zoom)));
    }

    private Rectangle canvasPoint(Item item, double relativeX, double relativeY) {
        Rectangle canvas = headless.canvas().canvasBounds();
        int zoom = studio.settings().getCanvasZoom();
        Rectangle bounds = Bounds.of(item, session.index(), session.images(), session.variables());
        return new Rectangle(canvas.x + (int) ((bounds.x + bounds.width * relativeX) * zoom),
                canvas.y + (int) ((bounds.y + bounds.height * relativeY) * zoom), 0, 0);
    }

    private BufferedImage frame() throws Exception {
        BufferedImage[] holder = new BufferedImage[1];
        onEdt(() -> holder[0] = headless.render(WIDTH, HEIGHT));
        return holder[0];
    }

    private void still(String name) throws Exception {
        ImageIO.write(frame(), "png", outputDir.resolve(name).toFile());
        written.add(name);
    }

    private void still(String name, java.util.function.Supplier<BufferedImage> supplier) throws Exception {
        ImageIO.write(supplier.get(), "png", outputDir.resolve(name).toFile());
        written.add(name);
    }

    private BufferedImage renderExampleStill() {
        SkinLayout layout = session.currentLayout();
        RenderOptions options = RenderOptions.of(session.variables()).withZoom(2).withoutCheckerboard();
        return session.renderer().render(layout, options);
    }

    private void stillVelocity() throws Exception {
        dev.zoroaster1x.vlcskin.format.SkinParser.Result result =
                dev.zoroaster1x.vlcskin.format.SkinParser.parse(velocityTheme);
        Studio velocityStudio = VlcSkinStudio.headlessStudio();
        var outcome = velocityStudio.service().open(velocityTheme.toAbsolutePath().toString());
        if (outcome.error()) {
            return;
        }
        EditorSession velocity = velocityStudio.session();
        for (var window : velocity.theme().getWindows()) {
            for (var layout : window.getLayouts()) {
                String name = "velocity-" + window.getId() + "-" + layout.getId() + ".png";
                RenderOptions options = RenderOptions.of(velocity.variables()).withZoom(1).withoutCheckerboard();
                BufferedImage image = velocity.renderer().render(layout, options);
                ImageIO.write(image, "png", outputDir.resolve(name).toFile());
                written.add(name);
            }
        }
    }

    private void writeContactSheet() throws Exception {
        if (tourFrames.isEmpty()) {
            return;
        }
        int columns = 3;
        int rows = (int) Math.ceil(tourFrames.size() / (double) columns);
        int cellWidth = 360;
        int cellHeight = 232;
        BufferedImage sheet = new BufferedImage(columns * cellWidth + (columns + 1) * 8,
                rows * cellHeight + (rows + 1) * 8, BufferedImage.TYPE_INT_RGB);
        Graphics2D g = sheet.createGraphics();
        try {
            g.setColor(new Color(0x18, 0x1A, 0x1F));
            g.fillRect(0, 0, sheet.getWidth(), sheet.getHeight());
            for (int i = 0; i < tourFrames.size(); i++) {
                int column = i % columns;
                int row = i / columns;
                int x = 8 + column * (cellWidth + 8);
                int y = 8 + row * (cellHeight + 8);
                g.drawImage(tourFrames.get(i), x, y, cellWidth, cellHeight, null);
            }
        } finally {
            g.dispose();
        }
        ImageIO.write(sheet, "png", outputDir.resolve("full-tour-frames.png").toFile());
        written.add("full-tour-frames.png");
    }

    private static BufferedImage withCaption(BufferedImage source, String text) {
        if (text == null || text.isBlank()) {
            return source;
        }
        BufferedImage out = new BufferedImage(source.getWidth(), source.getHeight(), BufferedImage.TYPE_INT_RGB);
        Graphics2D g = out.createGraphics();
        try {
            g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
            g.drawImage(source, 0, 0, null);
            g.setColor(new Color(12, 14, 18, 235));
            g.fillRoundRect(14, source.getHeight() - 48, source.getWidth() - 28, 34, 10, 10);
            g.setColor(new Color(0xE0, 0x6C, 0x38));
            g.fillRect(20, source.getHeight() - 40, 4, 18);
            g.setColor(new Color(0xE8, 0xEA, 0xEE));
            g.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 13));
            g.drawString(text, 34, source.getHeight() - 26);
        } finally {
            g.dispose();
        }
        return out;
    }

    private static boolean similar(BufferedImage a, BufferedImage b) {
        if (a.getWidth() != b.getWidth() || a.getHeight() != b.getHeight()) {
            return false;
        }
        long diff = 0;
        int samples = 0;
        for (int y = 0; y < a.getHeight(); y += 23) {
            for (int x = 0; x < a.getWidth(); x += 23) {
                samples++;
                if (a.getRGB(x, y) != b.getRGB(x, y)) {
                    diff++;
                }
            }
        }
        return diff < Math.max(1, samples / 2000);
    }

    private void onEdt(Runnable task) throws Exception {
        if (SwingUtilities.isEventDispatchThread()) {
            task.run();
        } else {
            SwingUtilities.invokeAndWait(task);
        }
    }

    private MouseEvent mouse(java.awt.Component target, int id, int x, int y) {
        return new MouseEvent(target, id, System.currentTimeMillis(), 0, x, y, 1, false);
    }
}
