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

Children: `(ThemeInfo,(Include|IniFile|Bitmap|BitmapFont|Font|PopupMenu|Window)*)`

| Attribute | Type | Required or default |
|---|---|---|
| `version` | CDATA | required |
| `tooltipfont` | CDATA | `defaultfont` |
| `magnet` | CDATA | `15` |
| `alpha` | CDATA | `255` |
| `movealpha` | CDATA | `255` |

### IniFile

| Attribute | Type | Required or default |
|---|---|---|
| `id` | CDATA | required |
| `file` | CDATA | required |

### Include

| Attribute | Type | Required or default |
|---|---|---|
| `file` | CDATA | required |

### Bitmap

Children: `(SubBitmap)*`

| Attribute | Type | Required or default |
|---|---|---|
| `id` | CDATA | required |
| `file` | CDATA | required |
| `alphacolor` | CDATA | required |
| `nbframes` | CDATA | `1` |
| `fps` | CDATA | `0` |
| `loop` | CDATA | `0` |

### SubBitmap

| Attribute | Type | Required or default |
|---|---|---|
| `id` | CDATA | required |
| `x` | CDATA | required |
| `y` | CDATA | required |
| `width` | CDATA | required |
| `height` | CDATA | required |
| `nbframes` | CDATA | `1` |
| `fps` | CDATA | `0` |
| `loop` | CDATA | `0` |

### Font

| Attribute | Type | Required or default |
|---|---|---|
| `id` | CDATA | required |
| `file` | CDATA | required |
| `size` | CDATA | `12` |

### BitmapFont

| Attribute | Type | Required or default |
|---|---|---|
| `id` | CDATA | required |
| `file` | CDATA | required |
| `type` | CDATA | `digits` |

### PopupMenu

Children: `(MenuItem|MenuSeparator)+`

| Attribute | Type | Required or default |
|---|---|---|
| `id` | CDATA | required |

### MenuItem

| Attribute | Type | Required or default |
|---|---|---|
| `label` | CDATA | required |
| `action` | CDATA | `none` |

### MenuSeparator

No attributes.

### ThemeInfo

| Attribute | Type | Required or default |
|---|---|---|
| `name` | CDATA | optional |
| `author` | CDATA | optional |
| `email` | CDATA | optional |
| `webpage` | CDATA | optional |

### Window

Children: `(Layout)+`

| Attribute | Type | Required or default |
|---|---|---|
| `id` | CDATA | `none` |
| `visible` | CDATA | `true` |
| `x` | CDATA | `0` |
| `y` | CDATA | `0` |
| `position` | CDATA | `-1` |
| `xoffset` | CDATA | `0` |
| `yoffset` | CDATA | `0` |
| `xmargin` | CDATA | `0` |
| `ymargin` | CDATA | `0` |
| `dragdrop` | CDATA | `true` |
| `playondrop` | CDATA | `true` |

### Layout

Children: `(Group|Image|Button|Playlist|Slider|RadialSlider|Text|Checkbox| Anchor|Video|Playtree|Panel)+`

| Attribute | Type | Required or default |
|---|---|---|
| `id` | CDATA | `none` |
| `width` | CDATA | required |
| `height` | CDATA | required |
| `minwidth` | CDATA | `-1` |
| `maxwidth` | CDATA | `-1` |
| `minheight` | CDATA | `-1` |
| `maxheight` | CDATA | `-1` |

### Group

Children: `(Group|Image|Button|Playlist|Slider|RadialSlider|Text|Checkbox| Anchor|Video|Playtree|Panel)+`

| Attribute | Type | Required or default |
|---|---|---|
| `id` | CDATA | `none` |
| `x` | CDATA | `0` |
| `y` | CDATA | `0` |

### Panel

Children: `(Group|Image|Button|Playlist|Slider|RadialSlider|Text|Checkbox| Anchor|Video|Playtree|Panel)+`

| Attribute | Type | Required or default |
|---|---|---|
| `id` | CDATA | `none` |
| `x` | CDATA | `0` |
| `y` | CDATA | `0` |
| `lefttop` | CDATA | `lefttop` |
| `rightbottom` | CDATA | `lefttop` |
| `xkeepratio` | CDATA | `false` |
| `ykeepratio` | CDATA | `false` |
| `width` | CDATA | required |
| `height` | CDATA | required |
| `position` | CDATA | `-1` |
| `xoffset` | CDATA | `0` |
| `yoffset` | CDATA | `0` |
| `xmargin` | CDATA | `0` |
| `ymargin` | CDATA | `0` |

