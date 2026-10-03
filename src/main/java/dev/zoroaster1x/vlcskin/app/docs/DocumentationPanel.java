package dev.zoroaster1x.vlcskin.app.docs;

import dev.zoroaster1x.vlcskin.app.i18n.Messages;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.nio.file.Path;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.List;
import java.util.Locale;
import javax.swing.BorderFactory;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JEditorPane;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.Timer;
import javax.swing.event.HyperlinkEvent;
import javax.swing.text.html.HTMLEditorKit;

/**
 * The built in documentation viewer: a live search over every page, heading and
 * line on the left, the rendered markdown on the right, images included. All of
 * it works offline from the bundled documentation.
 */
public final class DocumentationPanel extends JPanel {

    private final DocumentationBundle bundle = new DocumentationBundle();
    private final DocumentationSearch search = new DocumentationSearch(bundle);
    private final MarkdownRenderer renderer = new MarkdownRenderer();
    private final JTextField searchField = new JTextField();
    private final JLabel searchHint = new JLabel(" ");
    private final DefaultListModel<Object> listModel = new DefaultListModel<>();
    private final JList<Object> resultList = new JList<>(listModel);
    private final JEditorPane viewer = new JEditorPane();
    private final JLabel status = new JLabel(" ");
    private final JLabel breadcrumb = new JLabel(" ");
    private final JLabel source = new JLabel(" ");
    private final JButton backButton = new JButton(Messages.get("APP_DOCS_BACK", "Back"));
    private final JButton forwardButton = new JButton(Messages.get("APP_DOCS_FORWARD", "Forward"));
    private final JButton onlineButton = new JButton(Messages.get("APP_DOCS_ONLINE", "Open online"));
    private final Deque<String> history = new ArrayDeque<>();
    private final Deque<String> forwardHistory = new ArrayDeque<>();
    private final Timer debounce;
    private Path bundleFolder;
    private String currentTopicId;
    private String activeQuery = "";

    public DocumentationPanel() {
        setLayout(new BorderLayout());
        try {
            bundleFolder = bundle.materialize();
        } catch (Exception ex) {
            bundleFolder = null;
        }

        debounce = new Timer(180, event -> runSearch(searchField.getText()));
        debounce.setRepeats(false);

        searchField.putClientProperty("JTextField.placeholderText",
                Messages.get("APP_DOCS_SEARCH_HINT", "Search pages, headings and text"));
        searchField.addActionListener(event -> {
            debounce.stop();
            runSearch(searchField.getText());
        });
        searchField.getDocument().addDocumentListener(new javax.swing.event.DocumentListener() {
            @Override
            public void insertUpdate(javax.swing.event.DocumentEvent e) {
                debounce.restart();
            }

            @Override
            public void removeUpdate(javax.swing.event.DocumentEvent e) {
                debounce.restart();
            }

            @Override
            public void changedUpdate(javax.swing.event.DocumentEvent e) {
                debounce.restart();
            }
        });

        searchHint.setForeground(javax.swing.UIManager.getColor("Label.disabledForeground"));
        searchHint.setFont(searchHint.getFont().deriveFont(11f));

        resultList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        resultList.setFixedCellHeight(-1);
        resultList.setCellRenderer(new ResultRenderer());
        resultList.addListSelectionListener(event -> {
            if (event.getValueIsAdjusting()) {
                return;
            }
            Object value = resultList.getSelectedValue();
            if (value instanceof DocumentationBundle.Topic topic) {
                activeQuery = "";
                open(topic, true);
            } else if (value instanceof DocumentationSearch.Hit hit) {
                activeQuery = searchField.getText();
                open(hit.topic(), true);
            }
        });

        backButton.setEnabled(false);
        forwardButton.setEnabled(false);
        backButton.addActionListener(event -> goBack());
        forwardButton.addActionListener(event -> goForward());
        JButton topicsButton = new JButton(Messages.get("APP_DOCS_TOPICS", "Topics"));
        topicsButton.addActionListener(event -> {
            searchField.setText("");
            showTopicsList("");
        });
        onlineButton.addActionListener(event -> browse(currentSource()));

        JPanel toolbar = new JPanel();
        toolbar.setLayout(new javax.swing.BoxLayout(toolbar, javax.swing.BoxLayout.X_AXIS));
        toolbar.setBorder(BorderFactory.createEmptyBorder(8, 8, 8, 8));
        toolbar.add(backButton);
        toolbar.add(javax.swing.Box.createHorizontalStrut(4));
        toolbar.add(forwardButton);
        toolbar.add(javax.swing.Box.createHorizontalStrut(12));
        toolbar.add(topicsButton);
        toolbar.add(javax.swing.Box.createHorizontalGlue());
        toolbar.add(onlineButton);

        JPanel searchRow = new JPanel(new BorderLayout(6, 2));
        searchRow.setBorder(BorderFactory.createEmptyBorder(8, 8, 4, 8));
        searchRow.add(searchField, BorderLayout.CENTER);
        searchRow.add(searchHint, BorderLayout.SOUTH);

        JPanel left = new JPanel(new BorderLayout());
        left.add(searchRow, BorderLayout.NORTH);
        left.add(new JScrollPane(resultList), BorderLayout.CENTER);
        left.setPreferredSize(new Dimension(360, 100));

        viewer.setEditable(false);
        viewer.putClientProperty(JEditorPane.HONOR_DISPLAY_PROPERTIES, Boolean.TRUE);
        viewer.setEditorKit(new HTMLEditorKit());
        viewer.addHyperlinkListener(event -> {
            if (event.getEventType() != HyperlinkEvent.EventType.ACTIVATED) {
                return;
            }
            String href = event.getDescription();
            if (href == null) {
                return;
            }
            if (href.startsWith("doc:")) {
                DocumentationBundle.Topic topic = bundle.topic(href.substring("doc:".length()));
                if (topic != null) {
                    activeQuery = "";
                    open(topic, true);
                }
            } else if (href.startsWith("http")) {
                browse(href);
            }
        });

        JPanel right = new JPanel(new BorderLayout());
        right.add(new JScrollPane(viewer), BorderLayout.CENTER);
        JPanel footer = new JPanel(new BorderLayout(8, 0));
        footer.setBorder(BorderFactory.createEmptyBorder(4, 8, 6, 8));
        footer.add(status, BorderLayout.WEST);
        footer.add(source, BorderLayout.EAST);
        right.add(footer, BorderLayout.SOUTH);

        JPanel page = new JPanel(new BorderLayout());
        breadcrumb.setBorder(BorderFactory.createEmptyBorder(6, 10, 0, 10));
        breadcrumb.setFont(breadcrumb.getFont().deriveFont(java.awt.Font.BOLD, 12f));
        page.add(breadcrumb, BorderLayout.NORTH);
        page.add(right, BorderLayout.CENTER);

        JPanel main = new JPanel(new BorderLayout());
        main.add(toolbar, BorderLayout.NORTH);
        main.add(left, BorderLayout.WEST);
        if (bundle.available()) {
            main.add(page, BorderLayout.CENTER);
        } else {
            JLabel missing = new JLabel(Messages.get("APP_DOCS_MISSING",
                    "The documentation bundle is not part of this build."));
            missing.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
            main.add(missing, BorderLayout.CENTER);
        }
        add(main, BorderLayout.CENTER);

        if (bundle.available()) {
            showTopicsList("");
            open(bundle.topics().get(0), false);
        }
    }

