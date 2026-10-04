package dev.zoroaster1x.vlcskin.example;

import static org.assertj.core.api.Assertions.assertThat;

import dev.zoroaster1x.vlcskin.format.ParseIssue;
import dev.zoroaster1x.vlcskin.format.SkinParser;
import java.nio.file.Files;
import java.nio.file.Path;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

/**
 * The built in examples must all create a theme that parses, and the bundled
 * VeLoCity example must carry its license file.
 */
class ExampleSkinsTest {

    @Test
    void everyExampleCreatesAParsableTheme(@TempDir Path folder) throws Exception {
        for (ExampleSkins.Example example : ExampleSkins.catalog()) {
            Path target = folder.resolve(example.id());
            Path themeFile = ExampleSkins.create(target, example);
            assertThat(themeFile).as(example.id()).exists();
            SkinParser.Result result = SkinParser.parse(themeFile);
            assertThat(result.issues()).as(example.id())
                    .noneMatch(issue -> issue.severity() == ParseIssue.Severity.ERROR);
            assertThat(result.theme().getWindows()).as(example.id()).isNotEmpty();
        }
    }

    @Test
    void velocityShipsItsLicenseAndAssets(@TempDir Path folder) throws Exception {
        Path target = folder.resolve("velocity");
        ExampleSkins.create(target, ExampleSkins.VELOCITY);
        assertThat(target.resolve("LICENSE-VeLoCity.txt")).exists();
        assertThat(Files.readString(target.resolve("LICENSE-VeLoCity.txt")))
                .contains("MIT License")
                .contains("dmtiir");
        assertThat(target.resolve("roboto.ttf")).exists();
        SkinParser.Result result = SkinParser.parse(target.resolve("theme.xml"));
        assertThat(result.theme().getWindows()).hasSize(4);
    }
}
