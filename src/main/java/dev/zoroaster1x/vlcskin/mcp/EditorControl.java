package dev.zoroaster1x.vlcskin.mcp;

import com.fasterxml.jackson.databind.JsonNode;
import dev.zoroaster1x.vlcskin.Version;
import dev.zoroaster1x.vlcskin.edit.DeepCopy;
import dev.zoroaster1x.vlcskin.edit.EditorSession;
import dev.zoroaster1x.vlcskin.edit.ValueCommand;
import dev.zoroaster1x.vlcskin.edit.commands.MoveNodeCommand;
import dev.zoroaster1x.vlcskin.edit.commands.ReorderCommand;
import dev.zoroaster1x.vlcskin.format.ParseIssue;
import dev.zoroaster1x.vlcskin.format.SkinParser;
import dev.zoroaster1x.vlcskin.model.SkinIndex;
import dev.zoroaster1x.vlcskin.model.SkinLayout;
import dev.zoroaster1x.vlcskin.model.SkinWindow;
import dev.zoroaster1x.vlcskin.model.item.GroupItem;
import dev.zoroaster1x.vlcskin.model.item.Item;
import dev.zoroaster1x.vlcskin.model.item.PanelItem;
import dev.zoroaster1x.vlcskin.model.item.SliderItem;
import dev.zoroaster1x.vlcskin.model.resource.BitmapResource;
import dev.zoroaster1x.vlcskin.model.resource.Resource;
import dev.zoroaster1x.vlcskin.render.SliderBackgroundGenerator;
import dev.zoroaster1x.vlcskin.snapshot.UiInspector;
import dev.zoroaster1x.vlcskin.util.Json;
import dev.zoroaster1x.vlcskin.util.VlcFinder;
import java.awt.image.BufferedImage;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import javax.imageio.ImageIO;

/**
 * The rest of what a user can do in the desktop window, exposed to MCP: undo
 * history, selection, reordering and reparenting, preferences, the XML view,
 * preview export, launching VLC, the slider background generator and the
 * update check.
 */
public final class EditorControl {

    private final EditorService service;

    public EditorControl(EditorService service) {
        this.service = service;
    }

    /**
     * The service this control drives.
     */
    public EditorService service() {
        return service;
    }

    private EditorSession session() {
        return service.session();
    }

    private SkinIndex index() {
        return service.session().index();
    }

    // ------------------------------------------------------------- history

    public ToolOutcome historyState() {
        var history = session().history();
        Map<String, Object> state = new LinkedHashMap<>();
        state.put("canUndo", history.canUndo());
        state.put("undoDescription", history.undoDescription());
        state.put("canRedo", history.canRedo());
        state.put("redoDescription", history.redoDescription());
        state.put("dirty", session().isDirty());
        state.put("revision", session().revision());
        return ToolOutcome.text(Json.write(state), state);
    }

    public ToolOutcome undo() {
        session().undo();
        return ToolOutcome.text("Undone", historyState().structured());
    }

    public ToolOutcome redo() {
        session().redo();
        return ToolOutcome.text("Redone", historyState().structured());
    }

    // ----------------------------------------------------------- selection

    public ToolOutcome select(String kind, String id) {
        String type = kind == null ? "" : kind.toLowerCase(Locale.ROOT);
        switch (type) {
            case "item" -> {
                if (index().findItem(id) == null) {
                    return ToolOutcome.error("No item with id \"" + id + "\"");
                }
                session().selection().selectItem(id);
            }
            case "resource", "bitmap", "subbitmap", "font" -> {
                if (index().findResource(id) == null && index().findImage(id) == null) {
                    return ToolOutcome.error("No resource with id \"" + id + "\"");
                }
                session().selection().selectResource(id);
            }
            case "window" -> {
                if (index().findWindow(id) == null) {
                    return ToolOutcome.error("No window with id \"" + id + "\"");
                }
                session().selection().selectWindow(id);
            }
            case "layout" -> {
                SkinLayout layout = index().findAnyLayout(id);
                if (layout == null) {
                    return ToolOutcome.error("No layout with id \"" + id + "\"");
                }
                SkinWindow window = index().windowOf(layout);
                session().selection().selectLayout(window == null ? null : window.getId(), id);
            }
            default -> {
                return ToolOutcome.error("kind must be item, resource, window or layout");
            }
        }
        session().fireChanged();
        return ToolOutcome.text("Selected " + type + " " + id, selectionState().structured());
    }

