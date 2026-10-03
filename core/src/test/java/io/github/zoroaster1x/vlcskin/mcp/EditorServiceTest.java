package io.github.zoroaster1x.vlcskin.mcp;

import static org.assertj.core.api.Assertions.assertThat;

import io.github.zoroaster1x.vlcskin.edit.EditorSession;
import io.github.zoroaster1x.vlcskin.example.ExampleSkins;
import java.nio.file.Path;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class EditorServiceTest {

    private EditorService service(Path folder) throws Exception {
        Path theme = ExampleSkins.create(folder, ExampleSkins.NEON);
        EditorSession session = EditorSession.open(theme);
        return new EditorService(session);
    }

    @Test
    void documentInfoListsTheExample(@TempDir Path folder) throws Exception {
        EditorService service = service(folder);
        Map<String, Object> info = service.documentInfo();
        assertThat(info.get("name")).isEqualTo("Neon player");
        assertThat(info.get("windows")).isEqualTo(1);
        assertThat(info.get("items")).isEqualTo(12);
        assertThat((java.util.List<?>) info.get("problems")).isEmpty();
    }

    @Test
    void addMoveEditUndo(@TempDir Path folder) throws Exception {
        EditorService service = service(folder);
        ToolOutcome added = service.addItem("Text", "main", "main", null, 10, 10,
                Map.of("text", "Hello", "font", "defaultfont", "color", "#FFAA00"));
        assertThat(added.error()).isFalse();
        String id = (String) ((Map<?, ?>) added.structured()).get("id");

        assertThat(service.moveItem(id, 40, 60).error()).isFalse();
        assertThat(service.setItemProperty(id, "text", "Changed").error()).isFalse();
        assertThat(service.getItem(id).text()).contains("\"text\":\"Changed\"");

        EditorSession session = service.session();
        session.undo();
        assertThat(service.getItem(id).text()).contains("\"text\":\"Hello\"");
        session.undo();
        assertThat(service.getItem(id).text()).contains("\"x\":\"10\"");
        session.undo();
        assertThat(service.getItem(id).error()).isTrue();
    }

    @Test
    void unknownAttributeIsRejected(@TempDir Path folder) throws Exception {
        EditorService service = service(folder);
        ToolOutcome outcome = service.setItemProperty("play_btn", "banana", "yes");
        assertThat(outcome.error()).isTrue();
        assertThat(outcome.text()).contains("Unknown attribute");
    }

    @Test
    void renderToolReturnsPngAndGeometry(@TempDir Path folder) throws Exception {
        EditorService service = service(folder);
        ToolOutcome outcome = service.renderLayout(null, null, 2, null);
        assertThat(outcome.error()).isFalse();
        assertThat(outcome.png()).isNotEmpty();
        assertThat(outcome.structured()).isInstanceOf(
                io.github.zoroaster1x.vlcskin.describe.LayoutDescription.class);
        var description = (io.github.zoroaster1x.vlcskin.describe.LayoutDescription) outcome.structured();
        assertThat(description.layoutWidth()).isEqualTo(320);
        assertThat(description.zoom()).isEqualTo(2);
        assertThat(description.items()).hasSize(12);
        var play = description.items().stream().filter(node -> node.id().equals("play_btn")).findFirst();
        assertThat(play).isPresent();
        assertThat(play.get().x()).isEqualTo(68);
    }

    @Test
    void validationFindsBrokenReferences(@TempDir Path folder) throws Exception {
        EditorService service = service(folder);
        service.setItemProperty("play_btn", "up", "missing_bitmap");
        ToolOutcome outcome = service.validate();
        assertThat(outcome.text()).contains("problems found");
        assertThat(outcome.text()).contains("missing resource");
    }

    @Test
    void playtreesAcceptExactlyOneSlider(@TempDir Path folder) throws Exception {
        EditorService service = service(folder);
        var playlist = service.addItem("Playlist", "main", "main", null, 200, 20, null);
        assertThat(playlist.error()).isFalse();
        String playlistId = (String) ((Map<?, ?>) playlist.structured()).get("id");
        var playtree = (io.github.zoroaster1x.vlcskin.model.item.PlaytreeItem)
                service.session().index().findItem(playlistId);
        assertThat(playtree.getSlider()).as("a new playlist comes with its scroll slider").isNotNull();
        assertThat(service.addItem("Slider", null, null, playlistId, null, null, null).error())
                .as("a second slider is refused")
                .isTrue();
        assertThat(service.addItem("Text", null, null, playlistId, null, null, null).error())
                .as("only a slider may enter a playlist")
                .isTrue();
        assertThat(service.duplicateItem(playtree.getSlider().getId()).error())
                .as("the playlist slider cannot be duplicated")
                .isTrue();
        assertThat(service.deleteItem(playtree.getSlider().getId()).error()).isFalse();
        assertThat(playtree.getSlider()).isNull();
    }

    @Test
    void duplicateIdsAreRejected(@TempDir Path folder) throws Exception {
        EditorService service = service(folder);
        ToolOutcome outcome = service.setItemProperty("play_btn", "id", "next_btn");
        assertThat(outcome.error()).isTrue();
        assertThat(outcome.text()).contains("already used");
        assertThat(service.setItemProperty("play_btn", "id", "").error()).isTrue();
    }

    @Test
    void subBitmapsCanBeAddedEditedAndDeleted(@TempDir Path folder) throws Exception {
        EditorService service = service(folder);
        assertThat(service.addResource("bitmap", "sheet", "play.png", Map.of()).error()).isFalse();
        assertThat(service.addSubBitmap("sheet", "icon_a", 0, 0, 12, 12).error()).isFalse();
        assertThat(service.addSubBitmap("sheet", "icon_b", 12, 0, 12, 12).error()).isFalse();
        assertThat(service.setSubBitmapProperty("sheet", "icon_a", "width", "14").error()).isFalse();

        var image = service.addItem("Image", "main", "main", null, 0, 0,
                Map.of("image", "icon_a"));
        assertThat(image.error()).isFalse();
        assertThat(service.deleteSubBitmap("sheet", "icon_a").error())
                .as("an id still used by an item cannot be deleted")
                .isTrue();
        assertThat(service.deleteSubBitmap("sheet", "icon_b").error()).isFalse();
        assertThat(service.deleteSubBitmap("sheet", "missing").error()).isTrue();
    }

    @Test
    void nestedItemsResolveTheirOwnParentList(@TempDir Path folder) throws Exception {
        EditorService service = service(folder);
        var group = service.addItem("Group", "main", "main", null, 50, 50, null);
        String groupId = (String) ((Map<?, ?>) group.structured()).get("id");
        var text = service.addItem("Text", null, null, groupId, 5, 5, Map.of("text", "Nested"));
        assertThat(text.error()).isFalse();
        String textId = (String) ((Map<?, ?>) text.structured()).get("id");

        assertThat(service.moveItem(textId, 9, 9).error()).isFalse();
        assertThat(service.deleteItem(textId).error()).isFalse();
        service.session().undo();
        assertThat(service.getItem(textId).error()).isFalse();
        var parent = service.session().index().parentItemOf(textId);
        assertThat(parent).isNotNull();
        assertThat(parent.getId()).isEqualTo(groupId);
    }

    @Test
    void allToolsHaveNamesAndSchemas(@TempDir Path folder) throws Exception {
        EditorService service = service(folder);
        var tools = new McpToolset(service).tools();
        assertThat(tools).hasSizeGreaterThan(20);
        assertThat(tools).allSatisfy(tool -> {
            assertThat(tool.name()).matches("[a-z_]+");
            assertThat(tool.description()).isNotBlank();
            assertThat(tool.inputSchema()).containsKey("type");
        });
        assertThat(tools).extracting(ToolSpec::name).doesNotHaveDuplicates();
    }

    @Test
    void toolHandlersWorkThroughTheCatalog(@TempDir Path folder) throws Exception {
        EditorService service = service(folder);
        var tools = new McpToolset(service).tools();
        ToolSpec render = tools.stream().filter(tool -> tool.name().equals("render_layout")).findFirst().orElseThrow();
        ToolOutcome outcome = render.call(Map.of("zoom", 1));
        assertThat(outcome.error()).isFalse();
        assertThat(outcome.hasPng()).isTrue();

        ToolSpec add = tools.stream().filter(tool -> tool.name().equals("add_item")).findFirst().orElseThrow();
        ToolOutcome added = add.call(Map.of("type", "Image", "x", 5, "y", 6,
                "properties", Map.of("image", "play")));
        assertThat(added.error()).isFalse();
    }
}
