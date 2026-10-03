---
title: Theme structure
section: Appendix: the format explained
source: rewritten
---

# Theme structure

A theme is a tree of four levels. The `Theme` element is the root. It holds metadata, resources and windows. A `Window` holds one or more `Layout` elements, and a `Layout` holds the controls, called items. This page is the reference for each level; a working example is in [Format basics](format-basics.md).

![Theme structure](images/handbook-theme-structure.png)

## Theme

`Theme` carries the global settings and owns every resource and window in the file.

| Attribute | Default | Meaning |
|---|---|---|
| `version` | required | Format version. Must be `2.x` for skins2. |
| `tooltipfont` | `defaultfont` | Font or bitmap font used for tooltips. |
| `magnet` | `15` | Distance in pixels at which windows stick to screen edges. `0` disables it. |
| `alpha` | `255` | Window opacity while the user is not moving them, from `1` (nearly invisible) to `255` (opaque). VLC applies it only when skin transparency is enabled. |
| `movealpha` | `255` | Window opacity while a window is being moved. |

The children of `Theme`, in the order VLC expects, are:

| Child | Purpose |
|---|---|
| `ThemeInfo` | Free text about the skin: `name`, `author`, `email`, `webpage`. VLC itself does not display it. |
| `Include` | Pulls in another skin file by name. Include elements carry no id. |
| `IniFile` | A helper ini file, referenced by id and file. |
| `Bitmap` | An image resource, optionally with sub bitmaps. |
| `BitmapFont` | A Winamp style digit or text sheet. |
| `Font` | A TTF or OTF font file. |
| `PopupMenu` | A named menu with entries and separators. |
| `Window` | A top level window with its layouts. |

Resources are shared by the whole theme: any item in any window can reference any bitmap, font or menu by id.

## Window

A window is what the user sees on screen. It owns its position, its drag and drop behaviour, and the list of layouts that can be shown inside it.

| Attribute | Default | Meaning |
|---|---|---|
| `id` | `none` | Name used by actions and by window-qualified boolean expressions. |
| `visible` | `true` | Whether the window appears the first time VLC loads the theme. |
| `x`, `y` | `0` | Initial position on screen, used the first time VLC loads the theme. |
| `dragdrop` | `true` | Whether files can be dropped onto this window. |
| `playondrop` | `true` | Whether a dropped file starts playing immediately. When false it is only enqueued. Has no effect when `dragdrop` is false. |

A window must contain at least one `Layout`. If it has several, only one is visible at a time. The layout that appears first when VLC loads the theme is the last one in the list, so move a layout to the bottom to make it the default. The action `windowId.setLayout(layoutId)` switches between them at runtime.

VLC treats one window id specially: `fullscreenController`, which shows a skinnable controller in fullscreen mode. It is a VLC behaviour, not something the editor builds for you.

## Layout

A layout is one arrangement of controls at one size. The window can hold several layouts, and all of them should use the same size, otherwise graphics glitch when VLC switches between them.

| Attribute | Default | Meaning |
|---|---|---|
| `id` | `none` | Name used by `setLayout` actions. Unique inside its window. |
| `width`, `height` | required | Initial size. A theme is not required to be resizable; these still define the visible area. |
| `minwidth`, `minheight` | `-1` | Smallest size allowed when resizing. `-1` means the initial width or height is the minimum. |
| `maxwidth`, `maxheight` | `-1` | Largest size allowed when resizing. `-1` means the initial width or height is the maximum. |

Resizing only becomes possible when at least one item is set up as a resize handle; see [Layouts and anchors](layouts-and-anchors.md).

## Items

`Layout` accepts `Anchor`, `Button`, `Checkbox`, `Group`, `Image`, `Panel`, `Playlist`, `Playtree`, `RadialSlider`, `Slider`, `Text` and `Video`. Each item draws at `x`, `y` relative to its parent container. Two items are containers:

- `Group` adds an offset to everything inside it. It draws nothing itself and is deprecated in the format, but old themes use it heavily.
- `Panel` does the same and also acts as the reference box for its children's resize anchors. Panels can nest.

Two items can have children of their own: a `Slider` can contain one `SliderBackground`, and `Playlist` and `Playtree` can each contain one `Slider` used for scrolling. These are not free containers; the editor refuses to drop other items into them.

The [items overview](items-overview.md) lists every type with a link to its page.

## Ids and their namespaces

Ids are not one pool. VLC and the editor look ids up per kind, and the uniqueness rules differ:

| Id | Unique where |
|---|---|
| Item id | Across the whole theme. One namespace for every control in every layout. |
| Resource id | Across all resources. Sub bitmap ids are part of the same pool as their parent bitmap's id. |
| Window id | Across all windows. |
| Layout id | Inside its window only. Two windows may each have a layout named `main`. |

A window and its layout may share a name; they are different kinds and never collide. The editor never looks an id up without knowing the kind.

An absent `id` attribute or the literal value `none` means "no id" to VLC. When the editor loads such an element it generates one so the trees can select it, using the pattern `Button #1`, `Layout #2` and so on, and writes the generated id back on save.

## Unknown content survives

Attributes and child elements this editor does not recognize are kept as written. They are stored on the node and appended again when the file is saved, in their original order. Later format additions such as the placement attributes `position`, `xoffset`, `yoffset`, `xmargin` and `ymargin` are preserved this way; the editor has no dedicated fields for them, but the Skin XML panel can edit them directly.

Next: [Layouts and anchors](layouts-and-anchors.md).
