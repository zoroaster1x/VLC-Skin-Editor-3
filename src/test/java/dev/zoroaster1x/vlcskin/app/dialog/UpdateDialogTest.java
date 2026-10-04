package dev.zoroaster1x.vlcskin.app.dialog;

import static org.assertj.core.api.Assertions.assertThat;

import dev.zoroaster1x.vlcskin.update.UpdateService;
import java.util.List;
import org.junit.jupiter.api.Test;

class UpdateDialogTest {

    @Test
    void countsTheMissedReleasesAndTheApproximateAge() {
        var latest = release("3.0.3", "2026-10-04T00:00:00Z");
        var info = new UpdateService.UpdateInfo("3.0.0", "2026-06-01T00:00:00Z", latest,
                List.of(release("3.0.1", "2026-06-05T00:00:00Z"), release("3.0.2", "2026-09-01T00:00:00Z"), latest));

        assertThat(info.updatesBehind()).isEqualTo(3);
        assertThat(info.daysBehind()).isEqualTo(125);
        assertThat(UpdateDialog.ageFor(info))
                .isEqualTo("Your copy is 3 updates old (approximately 125 days old!).");
    }

    @Test
    void aSingleMissedReleaseReadsWellWithoutAKnownDate() {
        var latest = release("3.0.1", "2026-06-10T00:00:00Z");
        var info = new UpdateService.UpdateInfo("3.0.0", null, latest, List.of(latest));

        assertThat(info.daysBehind()).isEqualTo(-1);
        assertThat(UpdateDialog.ageFor(info)).isEqualTo("Your copy is 1 update old.");
    }

    @Test
    void keepsChangesAndCommitsAndOnlyTheNewestInstallAndFunding() {
        String body = """
                # VLC Skin Studio 3.0.3

                Intro text.

                Attestation text.

                ## Changes since 3.0.2

                - Fixed the thing

                ## Commits

                - [abc123] Fixed the thing (Zoroaster1x)

                **Full changelog:** link

                ## Install

                Download the zip.

                ## Documentation

                README link.

                ## Funding

                Monero address.

                ## License

                GPL.
                """;

        String older = UpdateDialog.releaseSummary(body, false);
        assertThat(older).contains("## Changes since 3.0.2").contains("- Fixed the thing")
                .contains("## Commits").contains("- [abc123] Fixed the thing")
                .doesNotContain("## Install").doesNotContain("## Funding")
                .doesNotContain("## License").doesNotContain("Attestation text");

        String newest = UpdateDialog.releaseSummary(body, true);
        assertThat(newest).contains("## Install").contains("Download the zip.")
                .contains("## Funding").contains("Monero address.")
                .doesNotContain("## Documentation");
    }

    @Test
    void keepsABodyWithoutHeadingsWhole() {
        assertThat(UpdateDialog.releaseSummary("Just some notes.", false)).isEqualTo("Just some notes.");
    }

    private static UpdateService.Release release(String version, String publishedAt) {
        return new UpdateService.Release(version, "VLC Skin Studio " + version, "notes",
                "https://example", publishedAt, "https://example/zip", "https://example/sums");
    }
}
