package dev.zoroaster1x.vlcskin.model;

import static org.assertj.core.api.Assertions.assertThat;

import dev.zoroaster1x.vlcskin.model.item.TextItem;
import org.junit.jupiter.api.Test;

/**
 * Copy id generation from a rename pattern.
 */
class SkinIndexTest {

    private SkinIndex indexWith(String id) {
        SkinTheme theme = new SkinTheme();
        SkinWindow window = new SkinWindow();
        window.setId("main");
        SkinLayout layout = new SkinLayout();
        layout.setId("default");
        layout.setWidth(100);
        layout.setHeight(100);
        TextItem item = new TextItem();
        item.setId(id);
        layout.getItems().add(item);
        window.getLayouts().add(layout);
        theme.getWindows().add(window);
        return new SkinIndex(theme);
    }

    @Test
    void copyPatternKeepsEveryLetter() {
        SkinIndex index = indexWith("play");
        // A pattern with an s in it used to lose the letter: %oldid%_slider became _lider.
        assertThat(index.uniqueCopy("%oldid%_slider", "play")).isEqualTo("play_slider");
        assertThat(index.uniqueCopy("%oldid%_copy", "play")).isEqualTo("play_copy");
        assertThat(index.uniqueCopy("copy of %oldid%", "play")).isEqualTo("copy of play");
    }

    @Test
    void copyIdsStayUniqueAndClean() {
        SkinIndex index = indexWith("play_copy");
        assertThat(index.uniqueCopy("%oldid%_copy", "play")).isEqualTo("play_copy_2");
        assertThat(index.uniqueCopy("%oldid%\"", "play")).isEqualTo("play");
        assertThat(index.uniqueCopy("", "play")).isEqualTo("play_copy_2");
        assertThat(index.uniqueCopy("   ", "play")).isEqualTo("play_copy_2");
    }

    @Test
    void semicolonReferencesUseTheFirstExistingResource() {
        SkinTheme theme = new SkinTheme();
        dev.zoroaster1x.vlcskin.model.resource.BitmapResource first =
                new dev.zoroaster1x.vlcskin.model.resource.BitmapResource();
        first.setId("first");
        first.setFile("first.png");
        dev.zoroaster1x.vlcskin.model.resource.BitmapResource second =
                new dev.zoroaster1x.vlcskin.model.resource.BitmapResource();
        second.setId("second");
        second.setFile("second.png");
        theme.getResources().add(first);
        theme.getResources().add(second);
        SkinIndex index = new SkinIndex(theme);

        assertThat(index.findImage("missing;first").bitmap().getId()).isEqualTo("first");
        assertThat(index.findImage("missing ; second").bitmap().getId()).isEqualTo("second");
        assertThat(index.findImage("first;second").bitmap().getId()).isEqualTo("first");
        assertThat(index.findImage("missing;also")).isNull();
        assertThat(index.findResource("missing;second")).isSameAs(second);
    }
}
