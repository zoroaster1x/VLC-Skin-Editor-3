---
title: Format reference (under the hood)
section: Appendix: the format explained
source: generated
---

# skins2 format reference

Generated from VLC's `share/skins2/skin.dtd` by `tools/dtd-to-markdown.py`.
Every element and attribute VLC parses is listed, with the DTD default.
Skins2 version: 2.0.

## Element hierarchy

```
Theme
  ThemeInfo
  Include
  IniFile
  Bitmap
    SubBitmap
  BitmapFont
  Font
  PopupMenu
    MenuItem
    MenuSeparator
  Window
    Layout
      Group
        Group
        Image
        Button
        Playlist
          Slider
        Slider
          SliderBackground
        RadialSlider
        Text
        Checkbox
        Anchor
        Video
        Playtree
          Slider
        Panel
          Group
          Image
          Button
          Playlist
          Slider
          RadialSlider
          Text
          Checkbox
          Anchor
          Video
          Playtree
          Panel
      Image
      Button
      Playlist
        Slider
          SliderBackground
      Slider
        SliderBackground
      RadialSlider
      Text
      Checkbox
      Anchor
      Video
      Playtree
        Slider
          SliderBackground
      Panel
        Group
          Group
          Image
          Button
          Playlist
          Slider
          RadialSlider
          Text
          Checkbox
          Anchor
          Video
          Playtree
          Panel
        Image
        Button
        Playlist
          Slider
        Slider
          SliderBackground
        RadialSlider
        Text
        Checkbox
        Anchor
        Video
        Playtree
          Slider
        Panel
```

## Elements

### Theme

The document root. Version must be 2.x for skins2.

Children: `(ThemeInfo,(Include|IniFile|Bitmap|BitmapFont|Font|PopupMenu|Window)*)`

| Attribute | Type | Required or default | Meaning |
|---|---|---|---|
| `version` | CDATA | required | Skin format version, must be 2.x for VLC skins2. |
| `tooltipfont` | CDATA | `defaultfont` | Font resource used for tooltips. |
| `magnet` | CDATA | `15` | Snapping distance used by the editor when dragging items. |
| `alpha` | CDATA | `255` | Window opacity, 0 to 255. |
| `movealpha` | CDATA | `255` | Opacity of the window while it is being moved. |

### IniFile

An ini file resource used by some skins for configuration.

| Attribute | Type | Required or default | Meaning |
|---|---|---|---|
| `id` | CDATA | required | Unique name in its own namespace. VLC treats the literal none as no id. |
| `file` | CDATA | required | Path to the asset, relative to the skin folder. |

### Include

Pulls another skin file into the theme.

| Attribute | Type | Required or default | Meaning |
|---|---|---|---|
| `file` | CDATA | required | Path to the asset, relative to the skin folder. |

### Bitmap

An image resource, alphacolor keyed and cut into nbframes strips.

Children: `(SubBitmap)*`

| Attribute | Type | Required or default | Meaning |
|---|---|---|---|
| `id` | CDATA | required | Unique name in its own namespace. VLC treats the literal none as no id. |
| `file` | CDATA | required | Path to the asset, relative to the skin folder. |
| `alphacolor` | CDATA | required | Pixels with exactly this RGB become fully transparent. |
| `nbframes` | CDATA | `1` | Number of equal frames in a bitmap strip or background grid. |
| `fps` | CDATA | `0` | Animation frames per second for a multi frame bitmap. |
| `loop` | CDATA | `0` | Restart the animation after the last frame. |

### SubBitmap

A named rectangle cut out of a parent Bitmap.

| Attribute | Type | Required or default | Meaning |
|---|---|---|---|
| `id` | CDATA | required | Unique name in its own namespace. VLC treats the literal none as no id. |
| `x` | CDATA | required | Left position in layout pixels, before anchoring. |
| `y` | CDATA | required | Top position in layout pixels, before anchoring. |
| `width` | CDATA | required | Width in pixels. A negative width stretches to the right edge. |
| `height` | CDATA | required | Height in pixels. A negative height stretches to the bottom edge. |
| `nbframes` | CDATA | `1` | Number of equal frames in a bitmap strip or background grid. |
| `fps` | CDATA | `0` | Animation frames per second for a multi frame bitmap. |
| `loop` | CDATA | `0` | Restart the animation after the last frame. |

### Font

