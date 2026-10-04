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
 * The upgrade notification: the newest version, the notes of every release the
 * user missed, and a button that downloads the jar, checks its SHA-256 and
 * installs it over the running one.
 */
public final class UpdateDialog extends JDialog {

    private final UpdateService.UpdateInfo info;

    public UpdateDialog(Studio studio, Component parent, UpdateService.UpdateInfo info, Runnable quit) {
        super(parent == null ? null : SwingUtilities.getWindowAncestor(parent),
                Messages.get("APP_UPDATE_TITLE", "Update available"), ModalityType.APPLICATION_MODAL);
        this.info = info;
        UpdateService.Release latest = info.latest();

        JTextArea headline = new JTextArea(Messages.format("APP_UPDATE_HEADLINE",
                "VLC Skin Studio %s is available. You have %s.", latest.version(), info.current()));
        headline.setEditable(false);
        headline.setOpaque(false);
        headline.setLineWrap(true);
        headline.setWrapStyleWord(true);
        headline.setFocusable(false);
        headline.setFont(getFont().deriveFont(Font.BOLD, 14f));
        headline.setBorder(BorderFactory.createEmptyBorder(0, 2, 8, 2));

        JTextArea notes = new JTextArea(notesFor(info));
        notes.setEditable(false);
        notes.setLineWrap(true);
        notes.setWrapStyleWord(true);
        notes.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 12));
        notes.setCaretPosition(0);
        JScrollPane scroll = new JScrollPane(notes);
        scroll.setPreferredSize(new Dimension(600, 340));
        scroll.setBorder(BorderFactory.createLineBorder(
                javax.swing.UIManager.getColor("Component.borderColor")));

        JButton later = new JButton(Messages.get("APP_UPDATE_LATER", "Later"));
        later.addActionListener(event -> dispose());
        JButton install = new JButton(Messages.get("APP_UPDATE_INSTALL", "Install update"));
        install.addActionListener(event -> install(studio, install, quit));

        JPanel footer = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        footer.add(later);
        footer.add(install);

        JPanel content = new JPanel(new BorderLayout(0, 10));
        content.setBorder(BorderFactory.createEmptyBorder(14, 16, 14, 16));
        content.add(headline, BorderLayout.NORTH);
        content.add(scroll, BorderLayout.CENTER);
        content.add(footer, BorderLayout.SOUTH);
        setContentPane(content);
        pack();
        setLocationRelativeTo(getOwner());
    }

    /**
     * The notes of every missed release, oldest first; the 1.0.1 release of a
     * fresh repository can carry the whole history, so the text area scrolls.
     */
    static String notesFor(UpdateService.UpdateInfo info) {
        StringBuilder text = new StringBuilder();
        for (UpdateService.Release release : info.missed()) {
            text.append("# ").append(release.tag());
            String published = release.publishedAt();
            if (published != null && published.length() >= 10) {
                text.append("  (").append(published, 0, 10).append(')');
            }
            text.append("\n\n").append(release.notes() == null ? "" : release.notes().strip()).append("\n\n");
        }
        return text.toString().strip();
    }

    private void install(Studio studio, JButton button, Runnable quit) {
        button.setEnabled(false);
        UpdateService.Release latest = info.latest();
        try {
            Path updates = AppPaths.configDir().resolve("updates");
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
        service.downloadJar(latest, jar, null);
        service.downloadChecksums(latest, sums);
        String expected = UpdateService.expectedChecksum(sums);
        if (!UpdateService.verify(jar, expected)) {
            throw new java.io.IOException("The downloaded jar failed its SHA-256 check");
        }
        if (running == null) {
            return null;
        }
        return UpdateService.install(jar, running);
    }
}
