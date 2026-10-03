package dev.zoroaster1x.vlcskin.app.dialog;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Window;
import java.lang.reflect.InvocationTargetException;
import java.util.concurrent.Callable;
import java.util.concurrent.FutureTask;
import javax.swing.BorderFactory;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.SwingUtilities;

/**
 * A small modal progress dialog for work that would otherwise freeze the UI.
 * The work runs on a background thread while the dialog animates.
 */
public final class ProgressDialog extends JDialog {

    private final JLabel messageLabel = new JLabel(" ");
    private final JProgressBar bar = new JProgressBar();

    public ProgressDialog(Component parent) {
        super(parent == null ? null : SwingUtilities.getWindowAncestor(parent), "Working",
                ModalityType.APPLICATION_MODAL);
        bar.setIndeterminate(true);
        JPanel content = new JPanel(new BorderLayout(8, 10));
        content.setBorder(BorderFactory.createEmptyBorder(16, 20, 16, 20));
        content.add(messageLabel, BorderLayout.NORTH);
        content.add(bar, BorderLayout.CENTER);
        setContentPane(content);
        setResizable(false);
        setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
    }

    /**
     * Shows the dialog, runs the callable off the event thread, and returns its
     * result or throws what the callable threw. The dialog is disposed on the
     * event thread in every case.
     */
    public <T> T run(String title, String message, Callable<T> work) throws Exception {
        FutureTask<T> task = new FutureTask<>(work) {
            @Override
            protected void done() {
                disposeOnEventThread();
            }
        };
        Thread.ofVirtual().name("vlc-skin-studio-progress").start(task);
        showAndWait(title, message);
        try {
            return task.get();
        } catch (java.util.concurrent.ExecutionException ex) {
            if (ex.getCause() instanceof Exception exception) {
                throw exception;
            }
            if (ex.getCause() instanceof Error error) {
                throw error;
            }
            throw new RuntimeException(ex.getCause());
        }
    }

    private void showAndWait(String title, String message) throws InterruptedException, InvocationTargetException {
        Runnable show = () -> {
            setTitle(title);
            messageLabel.setText(message);
            pack();
            setLocationRelativeTo(getOwner());
            setVisible(true);
        };
        if (SwingUtilities.isEventDispatchThread()) {
            show.run();
        } else {
            SwingUtilities.invokeAndWait(show);
        }
    }

    private void disposeOnEventThread() {
        try {
            if (SwingUtilities.isEventDispatchThread()) {
                dispose();
            } else {
                SwingUtilities.invokeAndWait(this::dispose);
            }
        } catch (InterruptedException ex) {
            Thread.currentThread().interrupt();
        } catch (InvocationTargetException ex) {
            // Disposal must never hide the result of the work.
        }
    }
}
