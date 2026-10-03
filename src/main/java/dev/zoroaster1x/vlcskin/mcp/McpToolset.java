package dev.zoroaster1x.vlcskin.mcp;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * The full tool catalog the MCP server exposes.
 */
public final class McpToolset {

    private final EditorService service;
    private final EditorControl control;

    public McpToolset(EditorService service) {
        this.service = service;
        this.control = new EditorControl(service);
    }

    public EditorService service() {
        return service;
    }

    public List<ToolSpec> tools() {
        List<ToolSpec> tools = new ArrayList<>();
        tools.add(new ToolSpec("open_skin", "Open a skin",
                "Open a skin XML file as the active document for every other tool.",
                Schema.object().string("path", "Path to the skin XML file").required("path").build(),
                args -> service.open(str(args, "path"))));
        tools.add(new ToolSpec("new_skin", "New skin",
                "Start a new empty skin with one window and one 320x140 layout.",
                Schema.object().string("name", "Theme name").build(),
                args -> service.newSkin(str(args, "name"))));
        tools.add(new ToolSpec("save_skin", "Save the skin",
                "Save the active skin. Without a path the current file is overwritten.",
                Schema.object().string("path", "Optional new file path").build(),
                args -> service.save(str(args, "path"))));
        tools.add(new ToolSpec("document_info", "Document info",
                "Theme metadata, resource and window counts, layout list and validation problems.",
                Schema.object().build(), args -> ToolOutcome.text(service.documentInfo())));
        tools.add(new ToolSpec("export_vlt", "Export VLT",
                "Write the active skin as a .vlt theme archive with all referenced assets.",
                Schema.object().string("path", "Target .vlt path").required("path").build(),
                args -> service.exportVlt(str(args, "path"))));
        tools.add(new ToolSpec("import_vlt", "Import VLT",
                "Unpack a .vlt theme archive and open its theme.xml.",
                Schema.object().string("path", "The archive path")
                        .string("folder", "Optional target folder").required("path").build(),
                args -> service.importVlt(str(args, "path"), str(args, "folder"))));

        tools.add(new ToolSpec("layout_tree", "Layout geometry",
                "Every item of a layout with absolute x, y, width, height, z order, text and attributes. "
                        + "Use this to reason about the layout without an image.",
                Schema.object().string("window", "Window id, defaults to the selected one")
                        .string("layout", "Layout id, defaults to the selected one")
                        .integer("zoom", "Zoom level for the description")
                        .build(),
                args -> service.layoutTree(str(args, "window"), str(args, "layout"), integer(args, "zoom"))));
        tools.add(new ToolSpec("render_layout", "Render layout to PNG",
                "Render a layout and return both a PNG image and the geometry data.",
                Schema.object().string("window", "Window id")
                        .string("layout", "Layout id")
                        .integer("zoom", "Zoom 1 to 16")
                        .string("selection", "Optional item id to highlight")
                        .build(),
                args -> service.renderLayout(str(args, "window"), str(args, "layout"), integer(args, "zoom"),
                        str(args, "selection"))));
        tools.add(new ToolSpec("list_items", "List items",
                "All layout elements, optionally filtered by type.",
                Schema.object().string("type", "Anchor, Button, Checkbox, Group, Image, Panel, Playlist, "
                                + "Playtree, RadialSlider, Slider, Text or Video")
                        .string("window", "Optional window filter")
                        .string("layout", "Optional layout filter").build(),
                args -> service.listItems(str(args, "type"), str(args, "window"), str(args, "layout"))));
        tools.add(new ToolSpec("get_item", "Get item",
                "Every attribute of one item by id.",
                Schema.object().string("id", "Item id").required("id").build(),
                args -> service.getItem(str(args, "id"))));

        tools.add(new ToolSpec("add_item", "Add item",
                "Add a layout element. Required image and font references default to \"none\" so the item "
                        + "exists before its bitmap does; set them afterwards or with properties.",
                Schema.object()
                        .string("type", "Anchor, Button, Checkbox, Group, Image, Panel, Playlist, Playtree, "
                                + "RadialSlider, Slider, Text or Video")
                        .string("window", "Target window id")
                        .string("layout", "Target layout id")
                        .string("parent", "Optional container item id")
                        .integer("x", "X position")
                        .integer("y", "Y position")
                        .object("properties", "Attribute map", Map.of())
                        .required("type").build(),
                args -> service.addItem(str(args, "type"), str(args, "window"), str(args, "layout"),
                        str(args, "parent"), integer(args, "x"), integer(args, "y"), stringMap(args, "properties"))));
        tools.add(new ToolSpec("delete_item", "Delete item",
                "Remove an item from its layout or container.",
                Schema.object().string("id", "Item id").required("id").build(),
                args -> service.deleteItem(str(args, "id"))));
        tools.add(new ToolSpec("move_item", "Move item",
                "Set the x and y of an item.",
                Schema.object().string("id", "Item id").integer("x", "New x").integer("y", "New y")
                        .required("id", "x", "y").build(),
                args -> service.moveItem(str(args, "id"), integer(args, "x"), integer(args, "y"))));
        tools.add(new ToolSpec("set_item_property", "Set item attribute",
                "Change one item attribute by name. The known names are returned by get_item.",
                Schema.object().string("id", "Item id").string("name", "Attribute name")
                        .string("value", "New value").required("id", "name", "value").build(),
                args -> service.setItemProperty(str(args, "id"), str(args, "name"), str(args, "value"))));
        tools.add(new ToolSpec("duplicate_item", "Duplicate item",
                "Copy an item with a fresh id; the pattern replaces %oldid%.",
                Schema.object().string("id", "Item id")
                        .string("pattern", "Rename pattern, defaults to %oldid%_copy")
                        .required("id").build(),
                args -> service.duplicateItem(str(args, "id"),
                        str(args, "pattern") == null ? "%oldid%_copy" : str(args, "pattern"))));

        tools.add(new ToolSpec("add_resource", "Add resource",
                "Add a bitmap, font, bitmap font, popup menu or ini file resource.",
                Schema.object().string("type", "bitmap, font, bitmapfont, popupmenu or inifile")
                        .string("id", "Optional id")
                        .string("file", "Optional file path relative to the skin")
                        .object("properties", "Attribute map", Map.of())
                        .required("type").build(),
                args -> service.addResource(str(args, "type"), str(args, "id"), str(args, "file"),
                        stringMap(args, "properties"))));
        tools.add(new ToolSpec("add_bitmap_from_file", "Add bitmap from file",
                "Import an image file as a bitmap resource, with the path made relative to the skin.",
                Schema.object().string("file", "Image file path").string("id", "Optional id")
                        .required("file").build(),
                args -> service.addBitmapFromFile(str(args, "file"), str(args, "id"))));
        tools.add(new ToolSpec("add_sub_bitmap", "Add sub bitmap",
                "Cut a named rectangle out of a bitmap resource; the most common way skins reuse one "
                        + "sprite sheet for many controls.",
                Schema.object().string("bitmap", "Parent bitmap id").string("id", "Sub bitmap id")
                        .integer("x", "Left").integer("y", "Top")
                        .integer("width", "Width").integer("height", "Height")
                        .required("bitmap").build(),
                args -> service.addSubBitmap(str(args, "bitmap"), str(args, "id"), integer(args, "x"),
                        integer(args, "y"), integer(args, "width"), integer(args, "height"))));
        tools.add(new ToolSpec("set_resource_property", "Set resource attribute",
                "Change one resource attribute: file, alphacolor, nbframes, fps, size or type.",
                Schema.object().string("id", "Resource id").string("name", "Attribute name")
                        .string("value", "New value").required("id", "name", "value").build(),
                args -> service.setResourceProperty(str(args, "id"), str(args, "name"), str(args, "value"))));
        tools.add(new ToolSpec("set_sub_bitmap_property", "Set sub bitmap attribute",
                "Change one sub bitmap: id, x, y, width, height, nbframes or fps.",
                Schema.object().string("bitmap", "Parent bitmap id").string("sub", "Sub bitmap id")
                        .string("name", "Attribute name").string("value", "New value")
                        .required("bitmap", "sub", "name", "value").build(),
                args -> service.setSubBitmapProperty(str(args, "bitmap"), str(args, "sub"), str(args, "name"),
                        str(args, "value"))));
        tools.add(new ToolSpec("delete_sub_bitmap", "Delete sub bitmap",
                "Remove a sub bitmap that no item uses.",
                Schema.object().string("bitmap", "Parent bitmap id").string("sub", "Sub bitmap id")
                        .required("bitmap", "sub").build(),
                args -> service.deleteSubBitmap(str(args, "bitmap"), str(args, "sub"))));
        tools.add(new ToolSpec("delete_resource", "Delete resource",
                "Remove an unused resource.",
                Schema.object().string("id", "Resource id").required("id").build(),
                args -> service.deleteResource(str(args, "id"))));

        tools.add(new ToolSpec("add_window", "Add window",
                "Add a window with one layout.",
                Schema.object().string("id", "Optional window id").integer("width", "Layout width")
                        .integer("height", "Layout height").build(),
                args -> service.addWindow(str(args, "id"), integer(args, "width"), integer(args, "height"))));
        tools.add(new ToolSpec("add_layout", "Add layout",
                "Add a layout to an existing window.",
                Schema.object().string("window", "Window id").string("id", "Optional layout id")
                        .integer("width", "Width").integer("height", "Height").required("window").build(),
                args -> service.addLayout(str(args, "window"), str(args, "id"), integer(args, "width"),
                        integer(args, "height"))));
        tools.add(new ToolSpec("delete_window", "Delete window",
                "Remove a window and its layouts; the last window is kept.",
                Schema.object().string("window", "Window id").required("window").build(),
                args -> service.deleteWindow(str(args, "window"))));
        tools.add(new ToolSpec("delete_layout", "Delete layout",
                "Remove a layout from its window; the last layout is kept.",
                Schema.object().string("window", "Window id").string("layout", "Layout id")
                        .required("window", "layout").build(),
                args -> service.deleteLayout(str(args, "window"), str(args, "layout"))));
        tools.add(new ToolSpec("duplicate_window", "Duplicate window",
                "Copy a window with its layouts and fresh ids.",
                Schema.object().string("window", "Window id")
                        .string("pattern", "Rename pattern, defaults to %oldid%_copy")
                        .required("window").build(),
                args -> service.duplicateWindow(str(args, "window"),
                        str(args, "pattern") == null ? "%oldid%_copy" : str(args, "pattern"))));
        tools.add(new ToolSpec("duplicate_layout", "Duplicate layout",
                "Copy a layout inside its window with fresh item ids.",
                Schema.object().string("window", "Window id").string("layout", "Layout id")
                        .string("pattern", "Rename pattern, defaults to %oldid%_copy")
                        .required("window", "layout").build(),
                args -> service.duplicateLayout(str(args, "window"), str(args, "layout"),
                        str(args, "pattern") == null ? "%oldid%_copy" : str(args, "pattern"))));
        tools.add(new ToolSpec("set_theme_property", "Set theme attribute",
                "Change theme metadata or attributes: name, author, email, webpage, magnet, alpha, "
                        + "movealpha or tooltipfont.",
                Schema.object().string("name", "Attribute name").string("value", "New value")
                        .required("name", "value").build(),
                args -> service.setThemeProperty(str(args, "name"), str(args, "value"))));
        tools.add(new ToolSpec("set_window_property", "Set window attribute",
                "Change a window: id, visible, x, y, dragdrop or playondrop.",
                Schema.object().string("window", "Window id").string("name", "Attribute name")
                        .string("value", "New value").required("window", "name", "value").build(),
                args -> service.setWindowProperty(str(args, "window"), str(args, "name"),
                        str(args, "value"))));
        tools.add(new ToolSpec("set_layout_property", "Set layout attribute",
                "Change a layout: id, width, height, minwidth, maxwidth, minheight or maxheight.",
                Schema.object().string("window", "Window id").string("layout", "Layout id")
                        .string("name", "Attribute name").string("value", "New value")
                        .required("window", "layout", "name", "value").build(),
                args -> service.setLayoutProperty(str(args, "window"), str(args, "layout"),
                        str(args, "name"), str(args, "value"))));

        tools.add(new ToolSpec("validate_skin", "Validate skin",
                "Check ids, references, sizes, colors and files and list every problem.",
                Schema.object().build(), args -> service.validate()));
        tools.add(new ToolSpec("set_variables", "Set preview variables",
                "Simulate player state: booleans such as vlc.isPlaying, texts such as $N, and sliderValue 0 to 1.",
                Schema.object()
                        .object("booleans", "Variable to boolean", Map.of())
                        .object("texts", "Token to text", Map.of())
                        .number("sliderValue", "Slider position 0 to 1")
                        .build(),
                args -> service.setVariables(boolMap(args, "booleans"), stringMap(args, "texts"),
                        number(args, "sliderValue"))));
        tools.add(new ToolSpec("list_actions", "List actions",
                "Every action code a button, image or checkbox can run.",
                Schema.object().build(), args -> service.listActions()));
        tools.add(new ToolSpec("list_examples", "List examples",
                "Built in example themes that can be created instantly.",
                Schema.object().build(), args -> service.listExamples()));
        tools.add(new ToolSpec("create_example", "Create example",
                "Create an example theme with generated assets and open it. Great for a starting point.",
                Schema.object().string("id", "Example id from list_examples")
                        .string("folder", "Optional target folder").required("id").build(),
                args -> service.createExample(str(args, "id"), str(args, "folder"))));
        tools.add(new ToolSpec("describe_editor_ui", "Describe editor UI",
                "JSON of the running desktop window: panels, their sizes and proportions, the focused control "
                        + "and the selected document. Use when the UI is open.",
                Schema.object().build(), args -> service.describeUi()));
        tools.add(new ToolSpec("screenshot_editor", "Screenshot editor UI",
                "PNG of the running desktop window.",
                Schema.object().build(), args -> service.screenshotUi()));

        tools.add(new ToolSpec("undo", "Undo",
                "Undo the last change; the original editor's Ctrl+Z.",
                Schema.object().build(), args -> control.undo()));
        tools.add(new ToolSpec("redo", "Redo",
                "Redo the last undone change.",
                Schema.object().build(), args -> control.redo()));
        tools.add(new ToolSpec("history_state", "History state",
                "Whether undo and redo are available, with their descriptions.",
                Schema.object().build(), args -> control.historyState()));
        tools.add(new ToolSpec("select_element", "Select element",
                "Select an item, resource, window or layout, the same selection the trees and the inspector use.",
                Schema.object().string("kind", "item, resource, window or layout")
                        .string("id", "Element id").required("kind", "id").build(),
                args -> control.select(str(args, "kind"), str(args, "id"))));
        tools.add(new ToolSpec("get_selection", "Get selection",
                "What is currently selected and its type.",
                Schema.object().build(), args -> control.selectionState()));
        tools.add(new ToolSpec("nudge_item", "Nudge item",
                "Move an item by a delta; repeated nudges of the same item are one undo step.",
                Schema.object().string("id", "Item id").integer("dx", "Delta x").integer("dy", "Delta y")
                        .required("id", "dx", "dy").build(),
                args -> control.nudgeItem(str(args, "id"), integer(args, "dx"), integer(args, "dy"))));
        tools.add(new ToolSpec("reorder_item", "Reorder item",
                "Change an item's z order or list position: up, down, front or back.",
                Schema.object().string("id", "Item id").string("action", "up, down, front or back")
                        .required("id", "action").build(),
                args -> control.reorderItem(str(args, "id"), str(args, "action"))));
        tools.add(new ToolSpec("reparent_item", "Reparent item",
                "Move an item into a group or panel, or back to the layout root.",
                Schema.object().string("id", "Item id").string("parent", "New parent id, empty for the layout root")
                        .integer("index", "Optional position in the new parent")
                        .required("id").build(),
                args -> control.reparentItem(str(args, "id"), str(args, "parent"), integer(args, "index"))));
        tools.add(new ToolSpec("reorder_layout", "Reorder layout",
                "Move a layout to another position in its window; the last one is the default size.",
                Schema.object().string("window", "Window id").string("layout", "Layout id")
                        .integer("index", "New position").required("window", "layout", "index").build(),
                args -> control.reorderLayout(str(args, "window"), str(args, "layout"), integer(args, "index"))));
        tools.add(new ToolSpec("get_xml", "Get skin XML",
                "The generated skin XML, the same text the Skin XML panel shows.",
                Schema.object().build(), args -> control.getXml()));
        tools.add(new ToolSpec("apply_xml", "Apply skin XML",
                "Parse an edited skin XML and replace the open document, like the Apply button.",
                Schema.object().string("xml", "The complete theme XML").required("xml").build(),
                args -> control.applyXml(str(args, "xml"))));
        tools.add(new ToolSpec("reload_images", "Reload images",
                "Drop the decoded bitmap cache and reload every image from disk.",
                Schema.object().build(), args -> control.reloadImages()));
        tools.add(new ToolSpec("save_preview", "Save preview PNG",
                "Write a layout render to a PNG file, the same as Save preview as PNG.",
                Schema.object().string("path", "Target PNG path").string("window", "Window id")
                        .string("layout", "Layout id").integer("zoom", "Zoom 1 to 16")
                        .required("path").build(),
                args -> control.savePreview(str(args, "path"), str(args, "window"), str(args, "layout"),
                        integer(args, "zoom"))));
        tools.add(new ToolSpec("test_in_vlc", "Test skin in VLC",
                "Save the skin, install the .vlt into VLC's skins folder and start VLC in skins2 mode. "
                        + "Handles native and Flatpak VLC.",
                Schema.object().build(), args -> control.testInVlc()));
        tools.add(new ToolSpec("generate_slider_background", "Generate slider background",
                "Build a slider background strip from images and register it, the same as the generator wizard.",
                Schema.object()
                        .string("slider", "Slider item id")
                        .string("middle", "Required middle image path")
                        .string("background", "Optional background image path")
                        .string("startEdge", "Optional start edge image path")
                        .string("endEdge", "Optional end edge image path")
                        .string("overlay", "Optional overlay image path")
                        .string("orientation", "horizontal (default) or vertical")
                        .string("path", "Optional output PNG path")
                        .integer("width", "Track width for horizontal")
                        .integer("height", "Track height")
                        .integer("marginLeft", "Left margin").integer("marginRight", "Right margin")
                        .integer("marginTop", "Top margin").integer("marginBottom", "Bottom margin")
                        .bool("tileBackground", "Tile the background").bool("tileMiddle", "Tile the middle")
                        .required("slider", "middle").build(),
                args -> control.generateSliderBackground(args)));
        tools.add(new ToolSpec("get_preferences", "Get preferences",
                "The desktop host preferences: theme, language, checkerboard, toolbar and canvas state.",
                Schema.object().build(), args -> control.getPreferences()));
        tools.add(new ToolSpec("set_preferences", "Set preferences",
                "Change host preferences: theme, language, checkerboard, showToolbar, canvasZoom or tool.",
                Schema.object().object("values", "Preference key to value", Map.of())
                        .required("values").build(),
                args -> control.setPreferences(stringMap(args, "values"))));
        tools.add(new ToolSpec("set_canvas", "Set canvas state",
                "Zoom, tool (move or path) and checkerboard of the running canvas.",
                Schema.object().integer("zoom", "Zoom 1 to 16").string("tool", "move or path")
                        .bool("checkerboard", "Checkerboard behind the preview").build(),
                args -> control.setCanvas(integer(args, "zoom"), str(args, "tool"),
                        args.get("checkerboard") == null ? null : Boolean.parseBoolean(args.get("checkerboard").toString()))));
        tools.add(new ToolSpec("show_panel", "Show panel",
                "Bring a dock panel to the front: Resources, Windows and layouts, Items, Canvas, Inspector, "
                        + "Variables, Problems, Skin XML or AI assistant.",
                Schema.object().string("name", "Panel name").required("name").build(),
                args -> control.showPanel(str(args, "name"))));
        tools.add(new ToolSpec("open_settings", "Open skin settings",
                "Open the Skin settings dialog in the running window.",
                Schema.object().build(), args -> control.openSettings()));
        tools.add(new ToolSpec("get_resource", "Get resource",
                "Every attribute of a bitmap, sub bitmap, font, bitmap font, menu or ini file.",
                Schema.object().string("id", "Resource id").required("id").build(),
                args -> control.getResource(str(args, "id"))));
        tools.add(new ToolSpec("get_variables", "Get preview variables",
                "The simulated player state the preview renders against.",
                Schema.object().build(), args -> control.getVariables()));
        tools.add(new ToolSpec("check_for_updates", "Check for updates",
                "Compare the running version with the latest GitHub release.",
                Schema.object().build(), args -> control.checkForUpdates()));
        tools.add(new ToolSpec("list_gallery_themes", "List gallery themes",
                "The official VideoLAN skins gallery; filter by name or author.",
                Schema.object().string("query", "Optional filter").build(),
                args -> control.listGalleryThemes(str(args, "query"))));
        tools.add(new ToolSpec("import_gallery_theme", "Import a gallery theme",
                "Download one theme from the official gallery, unpack it and open it.",
                Schema.object().string("name", "Theme name or archive file")
                        .string("folder", "Optional target folder")
                        .required("name").build(),
                args -> control.importGalleryTheme(str(args, "name"), str(args, "folder"))));
        tools.add(new ToolSpec("reset_skin", "New empty skin",
                "Start a new empty skin, the same as File > New without the path prompt.",
                Schema.object().build(), args -> control.resetSkin()));
        tools.add(new ToolSpec("duplicate_resource", "Duplicate resource",
                "Copy a resource with a rename pattern such as %oldid%_copy.",
                Schema.object().string("id", "Resource id")
                        .string("pattern", "Rename pattern, defaults to %oldid%_copy")
                        .required("id").build(),
                args -> control.duplicateResource(str(args, "id"),
                        str(args, "pattern") == null ? "%oldid%_copy" : str(args, "pattern"))));
        return tools;
    }


