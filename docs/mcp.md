# MCP server

The server speaks MCP over stdio and exposes the editor, so an AI client can
work the same document the window shows. Every user action has a tool; the
[in-app handbook](../src/main/resources/dev/zoroaster1x/vlcskin/app/docs/pages/handbook-mcp-and-ai.md)
is the tool reference the app itself ships, and `read_documentation` returns it
to the model.

## Tool catalog

* `open_skin`, `new_skin`, `save_skin`, `import_vlt`, `export_vlt`,
  `document_info`, `reset_skin`
* `layout_tree`, `render_layout`, `list_items`, `get_item`, `get_resource`
* `add_item`, `delete_item`, `move_item`, `nudge_item`, `reorder_item`,
  `reparent_item`, `set_item_property`, `duplicate_item`
* `add_resource`, `add_bitmap_from_file`, `add_sub_bitmap`,
  `set_resource_property`, `set_sub_bitmap_property`, `delete_sub_bitmap`,
  `delete_resource`, `duplicate_resource`
* `add_window`, `delete_window`, `add_layout`, `delete_layout`,
  `duplicate_window`, `duplicate_layout`, `reorder_layout`,
  `set_theme_property`, `set_window_property`, `set_layout_property`
* `undo`, `redo`, `history_state`, `select_element`, `get_selection`
* `get_xml`, `apply_xml`, `reload_images`, `save_preview`, `test_in_vlc`
* `generate_slider_background`, `validate_skin`, `set_variables`,
  `get_variables`, `list_actions`, `list_examples`, `create_example`
* `get_preferences`, `set_preferences`, `set_canvas`, `fit_canvas`,
  `show_panel`, `open_settings`, `reset_panel_layout`, `quit_app`
* `check_for_updates`, `install_update`
* `app_info`, `list_documentation`, `search_documentation`,
  `read_documentation` (the handbook, the skin format reference and the
  archived original help)
* `list_gallery_themes`, `gallery_theme_preview`, `import_gallery_theme`
  (the official VideoLAN gallery, with preview images before importing)
* `describe_editor_ui`, `screenshot_editor` (available when the desktop window
  is running in the same process)
* `disk_diff`, `sync_from_disk`: the lines that changed on disk when another
  program saved the file, and a three-way merge of those changes into the open
  document without discarding either side.

## The shared file and the activity log

The server is a separate process from the desktop window, so the two share the
file rather than memory. A tool result can start with a `[NOTICE]` line when
the file changed on disk outside the server; `disk_diff` lists the changed
lines and `sync_from_disk` merges them (`format/XmlMerger`). A change only one
side made is applied; a conflict keeps the open document's version and is
reported.

The server can be disabled in Preferences (AI and MCP, Enable the MCP server);
the `mcp` command then refuses to start unless `--force` is passed. Every run
appends timestamped lines to the cache `mcp.log` and updates `mcp-status.json`,
which the MCP activity panel tails live, and `Show tool calls in the status
bar` names every call in the status bar.

## Register with OpenCode

V2 configuration:

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

Or with the CLI: `opencode mcp add vlc-skin-studio --global -- java -jar /path/vlc-skin-studio.jar mcp`.

Start it with a skin already open if you like:
`java -jar vlc-skin-studio.jar mcp --file out/theme.xml`.

## A typical session

Create an example, ask for the geometry, change a few attributes, ask for a
render and look at it. The server returns the PNG as image content and the
geometry as structured data in the same reply.

Scripts, editors and AI clients drive the same `EditorService` over the MCP
server, so every operation the window can do is reproducible from a terminal.
