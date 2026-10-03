package io.github.zoroaster1x.vlcskin.app.panel;

import io.github.zoroaster1x.vlcskin.app.Studio;
import io.github.zoroaster1x.vlcskin.app.i18n.Messages;
import io.github.zoroaster1x.vlcskin.edit.ItemFactory;
import io.github.zoroaster1x.vlcskin.edit.commands.AddNodeCommand;
import io.github.zoroaster1x.vlcskin.edit.commands.RemoveNodeCommand;
import io.github.zoroaster1x.vlcskin.edit.commands.ReorderCommand;
import io.github.zoroaster1x.vlcskin.model.ItemType;
import io.github.zoroaster1x.vlcskin.model.SkinLayout;
import io.github.zoroaster1x.vlcskin.model.item.Item;
import java.util.List;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import javax.swing.tree.DefaultMutableTreeNode;

/**
 * The items tree of the current layout.
 */
public final class ItemsPanel extends AbstractTreePanel {

    public ItemsPanel(Studio studio, java.util.function.BooleanSupplier refreshGuard) {
        super(studio, io.github.zoroaster1x.vlcskin.app.i18n.PanelTitles.items(), refreshGuard);
        toolbar.add(toolButton(Messages.get("WIN_ITEMS_ADD", "Add item"), "add", this::showAddMenu));
        toolbar.add(toolButton(Messages.get("WIN_ITEMS_MOVE_UP", "Move up"), "up", () -> move(-1)));
        toolbar.add(toolButton(Messages.get("WIN_ITEMS_MOVE_DOWN", "Move down"), "down", () -> move(1)));
        toolbar.add(toolButton(Messages.get("WIN_ITEMS_COPY", "Duplicate"), "duplicate", this::duplicate));
        toolbar.add(toolButton(Messages.get("WIN_ITEMS_DELETE", "Delete"), "delete", this::delete));
    }

    @Override
    public void refresh() {
        DefaultMutableTreeNode root = new DefaultMutableTreeNode("root");
        SkinLayout layout = studio.session().selection().layout(studio.session().index());
        if (layout == null) {
            layout = studio.session().currentLayout();
        }
        if (layout != null) {
            for (Item item : layout.getItems()) {
                root.add(node(item));
            }
        }
        model.setRoot(root);
        for (int row = 0; row < tree.getRowCount(); row++) {
            tree.expandRow(row);
        }
        select(TreeRef.Kind.ITEM, studio.session().selection().itemId());
    }

    private DefaultMutableTreeNode node(Item item) {
        DefaultMutableTreeNode node = new DefaultMutableTreeNode(
                new TreeRef(TreeRef.Kind.ITEM, item.getId(), typeName(item.type()) + ": " + item.getId(),
                        item.type().name()));
        for (Item child : item.children()) {
            node.add(node(child));
        }
        return node;
    }

    private static String typeName(ItemType type) {
        return switch (type) {
            case ANCHOR -> Messages.get("ANCHOR", "Anchor");
            case BUTTON -> Messages.get("BUTTON", "Button");
            case CHECKBOX -> Messages.get("CHECKBOX", "Checkbox");
            case GROUP -> Messages.get("GROUP", "Group");
            case IMAGE -> Messages.get("IMAGE", "Image");
            case PANEL -> Messages.get("PANEL", "Panel");
            case PLAYLIST -> Messages.get("APP_TYPE_PLAYLIST", "Playlist");
            case PLAYTREE -> Messages.get("PLAYTREE", "Playtree");
            case RADIAL_SLIDER -> Messages.get("RADIALSLIDER", "Radial slider");
            case SLIDER -> Messages.get("SLIDER", "Slider");
            case SLIDER_BACKGROUND -> Messages.get("SLIDERBG", "SliderBackground");
            case TEXT -> Messages.get("TEXT", "Text");
            case VIDEO -> Messages.get("VIDEO", "Video");
        };
    }

    @Override
    protected void selectionChanged(TreeRef ref) {
        if (ref != null) {
            studio.session().selection().selectItem(ref.id());
            studio.session().fireChanged();
        } else {
            studio.session().selection().clearItem();
        }
    }

    @Override
    protected void editSelected() {
        selectionChanged(selectedRef());
    }

    private void showAddMenu() {
        JPopupMenu menu = new JPopupMenu();
        Item parent = currentParent();
        for (ItemType type : ItemType.values()) {
            if (type == ItemType.SLIDER_BACKGROUND) {
                continue;
            }
            JMenuItem item = new JMenuItem(typeName(type));
            item.addActionListener(e -> addItem(type));
            menu.add(item);
        }
        menu.show(toolbar, 4, toolbar.getHeight());
    }

