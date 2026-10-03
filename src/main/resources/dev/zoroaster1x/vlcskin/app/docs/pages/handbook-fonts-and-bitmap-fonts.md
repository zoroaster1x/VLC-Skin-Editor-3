---
title: Fonts and bitmap fonts
section: Making a skin
source: rewritten
---

# Fonts and bitmap fonts

Text items and playlist rows draw with a font resource. There are two kinds: `Font`, which loads a font file, and `BitmapFont`, which loads a Winamp-style sheet of glyphs.

## In the editor

Save the theme first, then use Add font in the Resources panel. The chooser takes `.ttf` and `.otf` and can select several files at once. The editor stores the path relative to the theme, so keep the file beside the theme or in a subfolder.

Select a font in the tree and edit it in the Inspector: ID, File and Size, with a Choose TTF or OTF... button. A BitmapFont shows ID, File and Type instead. Add font creates a `Font`; bitmap fonts and their `type` come from the file being loaded and are edited in place.

The editor's preview uses the JVM's font stack. A skin that depends on a particular platform's hinting can look a pixel or two different from VLC on another machine; that is a known limit of previewing, not a broken theme. When the file is missing or cannot be parsed, the editor falls back to Sans Serif at the same size instead of failing the render. [Step 4](step-4-text-and-variables.md) shows the workflow with a Text item.

## Font

| Attribute | Default | Meaning |
|---|---|---|
| `id` | required | Name used by Text, Playtree and the theme's `tooltipfont`. |
| `file` | required | TTF or OTF file, relative to the folder holding the theme. |
| `size` | `12` | Size in points. |

A font resource loads the file at its declared size and can be referenced by any number of items. The validator reports missing referenced files as warnings, which is the signal to track the file down before shipping.

## defaultfont

`defaultfont` is not a resource in your file. It is a built-in font provided by VLC and the editor; in the preview it is Sans Serif at 12 points. It is the default for the theme's `tooltipfont` and the fallback when a `Text` item has no usable font. You never need to ship it.

If you want a different size or face, add a real `Font` resource and point the items at it. Text fields accept `defaultfont` by name, so the drop-downs include it beside your resources.

## BitmapFont

| Attribute | Default | Meaning |
|---|---|---|
| `id` | required | Name used by items and by `tooltipfont`. |
| `file` | required | The sheet image, relative to the theme folder. |
| `type` | `digits` | The sheet layout: `digits` for numbers and a few symbols, `text` for a full character set. |

A bitmap font draws pre-rendered glyphs, which is how old skins made large counters and clock displays. The trade-off is coverage: any character missing from the sheet prints as a space, so a bitmap font used for stream titles will drop letters it does not contain. The old skins2 guide warns about using a bitmap font for `tooltipfont` for the same reason.

The editor keeps the resource, its type and its references intact, but the preview draws bitmap fonts with a system font instead of the sheet. Check the real look in VLC.

## Where fonts are used

| Place | Attribute |
|---|---|
| Text item | `font` |
| Playlist and playtree | `font` |
| Theme tooltips | `tooltipfont` |

All three accept a `Font` or `BitmapFont` id.

Next: [Actions and variables](actions-and-variables.md).
