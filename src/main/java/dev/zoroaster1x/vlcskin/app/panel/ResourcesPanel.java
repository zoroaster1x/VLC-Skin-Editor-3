package dev.zoroaster1x.vlcskin.app.panel;

import dev.zoroaster1x.vlcskin.app.Studio;
import dev.zoroaster1x.vlcskin.app.i18n.Messages;
import dev.zoroaster1x.vlcskin.model.resource.BitmapFontResource;
import dev.zoroaster1x.vlcskin.model.resource.BitmapResource;
import dev.zoroaster1x.vlcskin.model.resource.FontResource;
import dev.zoroaster1x.vlcskin.model.resource.IniFileResource;
import dev.zoroaster1x.vlcskin.model.resource.PopupMenuResource;
import dev.zoroaster1x.vlcskin.model.resource.Resource;
import dev.zoroaster1x.vlcskin.model.resource.SubBitmap;
import java.nio.file.Path;
import java.util.Map;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.tree.DefaultMutableTreeNode;

/**
 * The resources tree: bitmaps with sub bitmaps, fonts and the rest.
 */
public final class ResourcesPanel extends AbstractTreePanel {

    public ResourcesPanel(Studio studio, java.util.function.BooleanSupplier refreshGuard) {
        super(studio, dev.zoroaster1x.vlcskin.app.i18n.PanelTitles.resources(), refreshGuard);
        toolbar.add(toolButton(Messages.get("WIN_RES_ADD_BMP", "Add bitmap"), "image", this::addBitmap));
        toolbar.add(toolButton(Messages.get("WIN_RES_ADD_FONT", "Add font"), "font", this::addFont));
        toolbar.add(toolButton(Messages.get("WIN_RES_COPY", "Duplicate"), "duplicate", this::duplicate));
        toolbar.add(toolButton(Messages.get("WIN_RES_DELETE", "Delete"), "delete", this::delete));
        toolbar.add(toolButton(Messages.get("APP_RES_RELOAD", "Reload image"), "refresh", this::reload));
    }

    @Override
    public void refresh() {
        DefaultMutableTreeNode root = new DefaultMutableTreeNode("root");
        DefaultMutableTreeNode bitmaps = new DefaultMutableTreeNode(Messages.get("WIN_RES_BITMAPS", "Bitmaps"));
        DefaultMutableTreeNode fonts = new DefaultMutableTreeNode(Messages.get("WIN_RES_FONTS", "Fonts"));
        DefaultMutableTreeNode other = new DefaultMutableTreeNode(Messages.get("APP_RES_OTHER", "Other"));
        for (Resource resource : studio.session().theme().getResources()) {
            if (resource instanceof BitmapResource bitmap) {
                DefaultMutableTreeNode node = new DefaultMutableTreeNode(
                        new TreeRef(TreeRef.Kind.RESOURCE, bitmap.getId(),
                                Messages.get("BITMAP", "Bitmap") + ": " + bitmap.getId()));
                for (SubBitmap sub : bitmap.getSubBitmaps()) {
                    node.add(new DefaultMutableTreeNode(new TreeRef(TreeRef.Kind.SUB_BITMAP, sub.getId(),
                            Messages.get("SUBBITMAP", "SubBitmap") + ": " + sub.getId())));
                }
                bitmaps.add(node);
            } else if (resource instanceof FontResource font) {
                fonts.add(new DefaultMutableTreeNode(new TreeRef(TreeRef.Kind.RESOURCE, font.getId(),
                        Messages.get("FONT", "Font") + ": " + font.getId())));
            } else if (resource instanceof BitmapFontResource font) {
                fonts.add(new DefaultMutableTreeNode(new TreeRef(TreeRef.Kind.RESOURCE, font.getId(),
                        Messages.get("APP_RES_BITMAP_FONT", "BitmapFont") + ": " + font.getId())));
            } else if (resource instanceof PopupMenuResource menu) {
                other.add(new DefaultMutableTreeNode(new TreeRef(TreeRef.Kind.RESOURCE, menu.getId(),
                        Messages.get("APP_RES_POPUP_MENU", "PopupMenu") + ": " + menu.getId())));
            } else if (resource instanceof IniFileResource ini) {
                other.add(new DefaultMutableTreeNode(new TreeRef(TreeRef.Kind.RESOURCE, ini.getId(),
                        Messages.get("APP_RES_INI_FILE", "IniFile") + ": " + ini.getId())));
            }
        }
        java.util.Set<String> expanded = expandedKeys();
        root.add(bitmaps);
        root.add(fonts);
        if (other.getChildCount() > 0) {
            root.add(other);
        }
        model.setRoot(root);
        javax.swing.tree.TreePath path = new javax.swing.tree.TreePath(root);
        tree.expandPath(path);
        if (bitmaps.getChildCount() > 0) {
            tree.expandPath(path.pathByAddingChild(bitmaps));
        }
        if (fonts.getChildCount() > 0) {
            tree.expandPath(path.pathByAddingChild(fonts));
        }
        if (other.getChildCount() > 0) {
            tree.expandPath(path.pathByAddingChild(other));
        }
        restoreExpansion(expanded);
        selectResource(studio.session().selection().resourceId());
    }