    public ToolOutcome selectionState() {
        var selection = session().selection();
        Map<String, Object> state = new LinkedHashMap<>();
        state.put("window", selection.windowId());
        state.put("layout", selection.layoutId());
        state.put("item", selection.itemId());
        state.put("resource", selection.resourceId());
        Item item = selection.item(index());
        if (item != null) {
            state.put("itemType", item.type().displayName());
            state.put("itemX", item.getX());
            state.put("itemY", item.getY());
        }
        return ToolOutcome.text(Json.write(state), state);
    }

    // -------------------------------------------------------------- moving

    public ToolOutcome nudgeItem(String id, int dx, int dy) {
        Item item = index().findItem(id);
        if (item == null) {
            return ToolOutcome.error("No item with id \"" + id + "\"");
        }
        session().nudge(item, dx, dy);
        return ToolOutcome.text(id + " moved by " + dx + "," + dy);
    }

    public ToolOutcome reorderItem(String id, String action) {
        Item item = index().findItem(id);
        if (item == null) {
            return ToolOutcome.error("No item with id \"" + id + "\"");
        }
        List<Item> list = index().parentListOf(id);
        if (list == null) {
            return ToolOutcome.error("Item \"" + id + "\" is not in a layout");
        }
        int from = list.indexOf(item);
        int to = switch (action == null ? "" : action.toLowerCase(Locale.ROOT)) {
            case "up" -> from - 1;
            case "down" -> from + 1;
            case "front", "top" -> list.size() - 1;
            case "back", "bottom" -> 0;
            default -> -1;
        };
        if (to < 0 || to >= list.size() || to == from) {
            return ToolOutcome.text("Item " + id + " is already at the requested position");
        }
        session().apply(new ReorderCommand<>(list, from, to, "Reorder " + item.type().displayName()));
        return ToolOutcome.text("Moved " + id + " to position " + to + " of " + list.size());
    }

    public ToolOutcome reparentItem(String id, String newParentId, Integer index) {
        Item item = index().findItem(id);
        if (item == null) {
            return ToolOutcome.error("No item with id \"" + id + "\"");
        }
        List<Item> source = index().parentListOf(id);
        if (source == null) {
            return ToolOutcome.error("Item \"" + id + "\" is not in a layout");
        }
        List<Item> target;
        if (newParentId == null || newParentId.isBlank()) {
            SkinLayout layout = index().layoutOf(id);
            if (layout == null) {
                return ToolOutcome.error("The owning layout was not found");
            }
            target = layout.getItems();
        } else {
            Item parent = index().findItem(newParentId);
            if (!(parent instanceof GroupItem || parent instanceof PanelItem)) {
                return ToolOutcome.error("Item \"" + newParentId + "\" cannot contain children");
            }
            if (isDescendant(item, newParentId)) {
                return ToolOutcome.error("An item cannot be moved into its own subtree");
            }
            target = parent.children();
        }
        int insert = index == null ? target.size() : Math.max(0, Math.min(index, target.size()));
        session().apply(new MoveNodeCommand<>(source, target, item, insert,
                "Move " + item.type().displayName()));
        session().selection().selectItem(id);
        session().fireChanged();
        return ToolOutcome.text("Moved " + id + " to " + (newParentId == null ? "the layout root" : newParentId));
    }

    private boolean isDescendant(Item item, String candidateId) {
        for (Item child : item.children()) {
            if (child.getId().equals(candidateId) || isDescendant(child, candidateId)) {
                return true;
            }
        }
        return false;
    }

    public ToolOutcome reorderLayout(String windowId, String layoutId, int index) {
        SkinWindow window = index().findWindow(windowId);
        SkinLayout layout = window == null ? null : index().findLayout(window, layoutId);
        if (layout == null) {
            return ToolOutcome.error("No layout \"" + layoutId + "\" in window \"" + windowId + "\"");
        }
        int from = window.getLayouts().indexOf(layout);
        if (index < 0 || index >= window.getLayouts().size()) {
            return ToolOutcome.error("index must be between 0 and " + (window.getLayouts().size() - 1));
        }
        if (from == index) {
            return ToolOutcome.text("The layout is already at position " + index);
        }
        session().apply(new ReorderCommand<>(window.getLayouts(), from, index, "Reorder layouts"));
        return ToolOutcome.text("Moved layout to position " + index);
    }

    // --------------------------------------------------------------- source

    public ToolOutcome getXml() {
        String xml = session().toXml();
        return ToolOutcome.text(xml, Map.of("file", session().file() == null ? "" : session().file().toString(),
                "xml", xml));
    }

