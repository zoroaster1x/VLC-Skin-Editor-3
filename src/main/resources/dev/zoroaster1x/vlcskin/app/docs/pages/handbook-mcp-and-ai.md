---
title: MCP tool reference
section: Extras and tools
source: rewritten
---

# MCP tool reference

Every tool the MCP server exposes, grouped. The introduction, the server setup and example requests are in [MCP for automation](mcp-for-automation.md); this page is the catalog to look a name up in.

## Running the MCP server

The server speaks MCP over stdio:

```bash
java -jar vlc-skin-studio.jar mcp
java -jar vlc-skin-studio.jar mcp --file out/theme.xml
```

The optional `--file` opens a theme before the first request. Register the server with OpenCode (V2 configuration):

```jsonc
{
  "mcp": {
    "servers": {
      "vlc-skin-studio": {
        "type": "local",
        "command": ["java", "-jar", "/absolute/path/vlc-skin-studio.jar", "mcp"]
      }
    }
  }
}
```

With the OpenCode CLI you can add it directly instead: `opencode mcp add vlc-skin-studio --global -- java -jar /path/vlc-skin-studio.jar mcp`.

## Tool groups

### Document

| Tool | What it does |
|---|---|
| `open_skin` | Open a theme XML as the active document. |
| `new_skin` | Start an empty theme with one 320x140 layout. |
| `save_skin` | Save to the current file or a new path. |
| `document_info` | Metadata, counts, layout list and validation problems. |
| `export_vlt` | Write the theme and its assets as a `.vlt`. |
| `import_vlt` | Unpack a `.vlt` and open the theme inside. |
| `reset_skin` | Start over without a path prompt. |
| `disk_diff` | The changed lines when the file changed on disk since the last call. |
| `sync_from_disk` | Three-way merge those changes into the open document; a conflict keeps the open version and is reported. |

### Inspect and render

| Tool | What it does |
|---|---|
| `layout_tree` | Geometry as data: every item with id, type, parent, z order, absolute bounds, visibility, text and key attributes. |
| `render_layout` | A PNG plus the same geometry, optionally highlighting one item. |
| `list_items` | All items, filtered by type, window or layout. |
| `get_item` | Every attribute of one item. |
| `get_resource` | Every attribute of a bitmap, sub bitmap, font, bitmap font, menu or ini file. |
| `get_xml` | The generated theme XML. |
| `describe_editor_ui`, `screenshot_editor` | The running desktop window as JSON and PNG. These need the desktop window in the same process. |

### Edit items

| Tool | What it does |
|---|---|
| `add_item` | Add an item to a layout or container. Image and font references default to `none` so the item exists before its bitmap does. |
| `delete_item`, `duplicate_item` | Remove or copy one item; the duplicate pattern replaces `%oldid%`. |
| `move_item`, `nudge_item` | Set or change x and y. Repeated nudges of the same item coalesce into one undo step. |
| `set_item_property` | Change one attribute by name. |
| `reorder_item`, `reparent_item` | Change z order, or move an item into a group or panel. |

### Resources

| Tool | What it does |
|---|---|
| `add_resource` | Add a bitmap, font, bitmap font, popup menu or ini file. |
| `add_bitmap_from_file` | Import an image and store its path relative to the theme. |
| `add_sub_bitmap` | Cut a rectangle out of a bitmap. |
| `set_resource_property`, `set_sub_bitmap_property` | Change attributes. |
| `delete_sub_bitmap`, `delete_resource` | Remove what no item uses. |
| `duplicate_resource` | Copy with a rename pattern. |
| `reload_images` | Drop the picture cache and reload from disk. |

### Windows, layouts and theme

| Tool | What it does |
|---|---|
| `add_window`, `delete_window`, `duplicate_window` | Manage top level windows. |
| `add_layout`, `delete_layout`, `duplicate_layout`, `reorder_layout` | Manage layouts; the last one is the default. |
| `set_theme_property` | Theme metadata and attributes. |
| `set_window_property`, `set_layout_property` | Window and layout attributes. |

### History, selection and state

| Tool | What it does |
|---|---|
| `undo`, `redo`, `history_state` | Drive and inspect the undo stack. |
| `select_element`, `get_selection` | Use the same selection the trees and Inspector use. |
| `set_variables`, `get_variables` | Simulate player state: booleans, text variables and `sliderValue` from 0 to 1. |
| `validate_skin` | Run the validator and list every problem. |
| `list_actions`, `list_examples`, `create_example` | Action catalog, built in examples and instantiation. |

### Output and host

| Tool | What it does |
|---|---|
| `apply_xml` | Parse edited XML and replace the document, like the Apply button. |
| `save_preview` | Write a layout render to a PNG file. |
| `test_in_vlc` | Save, install the `.vlt` into VLC's skins folder and start VLC. |
| `generate_slider_background` | Build a background strip and register it on a slider. |
| `get_preferences`, `set_preferences` | Host preferences such as theme, language, canvas background, checkerboard, auto update and canvas state, plus the recent files list. |
| `set_canvas`, `fit_canvas` | Zoom, tool, checkerboard and Fit window of the running canvas. |
| `show_panel`, `open_settings`, `reset_panel_layout`, `quit_app` | Bring a panel forward, open Skin settings, restore the default dock arrangement or close the window. |
| `check_for_updates`, `install_update` | See the newest release and every missed patch note, then download, verify and install the jar. |
| `app_info`, `list_documentation`, `search_documentation`, `read_documentation` | The About box and Help links, plus the whole bundled documentation: topic list, ranked search and full markdown, including the skin format reference and this MCP guide. |
| `list_gallery_themes`, `gallery_theme_preview`, `import_gallery_theme` | Search the official VideoLAN gallery, look at a theme's preview PNG before deciding, then import it. |

## The shared file and the activity log

The server is a separate process from the window, so they share the file rather than memory. A result can start with a `[NOTICE]` line when the file changed on disk outside the server; `disk_diff` shows the changed lines and `sync_from_disk` merges them. The MCP activity panel tails the server's cache `mcp.log` and `mcp-status.json`, and Preferences (AI and MCP) can disable the server; the `mcp` command then refuses to start unless `--force` is passed.

## Geometry or pixels

`layout_tree` describes a layout as structured data. Each node carries the item id, its display type and element name, parent, depth, z order, absolute x, y, width and height, visibility under the current variables, text where there is text, and the attributes that matter for the type. A model without image input can reason about the whole layout from that alone.

`render_layout` returns the same data and a PNG. Use it when a visual check helps: colors, sprite choice, overlapping items, thumb position. Both tools accept a zoom, and `render_layout` can highlight one item id for the reply.

An AI session follows a simple pattern: open or create a theme, ask for `layout_tree`, change a few attributes, then ask for `render_layout` and look at the result. The bundled acceptance script `tools/recreate-velocity-via-mcp.py` rebuilds the VeLoCity player window exactly that way, one tool call per element.

Next: [VLT archives](vlt-archives.md).
