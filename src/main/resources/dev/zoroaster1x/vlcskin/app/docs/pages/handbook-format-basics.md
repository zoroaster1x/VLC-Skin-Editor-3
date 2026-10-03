---
title: Format basics
section: Appendix: the format explained
source: rewritten
---

# Format basics

This appendix explains the file the editor reads and writes. It is reference material: read the step pages first if you are still making your first skin.

## What theme.xml looks like

A theme is one XML file, usually called `theme.xml`, plus the assets it references. The root element is `Theme`, and its `version` attribute must start with `2` for the skins2 interface. Inside it come metadata, resources and windows, in that order:

```xml
<!DOCTYPE Theme PUBLIC "-//VideoLAN//DTD VLC Skins V2.0//EN" "skin.dtd">
<Theme version="2.0">
  <ThemeInfo name="Minimal"/>
  <Bitmap id="background" file="background.png" alphacolor="#FF00FF"/>
  <Window id="main" visible="true">
    <Layout id="main" width="320" height="140">
      <Image id="bg" x="0" y="0" image="background" action="move"/>
    </Layout>
  </Window>
</Theme>
```

Notes on the shape:

- The DOCTYPE line names VLC's own `skin.dtd`. It is metadata; the editor writes it and the parser accepts files with or without it, and it does not need the DTD file to be present.
- The editor's writer omits attributes that sit at their defaults and keeps unknown attributes and child elements exactly as written, so a saved file can look different from a hand written one without losing anything.
- `ThemeInfo` is optional. With no name it produces an info message in the validator, not an error.
- Resource ids are one namespace; item ids are another. See [Theme structure](theme-structure.md) for the rules.

## A minimal complete example

This theme renders a 320x140 draggable bar with one background image. It validates with no issues when a 320x140 PNG named `background.png` sits next to the file:

```xml
<?xml version="1.0" encoding="UTF-8"?>
<Theme version="2.0">
  <ThemeInfo name="Minimal"/>
  <Bitmap id="background" file="background.png" alphacolor="#FF00FF"/>
  <Window id="main" visible="true">
    <Layout id="main" width="320" height="140">
      <Image id="bg" x="0" y="0" image="background" action="move"/>
    </Layout>
  </Window>
</Theme>
```

The `action="move"` is what lets the user drag the window by the image. Every other control type is a variation on the same three steps: declare a resource, declare a window with a layout, and place items in the layout.

## Where resources live

- Paths in the file are relative to the folder that holds the XML, and they may point into subfolders such as `img/background.png`.
- PNG is the image format skins have always used. VLC itself accepts more formats, but the editor's Add bitmap offers PNG and the decoder follows VLC's `FileBitmap` rules.
- Save the theme before adding assets. With no file there is no folder, and Add bitmap cannot compute a relative path.
- Fonts are TTF or OTF files loaded from the same relative paths.

## From a folder to a .vlt

The folder layout maps directly onto the package. A `.vlt` is a gzipped tar archive that holds `theme.xml` at the root and every referenced asset at the path the file already uses:

```
theme.xml
background.png
img/play_up.png
```

VLC extracts the archive to a temporary folder when it loads it and resolves the relative paths there, so a theme works identically from a folder and from a package. Plain zip archives are accepted too, because many gallery downloads are zips. Importing a `.vlt` into the editor unpacks it next to the archive into a folder named `<archive>_unpacked`; a zip that bundles several `.vlt` files expands each one into its own folder.

[VLT archives](vlt-archives.md) covers the packaging and install rules; [Format reference](format-reference.md) lists every element and attribute VLC parses, with its default.

Next: [Theme structure](theme-structure.md).