A TrueType or OpenType font resource with a size.

| Attribute | Type | Required or default | Meaning |
|---|---|---|---|
| `id` | CDATA | required | Unique name in its own namespace. VLC treats the literal none as no id. |
| `file` | CDATA | required | Path to the asset, relative to the skin folder. |
| `size` | CDATA | `12` | Font size in points. |

### BitmapFont

A font drawn from a bitmap, kept as parsed data.

| Attribute | Type | Required or default | Meaning |
|---|---|---|---|
| `id` | CDATA | required | Unique name in its own namespace. VLC treats the literal none as no id. |
| `file` | CDATA | required | Path to the asset, relative to the skin folder. |
| `type` | CDATA | `digits` | Value kind for a slider, for example position, volume or time. |

### PopupMenu

Context menu resource with items and separators.

Children: `(MenuItem|MenuSeparator)+`

| Attribute | Type | Required or default | Meaning |
|---|---|---|---|
| `id` | CDATA | required | Unique name in its own namespace. VLC treats the literal none as no id. |

### MenuItem

One entry of a PopupMenu with its action.

| Attribute | Type | Required or default | Meaning |
|---|---|---|---|
| `label` | CDATA | required |  |
| `action` | CDATA | `none` | Semicolon separated action chain, for example vlc.play(). |

### MenuSeparator

A divider inside a PopupMenu.

No attributes.

### ThemeInfo

Human readable metadata: name, author, email, webpage.

| Attribute | Type | Required or default | Meaning |
|---|---|---|---|
| `name` | CDATA | optional | Human readable name shown in VLC and the editor. |
| `author` | CDATA | optional | Author name kept in ThemeInfo. |
| `email` | CDATA | optional | Contact address kept in ThemeInfo. |
| `webpage` | CDATA | optional | Project page kept in ThemeInfo. |

### Window

A top level window VLC creates. Owns layouts and their sizes.

Children: `(Layout)+`

| Attribute | Type | Required or default | Meaning |
|---|---|---|---|
| `id` | CDATA | `none` | Unique name in its own namespace. VLC treats the literal none as no id. |
| `visible` | CDATA | `true` | Boolean expression, for example not vlc.isPlaying. True by default. |
| `x` | CDATA | `0` | Left position in layout pixels, before anchoring. |
| `y` | CDATA | `0` | Top position in layout pixels, before anchoring. |
| `position` | CDATA | `-1` |  |
| `xoffset` | CDATA | `0` |  |
| `yoffset` | CDATA | `0` |  |
| `xmargin` | CDATA | `0` |  |
| `ymargin` | CDATA | `0` |  |
| `dragdrop` | CDATA | `true` |  |
| `playondrop` | CDATA | `true` |  |

### Layout

One arrangement of items inside a window. VLC switches between layouts.

Children: `(Group|Image|Button|Playlist|Slider|RadialSlider|Text|Checkbox| Anchor|Video|Playtree|Panel)+`

| Attribute | Type | Required or default | Meaning |
|---|---|---|---|
| `id` | CDATA | `none` | Unique name in its own namespace. VLC treats the literal none as no id. |
| `width` | CDATA | required | Width in pixels. A negative width stretches to the right edge. |
| `height` | CDATA | required | Height in pixels. A negative height stretches to the bottom edge. |
| `minwidth` | CDATA | `-1` | Smallest width when the layout shrinks. |
| `maxwidth` | CDATA | `-1` | Largest width when the layout grows. |
| `minheight` | CDATA | `-1` | Smallest height when the layout shrinks. |
| `maxheight` | CDATA | `-1` | Largest height when the layout grows. |

### Group

Invisible container that moves its children together.

Children: `(Group|Image|Button|Playlist|Slider|RadialSlider|Text|Checkbox| Anchor|Video|Playtree|Panel)+`

| Attribute | Type | Required or default | Meaning |
|---|---|---|---|
| `id` | CDATA | `none` | Unique name in its own namespace. VLC treats the literal none as no id. |
| `x` | CDATA | `0` | Left position in layout pixels, before anchoring. |
| `y` | CDATA | `0` | Top position in layout pixels, before anchoring. |

### Panel

Visible container with its own background and size.

Children: `(Group|Image|Button|Playlist|Slider|RadialSlider|Text|Checkbox| Anchor|Video|Playtree|Panel)+`