    public boolean bundleAvailable() {
        return bundle.available();
    }

    /**
     * Runs the ranked search now, skipping the debounce.
     */
    public void searchAll(String query) {
        runSearch(query);
    }

    public void selectTopic(String id) {
        DocumentationBundle.Topic topic = bundle.topic(id);
        if (topic != null) {
            activeQuery = "";
            open(topic, true);
        }
    }

    public String pageText() {
        return viewer.getText();
    }

    public String statusText() {
        return status.getText();
    }

    public int listSize() {
        return listModel.size();
    }

    private void runSearch(String query) {
        String text = query == null ? "" : query.strip();
        if (text.isEmpty()) {
            showTopicsList("");
            return;
        }
        List<DocumentationSearch.Hit> hits = search.search(text, 250);
        listModel.clear();
        for (DocumentationSearch.Hit hit : hits) {
            listModel.addElement(hit);
        }
        status.setText(Messages.format("APP_DOCS_RESULTS", "%s matches", hits.size()));
        searchHint.setText(searchSummary(text, hits.size()));
    }

    private String searchSummary(String query, int count) {
        if (count == 0) {
            return Messages.format("APP_DOCS_NO_RESULTS",
                    "Nothing for \"%s\". Try a single word such as button, slider or layout.", query);
        }
        return Messages.format("APP_DOCS_RESULTS_HINT",
                "%s results for \"%s\", ranked by title, heading and text.", count, query);
    }

    private void showTopicsList(String filter) {
        listModel.clear();
        String needle = filter == null ? "" : filter.toLowerCase(Locale.ROOT).trim();
        for (DocumentationBundle.Topic topic : bundle.topics()) {
            if (needle.isEmpty()
                    || topic.title().toLowerCase(Locale.ROOT).contains(needle)
                    || topic.section().toLowerCase(Locale.ROOT).contains(needle)) {
                listModel.addElement(topic);
            }
        }
        status.setText(Messages.format("APP_DOCS_TOPICS_COUNT", "%s topics", listModel.size()));
        searchHint.setText(searchField.getText().isBlank() ? " " : Messages.get("APP_DOCS_SEARCH_HINT",
                "Search pages, headings and text"));
    }

