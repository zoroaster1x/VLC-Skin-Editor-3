package dev.zoroaster1x.vlcskin.mcp;

import static org.assertj.core.api.Assertions.assertThat;

import dev.zoroaster1x.vlcskin.example.ExampleSkins;
import java.nio.file.Path;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

/**
 * The disk-change notice and the merge that lets a session keep working when
 * another program edits the same file.
 */
class EditorDiskSyncTest {

    @Test
    void noticesAndMergesDiskChangesWithoutDiscardingEitherSide(@TempDir Path folder) throws Exception {
        Path theme = ExampleSkins.create(folder, ExampleSkins.NEON);
        EditorService service = new EditorService();
        assertThat(service.open(theme.toString()).error()).isFalse();
        service.beginToolCall("open_skin");
        service.endToolCall("open_skin", 1, false);

        // Our side edits the theme name.
        assertThat(service.setThemeProperty("name", "Our Name").error()).isFalse();
        service.beginToolCall("set_theme_property");
        service.endToolCall("set_theme_property", 1, false);

        // Another program edits the same file: a second service changes a
        // layout height and saves.
        EditorService other = new EditorService();
        assertThat(other.open(theme.toString()).error()).isFalse();
        assertThat(other.setLayoutProperty("main", "main", "height", "240").error()).isFalse();
        assertThat(other.save(null).error()).isFalse();

        // The next call sees the disk change and explains how to merge.
        String notice = service.beginToolCall("document_info");
        service.endToolCall("document_info", 1, false);
        assertThat(notice).as("disk notice").isNotNull();
        assertThat(notice).contains("disk").contains("sync_from_disk").contains("disk_diff");

        ToolOutcome diff = service.diskDiff();
        assertThat(diff.error()).isFalse();
        assertThat(diff.text()).contains("differences").contains("lines");

        ToolOutcome sync = service.syncFromDisk();
        assertThat(sync.error()).as(sync.text()).isFalse();
        @SuppressWarnings("unchecked")
        java.util.Map<String, Object> structured = (java.util.Map<String, Object>) sync.structured();
        assertThat(structured).containsEntry("conflicts", 0);

        // Both edits survive: our name and their height.
        assertThat(service.documentInfo().get("name")).isEqualTo("Our Name");
        @SuppressWarnings("unchecked")
        java.util.List<java.util.Map<String, Object>> layouts =
                (java.util.List<java.util.Map<String, Object>>) service.documentInfo().get("layoutsDetail");
        assertThat(layouts).anyMatch(layout -> Integer.valueOf(240).equals(layout.get("height")));
    }
}