| Attribute | Type | Required or default | Meaning |
|---|---|---|---|
| `id` | CDATA | `none` | Unique name in its own namespace. VLC treats the literal none as no id. |
| `x` | CDATA | `0` | Left position in layout pixels, before anchoring. |
| `y` | CDATA | `0` | Top position in layout pixels, before anchoring. |
| `lefttop` | CDATA | `lefttop` | Anchor name for the top left corner. Items keep their distance to it. |
| `rightbottom` | CDATA | `lefttop` | Anchor name for the bottom right corner. Defaults to lefttop. |
| `xkeepratio` | CDATA | `false` | Keep the horizontal distance between both anchors when resizing. |
| `ykeepratio` | CDATA | `false` | Keep the vertical distance between both anchors when resizing. |
| `width` | CDATA | required | Width in pixels. A negative width stretches to the right edge. |
| `height` | CDATA | required | Height in pixels. A negative height stretches to the bottom edge. |
| `position` | CDATA | `-1` |  |
| `xoffset` | CDATA | `0` |  |
| `yoffset` | CDATA | `0` |  |
| `xmargin` | CDATA | `0` |  |
| `ymargin` | CDATA | `0` |  |

### Anchor

Invisible marker used as a reference point for other items.

| Attribute | Type | Required or default | Meaning |
|---|---|---|---|
| `id` | CDATA | `none` | Unique name in its own namespace. VLC treats the literal none as no id. |
| `x` | CDATA | `0` | Left position in layout pixels, before anchoring. |
| `y` | CDATA | `0` | Top position in layout pixels, before anchoring. |
| `lefttop` | CDATA | `lefttop` | Anchor name for the top left corner. Items keep their distance to it. |
| `priority` | CDATA | required |  |
| `points` | CDATA | `(0,0)` | Slider track control points as x,y pairs, sampled as a bezier. |
| `range` | CDATA | `10` | Anchor range in layout pixels. |

### Image

Static picture, optionally clickable through an action.

| Attribute | Type | Required or default | Meaning |
|---|---|---|---|
| `id` | CDATA | `none` | Unique name in its own namespace. VLC treats the literal none as no id. |
| `visible` | CDATA | `true` | Boolean expression, for example not vlc.isPlaying. True by default. |
| `x` | CDATA | `0` | Left position in layout pixels, before anchoring. |
| `y` | CDATA | `0` | Top position in layout pixels, before anchoring. |
| `width` | CDATA | `-1` | Width in pixels. A negative width stretches to the right edge. |
| `height` | CDATA | `-1` | Height in pixels. A negative height stretches to the bottom edge. |
| `lefttop` | CDATA | `lefttop` | Anchor name for the top left corner. Items keep their distance to it. |
| `rightbottom` | CDATA | `lefttop` | Anchor name for the bottom right corner. Defaults to lefttop. |
| `xkeepratio` | CDATA | `false` | Keep the horizontal distance between both anchors when resizing. |
| `ykeepratio` | CDATA | `false` | Keep the vertical distance between both anchors when resizing. |
| `image` | CDATA | required | Bitmap or sub bitmap id drawn behind a control. |
| `action` | CDATA | `none` | Semicolon separated action chain, for example vlc.play(). |
| `action2` | CDATA | `none` |  |
| `resize` | CDATA | `mosaic` |  |
| `help` | CDATA | `` | Help text shown by the editor and in some VLC views. |
| `art` | CDATA | `false` |  |

### Button

Clickable control with up, down and over images plus an action.

| Attribute | Type | Required or default | Meaning |
|---|---|---|---|
| `id` | CDATA | `none` | Unique name in its own namespace. VLC treats the literal none as no id. |
| `visible` | CDATA | `true` | Boolean expression, for example not vlc.isPlaying. True by default. |
| `x` | CDATA | `0` | Left position in layout pixels, before anchoring. |
| `y` | CDATA | `0` | Top position in layout pixels, before anchoring. |
| `lefttop` | CDATA | `lefttop` | Anchor name for the top left corner. Items keep their distance to it. |
| `rightbottom` | CDATA | `lefttop` | Anchor name for the bottom right corner. Defaults to lefttop. |
| `xkeepratio` | CDATA | `false` | Keep the horizontal distance between both anchors when resizing. |
| `ykeepratio` | CDATA | `false` | Keep the vertical distance between both anchors when resizing. |
| `up` | CDATA | required | Bitmap or sub bitmap drawn in the normal state. |
| `down` | CDATA | `none` | Bitmap or sub bitmap drawn while pressed. |
| `over` | CDATA | `none` | Bitmap or sub bitmap drawn while hovered. |
| `action` | CDATA | `none` | Semicolon separated action chain, for example vlc.play(). |
| `tooltiptext` | CDATA | `` | Tooltip shown when the pointer rests on the item. |
| `help` | CDATA | `` | Help text shown by the editor and in some VLC views. |

