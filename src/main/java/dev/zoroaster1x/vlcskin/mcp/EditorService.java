package dev.zoroaster1x.vlcskin.mcp;

import dev.zoroaster1x.vlcskin.action.ActionCatalog;
import dev.zoroaster1x.vlcskin.describe.LayoutDescription;
import dev.zoroaster1x.vlcskin.describe.LayoutDescriber;
import dev.zoroaster1x.vlcskin.edit.DeepCopy;
import dev.zoroaster1x.vlcskin.edit.EditorSession;
import dev.zoroaster1x.vlcskin.edit.ItemFactory;
import dev.zoroaster1x.vlcskin.edit.ValueCommand;
import dev.zoroaster1x.vlcskin.edit.commands.AddNodeCommand;
import dev.zoroaster1x.vlcskin.edit.commands.RemoveNodeCommand;
import dev.zoroaster1x.vlcskin.example.ExampleSkins;
import dev.zoroaster1x.vlcskin.format.ParseIssue;
import dev.zoroaster1x.vlcskin.format.SkinParser;
import dev.zoroaster1x.vlcskin.format.SkinValidator;
import dev.zoroaster1x.vlcskin.format.VltCodec;
import dev.zoroaster1x.vlcskin.model.ItemType;
import dev.zoroaster1x.vlcskin.model.SkinIndex;
import dev.zoroaster1x.vlcskin.model.SkinLayout;
import dev.zoroaster1x.vlcskin.model.SkinTheme;
import dev.zoroaster1x.vlcskin.model.SkinWindow;
import dev.zoroaster1x.vlcskin.model.item.AbstractItem;
import dev.zoroaster1x.vlcskin.model.item.GroupItem;
import dev.zoroaster1x.vlcskin.model.item.Item;
import dev.zoroaster1x.vlcskin.model.item.PanelItem;
import dev.zoroaster1x.vlcskin.model.item.PlaytreeItem;
import dev.zoroaster1x.vlcskin.model.item.SliderItem;
import dev.zoroaster1x.vlcskin.model.resource.BitmapFontResource;
import dev.zoroaster1x.vlcskin.model.resource.BitmapResource;
import dev.zoroaster1x.vlcskin.model.resource.FontResource;
import dev.zoroaster1x.vlcskin.model.resource.IniFileResource;
import dev.zoroaster1x.vlcskin.model.resource.PopupMenuResource;
import dev.zoroaster1x.vlcskin.model.resource.Resource;
import dev.zoroaster1x.vlcskin.model.resource.SubBitmap;
import dev.zoroaster1x.vlcskin.snapshot.PreviewSnapshot;
import dev.zoroaster1x.vlcskin.snapshot.UiInspector;
import dev.zoroaster1x.vlcskin.util.Json;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.function.Consumer;

/**
 * Everything an AI or a script may do with the open document. All entry points
 * are synchronized so the desktop UI and an MCP thread can share one session.
 */
public final class EditorService {

    private EditorSession session;
    private UiInspector ui;
    private volatile java.util.function.Consumer<Runnable> dispatcher = Runnable::run;

    public EditorService(EditorSession session) {
        this.session = session;
        session.setDispatcher(dispatcher);
    }

    public EditorService() {
        this(EditorSession.empty());
    }

    /**
     * How session change notifications reach the UI. The desktop app passes a
     * dispatcher that marshals onto the event thread; the default runs inline.
     */
    public synchronized void setDispatcher(java.util.function.Consumer<Runnable> dispatcher) {
        this.dispatcher = dispatcher == null ? Runnable::run : dispatcher;
        session.setDispatcher(this.dispatcher);
    }

    public synchronized void setUi(UiInspector inspector) {
        this.ui = inspector;
    }

    public synchronized UiInspector ui() {
        return ui;
    }

    public synchronized EditorSession session() {
        return session;
    }

    public synchronized void useSession(EditorSession newSession) {
        this.session = newSession;
        newSession.setDispatcher(dispatcher);
    }


    public synchronized ToolOutcome open(String path) {
        try {
            session = EditorSession.open(Path.of(path));
            session.setDispatcher(dispatcher);
            return ToolOutcome.text("Opened " + path, documentInfo());
        } catch (IOException ex) {
            return ToolOutcome.error(ex.getMessage());
        }
    }

    public synchronized ToolOutcome newSkin(String name) {
        SkinTheme theme = new SkinTheme();
        if (name != null && !name.isBlank()) {
            theme.getThemeInfo().setName(name);
        }
        SkinWindow window = new SkinWindow();
        window.setId("main");
        SkinLayout layout = new SkinLayout();
        layout.setId("main");
        layout.setWidth(320);
        layout.setHeight(140);
        window.getLayouts().add(layout);
        theme.getWindows().add(window);
        session = EditorSession.of(theme, null);
        session.setDispatcher(dispatcher);
        return ToolOutcome.text("Started a new skin", documentInfo());
    }

    public synchronized ToolOutcome save(String path) {
        try {
            if (path != null && !path.isBlank()) {
                session.saveAs(Path.of(path));
            } else {
                session.save();
            }
            return ToolOutcome.text("Saved " + (path == null ? session.file() : path));
        } catch (IOException ex) {
            return ToolOutcome.error(ex.getMessage());
        }
    }

    public synchronized ToolOutcome exportVlt(String path) {
        try {
            VltCodec.write(session.theme(), session.file(), Path.of(path));
            return ToolOutcome.text("Exported " + path);
        } catch (IOException ex) {
            return ToolOutcome.error(ex.getMessage());
        }
    }

