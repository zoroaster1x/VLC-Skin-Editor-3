---
title: VLC skins2 handbook
section: Start here
source: rewritten
---

# VLC skins2 handbook

This handbook is for making a VLC skin with VLC Skin Studio. It starts with the application: how to begin a theme, where every tool lives, and each step from artwork to a packaged `.vlt`. The deep format rules are kept as an appendix, so you can read them when a reference is what you need rather than a walkthrough.

The pages are available offline in the editor's documentation viewer on F1; the viewer groups them by the same sections shown here.

## Start here

- [Welcome and your first skin](welcome-and-first-skin.md): the welcome card, new and open, the examples, recent files.
- [Window tour](window-tour.md): every panel, dockable panels, the toolbar, the status bar, the canvas controls and the three trees.
- [Menus and shortcuts](menus-and-shortcuts.md): every menu entry in plain language, plus the shortcut tables.
- [Getting started](getting-started.md): what a skin is, how VLC loads one, and the fastest path to a result.
- [Editor guide](editor-guide.md): the editor, CLI, terminal UI and MCP in one view.

## Making a skin

- [Step 1: prepare your artwork](step-1-prepare-artwork.md): plan the layout, draw and slice the PNGs, pick an alphacolor.
- [Step 2: start a new theme](step-2-new-theme.md): File > New, Skin settings, windows, layouts and sizes.
- [Step 3: add and place controls](step-3-add-and-place-controls.md): the Items panel, dragging, nudges, the action editor and tooltips.
- [Step 4: text and variables](step-4-text-and-variables.md): Text items, fonts, the Variables panel and the `$` tokens.
- [Step 5: sliders and backgrounds](step-5-sliders-and-backgrounds.md): slider values, the Path tool and the background wizard.
- [Step 6: animations and playlists](step-6-animations-and-playlists.md): frames, sub bitmaps, playlists and their slider.
- [Step 7: validate, test and export](step-7-validate-test-export.md): the Problems panel, Test in VLC, preview PNG and Export as VLT.
- [Buttons, checkboxes and images](buttons-checkboxes-and-images.md): the three click controls and their Inspector fields.
- [Text items](text-items.md): the Text attribute table and the tooltip rules.
- [Sliders](sliders.md): the slider attributes, bezier paths and radial knobs.
- [Slider backgrounds](slider-backgrounds.md): the frame grid and its counting rules.
- [Playlists and playtrees](playlists-and-playtrees.md): the list control, its slider and its colours.
- [Bitmaps and animations](bitmaps-and-animations.md): alphacolor keying, frames, sub bitmaps and the picture cache.
- [Fonts and bitmap fonts](fonts-and-bitmap-fonts.md): Font and BitmapFont resources and where they are used.

## Extras and tools

- [Theme browser](theme-browser.md): the gallery, the filter, previews, downloads and the cache.
- [Documentation viewer](documentation-viewer.md): this viewer, its search and the legacy archive.
- [MCP for automation](mcp-for-automation.md): the server, the tool groups and example requests.
- [MCP tool reference](mcp-and-ai.md): every tool name in one place.
- [VLT archives](vlt-archives.md): packaging, importing, installing and the container format.

## Troubleshooting

- [Common problems](common-problems.md): the seven failures that come up most often.
- [FAQ](faq.md): short answers to shorter questions.
- [Validation and troubleshooting](validation-and-troubleshooting.md): the validator reference and every message it can print.

## Appendix: the format explained

- [Format basics](format-basics.md): what `theme.xml` looks like, a minimal example and where resources live.
- [Theme structure](theme-structure.md): Theme, Window, Layout, Item and the id namespaces.
- [Layouts and anchors](layouts-and-anchors.md): coordinates, corner anchors, keep ratio, size limits and anchor items.
- [Items overview](items-overview.md): every control element in one table.
- [Popup menus and ini files](popupmenus-and-inifiles.md): the two theme resources with the thin editor UI.
- [Actions and variables](actions-and-variables.md): action chains, the code catalog, expressions and percentage variables.
- [Format reference](format-reference.md): generated from VLC's `skin.dtd`, every element and attribute with its default.