### Checkbox

Two state control with its own images and state expression.

| Attribute | Type | Required or default | Meaning |
|---|---|---|---|
| `id` | CDATA | `none` | Unique name in its own namespace. VLC treats the literal none as no id. |
| `visible` | CDATA | `true` | Boolean expression, for example not vlc.isPlaying. True by default. |
| `x` | CDATA | `0` | Left position in layout pixels, before anchoring. |
| `y` | CDATA | `0` | Top position in layout pixels, before anchoring. |
| `lefttop` | CDATA | `lefttop` | Anchor name for the top left corner. Items keep their distance to it. |
| `rightbottom` | CDATA | `lefttop` | Anchor name for the bottom right corner. Defaults to lefttop. |
| `xkeepratio` | CDATA | `false` | Keep the horizontal distance between both anchors when resizing. |
| `ykeepratio` | CDATA | `false` | Keep the vertical distance between both anchors when resizing. |
| `up1` | CDATA | required |  |
| `down1` | CDATA | `none` |  |
| `over1` | CDATA | `none` |  |
| `up2` | CDATA | required |  |
| `down2` | CDATA | `none` |  |
| `over2` | CDATA | `none` |  |
| `state` | CDATA | required | Boolean expression or value for a checkbox state. |
| `action1` | CDATA | `none` |  |
| `action2` | CDATA | `none` |  |
| `tooltiptext1` | CDATA | `` |  |
| `tooltiptext2` | CDATA | `` |  |
| `help` | CDATA | `` | Help text shown by the editor and in some VLC views. |

### Slider

Track and thumb control for a player value. Points describe the track.

Children: `(SliderBackground)?`

| Attribute | Type | Required or default | Meaning |
|---|---|---|---|
| `id` | CDATA | `none` | Unique name in its own namespace. VLC treats the literal none as no id. |
| `visible` | CDATA | `true` | Boolean expression, for example not vlc.isPlaying. True by default. |
| `x` | CDATA | `0` | Left position in layout pixels, before anchoring. |
| `y` | CDATA | `0` | Top position in layout pixels, before anchoring. |
| `width` | CDATA | `-1` | Width in pixels. A negative width stretches to the right edge. |
| `height` | CDATA | `-1` | Height in pixels. A negative height stretches to the bottom edge. |
| `lefttop` | CDATA | `lefttop` | Anchor name for the top left corner. Items keep their distance to it. |
| `rightbottom` | CDATA | `lefttop` | Anchor name for the bottom right corner. Defaults to lefttop. |
| `xkeepratio` | CDATA | `false` | Keep the horizontal distance between both anchors when resizing. |
| `ykeepratio` | CDATA | `false` | Keep the vertical distance between both anchors when resizing. |
| `up` | CDATA | required | Bitmap or sub bitmap drawn in the normal state. |
| `down` | CDATA | `none` | Bitmap or sub bitmap drawn while pressed. |
| `over` | CDATA | `none` | Bitmap or sub bitmap drawn while hovered. |
| `points` | CDATA | required | Slider track control points as x,y pairs, sampled as a bezier. |
| `thickness` | CDATA | `10` | Slider track thickness in pixels. |
| `value` | CDATA | `none` | The player value the slider drives, for example position or volume. |
| `background` | CDATA | `none` | Bitmap id of a slider background or panel background. |
| `tooltiptext` | CDATA | `` | Tooltip shown when the pointer rests on the item. |
| `help` | CDATA | `` | Help text shown by the editor and in some VLC views. |

### SliderBackground

A bitmap cut into a frame grid, drawn behind a slider.

