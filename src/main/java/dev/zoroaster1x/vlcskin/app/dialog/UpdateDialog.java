package dev.zoroaster1x.vlcskin.app.dialog;

import dev.zoroaster1x.vlcskin.app.Studio;
import dev.zoroaster1x.vlcskin.app.config.AppPaths;
import dev.zoroaster1x.vlcskin.app.i18n.Messages;
import dev.zoroaster1x.vlcskin.update.UpdateService;
import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.nio.file.Files;
import java.nio.file.Path;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.SwingUtilities;

/**
 * The upgrade prompt: the newest version, the scrollable notes of every
 * release the user missed, an update button and a close button. Nothing is
 * downloaded or replaced unless the user presses update now; closing keeps
 * the old version.
 */
public final class UpdateDialog extends JDialog {

    private final UpdateService.UpdateInfo info;

    public UpdateDialog(Studio studio, Component parent, UpdateService.UpdateInfo info, Runnable quit) {
        super(parent == null ? null : SwingUtilities.getWindowAncestor(parent),
                Messages.get("APP_UPDATE_TITLE", "Update available"), ModalityType.APPLICATION_MODAL);
        this.info = info;
        UpdateService.Release latest = info.latest();

        String published = latest.publishedAt();
        String date = published != null && published.length() >= 10 ? published.substring(0, 10) : "an unknown date";
        JTextArea headline = new JTextArea(Messages.format("APP_UPDATE_HEADLINE",
                "VLC Skin Studio %s (released %s) is available. You have %s. Update now, or close "
                        + "this window to keep using the old version.",
                latest.version(), date, info.current()));
        headline.setEditable(false);
        headline.setOpaque(false);
        headline.setLineWrap(true);
        headline.setWrapStyleWord(true);
        headline.setFocusable(false);
        headline.setFont(getFont().deriveFont(Font.BOLD, getFont().getSize2D() + 2f));
        headline.setBorder(BorderFactory.createEmptyBorder(0, 2, 8, 2));

        JTextArea age = new JTextArea(ageFor(info));
        age.setEditable(false);
        age.setOpaque(false);
        age.setLineWrap(true);
        age.setWrapStyleWord(true);
        age.setFocusable(false);
        age.setBorder(BorderFactory.createEmptyBorder(0, 2, 8, 2));

        JPanel header = new JPanel();
        header.setOpaque(false);
        header.setLayout(new javax.swing.BoxLayout(header, javax.swing.BoxLayout.Y_AXIS));
        headline.setAlignmentX(Component.LEFT_ALIGNMENT);
        age.setAlignmentX(Component.LEFT_ALIGNMENT);
        header.add(headline);
        header.add(age);

        JTextArea notes = new JTextArea(notesFor(info));
        notes.setEditable(false);
        notes.setLineWrap(true);
        notes.setWrapStyleWord(true);
        notes.setFont(new Font(Font.MONOSPACED, Font.PLAIN, getFont().getSize()));
        notes.setCaretPosition(0);
        JScrollPane scroll = new JScrollPane(notes);
        scroll.setPreferredSize(new Dimension(600, 340));
        scroll.setBorder(BorderFactory.createLineBorder(
                javax.swing.UIManager.getColor("Component.borderColor")));

        JButton later = new JButton(Messages.get("APP_UPDATE_LATER", "Close"));
        later.addActionListener(event -> dispose());
        JButton install = new JButton(Messages.get("APP_UPDATE_INSTALL", "Update now"));
        install.addActionListener(event -> install(studio, install, quit));

        JPanel footer = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        footer.add(later);
        footer.add(install);

        JPanel content = new JPanel(new BorderLayout(0, 10));
        content.setBorder(BorderFactory.createEmptyBorder(14, 16, 14, 16));
        content.add(header, BorderLayout.NORTH);
        content.add(scroll, BorderLayout.CENTER);
        content.add(footer, BorderLayout.SOUTH);
        setContentPane(content);
        pack();
        setLocationRelativeTo(getOwner());
    }

    /**
     * The age line: how many releases the running copy missed and, when both
     * release dates are known, approximately how many days old it is.
     */
    static String ageFor(UpdateService.UpdateInfo info) {
        int updates = info.updatesBehind();
        String count = updates == 1 ? "1 update" : updates + " updates";
        long days = info.daysBehind();
        if (days >= 0) {
            return Messages.format("APP_UPDATE_AGE",
                    "Your copy is %s old (approximately %s days old!).", count, Long.toString(days));
        }
        return Messages.format("APP_UPDATE_AGE_UNKNOWN", "Your copy is %s old.", count);
    }