    public ToolOutcome applyXml(String xml) {
        if (xml == null || xml.isBlank()) {
            return ToolOutcome.error("The XML is empty");
        }
        var result = SkinParser.parse(xml.getBytes(StandardCharsets.UTF_8),
                session().file() == null ? "untitled.xml" : session().file().toString());
        if (result.hasErrors()) {
            StringBuilder message = new StringBuilder("The XML has errors:\n");
            result.issues().stream()
                    .filter(issue -> issue.severity() == ParseIssue.Severity.ERROR)
                    .limit(10)
                    .forEach(issue -> message.append("  ").append(issue).append('\n'));
            return ToolOutcome.error(message.toString());
        }
        session().replace(result.theme(), session().file(), result.issues());
        return ToolOutcome.text("Applied the XML", service.documentInfo());
    }

    // ------------------------------------------------------------- desktop

    public ToolOutcome reloadImages() {
        session().images().invalidate();
        session().fireChanged();
        return ToolOutcome.text("Images reloaded");
    }

    public ToolOutcome savePreview(String path, String windowId, String layoutId, Integer zoom) {
        if (path == null || path.isBlank()) {
            return ToolOutcome.error("A target PNG path is required");
        }
        ToolOutcome rendered = service.renderLayout(windowId, layoutId, zoom, null);
        if (rendered.error() || !rendered.hasPng()) {
            return rendered;
        }
        try {
            Path target = Path.of(path);
            if (!target.getFileName().toString().toLowerCase(Locale.ROOT).endsWith(".png")) {
                target = target.resolveSibling(target.getFileName() + ".png");
            }
            if (target.getParent() != null) {
                Files.createDirectories(target.getParent());
            }
            Files.write(target, rendered.png());
            return ToolOutcome.text("Wrote " + target);
        } catch (Exception ex) {
            return ToolOutcome.error(ex.getMessage());
        }
    }

    public ToolOutcome testInVlc() {
        try {
            if (session().file() == null) {
                return ToolOutcome.error("Save the skin first so VLC has a theme to open");
            }
            service.save(session().file().toString());
            Path archive = Files.createTempFile(session().file().getFileName().toString().replaceAll("\\.[^.]+$", ""),
                    ".vlt");
            dev.zoroaster1x.vlcskin.format.VltCodec.write(session().theme(), session().file(), archive);
            Path installed = VlcFinder.install(archive);
            Files.deleteIfExists(archive);
            VlcFinder.launch(installed);
            return ToolOutcome.text("VLC started with " + installed.getFileName()
                    + "\nCommand: " + VlcFinder.describeLaunch(installed));
        } catch (Exception ex) {
            return ToolOutcome.error("Could not start VLC: " + ex.getMessage()
                    + "\nTry: " + VlcFinder.describeLaunch(Path.of("theme.vlt")));
        }
    }

    // --------------------------------------------------- slider background

    public ToolOutcome generateSliderBackground(Map<String, Object> args) {
        String sliderId = McpToolset.str(args, "slider");
        Item item = index().findItem(sliderId);
        if (!(item instanceof SliderItem slider)) {
            return ToolOutcome.error("No slider with id \"" + sliderId + "\"");
        }
        try {
            BufferedImage middle = readImage(McpToolset.str(args, "middle"));
            if (middle == null) {
                return ToolOutcome.error("The middle image path is required");
            }
            boolean horizontal = !"vertical".equalsIgnoreCase(McpToolset.str(args, "orientation"));
            var spec = new SliderBackgroundGenerator.Spec(
                    number(args, "width", 180),
                    number(args, "height", 10),
                    number(args, "marginLeft", 0),
                    number(args, "marginRight", 0),
                    number(args, "marginTop", 0),
                    number(args, "marginBottom", 0),
                    horizontal,
                    bool(args, "tileBackground", true),
                    bool(args, "tileMiddle", true),
                    readImage(McpToolset.str(args, "background")),
                    readImage(McpToolset.str(args, "startEdge")),
                    middle,
                    readImage(McpToolset.str(args, "endEdge")),
                    readImage(McpToolset.str(args, "overlay")));
            BufferedImage strip = SliderBackgroundGenerator.generate(spec);
            int frames = SliderBackgroundGenerator.frameCount(spec);
            Path target = outputPath(args, slider);
            ImageIO.write(strip, "png", target.toFile());
            String imageId = "none";
            if (session().file() != null) {
                Path folder = session().file().getParent();
                String relative = folder.relativize(target).toString().replace('\\', '/');
                BitmapResource bitmap = new BitmapResource();
                bitmap.setId(index().uniqueUnnamed(slider.getId() + " bg"));
                bitmap.setFile(relative);
                String previousImage = slider.getBackground() == null ? null : slider.getBackground().getImage();
                session().apply(ValueCommand.builder("Generate slider background")
                        .step(() -> {
                            session().theme().getResources().add(bitmap);
                            background(slider, bitmap.getId(), frames, horizontal);
                        }, () -> {
                            session().theme().getResources().remove(bitmap);
                            background(slider, previousImage, 1, horizontal);
                        })
                        .build());
                imageId = bitmap.getId();
                session().images().invalidate(imageId);
            }
            session().fireChanged();
            return ToolOutcome.text("Generated " + target + " with " + frames + " frames"
                    + (imageId.equals("none") ? "" : " and registered bitmap " + imageId));
        } catch (Exception ex) {
            return ToolOutcome.error("Could not generate the background: " + ex.getMessage());
        }
    }