    @Override
    protected void selectionChanged(TreeRef ref) {
        if (ref != null) {
            studio.session().selection().selectResource(ref.id());
            studio.session().fireChanged();
        }
    }

    @Override
    protected void editSelected() {
        TreeRef ref = selectedRef();
        if (ref != null) {
            studio.session().selection().selectResource(ref.id());
            studio.session().fireChanged();
        }
    }

    private void addBitmap() {
        JFileChooser chooser = studio.chooser(this, Messages.get("WIN_RES_PU_ADD_BMP", "Add bitmaps"),
                new FileNameExtensionFilter(Messages.get("ADD_BMP_FILE_FILTER_DESC", "PNG image (*.png)"), "png"));
        chooser.setMultiSelectionEnabled(true);
        if (chooser.showOpenDialog(this) != JFileChooser.APPROVE_OPTION) {
            return;
        }
        if (studio.session().file() == null) {
            studio.error(Messages.get("APP_RES_SAVE_FIRST_IMAGE",
                    "Save the skin first so image paths can be made relative."));
            return;
        }
        for (java.io.File file : chooser.getSelectedFiles()) {
            var outcome = studio.service().addBitmapFromFile(file.toPath().toAbsolutePath().toString(), null);
            if (outcome.error()) {
                studio.error(outcome.text());
                return;
            }
        }
        studio.session().fireChanged();
    }

    private void addFont() {
        JFileChooser chooser = studio.chooser(this, Messages.get("APP_RES_ADD_FONTS", "Add fonts"),
                new FileNameExtensionFilter(Messages.get("ADD_FONT_FILE_FILTER_DESC", "Fonts (*.ttf, *.otf)"),
                        "ttf", "otf"));
        chooser.setMultiSelectionEnabled(true);
        if (chooser.showOpenDialog(this) != JFileChooser.APPROVE_OPTION) {
            return;
        }
        if (studio.session().file() == null) {
            studio.error(Messages.get("APP_RES_SAVE_FIRST_FONT",
                    "Save the skin first so font paths can be made relative."));
            return;
        }
        Path folder = studio.session().file().toAbsolutePath().getParent();
        for (java.io.File file : chooser.getSelectedFiles()) {
            Path path = file.toPath();
            String relative = folder.relativize(path.toAbsolutePath()).toString().replace('\\', '/');
            String id = studio.session().index().uniqueUnnamed(
                    path.getFileName().toString().replaceAll("\\.[^.]+$", ""));
            var outcome = studio.service().addResource("font", id, relative, Map.of());
            if (outcome.error()) {
                studio.error(outcome.text());
                return;
            }
        }
        studio.session().fireChanged();
    }

