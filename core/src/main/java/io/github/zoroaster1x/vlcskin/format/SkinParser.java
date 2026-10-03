package io.github.zoroaster1x.vlcskin.format;

import io.github.zoroaster1x.vlcskin.format.parse.ParseContext;
import io.github.zoroaster1x.vlcskin.format.parse.ResourceParser;
import io.github.zoroaster1x.vlcskin.format.parse.WindowParser;
import io.github.zoroaster1x.vlcskin.model.SkinTheme;
import io.github.zoroaster1x.vlcskin.model.resource.Resource;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * Reads skin XML into the model.
 */
public final class SkinParser {

    /**
     * The parsed theme plus everything that looked wrong along the way.
     */
    public record Result(SkinTheme theme, List<ParseIssue> issues) {

        public boolean hasErrors() {
            return issues.stream().anyMatch(issue -> issue.severity() == ParseIssue.Severity.ERROR);
        }
    }

    private SkinParser() {
    }

    public static Result parse(Path file) throws IOException {
        return parse(Files.readAllBytes(file), file.toAbsolutePath().toString());
    }

    public static Result parse(byte[] bytes, String sourcePath) {
        try {
            Document document = XmlSupport.parse(bytes);
            Element root = document.getDocumentElement();
            if (root == null || !"Theme".equals(root.getNodeName())) {
                SkinTheme empty = new SkinTheme();
                return new Result(empty, List.of(ParseIssue.error(
                        "The file root is not a <Theme> element", sourcePath)));
            }
            ParseContext context = new ParseContext(new SkinTheme());
            SkinTheme theme = context.theme();
            theme.setSourcePath(sourcePath);
            parseTheme(root, context);
            return new Result(theme, List.copyOf(context.issues()));
        } catch (Exception ex) {
            SkinTheme empty = new SkinTheme();
            empty.setSourcePath(sourcePath);
            return new Result(empty, List.of(ParseIssue.error(
                    "The skin could not be parsed: " + ex.getMessage(), sourcePath)));
        }
    }

    private static void parseTheme(Element root, ParseContext context) {
        SkinTheme theme = context.theme();
        io.github.zoroaster1x.vlcskin.format.parse.Attributes attrs =
                new io.github.zoroaster1x.vlcskin.format.parse.Attributes(root, context.issues());
        String version = attrs.str("version", null);
        if (version == null) {
            context.issue(ParseIssue.warning("The Theme has no version attribute; assuming 2.0",
                    XmlSupport.path(root)));
        } else if (!XmlSupport.isCompatibleThemeVersion(theme, version)) {
            context.issue(ParseIssue.warning(
                    "Theme version " + version + " is newer than the supported 2.0",
                    XmlSupport.path(root)));
        }
        theme.setVersion(version == null || version.isBlank() ? SkinTheme.SUPPORTED_VERSION : version);
        theme.setTooltipfont(attrs.str("tooltipfont", "defaultfont"));
        theme.setMagnet(attrs.integer("magnet", SkinTheme.DEFAULT_MAGNET));
        theme.setAlpha(attrs.integer("alpha", SkinTheme.DEFAULT_ALPHA));
        theme.setMovealpha(attrs.integer("movealpha", SkinTheme.DEFAULT_ALPHA));
        attrs.finish(theme);

        ResourceParser resourceParser = new ResourceParser(context);
        WindowParser windowParser = new WindowParser(context);
        for (Element child : XmlSupport.childElements(root)) {
            switch (child.getNodeName()) {
                case "ThemeInfo" -> parseThemeInfo(child, context);
                case "Include" -> theme.getIncludes().add(resourceParser.parseInclude(child));
                case "Window" -> theme.getWindows().add(windowParser.parseWindow(child));
                default -> {
                    Resource resource = resourceParser.parse(child);
                    if (resource != null) {
                        theme.getResources().add(resource);
                    } else {
                        theme.preserveChild(XmlSupport.elementToXml(child));
                    }
                }
            }
        }
        if (theme.getWindows().isEmpty()) {
            context.issue(ParseIssue.warning("The theme defines no windows", theme.getSourcePath()));
        }
    }

    private static void parseThemeInfo(Element element, ParseContext context) {
        io.github.zoroaster1x.vlcskin.format.parse.Attributes attrs =
                new io.github.zoroaster1x.vlcskin.format.parse.Attributes(element, context.issues());
        var info = context.theme().getThemeInfo();
        info.setName(attrs.str("name", info.getName()));
        info.setAuthor(attrs.str("author", info.getAuthor()));
        info.setEmail(attrs.str("email", info.getEmail()));
        info.setWebpage(attrs.str("webpage", info.getWebpage()));
        attrs.finish(info);
    }
}
