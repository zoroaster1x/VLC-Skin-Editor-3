---
title: Common problems
section: Troubleshooting
source: rewritten
---

# Common problems

Seven failures that account for most confused afternoons, and what to check for each.

## A bitmap does not show

Work through these in order:

1. The item's image field names an existing bitmap or sub bitmap id. The validator reports an unresolved reference as an error, so press Validate now first.
2. The file exists next to the theme with the path exactly as written. The validator reports a missing file as a warning.
3. Alphacolor keying did not remove it. Every pixel whose RGB equals the bitmap's alphacolor becomes fully transparent, whatever its alpha. Check the Alpha color field on the bitmap; see [Bitmaps and animations](bitmaps-and-animations.md).
4. Another item covers it. The canvas selects the topmost item, so click where you expect your item and check which id the Inspector shows.
5. Its Visible expression is true. A name the editor does not know evaluates to false, which hides the item.
6. For an animated bitmap, only frame 0 shows on static draws. Make sure the frame you want is the first row.
7. If you edited the PNG outside the editor, use Reload image in the Resources panel. The picture cache does not watch the disk.

## A slider does not move

The XML `value` attribute names the variable VLC binds the slider to; it is not a stored position, and moving the value field does not move the thumb in the editor.

- The preview draws the thumb at the single Slider position in the Variables panel, which starts at 50. Drag it and the thumb follows.
- Inside a playlist or playtree the slider has no value at all: it follows the playlist scroll position, and the preview has no playlist to scroll, so the thumb stays where it is.
- A malformed Points field makes the path undefined; the validator reports Invalid points as an error. Shape the path with the Path tool or type `(x,y),(x,y)` pairs.
- A missing thumb is reported as an error, and a non positive thickness as a warning.

## The preview looks different from VLC

The preview is a renderer, not a player. These are the usual differences:

- Actions do not run in the preview. A `vlc.play()` button draws its states, and the click does nothing.
- Video is a black rectangle; the preview has no media pipeline.
- Tooltips are not drawn; only VLC draws them.
- A BitmapFont draws with a system font in the preview, not with the glyph sheet.
- Fonts use the JVM font stack, so a machine with different fonts or hinting can differ by a pixel or two from VLC.
- An animated bitmap with `fps` of 0 plays at 10 fps in the preview. The `loop` attribute is preserved but the preview always loops.
- Playlist rows are VLC's idle tree nodes, not your library, and scrolling does nothing.
- Window position and visibility are hints for the first load; VLC remembers its own window positions between runs, so a fresh position may need VLC's state cleared.

When the difference matters, use File > Test skin in VLC and judge there.

## The validator reports duplicate ids

Duplicate id messages appear in several forms:

| Message | What it covers |
|---|---|
| The item id "x" is used N times | Item ids share one namespace across every layout and window. |
| Duplicate resource id "x" | Bitmaps, fonts, bitmap fonts, popup menus and ini files. |
| Duplicate SubBitmap id "x" | Sub bitmap ids live in the resource namespace too. |
| Duplicate window id "x" | Window actions and `windowId.*` expressions cannot tell the windows apart. |
| Duplicate layout id "x" in window "y" | Layout ids are unique inside their window only, so the same name in another window is fine. |

VLC resolves an id to the first match. The theme still loads and draws, but later elements with the same id are invisible to actions, expressions and lookups, which is a silent defect. Renaming the later element is usually safe unless an action targets that id. The conformance sweep of the official gallery found 80 duplicate item ids and 25 duplicate resource ids, so old themes live with this all the time.

## VLC does not start in skins mode

File > Test skin in VLC saves the theme and looks for a native VLC, a Flatpak export or `vlc` on the PATH. If none is found, the editor shows the command to run yourself:

```bash
vlc -I skins2 --skins2-last=path/to/theme.xml
```

Check these in order:

1. Run that command from a terminal. If VLC itself starts but the skin does not, the failure is in the theme; run Validate now and fix the errors first.
2. Make sure the VLC build has the skins2 module. The error message from VLC names the missing module.
3. If VLC starts with its normal interface, the interface setting may still be pointing elsewhere. Tools > Preferences > Interface > Use custom skin needs a VLC restart before it shows.
4. Under Flatpak, VLC can only open files its sandbox can see. The MCP tool `test_in_vlc` copies the `.vlt` into VLC's skins folder and launches that installed copy, which is the most reliable route; see [VLT archives](vlt-archives.md) for the folder.
5. VLC's own user documentation has stated that skins are not supported on macOS; the editor still offers the launch and computes the macOS skins folder.

## Fonts look different

- The preview uses the JVM's fonts; VLC uses its own platform stack. A font that is not installed falls back to Sans Serif at the same size rather than failing.
- A BitmapFont resource is drawn as a system font in the preview, so its sheet shape only appears in VLC.
- A font resource has a File and a Size. Check both, and keep the file next to the theme or in a subfolder so the path resolves.
- For a pixel exact result, compare on the machine where the skin will be used, not only in the preview.

## Changes were lost after a crash

The editor has no autosave and no recovery file. Edits live in memory until you save:

- Ctrl+S and File > Save write the theme. Watch the star in the title bar and the Unsaved changes note in the status bar.
- File > Test skin in VLC saves first, so testing preserves your work.
- File > Export as VLT packages the current in-memory state without writing the `.xml` back. Save first if the XML should match the archive.
- Closing the window asks whether to save when the document is dirty, and the Exit entry does the same.
- Window size, panel arrangement, recent files, theme and preferences are stored separately as they change, and the settings writer keeps a backup, so those survive a crash.

Next: [FAQ](faq.md) for shorter questions, or [Validation and troubleshooting](validation-and-troubleshooting.md) for the full validator reference.