    public synchronized ToolOutcome importVlt(String archive, String targetFolder) {
        try {
            Path folder;
            if (targetFolder == null || targetFolder.isBlank()) {
                String base = Path.of(archive).getFileName().toString().replaceAll("\\.(vlt|zip|gz|tar)$", "");
                folder = Path.of(archive).toAbsolutePath().getParent().resolve(base);
            } else {
                folder = Path.of(targetFolder);
            }
            Path themeFile = VltCodec.unpack(Path.of(archive), folder);
            session = EditorSession.open(themeFile);
            session.setDispatcher(dispatcher);
            return ToolOutcome.text("Imported " + archive + " into " + folder, documentInfo());
        } catch (IOException ex) {
            return ToolOutcome.error(ex.getMessage());
        }
    }

    public synchronized Map<String, Object> documentInfo() {
        SkinTheme theme = session.theme();
        Map<String, Object> info = new LinkedHashMap<>();
        info.put("file", session.file() == null ? null : session.file().toString());
        info.put("name", theme.getThemeInfo().getName());
        info.put("author", theme.getThemeInfo().getAuthor());
        info.put("version", theme.getVersion());
        info.put("magnet", theme.getMagnet());
        info.put("alpha", theme.getAlpha());
        info.put("movealpha", theme.getMovealpha());
        info.put("dirty", session.isDirty());
        info.put("resources", theme.getResources().size());
        info.put("windows", theme.getWindows().size());
        int layouts = theme.getWindows().stream().mapToInt(window -> window.getLayouts().size()).sum();
        info.put("layouts", layouts);
        info.put("items", session.index().allItems().size());
        info.put("layoutsDetail", layoutSummaries());
        info.put("resourcesDetail", resourceSummaries());
        List<String> issues = new ArrayList<>();
        session.issues().forEach(issue -> issues.add(issue.toString()));
        SkinValidator.validate(theme, session.file() == null ? null : session.file().getParent())
                .forEach(issue -> issues.add(issue.toString()));
        info.put("problems", issues);
        return info;
    }

    private List<Map<String, Object>> layoutSummaries() {
        List<Map<String, Object>> summaries = new ArrayList<>();
        for (SkinWindow window : session.theme().getWindows()) {
            for (SkinLayout layout : window.getLayouts()) {
                Map<String, Object> summary = new LinkedHashMap<>();
                summary.put("window", window.getId());
                summary.put("layout", layout.getId());
                summary.put("width", layout.getWidth());
                summary.put("height", layout.getHeight());
                summary.put("items", layout.getItems().size());
                summaries.add(summary);
            }
        }
        return summaries;
    }

    private List<Map<String, Object>> resourceSummaries() {
        List<Map<String, Object>> summaries = new ArrayList<>();
        for (Resource resource : session.theme().getResources()) {
            Map<String, Object> summary = new LinkedHashMap<>();
            summary.put("id", resource.getId());
            summary.put("type", resource.typeName());
            if (resource instanceof BitmapResource bitmap) {
                summary.put("file", bitmap.getFile());
                summary.put("subBitmaps", bitmap.getSubBitmaps().size());
            }
            summaries.add(summary);
        }
        return summaries;
    }


    public synchronized ToolOutcome layoutTree(String windowId, String layoutId, Integer zoom) {
        SkinLayout layout = resolveLayout(windowId, layoutId);
        if (layout == null) {
            return ToolOutcome.error("No layout selected");
        }
        SkinWindow window = session.index().windowOf(layout);
        LayoutDescription description = LayoutDescriber.describe(window, layout, session.index(), session.images(),
                session.variables(), zoom == null ? 1 : zoom);
        return ToolOutcome.text("Layout " + layout.getId() + " with " + description.items().size() + " items",
                description);
    }

    public synchronized ToolOutcome renderLayout(String windowId, String layoutId, Integer zoom, String selectionId) {
        SkinLayout layout = resolveLayout(windowId, layoutId);
        if (layout == null) {
            return ToolOutcome.error("No layout selected");
        }
        try {
            SkinWindow window = session.index().windowOf(layout);
            var options = session.renderOptions().withZoom(zoom == null ? 1 : zoom);
            if (selectionId != null) {
                Item item = session.index().findItem(selectionId);
                options = options.withSelection(item);
            }
            PreviewSnapshot.Result result = PreviewSnapshot.capture(session.index(), session.images(), window, layout,
                    options, session.variables());
            String text = "Rendered layout " + layout.getId() + " at " + (zoom == null ? 1 : zoom) + "x";
            return ToolOutcome.image(result.png(), text, result.description());
        } catch (IOException | RuntimeException ex) {
            if (System.getProperty("vlcskin.debug") != null) {
                ex.printStackTrace();
            }
            String message = ex.getMessage() == null ? ex.toString() : ex.getMessage();
            return ToolOutcome.error("Could not render: " + message);
        }
    }

    public synchronized ToolOutcome listItems(String type, String windowId, String layoutId) {
        List<Map<String, Object>> items = new ArrayList<>();
        ItemType wanted = type == null || type.isBlank() ? null : ItemType.valueOf(type.toUpperCase());
        for (Item item : session.index().allItems()) {
            if (wanted != null && item.type() != wanted) {
                continue;
            }
            Map<String, Object> entry = new LinkedHashMap<>();
            entry.put("id", item.getId());
            entry.put("type", item.type().displayName());
            entry.put("x", item.getX());
            entry.put("y", item.getY());
            items.add(entry);
        }
        return ToolOutcome.text(items.size() + " items", Map.of("items", items));
    }

