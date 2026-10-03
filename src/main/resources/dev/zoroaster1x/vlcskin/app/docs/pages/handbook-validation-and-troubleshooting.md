---
title: Validation and troubleshooting
section: Troubleshooting
source: rewritten
---

# Validation and troubleshooting

This page is the validator reference: how to run it and what every message means. For the failures people actually hit, start with [Common problems](common-problems.md); this page is where you look up the exact wording.

## Running the validator

The editor checks a theme against the rules VLC cares about: ids, references, sizes, colours and files. It never talks to VLC to do this; everything is checked against the loaded document and the files next to it.

- In the window: the Problems panel with Validate now, or the Validate button on the toolbar.
- On the command line: `vlc-skin-studio validate theme.xml`. It prints one line per issue and a count, and exits 1 when there are errors.
- Over MCP: the `validate_skin` tool.

Problems come in three severities: errors, warnings and info. The list colours errors red and warnings amber, and an empty list shows No problems found. Double-click an entry, or select it and press Go to element, to select the element it points at.

## The messages

| Message | Severity | What it means for VLC |
|---|---|---|
| Theme version must be 2.x for skins2 | error | A `1.x` theme is not loaded by the modern skins2 interface. |
| The theme has no windows | error | Nothing can be displayed. |
| Window "x" has no layouts | error | VLC has nothing to show for that window. |
| Layout "x" needs a positive size | error | A zero or negative width or height is not drawable. |
| The item id "x" is used N times | error | VLC resolves an id to the first match, so later items with the same id are never found by actions or expressions. |
| Duplicate resource id "x" | error | Same first-match rule; only the first resource with that id is used. |
| Duplicate SubBitmap id "x" | error | Same, for sub bitmaps. |
| SubBitmap id "x" is already used by a resource | error | Sub bitmap ids share the resource namespace. |
| Duplicate window id "x" | error | Window actions and `windowId.*` expressions cannot tell the windows apart. |
| Duplicate layout id "x" in window "y" | error | `setLayout` cannot tell the layouts apart. |
| Item "x" references missing resource "y" | error | The item has nothing to draw and VLC logs a lookup failure. |
| Button "x" up image is missing | error | The button cannot draw its normal state. |
| Checkbox "x" up images are missing | error | One or both states have no normal sprite. |
| Image "x" image is missing | error | The image has nothing to draw. |
| Slider "x" up image is missing | error | The slider has no thumb. |
| Slider "x" background image is missing | error | The background child is set but cannot load. |
| Font "x" needs a positive size | error | A zero or negative point size is not renderable. |
| Bitmap "x" needs at least one frame | error | `nbframes` below 1 makes no sense; the parser falls back to 1. |
| SubBitmap "x" has no size | error | A zero or negative cut is not drawable. |
| Bitmap, Font, BitmapFont or IniFile "x" has no file | error | The resource has no file path at all. |
| Include has no file | error | An include element has no path. |
| Checkbox "x" has no state condition | error | Without a `state` expression the box cannot choose a sprite set. |
| Invalid points: ... | error | `points` cannot be parsed, so the path is undefined. |
| The points attribute is empty | error | Same, with no text to parse. |
| Layout "x" minwidth is larger than maxwidth | warning | No size can satisfy both limits. The same check exists for heights. |
| File not found: "x" | warning | The file path does not resolve next to the theme; the item will not draw. |
| Include file not found: "x" | warning | The included skin file is missing. |
| Panel "x" has a non positive size | warning | The panel has no visible area. |
| Video "x" has a non positive size | warning | The video rectangle is empty. |
| Playtree "x" has no slider | warning | The list cannot be scrolled. |
| Radial slider "x" needs images | warning | `nbimages` is not positive, so no frame can be chosen. |
| Slider "x" thickness should be positive | warning | A non positive thickness removes the click target around the path. |
| Text "x" width should not be negative | warning | A negative width is treated as auto, which may not be what the author meant. |
| Color "x" is not #RRGGBB | warning | VLC expects six hex digits; anything else, including `none` or an empty string, can fall back to a default. |
| Image "x" has an unknown resize mode: y | warning | The `resize` value is not `mosaic`, `scale` or `scale2`. |
| Theme alpha should be between 1 and 255 | warning | The opacity attribute is outside its range. The same check exists for `movealpha`. |
| Theme magnet should not be negative | warning | A negative snap distance is meaningless. |
| ThemeInfo has no name | info | Cosmetic; VLC does not display theme metadata anyway. |

## What the gallery sweep found

The conformance sweep over the official VideoLAN gallery plus the two themes VLC ships imported, validated and rendered all 123 themes. The summary:

| Number | Value |
|---|---|
| Themes | 123 |
| Imported and rendered | 123 |
| Layout items seen | 12336 |
| Validation issues | 550, of which 133 errors |
| Render time | median 959 ms, maximum 2249 ms |

The most common messages were:

| Count | Severity | Message |
|---|---|---|
| 83 | warning | Non positive size |
| 76 | error | Duplicate item id |
| 65 | warning | Missing referenced file |
| 27 | error | Duplicate resource id |
| 21 | error | Missing resource reference |
| 14 | warning | Playtree without slider |
| 5 | warning | Colors that are not `#RRGGBB`, including `none` and empty strings |
| 4 | warning | Slider thickness should be positive |

Two things are worth knowing about that list. First, none of these stopped a theme from loading or rendering; they are defects the old skins carry, not import failures. Second, duplicate ids are the most common problem in real themes because themes were often assembled by copying layouts, and VLC quietly resolves each id to the first match. The control that loses the lookup simply never updates.

Next: [Common problems](common-problems.md) for fixes, or the [format appendix](format-basics.md) for what the validator is checking against.
