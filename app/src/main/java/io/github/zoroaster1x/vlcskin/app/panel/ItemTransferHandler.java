package io.github.zoroaster1x.vlcskin.app.panel;

import io.github.zoroaster1x.vlcskin.edit.commands.MoveNodeCommand;
import io.github.zoroaster1x.vlcskin.model.item.Item;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.util.List;
import javax.swing.JComponent;
import javax.swing.JTree;
import javax.swing.TransferHandler;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreePath;

/**
 * Drag and drop for the item tree: drop on a container appends to it, drop
 * between rows inserts, and a node can never be dropped into its own subtree.
 */
final class ItemTransferHandler extends TransferHandler {

    private static final DataFlavor ITEM_FLAVOR = new DataFlavor(TreeRef.class, "VLC skin item");

    private final io.github.zoroaster1x.vlcskin.app.Studio studio;

    ItemTransferHandler(AbstractTreePanel panel) {
        this.studio = panel.studio;
    }

    @Override
    protected Transferable createTransferable(JComponent component) {
        TreeRef ref = selectedRef((JTree) component);
        if (ref == null || ref.kind() != TreeRef.Kind.ITEM) {
            return null;
        }
        return new ItemTransferable(ref);
    }

    @Override
    public int getSourceActions(JComponent component) {
        return MOVE;
    }

    @Override
    public boolean canImport(TransferSupport support) {
        if (!support.isDrop() || !support.isDataFlavorSupported(ITEM_FLAVOR)) {
            return false;
        }
        JTree.DropLocation location = (JTree.DropLocation) support.getDropLocation();
        TreePath path = location.getPath();
        if (path == null) {
            return false;
        }
        TreeRef target = refOf(path);
        if (target == null || target.kind() != TreeRef.Kind.ITEM) {
            return false;
        }
        TreeRef source = selectedRef((JTree) support.getComponent());
        if (source == null) {
            try {
                source = (TreeRef) support.getTransferable().getTransferData(ITEM_FLAVOR);
            } catch (Exception ex) {
                return false;
            }
        }
        if (source.id().equals(target.id())) {
            return false;
        }
        if (isForbiddenSource(source.id())) {
            return false;
        }
        Item targetItem = studio.session().index().findItem(target.id());
        if (targetItem == null) {
            return false;
        }
        if (!isAllowedDropTarget(targetItem)) {
            return false;
        }
        return !isDescendant(source.id(), target.id());
    }

    /**
     * A playlist slider and a slider background have a fixed place and must not
     * be dragged around, matching the original editor's restrictions.
     */
    private boolean isForbiddenSource(String id) {
        Item item = studio.session().index().findItem(id);
        if (item instanceof io.github.zoroaster1x.vlcskin.model.item.SliderBackground) {
            return true;
        }
        if (item instanceof io.github.zoroaster1x.vlcskin.model.item.SliderItem slider
                && slider.isInPlaytree()) {
            return true;
        }
        return false;
    }

    /**
     * Only groups and panels take children; leaves accept insertion next to them.
     */
    private boolean isAllowedDropTarget(Item target) {
        return !(target instanceof io.github.zoroaster1x.vlcskin.model.item.SliderItem)
                && !(target instanceof io.github.zoroaster1x.vlcskin.model.item.PlaytreeItem)
                && !(target instanceof io.github.zoroaster1x.vlcskin.model.item.SliderBackground);
    }

    @Override
    public boolean importData(TransferSupport support) {
        if (!canImport(support)) {
            return false;
        }
        TreeRef source;
        try {
            source = (TreeRef) support.getTransferable().getTransferData(ITEM_FLAVOR);
        } catch (UnsupportedFlavorException | java.io.IOException ex) {
            return false;
        }
        JTree.DropLocation location = (JTree.DropLocation) support.getDropLocation();
        TreeRef target = refOf(location.getPath());
        Item sourceItem = studio.session().index().findItem(source.id());
        Item targetItem = studio.session().index().findItem(target.id());
        if (sourceItem == null || targetItem == null) {
            return false;
        }
        List<Item> sourceList = studio.session().index().parentListOf(source.id());
        if (sourceList == null) {
            return false;
        }
        int childIndex = location.getChildIndex();
        boolean insert = childIndex >= 0;
        List<Item> targetList;
        int insertIndex;
        if (!insert && !targetItem.children().isEmpty()) {
            targetList = targetItem.children();
            insertIndex = targetList.size();
        } else {
            targetList = studio.session().index().parentListOf(target.id());
            if (targetList == null) {
                return false;
            }
            insertIndex = insert ? childIndex : targetList.indexOf(targetItem);
        }
        if (targetList == sourceList && insertIndex == sourceList.indexOf(sourceItem)) {
            return false;
        }
        studio.session().apply(new MoveNodeCommand<>(sourceList, targetList, sourceItem, insertIndex,
                "Move " + sourceItem.type().displayName()));
        studio.session().selection().selectItem(source.id());
        studio.session().fireChanged();
        return true;
    }

    private boolean isDescendant(String sourceId, String targetId) {
        Item source = studio.session().index().findItem(sourceId);
        return source != null && contains(source, targetId);
    }

    private boolean contains(Item item, String id) {
        for (Item child : item.children()) {
            if (child.getId().equals(id) || contains(child, id)) {
                return true;
            }
        }
        return false;
    }

    private TreeRef selectedRef(JTree tree) {
        TreePath path = tree.getSelectionModel().getSelectionPath();
        return path == null ? null : refOf(path);
    }

    private TreeRef refOf(TreePath path) {
        if (path == null || !(path.getLastPathComponent() instanceof DefaultMutableTreeNode node)) {
            return null;
        }
        return node.getUserObject() instanceof TreeRef ref ? ref : null;
    }

    private record ItemTransferable(TreeRef ref) implements Transferable {

        @Override
        public DataFlavor[] getTransferDataFlavors() {
            return new DataFlavor[] {ITEM_FLAVOR};
        }

        @Override
        public boolean isDataFlavorSupported(DataFlavor flavor) {
            return ITEM_FLAVOR.equals(flavor);
        }

        @Override
        public Object getTransferData(DataFlavor flavor) {
            return ref;
        }
    }
}
