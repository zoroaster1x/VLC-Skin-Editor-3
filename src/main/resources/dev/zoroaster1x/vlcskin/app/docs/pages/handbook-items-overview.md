---
title: Items overview
section: Appendix: the format explained
source: rewritten
---

# Items overview

Every control in a layout is one of thirteen elements. They share a set of common attributes and differ in what they draw and which resources they use. This page is the format-level catalog; for adding and placing them in the editor see [Step 3](step-3-add-and-place-controls.md).

The Items panel's Add item menu carries the same names, minus `SliderBackground`, which is enabled from a slider's Inspector.

| Element | Purpose | Details |
|---|---|---|
| `Anchor` | A magnetic point or curve that sticks windows together. | [Layouts and anchors](layouts-and-anchors.md) |
| `Button` | A clickable three state image. | [Buttons, checkboxes and images](buttons-checkboxes-and-images.md) |
| `Checkbox` | A two state control with a sprite triple per state. | [Buttons, checkboxes and images](buttons-checkboxes-and-images.md) |
| `Group` | An invisible offset container; deprecated but common in old themes. | [Theme structure](theme-structure.md) |
| `Image` | A static image, optionally clickable or used as a move or resize handle. | [Buttons, checkboxes and images](buttons-checkboxes-and-images.md) |
| `Panel` | A container that adds an offset and is the resize reference for its children. | [Theme structure](theme-structure.md) |
| `Playlist` | The playlist control in its older flat syntax. | [Playlists and playtrees](playlists-and-playtrees.md) |
| `Playtree` | The playlist control with folder rows and a `flat` option. | [Playlists and playtrees](playlists-and-playtrees.md) |
| `RadialSlider` | A rotating knob built from a strip of images. | [Sliders](sliders.md) |
| `Slider` | A control that reads and writes a percentage variable along a bezier path. | [Sliders](sliders.md) |
| `SliderBackground` | The visible fill behind a slider, cut from one bitmap grid. | [Slider backgrounds](slider-backgrounds.md) |
| `Text` | A line of text drawn in a font resource, with dynamic variables. | [Text items](text-items.md) |
| `Video` | The rectangle where VLC draws video output. | This page, below. |

## Common attributes

Most items share the following attributes. The exceptions are noted after the table. In the Inspector these live in the General section at the top of every item.

| Attribute | Default | Meaning |
|---|---|---|
| `id` | `none` | Item name. One namespace for all items in the theme. See [Theme structure](theme-structure.md). |
| `visible` | `true` | A boolean expression, not just `true` or `false`. See [Actions and variables](actions-and-variables.md). |
| `x`, `y` | `0` | Offset relative to the parent container. |
| `lefttop`, `rightbottom` | `lefttop` | Corner anchors for resizing. See [Layouts and anchors](layouts-and-anchors.md). |
| `xkeepratio`, `ykeepratio` | `false` | Keep the relative position, with a constant size. |
| `help` | empty | Help text. The variable `$H` expands to it while the pointer is over the item. |

`Group` is the exception to the shared set: it has only `id`, `x` and `y`, because it never draws and never resizes. `Anchor` has no `rightbottom` because it is not resizable, and it uses `lefttop` just to choose which container corner it follows. Some items add an optional width or height; `Text`, for example, leaves `width` at `0` and computes its width from the drawn text.

## Containers and children

Only three relationships exist:

- `Group` and `Panel` can hold any items and can nest.
- `Slider` can hold one `SliderBackground`.
- `Playlist` and `Playtree` can hold one `Slider`.

A `Group` is invisible; it only shifts the coordinates of its content. A `Panel` does the same and also acts as the reference box for its children's resize anchors, which makes it the tool for keeping a cluster of controls in place while the window grows. The editor enforces these rules when you drag nodes in the Items tree: a slider refuses children, a playlist slider and a slider background cannot be dragged, and a node can never be dropped into its own subtree.

## Video

`Video` marks the area where VLC renders video output. It draws a black rectangle in the editor preview, because the preview has no media pipeline.

| Attribute | Default | Meaning |
|---|---|---|
| `width`, `height` | `0` | Initial size of the video rectangle. |
| `autoresize` | `true` | Let the layout grow when the video is larger than the rectangle. |

The Inspector edits Width, Height and Auto resize. The element also accepts `position`, `xoffset`, `yoffset`, `xmargin` and `ymargin` from the later format additions; the editor preserves them. Video rendering is VLC runtime behaviour, and switching layouts with video on screen has known quirks in VLC itself. The editor keeps the element and its attributes intact either way.

Next: [Popup menus and ini files](popupmenus-and-inifiles.md).
