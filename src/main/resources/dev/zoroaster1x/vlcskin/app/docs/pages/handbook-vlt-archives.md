---
title: VLT archives
section: Extras and tools
source: rewritten
---

# VLT archives

A `.vlt` file is a theme in one package: `theme.xml` plus every file the theme references. VLC extracts it to a temporary folder when it loads it, searches for `theme.xml`, and removes the folder when VLC exits or the interface changes. In the editor flow this is the last step; [Step 7](step-7-validate-test-export.md) walks through exporting and installing.

![VLT archive](images/handbook-vlt-archive.png)

## Container format

The standard container is a gzipped tar archive, which the original guide recommends giving the `.vlt` extension. Since VLC 0.8.5 a plain zip archive works too, and many gallery downloads are zips. The editor detects the type from the first bytes of the file, so both open the same way. Zip archives are also what a bundled download looks like when it holds several themes at once.

## What gets packaged

The exporter walks the theme and writes:

- `theme.xml`, first.
- Every referenced bitmap, font, bitmap font and ini file.
- Every `Include` file.

Paths are stored as written in the XML, so a theme that keeps images in an `img/` subfolder keeps that structure inside the archive. Files that are referenced but missing on disk are skipped, and the editor shows a warning afterwards listing them; a theme with missing assets is exactly the case the validator's missing file warnings are for.

Because paths are relative, the theme still resolves them when VLC extracts the archive: `theme.xml` and the assets end up side by side in the temporary folder, in the same layout they had on disk.

## Import

Importing a `.vlt` unpacks it into a folder named after the archive with `_unpacked` appended, next to the archive itself, and opens the `theme.xml` inside. If the archive keeps everything under one top folder, for example `CoolSkin/theme.xml` and `CoolSkin/image.png`, that prefix is dropped so the theme lands flat in the target.

Some gallery entries are zips that bundle further `.vlt` archives instead of a single theme. Import detects that case and unpacks every bundled theme, one folder per theme, then opens the first. The Ecco ColdBlue and FreshGreen download is the known example.

Extraction is guarded. An entry whose normalized path would leave the target folder is refused with an error, so a malicious archive cannot write files anywhere else.

## Command line

The CLI exposes the same operations:

```bash
vlc-skin-studio vlt export out/theme.xml out/theme.vlt
vlc-skin-studio vlt import out/theme.vlt
vlc-skin-studio vlt import out/theme.vlt some/target/folder
```

## Installing for VLC

To make a theme appear in VLC's Change skin dialog, put the `.vlt` in VLC's skins folder:

| Platform | Folder |
|---|---|
| Linux | `~/.local/share/vlc/skins2` |
| Flatpak | `~/.var/app/org.videolan.VLC/data/vlc/skins2` |
| Windows | `VLC\skins`, under the VLC install folder |
| macOS | `~/Library/Application Support/org.videolan.vlc/skins2` |

The editor's File > Test skin in VLC writes the saved XML straight into a VLC launch and does not install anything permanently. The MCP tool `test_in_vlc` exports the `.vlt`, copies it into the skins folder and starts VLC on the installed copy, which is the closest thing to what a user downloading the theme will see.

Next: [MCP for automation](mcp-for-automation.md).
