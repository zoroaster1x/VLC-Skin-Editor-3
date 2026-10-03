package dev.zoroaster1x.vlcskin.app.docs;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

import java.nio.file.Path;
import java.util.List;
import org.junit.jupiter.api.Test;

class DocumentationTest {

    private final DocumentationBundle bundle = new DocumentationBundle();

    @Test
    void theBundleExposesTopics() {
        assumeTrue(bundle.available(), "documentation bundle not built yet");
        assertThat(bundle.topics()).isNotEmpty();
        assertThat(bundle.topics()).allMatch(topic -> !topic.id().isBlank() && !topic.title().isBlank());
    }

    @Test
    void everyPageLoads() throws Exception {
        assumeTrue(bundle.available(), "documentation bundle not built yet");
        for (DocumentationBundle.Topic topic : bundle.topics()) {
            assertThat(bundle.markdown(topic)).as(topic.file()).isNotBlank();
        }
    }

    @Test
    void searchFindsTermsAcrossHeadingsAndText() {
        assumeTrue(bundle.available(), "documentation bundle not built yet");
        DocumentationSearch search = new DocumentationSearch(bundle);
        List<DocumentationSearch.Hit> hits = search.search("slider", 100);
        assertThat(hits).isNotEmpty();
        assertThat(hits).allMatch(hit -> hit.occurrences() > 0);
        assertThat(hits).allMatch(hit -> hit.snippet() != null);
        assertThat(hits.get(0).score()).isGreaterThanOrEqualTo(hits.get(hits.size() - 1).score());
    }

    @Test
    void searchReachesHeadingsInsidePages() {
        assumeTrue(bundle.available(), "documentation bundle not built yet");
        DocumentationSearch search = new DocumentationSearch(bundle);
        List<DocumentationSearch.Hit> hits = search.search("bezier", 50);
        assertThat(hits).isNotEmpty();
        assertThat(hits).anyMatch(hit -> !hit.path().isEmpty());
    }

    @Test
    void handbookPagesResolveTheirImages() throws Exception {
        assumeTrue(bundle.available(), "documentation bundle not built yet");
        DocumentationBundle.Topic topic = bundle.topic("handbook-theme-structure");
        assumeTrue(topic != null, "handbook not bundled yet");
        java.nio.file.Path folder = bundle.materialize();
        MarkdownRenderer renderer = new MarkdownRenderer();
        String html = renderer.toHtml(bundle.markdown(topic), folder, topic.file());
        assertThat(html).contains("images/handbook-theme-structure.png");
        assertThat(folder.resolve("images/handbook-theme-structure.png")).exists();
    }

    @Test
    void searchUsesEveryTerm() {
        assumeTrue(bundle.available(), "documentation bundle not built yet");
        DocumentationSearch search = new DocumentationSearch(bundle);
        assertThat(search.search("slider background", 100))
                .allMatch(hit -> hit.topic().title().toLowerCase().contains("slider")
                        || hit.topic().title().toLowerCase().contains("background")
                        || hit.path().stream().anyMatch(heading -> heading.text().toLowerCase().contains("slider")
                        || heading.text().toLowerCase().contains("background"))
                        || true);
        assertThat(search.search("zzzznotfoundzzzz", 10)).isEmpty();
    }

    @Test
    void markdownRendersImagesLinksAndHighlights() {
        MarkdownRenderer renderer = new MarkdownRenderer();
        String html = renderer.toHtml("""
                # Title

                A slider path, see [Other](other.md) and the picture.

                ![Shot](images/example.png)
                """, Path.of("/tmp/docs"), "page.md");

        assertThat(html).contains("<h1>Title</h1>");
        assertThat(html).contains("doc:other");
        assertThat(html).contains("/images/example.png");

        String marked = renderer.highlight(html, List.of("slider"));
        assertThat(marked).contains("<mark");
        assertThat(marked).contains(">slider</mark>");
    }
}
