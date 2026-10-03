package io.github.zoroaster1x.vlcskin.cli;

import io.github.zoroaster1x.vlcskin.edit.EditorSession;
import io.github.zoroaster1x.vlcskin.format.ParseIssue;
import io.github.zoroaster1x.vlcskin.format.SkinValidator;
import java.nio.file.Path;
import java.util.List;
import java.util.concurrent.Callable;
import picocli.CommandLine.Command;
import picocli.CommandLine.Parameters;

/**
 * Validate a skin; exits 1 when errors are found.
 */
@Command(name = "validate", description = "Check a skin for errors and warnings.")
public final class ValidateCommand implements Callable<Integer> {

    @Parameters(index = "0", paramLabel = "SKIN", description = "The skin XML file.")
    Path skin;

    @Override
    public Integer call() throws Exception {
        EditorSession session = EditorSession.open(skin);
        List<ParseIssue> issues = SkinValidator.validate(session.theme(),
                skin.toAbsolutePath().getParent());
        long errors = issues.stream().filter(issue -> issue.severity() == ParseIssue.Severity.ERROR).count();
        for (ParseIssue issue : issues) {
            System.out.println(issue);
        }
        System.out.println(issues.size() + " issues, " + errors + " errors");
        return errors > 0 ? 1 : 0;
    }
}
