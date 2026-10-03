package dev.zoroaster1x.vlcskin.mcp;

import static org.assertj.core.api.Assertions.assertThat;

import dev.zoroaster1x.vlcskin.example.ExampleSkins;
import dev.zoroaster1x.vlcskin.model.item.SliderItem;
import dev.zoroaster1x.vlcskin.snapshot.UiInspector;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import javax.imageio.ImageIO;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

/** The control surface behind the second half of the MCP tool catalog. */
class EditorControlTest {

    /** A host that records what the tools asked for. */
    private static final class StubHost implements UiInspector {
        final Map<String, String> preferences = new LinkedHashMap<>();
        String theme;
        String panel;
        Integer zoom;
        String tool;
        Boolean checkerboard;
        boolean settingsOpened;

        StubHost() {
            preferences.put("theme", "dark");
            preferences.put("language", "en");
        }

        @Override
        public boolean available() {
            return true;
        }

        @Override
        public String describeUi() {
            return "{}";
        }

        @Override
        public byte[] screenshotPng() {
            return null;
        }

        @Override
        public String summary() {
            return "stub";
        }

        @Override
        public boolean applyTheme(String themeId) {
            theme = themeId;
            preferences.put("theme", themeId);
            return true;
        }

        @Override
        public boolean showPanel(String panelName) {
            panel = panelName;
            return "Inspector".equalsIgnoreCase(panelName);
        }

        @Override
        public boolean setCanvas(Integer newZoom, String newTool, Boolean newCheckerboard) {
            zoom = newZoom;
            tool = newTool;
            checkerboard = newCheckerboard;
            return true;
        }

        @Override
        public boolean openSettings() {
            settingsOpened = true;
            return true;
        }

        @Override
        public Map<String, String> preferences() {
            return Map.copyOf(preferences);
        }

        @Override
        public boolean setPreference(String key, String value) {
            preferences.put(key, value);
            return true;
        }
    }

    private EditorService service(Path folder) throws Exception {
        Path theme = ExampleSkins.create(folder, ExampleSkins.NEON);
        return new EditorService(dev.zoroaster1x.vlcskin.edit.EditorSession.open(theme));
    }

    private EditorControl control(Path folder) throws Exception {
        return new EditorControl(service(folder));
    }

    @Test
    void historyUndoRedo(@TempDir Path folder) throws Exception {
        EditorControl control = control(folder);
        control.nudgeItem("play_btn", 3, 0);
        assertThat(control.historyState().text()).contains("\"canUndo\":true");
        assertThat(control.undo().error()).isFalse();
        assertThat(control.historyState().text()).contains("\"canRedo\":true");
        control.redo();
        assertThat(control.selectionState().error()).isFalse();
    }

    @Test
    void selectionAndNudge(@TempDir Path folder) throws Exception {
        EditorControl control = control(folder);
        assertThat(control.select("item", "play_btn").error()).isFalse();
        assertThat(control.selectionState().text()).contains("\"item\":\"play_btn\"");
        assertThat(control.select("banana", "x").error()).isTrue();
        int before = control.selectionState().structured().toString().length();
        assertThat(control.nudgeItem("play_btn", 2, 1).error()).isFalse();
        assertThat(before).isPositive();
    }

    @Test
    void reorderAndReparent(@TempDir Path folder) throws Exception {
        EditorControl control = control(folder);
        assertThat(control.reorderItem("background_img", "front").error()).isFalse();
        assertThat(control.reparentItem("artist", "background_img", null).error())
                .as("an image cannot contain children")
                .isTrue();
        var group = control.service().addItem("Group", "main", "main", null, 5, 5, null);
        assertThat(group.error()).isFalse();
        String groupId = (String) ((Map<?, ?>) group.structured()).get("id");
        assertThat(control.reparentItem("artist", groupId, 0).error()).isFalse();
        assertThat(control.service().session().index().parentItemOf("artist").getId()).isEqualTo(groupId);
        assertThat(control.reparentItem("artist", null, null).error()).isFalse();
        assertThat(control.service().session().index().parentItemOf("artist")).isNull();
    }

    @Test
    void xmlRoundTrip(@TempDir Path folder) throws Exception {
        EditorControl control = control(folder);
        String xml = (String) ((Map<?, ?>) control.getXml().structured()).get("xml");
        assertThat(xml).contains("<Theme");
        ToolOutcome applied = control.applyXml(xml);
        assertThat(applied.error()).isFalse();
        assertThat(control.applyXml("<Theme>").error()).isTrue();
    }

