package dev.zoroaster1x.vlcskin.app.docs;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;

/**
 * The documentation search index. Every page is split into a heading tree and
 * plain text lines, so a hit can say exactly where it lives and the viewer can
 * show the path (section, page, sub heading) instead of a bare line.
 */
public final class DocumentationSearch {

    /**
     * One heading inside a page, the "subnotes" of the documentation.
     */
    public record Heading(int level, String text, int line) {
    }

    /**
     * One result: where it is and why it matched.
     */
    public record Hit(DocumentationBundle.Topic topic, int line, String snippet, List<Heading> path,
                      int score, int occurrences) {
    }

    private record IndexedPage(DocumentationBundle.Topic topic, List<Heading> headings, String[] lines,
                               String titleLower, String sectionLower, String bodyLower) {
    }

    private final DocumentationBundle bundle;
    private final List<IndexedPage> pages = new ArrayList<>();
    private boolean built;

    public DocumentationSearch(DocumentationBundle bundle) {
        this.bundle = bundle;
    }

    /**
     * Builds the in memory index; cheap enough to run once on open, but safe
     * to call from a background thread.
     */
    public synchronized void build() {
        if (built) {
            return;
        }
        for (DocumentationBundle.Topic topic : bundle.topics()) {
            try {
                pages.add(index(topic, bundle.markdown(topic)));
            } catch (IOException ex) {
                // A missing page is skipped; the list still shows the topic.
            }
        }
        built = true;
    }

    public boolean ready() {
        return built;
    }

    public int pageCount() {
        return pages.size();
    }

    /**
     * Searches titles, headings and body, ranked: title first, then headings,
     * then body count. Every whitespace separated term must appear somewhere in
     * the page.
     */
    public List<Hit> search(String query, int limit) {
        build();
        List<Hit> hits = new ArrayList<>();
        List<String> terms = terms(query);
        if (terms.isEmpty()) {
            return hits;
        }
        for (IndexedPage page : pages) {
            String bodyLower = page.bodyLower();
            boolean all = terms.stream().allMatch(bodyLower::contains);
            if (!all) {
                continue;
            }
            int score = 0;
            int firstLine = -1;
            int occurrences = 0;
            for (String term : terms) {
                if (page.titleLower.contains(term)) {
                    score += 120;
                }
                if (page.sectionLower.contains(term)) {
                    score += 40;
                }
                for (Heading heading : page.headings) {
                    if (heading.text().toLowerCase(Locale.ROOT).contains(term)) {
                        score += 60;
                        if (firstLine < 0) {
                            firstLine = heading.line();
                        }
                    }
                }
                for (int i = 0; i < page.lines.length; i++) {
                    String lineLower = page.lines[i].toLowerCase(Locale.ROOT);
                    int at = lineLower.indexOf(term);
                    while (at >= 0) {
                        occurrences++;
                        score += 4;
                        if (firstLine < 0) {
                            firstLine = i + 1;
                        }
                        at = lineLower.indexOf(term, at + term.length());
                    }
                }
            }
            if (score == 0) {
                continue;
            }
            int line = firstLine < 0 ? 1 : firstLine;
            hits.add(new Hit(page.topic, line, snippet(page.lines, line - 1, terms),
                    pathFor(page.headings, line), score, occurrences));
        }
        hits.sort(Comparator.comparingInt(Hit::score).reversed()
                .thenComparing(hit -> hit.topic().title().toLowerCase(Locale.ROOT)));
        return hits.size() > limit ? hits.subList(0, limit) : hits;
    }

    /**
     * The heading path that contains a line: top level heading, then the
     * deepest heading before the line.
     */
    public static List<Heading> pathFor(List<Heading> headings, int line) {
        List<Heading> path = new ArrayList<>();
        Heading level1 = null;
        Heading level2 = null;
        Heading level3 = null;
        for (Heading heading : headings) {
            if (heading.line() > line) {
                break;
            }
            switch (heading.level()) {
                case 1 -> {
                    level1 = heading;
                    level2 = null;
                    level3 = null;
                }
                case 2 -> {
                    level2 = heading;
                    level3 = null;
                }
                default -> level3 = heading;
            }
        }
        if (level1 != null) {
            path.add(level1);
        }
        if (level2 != null) {
            path.add(level2);
        }
        if (level3 != null) {
            path.add(level3);
        }
        return path;
    }

    private static IndexedPage index(DocumentationBundle.Topic topic, String markdown) {
        String[] lines = markdown.split("\n", -1);
        List<Heading> headings = new ArrayList<>();
        boolean inFence = false;
        for (int i = 0; i < lines.length; i++) {
            String line = lines[i];
            String trimmed = line.strip();
            if (trimmed.startsWith("```")) {
                inFence = !inFence;
                continue;
            }
            if (inFence || trimmed.isEmpty()) {
                continue;
            }
            int level = headingLevel(trimmed);
            if (level > 0) {
                headings.add(new Heading(level, stripHeading(trimmed), i + 1));
            }
        }
        return new IndexedPage(topic, headings, lines,
                topic.title().toLowerCase(Locale.ROOT),
                topic.section().toLowerCase(Locale.ROOT),
                markdown.toLowerCase(Locale.ROOT));
    }

    private static int headingLevel(String line) {
        int level = 0;
        while (level < line.length() && line.charAt(level) == '#') {
            level++;
        }
        return level > 0 && level <= 6 && level < line.length() && line.charAt(level) == ' ' ? level : 0;
    }

    private static String stripHeading(String line) {
        int start = 0;
        while (start < line.length() && line.charAt(start) == '#') {
            start++;
        }
        return line.substring(start).strip();
    }

    /**
     * One readable line around the match, shortened for the result cell.
     */
    private static String snippet(String[] lines, int index, List<String> terms) {
        int from = Math.max(0, index);
        String text = "";
        for (int i = from; i < lines.length && i < from + 4; i++) {
            String candidate = lines[i].strip();
            if (!candidate.isEmpty() && !candidate.startsWith("#")) {
                text = candidate;
                break;
            }
        }
        if (text.isEmpty() && index >= 0 && index < lines.length) {
            text = lines[index].strip();
        }
        text = text.replaceAll("[*_`>#\\[\\]]", "").replaceAll("\\s+", " ").strip();
        if (text.length() > 150) {
            String lower = text.toLowerCase(Locale.ROOT);
            int at = lower.indexOf(terms.get(0));
            int start = at > 60 ? at - 60 : 0;
            text = (start > 0 ? "..." : "") + text.substring(start, Math.min(text.length(), start + 150))
                    + (start + 150 < text.length() ? "..." : "");
        }
        return text;
    }

    public static List<String> terms(String query) {
        List<String> terms = new ArrayList<>();
        if (query == null) {
            return terms;
        }
        for (String part : query.toLowerCase(Locale.ROOT).split("\\s+")) {
            if (part.length() >= 2 && !terms.contains(part)) {
                terms.add(part);
            }
        }
        return terms;
    }
}
