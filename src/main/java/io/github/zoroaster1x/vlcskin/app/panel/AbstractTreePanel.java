package io.github.zoroaster1x.vlcskin.app.panel;

import io.github.zoroaster1x.vlcskin.app.Studio;
import io.github.zoroaster1x.vlcskin.app.component.Icons;
import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.FlowLayout;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTree;
import javax.swing.SwingConstants;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeCellRenderer;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreePath;

/**
 * Shared plumbing for the tree side panels.
 */
abstract class AbstractTreePanel extends JPanel {

    protected final Studio studio;
    protected final JTree tree;
    protected final DefaultTreeModel model = new DefaultTreeModel(new DefaultMutableTreeNode("root"));
    protected final JPanel toolbar = new JPanel(new FlowLayout(FlowLayout.LEFT, 4, 4));
    private final java.util.function.BooleanSupplier refreshGuard;

    AbstractTreePanel(Studio studio, String title, java.util.function.BooleanSupplier refreshGuard) {
        this.studio = studio;
        this.refreshGuard = refreshGuard;
        setLayout(new BorderLayout());
        tree = new JTree(model);
        tree.setRootVisible(false);
        tree.setShowsRootHandles(true);
        tree.setRowHeight(22);
        tree.setCellRenderer(new RefRenderer());
        tree.getSelectionModel().setSelectionMode(javax.swing.tree.TreeSelectionModel.SINGLE_TREE_SELECTION);
        tree.setDragEnabled(true);
        tree.setDropMode(javax.swing.DropMode.ON_OR_INSERT);
        tree.setTransferHandler(new ItemTransferHandler(this));
        tree.addTreeSelectionListener(e -> {
            if (!refreshGuard.getAsBoolean()) {
                selectionChanged(selectedRef());
            }
        });
        tree.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2) {
                    editSelected();
                }
            }

            @Override
            public void mousePressed(MouseEvent e) {
                maybeShowContextMenu(e);
            }

            @Override
            public void mouseReleased(MouseEvent e) {
                maybeShowContextMenu(e);
            }

            private void maybeShowContextMenu(MouseEvent e) {
                if (!e.isPopupTrigger()) {
                    return;
                }
                TreePath path = tree.getPathForLocation(e.getX(), e.getY());
                if (path != null) {
                    tree.setSelectionPath(path);
                }
                javax.swing.JPopupMenu menu = buildContextMenu(selectedRef());
                if (menu != null && menu.getComponentCount() > 0) {
                    menu.show(tree, e.getX(), e.getY());
                }
            }
        });

        JPanel header = new JPanel(new BorderLayout());
        JLabel label = new JLabel(title);
        label.setBorder(javax.swing.BorderFactory.createEmptyBorder(6, 8, 2, 8));
        label.setFont(label.getFont().deriveFont(java.awt.Font.BOLD, 12f));
        header.add(label, BorderLayout.WEST);
        header.add(toolbar, BorderLayout.SOUTH);
        add(header, BorderLayout.NORTH);
        add(new JScrollPane(tree), BorderLayout.CENTER);
    }

    protected JButton toolButton(String tooltip, String icon, Runnable action) {
        JButton button = new JButton(Icons.of(icon, 14));
        button.setToolTipText(tooltip);
        button.setFocusable(true);
        button.putClientProperty("JButton.buttonType", "toolBarButton");
        button.addActionListener(e -> action.run());
        return button;
    }

    protected TreeRef selectedRef() {
        TreePath path = tree.getSelectionModel().getSelectionPath();
        if (path == null) {
            return null;
        }
        Object node = path.getLastPathComponent();
        if (node instanceof DefaultMutableTreeNode mutable && mutable.getUserObject() instanceof TreeRef ref) {
            return ref;
        }
        return null;
    }

    /**
     * Selects a node by kind and id; ids are not unique across kinds.
     */
    protected void select(TreeRef.Kind kind, String id) {
        if (id == null) {
            return;
        }
        TreePath current = tree.getSelectionModel().getSelectionPath();
        if (current != null && current.getLastPathComponent() instanceof DefaultMutableTreeNode node
                && node.getUserObject() instanceof TreeRef ref && kind == ref.kind() && id.equals(ref.id())) {
            return;
        }
        for (int row = 0; row < tree.getRowCount(); row++) {
            TreePath path = tree.getPathForRow(row);
            if (path != null && path.getLastPathComponent() instanceof DefaultMutableTreeNode node
                    && node.getUserObject() instanceof TreeRef ref && kind == ref.kind() && id.equals(ref.id())) {
                tree.setSelectionPath(path);
                tree.scrollPathToVisible(path);
                return;
            }
        }
    }

    /**
     * Selects either a resource or a sub bitmap by id.
     */
    protected void selectResource(String id) {
        if (id == null) {
            return;
        }
        for (int row = 0; row < tree.getRowCount(); row++) {
            TreePath path = tree.getPathForRow(row);
            if (path != null && path.getLastPathComponent() instanceof DefaultMutableTreeNode node
                    && node.getUserObject() instanceof TreeRef ref
                    && (ref.kind() == TreeRef.Kind.RESOURCE || ref.kind() == TreeRef.Kind.SUB_BITMAP)
                    && id.equals(ref.id())) {
                tree.setSelectionPath(path);
                tree.scrollPathToVisible(path);
                return;
            }
        }
    }

    /**
     * Rebuilds the model from the session. Must be cheap and idempotent.
     */
    public abstract void refresh();

    protected void selectionChanged(TreeRef ref) {
    }

    protected void editSelected() {
    }

    /**
     * The right click menu for a node; empty when nothing applies.
     */
    protected javax.swing.JPopupMenu buildContextMenu(TreeRef ref) {
        return new javax.swing.JPopupMenu();
    }

    protected javax.swing.JMenuItem menuItem(java.awt.Container menu, String text, Runnable action) {
        javax.swing.JMenuItem item = new javax.swing.JMenuItem(text);
        item.addActionListener(e -> action.run());
        menu.add(item);
        return item;
    }

    private static final class RefRenderer extends DefaultTreeCellRenderer {

        @Override
        public Component getTreeCellRendererComponent(JTree tree, Object value, boolean selected, boolean expanded,
                                                      boolean leaf, int row, boolean hasFocus) {
            JLabel label = (JLabel) super.getTreeCellRendererComponent(tree, value, selected, expanded, leaf, row,
                    hasFocus);
            label.setHorizontalAlignment(SwingConstants.LEFT);
            if (value instanceof DefaultMutableTreeNode node && node.getUserObject() instanceof TreeRef ref) {
                label.setText(ref.label());
                label.setIcon(Icons.of(iconFor(ref), 14));
                if (label.getForeground() != null && !selected
                        && ref.kind() == TreeRef.Kind.SLIDER_BACKGROUND) {
                    label.setForeground(javax.swing.UIManager.getColor("Label.disabledForeground"));
                }
            }
            return label;
        }

        private static String iconFor(TreeRef ref) {
            if (ref.kind() == TreeRef.Kind.ITEM && ref.itemType() != null) {
                return switch (ref.itemType()) {
                    case "ANCHOR" -> "anchor";
                    case "BUTTON" -> "button";
                    case "CHECKBOX" -> "checkbox";
                    case "GROUP" -> "group";
                    case "IMAGE" -> "image";
                    case "PANEL" -> "panel";
                    case "PLAYLIST", "PLAYTREE" -> "playlist";
                    case "RADIAL_SLIDER", "SLIDER", "SLIDER_BACKGROUND" -> "slider";
                    case "TEXT" -> "text";
                    case "VIDEO" -> "video";
                    default -> "panel";
                };
            }
            return switch (ref.kind()) {
                case THEME -> "layers";
                case RESOURCE -> "image";
                case SUB_BITMAP -> "grid";
                case WINDOW -> "window";
                case LAYOUT -> "layout";
                case ITEM -> "panel";
                case SLIDER_BACKGROUND -> "slider";
            };
        }
    }
}
