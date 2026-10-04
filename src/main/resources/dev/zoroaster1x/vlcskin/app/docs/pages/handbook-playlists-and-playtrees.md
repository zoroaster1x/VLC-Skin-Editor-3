---
title: Playlists and playtrees
section: Making a skin
source: rewritten
---

# Playlists and playtrees

VLC has one playlist control with two spellings in the format. `Playtree` shows the tree structure with folder rows. `Playlist` is the older flat form and is deprecated; it behaves like a playtree with folders collapsed.

The editor models both with one class. It remembers which spelling the file used and hides the folder settings when the document is in `Playlist` syntax, so a round trip never silently rewrites one form into the other.

## In the editor

Add Playlist or Playtree from the Items panel and fill in the Inspector's Playlist section:

- Width and Height size the list area, and Font picks a Font or BitmapFont resource.
- Background image replaces the alternating stripe colours when set.
- For a Playtree the form also shows Item icon, Open folder icon, Closed folder icon and a Flat checkbox. In `Playlist` syntax those fields are hidden because the syntax has no folder rows.
- Text color, Background color 1, Background color 2, Playing color and Selection color take `#RRGGBB` values.
- Edit playlist's slider appears when the list has its child slider; pressing it selects the slider so the Inspector switches to it.

The preview draws exactly what VLC shows with no media loaded: the two tree nodes "Playlist" and "Media Library", with the item image when one is set. The nested slider is drawn at the fully scrolled position and scrolling does nothing, because there is no real playlist in the editor.

## Attributes

| Attribute | Default | Meaning |
|---|---|---|
| `id` | required | Control name. |
| `width`, `height` | `0` | Size of the control. A long item name is cut off with an ellipsis. |
| `font` | required | Font resource used for the rows. |
| `bgimage` | `none` | Bitmap used as the background. When set, the color stripes below are ignored. |
| `fgcolor` | `#000000` | Text color of normal rows. |
| `playcolor` | `#FF0000` | Text color of the row being played. |
| `selcolor` | `#0000FF` | Background color of selected rows. |
| `bgcolor1` | `#FFFFFF` | Background color for odd rows. |
| `bgcolor2` | `#FFFFFF` | Background color for even rows. |
| `itemimage` | `none` | `Playtree` only. Icon left of a leaf item. |
| `openimage` | `none` | `Playtree` only. Icon left of an expanded folder. |
| `closedimage` | `none` | `Playtree` only. Icon left of a collapsed folder. |
| `flat` | `false` | `Playtree` only. Hide the tree structure and show only leaves. |
| `help` | empty | Help text for `$H`. |

![Playtree](images/handbook-playtree.png)

## The slider child

A playlist contains one `Slider` element that scrolls its rows. Without it the list cannot be scrolled, and the validator reports a warning when a playtree has no slider at all.

Add it by selecting the list in the Items tree and choosing Add item > Slider. The slider becomes the list's child; shape its path with the Path tool like any other slider.

The slider is special: it has no percentage variable to choose, because its position is always the playlist scroll position. The Inspector shows `Playtree scrolling` instead of the Value list, and the editor does not write a `value` attribute for it; a value carried by an old file is preserved as written. Its `points` still define the thumb path along the scrollbar, and the usual `up`, `over` and `down` sprites and `thickness` apply.

## Folder rows

A `Playtree` with `flat="false"` shows folder rows and uses `openimage` and `closedimage` for them. Setting `flat="true"`, or using the `Playlist` element, removes folder rows and gives a flat list. `itemimage` marks leaf items in either mode.

## The colors

When `bgimage` is `none`, VLC fills the background with `bgcolor1` and `bgcolor2` in alternating stripes. `fgcolor` draws the text, `selcolor` fills the selected rows, and `playcolor` highlights the item currently playing.

Playlist buttons use the `playlist.*` action codes listed in [Actions and variables](actions-and-variables.md): add, remove, next, previous, sort, load, save, and the boolean toggles for random, loop and repeat.

Next: [Fonts and bitmap fonts](fonts-and-bitmap-fonts.md).
