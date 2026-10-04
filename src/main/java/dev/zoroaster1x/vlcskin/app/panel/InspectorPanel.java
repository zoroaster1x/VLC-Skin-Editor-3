package dev.zoroaster1x.vlcskin.app.panel;

import dev.zoroaster1x.vlcskin.app.Studio;
import dev.zoroaster1x.vlcskin.app.i18n.Messages;
import dev.zoroaster1x.vlcskin.app.i18n.TypeNames;
import dev.zoroaster1x.vlcskin.app.inspector.InspectorFields;
import dev.zoroaster1x.vlcskin.model.SkinLayout;
import dev.zoroaster1x.vlcskin.model.SkinWindow;
import dev.zoroaster1x.vlcskin.model.item.AnchorItem;
import dev.zoroaster1x.vlcskin.model.item.AbstractItem;
import dev.zoroaster1x.vlcskin.model.item.ButtonItem;
import dev.zoroaster1x.vlcskin.model.item.CheckboxItem;
import dev.zoroaster1x.vlcskin.model.item.GroupItem;
import dev.zoroaster1x.vlcskin.model.item.ImageItem;
import dev.zoroaster1x.vlcskin.model.item.Item;
import dev.zoroaster1x.vlcskin.model.item.PanelItem;
import dev.zoroaster1x.vlcskin.model.item.PlaytreeItem;
import dev.zoroaster1x.vlcskin.model.item.RadialSliderItem;
import dev.zoroaster1x.vlcskin.model.item.SliderBackground;
import dev.zoroaster1x.vlcskin.model.item.SliderItem;
import dev.zoroaster1x.vlcskin.model.item.TextItem;
import dev.zoroaster1x.vlcskin.model.item.VideoItem;
import dev.zoroaster1x.vlcskin.model.resource.BitmapFontResource;
import dev.zoroaster1x.vlcskin.model.resource.BitmapResource;
import dev.zoroaster1x.vlcskin.model.resource.FontResource;
import dev.zoroaster1x.vlcskin.model.resource.IniFileResource;
import dev.zoroaster1x.vlcskin.model.resource.PopupMenuResource;
import dev.zoroaster1x.vlcskin.model.resource.Resource;
import dev.zoroaster1x.vlcskin.model.resource.SubBitmap;
import java.awt.BorderLayout;
import java.util.List;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import javax.swing.filechooser.FileNameExtensionFilter;

/**
 * Attribute editor for whatever is selected: item, resource, window or layout.
 */
public final class InspectorPanel extends JPanel {

    private final Studio studio;
    private final JPanel header = new JPanel(new BorderLayout());
    private final JLabel title = new JLabel(Messages.get("APP_INSPECTOR_NOTHING", "Nothing selected"));
    private final JPanel form = new FormPanel();
    private String lastKey = "";