    public synchronized ToolOutcome getItem(String id) {
        Item item = session.index().findItem(id);
        if (item == null) {
            return ToolOutcome.error("No item with id \"" + id + "\"");
        }
        Map<String, Object> entry = new LinkedHashMap<>();
        entry.put("id", item.getId());
        entry.put("type", item.type().displayName());
        if (item instanceof AbstractItem abstractItem) {
            Map<String, String> attributes = new LinkedHashMap<>();
            for (String name : PropertyAccess.itemNames(abstractItem)) {
                attributes.put(name, PropertyAccess.getItem(abstractItem, name));
            }
            entry.put("attributes", attributes);
        }
        return ToolOutcome.text(Json.write(entry), entry);
    }


    public synchronized ToolOutcome addItem(String type, String windowId, String layoutId, String parentId, Integer x,
                                            Integer y, Map<String, String> properties) {
        ItemType itemType;
        try {
            itemType = ItemType.valueOf(type.replaceAll("([a-z0-9])([A-Z])", "$1_$2")
                    .toUpperCase(Locale.ROOT)
                    .replace(" ", "_"));
        } catch (IllegalArgumentException ex) {
            return ToolOutcome.error("Unknown item type \"" + type + "\"");
        }
        SkinLayout layout = resolveLayout(windowId, layoutId);
        if (layout == null) {
            return ToolOutcome.error("No layout selected");
        }
        List<Item> target = layout.getItems();
        if (parentId != null && !parentId.isBlank()) {
            Item parent = session.index().findItem(parentId);
            if (parent == null) {
                return ToolOutcome.error("No item with id \"" + parentId + "\"");
            }
            if (parent instanceof PlaytreeItem playtree && itemType == ItemType.SLIDER) {
                if (playtree.getSlider() != null) {
                    return ToolOutcome.error("Playtree \"" + parentId
                            + "\" already has a slider; a playlist cannot contain more than one");
                }
                AbstractItem slider = ItemFactory.create(ItemType.SLIDER, session.index());
                ((SliderItem) slider).setInPlaytree(true);
                ToolOutcome invalid = applyNewItem(properties, slider, x, y);
                if (invalid != null) {
                    return invalid;
                }
                session.apply(dev.zoroaster1x.vlcskin.edit.ValueCommand
                        .builder("Add Slider")
                        .step(() -> playtree.setSlider((SliderItem) slider),
                                () -> playtree.setSlider(null))
                        .build());
                return ToolOutcome.text("Added playlist slider \"" + slider.getId() + "\"");
            }
            if (parent instanceof SliderItem sliderItem && itemType == ItemType.SLIDER_BACKGROUND) {
                if (sliderItem.getBackground() != null) {
                    return ToolOutcome.error("Slider \"" + parentId + "\" already has a background");
                }
                AbstractItem background = ItemFactory.create(ItemType.SLIDER_BACKGROUND, session.index());
                ToolOutcome invalid = applyNewItem(properties, background, x, y);
                if (invalid != null) {
                    return invalid;
                }
                session.apply(dev.zoroaster1x.vlcskin.edit.ValueCommand
                        .builder("Add slider background")
                        .step(() -> sliderItem.setBackground(
                                        (dev.zoroaster1x.vlcskin.model.item.SliderBackground) background),
                                () -> sliderItem.setBackground(null))
                        .build());
                return ToolOutcome.text("Added slider background \"" + background.getId() + "\"");
            }
            if (!(parent instanceof GroupItem || parent instanceof PanelItem)) {
                return ToolOutcome.error("Item \"" + parentId + "\" cannot contain children");
            }
            target = parent.children();
        }
        AbstractItem item = ItemFactory.create(itemType, session.index());
        ToolOutcome invalid = applyNewItem(properties, item, x, y);
        if (invalid != null) {
            return invalid;
        }
        if (itemType == ItemType.SLIDER_BACKGROUND) {
            return ToolOutcome.error("A slider background belongs to a slider; pass the slider as parent");
        }
        session.apply(new AddNodeCommand<>(target, item, target.size(), "Add " + item.type().displayName()));
        return ToolOutcome.text("Added " + item.type().displayName() + " \"" + item.getId() + "\"", getItem(item.getId()).structured());
    }

    private ToolOutcome applyNewItem(Map<String, String> properties, AbstractItem item, Integer x, Integer y) {
        if (x != null) {
            item.setX(x);
        }
        if (y != null) {
            item.setY(y);
        }
        if (properties != null) {
            for (Map.Entry<String, String> entry : properties.entrySet()) {
                String previous;
                try {
                    previous = PropertyAccess.getItem(item, entry.getKey());
                } catch (IllegalArgumentException ex) {
                    return ToolOutcome.error(ex.getMessage());
                }
                if (previous == null) {
                    return ToolOutcome.error("Unknown attribute \"" + entry.getKey() + "\" for "
                            + item.type().displayName() + "; known: " + PropertyAccess.itemNames(item));
                }
                PropertyAccess.setItem(item, entry.getKey(), entry.getValue());
            }
        }
        return null;
    }

