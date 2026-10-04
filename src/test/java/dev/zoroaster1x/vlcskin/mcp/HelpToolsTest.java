package dev.zoroaster1x.vlcskin.mcp;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

/**
 * The Help menu surface as tools: version and links, the documentation list,
 * the ranked search and the full markdown read.
 */
class HelpToolsTest {

    private final HelpTools help = new HelpTools();

    @Test
    void appInfoPointsAtTheApiReference() {
        ToolOutcome outcome = help.appInfo();
        assertThat(outcome.error()).isFalse();
        assertThat(outcome.text()).contains("\"version\"");
        assertThat(outcome.text()).contains("handbook-format-reference");
        assertThat(outcome.text()).contains("handbook-mcp-for-automation");
        assertThat(outcome.text()).contains("onlineHelp");
    }

    @Test
    void listsEveryTopicGroupedBySection() {
        ToolOutcome outcome = help.list();
        assertThat(outcome.error()).isFalse();
        assertThat(outcome.text()).contains("handbook-format-reference");
        assertThat(outcome.text()).contains("Start here");
    }

    @Test
    void searchesAcrossTheBundle() {
        ToolOutcome outcome = help.search("slider background", null);
        assertThat(outcome.error()).isFalse();
        assertThat(outcome.text()).contains("\"hits\"");
        assertThat(outcome.text()).contains("\"topic\":\"handbook-");
    }

    @Test
    void readsTheFormatReference() {
        ToolOutcome outcome = help.read("format-reference", null);
        assertThat(outcome.error()).isFalse();
        assertThat(outcome.text()).contains("Slider");
        assertThat(outcome.text()).contains("Button");
    }

    @Test
    void refusesAnUnknownTopic() {
        assertThat(help.read("no-such-topic", null).error()).isTrue();
    }
}