    public InspectorPanel(Studio studio) {
        this.studio = studio;
        setLayout(new BorderLayout());
        title.setBorder(javax.swing.BorderFactory.createEmptyBorder(6, 8, 2, 8));
        title.setFont(title.getFont().deriveFont(java.awt.Font.BOLD));
        header.add(title, BorderLayout.CENTER);
        add(header, BorderLayout.NORTH);
        javax.swing.JScrollPane scroll = new javax.swing.JScrollPane(form,
                javax.swing.ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED,
                javax.swing.ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        scroll.setBorder(null);
        scroll.setWheelScrollingEnabled(true);
        scroll.getVerticalScrollBar().setUnitIncrement(16);
        scroll.getVerticalScrollBar().setBlockIncrement(140);
        add(scroll, BorderLayout.CENTER);
    }

    /**
     * The form fills the inspector width, so no horizontal scrollbar and the
     * rows never spill past the panel.
     */
    private static final class FormPanel extends JPanel implements javax.swing.Scrollable {
        @Override
        public java.awt.Dimension getPreferredScrollableViewportSize() {
            return getPreferredSize();
        }

        @Override
        public int getScrollableUnitIncrement(java.awt.Rectangle visible, int orientation, int direction) {
            return 16;
        }

        @Override
        public int getScrollableBlockIncrement(java.awt.Rectangle visible, int orientation, int direction) {
            return Math.max(16, visible.height);
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
     * Rebuilds only when the focused node changed; edits keep the form stable.
     */
    public void refresh() {
        var session = studio.session();
        Item item = session.selection().item(session.index());
        if (item instanceof AbstractItem abstractItem) {
            rebuildIfChanged("item:" + abstractItem.getId(), abstractItem);
            return;
        }
        Resource resource = session.selection().resource(session.index());
        if (resource != null) {
            rebuildIfChanged("resource:" + resource.getId(), resource);
            return;
        }
        dev.zoroaster1x.vlcskin.model.SkinIndex.ImageRef imageRef =
                session.index().findImage(session.selection().resourceId());
        if (imageRef != null && imageRef.sub() != null) {
            rebuildIfChanged("sub:" + imageRef.sub().getId(), imageRef.sub());
            return;
        }
        SkinLayout layout = session.selection().layout(session.index());
        if (layout != null) {
            rebuildIfChanged("layout:" + layout.getId(), layout);
            return;
        }
        SkinWindow window = session.selection().window(session.index());
        if (window != null) {
            rebuildIfChanged("window:" + window.getId(), window);
            return;
        }
        rebuildIfChanged("theme", session.theme());
    }

    public void forceRefresh() {
        lastKey = "";
        refresh();
    }

    private void rebuildIfChanged(String key, Object target) {
        if (key.equals(lastKey)) {
            return;
        }
        lastKey = key;
        form.removeAll();
        InspectorFields fields = new InspectorFields(studio, form);
        title.setText(switch (target) {
            case AbstractItem item -> TypeNames.item(item.type()) + ": " + item.getId();
            case Resource resource -> TypeNames.resource(resource) + ": " + resource.getId();
            case SubBitmap sub -> Messages.get("SUBBITMAP", "SubBitmap") + ": " + sub.getId();
            case SkinLayout layout -> Messages.get("LAYOUT", "Layout") + ": " + layout.getId();
            case SkinWindow window -> Messages.get("WINDOW", "Window") + ": " + window.getId();
            default -> Messages.get("WIN_THEME_TITLE", "Skin settings");
        });
        switch (target) {
            case AbstractItem item -> itemForm(fields, item);
            case BitmapResource bitmap -> bitmapForm(fields, bitmap);
            case FontResource font -> fontForm(fields, font);
            case BitmapFontResource font -> bitmapFontForm(fields, font);
            case IniFileResource ini -> iniForm(fields, ini);
            case Resource resource -> fields.note(Messages.format("APP_INSPECTOR_NO_ATTRS",
                    "%t has no editable attributes here.", TypeNames.resource(resource)));
            case SubBitmap sub -> subForm(fields, sub);
            case SkinLayout layout -> layoutForm(fields, layout);
            case SkinWindow window -> windowForm(fields, window);
            case dev.zoroaster1x.vlcskin.model.SkinTheme theme -> themeForm(fields);
            default -> {
            }
        }
        form.revalidate();
        form.repaint();
    }




    private void itemForm(InspectorFields fields, AbstractItem item) {
        fields.section(Messages.get("WIN_ITEM_GENERAL", "General"));
        fields.row(Messages.get("WIN_ITEM_ID", "ID"), fields.text(item.getId(), value -> setItem(item, "id", value)));
        fields.row(Messages.get("WIN_ITEM_X", "X"), fields.integer(item.getX(), value -> setItem(item, "x", value)));
        fields.row(Messages.get("WIN_ITEM_Y", "Y"), fields.integer(item.getY(), value -> setItem(item, "y", value)));
        fields.row(Messages.get("WIN_ITEM_VISIBLE", "Visible"), fields.text(item.getVisible(),
                value -> setItem(item, "visible", value)));
        fields.note(Messages.get("APP_INSPECTOR_VISIBLE_NOTE",
                "Visible is a boolean expression such as <b>vlc.isPlaying</b> or "
                        + "<b>not vlc.isPaused</b>. Fields marked with * in the original dialogs are required."));
        if (item instanceof AnchorItem anchor) {
            fields.row(Messages.get("WIN_ITEM_LEFTTOP", "Left top anchor"), fields.combo(item.getLefttop(), corners(),
                    value -> setItem(item, "lefttop", value)));
            anchorForm(fields, anchor);
            fields.row("", helpButton("i-anchor.html"));
            return;
        }
        fields.row(Messages.get("WIN_ITEM_LEFTTOP", "Left top anchor"), fields.combo(item.getLefttop(), corners(),
                value -> setItem(item, "lefttop", value)));
        fields.row(Messages.get("WIN_ITEM_RIGHTBOTTOM", "Right bottom anchor"),
                fields.combo(item.getRightbottom(), corners(), value -> setItem(item, "rightbottom", value)));
        fields.row(Messages.get("WIN_ITEM_XKEEPRATIO", "Keep x ratio"),
                fields.bool(item.isXkeepratio(), value -> setItem(item, "xkeepratio", value)));
        fields.row(Messages.get("WIN_ITEM_YKEEPRATIO", "Keep y ratio"),
                fields.bool(item.isYkeepratio(), value -> setItem(item, "ykeepratio", value)));
        fields.row(Messages.get("WIN_ITEM_HELP", "Help"),
                fields.text(item.getHelp(), value -> setItem(item, "help", value)));

        switch (item) {
            case AnchorItem anchor -> anchorForm(fields, anchor);
            case ButtonItem button -> buttonForm(fields, button);
            case CheckboxItem checkbox -> checkboxForm(fields, checkbox);
            case ImageItem image -> imageForm(fields, image);
            case PanelItem panel -> panelForm(fields, panel);
            case PlaytreeItem playtree -> playtreeForm(fields, playtree);
            case RadialSliderItem radial -> radialForm(fields, radial);
            case SliderItem slider -> sliderForm(fields, slider);
            case SliderBackground background -> backgroundForm(fields, background);
            case TextItem text -> textForm(fields, text);
            case VideoItem video -> videoForm(fields, video);
            default -> {
            }
        }
        fields.row("", helpButton(helpPage(item)));
    }

    private String helpPage(AbstractItem item) {
        return switch (item) {
            case AnchorItem ignored -> "i-anchor.html";
            case ButtonItem ignored -> "i-button.html";
            case CheckboxItem ignored -> "i-checkbox.html";
            case GroupItem ignored -> "i-group.html";
            case ImageItem ignored -> "i-image.html";
            case PanelItem ignored -> "i-panel.html";
            case PlaytreeItem ignored -> "i-playtree.html";
            case RadialSliderItem ignored -> "i-slider.html";
            case SliderItem ignored -> "i-slider.html";
            case SliderBackground ignored -> "i-sliderbg.html";
            case TextItem ignored -> "i-text.html";
            case VideoItem ignored -> "i-video.html";
        };
    }

    private JButton helpButton(String page) {
        JButton help = new JButton(Messages.get("BUTTON_HELP", "Help"));
        help.addActionListener(e -> dev.zoroaster1x.vlcskin.app.dialog.DocumentationDialog
                .openTopic(this, helpTopic(page)));
        return help;
    }

    /**
     * Maps a legacy help page to the matching topic in our own handbook.
     */
    private static String helpTopic(String page) {
        String name = page == null ? "" : page.replace(".html", "");
        return switch (name) {
            case "i-anchor", "resizable" -> "handbook-layouts-and-anchors";
            case "i-button", "i-checkbox", "i-image" -> "handbook-buttons-checkboxes-and-images";
            case "i-text", "textvars", "percent" -> "handbook-text-items";
            case "i-slider", "bezier" -> "handbook-sliders";
            case "i-sliderbg", "sbgwizard" -> "handbook-slider-backgrounds";
            case "i-playtree" -> "handbook-playlists-and-playtrees";
            case "res-bitmap", "res-subbitmap" -> "handbook-bitmaps-and-animations";
            case "res-font" -> "handbook-fonts-and-bitmap-fonts";
            case "boolexpr", "layout", "window-theme" -> "handbook-actions-and-variables";
            case "window", "theme" -> "handbook-step-2-new-theme";
            case "basics" -> "handbook-getting-started";
            default -> "handbook-index";
        };
    }

    private void subForm(InspectorFields fields, SubBitmap sub) {
        BitmapResource parent = parentBitmap(sub);
        fields.section(Messages.get("WIN_SBMP_TITLE", "SubBitmap"));
        fields.row(Messages.get("WIN_ITEM_ID", "ID"), fields.text(sub.getId(), value -> setSub(sub, "id", value)));
        fields.row(Messages.get("WIN_ITEM_X", "X"), fields.integer(sub.getX(), value -> setSub(sub, "x", value)));
        fields.row(Messages.get("WIN_ITEM_Y", "Y"), fields.integer(sub.getY(), value -> setSub(sub, "y", value)));
        fields.row(Messages.get("WIN_ITEM_WIDTH", "Width"),
                fields.integer(sub.getWidth(), 1, 10000, value -> setSub(sub, "width", value)));
        fields.row(Messages.get("WIN_ITEM_HEIGHT", "Height"),
                fields.integer(sub.getHeight(), 1, 10000, value -> setSub(sub, "height", value)));
        fields.row(Messages.get("WIN_BITMAP_NBFRAMES", "Frames"),
                fields.integer(sub.getNbframes(), 1, 100, value -> setSub(sub, "nbframes", value)));
        fields.row(Messages.get("WIN_BITMAP_FPS", "Frames per second"),
                fields.integer(sub.getFps(), 0, 240, value -> setSub(sub, "fps", value)));
        fields.row("", new ImagePreview(studio, parent, sub));
        if (parent != null && !subFitsParent(sub, parent)) {
            fields.note(Messages.get("ERROR_OUTSIDE_MSG",
                    "The rectangle is outside the parent bitmap and will be clipped."));
        }
        JButton visual = new JButton(Messages.get("APP_INSPECTOR_EDIT_VISUALLY", "Edit visually..."));
        visual.addActionListener(e -> {
            BitmapResource bitmap = parentBitmap(sub);
            if (bitmap != null) {
                new dev.zoroaster1x.vlcskin.app.dialog.SubBitmapEditorDialog(studio, bitmap, sub).setVisible(true);
            }
        });
        fields.row("", visual);
        JButton delete = new JButton(Messages.get("APP_INSPECTOR_DELETE_SUB", "Delete sub bitmap"));
        delete.addActionListener(e -> {
            BitmapResource bitmap = parentBitmap(sub);
            if (bitmap == null) {
                return;
            }
            int choice = javax.swing.JOptionPane.showConfirmDialog(null,
                    Messages.format("DEL_CONFIRM_MSG", "Delete \"%n\"?", sub.getId()),
                    Messages.get("DEL_CONFIRM_TITLE", "Delete SubBitmap"), javax.swing.JOptionPane.YES_NO_OPTION);
            if (choice != javax.swing.JOptionPane.YES_OPTION) {
                return;
            }
            var outcome = studio.service().deleteSubBitmap(bitmap.getId(), sub.getId());
            if (outcome.error()) {
                studio.error(outcome.text());
            }
            studio.session().selection().selectResource(bitmap.getId());
            studio.session().fireChanged();
        });
        fields.row("", delete);
        fields.row("", helpButton("res-subbitmap.html"));
    }

    private BitmapResource parentBitmap(SubBitmap sub) {
        for (Resource resource : studio.session().theme().getResources()) {
            if (resource instanceof BitmapResource bitmap && bitmap.getSubBitmaps().contains(sub)) {
                return bitmap;
            }
        }
        return null;
    }

    private boolean subFitsParent(SubBitmap sub, BitmapResource parent) {
        var image = studio.session().images().image(studio.session().index(), parent.getId());
        if (image == null) {
            return true;
        }
        return sub.getX() >= 0 && sub.getY() >= 0
                && sub.getX() + sub.getWidth() <= image.getWidth()
                && sub.getY() + sub.getHeight() <= image.getHeight();
    }

    private void setSub(SubBitmap sub, String name, int value) {
        setSub(sub, name, Integer.toString(value));
    }

    private void setSub(SubBitmap sub, String name, String value) {
        BitmapResource bitmap = parentBitmap(sub);
        if (bitmap == null) {
            return;
        }
        var outcome = studio.service().setSubBitmapProperty(bitmap.getId(), sub.getId(), name, value);
        if (outcome.error()) {
            studio.status(outcome.text());
        } else {
            studio.session().images().invalidate(bitmap.getId());
            studio.session().images().invalidate(sub.getId());
            studio.session().fireChanged();
        }
    }

    private void anchorForm(InspectorFields fields, AnchorItem anchor) {
        fields.section(Messages.get("WIN_ANCHOR_TITLE", "Anchor"));
        fields.row(Messages.get("WIN_ANCHOR_POINTS", "Points"),
                fields.points(anchor.getPoints(), value -> setItem(anchor, "points", value)));
        fields.row(Messages.get("WIN_ANCHOR_PRIORITY", "Priority"),
                fields.integer(anchor.getPriority(), value -> setItem(anchor, "priority", value)));
        fields.row(Messages.get("WIN_ANCHOR_RANGE", "Range"),
                fields.integer(anchor.getRange(), 0, 1000, value -> setItem(anchor, "range", value)));
        fields.note(Messages.get("APP_INSPECTOR_ANCHOR_NOTE",
                "The window sticks to this curve while resizing; the highest priority wins."));
    }

    private void buttonForm(InspectorFields fields, ButtonItem button) {
        fields.section(Messages.get("WIN_BUTTON_TITLE", "Button"));
        fields.row(Messages.get("WIN_BUTTON_UP", "Normal image"), fields.resourceCombo(button.getUp(), false,
                value -> setItem(button, "up", value)));
        fields.row(Messages.get("WIN_BUTTON_OVER", "Hover image"), fields.resourceCombo(button.getOver(), false,
                value -> setItem(button, "over", value)));
        fields.row(Messages.get("WIN_BUTTON_DOWN", "Clicked image"), fields.resourceCombo(button.getDown(), false,
                value -> setItem(button, "down", value)));
        fields.row(Messages.get("WIN_BUTTON_ACTION", "Action"),
                fields.action(button.getAction(), value -> setItem(button, "action", value)));
        fields.row(Messages.get("WIN_ITEM_TOOLTIPTEXT", "Tooltip"), fields.text(button.getTooltiptext(),
                value -> setItem(button, "tooltiptext", value)));
    }

    private void checkboxForm(InspectorFields fields, CheckboxItem checkbox) {
        fields.section(Messages.get("WIN_CHECKBOX_CONDITION", "Condition"));
        fields.row(Messages.get("WIN_CHECKBOX_STATE", "State"),
                fields.text(checkbox.getState(), value -> setItem(checkbox, "state", value)));
        fields.note(Messages.get("APP_INSPECTOR_CHECKBOX_NOTE",
                "For example <b>playlist.isRandom</b>. State 2 is drawn when it is true."));
        fields.section(Messages.get("WIN_CHECKBOX_STATE1", "State 1"));
        fields.row(Messages.get("WIN_CHECKBOX_UP", "Normal"),
                fields.resourceCombo(checkbox.getUp1(), false, value -> setItem(checkbox, "up1", value)));
        fields.row(Messages.get("WIN_CHECKBOX_OVER", "Hover"), fields.resourceCombo(checkbox.getOver1(), false,
                value -> setItem(checkbox, "over1", value)));
        fields.row(Messages.get("WIN_CHECKBOX_DOWN", "Clicked"), fields.resourceCombo(checkbox.getDown1(), false,
                value -> setItem(checkbox, "down1", value)));
        fields.row(Messages.get("WIN_BUTTON_ACTION", "Action"),
                fields.action(checkbox.getAction1(), value -> setItem(checkbox, "action1", value)));
        fields.row(Messages.get("WIN_ITEM_TOOLTIPTEXT", "Tooltip"), fields.text(checkbox.getTooltiptext1(),
                value -> setItem(checkbox, "tooltiptext1", value)));
        fields.section(Messages.get("WIN_CHECKBOX_STATE2", "State 2"));
        fields.row(Messages.get("WIN_CHECKBOX_UP", "Normal"),
                fields.resourceCombo(checkbox.getUp2(), false, value -> setItem(checkbox, "up2", value)));
        fields.row(Messages.get("WIN_CHECKBOX_OVER", "Hover"), fields.resourceCombo(checkbox.getOver2(), false,
                value -> setItem(checkbox, "over2", value)));
        fields.row(Messages.get("WIN_CHECKBOX_DOWN", "Clicked"), fields.resourceCombo(checkbox.getDown2(), false,
                value -> setItem(checkbox, "down2", value)));
        fields.row(Messages.get("WIN_BUTTON_ACTION", "Action"),
                fields.action(checkbox.getAction2(), value -> setItem(checkbox, "action2", value)));
        fields.row(Messages.get("WIN_ITEM_TOOLTIPTEXT", "Tooltip"), fields.text(checkbox.getTooltiptext2(),
                value -> setItem(checkbox, "tooltiptext2", value)));
    }

    private void imageForm(InspectorFields fields, ImageItem image) {
        fields.section(Messages.get("WIN_IMAGE_TITLE", "Image"));
        fields.row(Messages.get("WIN_IMAGE_IMAGE", "Bitmap"),
                fields.resourceCombo(image.getImage(), false, value -> setItem(image, "image", value)));
        fields.row(Messages.get("WIN_IMAGE_RESIZE", "Resize"), fields.combo(image.getResize(),
                List.of("mosaic", "scale", "scale2"),
                value -> setItem(image, "resize", value)));
        // The preset combo and the action chain edit the same attribute, so
        // each updates the other instead of silently overwriting on next view.
        List<String> presets = List.of("none", "move", "resizeE", "resizeS", "resizeSE");
        JTextField actionField = fields.actionText(image.getAction(), value -> setItem(image, "action", value));
        JComboBox<String> actionCombo = fields.combo(image.getAction(), presets, value -> {
            actionField.setText(value);
            setItem(image, "action", value);
        });
        fields.row(Messages.get("WIN_IMAGE_ACTION", "Click action"), actionCombo);
        fields.row(Messages.get("APP_INSPECTOR_ANY_ACTION", "Action chain"), fields.actionPanel(actionField));
        fields.row(Messages.get("WIN_IMAGE_ACTION2", "Double click action"), fields.action(image.getAction2(),
                value -> setItem(image, "action2", value)));
        fields.row(Messages.get("WIN_IMAGE_ART", "Cover art"),
                fields.bool(image.isArt(), value -> setItem(image, "art", value)));
    }

    private void panelForm(InspectorFields fields, PanelItem panel) {
        fields.section(Messages.get("WIN_PANEL_TITLE", "Panel"));
        fields.row(Messages.get("WIN_PANEL_WIDTH", "Width"),
                fields.integer(panel.getWidth(), 0, 10000, value -> setItem(panel, "width", value)));
        fields.row(Messages.get("WIN_PANEL_HEIGHT", "Height"),
                fields.integer(panel.getHeight(), 0, 10000, value -> setItem(panel, "height", value)));
        fields.note(Messages.get("APP_INSPECTOR_PANEL_NOTE",
                "Children move and resize with the panel according to the anchors."));
    }

    private void playtreeForm(InspectorFields fields, PlaytreeItem playtree) {
        fields.section(Messages.get("WIN_PLAYTREE_TITLE", "Playlist"));
        fields.row(Messages.get("WIN_ITEM_WIDTH", "Width"),
                fields.integer(playtree.getWidth(), 0, 10000, value -> setItem(playtree, "width", value)));
        fields.row(Messages.get("WIN_ITEM_HEIGHT", "Height"),
                fields.integer(playtree.getHeight(), 0, 10000, value -> setItem(playtree, "height", value)));
        fields.row(Messages.get("WIN_PLAYTREE_FONT", "Font"),
                fields.resourceCombo(playtree.getFont(), true, value -> setItem(playtree, "font", value)));
        fields.row(Messages.get("WIN_PLAYTREE_BGIMAGE", "Background image"),
                fields.resourceCombo(playtree.getBgimage(), false, value -> setItem(playtree, "bgimage", value)));
        if (!playtree.isPlaylistSyntax()) {
            fields.row(Messages.get("WIN_PLAYTREE_ITEMIMAGE", "Item icon"),
                    fields.resourceCombo(playtree.getItemimage(), false, value -> setItem(playtree, "itemimage", value)));
            fields.row(Messages.get("WIN_PLAYTREE_OPENIMAGE", "Open folder icon"),
                    fields.resourceCombo(playtree.getOpenimage(), false, value -> setItem(playtree, "openimage", value)));
            fields.row(Messages.get("WIN_PLAYTREE_CLOSEDIMAGE", "Closed folder icon"),
                    fields.resourceCombo(playtree.getClosedimage(), false,
                            value -> setItem(playtree, "closedimage", value)));
            fields.row(Messages.get("WIN_PLAYTREE_FLAT", "Flat"),
                    fields.bool(playtree.isFlat(), value -> setItem(playtree, "flat", value)));
        }
        fields.row(Messages.get("WIN_PLAYTREE_FGCOLOR", "Text color"),
                fields.color(playtree.getFgcolor(), value -> setItem(playtree, "fgcolor", value)));
        fields.row(Messages.get("WIN_PLAYTREE_BGCOLOR1", "Background color 1"), fields.color(playtree.getBgcolor1(),
                value -> setItem(playtree, "bgcolor1", value)));
        fields.row(Messages.get("WIN_PLAYTREE_BGCOLOR2", "Background color 2"), fields.color(playtree.getBgcolor2(),
                value -> setItem(playtree, "bgcolor2", value)));
        fields.row(Messages.get("WIN_PLAYTREE_PLAYCOLOR", "Playing color"), fields.color(playtree.getPlaycolor(),
                value -> setItem(playtree, "playcolor", value)));
        fields.row(Messages.get("WIN_PLAYTREE_SELCOLOR", "Selection color"), fields.color(playtree.getSelcolor(),
                value -> setItem(playtree, "selcolor", value)));
        if (playtree.getSlider() != null) {
            JButton editSlider = new JButton(Messages.get("WIN_PLAYTREE_SLIDER", "Edit playlist's slider"));
            editSlider.addActionListener(e -> {
                studio.session().selection().selectItem(playtree.getSlider().getId());
                studio.session().fireChanged();
            });
            fields.row("", editSlider);
        }
    }

    private void radialForm(InspectorFields fields, RadialSliderItem radial) {
        fields.section(Messages.get("RADIALSLIDER", "Radial slider"));
        fields.row(Messages.get("APP_INSPECTOR_SEQUENCE_BITMAP", "Sequence bitmap"),
                fields.resourceCombo(radial.getSequence(), false, value -> setItem(radial, "sequence", value)));
        fields.row(Messages.get("APP_INSPECTOR_IMAGES", "Images"), fields.integer(radial.getNbimages(), 1, 360,
                value -> setItem(radial, "nbimages", value)));
        fields.row(Messages.get("APP_INSPECTOR_MIN_ANGLE", "Minimum angle"), fields.integer(radial.getMinangle(), -360, 360,
                value -> setItem(radial, "minangle", value)));
        fields.row(Messages.get("APP_INSPECTOR_MAX_ANGLE", "Maximum angle"), fields.integer(radial.getMaxangle(), -360, 360,
                value -> setItem(radial, "maxangle", value)));
        fields.row(Messages.get("WIN_SLIDER_VALUE", "Value"),
                fields.text(radial.getValue(), value -> setItem(radial, "value", value)));
        fields.row(Messages.get("WIN_ITEM_TOOLTIPTEXT", "Tooltip"), fields.text(radial.getTooltiptext(),
                value -> setItem(radial, "tooltiptext", value)));
    }

    private void sliderForm(InspectorFields fields, SliderItem slider) {
        fields.section(Messages.get("WIN_SLIDER_TITLE", "Slider"));
        fields.row(Messages.get("WIN_SLIDER_UP", "Thumb image"),
                fields.resourceCombo(slider.getUp(), false, value -> setItem(slider, "up", value)));
        fields.row(Messages.get("WIN_SLIDER_OVER", "Hover image"), fields.resourceCombo(slider.getOver(), false,
                value -> setItem(slider, "over", value)));
        fields.row(Messages.get("WIN_SLIDER_DOWN", "Clicked image"), fields.resourceCombo(slider.getDown(), false,
                value -> setItem(slider, "down", value)));
        fields.row(Messages.get("WIN_SLIDER_POINTS", "Points"),
                fields.points(slider.getPoints(), value -> setItem(slider, "points", value)));
        fields.row(Messages.get("WIN_SLIDER_THICKNESS", "Thickness"), fields.integer(slider.getThickness(), 1, 200,
                value -> setItem(slider, "thickness", value)));
        if (slider.isInPlaytree()) {
            fields.row(Messages.get("WIN_SLIDER_VALUE", "Value"),
                    fields.combo("Playtree scrolling", List.of("Playtree scrolling"), value -> {
                    }));
            fields.note(Messages.get("APP_INSPECTOR_SLIDER_SCROLL_NOTE",
                    "A playlist slider always follows the playlist scroll position."));
        } else {
            fields.row(Messages.get("WIN_SLIDER_VALUE", "Value"),
                    fields.combo(slider.getValue(), List.of("time", "volume", "equalizer.preamp",
                                    "equalizer.band(0)", "equalizer.band(1)", "equalizer.band(2)", "equalizer.band(3)",
                                    "equalizer.band(4)", "equalizer.band(5)", "equalizer.band(6)", "equalizer.band(7)",
                                    "equalizer.band(8)", "equalizer.band(9)"),
                            value -> setItem(slider, "value", value)));
        }
        fields.row(Messages.get("WIN_ITEM_TOOLTIPTEXT", "Tooltip"), fields.text(slider.getTooltiptext(),
                value -> setItem(slider, "tooltiptext", value)));
        JButton pathTool = new JButton(Messages.get("APP_INSPECTOR_EDIT_PATH", "Edit path on canvas"));
        pathTool.addActionListener(e -> studio.status(Messages.get("APP_INSPECTOR_PATH_HINT",
                "Pick the path tool in the toolbar, then drag the yellow points.")));
        fields.row("", pathTool);
        fields.section(Messages.get("WIN_SLIDER_BG", "Slider background"));
        boolean hasBackground = slider.getBackground() != null;
        fields.row(Messages.get("WIN_SLIDER_BG_ENABLE", "Enabled"),
                fields.bool(hasBackground, value -> toggleSliderBackground(slider, value)));
        if (hasBackground) {
            SliderBackground background = slider.getBackground();
            JButton generate = new JButton(Messages.get("WIN_SBG_WIZARD", "Generate background strip..."));
            generate.addActionListener(e -> new dev.zoroaster1x.vlcskin.app.dialog.SliderBackgroundGeneratorDialog(
                    studio, background).setVisible(true));
            fields.row("", generate);
            fields.row(Messages.get("WIN_SBG_IMAGE", "Image"), fields.resourceCombo(background.getImage(), false,
                    value -> setSubItem(background, "image", value)));
            fields.row(Messages.get("WIN_SBG_NBHORIZ", "Horizontal frames"),
                    fields.integer(background.getNbhoriz(), 1, 100,
                            value -> setSubItem(background, "nbhoriz", value)));
            fields.row(Messages.get("WIN_SBG_NBVERT", "Vertical frames"),
                    fields.integer(background.getNbvert(), 1, 100,
                            value -> setSubItem(background, "nbvert", value)));
            fields.row(Messages.get("WIN_SBG_PADHORIZ", "Horizontal padding"),
                    fields.integer(background.getPadhoriz(), 0, 100,
                            value -> setSubItem(background, "padhoriz", value)));
            fields.row(Messages.get("WIN_SBG_PADVERT", "Vertical padding"),
                    fields.integer(background.getPadvert(), 0, 100,
                            value -> setSubItem(background, "padvert", value)));
        }
    }

    private void setSubItem(SliderBackground background, String name, String value) {
        String previous = switch (name) {
            case "image" -> background.getImage();
            case "nbhoriz" -> Integer.toString(background.getNbhoriz());
            case "nbvert" -> Integer.toString(background.getNbvert());
            case "padhoriz" -> Integer.toString(background.getPadhoriz());
            case "padvert" -> Integer.toString(background.getPadvert());
            default -> null;
        };
        if (previous == null) {
            return;
        }
        studio.session().apply(dev.zoroaster1x.vlcskin.edit.ValueCommand
                .builder("Edit slider background")
                .step(() -> applySubItem(background, name, value),
                        () -> applySubItem(background, name, previous))
                .build());
    }

    private void setSubItem(SliderBackground background, String name, int value) {
        setSubItem(background, name, Integer.toString(value));
    }

    private void applySubItem(SliderBackground background, String name, String value) {
        switch (name) {
            case "image" -> background.setImage(value);
            case "nbhoriz" -> background.setNbhoriz(Integer.parseInt(value));
            case "nbvert" -> background.setNbvert(Integer.parseInt(value));
            case "padhoriz" -> background.setPadhoriz(Integer.parseInt(value));
            case "padvert" -> background.setPadvert(Integer.parseInt(value));
            default -> {
            }
        }
    }

    private void toggleSliderBackground(SliderItem slider, boolean enabled) {
        if (enabled && slider.getBackground() == null) {
            SliderBackground background = new SliderBackground();
            background.setId(studio.session().index().uniqueUnnamed("Slider background"));
            background.setImage("none");
            slider.setBackground(background);
        } else if (!enabled) {
            slider.setBackground(null);
        }
        studio.session().fireChanged();
        forceRefresh();
    }

    private void backgroundForm(InspectorFields fields, SliderBackground background) {
        fields.section(Messages.get("WIN_SLIDER_BG", "Slider background"));
        fields.row(Messages.get("WIN_SBG_IMAGE", "Image"), fields.resourceCombo(background.getImage(), false,
                value -> setItem(background, "image", value)));
        fields.row(Messages.get("WIN_SBG_NBHORIZ", "Horizontal frames"),
                fields.integer(background.getNbhoriz(), 1, 100, value -> setItem(background, "nbhoriz", value)));
        fields.row(Messages.get("WIN_SBG_NBVERT", "Vertical frames"),
                fields.integer(background.getNbvert(), 1, 100, value -> setItem(background, "nbvert", value)));
        fields.row(Messages.get("WIN_SBG_PADHORIZ", "Horizontal padding"),
                fields.integer(background.getPadhoriz(), 0, 100, value -> setItem(background, "padhoriz", value)));
        fields.row(Messages.get("WIN_SBG_PADVERT", "Vertical padding"),
                fields.integer(background.getPadvert(), 0, 100, value -> setItem(background, "padvert", value)));
    }

    private void textForm(InspectorFields fields, TextItem text) {
        fields.section(Messages.get("WIN_TEXT_TITLE", "Text"));
        fields.row(Messages.get("WIN_TEXT_TEXT", "Text"),
                fields.text(text.getText(), value -> setItem(text, "text", value)));
        fields.note(Messages.get("APP_INSPECTOR_TEXT_NOTE",
                "Variables such as <b>$T</b> (time), <b>$N</b> (title) and <b>$V</b> (volume) are replaced live."));
        fields.row(Messages.get("WIN_TEXT_FONT", "Font"),
                fields.resourceCombo(text.getFont(), true, value -> setItem(text, "font", value)));
        fields.row(Messages.get("WIN_TEXT_COLOR", "Color"),
                fields.color(text.getColor(), value -> setItem(text, "color", value)));
        fields.row(Messages.get("WIN_ITEM_WIDTH", "Width"),
                fields.integer(text.getWidth(), 0, 10000, value -> setItem(text, "width", value)));
        fields.row(Messages.get("WIN_TEXT_ALIGNMENT", "Alignment"), fields.combo(text.getAlignment(),
                List.of("left", "center", "right"),
                value -> setItem(text, "alignment", value)));
        fields.row(Messages.get("WIN_TEXT_SCROLLING", "Scrolling"), fields.combo(text.getScrolling(),
                List.of("none", "auto", "manual"),
                value -> setItem(text, "scrolling", value)));
    }

    private void videoForm(InspectorFields fields, VideoItem video) {
        fields.section(Messages.get("WIN_VIDEO_TITLE", "Video"));
        fields.row(Messages.get("WIN_ITEM_WIDTH", "Width"),
                fields.integer(video.getWidth(), 0, 10000, value -> setItem(video, "width", value)));
        fields.row(Messages.get("WIN_ITEM_HEIGHT", "Height"),
                fields.integer(video.getHeight(), 0, 10000, value -> setItem(video, "height", value)));
        fields.row(Messages.get("WIN_VIDEO_AUTORESIZE", "Auto resize"),
                fields.bool(video.isAutoresize(), value -> setItem(video, "autoresize", value)));
    }


    private void bitmapForm(InspectorFields fields, BitmapResource bitmap) {
        fields.section(Messages.get("WIN_BITMAP_TITLE", "Bitmap"));
        fields.row(Messages.get("WIN_ITEM_ID", "ID"), fields.text(bitmap.getId(), value -> setResource(bitmap, "id", value)));
        fields.row(Messages.get("WIN_BITMAP_FILE", "File"),
                fields.text(bitmap.getFile(), value -> setResource(bitmap, "file", value)));
        JButton chooseFile = new JButton(Messages.get("WIN_BITMAP_OPEN", "Choose PNG..."));
        chooseFile.addActionListener(e -> chooseResourceFile(bitmap, "png"));
        fields.row("", chooseFile);
        fields.row(Messages.get("WIN_BITMAP_ALPHACOLOR", "Alpha color"), fields.color(bitmap.getAlphacolor(),
                value -> setResource(bitmap, "alphacolor", value)));
        fields.row(Messages.get("WIN_BITMAP_NBFRAMES", "Frames"), fields.integer(bitmap.getNbframes(), 1, 100,
                value -> setResource(bitmap, "nbframes", value)));
        fields.row(Messages.get("WIN_BITMAP_FPS", "Frames per second"), fields.integer(bitmap.getFps(), 0, 240,
                value -> setResource(bitmap, "fps", value)));
        fields.row("", new ImagePreview(studio, bitmap, null));
        if (!bitmap.getSubBitmaps().isEmpty()) {
            fields.section(Messages.get("APP_INSPECTOR_SUB_BITMAPS", "Sub bitmaps"));
            for (SubBitmap sub : bitmap.getSubBitmaps()) {
                JButton edit = new JButton(sub.getId());
                edit.addActionListener(e -> {
                    studio.session().selection().selectResource(sub.getId());
                    studio.settings().setLastDirectory(studio.settings().getLastDirectory());
                    studio.session().fireChanged();
                    new dev.zoroaster1x.vlcskin.app.dialog.SubBitmapEditorDialog(studio, bitmap, sub).show();
                });
                fields.row("", edit);
            }
        }
        JButton addSub = new JButton(Messages.get("APP_RES_ADD_SBMP", "Add sub bitmap"));
        addSub.addActionListener(e -> {
            SubBitmap sub = new SubBitmap();
            sub.setId(studio.session().index().uniqueUnnamed("SubBitmap"));
            sub.setWidth(32);
            sub.setHeight(32);
            studio.session().apply(new dev.zoroaster1x.vlcskin.edit.commands.AddNodeCommand<>(
                    bitmap.getSubBitmaps(), sub, bitmap.getSubBitmaps().size(), "Add SubBitmap"));
            studio.session().images().invalidate(bitmap.getId());
            studio.session().fireChanged();
            forceRefresh();
        });
        fields.row("", addSub);
    }

    private void fontForm(InspectorFields fields, FontResource font) {
        fields.section(Messages.get("WIN_FONT_TITLE", "Font"));
        fields.row(Messages.get("WIN_ITEM_ID", "ID"), fields.text(font.getId(), value -> setResource(font, "id", value)));
        fields.row(Messages.get("WIN_FONT_FILE", "File"),
                fields.text(font.getFile(), value -> setResource(font, "file", value)));
        JButton chooseFile = new JButton(Messages.get("WIN_FONT_OPEN", "Choose TTF or OTF..."));
        chooseFile.addActionListener(e -> chooseResourceFile(font, "ttf", "otf"));
        fields.row("", chooseFile);
        fields.row(Messages.get("WIN_FONT_SIZE", "Size"),
                fields.integer(font.getSize(), 1, 200, value -> setResource(font, "size", value)));
        fields.note(Messages.get("APP_INSPECTOR_FONT_NOTE",
                "TrueType and OpenType with TrueType outlines load directly; a font the JVM cannot "
                        + "read falls back to Sans Serif so the preview keeps rendering."));
        fields.row("", helpButton("res-font.html"));
    }

    private void chooseResourceFile(Resource resource, String... extensions) {
        JFileChooser chooser = studio.chooser(this, Messages.get("APP_INSPECTOR_CHOOSE_FILE", "Choose file"),
                new FileNameExtensionFilter(Messages.get("APP_INSPECTOR_SUPPORTED_FILES", "Supported files"), extensions));
        if (chooser.showOpenDialog(this) != JFileChooser.APPROVE_OPTION) {
            return;
        }
        java.nio.file.Path file = chooser.getSelectedFile().toPath().toAbsolutePath();
        java.nio.file.Path folder = studio.session().file() == null
                ? file.getParent() : studio.session().file().toAbsolutePath().getParent();
        String relative = folder.relativize(file).toString().replace('\\', '/');
        setResource(resource, "file", relative);
    }

    private void bitmapFontForm(InspectorFields fields, BitmapFontResource font) {
        fields.section(Messages.get("APP_INSPECTOR_BITMAP_FONT", "Bitmap font"));
        fields.row(Messages.get("WIN_ITEM_ID", "ID"), fields.text(font.getId(), value -> setResource(font, "id", value)));
        fields.row(Messages.get("WIN_FONT_FILE", "File"),
                fields.text(font.getFile(), value -> setResource(font, "file", value)));
        fields.row(Messages.get("APP_INSPECTOR_TYPE", "Type"),
                fields.text(font.getType(), value -> setResource(font, "type", value)));
    }

    private void iniForm(InspectorFields fields, IniFileResource ini) {
        fields.section(Messages.get("APP_INSPECTOR_INI_FILE", "Ini file"));
        fields.row(Messages.get("WIN_ITEM_ID", "ID"), fields.text(ini.getId(), value -> setResource(ini, "id", value)));
        fields.row(Messages.get("APP_INSPECTOR_FILE", "File"),
                fields.text(ini.getFile(), value -> setResource(ini, "file", value)));
    }


    private void layoutForm(InspectorFields fields, SkinLayout layout) {
        SkinWindow window = studio.session().index().windowOf(layout);
        fields.section(Messages.get("WIN_LAYOUT_TITLE", "Layout"));
        fields.row(Messages.get("WIN_ITEM_ID", "ID"), fields.text(layout.getId(), value -> setLayout(window, layout, "id", value)));
        fields.row(Messages.get("WIN_LAYOUT_WIDTH", "Width"), fields.integer(layout.getWidth(), 1, 10000,
                value -> setLayout(window, layout, "width", value)));
        fields.row(Messages.get("WIN_LAYOUT_HEIGHT", "Height"), fields.integer(layout.getHeight(), 1, 10000,
                value -> setLayout(window, layout, "height", value)));
        fields.row(Messages.get("WIN_LAYOUT_MINWIDTH", "Minimum width"),
                fields.integer(layout.getMinwidth(), -1, 10000, value -> setLayout(window, layout, "minwidth", value)));
        fields.row(Messages.get("WIN_LAYOUT_MAXWIDTH", "Maximum width"),
                fields.integer(layout.getMaxwidth(), -1, 10000, value -> setLayout(window, layout, "maxwidth", value)));
        fields.row(Messages.get("WIN_LAYOUT_MINHEIGHT", "Minimum height"),
                fields.integer(layout.getMinheight(), -1, 10000, value -> setLayout(window, layout, "minheight", value)));
        fields.row(Messages.get("WIN_LAYOUT_MAXHEIGHT", "Maximum height"),
                fields.integer(layout.getMaxheight(), -1, 10000, value -> setLayout(window, layout, "maxheight", value)));
        if (window != null && window.getLayouts().indexOf(layout) == window.getLayouts().size() - 1) {
            fields.note(Messages.get("APP_INSPECTOR_LAST_LAYOUT",
                    "This is the last layout, so it is the window's default size."));
        }
    }

    private void windowForm(InspectorFields fields, SkinWindow window) {
        fields.section(Messages.get("WIN_WINDOW_TITLE", "Window"));
        fields.row(Messages.get("WIN_ITEM_ID", "ID"), fields.text(window.getId(), value -> setWindow(window, "id", value)));
        fields.row(Messages.get("WIN_ITEM_VISIBLE", "Visible"),
                fields.text(window.getVisible(), value -> setWindow(window, "visible", value)));
        fields.row(Messages.get("WIN_ITEM_X", "Initial X"), fields.integer(window.getX(), value -> setWindow(window, "x", value)));
        fields.row(Messages.get("WIN_ITEM_Y", "Initial Y"), fields.integer(window.getY(), value -> setWindow(window, "y", value)));
        fields.row(Messages.get("WIN_WINDOW_DD", "Drag and drop"),
                fields.bool(window.isDragdrop(), value -> setWindow(window, "dragdrop", value)));
        fields.row(Messages.get("WIN_WINDOW_PD", "Play on drop"),
                fields.bool(window.isPlayondrop(), value -> setWindow(window, "playondrop", value)));
    }

    private void themeForm(InspectorFields fields) {
        var theme = studio.session().theme();
        var info = theme.getThemeInfo();
        fields.section(Messages.get("WIN_THEME_INFO_TITLE", "Theme info"));
        fields.row(Messages.get("WIN_THEME_NAME", "Name"), fields.text(info.getName(), value -> setTheme("name", value)));
        fields.row(Messages.get("WIN_THEME_AUTHOR", "Author"), fields.text(info.getAuthor(), value -> setTheme("author", value)));
        fields.row(Messages.get("WIN_THEME_EMAIL", "Email"), fields.text(info.getEmail(), value -> setTheme("email", value)));
        fields.row(Messages.get("WIN_THEME_WEB", "Webpage"), fields.text(info.getWebpage(), value -> setTheme("webpage", value)));
        fields.section(Messages.get("WIN_THEME_ATTR_TITLE", "Theme attributes"));
        fields.row(Messages.get("WIN_THEME_MAGNET", "Magnet"),
                fields.integer(theme.getMagnet(), 0, 500, value -> setTheme("magnet", value)));
        fields.row(Messages.get("WIN_THEME_ALPHA", "Opacity"),
                fields.integer(theme.getAlpha(), 1, 255, value -> setTheme("alpha", value)));
        fields.row(Messages.get("WIN_THEME_MOVEALPHA", "Opacity while moving"),
                fields.integer(theme.getMovealpha(), 1, 255, value -> setTheme("movealpha", value)));
        fields.note(Messages.get("APP_INSPECTOR_SELECT_NOTE",
                "Select a window, layout or item in the trees to edit it."));
    }


    private void setItem(AbstractItem item, String name, String value) {
        var outcome = studio.service().setItemProperty(item.getId(), name, value);
        if (outcome.error()) {
            studio.status(outcome.text());
        } else {
            studio.session().fireChanged();
        }
    }

    private void setItem(AbstractItem item, String name, int value) {
        setItem(item, name, Integer.toString(value));
    }

    private void setItem(AbstractItem item, String name, boolean value) {
        setItem(item, name, Boolean.toString(value));
    }

    private void setResource(Resource resource, String name, String value) {
        var outcome = studio.service().setResourceProperty(resource.getId(), name, value);
        if (outcome.error()) {
            studio.status(outcome.text());
        } else {
            studio.session().images().invalidate(resource.getId());
            studio.session().fireChanged();
        }
    }

    private void setResource(Resource resource, String name, int value) {
        setResource(resource, name, Integer.toString(value));
    }

    private void setWindow(SkinWindow window, String name, String value) {
        var outcome = studio.service().setWindowProperty(window.getId(), name, value);
        if (outcome.error()) {
            studio.status(outcome.text());
        } else {
            studio.session().fireChanged();
        }
    }

    private void setWindow(SkinWindow window, String name, int value) {
        setWindow(window, name, Integer.toString(value));
    }

    private void setWindow(SkinWindow window, String name, boolean value) {
        setWindow(window, name, Boolean.toString(value));
    }

    private void setLayout(SkinWindow window, SkinLayout layout, String name, String value) {
        String windowId = window == null ? "none" : window.getId();
        var outcome = studio.service().setLayoutProperty(windowId, layout.getId(), name, value);
        if (outcome.error()) {
            studio.status(outcome.text());
        } else {
            studio.session().fireChanged();
        }
    }

    private void setLayout(SkinWindow window, SkinLayout layout, String name, int value) {
        setLayout(window, layout, name, Integer.toString(value));
    }

    private void setTheme(String name, String value) {
        var outcome = studio.service().setThemeProperty(name, value);
        if (outcome.error()) {
            studio.status(outcome.text());
        } else {
            studio.session().fireChanged();
        }
    }

    private void setTheme(String name, int value) {
        setTheme(name, Integer.toString(value));
    }

    private static List<String> corners() {
        return List.of("lefttop", "leftbottom", "righttop", "rightbottom");
    }

    /**
     * Focuses the id field when the panel is shown, for quick keyboard flow.
     */
    public void focusIdField() {
        SwingUtilities.invokeLater(() -> {
            if (form.getComponentCount() > 1 && form.getComponent(1) instanceof javax.swing.JTextField field) {
                field.selectAll();
                field.requestFocusInWindow();
            }
        });
    }

    /**
     * A checkerboard backed preview of a bitmap or sub bitmap, scaled to fit,
     * with a caption of the file, size, frames and alphacolor.
     */
    private static final class ImagePreview extends JPanel {
        ImagePreview(Studio studio, BitmapResource bitmap, SubBitmap sub) {
            super(new BorderLayout(0, 4));
            setOpaque(false);
            java.awt.image.BufferedImage decoded = null;
            String caption;
            try {
                if (sub != null && bitmap != null) {
                    decoded = studio.session().images().image(studio.session().index(), sub.getId());
                    caption = bitmap.getId() + " > " + sub.getId() + "   "
                            + sub.getWidth() + "x" + sub.getHeight()
                            + " at " + sub.getX() + "," + sub.getY();
                } else {
                    decoded = studio.session().images().wholeImage(studio.session().index(), bitmap);
                    caption = (decoded == null
                            ? bitmap.getFile()
                            : decoded.getWidth() + "x" + decoded.getHeight() + "   " + bitmap.getFile())
                            + (bitmap.getNbframes() > 1
                                    ? "   " + bitmap.getNbframes() + " frames, " + bitmap.getFps() + " fps"
                                    : "")
                            + "   alphacolor " + bitmap.getAlphacolor();
                }
            } catch (RuntimeException ex) {
                caption = ex.getMessage();
            }
            add(new PreviewSurface(decoded), BorderLayout.CENTER);
            JLabel label = new JLabel(caption == null ? " " : caption);
            label.setFont(label.getFont().deriveFont(10f));
            label.setForeground(javax.swing.UIManager.getColor("Label.disabledForeground"));
            add(label, BorderLayout.SOUTH);
            setPreferredSize(new java.awt.Dimension(300, 210));
            setMaximumSize(new java.awt.Dimension(Integer.MAX_VALUE, 240));
        }
    }

    /**
     * Draws the image over a checkerboard so transparency is visible.
     */
    private static final class PreviewSurface extends JPanel {
        private final java.awt.image.BufferedImage image;

        PreviewSurface(java.awt.image.BufferedImage image) {
            this.image = image;
            setPreferredSize(new java.awt.Dimension(280, 170));
            setOpaque(true);
            setBackground(javax.swing.UIManager.getColor("Panel.background"));
        }

        @Override
        protected void paintComponent(java.awt.Graphics g) {
            super.paintComponent(g);
            java.awt.Graphics2D g2 = (java.awt.Graphics2D) g.create();
            g2.setRenderingHint(java.awt.RenderingHints.KEY_INTERPOLATION,
                    java.awt.RenderingHints.VALUE_INTERPOLATION_BILINEAR);
            int cell = 10;
            for (int y = 0; y < getHeight(); y += cell) {
                for (int x = 0; x < getWidth(); x += cell) {
                    boolean light = ((x / cell) + (y / cell)) % 2 == 0;
                    g2.setColor(light ? new java.awt.Color(0x3A, 0x3C, 0x40)
                            : new java.awt.Color(0x2B, 0x2D, 0x30));
                    g2.fillRect(x, y, cell, cell);
                }
            }
            if (image != null) {
                double scale = Math.min((getWidth() - 8) / (double) image.getWidth(),
                        (getHeight() - 8) / (double) image.getHeight());
                int width = Math.max(1, (int) Math.round(image.getWidth() * scale));
                int height = Math.max(1, (int) Math.round(image.getHeight() * scale));
                int x = (getWidth() - width) / 2;
                int y = (getHeight() - height) / 2;
                g2.drawImage(image, x, y, width, height, null);
                g2.setColor(javax.swing.UIManager.getColor("Component.borderColor"));
                g2.drawRect(x, y, width, height);
            }
            g2.dispose();
        }
    }
}