    public synchronized ToolOutcome deleteItem(String id) {
        Item item = session.index().findItem(id);
        if (item == null) {
            return ToolOutcome.error("No item with id \"" + id + "\"");
        }
        if (item instanceof dev.zoroaster1x.vlcskin.model.item.SliderBackground background) {
            Item parent = session.index().parentItemOf(id);
            if (!(parent instanceof SliderItem slider)) {
                return ToolOutcome.error("The slider background has no slider");
            }
            session.apply(ValueCommand.builder("Delete slider background")
                    .step(() -> slider.setBackground(null), () -> slider.setBackground(background))
                    .build());
            return ToolOutcome.text("Removed the background of slider \"" + slider.getId() + "\"");
        }
        if (item instanceof SliderItem slider
                && session.index().parentItemOf(id) instanceof PlaytreeItem playtree) {
            session.apply(ValueCommand.builder("Delete playlist slider")
                    .step(() -> playtree.setSlider(null), () -> playtree.setSlider(slider))
                    .build());
            return ToolOutcome.text("Removed the slider from playlist \"" + playtree.getId() + "\"");
        }
        List<Item> target = session.index().parentListOf(id);
        if (target == null) {
            return ToolOutcome.error("Item \"" + id + "\" is not in a layout");
        }
        session.apply(new RemoveNodeCommand<>(target, item, "Delete " + item.type().displayName()));
        return ToolOutcome.text("Deleted " + id);
    }

    public synchronized ToolOutcome moveItem(String id, int x, int y) {
        Item item = session.index().findItem(id);
        if (item == null) {
            return ToolOutcome.error("No item with id \"" + id + "\"");
        }
        if (item.getX() == x && item.getY() == y) {
            return ToolOutcome.text("Item " + id + " is already at " + x + "," + y);
        }
        ValueCommand command = ValueCommand.builder("Move " + item.type().displayName())
                .set(item.getX(), x, item::setX)
                .set(item.getY(), y, item::setY)
                .build();
        session.apply(command);
        return ToolOutcome.text("Moved " + id + " to " + x + "," + y);
    }

    public synchronized ToolOutcome setItemProperty(String id, String name, String value) {
        Item item = session.index().findItem(id);
        if (item == null) {
            return ToolOutcome.error("No item with id \"" + id + "\"");
        }
        if (!(item instanceof AbstractItem abstractItem)) {
            return ToolOutcome.error("Item \"" + id + "\" has no editable attributes");
        }
        if ("id".equalsIgnoreCase(name)) {
            String problem = idProblem(id, value);
            if (problem != null) {
                return ToolOutcome.error(problem);
            }
        }
        String previous;
        try {
            previous = PropertyAccess.getItem(abstractItem, name);
        } catch (IllegalArgumentException ex) {
            return ToolOutcome.error(ex.getMessage());
        }
        if (previous == null) {
            return ToolOutcome.error("Unknown attribute \"" + name + "\"; known: "
                    + PropertyAccess.itemNames(abstractItem));
        }
        try {
            AbstractItem target = abstractItem;
            session.apply(ValueCommand.builder("Edit " + target.type().displayName())
                    .step(() -> PropertyAccess.setItem(target, name, value),
                            () -> PropertyAccess.setItem(target, name, previous))
                    .build());
        } catch (NumberFormatException ex) {
            return ToolOutcome.error("Attribute \"" + name + "\" needs a number, got \"" + value + "\"");
        }
        return ToolOutcome.text(id + "." + name + " = " + value + " (was " + previous + ")");
    }

    public synchronized ToolOutcome duplicateItem(String id) {
        return duplicateItem(id, DeepCopy.COPY_PATTERN);
    }

    /**
     * Duplicates an item with a rename pattern such as {@code %oldid%_copy}.
     */
    public synchronized ToolOutcome duplicateItem(String id, String pattern) {
        Item item = session.index().findItem(id);
        if (item == null) {
            return ToolOutcome.error("No item with id \"" + id + "\"");
        }
        if (item instanceof dev.zoroaster1x.vlcskin.model.item.SliderBackground) {
            return ToolOutcome.error("A slider cannot contain more than one background");
        }
        if (item instanceof SliderItem && session.index().parentItemOf(id) instanceof PlaytreeItem) {
            return ToolOutcome.error("A playlist cannot contain more than one slider");
        }
        List<Item> target = session.index().parentListOf(id);
        if (target == null) {
            return ToolOutcome.error("Item \"" + id + "\" is not in a layout");
        }
        Item copy = DeepCopy.item(item, session.index(), pattern);
        session.apply(new AddNodeCommand<>(target, copy, target.indexOf(item) + 1,
                "Duplicate " + item.type().displayName()));
        return ToolOutcome.text("Duplicated as \"" + copy.getId() + "\"");
    }


