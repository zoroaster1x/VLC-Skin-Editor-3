---
title: Step 2: start a new theme
section: Making a skin
source: rewritten
---

# Step 2: start a new theme

## File > New

Press Ctrl+N or choose File > New. The save dialog asks where the theme file should live. (If the theme was started from the welcome card's New skin button it is still untitled; Ctrl+S opens the same dialog.)

- The editor appends `.xml` when the name has no extension.
- If the file exists you are asked whether to replace it.
- The new file is written immediately, so the path on disk and the path in the editor already agree. That matters for the next step, because Add bitmap needs a theme folder to compute relative paths against.

The new document contains one window named `main` with one layout named `main` at 320x140. Open Skin settings to replace the `Untitled` name before exporting.

## Skin settings

Ctrl+I, the toolbar button, or Edit > Skin settings opens the dialog:

| Field | Meaning |
|---|---|
| Name | Theme name stored in ThemeInfo. VLC stores it but does not display it. |
| Author, Email, Webpage | Free text metadata for the same block. |
| Magnet | Snapping distance in pixels. `0` disables snapping. |
| Opacity | Window opacity from 1 (nearly invisible) to 255 (opaque) while a window is not being moved. VLC applies it only when skin transparency is enabled. |
| Opacity while moving | The same value for the moment a window is dragged. |

The Help button in the dialog opens the theme page of the original online help in your browser.

## Add windows and layouts

Work in the Windows and layouts panel:

| Action | Result |
|---|---|
| Add window | Adds a window named `Window #n` with one 320x140 layout. |
| Add layout | Adds a layout to the selected window, sized like that window's first layout. |
| Move layout up / Move layout down | Reorders the layouts inside their window. |
| Duplicate | Copies the window or layout after the `%oldid%` rename prompt. |
| Delete | Removes the selection after a confirmation. The theme always keeps at least one window, and a window keeps at least one layout. |

A theme can hold several windows, for example a main window and a separate playlist window. A window can hold several layouts, for example `main` and `compact`; VLC switches between them with a `setLayout` action. The last layout in the list is the one VLC shows first, and the Inspector says so in a note when you select it.

## Choose the sizes

Select a layout and use the Inspector:

| Field | Meaning |
|---|---|
| Width, Height | The visible size of the layout. Match them to the background bitmap. |
| Minimum width, Minimum height | Smallest size when the layout can be resized. `-1` uses the initial width or height as the minimum. |
| Maximum width, Maximum height | Largest size. `-1` uses the initial width or height as the maximum. |

Keep every layout of one window at the same size. VLC switches layouts in place, so different sizes make the window jump and the artwork glitch.

## What anchors do when the layout resizes

A layout only becomes resizable when three things line up:

1. The size limits above declare a range.
2. An image acts as the resize handle through its Click action: `resizeE` (width), `resizeS` (height) or `resizeSE` (both).
3. The items declare how they follow the edges.

By default every item has both corner anchors set to `lefttop`, so it stays exactly where it is when the layout grows. The Inspector exposes the four corner choices and the Keep x ratio and Keep y ratio checkboxes on the selected item. Nine slice backgrounds are the classic use: corners pinned, edges stretched along one axis, middle stretched in both directions. The full walkthrough is in [Layouts and anchors](layouts-and-anchors.md).

## Save the .xml

Ctrl+S writes the theme. Save as a new name with File > Save as.

The writer stores a canonical attribute order and omits attributes that sit at their default values, so a saved file is shorter than a hand written one. Anything the editor does not recognize, such as attributes from newer format additions, is preserved and written back untouched.

Do this before adding artwork: [Step 1](step-1-prepare-artwork.md) explains why Add bitmap wants a saved theme. Then continue with [Step 3: add and place controls](step-3-add-and-place-controls.md).
