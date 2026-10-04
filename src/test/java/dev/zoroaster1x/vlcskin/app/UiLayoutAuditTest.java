package dev.zoroaster1x.vlcskin.app;

import static org.assertj.core.api.Assertions.assertThat;

import dev.zoroaster1x.vlcskin.example.ExampleSkins;
import java.awt.Component;
import java.awt.Container;
import java.awt.Rectangle;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;
import java.util.function.Supplier;
import javax.swing.AbstractButton;
import javax.swing.JLabel;
import javax.swing.border.TitledBorder;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

/**
 * Machine checks for the UI defects a person or a model tends to overlook:
 * labels whose text is wider than the space the layout gave them, buttons with
 * clipped captions, controls squashed below their preferred height, and
 * siblings whose bounds overlap. It runs at several window sizes and font
 * scales and fails with a list, so "looks fine to me" never decides.
 *
 * <p>The report is also written to build/reports/ui-audit.md for review.
 */
class UiLayoutAuditTest {

    private static final int[][] SIZES = {{1440, 900}, {1180, 780}, {980, 660}};

    @Test
    void nothingIsClippedOrOverlapping(@TempDir Path folder) throws Exception {
        List<String> problems = new ArrayList<>();
        StringBuilder report = new StringBuilder("# UI layout audit\n\n");
        for (int[] size : SIZES) {
            HeadlessStudio headless = studio(folder.resolve(size[0] + "x" + size[1]));
            headless.render(size[0], size[1]);
            List<String> found = audit(headless);
            report.append("## ").append(size[0]).append("x").append(size[1]).append('\n');
            if (found.isEmpty()) {
                report.append("* clean\n");
            } else {
                for (String problem : found) {
                    report.append("* ").append(problem).append('\n');
                }
            }
            problems.addAll(found);
        }
        // The audit must not be fooled by a layout that never painted.
        assertThat(countLabels(studio(folder.resolve("probe")))).isGreaterThan(20);
        Path reportFile = Path.of("build", "reports", "ui-audit.md");
        Files.createDirectories(reportFile.getParent());
        Files.writeString(reportFile, report.toString());
        assertThat(problems).as("ui layout problems, see build/reports/ui-audit.md").isEmpty();
    }

    @Test
    void nothingIsClippedAtOneHundredFiftyPercent(@TempDir Path folder) throws Exception {
        HeadlessStudio headless = studio(folder);
        headless.studio().applyFontScale(150);
        try {
            headless.render(1440, 900);
            List<String> found = audit(headless);
            StringBuilder report = new StringBuilder("## 1440x900 at 150 percent\n");
            if (found.isEmpty()) {
                report.append("* clean\n");
            } else {
                for (String problem : found) {
                    report.append("* ").append(problem).append('\n');
                }
            }
            Path reportFile = Path.of("build", "reports", "ui-audit.md");
            Files.createDirectories(reportFile.getParent());
            Files.writeString(reportFile, "# UI layout audit\n\n" + report);
            assertThat(found).as("ui layout problems at 150 percent, see build/reports/ui-audit.md")
                    .isEmpty();
        } finally {
            headless.studio().applyFontScale(100);
        }
    }

    private HeadlessStudio studio(Path folder) throws Exception {
        Path theme = ExampleSkins.create(folder, ExampleSkins.NEON);
        Studio studio = VlcSkinStudio.headlessStudio();
        studio.openFile(theme);
        return new HeadlessStudio(studio);
    }

    private List<String> audit(Container root) {
        List<String> problems = new ArrayList<>();
        walk(root, problems, false);
        return problems;
    }

    private void walk(Container parent, List<String> problems, boolean inViewport) {
        List<Component> visible = new ArrayList<>();
        for (Component child : parent.getComponents()) {
            if (!child.isVisible() || child.getWidth() <= 0 || child.getHeight() <= 0) {
                continue;
            }
            visible.add(child);
            String description = describe(child);
            if (child instanceof JLabel label && label.getText() != null
                    && !label.getText().isEmpty() && !label.getText().startsWith("<html")) {
                int available = child.getWidth();
                int needed = child.getPreferredSize().width;
                if (needed > available + 2) {
                    problems.add("clipped text: " + description + " needs " + needed
                            + "px, has " + available + "px");
                }
            }
            if (child instanceof AbstractButton button && button.getText() != null
                    && !button.getText().isEmpty()) {
                int available = child.getWidth();
                int needed = child.getPreferredSize().width;
                if (needed > available + 2) {
                    problems.add("clipped button: " + description + " needs " + needed
                            + "px, has " + available + "px");
                }
            }
            // A viewport is allowed to show less height than the content: that
            // is what the scrollbar is for. Width clipping still matters,
            // because the inspector deliberately has no horizontal scrollbar.
            if (!inViewport && child.getHeight() < child.getMinimumSize().height - 1
                    && child.getMinimumSize().height > 0) {
                problems.add("squashed: " + description + " is " + child.getHeight()
                        + "px tall, minimum " + child.getMinimumSize().height);
            }
            if (child instanceof Container container) {
                walk(container, problems, inViewport || child instanceof javax.swing.JViewport);
            }
        }
        for (int i = 0; i < visible.size(); i++) {
            for (int j = i + 1; j < visible.size(); j++) {
                Rectangle a = visible.get(i).getBounds();
                Rectangle b = visible.get(j).getBounds();
                Rectangle overlap = a.intersection(b);
                if (overlap.width > 3 && overlap.height > 3) {
                    problems.add("overlap: " + describe(visible.get(i)) + " and "
                            + describe(visible.get(j)) + " share " + overlap.width + "x"
                            + overlap.height + "px");
                }
            }
        }
    }

    private int countLabels(HeadlessStudio headless) throws Exception {
        headless.render(1000, 700);
        return count(headless);
    }

    private int count(Container container) {
        int total = 0;
        for (Component child : container.getComponents()) {
            if (child instanceof JLabel) {
                total++;
            }
            if (child instanceof Container nested) {
                total += count(nested);
            }
        }
        return total;
    }

    private String describe(Component component) {
        Deque<String> parts = new ArrayDeque<>();
        Component current = component;
        for (int depth = 0; current != null && depth < 6; depth++) {
            String name = current.getName();
            if (name == null) {
                name = current.getClass().getSimpleName();
            }
            parts.addFirst(name);
            current = current.getParent();
        }
        String text = component instanceof JLabel label ? label.getText()
                : component instanceof AbstractButton button ? button.getText() : null;
        String suffix = text == null || text.isBlank() ? "" : " [" + shorten(text) + "]";
        return String.join("/", parts) + suffix;
    }

    private String shorten(String text) {
        String flat = text.replaceAll("<[^>]+>", "");
        return flat.length() <= 24 ? flat : flat.substring(0, 21) + "...";
    }
}