    private void open(DocumentationBundle.Topic topic, boolean record) {
        if (record && currentTopicId != null && !currentTopicId.equals(topic.id())) {
            history.push(currentTopicId);
            forwardHistory.clear();
        }
        currentTopicId = topic.id();
        render(topic);
        backButton.setEnabled(!history.isEmpty());
        forwardButton.setEnabled(!forwardHistory.isEmpty());
    }

    private void goBack() {
        if (history.isEmpty()) {
            return;
        }
        if (currentTopicId != null) {
            forwardHistory.push(currentTopicId);
        }
        currentTopicId = history.pop();
        DocumentationBundle.Topic previous = bundle.topic(currentTopicId);
        if (previous != null) {
            render(previous);
        }
        backButton.setEnabled(!history.isEmpty());
        forwardButton.setEnabled(!forwardHistory.isEmpty());
    }

    private void goForward() {
        if (forwardHistory.isEmpty()) {
            return;
        }
        if (currentTopicId != null) {
            history.push(currentTopicId);
        }
        currentTopicId = forwardHistory.pop();
        DocumentationBundle.Topic next = bundle.topic(currentTopicId);
        if (next != null) {
            render(next);
        }
        backButton.setEnabled(!history.isEmpty());
        forwardButton.setEnabled(!forwardHistory.isEmpty());
    }

    private void render(DocumentationBundle.Topic topic) {
        try {
            String markdown = bundle.markdown(topic);
            Path folder = bundleFolder == null ? Path.of(".") : bundleFolder;
            String html = renderer.toHtml(markdown, folder, topic.file());
            if (!activeQuery.isBlank()) {
                html = renderer.highlight(html, DocumentationSearch.terms(activeQuery));
            }
            viewer.setText(html);
            viewer.setCaretPosition(0);
            status.setText(topic.section() + " / " + topic.title());
            source.setText(topic.sourceUrl() == null || topic.sourceUrl().isBlank() ? " " : topic.sourceUrl());
            onlineButton.setEnabled(topic.sourceUrl() != null && topic.sourceUrl().startsWith("http"));
            breadcrumb.setText(topic.section() + "   >   " + topic.title());
        } catch (Exception ex) {
            viewer.setText("<html><body><p>Could not open " + topic.file() + ": " + ex.getMessage() + "</p></body></html>");
        }
    }

    private String currentSource() {
        String text = source.getText();
        return text == null || text.isBlank() ? "https://images.videolan.org/vlc/skinedhlp/" : text.strip();
    }

    private static void browse(String url) {
        if (url == null || !url.startsWith("http")) {
            return;
        }
        try {
            if (java.awt.Desktop.isDesktopSupported()) {
                java.awt.Desktop.getDesktop().browse(java.net.URI.create(url));
            }
        } catch (Exception ex) {
            // Opening a browser is best effort.
        }
    }

    /**
     * Two or three line cells: title, where it lives, and the matching text.
     */
    private final class ResultRenderer extends javax.swing.DefaultListCellRenderer {
        @Override
        public java.awt.Component getListCellRendererComponent(JList<?> list, Object value, int index,
                                                               boolean selected, boolean focused) {
            JLabel label = (JLabel) super.getListCellRendererComponent(list, value, index, selected, focused);
            if (value instanceof DocumentationBundle.Topic topic) {
                label.setText("<html><b>" + mark(topic.title()) + "</b><br><span style='font-size:9px'>"
                        + escape(topic.section()) + "</span></html>");
            } else if (value instanceof DocumentationSearch.Hit hit) {
                String path = pathText(hit);
                label.setText("<html><b>" + mark(hit.topic().title()) + "</b>"
                        + "<br><span style='font-size:9px;color:gray'>" + escape(path) + "</span>"
                        + "<br><span style='font-size:10px'>" + mark(hit.snippet()) + "</span></html>");
            }
            label.setBorder(BorderFactory.createEmptyBorder(4, 4, 4, 4));
            return label;
        }

        private static String pathText(DocumentationSearch.Hit hit) {
            StringBuilder path = new StringBuilder(hit.topic().section());
            for (DocumentationSearch.Heading heading : hit.path()) {
                path.append("  >  ").append(heading.text());
            }
            path.append("  (line ").append(hit.line()).append(", ").append(hit.occurrences()).append(" hits)");
            return path.toString();
        }

        private String mark(String text) {
            String escaped = escape(text);
            for (String term : DocumentationSearch.terms(searchField.getText())) {
                escaped = escaped.replaceAll("(?i)(" + java.util.regex.Pattern.quote(term) + ")",
                        "<b style='color:#E06C38'>$1</b>");
            }
            return escaped;
        }

        private static String escape(String text) {
            return text == null ? "" : text.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;");
        }
    }
}