    @Test
    void savePreviewAndReload(@TempDir Path folder) throws Exception {
        EditorControl control = control(folder);
        Path png = folder.resolve("out/preview");
        ToolOutcome saved = control.savePreview(png.toString(), null, null, 1);
        assertThat(saved.error()).isFalse();
        assertThat(folder.resolve("out/preview.png")).exists();
        assertThat(control.reloadImages().error()).isFalse();
    }

    @Test
    void duplicateResourceAndGet(@TempDir Path folder) throws Exception {
        EditorControl control = control(folder);
        assertThat(control.getResource("play").text()).contains("Bitmap");
        assertThat(control.duplicateResource("play", "%oldid%_2").error()).isFalse();
        assertThat(control.service().session().index().findResource("play_2")).isNotNull();
    }

    @Test
    void duplicateSubBitmap(@TempDir Path folder) throws Exception {
        EditorControl control = control(folder);
        assertThat(control.service().addSubBitmap("play", "play_part", 0, 0, 4, 4).error()).isFalse();
        assertThat(control.duplicateResource("play_part", "%oldid%_copy").error()).isFalse();
        var copy = control.service().session().index().findImage("play_part_copy");
        assertThat(copy).isNotNull();
        assertThat(copy.sub()).isNotNull();
    }

    @Test
    void sliderBackgroundGenerator(@TempDir Path folder) throws Exception {
        EditorControl control = control(folder);
        BufferedImage middle = new BufferedImage(8, 8, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = middle.createGraphics();
        g.setColor(Color.RED);
        g.fillRect(0, 0, 8, 8);
        g.dispose();
        Path middleFile = folder.resolve("middle.png");
        ImageIO.write(middle, "png", middleFile.toFile());

        Map<String, Object> args = new LinkedHashMap<>();
        args.put("slider", "position");
        args.put("middle", middleFile.toString());
        args.put("width", 100);
        args.put("height", 12);
        args.put("orientation", "horizontal");
        ToolOutcome generated = control.generateSliderBackground(args);
        assertThat(generated.error()).as(generated.text()).isFalse();
        assertThat(folder.resolve("position_bg.png")).exists();
        var slider = (SliderItem) control.service().session().index().findItem("position");
        assertThat(slider.getBackground()).isNotNull();
        assertThat(slider.getBackground().getNbvert()).isEqualTo(101);
        assertThat(control.service().session().theme().getResources().stream()
                .anyMatch(resource -> resource.getId().contains("position"))).isTrue();
    }

    @Test
    void preferencesAndCanvasThroughTheHost(@TempDir Path folder) throws Exception {
        EditorService service = service(folder);
        StubHost host = new StubHost();
        service.setUi(host);
        EditorControl control = new EditorControl(service);
        assertThat(control.getPreferences().text()).contains("\"theme\":\"dark\"");
        assertThat(control.setPreferences(Map.of("theme", "light", "language", "de")).error()).isFalse();
        assertThat(host.theme).isEqualTo("light");
        assertThat(host.preferences.get("language")).isEqualTo("de");
        assertThat(control.setCanvas(3, "path", true).error()).isFalse();
        assertThat(host.zoom).isEqualTo(3);
        assertThat(host.tool).isEqualTo("path");
        assertThat(host.checkerboard).isTrue();
        assertThat(control.showPanel("Inspector").error()).isFalse();
        assertThat(host.panel).isEqualTo("Inspector");
        assertThat(control.showPanel("Nope").text()).contains("No panel");
        assertThat(control.openSettings().error()).isFalse();
        assertThat(host.settingsOpened).isTrue();
    }

    @Test
    void resetSkinStartsEmpty(@TempDir Path folder) throws Exception {
        EditorControl control = control(folder);
        assertThat(control.resetSkin().error()).isFalse();
        assertThat(control.service().documentInfo().get("items")).isEqualTo(0);
    }

    @Test
    void everyControlToolHasAHandler(@TempDir Path folder) throws Exception {
        EditorService service = service(folder);
        var tools = new McpToolset(service).tools();
        assertThat(tools).hasSizeGreaterThan(50);
        List<String> names = tools.stream().map(ToolSpec::name).toList();
        assertThat(names).contains("undo", "redo", "select_element", "reparent_item", "apply_xml",
                "save_preview", "test_in_vlc", "generate_slider_background", "set_preferences",
                "set_canvas", "show_panel", "check_for_updates");
        assertThat(names).doesNotHaveDuplicates();
    }
}
