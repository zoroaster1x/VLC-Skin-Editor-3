package dev.zoroaster1x.vlcskin.app.panel;

import dev.zoroaster1x.vlcskin.app.Studio;
import dev.zoroaster1x.vlcskin.app.component.Icons;
import dev.zoroaster1x.vlcskin.app.i18n.Messages;
import dev.zoroaster1x.vlcskin.example.ExampleSkins;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.nio.file.Path;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextArea;
import javax.swing.SwingConstants;
import javax.swing.UIManager;

/**
 * The first screen: start a skin, open one, or try an example.
 */
final class WelcomeCard extends JPanel {

    private final Studio studio;
    private final JPanel recentPanel = new JPanel();

    WelcomeCard(Studio studio) {
        this.studio = studio;
        setOpaque(true);
        setBackground(new Color(0x16, 0x18, 0x1D));
        setLayout(new java.awt.GridBagLayout());

        JPanel card = new TrackWidthPanel();
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setBackground(UIManager.getColor("Panel.background"));
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(UIManager.getColor("Component.borderColor"), 1, true),
                BorderFactory.createEmptyBorder(28, 34, 28, 34)));

        JTextArea title = wrapArea(Messages.get("WELCOME_TITLE", "VLC Skin Studio"),
                getFont().deriveFont(Font.BOLD, 24f), null);
        JTextArea subtitle = wrapArea(Messages.get("APP_WELCOME_SUBTITLE",
                        "Design VLC skins2 themes with a live preview."),
                getFont().deriveFont(Font.PLAIN, 12f), UIManager.getColor("Label.disabledForeground"));

        JPanel actions = new JPanel(new java.awt.FlowLayout(java.awt.FlowLayout.LEFT, 10, 6));
        actions.setOpaque(false);
        actions.setAlignmentX(LEFT_ALIGNMENT);
        actions.add(button(Messages.get("WELCOME_NEW", "New skin"), "new", e -> studio.newSkin()));
        actions.add(button(Messages.get("WELCOME_OPEN", "Open skin"), "open", e -> studio.openDialog(this)));
        actions.add(button(Messages.get("APP_WELCOME_EXAMPLES", "Examples"), "layers", e -> showExamples()));
        actions.setMaximumSize(new Dimension(Integer.MAX_VALUE, 48));

        recentPanel.setOpaque(false);
        recentPanel.setLayout(new BoxLayout(recentPanel, BoxLayout.Y_AXIS));
        recentPanel.setAlignmentX(LEFT_ALIGNMENT);

        card.add(title);
        card.add(Box.createVerticalStrut(6));
        card.add(subtitle);
        card.add(Box.createVerticalStrut(22));
        card.add(actions);
        card.add(Box.createVerticalStrut(18));
        card.add(sectionLabel(Messages.get("APP_WELCOME_RECENT", "Recent")));
        card.add(Box.createVerticalStrut(6));
        card.add(recentPanel);
        card.add(Box.createVerticalStrut(14));
        card.add(sectionLabel(Messages.get("APP_WELCOME_EXAMPLES", "Examples")));
        card.add(Box.createVerticalStrut(6));
        for (ExampleSkins.Example example : ExampleSkins.catalog()) {
            card.add(exampleRow(example));
        }
        card.add(Box.createVerticalGlue());

        javax.swing.JScrollPane scroll = new javax.swing.JScrollPane(card,
                javax.swing.ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED,
                javax.swing.ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        scroll.setBorder(null);
        scroll.setOpaque(false);
        scroll.getViewport().setOpaque(false);
        scroll.getVerticalScrollBar().setUnitIncrement(16);

        java.awt.GridBagConstraints constraints = new java.awt.GridBagConstraints();
        constraints.fill = java.awt.GridBagConstraints.BOTH;
        constraints.weightx = 1;
        constraints.weighty = 1;
        constraints.insets = new java.awt.Insets(18, 18, 18, 18);
        add(scroll, constraints);
    }

    /**
     * The card stretches to the viewport width so the buttons wrap and the text
     * gets the room the panel actually has.
     */
    private static final class TrackWidthPanel extends JPanel implements javax.swing.Scrollable {
        @Override
        public Dimension getPreferredScrollableViewportSize() {
            return getPreferredSize();
        }

        @Override
        public int getScrollableUnitIncrement(java.awt.Rectangle visible, int orientation, int direction) {
            return 16;
        }

        @Override
        public int getScrollableBlockIncrement(java.awt.Rectangle visible, int orientation, int direction) {
            return visible.height;
        }

        @Override
        public boolean getScrollableTracksViewportWidth() {
            return true;
        }

        @Override
        public boolean getScrollableTracksViewportHeight() {
            return false;
        }
    }

    /**
     * A text area that wraps and reports a height for the width it actually
     * gets, so the welcome card works in a narrow dock.
     */
    private static final class WrapArea extends javax.swing.JTextArea {
        WrapArea(String text, Font font, Color color) {
            super(text);
            setFont(font);
            if (color != null) {
                setForeground(color);
            }
            setOpaque(false);
            setEditable(false);
            setFocusable(false);
            setLineWrap(true);
            setWrapStyleWord(true);
            setColumns(1);
            setRows(1);
            setBorder(null);
            setAlignmentX(LEFT_ALIGNMENT);
        }

        @Override
        public Dimension getPreferredSize() {
            int width = getWidth() > 0 ? getWidth() : 240;
            setSize(width, Integer.MAX_VALUE);
            Dimension size = super.getPreferredSize();
            return new Dimension(width, size.height);
        }
    }

    private static JTextArea wrapArea(String text, Font font, Color color) {
        return new WrapArea(text, font, color);
    }

    private JLabel sectionLabel(String text) {
        JLabel label = new JLabel(text);
        label.setFont(label.getFont().deriveFont(Font.BOLD, 12f));
        label.setForeground(UIManager.getColor("Label.disabledForeground"));
        label.setAlignmentX(LEFT_ALIGNMENT);
        return label;
    }

    private JButton button(String text, String icon, java.awt.event.ActionListener listener) {
        JButton button = new JButton(text, Icons.of(icon));
        button.addActionListener(listener);
        button.setFocusable(true);
        return button;
    }

    private JPanel exampleRow(ExampleSkins.Example example) {
        JPanel row = new JPanel(new BorderLayout(8, 0));
        row.setOpaque(false);
        row.setAlignmentX(LEFT_ALIGNMENT);
        JPanel texts = new JPanel();
        texts.setOpaque(false);
        texts.setLayout(new BoxLayout(texts, BoxLayout.Y_AXIS));
        texts.add(wrapArea(example.name(), getFont().deriveFont(Font.BOLD), null));
        texts.add(wrapArea(example.description(),
                getFont().deriveFont(Font.PLAIN, 10f), UIManager.getColor("Label.disabledForeground")));
        row.add(texts, BorderLayout.CENTER);
        JButton create = new JButton(Messages.get("APP_WELCOME_CREATE", "Create"));
        create.addActionListener(e -> {
            Path folder = studio.settings().getLastDirectory() != null
                    ? Path.of(studio.settings().getLastDirectory()).resolve("vlc-skin-" + example.id())
                    : Path.of(System.getProperty("user.home"), "vlc-skin-" + example.id());
            var outcome = studio.service().createExample(example.id(), folder.toString());
            if (outcome.error()) {
                studio.error(outcome.text());
            } else {
                studio.status(Messages.format("APP_WELCOME_EXAMPLE_CREATED", "Created example in %s", folder));
                studio.session().fireChanged();
            }
        });
        row.add(create, BorderLayout.EAST);
        row.setMaximumSize(new Dimension(Integer.MAX_VALUE, 48));
        return row;
    }

    private void showExamples() {
        javax.swing.JPopupMenu menu = new javax.swing.JPopupMenu();
        for (ExampleSkins.Example example : ExampleSkins.catalog()) {
            javax.swing.JMenuItem item = new javax.swing.JMenuItem(example.name());
            item.addActionListener(e -> {
                Path folder = Path.of(System.getProperty("user.home"), "vlc-skin-" + example.id());
                var outcome = studio.service().createExample(example.id(), folder.toString());
                if (outcome.error()) {
                    studio.error(outcome.text());
                } else {
                    studio.session().fireChanged();
                }
            });
            menu.add(item);
        }
        menu.show(this, 10, 10);
    }

    void refresh() {
        recentPanel.removeAll();
        for (String file : studio.settings().getRecentFiles()) {
            JLabel link = new JLabel("<html><u>" + file + "</u></html>");
            link.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            link.setAlignmentX(LEFT_ALIGNMENT);
            link.addMouseListener(new MouseAdapter() {
                @Override
                public void mouseClicked(MouseEvent e) {
                    studio.openFile(Path.of(file));
                }
            });
            recentPanel.add(link);
        }
        if (studio.settings().getRecentFiles().isEmpty()) {
            JLabel none = new JLabel(Messages.get("APP_WELCOME_NO_RECENT", "No recent files yet"));
            none.setForeground(UIManager.getColor("Label.disabledForeground"));
            none.setAlignmentX(LEFT_ALIGNMENT);
            recentPanel.add(none);
        }
        recentPanel.revalidate();
        recentPanel.repaint();
    }

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
        g2.setColor(getBackground());
        g2.fillRect(0, 0, getWidth(), getHeight());
        g2.dispose();
    }
}