| Attribute | Type | Required or default | Meaning |
|---|---|---|---|
| `id` | CDATA | `none` | Unique name in its own namespace. VLC treats the literal none as no id. |
| `image` | CDATA | required | Bitmap or sub bitmap id drawn behind a control. |
| `nbhoriz` | CDATA | `1` | Number of background frames per row. |
| `nbvert` | CDATA | `1` | Number of background frame rows. |
| `padhoriz` | CDATA | `0` | Pixels between horizontal frames. |
| `padvert` | CDATA | `0` | Pixels between vertical frame rows. |

### RadialSlider

Circular slider drawn along an arc, same concept as Slider.

| Attribute | Type | Required or default | Meaning |
|---|---|---|---|
| `id` | CDATA | `none` | Unique name in its own namespace. VLC treats the literal none as no id. |
| `visible` | CDATA | `true` | Boolean expression, for example not vlc.isPlaying. True by default. |
| `x` | CDATA | `0` | Left position in layout pixels, before anchoring. |
| `y` | CDATA | `0` | Top position in layout pixels, before anchoring. |
| `lefttop` | CDATA | `lefttop` | Anchor name for the top left corner. Items keep their distance to it. |
| `rightbottom` | CDATA | `lefttop` | Anchor name for the bottom right corner. Defaults to lefttop. |
| `xkeepratio` | CDATA | `false` | Keep the horizontal distance between both anchors when resizing. |
| `ykeepratio` | CDATA | `false` | Keep the vertical distance between both anchors when resizing. |
| `sequence` | CDATA | required |  |
| `nbimages` | CDATA | required |  |
| `minangle` | CDATA | `0` |  |
| `maxangle` | CDATA | `360` |  |
| `value` | CDATA | `none` | The player value the slider drives, for example position or volume. |
| `tooltiptext` | CDATA | `` | Tooltip shown when the pointer rests on the item. |
| `help` | CDATA | `` | Help text shown by the editor and in some VLC views. |

### Text

A label rendered with a font resource and $ variables.

| Attribute | Type | Required or default | Meaning |
|---|---|---|---|
| `id` | CDATA | `none` | Unique name in its own namespace. VLC treats the literal none as no id. |
| `visible` | CDATA | `true` | Boolean expression, for example not vlc.isPlaying. True by default. |
| `x` | CDATA | `0` | Left position in layout pixels, before anchoring. |
| `y` | CDATA | `0` | Top position in layout pixels, before anchoring. |
| `width` | CDATA | `0` | Width in pixels. A negative width stretches to the right edge. |
| `lefttop` | CDATA | `lefttop` | Anchor name for the top left corner. Items keep their distance to it. |
| `rightbottom` | CDATA | `lefttop` | Anchor name for the bottom right corner. Defaults to lefttop. |
| `xkeepratio` | CDATA | `false` | Keep the horizontal distance between both anchors when resizing. |
| `ykeepratio` | CDATA | `false` | Keep the vertical distance between both anchors when resizing. |
| `text` | CDATA | `` | Label text with $ variables such as $N, $T, $V. |
| `font` | CDATA | required | Font resource id used to draw the text. |
| `color` | CDATA | `#000000` |  |
| `scrolling` | CDATA | `auto` |  |
| `alignment` | CDATA | `left` |  |
| `focus` | CDATA | `true` |  |
| `help` | CDATA | `` | Help text shown by the editor and in some VLC views. |

### Playlist

The playlist view, optionally with a scrollbar slider child.

Children: `(Slider)?`

| Attribute | Type | Required or default | Meaning |
|---|---|---|---|
| `id` | CDATA | required | Unique name in its own namespace. VLC treats the literal none as no id. |
| `visible` | CDATA | `true` | Boolean expression, for example not vlc.isPlaying. True by default. |
| `x` | CDATA | `0` | Left position in layout pixels, before anchoring. |
| `y` | CDATA | `0` | Top position in layout pixels, before anchoring. |
| `width` | CDATA | `0` | Width in pixels. A negative width stretches to the right edge. |
| `height` | CDATA | `0` | Height in pixels. A negative height stretches to the bottom edge. |
| `position` | CDATA | `-1` |  |
| `xoffset` | CDATA | `0` |  |
| `yoffset` | CDATA | `0` |  |
| `xmargin` | CDATA | `0` |  |
| `ymargin` | CDATA | `0` |  |
| `lefttop` | CDATA | `lefttop` | Anchor name for the top left corner. Items keep their distance to it. |
| `rightbottom` | CDATA | `lefttop` | Anchor name for the bottom right corner. Defaults to lefttop. |
| `xkeepratio` | CDATA | `false` | Keep the horizontal distance between both anchors when resizing. |
| `ykeepratio` | CDATA | `false` | Keep the vertical distance between both anchors when resizing. |
| `font` | CDATA | required | Font resource id used to draw the text. |
| `bgimage` | CDATA | `none` |  |
| `fgcolor` | CDATA | `#000000` |  |
| `playcolor` | CDATA | `#FF0000` |  |
| `bgcolor1` | CDATA | `#FFFFFF` |  |
| `bgcolor2` | CDATA | `#FFFFFF` |  |
| `selcolor` | CDATA | `#0000FF` |  |
| `help` | CDATA | `` | Help text shown by the editor and in some VLC views. |