    public synchronized ToolOutcome addResource(String type, String id, String file, Map<String, String> properties) {
        Resource resource = switch (type.toLowerCase()) {
            case "bitmap" -> {
                BitmapResource bitmap = new BitmapResource();
                bitmap.setFile(file);
                yield bitmap;
            }
            case "font" -> {
                FontResource font = new FontResource();
                font.setFile(file);
                yield font;
            }
            case "bitmapfont" -> {
                BitmapFontResource font = new BitmapFontResource();
                font.setFile(file);
                yield font;
            }
            case "popupmenu" -> new PopupMenuResource();
            case "inifile" -> {
                IniFileResource ini = new IniFileResource();
                ini.setFile(file);
                yield ini;
            }
            default -> null;
        };
        if (resource == null) {
            return ToolOutcome.error("Unknown resource type \"" + type + "\"");
        }
        resource.setId(id == null || id.isBlank() ? session.index().uniqueUnnamed(resource.typeName()) : id);
        if (properties != null) {
            for (Map.Entry<String, String> entry : properties.entrySet()) {
                if (!PropertyAccess.isKnownResourceAttribute(resource, entry.getKey())) {
                    return ToolOutcome.error("Unknown attribute \"" + entry.getKey() + "\" for "
                            + resource.typeName());
                }
                PropertyAccess.setResource(resource, entry.getKey(), entry.getValue());
            }
        }
        session.apply(new AddNodeCommand<>(session.theme().getResources(), resource,
                session.theme().getResources().size(), "Add " + resource.typeName()));
        return ToolOutcome.text("Added " + resource.typeName() + " \"" + resource.getId() + "\"");
    }

    public synchronized ToolOutcome setResourceProperty(String id, String name, String value) {
        Resource resource = session.index().findResource(id);
        if (resource == null) {
            return ToolOutcome.error("No resource with id \"" + id + "\"");
        }
        if ("id".equalsIgnoreCase(name)) {
            String problem = idProblem(id, value);
            if (problem != null) {
                return ToolOutcome.error(problem);
            }
        }
        if (!PropertyAccess.isKnownResourceAttribute(resource, name)) {
            return ToolOutcome.error("Unknown attribute \"" + name + "\" for " + resource.typeName());
        }
        String previous = PropertyAccess.getResource(resource, name);
        try {
            session.apply(ValueCommand.builder("Edit " + resource.typeName())
                    .step(() -> PropertyAccess.setResource(resource, name, value),
                            () -> PropertyAccess.setResource(resource, name, previous))
                    .build());
        } catch (NumberFormatException ex) {
            return ToolOutcome.error("Attribute \"" + name + "\" needs a number, got \"" + value + "\"");
        }
        return ToolOutcome.text(id + "." + name + " = " + value + " (was " + previous + ")");
    }

    public synchronized ToolOutcome addSubBitmap(String bitmapId, String id, Integer x, Integer y, Integer width,
                                                 Integer height) {
        Resource resource = session.index().findResource(bitmapId);
        if (!(resource instanceof BitmapResource bitmap)) {
            return ToolOutcome.error("\"" + bitmapId + "\" is not a bitmap");
        }
        SubBitmap sub = new SubBitmap();
        sub.setId(id == null || id.isBlank() ? session.index().uniqueUnnamed("SubBitmap") : id);
        sub.setX(x == null ? 0 : x);
        sub.setY(y == null ? 0 : y);
        sub.setWidth(width == null ? 16 : width);
        sub.setHeight(height == null ? 16 : height);
        session.apply(new AddNodeCommand<>(bitmap.getSubBitmaps(), sub, bitmap.getSubBitmaps().size(),
                "Add SubBitmap"));
        session.images().invalidate(bitmapId);
        return ToolOutcome.text("Added SubBitmap \"" + sub.getId() + "\" to " + bitmapId);
    }

    public synchronized ToolOutcome deleteSubBitmap(String bitmapId, String subId) {
        Resource resource = session.index().findResource(bitmapId);
        if (!(resource instanceof BitmapResource bitmap)) {
            return ToolOutcome.error("\"" + bitmapId + "\" is not a bitmap");
        }
        SubBitmap sub = bitmap.getSubBitmaps().stream()
                .filter(candidate -> candidate.getId().equals(subId)).findFirst().orElse(null);
        if (sub == null) {
            return ToolOutcome.error("Bitmap \"" + bitmapId + "\" has no sub bitmap \"" + subId + "\"");
        }
        if (session.index().isResourceUsed(subId)) {
            return ToolOutcome.error("SubBitmap \"" + subId + "\" is still used by an item");
        }
        session.apply(new RemoveNodeCommand<>(bitmap.getSubBitmaps(), sub, "Delete SubBitmap"));
        session.images().invalidate(bitmapId);
        return ToolOutcome.text("Deleted SubBitmap " + subId);
    }

    public synchronized ToolOutcome setSubBitmapProperty(String bitmapId, String subId, String name, String value) {
        Resource resource = session.index().findResource(bitmapId);
        if (!(resource instanceof BitmapResource bitmap)) {
            return ToolOutcome.error("\"" + bitmapId + "\" is not a bitmap");
        }
        SubBitmap sub = bitmap.getSubBitmaps().stream()
                .filter(candidate -> candidate.getId().equals(subId)).findFirst().orElse(null);
        if (sub == null) {
            return ToolOutcome.error("Bitmap \"" + bitmapId + "\" has no sub bitmap \"" + subId + "\"");
        }
        String previous = PropertyAccess.setSubBitmap(sub, name, previousOf(sub, name));
        if (previous == null) {
            return ToolOutcome.error("Unknown attribute \"" + name + "\" for SubBitmap");
        }
        session.apply(ValueCommand.builder("Edit SubBitmap")
                .step(() -> PropertyAccess.setSubBitmap(sub, name, value),
                        () -> PropertyAccess.setSubBitmap(sub, name, previous))
                .build());
        session.images().invalidate(subId);
        return ToolOutcome.text(subId + "." + name + " = " + value);
    }

