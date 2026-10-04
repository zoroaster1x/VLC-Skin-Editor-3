package dev.zoroaster1x.vlcskin.format;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import org.junit.jupiter.api.Test;

/**
 * The three-way merge that lets an AI session and the desktop window edit the
 * same file without either side discarding the other's work.
 */
class XmlMergerTest {

    @Test
    void disjointEditsBothSurvive() {
        String base = "<Theme>\n  <A x=\"1\"/>\n  <B y=\"2\"/>\n</Theme>";
        String ours = "<Theme>\n  <A x=\"9\"/>\n  <B y=\"2\"/>\n</Theme>";
        String theirs = "<Theme>\n  <A x=\"1\"/>\n  <B y=\"8\"/>\n</Theme>";

        XmlMerger.Merge merge = XmlMerger.merge(base, ours, theirs);
        assertThat(merge.clean()).isTrue();
        assertThat(merge.text()).contains("x=\"9\"").contains("y=\"8\"");
    }

    @Test
    void identicalEditsCollapse() {
        String base = "<Theme>\n  <A x=\"1\"/>\n</Theme>";
        String both = "<Theme>\n  <A x=\"5\"/>\n</Theme>";
        XmlMerger.Merge merge = XmlMerger.merge(base, both, both);
        assertThat(merge.clean()).isTrue();
        assertThat(merge.text()).isEqualTo(both);
    }

    @Test
    void conflictingEditsKeepOursAndReport() {
        String base = "<Theme>\n  <A x=\"1\"/>\n</Theme>";
        String ours = "<Theme>\n  <A x=\"2\"/>\n</Theme>";
        String theirs = "<Theme>\n  <A x=\"3\"/>\n</Theme>";

        XmlMerger.Merge merge = XmlMerger.merge(base, ours, theirs);
        assertThat(merge.clean()).isFalse();
        assertThat(merge.text()).contains("x=\"2\"").doesNotContain("x=\"3\"");
        assertThat(merge.conflicts()).hasSize(1);
    }

    @Test
    void addedAndRemovedLinesMerge() {
        String base = "<Theme>\n  <A/>\n</Theme>";
        String ours = "<Theme>\n  <A/>\n  <NewByUs/>\n</Theme>";
        String theirs = "<Theme>\n  <NewByThem/>\n</Theme>";

        XmlMerger.Merge merge = XmlMerger.merge(base, ours, theirs);
        assertThat(merge.text()).contains("NewByUs").contains("NewByThem");
    }

    @Test
    void diffLinesListsBothDirections() {
        List<String> differences = XmlMerger.diffLines(
                "<Theme>\n  <A/>\n  <B/>\n</Theme>",
                "<Theme>\n  <A/>\n  <C/>\n</Theme>");
        assertThat(differences).anyMatch(line -> line.startsWith("- ") && line.contains("<B/>"));
        assertThat(differences).anyMatch(line -> line.startsWith("+ ") && line.contains("<C/>"));
    }
}
