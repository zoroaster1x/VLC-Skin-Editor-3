# Format support

Everything the DTD and the original editor support, plus the parts the original
dropped:

* Theme, ThemeInfo, Window, Layout, Include, IniFile
* Bitmap, SubBitmap, Font, BitmapFont
* PopupMenu with MenuItem and MenuSeparator
* Anchor, Button, Checkbox, Group, Image, Panel, Playlist, Playtree,
  RadialSlider, Slider, SliderBackground, Text, Video

The editor writes canonical attribute order and omits defaults. Unknown
attributes and elements are kept verbatim, so a theme from any VLC version
opens and saves without losing data.

The full element and attribute reference, generated from VLC's own
`share/skins2/skin.dtd`, ships with the app under Help > Documentation and is
available to MCP clients through `read_documentation` with the topic
`handbook-format-reference`.

`AGENTS.md` documents the exact behavior of defaults, ids, bezier paths, alpha
keying and boolean expressions, and the traps that cost real time to find.
