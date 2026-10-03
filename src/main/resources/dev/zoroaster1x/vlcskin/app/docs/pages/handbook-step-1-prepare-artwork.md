---
title: Step 1: prepare your artwork
section: Making a skin
source: rewritten
---

# Step 1: prepare your artwork

Every visible control draws a PNG. Before touching the editor, settle what the theme looks like and export the images it needs. The editor consumes finished images; it does not draw them.

## Plan the layout

Sketch each window on paper or in any image tool and note the pixel size. The layout width and height in the editor are the visible canvas, and they usually match the background image exactly. For each control, decide:

- Where it sits, in pixels from the top left corner of its container.
- What states it has. A button wants up, over and down images; a checkbox wants a sprite triple for each of its two states; a slider wants a thumb image, and optionally a background strip.
- Whether it animates. An animated bitmap needs its frames stacked in one file.
- Where the draggable and resizable areas are. A background image with the move action is how the user drags the window.

Keep windows small until the theme works. The built in examples are 320x140 and 420x220; many skins are between 200 and 400 pixels wide.

## Design the bitmaps

Use any graphics editor that exports PNG. A few rules keep the result predictable:

- Draw at the final size. The editor has no scaling step, so a button meant to be 28x28 is exported at 28x28.
- Keep each sprite on its own layer and export it on its own, or lay out a sprite sheet deliberately.
- Keep the background colour of the canvas transparent when the theme should show through it. PNG alpha is kept by the decoder.
- Animate by stacking frames, not by exporting many files. A multi frame bitmap is one PNG cut into equal horizontal rows, top to bottom.

## Slice sprites

A control state is a sprite:

- Button: `up`, `over` and `down`. Over and down may be left empty, in which case VLC draws `up`.
- Checkbox: `up1`, `over1`, `down1` for state 1 and `up2`, `over2`, `down2` for state 2.
- Slider: the thumb, plus optional hover and clicked thumbs.
- Playlist rows and folder icons: optional small images.

A sprite sheet with several sprites in one PNG is fine. Cut named sub bitmaps out of it in the editor instead of exporting one file per sprite; each sub bitmap is a full resource for any control. See [Bitmaps and animations](bitmaps-and-animations.md) for sub bitmap fields.

## Pick an alphacolor

Each bitmap has an alphacolor, a key colour in `#RRGGBB` form. Every pixel whose red, green and blue values match it exactly is drawn fully transparent, whatever its alpha. Pick a colour that does not appear in the artwork, for example `#FF00FF`. If a theme omits the key, the editor keys `#FF00FF` and writes the attribute back. The PNG's own alpha is respected as well.

## Export the PNGs

- Put the files next to `theme.xml`, or in a subfolder such as `img/`. The paths stored in the theme are relative to the theme folder.
- Save the theme first, then add the images in the Resources panel. The editor refuses Add bitmap on a document that has no file yet, because there is no folder to compute a relative path against.
- Name files after their role, such as `play_up.png`, so the resource ids read well in the trees.

## Common sizes

These are the sizes the built in examples use, useful as a starting point rather than a rule:

| Piece | Example size |
|---|---|
| Player bar window | 320x140 with a 320x140 background |
| Control panel window | 420x220 with a 420x220 background |
| Transport button | 28x28 |
| Close button | 18x18 |
| Slider thumb | 9x9 to 11x11 dot |
| Slider track | 272x8 or 80x6 |
| Video area | 400x150 inside the 420x220 panel |

A slider background strip is one frame per pixel of travel, so its length follows the slider, not a fixed size. The wizard in the editor generates that strip; see [Step 5: sliders and backgrounds](step-5-sliders-and-backgrounds.md).

Next: [Step 2: start a new theme](step-2-new-theme.md).