### Anchor

| Attribute | Type | Required or default |
|---|---|---|
| `id` | CDATA | `none` |
| `x` | CDATA | `0` |
| `y` | CDATA | `0` |
| `lefttop` | CDATA | `lefttop` |
| `priority` | CDATA | required |
| `points` | CDATA | `(0,0)` |
| `range` | CDATA | `10` |

### Image

| Attribute | Type | Required or default |
|---|---|---|
| `id` | CDATA | `none` |
| `visible` | CDATA | `true` |
| `x` | CDATA | `0` |
| `y` | CDATA | `0` |
| `width` | CDATA | `-1` |
| `height` | CDATA | `-1` |
| `lefttop` | CDATA | `lefttop` |
| `rightbottom` | CDATA | `lefttop` |
| `xkeepratio` | CDATA | `false` |
| `ykeepratio` | CDATA | `false` |
| `image` | CDATA | required |
| `action` | CDATA | `none` |
| `action2` | CDATA | `none` |
| `resize` | CDATA | `mosaic` |
| `help` | CDATA | `` |
| `art` | CDATA | `false` |

### Button

| Attribute | Type | Required or default |
|---|---|---|
| `id` | CDATA | `none` |
| `visible` | CDATA | `true` |
| `x` | CDATA | `0` |
| `y` | CDATA | `0` |
| `lefttop` | CDATA | `lefttop` |
| `rightbottom` | CDATA | `lefttop` |
| `xkeepratio` | CDATA | `false` |
| `ykeepratio` | CDATA | `false` |
| `up` | CDATA | required |
| `down` | CDATA | `none` |
| `over` | CDATA | `none` |
| `action` | CDATA | `none` |
| `tooltiptext` | CDATA | `` |
| `help` | CDATA | `` |

### Checkbox

| Attribute | Type | Required or default |
|---|---|---|
| `id` | CDATA | `none` |
| `visible` | CDATA | `true` |
| `x` | CDATA | `0` |
| `y` | CDATA | `0` |
| `lefttop` | CDATA | `lefttop` |
| `rightbottom` | CDATA | `lefttop` |
| `xkeepratio` | CDATA | `false` |
| `ykeepratio` | CDATA | `false` |
| `up1` | CDATA | required |
| `down1` | CDATA | `none` |
| `over1` | CDATA | `none` |
| `up2` | CDATA | required |
| `down2` | CDATA | `none` |
| `over2` | CDATA | `none` |
| `state` | CDATA | required |
| `action1` | CDATA | `none` |
| `action2` | CDATA | `none` |
| `tooltiptext1` | CDATA | `` |
| `tooltiptext2` | CDATA | `` |
| `help` | CDATA | `` |

### Slider

Children: `(SliderBackground)?`

| Attribute | Type | Required or default |
|---|---|---|
| `id` | CDATA | `none` |
| `visible` | CDATA | `true` |
| `x` | CDATA | `0` |
| `y` | CDATA | `0` |
| `width` | CDATA | `-1` |
| `height` | CDATA | `-1` |
| `lefttop` | CDATA | `lefttop` |
| `rightbottom` | CDATA | `lefttop` |
| `xkeepratio` | CDATA | `false` |
| `ykeepratio` | CDATA | `false` |
| `up` | CDATA | required |
| `down` | CDATA | `none` |
| `over` | CDATA | `none` |
| `points` | CDATA | required |
| `thickness` | CDATA | `10` |
| `value` | CDATA | `none` |
| `background` | CDATA | `none` |
| `tooltiptext` | CDATA | `` |
| `help` | CDATA | `` |

### SliderBackground

| Attribute | Type | Required or default |
|---|---|---|
| `id` | CDATA | `none` |
| `image` | CDATA | required |
| `nbhoriz` | CDATA | `1` |
| `nbvert` | CDATA | `1` |
| `padhoriz` | CDATA | `0` |
| `padvert` | CDATA | `0` |

### RadialSlider

