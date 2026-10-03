package io.github.zoroaster1x.vlcskin.app.panel;

import io.github.zoroaster1x.vlcskin.app.Studio;
import io.github.zoroaster1x.vlcskin.app.i18n.Messages;
import io.github.zoroaster1x.vlcskin.format.SkinValidator;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.List;
import javax.swing.DefaultListModel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JToolBar;
import javax.swing.ListSelectionModel;

/**
 * Validation results, refreshed on demand or after edits.
 */
public final class ProblemsPanel extends JPanel {

    private final Studio studio;
    private final DefaultListModel<String> model = new DefaultListModel<>();
    private final JList<String> list = new JList<>(model);
    private final java.util.List<io.github.zoroaster1x.vlcskin.format.ParseIssue> issues = new java.util.ArrayList<>();

    public ProblemsPanel(Studio studio) {
        this.studio = studio;
        setLayout(new BorderLayout());
        list.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        list.setCellRenderer(new javax.swing.DefaultListCellRenderer() {
            @Override
            public java.awt.Component getListCellRendererComponent(JList<?> source, Object value, int index,
                                                                   boolean selected, boolean focus) {
                java.awt.Component component = super.getListCellRendererComponent(source, value, index, selected,
                        focus);
                if (index >= 0 && index < issues.size()) {
                    var severity = issues.get(index).severity();
                    if (!selected) {
                        component.setForeground(switch (severity) {
                            case ERROR -> new Color(0xE0, 0x51, 0x51);
                            case WARNING -> new Color(0xD8, 0x9B, 0x2E);
                            case INFO -> javax.swing.UIManager.getColor("Label.foreground");
                        });
                    }
                }
                return component;
            }
        });
        list.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2) {
                    navigate();
                }
            }
        });

        JToolBar bar = new JToolBar();
        bar.setFloatable(false);
        javax.swing.JButton validate = new javax.swing.JButton(Messages.get("APP_PROBLEMS_VALIDATE", "Validate now"),
                io.github.zoroaster1x.vlcskin.app.component.Icons.of("validate", 14));
        validate.addActionListener(e -> validate());
        bar.add(validate);
        javax.swing.JButton navigate = new javax.swing.JButton(Messages.get("APP_PROBLEMS_GOTO", "Go to element"));
        navigate.addActionListener(e -> navigate());
        bar.add(navigate);

        add(bar, BorderLayout.NORTH);
        add(new JScrollPane(list), BorderLayout.CENTER);
    }

    public void validate() {
        issues.clear();
        model.clear();
        List<io.github.zoroaster1x.vlcskin.format.ParseIssue> found = SkinValidator.validate(
                studio.session().theme(),
                studio.session().file() == null ? null : studio.session().file().getParent());
        for (var issue : found) {
            issues.add(issue);
            model.addElement(issue.toString());
        }
        if (model.isEmpty()) {
            model.addElement(Messages.get("APP_PROBLEMS_NONE", "No problems found."));
        }
    }

    private void navigate() {
        int index = list.getSelectedIndex();
        if (index < 0 || index >= issues.size()) {
            return;
        }
        String location = issues.get(index).location();
        if (location == null) {
            return;
        }
        String id = extractId(location);
        if (id == null) {
            return;
        }
        var index2 = studio.session().index();
        if (index2.findItem(id) != null) {
            studio.session().selection().selectItem(id);
        } else if (index2.findResource(id) != null) {
            studio.session().selection().selectResource(id);
        } else if (index2.findAnyLayout(id) != null) {
            var layout = index2.findAnyLayout(id);
            var window = index2.windowOf(layout);
            if (window != null) {
                studio.session().selection().selectLayout(window.getId(), id);
            }
        }
        studio.session().fireChanged();
    }

    private String extractId(String location) {
        int open = location.indexOf('[');
        int close = location.lastIndexOf(']');
        return open >= 0 && close > open ? location.substring(open + 1, close) : null;
    }
}
