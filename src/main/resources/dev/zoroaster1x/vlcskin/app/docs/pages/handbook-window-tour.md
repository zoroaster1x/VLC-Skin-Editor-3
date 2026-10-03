---
title: Window tour
section: Start here
source: rewritten
---

# Window tour

The desktop window is a menu bar, a toolbar, a dockable panel area and a status bar. This page goes through every part. The menu and keyboard reference is in [Menus and shortcuts](menus-and-shortcuts.md).

## The panels

The editor has eight panels. Six of them surround the canvas; the Problems and Skin XML panels share the bottom strip.

| Panel | What it is for |
|---|---|
| Resources | The theme assets. Bitmaps are listed with their sub bitmaps, then Fonts and bitmap fonts, then Popup menus and ini files under Other. The toolbar has Add bitmap, Add font, Duplicate, Delete and Reload image. |
| Windows and layouts | The window tree. Each window lists its layouts. The toolbar has Add window, Add layout, Move layout up, Move layout down, Duplicate and Delete. Selecting a layout drives the canvas. |
| Items | The controls of the selected layout, nested by container. The toolbar has Add item, Move up, Move down, Duplicate and Delete. |
| Canvas | The live preview with the layout label, the Move and Path tools, zoom and Fit. Without a selection it shows the welcome card. |
| Inspector | Every attribute of whatever is selected: an item, a resource, a sub bitmap, a window, a layout or the theme. |
| Variables | A simulated player: the slider position, sixteen boolean checkboxes and twelve text variables. Changes repaint the preview only. |
| Problems | Validation results with Validate now and Go to element. Errors are red, warnings amber. |
| Skin XML | The generated XML with syntax highlighting. Refresh rebuilds it from the model, Apply XML parses your edits back, Copy copies the whole text. |

The Inspector is one scrolling form rather than a set of tabs. Its header names the selected element, for example `Button: play_btn`, and the form below has a General section plus the sections that belong to the type. The sections are rebuilt when the selection changes, and every field commit is an undo step.

## Dockable panels

All eight panels are dockable. Drag a panel by its title tab:

- Drop it on the edge of another panel to split the area.
- Drop it on the center of another panel to stack them as tabs.
- Drag it out of the window to make it a floating window, and back in to re-dock it.

The arrangement is saved to `layout.xml` next to the settings file, `$XDG_CONFIG_HOME/vlc-skin-studio/settings.json` (or `~/.config/vlc-skin-studio/settings.json` when `XDG_CONFIG_HOME` is unset). The next start restores it.

View > Reset panel layout deletes that saved arrangement and docks the default one:

- Resources on the left, with Windows and layouts below it and Items below that.
- Canvas in the middle, with Problems along the bottom and Skin XML tabbed with Problems.
- Inspector on the right, with Variables below it.

## The toolbar

The toolbar sits under the menu bar and carries, left to right:

| Button | Tooltip |
|---|---|
| Open | Open a skin... |
| Save | Save skin modifications |
| Undo, Redo | Undo, Redo |
| Move, Path | Item moving tool, Slider editing tool |
| Zoom out, Zoom in, Fit | Zoom out, Zoom in, Fit window |
| Validate | Validate the skin |
| Render | Render the preview to PNG |
| Skin settings | Skin settings |
| Global variables | Global variables |

The toolbar can float as its own small window when you drag it off. Its visibility and position are kept between runs; Edit > Preferences has the Show the toolbar checkbox, and dragging a floating toolbar back to the window docks it again.

## The status bar

The left side shows the last message, for example `Opened theme.xml`, `Saved theme.xml`, `Validation finished` or `VLC was not found`. The right side shows:

- the current selection as `Type: id`, for example `Slider: seek`,
- the canvas zoom as `Zoom 2x`,
- `Unsaved changes` in the VLC orange while the document has edits that are not on disk.

The title bar repeats the file name and puts a star in front of it while the document is dirty.

## The canvas and its controls

Under the canvas there is one row of controls:

- The layout label on the left reads `window / layout`, for example `main / main`. It is empty while no layout is selected.
- Move and Path are toggle buttons for the two canvas tools.
- Zoom out and Zoom in step the zoom between 1x and 16x; the label between them reads `Zoom 4x` and so on.
- Fit picks the largest whole zoom at which the whole layout fits the canvas area.

On the canvas itself:

- A click selects the topmost item under the pointer. A click on empty space clears the selection.
- With the Move tool, dragging a selected item moves it; the whole drag is one undo step.
- Arrow keys nudge the selected item one pixel, and repeated nudges of the same item coalesce into one undo step.
- With the Path tool, dragging a yellow control point edits a slider path. Shift-click adds a point, Alt-click removes one. See [Step 5: sliders and backgrounds](step-5-sliders-and-backgrounds.md).
- Ctrl and the mouse wheel zoom in and out.
- The right click menu on an item offers Duplicate, Delete, Bring to front and Fit window. Duplicate asks for a rename pattern first.
- Transparency is drawn over a checkerboard pattern. View > Checkerboard toggles it.
- Animated bitmaps play in place. The preview runs at the fastest bitmap `fps` in the theme and stops when no animated bitmap remains.

## The three trees and their icons

The Resources, Windows and layouts, and Items panels are all trees, and each node carries a small icon for its kind:

- Resources: bitmaps use the image glyph, sub bitmaps the grid glyph, and fonts and the Other resources are grouped under their own parent nodes.
- Windows and layouts: a window glyph for each window, a layout glyph for each layout under it.
- Items: one glyph per control type, so a button, a checkbox, a slider, a text item, a video rectangle and the rest are told apart at a glance. Container nodes can be expanded to show their children.

Double-clicking a node in the Resources or Items trees focuses its element in the Inspector. Right-clicking opens the panel menu, which repeats the toolbar actions and adds element-specific entries such as Add sub bitmap on a bitmap. The Delete key acts on whichever tree has focus.

Next: [Menus and shortcuts](menus-and-shortcuts.md).