| Attribute | Type | Required or default |
|---|---|---|
| `id` | CDATA | `none` |
| `visible` | CDATA | `true` |
| `x` | CDATA | `0` |
| `y` | CDATA | `0` |
| `lefttop` | CDATA | `lefttop` |
| `rightbottom` | CDATA | `lefttop` |
| `xkeepratio` | CDATA | `false` |
| `ykeepratio` | CDATA | `false` |
| `sequence` | CDATA | required |
| `nbimages` | CDATA | required |
| `minangle` | CDATA | `0` |
| `maxangle` | CDATA | `360` |
| `value` | CDATA | `none` |
| `tooltiptext` | CDATA | `` |
| `help` | CDATA | `` |

### Text

| Attribute | Type | Required or default |
|---|---|---|
| `id` | CDATA | `none` |
| `visible` | CDATA | `true` |
| `x` | CDATA | `0` |
| `y` | CDATA | `0` |
| `width` | CDATA | `0` |
| `lefttop` | CDATA | `lefttop` |
| `rightbottom` | CDATA | `lefttop` |
| `xkeepratio` | CDATA | `false` |
| `ykeepratio` | CDATA | `false` |
| `text` | CDATA | `` |
| `font` | CDATA | required |
| `color` | CDATA | `#000000` |
| `scrolling` | CDATA | `auto` |
| `alignment` | CDATA | `left` |
| `focus` | CDATA | `true` |
| `help` | CDATA | `` |

### Playlist

Children: `(Slider)?`

| Attribute | Type | Required or default |
|---|---|---|
| `id` | CDATA | required |
| `visible` | CDATA | `true` |
| `x` | CDATA | `0` |
| `y` | CDATA | `0` |
| `width` | CDATA | `0` |
| `height` | CDATA | `0` |
| `position` | CDATA | `-1` |
| `xoffset` | CDATA | `0` |
| `yoffset` | CDATA | `0` |
| `xmargin` | CDATA | `0` |
| `ymargin` | CDATA | `0` |
| `lefttop` | CDATA | `lefttop` |
| `rightbottom` | CDATA | `lefttop` |
| `xkeepratio` | CDATA | `false` |
| `ykeepratio` | CDATA | `false` |
| `font` | CDATA | required |
| `bgimage` | CDATA | `none` |
| `fgcolor` | CDATA | `#000000` |
| `playcolor` | CDATA | `#FF0000` |
| `bgcolor1` | CDATA | `#FFFFFF` |
| `bgcolor2` | CDATA | `#FFFFFF` |
| `selcolor` | CDATA | `#0000FF` |
| `help` | CDATA | `` |

### Playtree

Children: `(Slider)?`

| Attribute | Type | Required or default |
|---|---|---|
| `id` | CDATA | required |
| `visible` | CDATA | `true` |
| `x` | CDATA | `0` |
| `y` | CDATA | `0` |
| `width` | CDATA | `0` |
| `height` | CDATA | `0` |
| `position` | CDATA | `-1` |
| `xoffset` | CDATA | `0` |
| `yoffset` | CDATA | `0` |
| `xmargin` | CDATA | `0` |
| `ymargin` | CDATA | `0` |
| `lefttop` | CDATA | `lefttop` |
| `rightbottom` | CDATA | `lefttop` |
| `xkeepratio` | CDATA | `false` |
| `ykeepratio` | CDATA | `false` |
| `font` | CDATA | required |
| `bgimage` | CDATA | `none` |
| `itemimage` | CDATA | `none` |
| `openimage` | CDATA | `none` |
| `closedimage` | CDATA | `none` |
| `fgcolor` | CDATA | `#000000` |
| `playcolor` | CDATA | `#FF0000` |
| `bgcolor1` | CDATA | `#FFFFFF` |
| `bgcolor2` | CDATA | `#FFFFFF` |
| `selcolor` | CDATA | `#0000FF` |
| `help` | CDATA | `` |
| `flat` | CDATA | `false` |

### Video

| Attribute | Type | Required or default |
|---|---|---|
| `id` | CDATA | `none` |
| `visible` | CDATA | `true` |
| `x` | CDATA | `0` |
| `y` | CDATA | `0` |
| `width` | CDATA | `0` |
| `height` | CDATA | `0` |
| `position` | CDATA | `-1` |
| `xoffset` | CDATA | `0` |
| `yoffset` | CDATA | `0` |
| `xmargin` | CDATA | `0` |
| `ymargin` | CDATA | `0` |
| `lefttop` | CDATA | `lefttop` |
| `rightbottom` | CDATA | `lefttop` |
| `xkeepratio` | CDATA | `false` |
| `ykeepratio` | CDATA | `false` |
| `autoresize` | CDATA | `true` |
| `help` | CDATA | `` |