    public static String str(Map<String, Object> args, String name) {
        Object value = args.get(name);
        return value == null ? null : value.toString();
    }

    public static Integer integer(Map<String, Object> args, String name) {
        Object value = args.get(name);
        if (value == null) {
            return null;
        }
        if (value instanceof Number number) {
            return number.intValue();
        }
        try {
            return Integer.parseInt(value.toString());
        } catch (NumberFormatException ex) {
            return null;
        }
    }

    public static Float number(Map<String, Object> args, String name) {
        Object value = args.get(name);
        if (value == null) {
            return null;
        }
        if (value instanceof Number number) {
            return number.floatValue();
        }
        try {
            return Float.parseFloat(value.toString());
        } catch (NumberFormatException ex) {
            return null;
        }
    }

    @SuppressWarnings("unchecked")
    public static Map<String, String> stringMap(Map<String, Object> args, String name) {
        Object value = args.get(name);
        if (!(value instanceof Map<?, ?> map)) {
            return Map.of();
        }
        Map<String, String> result = new LinkedHashMap<>();
        map.forEach((key, entry) -> result.put(key.toString(), entry == null ? "" : entry.toString()));
        return result;
    }

    @SuppressWarnings("unchecked")
    public static Map<String, Boolean> boolMap(Map<String, Object> args, String name) {
        Object value = args.get(name);
        if (!(value instanceof Map<?, ?> map)) {
            return Map.of();
        }
        Map<String, Boolean> result = new LinkedHashMap<>();
        map.forEach((key, entry) -> {
            if (entry instanceof Boolean bool) {
                result.put(key.toString(), bool);
            } else if (entry != null) {
                result.put(key.toString(), Boolean.parseBoolean(entry.toString()));
            }
        });
        return result;
    }
}