    private String previousOf(SubBitmap sub, String name) {
        return switch (name.toLowerCase()) {
            case "id" -> sub.getId();
            case "x" -> Integer.toString(sub.getX());
            case "y" -> Integer.toString(sub.getY());
            case "width" -> Integer.toString(sub.getWidth());
            case "height" -> Integer.toString(sub.getHeight());
            case "nbframes" -> Integer.toString(sub.getNbframes());
            case "fps" -> Integer.toString(sub.getFps());
            default -> "";
        };
    }

    public synchronized ToolOutcome deleteResource(String id) {
        Resource resource = session.index().findResource(id);
        if (resource == null) {
            return ToolOutcome.error("No resource with id \"" + id + "\"");
        }
        if (session.index().isResourceUsed(id)) {
            return ToolOutcome.error("Resource \"" + id + "\" is still used by an item");
        }
        session.apply(new RemoveNodeCommand<>(session.theme().getResources(), resource,
                "Delete " + resource.typeName()));
        return ToolOutcome.text("Deleted resource " + id);
    }

    public synchronized ToolOutcome addBitmapFromFile(String file, String id) {
        Path source = Path.of(file);
        if (!Files.isRegularFile(source)) {
            return ToolOutcome.error("No file at " + file);
        }
        Path folder = session.file() == null ? source.toAbsolutePath().getParent()
                : session.file().getParent();
        String relative = folder.relativize(source.toAbsolutePath()).toString().replace('\\', '/');
        BitmapResource bitmap = new BitmapResource();
        bitmap.setId(id == null || id.isBlank()
                ? session.index().uniqueUnnamed(source.getFileName().toString().replaceAll("\\.[^.]+$", ""))
                : id);
        bitmap.setFile(relative);
        session.apply(new AddNodeCommand<>(session.theme().getResources(), bitmap,
                session.theme().getResources().size(), "Add Bitmap"));
        return ToolOutcome.text("Added bitmap \"" + bitmap.getId() + "\" from " + relative);
    }


    public synchronized ToolOutcome setThemeProperty(String name, String value) {
        SkinTheme theme = session.theme();
        String key = name.toLowerCase();
        String previous;
        Consumer<String> setter;
        switch (key) {
            case "name" -> {
                previous = theme.getThemeInfo().getName();
                setter = theme.getThemeInfo()::setName;
            }
            case "author" -> {
                previous = theme.getThemeInfo().getAuthor();
                setter = theme.getThemeInfo()::setAuthor;
            }
            case "email" -> {
                previous = theme.getThemeInfo().getEmail();
                setter = theme.getThemeInfo()::setEmail;
            }
            case "webpage" -> {
                previous = theme.getThemeInfo().getWebpage();
                setter = theme.getThemeInfo()::setWebpage;
            }
            case "magnet" -> {
                previous = Integer.toString(theme.getMagnet());
                setter = v -> theme.setMagnet(Integer.parseInt(v));
            }
            case "alpha" -> {
                previous = Integer.toString(theme.getAlpha());
                setter = v -> theme.setAlpha(Integer.parseInt(v));
            }
            case "movealpha" -> {
                previous = Integer.toString(theme.getMovealpha());
                setter = v -> theme.setMovealpha(Integer.parseInt(v));
            }
            case "tooltipfont" -> {
                previous = theme.getTooltipfont();
                setter = theme::setTooltipfont;
            }
            default -> {
                return ToolOutcome.error("Unknown theme attribute \"" + name + "\"");
            }
        }
        session.apply(ValueCommand.builder("Edit theme")
                .step(() -> setter.accept(value), () -> setter.accept(previous))
                .build());
        return ToolOutcome.text("theme." + key + " = " + value + " (was " + previous + ")");
    }

    public synchronized ToolOutcome addWindow(String id, Integer width, Integer height) {
        SkinWindow window = new SkinWindow();
        window.setId(id == null || id.isBlank() ? session.index().uniqueUnnamed("Window") : id);
        SkinLayout layout = new SkinLayout();
        layout.setId(session.index().uniqueUnnamed("Layout"));
        layout.setWidth(width == null ? 320 : width);
        layout.setHeight(height == null ? 140 : height);
        window.getLayouts().add(layout);
        session.apply(new AddNodeCommand<>(session.theme().getWindows(), window,
                session.theme().getWindows().size(), "Add Window"));
        return ToolOutcome.text("Added window \"" + window.getId() + "\" with layout \"" + layout.getId() + "\"");
    }

    public synchronized ToolOutcome addLayout(String windowId, String id, Integer width, Integer height) {
        SkinWindow window = session.index().findWindow(windowId);
        if (window == null) {
            return ToolOutcome.error("No window with id \"" + windowId + "\"");
        }
        SkinLayout layout = new SkinLayout();
        layout.setId(id == null || id.isBlank() ? session.index().uniqueUnnamed("Layout") : id);
        layout.setWidth(width == null ? 320 : width);
        layout.setHeight(height == null ? 140 : height);
        session.apply(new AddNodeCommand<>(window.getLayouts(), layout, window.getLayouts().size(), "Add Layout"));
        return ToolOutcome.text("Added layout \"" + layout.getId() + "\"");
    }

    private String idProblem(String oldId, String newId) {
        if (newId == null || newId.isBlank()) {
            return "The id must not be empty";
        }
        if (newId.contains("\"")) {
            return "The id must not contain a quote";
        }
        if (!newId.equals(oldId) && session.index().idExists(newId)) {
            return "The id \"" + newId + "\" is already used";
        }
        return null;
    }

