package dev.zoroaster1x.vlcskin.format.parse;

import dev.zoroaster1x.vlcskin.format.ParseIssue;
import dev.zoroaster1x.vlcskin.model.SkinIndex;
import dev.zoroaster1x.vlcskin.model.SkinTheme;
import java.util.ArrayList;
import java.util.List;

/**
 * Shared state while one theme is being parsed.
 */
public final class ParseContext {

    private final SkinTheme theme;
    private final List<ParseIssue> issues = new ArrayList<>();
    private final java.util.Set<String> generatedIds = new java.util.HashSet<>();
    private final SkinIndex index;

    public ParseContext(SkinTheme theme) {
        this.theme = theme;
        this.index = new SkinIndex(theme);
    }

    public SkinTheme theme() {
        return theme;
    }

    public SkinIndex index() {
        return index;
    }

    public void issue(ParseIssue issue) {
        issues.add(issue);
    }

    public List<ParseIssue> issues() {
        return issues;
    }

    /**
     * A unique editor id for an element that carried none.
     */
    public String uniqueId(String typeName) {
        int counter = 1;
        String candidate;
        do {
            candidate = typeName + " #" + counter;
            counter++;
        } while (index.idExists(candidate) || !generatedIds.add(candidate));
        return candidate;
    }
}
