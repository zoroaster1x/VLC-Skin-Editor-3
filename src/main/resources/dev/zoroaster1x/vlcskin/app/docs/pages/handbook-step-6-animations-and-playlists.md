---
title: Step 6: animations and playlists
section: Making a skin
source: rewritten
---

# Step 6: animations and playlists

## Animate a bitmap

An animated control is one PNG holding every frame, cut into equal horizontal rows. Select the bitmap in the Resources tree and set:

| Field | Meaning |
|---|---|
| Frames | How many rows the image is cut into. `1` means a still image. |
| Frames per second | The playback speed. `0` falls back to 10 fps in the preview. |

Row 0, the top one, is the frame static controls draw. The canvas runs an animation timer at the fastest `fps` in the theme and stops it when no animated bitmap is left. Save current preview as image and the render tools always use frame 0, so exported PNGs never depend on the timer. The `loop` attribute is kept in the file, but the preview always loops.

Any sprite can point at an animated bitmap: button states, slider thumbs, playlist row icons and so on.

## Cut sub bitmaps from a sprite sheet

When one PNG holds several sprites, cut named rectangles instead of exporting separate files:

1. Select the bitmap in the Resources tree.
2. Right-click and pick Add sub bitmap, or press Add sub bitmap at the bottom of the bitmap's Inspector form. The new sub bitmap starts as a 16x16 rectangle at 0,0.
3. Select the sub bitmap and set X, Y, Width and Height in the Inspector, or press Edit visually... and drag the frame over the parent image.
4. The same form sets Frames and Frames per second for an animated sub bitmap.

A sub bitmap behaves like a full resource wherever an image id is accepted. Its id shares the resource namespace, so it must not collide with a bitmap or font id. A rectangle that runs past the parent edge is clipped and a note appears; the validator reports a non positive size as an error.

## Add a playlist

Add Playlist or Playtree from the Items panel. The two are one control with two spellings: Playtree shows folder rows, Playlist is the flat older form. The Inspector adapts to the spelling of the document.

| Field | Meaning |
|---|---|
| Width, Height | The size of the list area. |
| Font | Required. A Font or BitmapFont resource for the rows. |
| Background image | Optional bitmap behind the rows. When set, the stripe colours below are ignored. |
| Item icon, Open folder icon, Closed folder icon | Playtree only. Small images left of leaves and folders. |
| Flat | Playtree only. Hides the tree structure and shows a flat list. |
| Text color | Colour of normal rows. |
| Background color 1, Background color 2 | Alternating stripe colours. |
| Playing color | The colour of the row being played. |
| Selection color | Background of selected rows. |

## Add the playlist slider

Without a scrollbar slider the list cannot be scrolled, and the validator reports a warning. To add one:

1. Select the playlist or playtree in the Items tree.
2. Press Add item and choose Slider. It becomes the list's child, not a free item on the layout.
3. Select the slider and shape its path with the Path tool, exactly like a normal slider.

The Inspector for this slider has no Value list; it shows `Playtree scrolling` with a note instead. Its `points`, `thickness` and `up`, `over` and `down` sprites still apply. The XML `value` is not written for a playlist slider, because its position always follows the playlist scroll position.

## What the preview shows

The editor has no media library, so the playlist preview draws sample rows instead of your files: a closed folder, an open folder, a normal item, a playing item and a selected item for a tree, or the normal, playing and selected rows for a flat list. It also draws the slider child over the rows, and scrolling does nothing. The colours, the font and the row height are what you judge here; the real rows appear in VLC.

Playlist buttons use the `playlist.*` action codes: next, previous, add, remove, sort, load, save and the random, loop and repeat toggles. See [Actions and variables](actions-and-variables.md).

The full attribute tables are in [Playlists and playtrees](playlists-and-playtrees.md) and [Bitmaps and animations](bitmaps-and-animations.md).

Next: [Step 7: validate, test and export](step-7-validate-test-export.md).