    public synchronized ToolOutcome setWindowProperty(String id, String name, String value) {
        SkinWindow window = session.index().findWindow(id);
        if (window == null) {
            return ToolOutcome.error("No window with id \"" + id + "\"");
        }
        if ("id".equalsIgnoreCase(name)) {
            String problem = idProblem(id, value);
            if (problem != null) {
                return ToolOutcome.error(problem);
            }
        }
        String key = name.toLowerCase();
        String previous;
        Runnable apply;
        switch (key) {
            case "id" -> {
                previous = window.getId();
                apply = () -> window.setId(value);
            }
            case "visible" -> {
                previous = window.getVisible();
                apply = () -> window.setVisible(value);
            }
            case "x" -> {
                previous = Integer.toString(window.getX());
                apply = () -> window.setX(Integer.parseInt(value));
            }
            case "y" -> {
                previous = Integer.toString(window.getY());
                apply = () -> window.setY(Integer.parseInt(value));
            }
            case "dragdrop" -> {
                previous = Boolean.toString(window.isDragdrop());
                apply = () -> window.setDragdrop(Boolean.parseBoolean(value));
            }
            case "playondrop" -> {
                previous = Boolean.toString(window.isPlayondrop());
                apply = () -> window.setPlayondrop(Boolean.parseBoolean(value));
            }
            default -> {
                return ToolOutcome.error("Unknown window attribute \"" + name + "\"");
            }
        }
        String old = previous;
        session.apply(ValueCommand.builder("Edit window")
                .step(apply, () -> setWindowValue(window, key, old))
                .build());
        return ToolOutcome.text(id + "." + key + " = " + value + " (was " + previous + ")");
    }

    private void setWindowValue(SkinWindow window, String key, String value) {
        switch (key) {
            case "id" -> window.setId(value);
            case "visible" -> window.setVisible(value);
            case "x" -> window.setX(Integer.parseInt(value));
            case "y" -> window.setY(Integer.parseInt(value));
            case "dragdrop" -> window.setDragdrop(Boolean.parseBoolean(value));
            case "playondrop" -> window.setPlayondrop(Boolean.parseBoolean(value));
            default -> {
            }
        }
    }

    public synchronized ToolOutcome setLayoutProperty(String windowId, String layoutId, String name, String value) {
        SkinWindow window = session.index().findWindow(windowId);
        SkinLayout layout = window == null ? null : session.index().findLayout(window, layoutId);
        if (layout == null) {
            return ToolOutcome.error("No layout \"" + layoutId + "\" in window \"" + windowId + "\"");
        }
        if ("id".equalsIgnoreCase(name) && session.index().findAnyLayout(value) != null
                && !value.equals(layoutId)) {
            return ToolOutcome.error("The layout id \"" + value + "\" is already used");
        }
        String key = name.toLowerCase();
        String previous;
        switch (key) {
            case "id" -> previous = layout.getId();
            case "width" -> previous = Integer.toString(layout.getWidth());
            case "height" -> previous = Integer.toString(layout.getHeight());
            case "minwidth" -> previous = Integer.toString(layout.getMinwidth());
            case "maxwidth" -> previous = Integer.toString(layout.getMaxwidth());
            case "minheight" -> previous = Integer.toString(layout.getMinheight());
            case "maxheight" -> previous = Integer.toString(layout.getMaxheight());
            default -> {
                return ToolOutcome.error("Unknown layout attribute \"" + name + "\"");
            }
        }
        String old = previous;
        session.apply(ValueCommand.builder("Edit layout")
                .step(() -> setLayoutValue(layout, key, value), () -> setLayoutValue(layout, key, old))
                .build());
        return ToolOutcome.text(layoutId + "." + key + " = " + value + " (was " + previous + ")");
    }

    private void setLayoutValue(SkinLayout layout, String key, String value) {
        switch (key) {
            case "id" -> layout.setId(value);
            case "width" -> layout.setWidth(Integer.parseInt(value));
            case "height" -> layout.setHeight(Integer.parseInt(value));
            case "minwidth" -> layout.setMinwidth(Integer.parseInt(value));
            case "maxwidth" -> layout.setMaxwidth(Integer.parseInt(value));
            case "minheight" -> layout.setMinheight(Integer.parseInt(value));
            case "maxheight" -> layout.setMaxheight(Integer.parseInt(value));
            default -> {
            }
        }
    }

    public synchronized ToolOutcome deleteWindow(String id) {
        SkinWindow window = session.index().findWindow(id);
        if (window == null) {
            return ToolOutcome.error("No window with id \"" + id + "\"");
        }
        if (session.theme().getWindows().size() <= 1) {
            return ToolOutcome.error("A skin needs at least one window");
        }
        session.apply(new RemoveNodeCommand<>(session.theme().getWindows(), window, "Delete window"));
        return ToolOutcome.text("Deleted window " + id);
    }

    public synchronized ToolOutcome deleteLayout(String windowId, String layoutId) {
        SkinWindow window = session.index().findWindow(windowId);
        SkinLayout layout = window == null ? null : session.index().findLayout(window, layoutId);
        if (layout == null) {
            return ToolOutcome.error("No layout \"" + layoutId + "\" in window \"" + windowId + "\"");
        }
        if (window.getLayouts().size() <= 1) {
            return ToolOutcome.error("A window needs at least one layout");
        }
        session.apply(new RemoveNodeCommand<>(window.getLayouts(), layout, "Delete layout"));
        return ToolOutcome.text("Deleted layout " + layoutId);
    }