    /**
     * The notes of every missed release, oldest first. Each release keeps only
     * its change list and commits; install and funding come from the newest one
     * so a long history does not repeat the intro, attestation, documentation
     * and license of every release, and the full pages stay on GitHub.
     */
    static String notesFor(UpdateService.UpdateInfo info) {
        StringBuilder text = new StringBuilder();
        var missed = info.missed();
        for (int i = 0; i < missed.size(); i++) {
            UpdateService.Release release = missed.get(i);
            boolean newest = i == missed.size() - 1;
            text.append("# ").append(release.tag());
            String published = release.publishedAt();
            if (published != null && published.length() >= 10) {
                text.append("  (").append(published, 0, 10).append(')');
            }
            text.append("\n\n").append(releaseSummary(release.notes(), newest)).append("\n\n");
        }
        return text.toString().strip();
    }

    /**
     * The sections worth showing in the prompt: changes and commits for every
     * release, plus install and funding for the newest one. Intro, attestation,
     * documentation and license are dropped. A body without headings is kept
     * whole, so old or handwritten notes still show.
     */
    static String releaseSummary(String body, boolean newest) {
        if (body == null || body.isBlank()) {
            return "";
        }
        StringBuilder out = new StringBuilder();
        boolean keep = false;
        for (String line : body.split("\n")) {
            if (line.startsWith("## ")) {
                String section = line.substring(3).strip().toLowerCase(java.util.Locale.ROOT);
                keep = section.startsWith("changes") || section.startsWith("commits")
                        || (newest && (section.startsWith("install") || section.startsWith("funding")));
            }
            if (keep) {
                out.append(line).append('\n');
            }
        }
        String summary = out.toString().strip();
        return summary.isEmpty() ? body.strip() : summary;
    }

    private void install(Studio studio, JButton button, Runnable quit) {
        button.setEnabled(false);
        UpdateService.Release latest = info.latest();
        try {
            Path updates = AppPaths.updatesDir();
            Files.createDirectories(updates);
            Path jar = updates.resolve("vlc-skin-studio-" + latest.version() + ".jar");
            Path sums = updates.resolve("SHA256SUMS-" + latest.version());
            Path running = UpdateService.runningJar();
            var result = new ProgressDialog(this).run(
                    Messages.get("APP_UPDATE_PROGRESS_TITLE", "Installing update"),
                    Messages.get("APP_UPDATE_PROGRESS", "Downloading and verifying the update..."),
                    () -> installNow(latest, jar, sums, running));
            dispose();
            if (result == null) {
                JOptionPane.showMessageDialog(null, Messages.format("APP_UPDATE_NO_JAR",
                        "Downloaded and verified %s. This build does not run from a jar, "
                                + "so nothing was replaced.", jar),
                        Messages.get("APP_UPDATE_TITLE", "Update available"),
                        JOptionPane.INFORMATION_MESSAGE);
            } else if (result.restartHandled()) {
                JOptionPane.showMessageDialog(null, Messages.format("APP_UPDATE_RESTART",
                        "Updated to %s. The window will close and restart with the new version.",
                        latest.version()),
                        Messages.get("APP_UPDATE_TITLE", "Update available"),
                        JOptionPane.INFORMATION_MESSAGE);
                if (quit != null) {
                    quit.run();
                }
            } else {
                JOptionPane.showMessageDialog(null, Messages.format("APP_UPDATE_INSTALLED",
                        "Updated to %s. Restart VLC Skin Studio to use it.", latest.version()),
                        Messages.get("APP_UPDATE_TITLE", "Update available"),
                        JOptionPane.INFORMATION_MESSAGE);
            }
        } catch (Exception ex) {
            button.setEnabled(true);
            JOptionPane.showMessageDialog(this, Messages.format("APP_UPDATE_FAILED",
                            "Could not install the update: %s", ex.getMessage()),
                    Messages.get("APP_UPDATE_TITLE", "Update available"), JOptionPane.ERROR_MESSAGE);
        }
    }

    /**
     * The IO half, run behind the progress dialog: download, verify, then hand
     * the file to the platform installer.
     */
    private UpdateService.InstallResult installNow(UpdateService.Release latest, Path jar, Path sums, Path running)
            throws Exception {
        UpdateService service = new UpdateService();
        service.downloadVerifiedJar(latest, jar, sums, null);
        if (running == null) {
            return null;
        }
        return UpdateService.install(jar, running);
    }
}
