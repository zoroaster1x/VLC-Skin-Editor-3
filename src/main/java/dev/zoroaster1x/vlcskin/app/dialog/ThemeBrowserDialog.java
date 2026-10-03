package dev.zoroaster1x.vlcskin.app.dialog;

import dev.zoroaster1x.vlcskin.app.Studio;
import dev.zoroaster1x.vlcskin.app.i18n.Messages;
import dev.zoroaster1x.vlcskin.gallery.GalleryTheme;
import dev.zoroaster1x.vlcskin.gallery.ThemeGalleryClient;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.nio.file.Path;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import javax.imageio.ImageIO;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.SwingUtilities;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.TableRowSorter;

/**
 * Browses the official VideoLAN skins gallery and imports a theme in one
 * click. The list, the preview and the download all run off the EDT on virtual
 * threads, so the dialog never blocks the window.
 */
public final class ThemeBrowserDialog extends JDialog {

    private final Studio studio;
    private final ThemeGalleryClient client = new ThemeGalleryClient();
    private final ExecutorService executor = Executors.newVirtualThreadPerTaskExecutor();
    private final GalleryTableModel model = new GalleryTableModel();
    private final JTable table = new JTable(model);
    private final JTextField filterField = new JTextField();
    private final JLabel preview = new JLabel(Messages.get("APP_GALLERY_PREVIEW", "Select a theme to see its preview"),
            JLabel.CENTER);
    private final JLabel status = new JLabel(Messages.get("APP_GALLERY_LOADING", "Loading the gallery..."));
    private final JButton openButton = new JButton(Messages.get("APP_GALLERY_OPEN", "Download and open"));

    public ThemeBrowserDialog(Studio studio, JFrame owner) {
        super(owner, Messages.get("APP_GALLERY_TITLE", "Theme browser"), false);
        this.studio = studio;
        setSize(1080, 680);
        setLocationRelativeTo(owner);

        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        table.setRowHeight(24);
        TableRowSorter<GalleryTableModel> sorter = new TableRowSorter<>(model);
        table.setRowSorter(sorter);
        filterField.putClientProperty("JTextField.placeholderText",
                Messages.get("APP_GALLERY_FILTER", "Filter by name or author"));
        filterField.getDocument().addDocumentListener(new javax.swing.event.DocumentListener() {
            @Override
            public void insertUpdate(javax.swing.event.DocumentEvent e) {
                applyFilter(sorter);
            }

            @Override
            public void removeUpdate(javax.swing.event.DocumentEvent e) {
                applyFilter(sorter);
            }

            @Override
            public void changedUpdate(javax.swing.event.DocumentEvent e) {
                applyFilter(sorter);
            }
        });

        preview.setPreferredSize(new Dimension(400, 300));
        preview.setBorder(BorderFactory.createLineBorder(
                javax.swing.UIManager.getColor("Component.borderColor")));
        table.getSelectionModel().addListSelectionListener(event -> {
            if (!event.getValueIsAdjusting()) {
                loadPreview();
            }
        });

        openButton.setEnabled(false);
        openButton.addActionListener(event -> downloadSelected());
        table.getSelectionModel().addListSelectionListener(event -> openButton.setEnabled(selected() != null));
        JButton refresh = new JButton(Messages.get("APP_GALLERY_REFRESH", "Refresh"));
        refresh.addActionListener(event -> load(true));
        JButton close = new JButton(Messages.get("BUTTON_CLOSE", "Close"));
        close.addActionListener(event -> dispose());

        JPanel top = new JPanel(new BorderLayout(8, 0));
        top.setBorder(BorderFactory.createEmptyBorder(10, 10, 6, 10));
        top.add(filterField, BorderLayout.CENTER);
        top.add(refresh, BorderLayout.EAST);

        JPanel right = new JPanel(new BorderLayout(0, 8));
        right.setBorder(BorderFactory.createEmptyBorder(4, 8, 4, 10));
        right.add(preview, BorderLayout.NORTH);
        JPanel buttons = new JPanel(new GridLayout(2, 1, 0, 6));
        buttons.add(openButton);
        buttons.add(close);
        right.add(buttons, BorderLayout.SOUTH);

        JPanel content = new JPanel(new BorderLayout());
        content.add(top, BorderLayout.NORTH);
        content.add(new JScrollPane(table), BorderLayout.CENTER);
        content.add(right, BorderLayout.EAST);
        content.add(status, BorderLayout.SOUTH);
        status.setBorder(BorderFactory.createEmptyBorder(4, 10, 8, 10));
        setContentPane(content);

        addWindowListener(new java.awt.event.WindowAdapter() {
            @Override
            public void windowClosed(java.awt.event.WindowEvent event) {
                executor.shutdownNow();
            }
        });
        load();
    }