    private Path outputPath(Map<String, Object> args, SliderItem slider) {
        String given = McpToolset.str(args, "path");
        if (given != null && !given.isBlank()) {
            return Path.of(given);
        }
        Path folder = session().file() == null
                ? Path.of(System.getProperty("java.io.tmpdir"))
                : session().file().getParent();
        return folder.resolve(slider.getId() + "_bg.png");
    }

    private BufferedImage readImage(String path) throws Exception {
        if (path == null || path.isBlank()) {
            return null;
        }
        return ImageIO.read(Path.of(path).toFile());
    }

    private int number(Map<String, Object> args, String name, int fallback) {
        Integer value = McpToolset.integer(args, name);
        return value == null ? fallback : value;
    }

    private boolean bool(Map<String, Object> args, String name, boolean fallback) {
        Object value = args.get(name);
        return value == null ? fallback : Boolean.parseBoolean(value.toString());
    }

    private void background(SliderItem slider, String image, int frames, boolean horizontal) {
        var background = slider.getBackground();
        if (background == null) {
            background = new dev.zoroaster1x.vlcskin.model.item.SliderBackground();
            background.setId(index().uniqueUnnamed("Slider background"));
            slider.setBackground(background);
        }
        background.setImage(image);
        if (horizontal) {
            background.setNbhoriz(1);
            background.setNbvert(Math.max(1, frames));
        } else {
            background.setNbhoriz(Math.max(1, frames));
            background.setNbvert(1);
        }
        background.setPadhoriz(0);
        background.setPadvert(0);
    }

    // ------------------------------------------------------------ host/UI

    public ToolOutcome getPreferences() {
        UiInspector ui = service.ui();
        if (ui == null) {
            return ToolOutcome.text("No host is attached; preferences are only meaningful with the desktop app",
                    Map.of());
        }
        return ToolOutcome.text(Json.write(ui.preferences()), ui.preferences());
    }

    public ToolOutcome setPreferences(Map<String, String> values) {
        UiInspector ui = service.ui();
        if (ui == null || values == null || values.isEmpty()) {
            return ToolOutcome.text("No host is attached or no preferences were given", Map.of());
        }
        Map<String, String> applied = new LinkedHashMap<>();
        List<String> rejected = new ArrayList<>();
        values.forEach((key, value) -> {
            if ("theme".equalsIgnoreCase(key)) {
                if (ui.applyTheme(value)) {
                    applied.put(key, value);
                } else {
                    rejected.add(key);
                }
                return;
            }
            if ("canvasZoom".equalsIgnoreCase(key) || "tool".equalsIgnoreCase(key)) {
                boolean zoomOk = ui.setCanvas("canvasZoom".equalsIgnoreCase(key) ? Integer.valueOf(value) : null,
                        "tool".equalsIgnoreCase(key) ? value : null, null);
                if (zoomOk) {
                    applied.put(key, value);
                } else {
                    rejected.add(key);
                }
                return;
            }
            if (ui.setPreference(key, value)) {
                applied.put(key, value);
            } else {
                rejected.add(key);
            }
        });
        String text = applied.isEmpty() ? "No preference could be applied"
                : "Applied " + applied.size() + " preferences" + (rejected.isEmpty() ? ""
                        : ", rejected: " + rejected);
        return ToolOutcome.text(text, applied);
    }

    public ToolOutcome setCanvas(Integer zoom, String tool, Boolean checkerboard) {
        UiInspector ui = service.ui();
        if (ui == null) {
            return ToolOutcome.text("No desktop UI is attached; canvas state applies live only");
        }
        if (ui.setCanvas(zoom, tool, checkerboard)) {
            return ToolOutcome.text("Canvas updated");
        }
        return ToolOutcome.text("The attached host refused the canvas change");
    }

