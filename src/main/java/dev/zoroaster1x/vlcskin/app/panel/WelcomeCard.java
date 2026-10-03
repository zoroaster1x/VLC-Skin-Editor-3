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
import java.awt.GridLayout;
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

        JPanel card = new JPanel();
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setBackground(UIManager.getColor("Panel.background"));
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(UIManager.getColor("Component.borderColor"), 1, true),
                BorderFactory.createEmptyBorder(28, 34, 28, 34)));

        JLabel title = new JLabel(Messages.get("WELCOME_TITLE", "VLC Skin Studio"));
        title.setFont(title.getFont().deriveFont(Font.BOLD, 24f));
        title.setAlignmentX(LEFT_ALIGNMENT);
        JLabel subtitle = new JLabel(Messages.get("APP_WELCOME_SUBTITLE",
                "Design VLC skins2 themes with a live preview."));
        subtitle.setForeground(UIManager.getColor("Label.disabledForeground"));
        subtitle.setAlignmentX(LEFT_ALIGNMENT);

        JPanel actions = new JPanel(new GridLayout(1, 3, 10, 0));
        actions.setOpaque(false);
        actions.setAlignmentX(LEFT_ALIGNMENT);
        actions.add(button(Messages.get("WELCOME_NEW", "New skin"), "new", e -> studio.newSkin()));
        actions.add(button(Messages.get("WELCOME_OPEN", "Open skin"), "open", e -> studio.openDialog(this)));
        actions.add(button(Messages.get("APP_WELCOME_EXAMPLES", "Examples"), "layers", e -> showExamples()));
        actions.setMaximumSize(new Dimension(520, 40));

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
        add(card);
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
        JLabel label = new JLabel("<html><b>" + example.name() + "</b><br><span style='font-size:9px'>"
                + example.description() + "</span></html>");
        label.setHorizontalAlignment(SwingConstants.LEFT);
        row.add(label, BorderLayout.CENTER);
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
        row.setMaximumSize(new Dimension(520, 48));
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
