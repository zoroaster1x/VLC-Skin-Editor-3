---
title: Welcome and your first skin
section: Start here
source: rewritten
---

# Welcome and your first skin

When the editor starts it shows the welcome card in the middle of the window. The card is the empty state: nothing is open yet, and every way to begin is one click away.

## The welcome card

The card carries the title VLC Skin Studio and three buttons:

| Button | What it does |
|---|---|
| New skin | Asks where to save a new `.xml` file, then starts an empty theme there. |
| Open skin | Opens a `.xml` theme or imports a `.vlt` archive. |
| Examples | Opens a small menu with the two built in example themes. |

Below the buttons are the Recent list and the Examples list.

The Recent list shows the files you opened last, newest first. Click a path to open that file again. Up to twelve files are remembered; File > Recent files shows the same list as a menu. The list appears again on the welcome card and in the File menu after a restart.

The Examples list shows each example with a one line description and a Create button. Create writes the example into a new folder and opens it in the editor straight away:

- It uses the last folder you worked in when there is one, otherwise your home folder.
- The folder is named `vlc-skin-neon` or `vlc-skin-panel`.
- The theme file inside is `theme.xml`, and the PNG assets are generated next to it.

The two examples are:

| Example | What it is |
|---|---|
| Neon player | A 320x140 player bar with transport buttons, a seek slider and a volume slider. |
| Flat panel | A 420x220 plain control panel with a video area and a seek slider, a good starting point for a bigger window. |

The Examples button at the top of the card opens the same two themes as a popup menu and creates the chosen one in your home folder.

## Create a new skin

There are two ways in, and they differ in one respect: the path.

- New skin on the welcome card starts an empty theme named `Untitled` in memory and writes nothing yet. Save it with Ctrl+S to choose the file.
- File > New, or Ctrl+N, asks where the theme file should live before it starts.

When the save dialog is involved:

- Type a name and confirm. The editor appends `.xml` when you leave the extension off.
- If the file already exists you are asked whether to replace it.
- The theme is written to that path immediately, so nothing is lost if you close the editor before saving again.

Either way the new skin contains one window named `main` with one layout named `main` at 320x140 pixels. The ThemeInfo name is `Untitled` until you change it in Skin settings. The canvas keeps the welcome card until you select the layout, because a brand new session has no selection yet; the window and its layout are already in the Windows and layouts tree.

## What happens after creating one

1. The title bar shows the file name, with a star while there are unsaved changes, or `Untitled` when the card button started the theme. The status bar shows the same state in orange as Unsaved changes.
2. The canvas still shows the welcome card until you select the layout in the Windows and layouts tree. The new window and layout are already listed there; selecting the layout shows it as an empty 320x140 rectangle. There are no resources and no items yet.
3. The Resources panel is where the artwork goes. Save the theme first, because Add bitmap stores paths relative to the theme file and refuses to run when there is none.
4. The Windows and layouts panel holds the window and its layout. The Items panel will hold the controls once you add them.
5. The Inspector shows the layout when you select it, so its width and height are editable right away.

The next pages walk the whole route from here: [Prepare your artwork](step-1-prepare-artwork.md), then [Start a new theme](step-2-new-theme.md).

## Open an existing skin

Pick Open skin, press Ctrl+O, or use File > Open. The chooser accepts `.xml` and `.vlt`.

- An `.xml` file opens directly, and the folder next to it is used to resolve relative asset paths.
- A `.vlt` archive goes through the import step: a confirmation asks first, then the archive is unpacked into a folder named `<archive>_unpacked` next to the archive itself, and the `theme.xml` inside is opened. If that folder already exists you are asked whether to replace it. Use File > Import VLT when you want the import without the combined open step.
- A theme opened from the gallery is unpacked under your user data folder instead; see [Theme browser](theme-browser.md).

Opening replaces the current document in the window. Unsaved changes in the old document are not saved automatically; the editor warns through the title bar and the Exit prompt, not through the Open dialog.

Next: [Window tour](window-tour.md) or [Prepare your artwork](step-1-prepare-artwork.md).
