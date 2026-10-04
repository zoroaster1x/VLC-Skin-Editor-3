---
title: Step 7: validate, test and export
section: Making a skin
source: rewritten
---

# Step 7: validate, test and export

## Validate from the Problems panel

The Problems panel checks the loaded document and the files next to it; it never talks to VLC. Run it with the Validate now button in the panel, the Validate button on the toolbar, or the CLI command `vlc-skin-studio validate theme.xml`.

Problems carry three severities, coloured in the list: errors in red, warnings in amber and info in the normal text colour. Double-click an entry, or select it and press Go to element, to select the element it points at. An empty list shows No problems found.

Common messages and what to do about them:

| Message | Severity | What it means |
|---|---|---|
| Theme version must be 2.x for skins2 | error | An old 1.x theme is not loaded by the modern interface. |
| The theme has no windows | error | Nothing can be displayed. |
| Window "x" has no layouts | error | The window has nothing to show. |
| Layout "x" needs a positive size | error | Width or height is zero or negative. |
| The item id "x" is used N times | error | VLC resolves an id to the first match; later items are invisible to actions. |
| Item "x" references missing resource "y" | error | The item has nothing to draw. |
| Button "x" up image is missing | error | The normal state has no sprite. |
| File not found: "x" | warning | The path does not resolve next to the theme. |
| Playtree "x" has no slider | warning | The list cannot be scrolled. |
| Color "x" is not #RRGGBB | warning | VLC expects six hex digits and falls back on a default. |

The full message table, with everything the validator checks, is in [Validation and troubleshooting](validation-and-troubleshooting.md). Fix errors before warnings; a missing resource usually explains several smaller oddities.

## Test skin in VLC

Press Shift+Ctrl+T or choose File > Test skin in VLC. The editor:

1. Saves the theme. If it has no file yet, the save dialog appears first.
2. Looks for VLC: a native install, a Flatpak export or `vlc` on the PATH.
3. Starts it with

   ```bash
   vlc -I skins2 --skins2-last=path/to/theme.xml
   ```

If no VLC is found, the editor shows the command to run yourself. VLC reads the theme when it loads it and does not watch the file, so after changing the theme in the editor, reload the skin in VLC or start VLC again.

The MCP tool `test_in_vlc` goes one step further: it exports a `.vlt`, copies it into VLC's skins folder and starts VLC on the installed copy. That is the closest thing to what a user downloading the theme will see, and it makes the theme appear in VLC's Change skin dialog afterwards.

## Save a preview image

File > Save current preview as image writes the active layout to a PNG at zoom 1. It is a static render: animation uses frame 0, no tooltips or video are drawn, exactly like the canvas without a running timer. Use it for release notes, bug reports or review.

## Export as VLT

Press Shift+Ctrl+V or choose File > Export as VLT. Pick a target and the editor packages the theme into one archive:

- `theme.xml` first, then every referenced bitmap, font, bitmap font, ini file and Include file.
- Paths keep the structure they have on disk, so an `img/` subfolder stays a subfolder inside the package.
- Files that are referenced but missing are skipped, and the status bar then lists them. Run the validator first to catch those.

The format, the import rules and the archive layout are in [VLT archives](vlt-archives.md).

## Where VLC keeps skins

To install a theme by hand, put the `.vlt` in VLC's skins folder:

| Platform | Folder |
|---|---|
| Linux | `~/.local/share/vlc/skins2` |
| Flatpak | `~/.var/app/org.videolan.VLC/data/vlc/skins2` |
| Windows | `VLC\skins`, under the VLC install folder |
| macOS | `~/Library/Application Support/org.videolan.vlc/skins2` |

VLC's Change skin dialog lists both `.vlt` and `.xml`. Changing the interface to Use custom skin needs a VLC restart before it becomes visible. VLC's own user documentation has stated that skins are not supported on macOS at all; the editor still uses the folder above when it launches VLC there.

Next: [Common problems](common-problems.md), or the format appendix starting at [Format basics](format-basics.md).
