# Desktop UI

The desktop window is one editor surface among several: the terminal UI, the
CLI and the MCP server drive the same document model through `EditorService`,
so nothing in the window is a separate implementation. Screenshots live in the
[README](../README.md#screenshots).

## Panels

| Panel | What it does |
|---|---|
| Resources | bitmaps with sub bitmaps, fonts, bitmap fonts, popup menus, ini files; add bitmap/font, reload images |
| Windows and layouts | windows and their layouts; add, duplicate, delete, reorder |
| Items | the item tree of the active layout; add any control, duplicate, delete, reorder |
| Canvas | the live preview; click to select the topmost item, drag to move, path tool edits slider points, ctrl+wheel zooms |
| Inspector | every attribute of the selected item, resource, window or layout, committed through undo |
| Variables | simulates player state: booleans such as `vlc.isPlaying`, text variables such as `$N`, slider position |
| Problems | validation results; double click jumps to the element |
| Skin XML | the generated XML with syntax highlighting, editable with an Apply step |

Panels are dockable; drag them anywhere, float them, or restore the default
arrangement from View > Reset panel layout. The toolbar can float as its own
window.

## Shortcuts

| Keys | Action |
|---|---|
| `Ctrl+N` | New skin |
| `Ctrl+O` | Open |
| `Ctrl+S` | Save |
| `Ctrl+Z` / `Ctrl+Y` | Undo / redo |
| `Ctrl+Up/Down/Left/Right` | Nudge the selected item one pixel |
| `Delete` | Remove the focused selection, after a confirmation |
| `Ctrl+D` | Duplicate the selection, after the rename pattern prompt |
| `Ctrl+=` / `Ctrl+-` / `Ctrl+0` | Zoom in, zoom out, fit window |
| `Ctrl+B` | Browse the theme gallery |
| `Ctrl+I` | Skin settings |
| `Ctrl+G` | Global variables |
| `F1` | Documentation |

## Themes

Light, Dark, IntelliJ, Darcula, Arc, Arc dark and One dark. The accent is VLC
orange everywhere; every theme is first class, and the canvas backdrop follows
the theme unless Preferences pins it to Light or Dark.
