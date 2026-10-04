---
title: MCP for automation
section: Extras and tools
source: rewritten
---

# MCP for automation

MCP, the Model Context Protocol, is a small standard that lets a client program talk to a tool server. VLC Skin Studio ships such a server: it exposes the same document operations the desktop window uses, so a script or an AI client can open a theme, inspect it, change it, render it and package it without touching the mouse. The full tool catalog is in [MCP tool reference](mcp-and-ai.md).

## Start the server

The server speaks MCP over stdio, so the client launches it as a child process:

```bash
java -jar vlc-skin-studio.jar mcp
java -jar vlc-skin-studio.jar mcp --file out/theme.xml
```

The optional `--file` opens a theme before the first request. The native build serves the same command as `vlc-skin-studio mcp`.

## Point a client at the jar

Any MCP client that can launch a stdio command works. With OpenCode (V2 configuration):

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

The OpenCode CLI can register it directly:

```bash
opencode mcp add vlc-skin-studio --global -- java -jar /path/vlc-skin-studio.jar mcp
```

Use the absolute path to the fat jar, `build/libs/vlc-skin-studio.jar` after `./gradlew shadowJar`. The server runs as its own process with its own open document; the two tools that describe the desktop window, `describe_editor_ui` and `screenshot_editor`, need that window running in the same process.

The window and the server share the file rather than memory. When another program saves it, a result can start with a `[NOTICE]`; `disk_diff` lists the changed lines and `sync_from_disk` merges them without discarding either side. The server's activity is appended to the cache `mcp.log` and `mcp-status.json`, which the MCP activity panel tails, and Preferences (AI and MCP) can disable the server; the `mcp` command then refuses to start unless `--force` is passed.

## The tool groups in plain language

| Group | What it covers |
|---|---|
| Document | Open, create, save and reset a theme, export and import `.vlt`, read a summary of the open document, and reconcile a file another program changed with `disk_diff` and `sync_from_disk`. |
| Inspect and render | The layout as geometry data, the layout as a PNG plus geometry, item and resource attributes, and the generated XML. |
| Edit items | Add, delete, duplicate, move and nudge items, change one attribute by name, change z order and move items into groups and panels. |
| Resources | Add bitmaps, fonts, bitmap fonts, popup menus and ini files, import images from disk, cut sub bitmaps, edit them, delete and duplicate them, and reload images from disk. |
| Windows, layouts and theme | Add, delete, duplicate and reorder windows and layouts, and edit theme metadata and attributes. |
| History, selection and state | Undo and redo, read the history, use the same selection the trees use, simulate the player variables, and validate the theme. |
| Output and host | Apply edited XML, write a preview PNG, test in VLC, generate a slider background, read and write preferences, set or fit the canvas, show a panel, open Skin settings, reset the panel layout, quit the window, browse the gallery with preview images before importing, check for and install a SHA-256 verified update, and read the whole bundled documentation including the skin format reference. |

`layout_tree` is the workhorse: every item with its id, type, parent, z order, absolute bounds, visibility, text and the attributes that matter for the type. A client that cannot see images can still reason about the whole layout from that. `render_layout` adds the PNG and can highlight one item id.

## Three example requests

| Ask for | What the client does |
|---|---|
| Create the neon example in /tmp/demo and describe the layout. | `create_example`, then `layout_tree`. |
| Add a play button at 12,12 with the play_up bitmap, then render the layout at 2x. | `add_item` with properties, then `render_layout`. |
| The stop button overlaps the play button. Move it right by 24 pixels and render again. | `nudge_item` twice, then `render_layout`. |

A fourth common one: Validate the skin, rename the duplicate ids and export it as theme.vlt, which chains `validate_skin`, `set_item_property` and `export_vlt`.

Next: [MCP tool reference](mcp-and-ai.md).
