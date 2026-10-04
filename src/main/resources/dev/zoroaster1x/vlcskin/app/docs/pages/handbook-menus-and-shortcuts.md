---
title: Menus and shortcuts
section: Start here
source: rewritten
---

# Menus and shortcuts

Every menu entry, in plain language, followed by the keyboard shortcuts.

## File

| Entry | What it does |
|---|---|
| New | Asks for the path of a new `.xml` file and starts an empty 320x140 theme there. |
| Open... | Opens a `.xml` theme or imports a `.vlt` archive. |
| Recent files | The last twelve files you opened, each listed by file name. Hovering shows the full path. An empty list shows No recent files yet. |
| Save | Writes the current document to its file. An unsaved document asks for a path. |
| Save as... | Writes the document to a new path and remembers it as the current file. |
| Import VLT... | Unpacks a `.vlt` or `.zip` theme next to the archive into `<archive>_unpacked` and opens the `theme.xml` inside. |
| Browse themes... | Opens the official VideoLAN gallery browser. See [Theme browser](theme-browser.md). |
| Export as VLT... | Packages the theme and every referenced asset into one `.vlt` file. |
| Save current preview as image... | Writes the active layout to a PNG at zoom 1, exactly as the static preview draws it. |
| Test skin in VLC | Saves the theme, finds VLC and starts it in skins2 mode on the saved file. |
| Exit | Closes the window. If there are unsaved changes it asks whether to save first. |

## Edit

| Entry | What it does |
|---|---|
| Undo | Reverts the last change. The label names the change, for example `Undo: Move Button`. Disabled when there is nothing to undo. |
| Redo | Reapplies the last undone change, with the same kind of label. |
| Skin settings | Opens the dialog with the theme name, author, email, webpage, magnet and opacity values. |
| Global variables | Brings the Variables panel forward. |
| Preferences | Look and feel, language, canvas background, checkerboard and toolbar visibility. |
| Duplicate item | Copies the selection and asks for a rename pattern in which `%oldid%` becomes the old id. |
| Delete item | Deletes the focused selection after a confirmation. It acts on the tree that has keyboard focus: item, resource, layout or window. |
| Move selected item up / down / left / right | Moves the selected item by one pixel in that direction. The same as Ctrl and an arrow key. |

## View

| Entry | What it does |
|---|---|
| Zoom in | One zoom step closer, up to 16x. |
| Zoom out | One zoom step back, down to 1x. |
| Fit window | Picks the largest whole zoom that shows the whole layout. |
| Checkerboard | Draws the transparency checkerboard behind the preview. Checked by default. |
| Reset panel layout | Restores the default dock arrangement of the eight panels. |
| Move tool | The pointer selects and drags items. |
| Path tool | The pointer edits slider control points. |
| Dark theme | Switches between the Dark and Light themes. |
| Theme | The full theme list: Light, Dark, IntelliJ, Darcula, Arc, Arc dark and One dark. All of them use the VLC orange accent. |

## Help

| Entry | What it does |
|---|---|
| Documentation | Opens the bundled documentation viewer on F1. See [Documentation viewer](documentation-viewer.md). |
| Online help | Opens the original VLC Skin Editor help pages on videolan.org in your browser. |
| Check for updates | Asks GitHub for the releases now and opens the upgrade dialog when a newer one exists. |
| Check for updates on startup | On by default. Each start checks GitHub in the background and, when a newer release exists, shows the upgrader dialog with the notes of every release you missed, oldest first. |
| About | Version, license and project links. |

## Keyboard shortcuts

| Keys | Action |
|---|---|
| Ctrl+N | New skin. |
| Ctrl+O | Open a skin. |
| Ctrl+S | Save. There is no autosave; this is the only way edits reach the disk. |
| Ctrl+Z | Undo. |
| Ctrl+Y | Redo. |
| Ctrl+D | Duplicate the selection, after the rename pattern prompt. |
| Delete | Delete the selection in the focused tree, after a confirmation. |
| Ctrl+Up, Ctrl+Down | Nudge the selected item up and down by one pixel. |
| Ctrl+Left, Ctrl+Right | Nudge the selected item left and right by one pixel. |
| Ctrl+= | Zoom in. |
| Ctrl+- | Zoom out. |
| Ctrl+0 | Fit window. |
| Ctrl+B | Browse the theme gallery. |
| Ctrl+I | Skin settings. |
| Ctrl+G | Global variables. |
| Shift+Ctrl+V | Export as VLT. |
| Shift+Ctrl+T | Test skin in VLC. |
| F1 | Open the documentation viewer. |

## On macOS

Menu accelerators use the Command key instead of Control: Cmd+N, Cmd+O, Cmd+S, Cmd+Z, Cmd+Y, Cmd+D, Cmd+I, Cmd+G, Cmd+B, Cmd+= for Zoom in, Cmd+- for Zoom out, Cmd+Shift+V and Cmd+Shift+T. Delete item is Cmd+Backspace.

Fit window has no menu accelerator, and the item nudges plus the Ctrl+=, Ctrl+-, Ctrl+0 bindings are literal Control key bindings on every platform, including macOS. The apple.laf.useScreenMenuBar property puts the menu bar at the top of the screen, as a Mac application normally has it.

Next: [Step 1: prepare your artwork](step-1-prepare-artwork.md).
