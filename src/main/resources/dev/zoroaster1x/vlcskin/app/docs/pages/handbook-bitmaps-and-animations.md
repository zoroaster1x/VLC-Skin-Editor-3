---
title: Bitmaps and animations
section: Making a skin
source: rewritten
---

# Bitmaps and animations

Every visible control draws a bitmap resource. A bitmap is one image file, optionally split into frames and optionally cut into named sub bitmaps for reuse.

## In the editor

Work in the Resources panel:

1. Save the theme first, then press Add bitmap. The chooser takes PNG and can select several files at once.
2. Select a bitmap to edit its ID, File, Alpha color, Frames and Frames per second in the Inspector. Choose PNG... reopens the file chooser.
3. Right-click a bitmap and pick Add sub bitmap, or press Add sub bitmap at the bottom of the bitmap's form. Select the sub bitmap to set X, Y, Width, Height, Frames and FPS, or press Edit visually... to drag the rectangle over the parent image.
4. Reload image drops the picture cache and reads every file from disk again. Use it after editing a PNG in another program.

The editor stores the file path relative to the theme folder. Keep the images beside the theme or in a subfolder of it.

## Attributes

| Attribute | Default | Meaning |
|---|---|---|
| `id` | required | Name used by items. Bitmap and sub bitmap ids share one namespace. |
| `file` | required | Image path, relative to the folder holding the theme. |
| `alphacolor` | required in the DTD; editor writes `#FF00FF` when absent | The key color. |
| `nbframes` | `1` | Number of animation frames stacked vertically in the file. |
| `fps` | `0` | Frames per second for the animation. |
| `loop` | `0` | Number of loops before the animation stops; `0` means forever. The editor preserves it but does not model or simulate it. |

PNG is the format the importer offers, and it is the format skins have always used; VLC itself accepts more image formats, but not every machine decodes them identically.

## Alphacolor keying

The `alphacolor` attribute is a key color in `#RRGGBB` form. Every pixel whose red, green and blue values match that color becomes fully transparent, no matter what alpha it carried. Every other pixel keeps its alpha, so soft edges and translucent highlights survive.

![Alphacolor](images/handbook-alphacolor.png)

Pick a color that does not appear in the artwork. The DTD marks `alphacolor` as required; when a file omits it, the editor keys out `#FF00FF` and writes the attribute back. The PNG's own transparency mask is respected as well, so both transparent pixels and keyed pixels stay invisible.

The preview composites with straight alpha, the same way VLC's `FileBitmap` decoding does.

## Frames and animation

`nbframes` cuts the image into equal horizontal strips, top to bottom. Frame 0 is the first strip, and static controls always draw frame 0. This is how a sprite sheet or a film strip becomes one resource.

![Bitmap frames](images/handbook-bitmap-frames.png)

A bitmap with more than one frame animates at `fps` frames per second. When `fps` is `0`, the editor preview falls back to 10 frames per second. The canvas runs an animation timer while the theme contains any animated bitmap and stops it when none do. Static rendering, including Save current preview as image and the MCP `render_layout`, always uses tick 0, so exported PNGs never depend on the timer. The `loop` attribute exists in the format, but the editor keeps it only as a preserved attribute and the preview always loops.

Any item sprite can point at an animated bitmap: button states, slider thumbs, playtree row icons, and so on.

## Sub bitmaps

A `SubBitmap` is a named rectangle cut from its parent bitmap. It is a full resource for every control that can reference an image, which is how one sprite sheet serves many controls without extra files.

| Attribute | Default | Meaning |
|---|---|---|
| `id` | required | Name used by items. Must not collide with any other resource id. |
| `x`, `y` | required | Top left corner inside the parent bitmap. |
| `width`, `height` | required | Size of the cut rectangle. |
| `nbframes`, `fps` | `1`, `0` | Kept in the format and in the editor model. The preview draws one frame for a sub bitmap. |

When the rectangle runs past the edge of the parent, the editor clips it and warns, and the validator reports a non positive size as an error.

## The picture cache

The editor decodes each bitmap once and keeps it in memory, keyed by resource id and frame. Editing a resource or a sub bitmap drops the cached copy, and the Resources panel has Reload image (also available over MCP as `reload_images`) for the case where the file on disk changed. Without that step a theme that was edited in another program keeps showing the old pixels.

Next: [Playlists and playtrees](playlists-and-playtrees.md).
