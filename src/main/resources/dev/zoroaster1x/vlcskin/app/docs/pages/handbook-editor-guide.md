---
title: Editor guide
section: Start here
source: rewritten
---

# Editor guide

VLC Skin Studio opens a dockable desktop window on top of the same format engine the CLI, terminal UI and MCP server use. This page is the map; the details live in the pages it links to.

## What the window contains

A menu bar and a toolbar at the top, the eight dockable panels in the middle, and a status bar at the bottom. The canvas is the center of the work: it renders the selected layout and hosts the Move and Path tools.

- [Window tour](window-tour.md) covers every panel, the docking rules, the toolbar, the status bar, the canvas controls and the three trees.
- [Menus and shortcuts](menus-and-shortcuts.md) lists every menu entry and every keyboard shortcut.
- The welcome card fills the canvas while no layout is selected; [Welcome and your first skin](welcome-and-first-skin.md) explains it.

## Docking and panels

Every panel is dockable: drag it by its title to move, stack or float it. The arrangement is saved to `layout.xml` next to the settings, and View > Reset panel layout restores the default. Settings live in `$XDG_CONFIG_HOME/vlc-skin-studio/settings.json`, or `~/.config/vlc-skin-studio/settings.json` when `XDG_CONFIG_HOME` is unset.

The eight panels are Resources, Windows and layouts, Items, Canvas, Inspector, Variables, Problems and Skin XML. Their jobs are listed in the [window tour](window-tour.md).

## Themes, language and the toolbar

The theme list offers Light, Dark, IntelliJ, Darcula, Arc, Arc dark and One dark, with the VLC orange accent in every one. The Dark theme checkbox in the View menu toggles between Light and Dark. Edit > Preferences holds the look and feel, the language, the canvas background, the checkerboard and the toolbar visibility.

The 21 language files converted from the original VLC Skin Editor cover menus, toolbar tooltips, panel titles and common dialogs. Newer surfaces stay in English until a translation exists, and the note in the Preferences dialog says so. The status bar names the current file, the selection, the canvas zoom and whether the document has unsaved changes.

The toolbar carries Open, Save, Undo, Redo, the Move and Path tools, zoom out, zoom in, Fit, Validate, Render preview to PNG, Skin settings and Global variables. It can float and it can hide; both states are remembered.

## Gallery and documentation

- File > Browse themes (Ctrl+B) opens the official VideoLAN gallery; see [Theme browser](theme-browser.md).
- Help > Documentation (F1) opens the bundled handbook in its own window; see [Documentation viewer](documentation-viewer.md).

## Testing and the other ways in

File > Test skin in VLC saves the theme and starts VLC on it; [Step 7](step-7-validate-test-export.md) has the exact command and the install folders:

```bash
vlc-skin-studio new --example neon out/theme.xml
vlc-skin-studio render out/theme.xml -z 2 -o preview.png
vlc-skin-studio validate out/theme.xml
vlc-skin-studio vlt export out/theme.xml out/theme.vlt
```

The terminal UI (`tui`) browses a theme over SSH and includes `tree`, `render!`, `set`, `show` and `validate` commands. The MCP server exposes the document to scripts and AI clients; start at [MCP for automation](mcp-for-automation.md).

Next: [Menus and shortcuts](menus-and-shortcuts.md).
