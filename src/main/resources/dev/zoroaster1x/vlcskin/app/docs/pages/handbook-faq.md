---
title: FAQ
section: Troubleshooting
source: rewritten
---

# FAQ

Short answers to the questions that come up while building a theme. The longer walkthroughs for the common failures are in [Common problems](common-problems.md).

## Does VLC need a restart to see my changes?

VLC reads a theme when it loads it, and it does not watch the file afterwards. Edits made while VLC is running show up the next time the theme is loaded: either reload the skin or restart VLC. Switching the interface to Use custom skin also requires a restart, per VLC's user documentation. The fastest loop is File > Test skin in VLC, which saves first and starts VLC on the saved file.

## Why is my bitmap invisible?

The full checklist is in [Common problems](common-problems.md). The short version: the reference resolves, the file exists, alphacolor keying has not removed the pixels, nothing covers the item, the Visible expression is true, and an animated bitmap draws frame 0 on static controls. If you edited the PNG outside the editor, use Reload image in the Resources panel; the picture cache does not watch the disk.

## Why did my slider not move?

The XML `value` attribute names the percentage variable VLC binds to the slider; it is not a stored position. The preview draws the thumb at the simulated slider position in the Variables panel, which starts at 50. Drag that slider and the thumb follows. If the slider sits inside a playlist, it has no value at all; it follows the playlist scroll position, and the preview has no real playlist, so that thumb stays where it is.

## What does Check for updates do?

Help > Check for updates asks GitHub for the releases and, when a newer one exists, opens a dialog with the notes of every release you missed, oldest first, and an Install update button. The check on startup is on by default and can be turned off in the same menu. Installing downloads the release jar, verifies its SHA-256 against the SHA256SUMS asset the release workflow publishes, and replaces the running jar: on Linux and macOS in place, on Windows through a small helper script that waits for the editor to close, swaps the file and starts it again. If the editor does not run from a jar, it keeps the verified download and says so.

## Where do downloads go?

| What | Where |
|---|---|
| Gallery theme | `$XDG_DATA_HOME/vlc-skin-studio/themes/<name>`, or `~/.local/share/vlc-skin-studio/themes/<name>` |
| Gallery list and previews | `$XDG_CACHE_HOME/vlc-skin-studio`, or `~/.cache/vlc-skin-studio` |
| Imported `.vlt` | Next to the archive, in a folder named `<archive>_unpacked` |
| Documentation bundle | `$XDG_CACHE_HOME/vlc-skin-studio/docs`, or `~/.cache/vlc-skin-studio/docs` |
| Theme installed by MCP `test_in_vlc` | VLC's skins folder, usually `~/.local/share/vlc/skins2` |

## Do I have to package a .vlt, or can people use my theme.xml?

VLC loads both, and its Change skin dialog lists both. A `.vlt` is the portable choice because it carries every referenced file; an XML file only works while the assets sit beside it at the same relative paths. When you hand the XML to someone, hand them the whole folder. [VLT archives](vlt-archives.md) covers packaging and installation.

## Why does VLC open a different layout than the editor?

The last layout in a window's list is the one VLC shows first. The editor's layout list is in file order, with the default at the bottom, so reorder the layout there to change it. It also helps to keep every layout in a window at the same size, because VLC switches between them in place.

## Why does my text show $T instead of a time?

Substitution happens for the known `$` tokens in `Text` items and tooltip attributes. In the editor the token is replaced with whatever sample value the Variables panel holds, so you can watch the layout with realistic text. An unknown token stays literal, so check the spelling against the table in [Text items](text-items.md). VLC 2.0 documents `$R` for the playback rate; it has no preview sample and stays literal in the editor preview. If VLC itself shows the raw token, the string is in an attribute where VLC does not substitute variables.

## How do I make a window resizable?

Three things together:

1. Give the layout size limits: `minwidth`, `maxwidth`, `minheight` or `maxheight`. Without them VLC does not allow a resize.
2. Give the user a handle: an image with `action="resizeE"`, `"resizeS"` or `"resizeSE"`.
3. Decide what moves: set `lefttop` and `rightbottom` on the items that should follow the edges, and `xkeepratio` / `ykeepratio` on the ones that should keep their relative position at a fixed size.

[Layouts and anchors](layouts-and-anchors.md) has the full walkthrough with the corner combinations.

## Why does the validator report duplicate ids when the theme still renders?

Because VLC resolves an id by first match. The theme loads and draws, but any later element with the same id is invisible to actions, expressions and lookups, which is a silent defect rather than a crash. The conformance sweep of the 123 official gallery themes found 76 duplicate item ids and 27 duplicate resource ids, so old themes live with this all the time. If you are editing such a theme, the Problems panel lists each duplicate; renaming the later element is usually safe unless an action targets it by id.

## Is there an AI assistant in the editor?

Not in the current version. The in-app chat panel was removed on purpose; AI clients drive the editor through the MCP server instead. See [MCP for automation](mcp-for-automation.md).