    public ToolOutcome showPanel(String name) {
        UiInspector ui = service.ui();
        if (ui == null) {
            return ToolOutcome.text("No desktop UI is attached; panels exist only with the window");
        }
        return ui.showPanel(name) ? ToolOutcome.text("Showing panel " + name)
                : ToolOutcome.text("No panel named \"" + name + "\"");
    }

    public ToolOutcome openSettings() {
        UiInspector ui = service.ui();
        if (ui == null) {
            return ToolOutcome.text("No desktop UI is attached");
        }
        return ui.openSettings() ? ToolOutcome.text("Skin settings opened")
                : ToolOutcome.text("The attached host has no settings dialog");
    }

    // -------------------------------------------------------------- extras

    public ToolOutcome getResource(String id) {
        Resource resource = index().findResource(id);
        if (resource == null) {
            var image = index().findImage(id);
            if (image == null) {
                return ToolOutcome.error("No resource with id \"" + id + "\"");
            }
            Map<String, Object> entry = new LinkedHashMap<>();
            entry.put("id", id);
            entry.put("type", image.isSub() ? "SubBitmap" : "Bitmap");
            entry.put("parent", image.bitmap().getId());
            if (image.isSub()) {
                entry.put("x", image.sub().getX());
                entry.put("y", image.sub().getY());
                entry.put("width", image.sub().getWidth());
                entry.put("height", image.sub().getHeight());
                entry.put("nbframes", image.sub().getNbframes());
                entry.put("fps", image.sub().getFps());
            } else {
                entry.put("file", image.bitmap().getFile());
                entry.put("nbframes", image.bitmap().getNbframes());
                entry.put("fps", image.bitmap().getFps());
                entry.put("subBitmaps", image.bitmap().getSubBitmaps().size());
            }
            return ToolOutcome.text(Json.write(entry), entry);
        }
        Map<String, Object> entry = new LinkedHashMap<>();
        entry.put("id", resource.getId());
        entry.put("type", resource.typeName());
        Map<String, String> attributes = new LinkedHashMap<>();
        for (String name : List.of("file", "alphacolor", "nbframes", "fps", "size", "type")) {
            String value = PropertyAccess.getResource(resource, name);
            if (value != null) {
                attributes.put(name, value);
            }
        }
        entry.put("attributes", attributes);
        return ToolOutcome.text(Json.write(entry), entry);
    }

    public ToolOutcome getVariables() {
        Map<String, Object> state = new LinkedHashMap<>();
        state.put("booleans", session().variables().booleans());
        state.put("texts", session().variables().texts());
        state.put("sliderValue", session().variables().sliderValue());
        return ToolOutcome.text(Json.write(state), state);
    }

    public ToolOutcome checkForUpdates() {
        try {
            HttpClient client = HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(10)).build();
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create("https://api.github.com/repos/zoroaster1x/vlc-skin-editor/releases/latest"))
                    .timeout(Duration.ofSeconds(15))
                    .header("Accept", "application/vnd.github+json")
                    .GET()
                    .build();
            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() >= 400) {
                return ToolOutcome.text("The release check returned HTTP " + response.statusCode()
                        + "; the current version is " + Version.VERSION);
            }
            JsonNode node = Json.mapper().readTree(response.body());
            String latest = node.path("tag_name").asText("");
            String url = node.path("html_url").asText("");
            boolean newer = !latest.isBlank() && !latest.equals(Version.VERSION);
            Map<String, Object> info = new LinkedHashMap<>();
            info.put("current", Version.VERSION);
            info.put("latest", latest);
            info.put("updateAvailable", newer);
            info.put("url", url);
            return ToolOutcome.text(newer ? "A newer release is available: " + latest
                    : "Up to date (" + Version.VERSION + ")", info);
        } catch (Exception ex) {
            return ToolOutcome.text("The release check failed: " + ex.getMessage()
                    + "; the current version is " + Version.VERSION);
        }
    }

    public ToolOutcome resetSkin() {
        service.newSkin("Untitled");
        return ToolOutcome.text("Started a new empty skin", service.documentInfo());
    }

    public ToolOutcome duplicateResource(String id, String pattern) {
        Resource resource = index().findResource(id);
        if (resource == null) {
            return ToolOutcome.error("No resource with id \"" + id + "\"");
        }
        Resource copy = DeepCopy.resource(resource, index(), pattern == null ? "%oldid%_copy" : pattern);
        session().apply(new dev.zoroaster1x.vlcskin.edit.commands.AddNodeCommand<>(
                session().theme().getResources(), copy, session().theme().getResources().size(),
                "Duplicate " + resource.typeName()));
        return ToolOutcome.text("Duplicated as \"" + copy.getId() + "\"");
    }
}