### Playtree

The same control as Playlist, older spelling with folder rows.

Children: `(Slider)?`

| Attribute | Type | Required or default | Meaning |
|---|---|---|---|
| `id` | CDATA | required | Unique name in its own namespace. VLC treats the literal none as no id. |
| `visible` | CDATA | `true` | Boolean expression, for example not vlc.isPlaying. True by default. |
| `x` | CDATA | `0` | Left position in layout pixels, before anchoring. |
| `y` | CDATA | `0` | Top position in layout pixels, before anchoring. |
| `width` | CDATA | `0` | Width in pixels. A negative width stretches to the right edge. |
| `height` | CDATA | `0` | Height in pixels. A negative height stretches to the bottom edge. |
| `position` | CDATA | `-1` |  |
| `xoffset` | CDATA | `0` |  |
| `yoffset` | CDATA | `0` |  |
| `xmargin` | CDATA | `0` |  |
| `ymargin` | CDATA | `0` |  |
| `lefttop` | CDATA | `lefttop` | Anchor name for the top left corner. Items keep their distance to it. |
| `rightbottom` | CDATA | `lefttop` | Anchor name for the bottom right corner. Defaults to lefttop. |
| `xkeepratio` | CDATA | `false` | Keep the horizontal distance between both anchors when resizing. |
| `ykeepratio` | CDATA | `false` | Keep the vertical distance between both anchors when resizing. |
| `font` | CDATA | required | Font resource id used to draw the text. |
| `bgimage` | CDATA | `none` |  |
| `itemimage` | CDATA | `none` |  |
| `openimage` | CDATA | `none` |  |
| `closedimage` | CDATA | `none` |  |
| `fgcolor` | CDATA | `#000000` |  |
| `playcolor` | CDATA | `#FF0000` |  |
| `bgcolor1` | CDATA | `#FFFFFF` |  |
| `bgcolor2` | CDATA | `#FFFFFF` |  |
| `selcolor` | CDATA | `#0000FF` |  |
| `help` | CDATA | `` | Help text shown by the editor and in some VLC views. |
| `flat` | CDATA | `false` |  |

### Video

The video surface of the player inside the layout.

| Attribute | Type | Required or default | Meaning |
|---|---|---|---|
| `id` | CDATA | `none` | Unique name in its own namespace. VLC treats the literal none as no id. |
| `visible` | CDATA | `true` | Boolean expression, for example not vlc.isPlaying. True by default. |
| `x` | CDATA | `0` | Left position in layout pixels, before anchoring. |
| `y` | CDATA | `0` | Top position in layout pixels, before anchoring. |
| `width` | CDATA | `0` | Width in pixels. A negative width stretches to the right edge. |
| `height` | CDATA | `0` | Height in pixels. A negative height stretches to the bottom edge. |
| `position` | CDATA | `-1` |  |
| `xoffset` | CDATA | `0` |  |
| `yoffset` | CDATA | `0` |  |
| `xmargin` | CDATA | `0` |  |
| `ymargin` | CDATA | `0` |  |
| `lefttop` | CDATA | `lefttop` | Anchor name for the top left corner. Items keep their distance to it. |
| `rightbottom` | CDATA | `lefttop` | Anchor name for the bottom right corner. Defaults to lefttop. |
| `xkeepratio` | CDATA | `false` | Keep the horizontal distance between both anchors when resizing. |
| `ykeepratio` | CDATA | `false` | Keep the vertical distance between both anchors when resizing. |
| `autoresize` | CDATA | `true` |  |
| `help` | CDATA | `` | Help text shown by the editor and in some VLC views. |

