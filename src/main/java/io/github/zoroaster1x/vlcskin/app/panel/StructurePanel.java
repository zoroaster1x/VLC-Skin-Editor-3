package io.github.zoroaster1x.vlcskin.app.panel;

import io.github.zoroaster1x.vlcskin.app.Studio;
import io.github.zoroaster1x.vlcskin.app.i18n.Messages;
import io.github.zoroaster1x.vlcskin.edit.DeepCopy;
import io.github.zoroaster1x.vlcskin.edit.commands.AddNodeCommand;
import io.github.zoroaster1x.vlcskin.edit.commands.RemoveNodeCommand;
import io.github.zoroaster1x.vlcskin.edit.commands.ReorderCommand;
import io.github.zoroaster1x.vlcskin.model.SkinLayout;
import io.github.zoroaster1x.vlcskin.model.SkinWindow;
import javax.swing.tree.DefaultMutableTreeNode;

/**
 * Windows and their layouts.
 */
public final class StructurePanel extends AbstractTreePanel {

    public StructurePanel(Studio studio, java.util.function.BooleanSupplier refreshGuard) {
        super(studio, io.github.zoroaster1x.vlcskin.app.i18n.PanelTitles.windows(), refreshGuard);
        toolbar.add(toolButton(Messages.get("WIN_WIN_ADD", "Add window"), "window", this::addWindow));
        toolbar.add(toolButton(Messages.get("WIN_WIN_ADD_LAY", "Add layout"), "layout", this::addLayout));
        toolbar.add(toolButton(Messages.get("WIN_WIN_MOVE_UP", "Move layout up"), "up", () -> moveLayout(-1)));
        toolbar.add(toolButton(Messages.get("WIN_WIN_MOVE_DOWN", "Move layout down"), "down", () -> moveLayout(1)));
        toolbar.add(toolButton(Messages.get("WIN_WIN_COPY", "Duplicate"), "duplicate", this::duplicate));
        toolbar.add(toolButton(Messages.get("WIN_WIN_DELETE", "Delete"), "delete", this::delete));
    }

    @Override
    public void refresh() {
        DefaultMutableTreeNode root = new DefaultMutableTreeNode("root");
        for (SkinWindow window : studio.session().theme().getWindows()) {
            DefaultMutableTreeNode windowNode = new DefaultMutableTreeNode(
                    new TreeRef(TreeRef.Kind.WINDOW, window.getId(),
                            Messages.get("WINDOW", "Window") + ": " + window.getId()));
            for (SkinLayout layout : window.getLayouts()) {
                windowNode.add(new DefaultMutableTreeNode(
                        new TreeRef(TreeRef.Kind.LAYOUT, layout.getId(),
                                Messages.get("LAYOUT", "Layout") + ": " + layout.getId())));
            }
            root.add(windowNode);
        }
        model.setRoot(root);
        for (int row = 0; row < tree.getRowCount(); row++) {
            tree.expandRow(row);
        }
        if (studio.session().selection().layoutId() != null) {
            select(TreeRef.Kind.LAYOUT, studio.session().selection().layoutId());
        } else {
            select(TreeRef.Kind.WINDOW, studio.session().selection().windowId());
        }
    }

    @Override
    protected void selectionChanged(TreeRef ref) {
        if (ref == null) {
            return;
        }
        if (ref.kind() == TreeRef.Kind.WINDOW) {
            studio.session().selection().selectWindow(ref.id());
        } else if (ref.kind() == TreeRef.Kind.LAYOUT) {
            SkinWindow window = studio.session().index().windowOf(
                    studio.session().index().findAnyLayout(ref.id()));
            if (window != null) {
                studio.session().selection().selectLayout(window.getId(), ref.id());
            }
        }
        studio.session().fireChanged();
    }

    private SkinWindow selectedWindow() {
        TreeRef ref = selectedRef();
        if (ref == null) {
            return null;
        }
        if (ref.kind() == TreeRef.Kind.WINDOW) {
            return studio.session().index().findWindow(ref.id());
        }
        if (ref.kind() == TreeRef.Kind.LAYOUT) {
            return studio.session().index().windowOf(studio.session().index().findAnyLayout(ref.id()));
        }
        return null;
    }

    private void addWindow() {
        var outcome = studio.service().addWindow(null, 320, 140);
        if (outcome.error()) {
            studio.error(outcome.text());
        }
        studio.session().fireChanged();
    }

    private void addLayout() {
        SkinWindow window = selectedWindow();
        if (window == null) {
            studio.error(Messages.get("ERROR_ADD_LAYOUT_MSG", "Select a window first."));
            return;
        }
        var outcome = studio.service().addLayout(window.getId(), null, window.getLayouts().isEmpty()
                ? 320 : window.getLayouts().get(0).getWidth(), 140);
        if (outcome.error()) {
            studio.error(outcome.text());
        }
        studio.session().fireChanged();
    }

