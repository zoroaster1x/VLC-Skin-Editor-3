package dev.zoroaster1x.vlcskin.app.theme;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

class ThemeManagerTest {

    @Test
    void theBackdropPreferencePinsLightOrDarkAndFallsBackToTheTheme() {
        assertThat(ThemeManager.canvasBackground("light"))
                .isNotEqualTo(ThemeManager.canvasBackground("dark"));
        assertThat(ThemeManager.canvasBackground("nonsense"))
                .isEqualTo(ThemeManager.canvasBackground());
        assertThat(ThemeManager.normalizeCanvasBackground("LIGHT")).isEqualTo("light");
        assertThat(ThemeManager.normalizeCanvasBackground("dark")).isEqualTo("dark");
        assertThat(ThemeManager.normalizeCanvasBackground("nonsense")).isEqualTo("theme");
        assertThat(ThemeManager.normalizeCanvasBackground(null)).isEqualTo("theme");
    }
}
