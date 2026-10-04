---
title: Theme browser
section: Extras and tools
source: rewritten
---

# Theme browser

File > Browse themes, or Ctrl+B, opens the official VideoLAN skins gallery inside the editor. It downloads a theme, unpacks it and opens it in one step, so you can study a finished skin as a starting point.

## The window

The browser is a dialog with a filter, a sortable table, a preview and two buttons:

| Part | What it does |
|---|---|
| Filter | Matches theme name or author, case-insensitively, as you type. Clear it to see the whole list. |
| Table | One row per theme, with Theme, Author, Size, Downloads and Date columns. Click a column header to sort by it. |
| Preview | Select a row and the preview image loads beside the table, scaled to fit 400x300. |
| Refresh | Ignores the cached list and fetches the gallery page again. |
| Download and open | Downloads the archive, unpacks it and opens its `theme.xml` in the editor. |
| Close | Closes the browser. The editor window stays as it was. |

The status line under the table reports what is happening: `Loading the gallery...`, `138 themes available`, `Downloading <name>...`, `Unpacking <name>...`, `Opened <name>`, or an explanatory error when the network or the download fails.

## What it downloads

The list is read from the gallery page at `https://www.videolan.org/vlc/skins.html`, and the archives come from `https://www.videolan.org/vlc/skins2/`. A gallery download may be a gzipped tar `.vlt` or a plain zip; the importer detects which from the file and accepts both. Some gallery entries bundle several themes in one zip; those unpack into one folder per theme and the first opens.

## Where the files go

| What | Where |
|---|---|
| Gallery list | `$XDG_CACHE_HOME/vlc-skin-studio/gallery/themes.json`, or `~/.cache/vlc-skin-studio/gallery/themes.json` |
| Previews | `$XDG_CACHE_HOME/vlc-skin-studio/previews/` |
| Downloaded archives | `$XDG_CACHE_HOME/vlc-skin-studio/archives/` |
| Unpacked theme | `$XDG_DATA_HOME/vlc-skin-studio/themes/<name>`, or `~/.local/share/vlc-skin-studio/themes/<name>` |

## Caching and offline behaviour

- The list is cached for 24 hours. Within that window the browser opens without touching the network; Refresh forces a fetch.
- Preview images and archives are cached by name and URL. Importing the same theme again is instant.
- When the network is down, a cached list is still served, so the dialog keeps working with stale data. Only a first run without a connection fails to show the table.
- If the download itself fails, nothing is written to the themes folder; the status line carries the reason.

The imported theme keeps the structure of the archive; a zip that bundles further `.vlt` files expands all of them. See [VLT archives](vlt-archives.md) for the packaging rules, and [Documentation viewer](documentation-viewer.md) for the bundled help.

Next: [Documentation viewer](documentation-viewer.md).