    private void moveLayout(int delta) {
        TreeRef ref = selectedRef();
        if (ref == null || ref.kind() != TreeRef.Kind.LAYOUT) {
            return;
        }
        SkinLayout layout = studio.session().index().findAnyLayout(ref.id());
        SkinWindow window = layout == null ? null : studio.session().index().windowOf(layout);
        if (window == null) {
            return;
        }
        int from = window.getLayouts().indexOf(layout);
        int to = from + delta;
        if (to < 0 || to >= window.getLayouts().size()) {
            return;
        }
        studio.session().apply(new ReorderCommand<>(window.getLayouts(), from, to, "Reorder layouts"));
    }

    @Override
    protected javax.swing.JPopupMenu buildContextMenu(TreeRef ref) {
        javax.swing.JPopupMenu menu = new javax.swing.JPopupMenu();
        menuItem(menu, Messages.get("WIN_WIN_ADD", "Add window"), this::addWindow);
        menuItem(menu, Messages.get("WIN_WIN_ADD_LAY", "Add layout"), this::addLayout);
        if (ref != null) {
            menu.addSeparator();
            menuItem(menu, Messages.get("WIN_WIN_COPY", "Duplicate"), this::duplicate);
            menuItem(menu, Messages.get("WIN_WIN_DELETE", "Delete"), this::delete);
            if (ref.kind() == TreeRef.Kind.LAYOUT) {
                menu.addSeparator();
                menuItem(menu, Messages.get("WIN_WIN_MOVE_UP", "Move layout up"), () -> moveLayout(-1));
                menuItem(menu, Messages.get("WIN_WIN_MOVE_DOWN", "Move layout down"), () -> moveLayout(1));
            }
        }
        return menu;
    }

    private void duplicate() {
        TreeRef ref = selectedRef();
        if (ref == null) {
            return;
        }
        String pattern = javax.swing.JOptionPane.showInputDialog(this,
                Messages.get("DUPLICATE_MSG", "Rename pattern for the copy (%oldid% is the current id):"),
                "%oldid%_copy");
        if (pattern == null || pattern.isBlank()) {
            return;
        }
        if (ref.kind() == TreeRef.Kind.WINDOW) {
            SkinWindow window = studio.session().index().findWindow(ref.id());
            if (window == null) {
                return;
            }
            SkinWindow copy = DeepCopy.window(window, studio.session().index(), pattern);
            studio.session().apply(new AddNodeCommand<>(studio.session().theme().getWindows(), copy,
                    studio.session().theme().getWindows().size(), "Duplicate window"));
        } else if (ref.kind() == TreeRef.Kind.LAYOUT) {
            SkinLayout layout = studio.session().index().findAnyLayout(ref.id());
            SkinWindow window = layout == null ? null : studio.session().index().windowOf(layout);
            if (window == null) {
                return;
            }
            SkinLayout copy = DeepCopy.layout(layout, studio.session().index(), pattern);
            studio.session().apply(new AddNodeCommand<>(window.getLayouts(), copy,
                    window.getLayouts().indexOf(layout) + 1, "Duplicate layout"));
        }
        studio.session().fireChanged();
    }

    private void delete() {
        TreeRef ref = selectedRef();
        if (ref == null) {
            return;
        }
        int choice = javax.swing.JOptionPane.showConfirmDialog(this,
                Messages.format("DEL_CONFIRM_MSG", "Delete \"%n\"?", ref.id()),
                Messages.get("DEL_CONFIRM_TITLE", "Delete"), javax.swing.JOptionPane.YES_NO_OPTION);
        if (choice != javax.swing.JOptionPane.YES_OPTION) {
            return;
        }
        if (ref.kind() == TreeRef.Kind.WINDOW) {
            SkinWindow window = studio.session().index().findWindow(ref.id());
            if (window == null) {
                return;
            }
            if (studio.session().theme().getWindows().size() <= 1) {
                studio.error(Messages.get("APP_STRUCT_LAST_WINDOW", "A skin needs at least one window."));
                return;
            }
            studio.session().apply(new RemoveNodeCommand<>(studio.session().theme().getWindows(), window,
                    "Delete window"));
        } else if (ref.kind() == TreeRef.Kind.LAYOUT) {
            SkinLayout layout = studio.session().index().findAnyLayout(ref.id());
            SkinWindow window = layout == null ? null : studio.session().index().windowOf(layout);
            if (window == null) {
                return;
            }
            if (window.getLayouts().size() <= 1) {
                studio.error(Messages.get("APP_STRUCT_LAST_LAYOUT", "A window needs at least one layout."));
                return;
            }
            studio.session().apply(new RemoveNodeCommand<>(window.getLayouts(), layout, "Delete layout"));
        }
        studio.session().fireChanged();
    }
}
