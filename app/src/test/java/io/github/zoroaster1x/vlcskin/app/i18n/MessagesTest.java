package io.github.zoroaster1x.vlcskin.app.i18n;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

/** The converted original translations must load and fall back correctly. */
class MessagesTest {

    @AfterEach
    void reset() {
        Messages.setLanguage("en");
    }

    @Test
    void englishLoadsFromTheConvertedBundle() {
        Messages.setLanguage("en");
        assertThat(Messages.get("BUTTON_OK", "fallback")).isEqualTo("OK");
        assertThat(Messages.get("MENU_FILE", "fallback")).isEqualTo("File");
    }

    @Test
    void germanTranslationsResolve() {
        Messages.setLanguage("de");
        assertThat(Messages.get("BUTTON_OK", "fallback")).isEqualTo("OK");
        assertThat(Messages.get("MENU_FILE", "fallback")).isEqualTo("Datei");
        assertThat(Messages.get("TOOLBAR_OPEN", "fallback")).contains("Skin");
    }

    @Test
    void missingKeysFallBack() {
        Messages.setLanguage("de");
        assertThat(Messages.get("NO_SUCH_KEY_ANYWHERE", "fallback text")).isEqualTo("fallback text");
    }

    @Test
    void unknownLanguageFallsBackToEnglishDefaults() {
        Messages.setLanguage("xx");
        assertThat(Messages.get("BUTTON_OK", "fallback")).isEqualTo("fallback");
    }

    @Test
    void languageCatalogListsTheOriginalLanguages() {
        assertThat(Messages.available()).extracting(Messages.Language::code)
                .contains("en", "de", "fr", "es", "zh-tw", "sr", "sr-cyr");
    }

    @Test
    void placeholdersAreReplaced() {
        assertThat(Messages.format("ERROR_ID_EXISTS_MSG", "%i is used", "play_btn"))
                .contains("play_btn");
    }
}