    private void applyFilter(TableRowSorter<GalleryTableModel> sorter) {
        String text = filterField.getText().trim();
        sorter.setRowFilter(text.isEmpty() ? null
                : javax.swing.RowFilter.regexFilter("(?i)" + java.util.regex.Pattern.quote(text)));
    }

    private GalleryTheme selected() {
        int viewRow = table.getSelectedRow();
        if (viewRow < 0) {
            return null;
        }
        return model.themeAt(table.convertRowIndexToModel(viewRow));
    }

    private void load() {
        load(false);
    }

    private void load(boolean force) {
        status.setText(Messages.get("APP_GALLERY_LOADING", "Loading the gallery..."));
        openButton.setEnabled(false);
        executor.submit(() -> {
            try {
                List<GalleryTheme> themes = client.fetch(force);
                SwingUtilities.invokeLater(() -> {
                    model.setThemes(themes);
                    status.setText(Messages.format("APP_GALLERY_COUNT", "%s themes available", themes.size()));
                });
            } catch (Exception ex) {
                SwingUtilities.invokeLater(() -> status.setText(
                        Messages.format("APP_GALLERY_FAILED", "Could not load the gallery: %s", message(ex))));
            }
        });
    }

    private void loadPreview() {
        GalleryTheme theme = selected();
        preview.setIcon(null);
        if (theme == null) {
            preview.setText(Messages.get("APP_GALLERY_PREVIEW", "Select a theme to see its preview"));
            return;
        }
        preview.setText(Messages.get("APP_GALLERY_PREVIEW_LOADING", "Loading preview..."));
        executor.submit(() -> {
            byte[] bytes = client.image(theme.previewUrl());
            SwingUtilities.invokeLater(() -> {
                if (selected() != theme) {
                    return;
                }
                byte[] current = bytes;
                if (current == null) {
                    preview.setText(Messages.get("APP_GALLERY_NO_PREVIEW", "No preview"));
                    return;
                }
                try (var in = new java.io.ByteArrayInputStream(current)) {
                    java.awt.image.BufferedImage image = ImageIO.read(in);
                    if (image == null) {
                        preview.setText(Messages.get("APP_GALLERY_NO_PREVIEW", "No preview"));
                        return;
                    }
                    preview.setText(null);
                    int maxWidth = 400;
                    int maxHeight = 300;
                    double scale = Math.min(maxWidth / (double) image.getWidth(),
                            maxHeight / (double) image.getHeight());
                    int width = Math.max(1, (int) Math.round(image.getWidth() * scale));
                    int height = Math.max(1, (int) Math.round(image.getHeight() * scale));
                    preview.setIcon(new javax.swing.ImageIcon(
                            image.getScaledInstance(width, height, java.awt.Image.SCALE_SMOOTH)));
                } catch (Exception ex) {
                    preview.setText(Messages.get("APP_GALLERY_NO_PREVIEW", "No preview"));
                }
            });
        });
    }

    private void downloadSelected() {
        GalleryTheme theme = selected();
        if (theme == null) {
            return;
        }
        openButton.setEnabled(false);
        executor.submit(() -> {
            try {
                Path folder = themesFolder(theme);
                Path themeFile = client.download(theme, folder,
                        message -> SwingUtilities.invokeLater(() -> status.setText(message)));
                SwingUtilities.invokeLater(() -> {
                    studio.openFile(themeFile);
                    status.setText(Messages.format("APP_GALLERY_OPENED", "Opened %s", theme.name()));
                    openButton.setEnabled(true);
                });
            } catch (Exception ex) {
                SwingUtilities.invokeLater(() -> {
                    status.setText(Messages.format("APP_GALLERY_FAILED", "Could not load the gallery: %s", message(ex)));
                    openButton.setEnabled(true);
                });
            }
        });
    }

    private static Path themesFolder(GalleryTheme theme) {
        return dev.zoroaster1x.vlcskin.gallery.ThemeGalleryClient.themesFolder(theme);
    }

    private static String message(Exception ex) {
        return ex.getMessage() == null ? ex.toString() : ex.getMessage();
    }

    private static final class GalleryTableModel extends AbstractTableModel {
        private static final String[] COLUMNS = {"Theme", "Author", "Size", "Downloads", "Date"};
        private List<GalleryTheme> themes = List.of();

        void setThemes(List<GalleryTheme> themes) {
            this.themes = List.copyOf(themes);
            fireTableDataChanged();
        }

        GalleryTheme themeAt(int row) {
            return themes.get(row);
        }

        @Override
        public int getRowCount() {
            return themes.size();
        }

        @Override
        public int getColumnCount() {
            return COLUMNS.length;
        }

        @Override
        public String getColumnName(int column) {
            return COLUMNS[column];
        }

        @Override
        public Object getValueAt(int row, int column) {
            GalleryTheme theme = themes.get(row);
            return switch (column) {
                case 0 -> theme.name();
                case 1 -> theme.author();
                case 2 -> theme.size();
                case 3 -> theme.downloads();
                default -> theme.date();
            };
        }
    }
}
