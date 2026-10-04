---
title: Slider backgrounds
section: Making a skin
source: rewritten
---

# Slider backgrounds

A slider can look like a filled progress bar instead of a bare thumb. The `SliderBackground` child does that: it stores one bitmap cut into a grid of frames, one frame per fill level.

## In the editor

Select a slider and open the Slider background section of the Inspector:

1. Tick Enabled to add the background child.
2. Press Generate background strip... to open the wizard, or set the grid fields by hand.
3. The wizard's inputs are Width and height, Left and right margin, Top and bottom margin, Direction (Left to right or Bottom to top), the Background, Start edge, Middle (required), End edge and Overlay images, and the Tile background and Tile middle checkboxes.
4. Generate and use writes the strip next to the theme as `<id>_bg.png`, registers a bitmap resource for it, and sets the grid fields: a horizontal slider gets `nbhoriz="1"` with one row per frame, a vertical slider gets one column per frame with `nbvert="1"`, and both paddings are zero. Save the theme first so the file lands beside it.

Do not add a SliderBackground from the Items panel; it is not in the Add item menu. It only exists inside a slider.

## The child element

| Attribute | Default | Meaning |
|---|---|---|
| `id` | `none` | Name of the background element inside its slider. |
| `image` | required | Bitmap resource holding the whole grid. |
| `nbhoriz` | `1` | Number of frames across. |
| `nbvert` | `1` | Number of frames down. |
| `padhoriz` | `0` | Unused pixel columns between frames, horizontally. |
| `padvert` | `0` | Unused pixel rows between frames, vertically. |

`SliderBackground` can only appear inside a `Slider`, and a slider holds at most one.

## The frame formula

The grid holds `nbhoriz * nbvert` frames, called fields. For a slider value between 0 and 1, VLC picks:

```
fields = nbhoriz * nbvert
n      = (int)(value * (fields - 1))    clamped to 0 .. fields - 1
fx     = n % nbhoriz
fy     = n / nbhoriz
```

VLC uses the last frame index (`CtrlSliderBg::onUpdate`), so a full slider
shows the last frame and a value of 0 shows the first.

Frames fill left to right, then top to bottom. The frame size subtracts the padding between frames:

```
frameWidth  = (imageWidth  - padhoriz * (nbhoriz - 1)) / nbhoriz
frameHeight = (imageHeight - padvert  * (nbvert  - 1)) / nbvert
```

The frame origin is `fx * (frameWidth + padhoriz)`, `fy * (frameHeight + padvert)`.

`image` may name a sub bitmap; VLC cuts the frames from that rectangle, never from the whole parent sheet.

![Slider background](images/handbook-slider-background.png)

For example, a 200 by 1010 bitmap with `nbhoriz="1"` and `nbvert="101"` holds 101 frames of 200 by 10 with no padding, one for every percent. With `nbhoriz="10"`, `nbvert="10"` and `padvert="1"` it holds 100 frames in a 10 by 10 grid, each 20 pixels wide and `(imageHeight - 9) / 10` pixels tall.

A single frame is valid, and a missing or empty image simply draws nothing. The editor guards against a zero frame size.

Note that the generator and the frame formula have to agree. If you paint the strip yourself, count carefully: the number of frames is `nbhoriz * nbvert`, and the fill frame `n` is the one VLC shows when `(int)(value * (fields - 1))` equals `n`.

The same generator is available over MCP as `generate_slider_background`, with parameters for the same inputs plus a slider id and an output path. It writes the PNG next to the theme, adds the bitmap resource and points the slider at it.

Next: [Bitmaps and animations](bitmaps-and-animations.md).