    public synchronized ToolOutcome duplicateWindow(String id, String pattern) {
        SkinWindow window = session.index().findWindow(id);
        if (window == null) {
            return ToolOutcome.error("No window with id \"" + id + "\"");
        }
        SkinWindow copy = DeepCopy.window(window, session.index(), pattern);
        session.apply(new AddNodeCommand<>(session.theme().getWindows(), copy,
                session.theme().getWindows().size(), "Duplicate window"));
        return ToolOutcome.text("Duplicated as \"" + copy.getId() + "\"");
    }

    public synchronized ToolOutcome duplicateLayout(String windowId, String layoutId, String pattern) {
        SkinWindow window = session.index().findWindow(windowId);
        SkinLayout layout = window == null ? null : session.index().findLayout(window, layoutId);
        if (layout == null) {
            return ToolOutcome.error("No layout \"" + layoutId + "\" in window \"" + windowId + "\"");
        }
        SkinLayout copy = DeepCopy.layout(layout, session.index(), pattern);
        session.apply(new AddNodeCommand<>(window.getLayouts(), copy,
                window.getLayouts().indexOf(layout) + 1, "Duplicate layout"));
        return ToolOutcome.text("Duplicated as \"" + copy.getId() + "\"");
    }

    public synchronized ToolOutcome validate() {
        List<ParseIssue> issues = SkinValidator.validate(session.theme(),
                session.file() == null ? null : session.file().getParent());
        List<Map<String, Object>> entries = new ArrayList<>();
        StringBuilder text = new StringBuilder();
        for (ParseIssue issue : issues) {
            entries.add(Map.of("severity", issue.severity().name(), "message", issue.message(),
                    "location", issue.location() == null ? "" : issue.location()));
            text.append(issue).append('\n');
        }
        String summary = issues.isEmpty() ? "No problems found" : issues.size() + " problems found";
        String body = issues.isEmpty() ? summary : summary + "\n" + text.toString().strip();
        return ToolOutcome.text(body, Map.of("problems", entries));
    }

    public synchronized ToolOutcome setVariables(Map<String, Boolean> booleans, Map<String, String> texts,
                                                 Float sliderValue) {
        if (booleans != null) {
            booleans.forEach((name, value) -> {
                if (session.variables().booleans().containsKey(name)) {
                    session.variables().setBoolean(name, value);
                }
            });
        }
        if (texts != null) {
            texts.forEach((name, value) -> {
                if (session.variables().texts().containsKey(name)) {
                    session.variables().setText(name, value);
                }
            });
        }
        if (sliderValue != null) {
            session.variables().setSliderValue(sliderValue);
        }
        session.fireChanged();
        return ToolOutcome.text("Preview state updated",
                Map.of("booleans", session.variables().booleans(), "texts", session.variables().texts(),
                        "sliderValue", session.variables().sliderValue()));
    }

    public synchronized ToolOutcome listActions() {
        List<Map<String, String>> actions = new ArrayList<>();
        for (ActionCatalog.Action action : ActionCatalog.actions()) {
            actions.add(Map.of("code", action.code(), "description", action.display(),
                    "kind", action.kind().name()));
        }
        return ToolOutcome.text(actions.size() + " actions", Map.of("actions", actions));
    }

    public synchronized ToolOutcome listExamples() {
        List<Map<String, String>> examples = new ArrayList<>();
        for (ExampleSkins.Example example : ExampleSkins.catalog()) {
            examples.add(Map.of("id", example.id(), "name", example.name(), "description", example.description()));
        }
        return ToolOutcome.text(examples.size() + " examples", Map.of("examples", examples));
    }

    public synchronized ToolOutcome createExample(String exampleId, String folder) {
        ExampleSkins.Example example = ExampleSkins.catalog().stream()
                .filter(candidate -> candidate.id().equals(exampleId))
                .findFirst().orElse(ExampleSkins.NEON);
        try {
            Path target = folder == null || folder.isBlank()
                    ? Files.createTempDirectory("vlc-skin-example")
                    : Path.of(folder);
            Path themeFile = ExampleSkins.create(target, example);
            session = EditorSession.open(themeFile);
            return ToolOutcome.text("Created example \"" + example.name() + "\" in " + target, documentInfo());
        } catch (IOException ex) {
            return ToolOutcome.error(ex.getMessage());
        }
    }

    public synchronized ToolOutcome describeUi() {
        if (ui == null || !ui.available()) {
            return ToolOutcome.text("The desktop window is not running; only the skin document is available",
                    documentInfo());
        }
        return ToolOutcome.text(ui.describeUi());
    }

    public synchronized ToolOutcome screenshotUi() {
        if (ui == null || !ui.available()) {
            return ToolOutcome.error("The desktop window is not running");
        }
        byte[] png = ui.screenshotPng();
        if (png == null) {
            return ToolOutcome.error("The window is not ready to be captured");
        }
        return ToolOutcome.image(png, ui.summary(), null);
    }


    private SkinLayout resolveLayout(String windowId, String layoutId) {
        SkinIndex index = session.index();
        if (windowId != null && !windowId.isBlank()) {
            SkinWindow window = index.findWindow(windowId);
            if (window == null) {
                return null;
            }
            if (layoutId != null && !layoutId.isBlank()) {
                return index.findLayout(window, layoutId);
            }
            return window.getLayouts().isEmpty() ? null : window.getLayouts().get(0);
        }
        return session.currentLayout();
    }
}
