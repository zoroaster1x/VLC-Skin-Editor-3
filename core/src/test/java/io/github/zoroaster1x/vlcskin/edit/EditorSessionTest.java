package io.github.zoroaster1x.vlcskin.edit;

import static org.assertj.core.api.Assertions.assertThat;

import io.github.zoroaster1x.vlcskin.example.ExampleSkins;
import io.github.zoroaster1x.vlcskin.model.item.Item;
import java.nio.file.Path;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

/** Session behavior: coalesced nudges, dirty state and revision bumps. */
class EditorSessionTest {

    @Test
    void repeatedNudgesAreOneUndoStep(@TempDir Path folder) throws Exception {
        Path theme = ExampleSkins.create(folder, ExampleSkins.NEON);
        EditorSession session = EditorSession.open(theme);
        Item play = session.index().findItem("play_btn");
        int startX = play.getX();
        int startY = play.getY();
        session.nudge(play, 1, 0);
        session.nudge(play, 1, 0);
        session.nudge(play, 0, 1);
        assertThat(play.getX()).isEqualTo(startX + 2);
        assertThat(play.getY()).isEqualTo(startY + 1);
        assertThat(session.history().undoDescription()).isEqualTo("Move Button");
        session.undo();
        assertThat(play.getX()).isEqualTo(startX);
        assertThat(play.getY()).isEqualTo(startY);
    }

    @Test
    void nudgeOfAnotherItemStartsANewStep(@TempDir Path folder) throws Exception {
        Path theme = ExampleSkins.create(folder, ExampleSkins.NEON);
        EditorSession session = EditorSession.open(theme);
        Item play = session.index().findItem("play_btn");
        Item next = session.index().findItem("next_btn");
        session.nudge(play, 1, 0);
        session.nudge(next, 1, 0);
        session.undo();
        assertThat(next.getX()).isEqualTo(108);
        assertThat(play.getX()).isEqualTo(69);
    }

    @Test
    void revisionTracksChanges(@TempDir Path folder) throws Exception {
        Path theme = ExampleSkins.create(folder, ExampleSkins.NEON);
        EditorSession session = EditorSession.open(theme);
        long before = session.revision();
        session.selection().selectItem("play_btn");
        session.fireChanged();
        assertThat(session.revision()).isGreaterThan(before);
    }
}
