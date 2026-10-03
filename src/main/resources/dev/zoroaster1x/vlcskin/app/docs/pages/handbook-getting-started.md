---
title: Getting started
section: Start here
source: rewritten
---

# Getting started

## What a skin is

A VLC skin, also called a theme, is a folder of assets plus one XML file that describes how VLC draws and behaves. The assets are PNG images for controls, optional TTF or OTF fonts, and optional helper files. The XML file is usually named `theme.xml`.

VLC reads the theme and renders it with the skins2 interface. The editor reads and writes the same format, previews it, validates it and packages it. The rules behind it are in the [format appendix](format-basics.md); the step pages show how to build one in the editor.

## .xml or .vlt

The XML file works on its own while its assets stay beside it. To distribute a theme, package it as a `.vlt`, a gzipped tar archive holding `theme.xml` plus every referenced file. Some archives in the wild are plain zips instead; VLC and the editor accept both.

Keep the `.vlt` extension. VLC's Change skin dialog filters for `.vlt` and `.xml`, and `.vlt` is what people download from the skins gallery. [VLT archives](vlt-archives.md) covers packaging, importing and installing.

## How VLC loads a theme

Start VLC on a theme directly:

```bash
vlc -I skins2 --skins2-last=path/to/theme.xml
```

The same flag accepts a `.vlt`. VLC extracts the archive to a temporary folder, finds `theme.xml` and renders the result. In the user interface, open Tools > Preferences > Interface, set the look and feel to Use custom skin, and choose the file; VLC needs a restart before the change is visible. The usual install folders are:

| Platform | Folder |
|---|---|
| Linux | `~/.local/share/vlc/skins2` |
| Flatpak | `~/.var/app/org.videolan.VLC/data/vlc/skins2` |
| Windows | `VLC\skins`, under the VLC install folder |
| macOS | `~/Library/Application Support/org.videolan.vlc/skins2` |

VLC's own user documentation has stated that skins are not supported on macOS at all; the editor still uses the folder above when it launches VLC there.

Window position and visibility are hints: VLC remembers them between runs, so `Window` `x`, `y` and `visible` apply the first time the theme is loaded.

## The route through the editor

1. Plan and export the artwork: [Step 1](step-1-prepare-artwork.md).
2. Create the theme and set its sizes: [Step 2](step-2-new-theme.md).
3. Add controls and wire their actions: [Step 3](step-3-add-and-place-controls.md).
4. Add text and check it against the Variables panel: [Step 4](step-4-text-and-variables.md).
5. Build sliders and their backgrounds: [Step 5](step-5-sliders-and-backgrounds.md).
6. Animate bitmaps and add playlists: [Step 6](step-6-animations-and-playlists.md).
7. Validate, test in VLC and export: [Step 7](step-7-validate-test-export.md).

The welcome card starts all of this: see [Welcome and your first skin](welcome-and-first-skin.md).

## The command line

The CLI covers the same ground without a display, using the native binary or the jar:

```bash
vlc-skin-studio new --example neon out/theme.xml
vlc-skin-studio validate out/theme.xml
vlc-skin-studio render out/theme.xml -z 2 -o preview.png --json geometry.json
vlc-skin-studio vlt export out/theme.xml out/theme.vlt
```

The terminal UI (`tui`) browses and edits a theme over SSH. The MCP server exposes the same operations to scripts and AI clients; see [MCP for automation](mcp-for-automation.md).

Next: [Window tour](window-tour.md).