    @Override
    protected javax.swing.JPopupMenu buildContextMenu(TreeRef ref) {
        javax.swing.JPopupMenu menu = new javax.swing.JPopupMenu();
        menuItem(menu, Messages.get("WIN_RES_PU_ADD_BMP", "Add bitmap..."), this::addBitmap);
        menuItem(menu, Messages.get("WIN_RES_ADD_FONT", "Add font..."), this::addFont);
        if (ref != null) {
            menu.addSeparator();
            if (ref.kind() == TreeRef.Kind.RESOURCE) {
                menuItem(menu, Messages.get("APP_RES_ADD_SBMP", "Add sub bitmap"), this::addSubBitmapToSelection);
            }
            menuItem(menu, Messages.get("WIN_RES_COPY", "Duplicate"), this::duplicate);
            menuItem(menu, Messages.get("WIN_RES_DELETE", "Delete"), this::delete);
            menu.addSeparator();
            menuItem(menu, Messages.get("APP_RES_RELOAD", "Reload images"), this::reload);
        }
        return menu;
    }

    private void addSubBitmapToSelection() {
        TreeRef ref = selectedRef();
        Resource resource = ref == null ? null : studio.session().index().findResource(ref.id());
        if (!(resource instanceof BitmapResource bitmap)) {
            studio.error(Messages.get("ERROR_ADD_SBMP_NOBMP", "Select a bitmap first."));
            return;
        }
        SubBitmap sub = new SubBitmap();
        sub.setId(studio.session().index().uniqueUnnamed("SubBitmap"));
        sub.setX(0);
        sub.setY(0);
        sub.setWidth(16);
        sub.setHeight(16);
        studio.session().apply(new dev.zoroaster1x.vlcskin.edit.commands.AddNodeCommand<>(
                bitmap.getSubBitmaps(), sub, bitmap.getSubBitmaps().size(), "Add SubBitmap"));
        studio.session().images().invalidate(bitmap.getId());
        studio.session().fireChanged();
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
        if (ref.kind() == TreeRef.Kind.SUB_BITMAP) {
            var imageRef = studio.session().index().findImage(ref.id());
            if (imageRef == null || imageRef.sub() == null) {
                return;
            }
            var copy = dev.zoroaster1x.vlcskin.edit.DeepCopy.subBitmap(
                    imageRef.sub(), studio.session().index(), pattern);
            studio.session().apply(new dev.zoroaster1x.vlcskin.edit.commands.AddNodeCommand<>(
                    imageRef.bitmap().getSubBitmaps(), copy, imageRef.bitmap().getSubBitmaps().size(),
                    "Duplicate SubBitmap"));
            studio.session().images().invalidate(imageRef.bitmap().getId());
            return;
        }
        Resource resource = studio.session().index().findResource(ref.id());
        if (resource == null) {
            return;
        }
        Resource copy = dev.zoroaster1x.vlcskin.edit.DeepCopy.resource(resource, studio.session().index(),
                pattern);
        studio.session().apply(new dev.zoroaster1x.vlcskin.edit.commands.AddNodeCommand<>(
                studio.session().theme().getResources(), copy,
                studio.session().theme().getResources().size(), "Duplicate " + resource.typeName()));
    }

    private void delete() {
        TreeRef ref = selectedRef();
        if (ref == null || ref.kind() == TreeRef.Kind.SUB_BITMAP) {
            return;
        }
        int choice = javax.swing.JOptionPane.showConfirmDialog(this,
                Messages.format("DEL_CONFIRM_MSG", "Delete \"%n\"?", ref.id()),
                Messages.get("DEL_CONFIRM_TITLE", "Delete resource"), javax.swing.JOptionPane.YES_NO_OPTION);
        if (choice != javax.swing.JOptionPane.YES_OPTION) {
            return;
        }
        var outcome = studio.service().deleteResource(ref.id());
        if (outcome.error()) {
            studio.error(outcome.text());
        }
        studio.session().fireChanged();
    }

    private void reload() {
        studio.session().images().invalidate();
        studio.session().fireChanged();
        studio.status(Messages.get("APP_RES_IMAGES_RELOADED", "Images reloaded"));
    }
}