    private Item currentParent() {
        TreeRef ref = selectedRef();
        if (ref == null) {
            return null;
        }
        Item selected = studio.session().index().findItem(ref.id());
        if (selected != null && !selected.children().isEmpty()) {
            return selected;
        }
        if (selected != null && isContainerType(selected)) {
            return selected;
        }
        return null;
    }

    private boolean isContainerType(Item item) {
        return item.type() == ItemType.GROUP || item.type() == ItemType.PANEL
                || item.type() == ItemType.PLAYTREE || item.type() == ItemType.PLAYLIST;
    }

    private void addItem(ItemType type) {
        SkinLayout layout = studio.session().currentLayout();
        if (layout == null) {
            studio.error(Messages.get("APP_ITEMS_OPEN_LAYOUT", "Open a layout first."));
            return;
        }
        Item selected = studio.session().selection().item(studio.session().index());
        String parentId = selected != null && isContainerType(selected) && type != ItemType.SLIDER ? selected.getId()
                : null;
        if (type == ItemType.SLIDER && selected != null && selected.type() == ItemType.PLAYTREE) {
            var outcome = studio.service().addItem("Slider", null, null, selected.getId(), null, null, null);
            if (outcome.error()) {
                studio.error(outcome.text());
            }
            studio.session().fireChanged();
            return;
        }
        var outcome = studio.service().addItem(type.displayName(), null, null, parentId, 10, 10, null);
        if (outcome.error()) {
            studio.error(outcome.text());
        }
        studio.session().fireChanged();
    }

    private void move(int delta) {
        TreeRef ref = selectedRef();
        if (ref == null) {
            return;
        }
        Item item = studio.session().index().findItem(ref.id());
        List<Item> list = studio.session().index().parentListOf(ref.id());
        if (item == null || list == null) {
            return;
        }
        int from = list.indexOf(item);
        int to = from + delta;
        if (to < 0 || to >= list.size()) {
            return;
        }
        studio.session().apply(new ReorderCommand<>(list, from, to, "Reorder item"));
        studio.session().fireChanged();
    }

    @Override
    protected javax.swing.JPopupMenu buildContextMenu(TreeRef ref) {
        javax.swing.JPopupMenu menu = new javax.swing.JPopupMenu();
        javax.swing.JMenu addMenu = new javax.swing.JMenu(Messages.get("WIN_ITEMS_ADD", "Add item"));
        for (ItemType type : ItemType.values()) {
            if (type == ItemType.SLIDER_BACKGROUND) {
                continue;
            }
            menuItem(addMenu, typeName(type), () -> addItem(type));
        }
        menu.add(addMenu);
        if (ref != null) {
            menu.addSeparator();
            menuItem(menu, Messages.get("WIN_ITEMS_COPY", "Duplicate"), this::duplicate);
            menuItem(menu, Messages.get("WIN_ITEMS_DELETE", "Delete"), this::delete);
            menu.addSeparator();
            menuItem(menu, Messages.get("WIN_ITEMS_MOVE_UP", "Move up"), () -> move(-1));
            menuItem(menu, Messages.get("WIN_ITEMS_MOVE_DOWN", "Move down"), () -> move(1));
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
        var outcome = studio.service().duplicateItem(ref.id(), pattern);
        if (outcome.error()) {
            studio.error(outcome.text());
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
                Messages.get("DEL_CONFIRM_TITLE", "Delete item"), javax.swing.JOptionPane.YES_NO_OPTION);
        if (choice != javax.swing.JOptionPane.YES_OPTION) {
            return;
        }
        var outcome = studio.service().deleteItem(ref.id());
        if (outcome.error()) {
            studio.error(outcome.text());
        }
        studio.session().selection().clearItem();
        studio.session().fireChanged();
    }

    /**
     * Adds an item without a dialog, used by tests and the canvas menu.
     */
    public void addItemDirect(ItemType type, String parentId, int x, int y) {
        var outcome = studio.service().addItem(type.displayName(), null, null, parentId, x, y, null);
        if (outcome.error()) {
            studio.error(outcome.text());
        }
        studio.session().fireChanged();
    }

    /**
     * Removes the selected item without a confirmation; used by tests.
     */
    public void deleteDirect(String id) {
        studio.session().apply(new RemoveNodeCommand<>(
                studio.session().index().parentListOf(id),
                studio.session().index().findItem(id), "Delete item"));
    }

    /**
     * Adds an item at the layout root for tests.
     */
    public void addToRoot(ItemType type) {
        SkinLayout layout = studio.session().currentLayout();
        if (layout == null) {
            return;
        }
        Item item = ItemFactory.create(type, studio.session().index());
        studio.session().apply(new AddNodeCommand<>(layout.getItems(), item, layout.getItems().size(),
                "Add " + type.displayName()));
        studio.session().selection().selectItem(item.getId());
        studio.session().fireChanged();
    }
}
