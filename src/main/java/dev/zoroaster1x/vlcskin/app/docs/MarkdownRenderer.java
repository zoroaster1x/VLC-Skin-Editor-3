package dev.zoroaster1x.vlcskin.app.docs;

import java.nio.file.Path;
import org.commonmark.ext.gfm.tables.TablesExtension;
import org.commonmark.parser.Parser;
import org.commonmark.renderer.html.HtmlRenderer;
import java.util.List;

/**
 * Turns the bundled markdown into HTML the Swing editor pane can show. Image
 * sources point at the materialized bundle folder, and links to other pages
 * become {@code doc:<id>} hrefs the viewer intercepts.
 */
public final class MarkdownRenderer {

    private final Parser parser;
    private final HtmlRenderer renderer;

    public MarkdownRenderer() {
        List<org.commonmark.Extension> extensions = List.of(TablesExtension.create());
        this.parser = Parser.builder().extensions(extensions).build();
        this.renderer = HtmlRenderer.builder().extensions(extensions).escapeHtml(false).build();
    }

    public String toHtml(String markdown, Path bundleFolder, String currentFile) {
        String body = renderer.render(parser.parse(markdown));
        String images = bundleFolder.toUri().toString();
        if (!images.endsWith("/")) {
            images = images + "/";
        }
        body = body.replace("src=\"images/", "src=\"" + images + "images/");
        body = body.replace("src=\"./images/", "src=\"" + images + "images/");
        // Local pages live next to each other, so foo.md or dir/foo.md maps to doc:foo.
        body = body.replaceAll("href=\"([^\"]*?)([A-Za-z0-9._-]+)\\.md\"", "href=\"doc:$2\"");
        return "<html><head><style>"
                + "body { font-family: sans-serif; font-size: 13px; line-height: 1.5; margin: 16px 20px; }"
                + "h1 { font-size: 22px; } h2 { font-size: 18px; } h3 { font-size: 15px; }"
                + "code, pre { font-family: monospace; background: rgba(127,127,127,0.12); }"
                + "pre { padding: 8px; } img { max-width: 100%; }"
                + "table { border-collapse: collapse; } td, th { border: 1px solid rgba(127,127,127,0.4); padding: 4px 8px; }"
                + "</style></head><body>"
                + body
                + "</body></html>";
    }

    /**
     * Wraps every occurrence of the terms in a highlight mark, outside tags.
     */
    public String highlight(String html, List<String> terms) {
        if (terms == null || terms.isEmpty() || html == null) {
            return html;
        }
        int body = html.indexOf("<body>");
        if (body < 0) {
            return html;
        }
        StringBuilder out = new StringBuilder(html.substring(0, body + 6));
        String rest = html.substring(body + 6);
        int index = 0;
        while (index < rest.length()) {
            int tag = rest.indexOf('<', index);
            String text = tag < 0 ? rest.substring(index) : rest.substring(index, tag);
            out.append(markTerms(text, terms));
            if (tag < 0) {
                break;
            }
            int end = rest.indexOf('>', tag);
            if (end < 0) {
                out.append(rest.substring(tag));
                break;
            }
            out.append(rest, tag, end + 1);
            index = end + 1;
        }
        return out.toString();
    }

    private static String markTerms(String text, List<String> terms) {
        String result = text;
        for (String term : terms) {
            result = replaceIgnoreCase(result, term,
                    "<mark style=\"background:#E06C38;color:#1E1F22;padding:0 1px;\">" + term + "</mark>");
        }
        return result;
    }

    private static String replaceIgnoreCase(String text, String term, String replacement) {
        String lower = text.toLowerCase(java.util.Locale.ROOT);
        String needle = term.toLowerCase(java.util.Locale.ROOT);
        StringBuilder out = new StringBuilder();
        int from = 0;
        int at;
        while ((at = lower.indexOf(needle, from)) >= 0) {
            out.append(text, from, at).append(replacement);
            from = at + needle.length();
        }
        out.append(text.substring(from));
        return out.toString();
    }
}
