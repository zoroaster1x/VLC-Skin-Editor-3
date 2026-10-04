package dev.zoroaster1x.vlcskin.app.config;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.LinkedHashMap;
import java.util.Map;
import javax.swing.KeyStroke;
import org.junit.jupiter.api.Test;

/**
 * Parsing, storing and describing configurable shortcuts.
 */
class KeymapTest {

    @Test
    void defaultsAndOverrides() {
        Map<String, String> keys = new LinkedHashMap<>();
        assertThat(Keymap.stroke(keys, "file.save")).isEqualTo("control S");
        assertThat(Keymap.keyStroke(keys, "file.save")).isEqualTo(KeyStroke.getKeyStroke("control S"));

        Keymap.set(keys, "file.save", "Ctrl+Shift+S");
        assertThat(keys).containsEntry("file.save", "control shift S");
        assertThat(Keymap.describe(Keymap.stroke(keys, "file.save"))).isEqualTo("Shift+Ctrl+S");

        Keymap.set(keys, "file.save", "control S");
        assertThat(keys).as("a value equal to the default is not stored").doesNotContainKey("file.save");

        Keymap.set(keys, "file.save", "");
        assertThat(Keymap.stroke(keys, "file.save")).isEmpty();
        assertThat(Keymap.keyStroke(keys, "file.save")).isNull();
    }

    @Test
    void userInputIsNormalized() {
        assertThat(Keymap.parse("Ctrl+0")).isEqualTo(KeyStroke.getKeyStroke("control 0"));
        assertThat(Keymap.parse("command S")).isEqualTo(KeyStroke.getKeyStroke("meta S"));
        assertThat(Keymap.parse("Esc")).isNotNull();
        assertThat(Keymap.parse("garbage")).isNull();
        assertThat(Keymap.parse("")).isNull();
        assertThat(Keymap.parse(null)).isNull();
    }

    @Test
    void everyBindingHasADefault() {
        for (Keymap.Binding binding : Keymap.BINDINGS) {
            assertThat(binding.id()).isNotBlank();
            assertThat(binding.fallback()).isNotBlank();
            assertThat(Keymap.defaultStroke(binding.id())).isNotBlank();
        }
    }
}
