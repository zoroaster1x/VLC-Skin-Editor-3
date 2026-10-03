---
title: Layouts and anchors
section: Appendix: the format explained
source: rewritten
---

# Layouts and anchors

Every item sits at `x`, `y` inside a container. When a layout can be resized, four attribute families decide what happens to each item: its corner anchors, its keep-ratio flags, the layout's size limits, and the resize actions on images. This page is the reference for those rules; the editor fields that expose them are named where they exist.

![Layout coordinates](images/handbook-layout-coordinates.png)

## Coordinates

`x` and `y` are offsets in pixels, relative to the parent container. That container is the layout itself, a `Panel`, or a `Group`. `Group` and `Panel` both add their own offset to their children, and nested panels accumulate. The Layout is always the outermost box.

The origin is the top left corner of the container. Negative values are allowed and place an item partly or fully outside the visible area.

The later format additions let you place elements with named positions and margins instead of raw pixels: `position` (such as `Center` or `NorthWest`), `xmargin`, `ymargin`, `xoffset` and `yoffset`. They apply to windows, panels, video, playlist and playtree elements. The editor preserves them when a theme carries them, but has no dedicated fields; edit them through the Skin XML panel.

## Corner anchoring

Each item ties its top left corner and its bottom right corner to a corner of its parent container. The `lefttop` attribute says where the top left corner is tied, and `rightbottom` says where the bottom right corner is tied.

Possible values are `lefttop`, `leftbottom`, `righttop` and `rightbottom`. The default for both attributes is `lefttop`, which pins the item in place: when the container grows, the item does not move or stretch.

| `lefttop` | `rightbottom` | Effect when the container is resized |
|---|---|---|
| `lefttop` | `lefttop` | Fixed position and size, top left. |
| `righttop` | `righttop` | Fixed position and size, top right. |
| `lefttop` | `righttop` | Stretches horizontally, stays at the top. |
| `leftbottom` | `rightbottom` | Stretches horizontally, stays at the bottom. |
| `lefttop` | `leftbottom` | Stretches vertically, stays at the left. |
| `righttop` | `rightbottom` | Stretches vertically, stays at the right. |
| `lefttop` | `rightbottom` | Stretches in both directions. |

This is the mechanism for building a resizable background from nine slices: corners pinned to their corners, edges stretched along one axis, and the middle stretched in both directions.

![Anchors](images/handbook-anchors.png)

`Group` has no corner attributes of its own, so its children carry their own `lefttop` and `rightbottom`. In the Inspector the two fields are Left top anchor and Right bottom anchor.

## Keep-ratio flags

`xkeepratio` and `ykeepratio` are booleans, default `false`. When one is `true`, the item keeps its relative position along that axis and its size stays constant. If the space to the left of an item is twice the space to its right, it stays that way at every width.

Keep-ratio overrides corner anchoring on the axis it controls. A common use is a control cluster that should stay centered horizontally: set `xkeepratio="true"` and leave the width fixed, while `lefttop` and `rightbottom` still decide the vertical behaviour. The Inspector exposes both as Keep x ratio and Keep y ratio.

## Layout size limits

Resizing stays disabled until the layout declares a range. `minwidth`, `minheight`, `maxwidth` and `maxheight` default to `-1`, which means the initial width or height is used as the limit. Set the range you want, for example `minwidth="320"` and `maxwidth="960"`. The validator warns when a minimum is larger than the matching maximum, because VLC could not satisfy both.

## Resize actions

An `Image` item can act as the resize handle. Its `action` attribute accepts:

| Action | Meaning |
|---|---|
| `move` | Drag the window. |
| `resizeE` | Resize the window horizontally. |
| `resizeS` | Resize the window vertically. |
| `resizeSE` | Resize the window in both directions. |

When the pointer is over an image with a resize action, VLC shows the matching resize cursor. Put the handle in a corner that does not move, usually pinned with both anchors set to that corner. An image with `action="move"` lets the user drag the window by any part of the image, which is how most themes make the whole background draggable. The Inspector offers those four actions in the Click action drop-down.

Resizing only takes effect when the size limits above permit it and the resize actions give the user something to drag.

## Anchor items

An `Anchor` is a different feature from the corner attributes above. It defines a magnetic point, or a curve of magnetic points, that sticks two windows together.

| Attribute | Default | Meaning |
|---|---|---|
| `x`, `y` | `0` | Position of the anchor in its layout. |
| `lefttop` | `lefttop` | Which container corner the anchor follows when the layout is resized. Anchors have no `rightbottom` attribute because they are not resizable. |
| `priority` | required | Which anchor wins when two are attached. Higher priority wins. |
| `points` | `(0,0)` | Control points of the curve followed by the anchor. The default is a single point, a punctual anchor. |
| `range` | `10` | Distance in pixels within which another window's anchor is captured. |

When the anchor of another window enters the range, the two windows attach. Moving the window whose anchor has the higher priority drags the other with it; moving the lower priority window breaks the attachment. Anchors can follow a bezier curve, in which case a punctual anchor from another window can be captured by any point of the curve.

The Inspector edits Points, Priority and Range. The editor draws anchors in the canvas and writes the `range` attribute out; the original editor parsed it but dropped it on save, so old themes that carry it will keep it now.

Next: [Items overview](items-overview.md).
