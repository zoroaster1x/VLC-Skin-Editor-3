package dev.zoroaster1x.vlcskin.app.dialog;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Window;
import java.lang.reflect.InvocationTargetException;
import java.util.concurrent.Callable;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicReference;
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
        AtomicReference<T> result = new AtomicReference<>();
        AtomicReference<Throwable> failure = new AtomicReference<>();
        CountDownLatch shown = new CountDownLatch(1);
        Thread worker = new Thread(() -> {
            try {
                shown.await();
                result.set(work.call());
            } catch (Throwable ex) {
                failure.set(ex);
            } finally {
                disposeOnEventThread();
            }
        }, "vlc-skin-studio-progress");
        worker.setDaemon(true);
        worker.start();
        shown.countDown();
        showAndWait(title, message);
        worker.join();
        Throwable thrown = failure.get();
        if (thrown instanceof Exception ex) {
            throw ex;
        }
        if (thrown instanceof Error error) {
            throw error;
        }
        if (thrown != null) {
            throw new RuntimeException(thrown);
        }
        return result.get();
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
