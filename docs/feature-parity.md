# Feature parity audit: original VLC Skin Editor 0.8.6 vs VLC Skin Studio

Sources read:

* Original: the upstream VLC Skin Editor 0.8.6 source (Java Swing, GPL-2.0, Daniel Dreibrodt, version string `0.8.6.dev` in `src/vlcskineditor/Main.java`). `tools/parity/README.md` documents how to clone and build it for comparison runs. Paths below are relative to that root.
* Rewrite: this repository (single Gradle module, Java 25, package `dev.zoroaster1x.vlcskin`). Paths below are relative to this root.

Method: every class under `src/vlcskineditor/` (original) and `src/main/java/dev/zoroaster1x/vlcskin/` (rewrite) was read or grep-checked, plus `README.TXT`, `BUILDING.TXT`, `lang/en.txt` and `share/` for the original, and `README.md` plus the app/core packages for the rewrite. This audit did not touch build outputs.

## 0. Update: verified against VLC 4.0.0-dev (e77e49b5, 2026-10-02)

The rewrite was tested against real VLC skins2 and the original editor on an
isolated virtual display. The method, commands and lessons are in
`tools/parity/README.md`; the VLC behaviour with `file:line` citations is in
`docs/skins2-parser.md`, `docs/skins2-rendering.md` and
`docs/skins2-edge-cases.md`, and 53 runnable edge-case themes live in
`src/test/resources/edge-cases/` (opened by `format/EdgeCaseFixturesTest`).

Corpus: all 138 archives of the official gallery (46 MB) imported, validated
and rendered, one of which bundles two themes, so 139 themes in total; the
VeLoCity skins and VLC's own `default` and `winamp2.xml` themes are checked in
the same harness. Result after this update: 139/139 themes import and render,
including the Winamp2 archive the original editor refused.

Corrections to claims elsewhere in this document, which were true at some
earlier revision but are not true now:

* The AI assistant panel no longer exists. MCP is the single automation
  surface, as `AGENTS.md` states; the AI panel was deliberately removed.
* The MCP catalog is 76 tools, not 63.
* The rewrite has a working self updater: `update/UpdateService` reads the
  GitHub releases, collects missed release notes, and installs a downloaded
  jar only after its SHA-256 matches `SHA256SUMS`, with a Windows helper
  restart. Section 2 item 4 and the update table are stale.
* Section 3's threading risks are addressed: session listeners run through the
  dispatcher (event thread inline, `invokeLater` otherwise), `Studio.status`
  and `error` marshal to the event thread, `ProgressDialog` is a `FutureTask`
  disposed in `done()`, `EditorSession.revision` is atomic and `SelectionState`
  fields are volatile, `Messages` keeps a volatile language and a synchronized
  bundle cache, and `SettingsStore` serializes saves. The `AiPanel` that
  section names no longer exists.
* SubBitmap duplication exists (`EditorService.duplicateResource`,
  `DeepCopy.resource`), and `Anchor.range` is both parsed and written.
* Radial sliders are editable, and the Winamp2 and `Playlist` quirks are
  handled.

Bugs found by the comparison runs and fixed in this update, with the tests
that pin them down:

| Bug | Fix | Test |
|---|---|---|
| Inspector threw `IllegalArgumentException` on real values outside the editor's range (`maxwidth="99999"`, `alpha="0"`) | `InspectorFields.safeModel` widens the spinner range around the value; theme settings and the sub bitmap editor use it | `app/StudioUiTest.aThemeWithExtremeNumbersOpensAndBuildsBothForms` |
| Duplicate rename patterns lost every letter `s` (`replaceAll("[%s\"]", "")`) | strip only `%` and quotes, fall back to `<id>_copy` when empty | `model/SkinIndexTest` |
| `visible` expressions were never evaluated when drawing, so hidden controls painted | VLC semantics in `ItemPainters.draw`; unresolved expressions stay visible, like VLC's NULL variable | `render/VlcSemanticsTest` |
| Text was never clipped to `width`; alignment on overflow was wrong | VLC's CtrlText static-frame rules: clip and show start, middle or end per alignment | `render/VlcSemanticsTest` |
| Radial slider frame index used `floor(value * frames)` instead of VLC's `(int)(value * (frames - 1))` | exact VLC formula | `render/VlcSemanticsTest` |
| Boolean expressions did textual substitution, corrupting names that contain a known variable (`notvlc.isPlaying`) | tokenising evaluation with an unknown-aware resolver (`BooleanExpression.resolve`) | `render/BooleanExpressionTest`, `render/VlcSemanticsTest` |
| A playlist slider wrote its `value` attribute, although VLC always follows the playlist scroll position | `ItemWriter` skips `value` when `inPlaytree` | `format/SkinRoundTripTest.playlistSliderValueIsNotWritten` |
| VLT export wrote the stale on-disk theme.xml as an extra tar entry, after the current one | assets-only `referencedFiles` | `format/VltCodecTest` |
| Semicolon resource fallbacks (`id1;id2`) were unresolved, so Winamp2 themes validated as broken | `SkinIndex` tries each segment in order, like VLC's `find_first_object` | `model/SkinIndexTest` |
| IniFile color constants (`pledit.text.normal`) were unknown | `SkinIndex.constant` registers `<ini>.<section>.<key>` lowercased and `Colors.parse` resolves them | `render/VlcSemanticsTest` |
| Winamp2 archives (BMPs, no theme.xml) were rejected | bundled `winamp2.xml` template fallback in `VltCodec`, the way VLC's loader works | `format/VltCodecTest.winamp2ArchivesGainTheBundledTemplate` |

Still open and deliberately not ported yet: animation-frame preview for text
scrolling (`scrolling="auto"` draws the static frame, which is what PNG export
needs), anchor-driven resize simulation in the preview (a static preview at
the authored layout size is what VLC shows before any resize), cover art for
`art="true"` images, and per-slider preview values. Each is listed in
`docs/known-limits.md`.

Beyond the original, this update adds: Winamp2 archive import through VLC's own
template, semicolon resource fallback lists, IniFile constants as colors,
VLC's idle playlist preview, a keymap editor, a live interface scale, platform
correct config/cache/data directories under one `AppPaths` utility, numeric
pixel and region comparison tools, an MCP activity panel with a timestamped
log, and a three-way disk merge (`disk_diff`, `sync_from_disk`) so an AI
session and the window can share one file.

## 1. Feature parity table

### Menus

| Feature | Original class(es) | Our equivalent | Status | Notes |
|---|---|---|---|---|
| File menu | `src/vlcskineditor/Main.java` lines 202-253 | `app/chrome/MenuBarFactory.java` lines 42-63 | ported | Same entries plus Save as and Import VLT. |
| File > New | `Main.java` `createNew()` lines 1008-1027 | `app/Studio.java` `newSkin(Component)` lines 133-163 | ported | Both ask for a path first. |
| File > Open | `Main.java` `openFile()` lines 846-882 | `app/Studio.java` `openDialog()` lines 107-121 | ported | Original used a `.vlt` filter in Open; rewrite also has a separate Import VLT. |
| File > Save | `Skin.java` `save()` lines 233-243 | `app/Studio.java` `save()` lines 165-179 | ported | UTF-8 on both. |
| File > Save as | absent | `app/Studio.java` `saveAsDialog()` lines 181-201 | different | New in the rewrite. |
| File > Test skin in VLC | `Main.java` lines 1052-1062 | `app/StudioFrame.java` `testInVlc()` lines 255-277 | ported | Rewrite finds VLC portably; original depended on `vlc_dir`. |
| File > Export as VLT | `Main.java` lines 1065-1173 | `app/Studio.java` `exportVltDialog()` lines 249-279 | ported | Both tar.gz with theme.xml and assets. |
| File > Import VLT | absent (was Open) | `app/StudioFrame.java` `importVlt()` lines 236-242 | different | New explicit menu item. |
| File > Save current preview as image | `Main.java` lines 1176-1187 | `app/Studio.java` `renderPreviewDialog()` lines 302-316 | different | Original enabled the item only while a layout preview was shown (`Main.java` lines 1790, 1798); rewrite is always available and renders the selected or first layout. |
| File > Exit | `Main.java` `exit()` and `doExit()` lines 2035-2093 | `app/StudioFrame.java` `exit()` lines 566-591 | ported | Both prompt on unsaved changes and persist geometry. |
| Edit menu | `Main.java` lines 255-313 | `app/chrome/MenuBarFactory.java` lines 65-91 | ported | Adds Duplicate item. |
| Edit > Undo, Redo | `Main.java` lines 258-267 | `MenuBarFactory.java` lines 67-68 | ported | |
| Edit > Skin settings | `Skin.java` `showThemeOptions()` lines 245-438 | `app/dialog/ThemeSettingsDialog.java` | ported | |
| Edit > Global variables | `GlobalVariables.java` `showOptions()` lines 224-426 | `app/panel/VariablesPanel.java` | different | Rewrite is a dockable panel with live updates and text variables; original was a modal frame with booleans only. |
| Edit > Preferences | `Config.java` `showOptions()` lines 180-298 | `app/dialog/PreferencesDialog.java` | different | Different option set; see Preferences below. |
| Edit > Move up, down, left, right | `Main.java` lines 281-292, 1642-1645 | `MenuBarFactory.java` lines 83-90, `StudioFrame.java` lines 190-193 | ported | Accelerators Ctrl+arrows on both. |
| Edit > Delete item | `Main.java` lines 293-298, 1647-1655 | `MenuBarFactory.java` lines 77-81, delete in `StudioFrame.java` lines 311-386 | different | Original deleted from whichever internal frame was focused; rewrite deletes the current selection regardless of panel focus. |
| Edit > Duplicate item | absent | `MenuBarFactory.java` line 76 | different | New in the rewrite, originally only a toolbar button. |
| View menu | absent | `MenuBarFactory.java` lines 93-122 | different | New: zoom, fit, checkerboard, move/path tool, dark theme and theme list. |
| Help menu | `Main.java` lines 315-328 | `MenuBarFactory.java` lines 124-141 | ported | |
| Help > Online help (F1) | `Main.java` lines 318-322, 1198-1200 | `MenuBarFactory.java` lines 126-129 | ported | Same skinedhlp URL. |
| Help > About | `Main.java` lines 323-325, 1203-1206 | `app/dialog/AboutDialog.java` | different | Both dialogs; text and credits differ. |
| Help > Check for updates | absent in the menu | `MenuBarFactory.java` lines 130-131 | missing | Ours only opens the releases page; no version comparison from this item (the real check is `Studio.checkForUpdates()`, startup and MCP only). |

### Toolbar and keyboard shortcuts

| Feature | Original class(es) | Our equivalent | Status | Notes |
|---|---|---|---|---|
| Toolbar buttons | `Main.java` `initToolbar()` lines 760-798: open, save, undo, redo, move, path | `app/chrome/ToolBarFactory.java` lines 16-44 | ported | Rewrite adds zoom out, zoom in, fit, validate, render PNG, skin settings, variables, AI. |
| Toolbar show/hide | `Main.java` lines 733-758; `Config` key `toolbar` | `StudioFrame.java` lines 148-172, `StudioSettings.showToolbar` | ported | Live toggle from Preferences. |
| Toolbar floating and orientation | `Main.java` `saveToolbarState()` lines 800-822; `Config` keys `toolbar.floating`, `toolbar.x`, `toolbar.y`, `toolbar.orientation`, `toolbar.constraints` | `StudioFrame.java` lines 159-172, 585-586; `StudioSettings.toolbarFloating`, `toolbarOrientation` | different | Rewrite restores orientation and floating state but not the floating window position or the dock constraint. |
| Ctrl+N, Ctrl+O, Ctrl+S | `Main.java` lines 208, 213, 218 | `MenuBarFactory.java` lines 44-46 | ported | Menu accelerators. |
| Ctrl+Shift+T (test), Ctrl+Shift+V (export VLT) | `Main.java` lines 223, 228 | `MenuBarFactory.java` lines 51-52, 58-59 | ported | |
| Ctrl+Z, Ctrl+Y | `Main.java` lines 261, 266 | `MenuBarFactory.java` lines 67-68 | ported | |
| Ctrl+I (theme), Ctrl+G (variables) | `Main.java` lines 271, 275 | `MenuBarFactory.java` lines 70-72 | ported | |
| Ctrl+arrows move selected item | `Main.java` lines 282-291 | `StudioFrame.java` `installShortcuts()` lines 190-193 | ported | |
| Delete or Cmd+Backspace | `Main.java` lines 293-297 | `StudioFrame.java` lines 186-189 | ported | |
| F1 help | `Main.java` line 322 | `MenuBarFactory.java` line 128 | ported | |
| Ctrl+wheel and Ctrl+0 zoom, Ctrl+D duplicate | absent (zoom only via preview buttons) | `CanvasPanel.java` lines 336-344, `StudioFrame.java` lines 194-197 | different | New bindings. |
| Alt+F4 | `Main.java` line 238 | not bound | different | Handled by the window manager in the rewrite. |

### Window, layout, resource and item trees

| Feature | Original class(es) | Our equivalent | Status | Notes |
|---|---|---|---|---|
| Resources tree | `Main.java` lines 343-412 and 591-598 | `app/panel/ResourcesPanel.java` | ported | |
| Resources toolbar | add bitmap/sbmp popup, add font, duplicate, edit, delete (`Main.java` lines 360-388) | add bitmap, add font, duplicate, delete, reload images (`ResourcesPanel.java` lines 26-30) | different | Edit moved to the inspector; Add SubBitmap moved to the context menu and the bitmap/dialog; reload images is new. |
| Resources context menu | none (double click edited) | `ResourcesPanel.java` lines 140-155 | different | New: add bitmap, add font, add sub bitmap, duplicate, delete, reload. |
| Windows tree | `Main.java` lines 414-498 | `app/panel/StructurePanel.java` | ported | |
| Windows toolbar | add window, add layout, move layout up/down, duplicate, edit, delete (`Main.java` lines 431-474) | add window, add layout, move up, move down, duplicate, delete (`StructurePanel.java` lines 20-25) | ported | Edit moved to the inspector. |
| Selecting a window | Clears the preview and the items tree (`Main.java` lines 1780-1792) | `SelectionState.selectWindow` clears the layout (`SelectionState.java` lines 35-39), canvas shows the welcome card, but `ItemsPanel.refresh()` falls back to `currentLayout()` (`ItemsPanel.java` lines 34-37) | different | The items tree keeps showing the first layout while a window is selected. |
| Items tree | `Main.java` lines 500-577 | `app/panel/ItemsPanel.java` | ported | |
| Items toolbar | add popup, move up/down, duplicate, edit, delete (`Main.java` lines 520-555) | add, up, down, duplicate, delete (`ItemsPanel.java` lines 24-28) | ported | Edit moved to the inspector. |
| Items context menu | none (add popup had an "Add to selected Panel" submenu, `Main.java` lines 600-658) | `ItemsPanel.java` lines 171-190 | different | New right-click menu; the panel submenu is replaced by adding into the selected container implicitly. |
| Adding an item as a sibling | Adds into the parent list of the selected item (`Main.java` lines 1545-1598) | Adds to the layout root unless the selection is a container (`ItemsPanel.java` lines 127-149) | different | Nesting of the current selection is not respected. |
| Tree labels and icons | `TreeRenderer.java` | `AbstractTreePanel.RefRenderer` lines 182-227 | ported | Same "Type: id" labels, vector icons instead of PNGs. |
| Double click on a tree node | Opens the modal editor (`Main.java` lines 1846-1851) | Selects the node for the inspector (`AbstractTreePanel.java` lines 50-55, `ResourcesPanel.java` lines 80-87) | different | Editing is inspector based now. |
| Tree selection echo guard | none (rebuilds reselect via `expandItem` etc.) | `Panels.java` `refreshing` guard lines 20-64 | different | New, prevents selection loops. |
| Tooltips on tree nodes | `ToolTipManager` registration only (`Main.java` lines 350, 421, 507) | none | different | Minor. |

### Property editors, all control types

| Feature | Original class(es) | Our equivalent | Status | Notes |
|---|---|---|---|---|
| Editor form | one modal `JFrame` per item/resource with OK, Cancel, Help (`Item.java` `showOptions`) | one inspector form with commit-on-change rows (`app/panel/InspectorPanel.java`, `app/inspector/InspectorFields.java`) | different | Same fields, no per-dialog OK step, every change is one undo step. |
| Common item attributes | id, x, y, visible, lefttop, rightbottom, xkeepratio, ykeepratio, help in every dialog | `InspectorPanel.itemForm()` lines 171-215 | ported | Required-field checks from the original dialogs are enforced at MCP/validator level instead. |
| Anchor | `items/Anchor.java` | `InspectorPanel.anchorForm()` lines 334-344 | ported | points, priority, range. |
| Button | `items/Button.java` | `InspectorPanel.buttonForm()` lines 346-358 | ported | up, over, down, action, tooltiptext. |
| Checkbox | `items/Checkbox.java` | `InspectorPanel.checkboxForm()` lines 360-388 | ported | state plus both image/action sets. |
| Group | `items/Group.java` | common attributes only | ported | Group had no extra fields in the original either. |
| Image | `items/Image.java` | `InspectorPanel.imageForm()` lines 390-406 | ported | image, resize (mosaic, scale, scale2), action, action2, art. |
| Panel | `items/Panel.java` | `InspectorPanel.panelForm()` lines 408-416 | ported | width, height. |
| Playtree and Playlist | `items/Playtree.java` | `InspectorPanel.playtreeForm()` lines 418-457 | ported | font, bgimage, item/open/closed icons, flat, five colors, edit playlist slider button. Playlist syntax hides folder rows. |
| Radial slider | `items/RadialSlider.java` refused editing with "not yet supported" | `InspectorPanel.radialForm()` lines 459-473 | different | The rewrite supports sequence bitmap, nbimages, min/max angle, value, tooltip. |
| Slider | `items/Slider.java` | `InspectorPanel.sliderForm()` lines 475-532 | ported | up, over, down, points, thickness, value list, tooltip, background enable/generate and frame fields. Playlist sliders show "Playtree scrolling". |
| Slider background | `items/SliderBackground.java` | `InspectorPanel.backgroundForm()` lines 582-594 | ported | image, nbhoriz, nbvert, padhoriz, padvert, wizard button. |
| Text | `items/Text.java` | `InspectorPanel.textForm()` lines 596-614 | ported | text, font, color picker, width, alignment, scrolling. |
| Video | `items/Video.java` | `InspectorPanel.videoForm()` lines 616-624 | ported | width, height, autoresize. |
| Window | `Window.java` | `InspectorPanel.windowForm()` lines 737-748 | ported | id, visible, x, y, dragdrop, playondrop. |
| Layout | `Layout.java` | `InspectorPanel.layoutForm()` lines 715-735 | ported | id, width, height, min/max width and height, last-layout note. |
| Bitmap | `resources/Bitmap.java` | `InspectorPanel.bitmapForm()` lines 627-667 | ported | file with chooser, alphacolor, nbframes, fps, sub bitmap list. |
| Font | `resources/Font.java` | `InspectorPanel.fontForm()` lines 669-683 | ported | file, size. The original warned that OTF cannot be previewed; the rewrite falls back to Sans Serif and says so. |
| Sub bitmap properties | `resources/SubBitmap.java` | `InspectorPanel.subForm()` lines 247-294 | ported | id, x, y, width, height, nbframes, fps, outside-parent warning, visual edit, delete. |
| Color choosers | `WIN_BITMAP_CHOOSE`, `WIN_PLAYTREE_CHOOSER_TITLE`, `WIN_TEXT_CHOOSER_TITLE` with `JColorChooser` | `InspectorFields.color()` lines 104-130 | ported | |
| Action editor button | `ActionEditor.java`, `ActionPanel.java` | `app/dialog/ActionEditorDialog.java` | ported | Same groups and parameter handling. |
| Numeric field guard | `NumbersOnlyDocument.java` | spinners with `SpinnerNumberModel`, numeric parse in `mcp/PropertyAccess.java` | different | No typing restriction on all text fields; invalid numbers are rejected on commit. |
| Per-dialog Help buttons | every original editor | `InspectorPanel.helpButton()` lines 234-245, theme dialog | ported | Rewrite opens skinedhlp pages per item or resource instead of per dialog. |

### Sub bitmap editor

| Feature | Original class(es) | Our equivalent | Status | Notes |
|---|---|---|---|---|
| Visual sub bitmap cutter | `resources/SubBitmapEditWindow.java` | `app/dialog/SubBitmapEditorDialog.java` | ported | |
| Zoom controls | `-` and `+` buttons, 1x to 16x, blinking red frame | fit-to-window scaling, no zoom buttons, static orange frame | different | Drag behavior and field sync are equivalent. |
| Frames and fps editing | fields in `resources/SubBitmap.java` | spinners in the dialog | ported | |
| Undo | none for the visual drag (the text fields commit through the normal dialog) | one undoable command per Apply (`SubBitmapEditorDialog.java` lines 115-148) | different | Rewrite is undoable. |

### Slider background generator

| Feature | Original class(es) | Our equivalent | Status | Notes |
|---|---|---|---|---|
| Generator | `SliderBGGen.java` two-step wizard plus `SliderBGBuilder.java` | `app/dialog/SliderBackgroundGeneratorDialog.java` plus `render/SliderBackgroundGenerator.java` | ported | |
| Inputs | direction, width, height, four margins, background, start edge, middle (required), end edge, overlay, tile or stretch for background and middle (`SliderBGGen.java`, `SliderBGBuilder.java`) | same inputs, tile via two checkboxes, stretch is the unchecked state | different | Single form instead of Next/Previous cards. |
| Output | writes `<id>_bg.png` next to the skin, adds a Bitmap, sets frame counts and zero padding (`SliderBGGen.java` lines 406-435) | writes `<background id>_bg.png`, registers a Bitmap, sets frame counts and zero padding (`SliderBackgroundGeneratorDialog.java` lines 148-226) | ported | Rewrite only registers the bitmap when the skin has a file; otherwise it writes to `~/.config/vlc-skin-studio/exports`. |
| Missing middle image error | `ERROR_SBGGEN_MIDDLE_MSG` | `SliderBackgroundGeneratorDialog.java` lines 149-155 | ported | |

### Theme settings, preferences and look and feel

| Feature | Original class(es) | Our equivalent | Status | Notes |
|---|---|---|---|---|
| Theme info | name, author, email, webpage (`Skin.java` lines 252-259) | `ThemeSettingsDialog.java` lines 33-44, `InspectorPanel.themeForm()` | ported | |
| Theme attributes | magnet, alpha, movealpha with range checks (`Skin.java` lines 260-271, 408-415) | `ThemeSettingsDialog.java` lines 37-48, inspector with spinner ranges | ported | |
| tooltipfont | stored and written but not editable (`Skin.java` lines 71, 610) | not in either dialog; editable through MCP `set_theme_property` (`EditorService.java` lines 692-695) | different | Gap in both GUIs; rewrite at least exposes it through MCP. |
| Theme settings undo | `ThemeEditEvent` | one `ValueCommand` for the whole dialog (`ThemeSettingsDialog.java` lines 58-70) | ported | |
| Preferences | autoupdate, language, look-and-feel (System, Metal Steel, Metal Ocean), show toolbar, restart note (`Config.java` lines 180-298) | theme, language, canvas background, checkerboard, show toolbar (`PreferencesDialog.java`) | different | Autoupdate moved to the Help menu and now reads the GitHub releases with every missed patch note and installs the jar after a SHA-256 check; FlatLaf themes replace the three Swing LAFs; no restart note needed because language applies live. |
| Look and feel | `Main.main()` lines 2110-2131 | `app/theme/ThemeManager.java` | different | FlatLaf dark/light plus accent; original used the platform LAF or Metal. |
| Preference storage | `Config.java` `VLCSkinEditor.cfg` key pipe value in `~/.vlc` (Windows `%APPDATA%\vlc`) | `app/config/SettingsStore.java` JSON in `~/.config/vlc-skin-studio/settings.json` | different | |
| Window geometry | `Config.java` keys `win.*`, restored per internal frame (`Main.java` lines 2077-2088) | `StudioSettings.windowX/Y/Width/Height/Maximized` plus `layout.xml` for dock panels (`StudioFrame.java` lines 498-536) | different | Dock layout persistence is a new mechanism. |

### Language support

| Feature | Original class(es) | Our equivalent | Status | Notes |
|---|---|---|---|---|
| Language files | 22 files in `lang/` (`en`, `ca`, `cz`, `de`, `es`, `et`, `eu`, `fr`, `he`, `it`, `ko`, `nl`, `pl`, `pt-br`, `ru`, `sk`, `sr.cyr`, `sr.lat`, `sv`, `tr`, `zh-tw`, plus `languages.txt`) | 21 bundles in `src/main/resources/dev/zoroaster1x/vlcskin/app/messages/` plus `languages.properties`; `app/i18n/Messages.java` | ported | Same 21 languages (`sr-cyr` and `sr` merge the original `sr.cyr` and `sr.lat`). |
| Loading | `Language.java` reads `lang/*.txt` from disk next to the jar | classpath resource bundles | different | Runtime user-supplied translations are no longer possible without repackaging. |
| System locale default | `Language.isLanguageAvailable(user.language)` (`Config.java` lines 48-49) | `SettingsStore.defaultLanguage()` lines 51-56 | ported | |
| Restart requirement | Preferences note says some settings need a restart | language applies immediately (`Messages.setLanguage` from `PreferencesDialog.java` lines 82-86) | different | |
| Mnemonics | every menu item has a language-file mnemonic | top-level menus only; menu items use fixed accelerators (`MenuBarFactory.java`) | different | Translated mnemonic letters are not restored. |

### Undo and redo

| Feature | Original class(es) | Our equivalent | Status | Notes |
|---|---|---|---|---|
| History | `history/History.java` doubly linked list, limit 50 | `edit/CommandStack.java`, limit 50 | ported | |
| Undo labels | menu text updated with the event description (`Main.setUndoString()`) | `MenuBarFactory.refreshUndoLabels()` lines 145-168 | ported | |
| Undoable operations | about 30 event classes in `history/`; item moves, deletes, additions, dialogs, theme edits | `edit/commands/` plus `ValueCommand`, all service mutations | ported | |
| Known exception | slider background deletion is explicitly not undoable (`Main.java` line 1684 `TODO make undoable`) | delete of a slider background is a `ValueCommand` (`EditorService.java` lines 380-389) | different | Rewrite fixes it. |
| Tree drag and drop | direct list mutation, not undoable (`ItemTransferHandler.java` lines 138-145) | `MoveNodeCommand` (`app/panel/ItemTransferHandler.java` lines 145-148) | different | Rewrite adds undo and cycle protection. |
| Arrow key coalescing | `ItemMoveEvent` reuses the last event (`PreviewWindow.moveItem()` lines 383-400) | `EditorSession.nudge()` and `NudgeCommand` (`EditorSession.java` lines 179-234) | ported | |

### Copy, duplicate, delete

| Feature | Original class(es) | Our equivalent | Status | Notes |
|---|---|---|---|---|
| Duplicate resources | `Main.java` lines 1279-1309, handles Bitmap, SubBitmap, Font | `ResourcesPanel.duplicate()`, `DeepCopy.resource()`, MCP `duplicate_resource` | different | SubBitmap duplication is missing in the rewrite (see section 2). |
| Duplicate windows and layouts | `Main.java` lines 1311-1332 | `StructurePanel.duplicate()`, `EditorService.duplicateWindow/Layout()` lines 878-899 | ported | Original appended the layout copy at the end; rewrite inserts it after the source. |
| Duplicate items | `Main.java` lines 1334-1427; refuses SliderBackground and playlist sliders | `EditorService.duplicateItem()` lines 457-483; same refusals | ported | Rewrite inserts after the source; original appended. |
| Rename pattern | `%oldid%_copy` prompt | same prompt and `DeepCopy` pattern | ported | |
| Copy and paste to a clipboard | absent | absent | different | Neither tool has clipboard copy or paste; Duplicate is the only copy operation. |
| Delete confirmations | `DEL_CONFIRM_MSG` prompt | same key reused | ported | |
| Resource in use check | `Skin.isUsed()` (`Skin.java` lines 513-521) | `SkinIndex.isResourceUsed()` used from `StudioFrame.deleteSelected()` lines 328-335 and MCP | ported | |

### Zoom and view

| Feature | Original class(es) | Our equivalent | Status | Notes |
|---|---|---|---|---|
| Preview zoom | 1x to 16x buttons in `PreviewWindow.java` lines 208-223 | 1x to 16x buttons, label, Ctrl+wheel, fit (`CanvasPanel.java` lines 109-127, 336-344, 510-530) | ported | |
| Checkerboard | fixed white and light gray grid (`PreviewWindow.java` lines 185-193) | configurable checkerboard with View menu and Preferences | different | New option. |
| Preview title | internal frame title "Window: X - Layout: Y" (`PreviewWindow.java` line 149) | layout label in the canvas controls row (`CanvasPanel.java` lines 132-141) | ported | |
| Selection, hover and pressed rendering | item flags `setSelected`, `setHover`, `setClicked` | `RenderOptions` selection, hover, pressed (`render/RenderOptions.java`) | ported | |
| Animation | `FrameUpdater` thread at 5 fps idle, 25 fps while pressed (`FrameUpdater.java`, `PreviewWindow.java` lines 340, 351) | Swing timer at the fastest bitmap fps, tick 0 for static and exported renders (`CanvasPanel.java` lines 180-187, `snapshot/PreviewSnapshot.java`) | ported | |

### Validation

| Feature | Original class(es) | Our equivalent | Status | Notes |
|---|---|---|---|---|
| Static validation | none; dialogs checked ids, sizes, bitmap and font existence per field | `format/SkinValidator.java`, `app/panel/ProblemsPanel.java`, CLI `validate` | different | New feature: versions, ids, references, files, sizes, colors. |
| Parse warnings | exceptions and dialogs during open (`Skin.java` lines 137-145) | `format/ParseIssue.java` collected and shown in Problems | different | Rewrite keeps a document open with issues instead of failing. |
| Id validity and uniqueness | `Skin.idExists()` (`Skin.java` lines 445-455) and per dialog `ERROR_ID_EXISTS_MSG` | `SkinIndex.idExists()`, `EditorService.idProblem()` lines 732-743, validator duplicate checks | ported | |
| Problems navigation | none | double click a problem jumps to the element (`ProblemsPanel.navigate()` lines 89-115) | different | New. |

### VLT import and export

| Feature | Original class(es) | Our equivalent | Status | Notes |
|---|---|---|---|---|
| Import VLT | `Main.java` lines 899-985: zip then tar.gz, unpack to `<name>_unpacked`, open theme.xml | `format/VltCodec.java`, `Studio.importVlt()` lines 206-247 | ported | Rewrite rejects entries that would escape the target folder and recurses into nested `.vlt` zips. |
| Import confirmation | `VLT_EX_MSG` prompt | reused key (`Studio.java` lines 209-217) | ported | |
| Export VLT | `Main.java` lines 1065-1173: tar.gz, theme.xml first, then Bitmap and Font files | `VltCodec.write()` lines 106-130 | ported | Original had a Windows Vista VirtualStore workaround; dropped. |
| Export success and failure dialogs | `VLT_SUCCESS_*`, `ERROR_VLT_*` | same keys | ported | |
| Missing asset handling | silently logs to stderr | exporter skips missing files and the dialog warns (`Studio.java` lines 259-273) | different | Rewrite is louder. |

### VLC launch and test

| Feature | Original class(es) | Our equivalent | Status | Notes |
|---|---|---|---|---|
| Find VLC | Windows Program Files plus registry through `src/com/ice/jni/registry/*` and `ICE_JNIRegistry.dll` (Windows only); Linux hardcoded `~/.local/share/vlc/skins2` (`Main.java` lines 702-731) | `app/VlcLauncher.java` and `util/VlcFinder.java`, with Windows, macOS, Linux, Flatpak and PATH fallback | different | The Windows JNI registry code and DLL are dropped. |
| Launch command | `vlc -I skins2 --skins2-last=<theme> --skins2-systray` (`Main.java` line 1055) | same, plus Flatpak variant (`VlcFinder.launch()` lines 112-126) | ported | |
| Desktop Test item | saves then launches the XML directly (`StudioFrame.java` lines 255-277) | ported | | |
| MCP `test_in_vlc` | absent | installs a temporary VLT into VLC's skins folder first (`EditorControl.java` lines 301-319) | different | Two code paths exist; `VlcLauncher` (desktop) and `VlcFinder` (tools) duplicate the discovery logic. |

### Update check

| Feature | Original class(es) | Our equivalent | Status | Notes |
|---|---|---|---|---|
| Startup check | `Main.update()` lines 1933-2030, skipped for dev builds, auto-update default true (`Config.java` line 47) | `Studio.checkForUpdates()` lines 363-387, GitHub releases API, default false (`StudioSettings.java` line 28) | ported | |
| Download and install | downloads a zip, `Helper.unzip`, restarts `.exe` or jar (lines 1948-2011) | only a dialog with the releases URL | missing | Rewrite cannot update itself. |
| Settings wiring | Preferences checkbox | Help menu checkbox (`MenuBarFactory.java` lines 132-138) | different | |

### About box

| Feature | Original class(es) | Our equivalent | Status | Notes |
|---|---|---|---|---|
| About | `JOptionPane` with the app icon and HTML credits (`Main.java` lines 1203-1206, `lang/en.txt` `ABOUT_MSG`) | `app/dialog/AboutDialog.java`, JDialog | different | Same intent, new text; rewrite credits the original editor and states GPL derivative status. |

### Drag and drop

| Feature | Original class(es) | Our equivalent | Status | Notes |
|---|---|---|---|---|
| Item tree drag and drop | `ItemTransferHandler.java`, MOVE only, no undo, background thread refreshes the tree | `app/panel/ItemTransferHandler.java`, MOVE only, undoable, refuses self-drop, refuses dragging playlist sliders and backgrounds | ported | Rewrite closes the original thread hack (`ItemTransferHandler.java` lines 149-168) and the missing undo. |
| Drop into panels | drops into a Panel only (`ItemTransferHandler.java` lines 140-145) | drops into Group or Panel, insert between rows, reparent to root via MCP | different | Rewrite is more permissive and stricter about illegal drops. |
| Window dragdrop and playondrop | XML attributes with checkboxes in the window editor (`Window.java`) | same attributes in `InspectorPanel.windowForm()` and `setWindowProperty` | ported | |
| File drop onto the editor | absent | absent | different | Neither supports dropping media or images onto the window. |

### Playlist and playtree specifics

| Feature | Original class(es) | Our equivalent | Status | Notes |
|---|---|---|---|---|
| One control, two spellings | `items/Playtree.java` only | `model/item/PlaytreeItem.java` models `Playtree` and `Playlist` through `playlistSyntax` | different | Rewrite supports the `Playlist` element name and hides folder rows for it. |
| Nested slider | child slider in the tree (`Playtree.java` lines 1081-1083) | `PlaytreeItem.children()` returns the slider (lines 60-62) | ported | |
| Create slider with the playlist | `new Playtree(s)` creates a slider (`Playtree.java` lines 160-165) | `ItemFactory.create()` lines 57-67 | ported | |
| Edit playlist slider | button in the playtree dialog opens the slider editor (`Playtree.java` lines 841-843) | button selects the slider item (`InspectorPanel.playtreeForm()` lines 449-456) | different | Same effect, inspector instead of a dialog. |
| Slider value shows scrolling | `WIN_SLIDER_VALUE_SCROLL` when in a playlist | same, read-only combo (`InspectorPanel.sliderForm()` lines 487-492) | ported | |
| One slider per playlist, one background per slider | enforced by duplicate refusals (`Main.java` lines 1407-1413) and add rules | enforced in `EditorService.addItem()` lines 298-332 and duplicate refusals | ported | |

### Bitmap font, ini file and popup menu

| Feature | Original class(es) | Our equivalent | Status | Notes |
|---|---|---|---|---|
| Bitmap font support | `resources/BitmapFont.java` is dead code: `Skin.parseNode()` never instantiates it, `showOptions()` says "not yet supported" | `model/resource/BitmapFontResource.java`, parsed by `format/parse/ResourceParser.java` lines 91-98, inspector edits id, file, type | different | Rewrite stores and preserves bitmap fonts; neither renders them. The original dropped them on save. |
| Ini file support | not parsed | `model/resource/IniFileResource.java`, parsed and preserved, inspector edits id and file | different | No content editor in either; the original lost the element. |
| Popup menu support | not parsed | `model/resource/PopupMenuResource.java` with `MenuItemEntry` and `MenuSeparatorEntry`, parsed and preserved | different | No entry editor yet: the inspector shows the "no editable attributes" note, and MCP can only set the id (`mcp/PropertyAccess.java` lines 374-377). The original dropped the element. |
| Unknown attribute and child preservation | not done; the DOM is re-serialized from known fields | `SkinNode.foreignAttributes()`, `unknownChildren()`, every writer calls them last (documented in `AGENTS.md` section 6) | different | Rewrite guarantee, original could lose data. |

### Global variables

| Feature | Original class(es) | Our equivalent | Status | Notes |
|---|---|---|---|---|
| Boolean simulation | 17 combos in `GlobalVariables.showOptions()` lines 233-346 | `GlobalVariableCatalog.BOOLEANS`, `VariablesPanel` | ported | Original dialog had label bugs (`vlc.isLoop`, `vlc.isRepeat`) and evaluated `vlc.isStopped` as false; rewrite lists all correctly. |
| Text variables | fixed samples only, never editable | editable fields for `$B $V $T $t $L $l $D $d $H $N $F $S` | different | New editing capability. |
| Slider position | `JSlider` 0 to 100, float value | same via `PreviewVariables.sliderValue` | ported | |
| Variable substitution | regex `replaceAll` (`GlobalVariables.parseString()` lines 99-113) | literal `String.replace` (`render/PreviewVariables.java` per `AGENTS.md`) | different | Same result, no regex surprises on `$` values. |
| Boolean expression evaluation | `BooleanExpressionEvaluator.java` plus string replacement (`GlobalVariables.parseBoolean()` lines 121-223) | `render/BooleanExpression.java` | ported | |
| Update timing | variables applied on OK, then all items refreshed | live on every control change | different | |

### Tools and wizards, plus surfaces the original did not have

| Feature | Original class(es) | Our equivalent | Status | Notes |
|---|---|---|---|---|
| Action sequence editor | `ActionEditor.java`, `ActionPanel.java` | `app/dialog/ActionEditorDialog.java`, `action/ActionCatalog.java`, `action/ActionChain.java` | ported | Original declared an "always on top" action but never added it to the popup; rewrite includes `vlc.onTop()`. |
| Slider background wizard | `SliderBGGen.java` | `SliderBackgroundGeneratorDialog.java` | ported | See above. |
| Welcome dialog | `Main.showWelcomeDialog()` lines 827-841, shown when no file is open | `WelcomeCard` inside the canvas, shown when no layout is selected | different | Rewrite is an empty state with new, open, recent files and examples; no quit option. |
| Examples generator | absent | `example/ExampleSkins.java` | different | New. |
| CLI | absent | `cli/SkinStudioCli.java`: `mcp`, `render`, `inspect`, `validate`, `new`, `vlt`, `tui`, `examples` | different | New. |
| Terminal UI | absent | `tui/TuiShell.java`, `tui/TuiLoop.java`, `tui/AsciiRenderer.java` | different | New. |
| MCP server | absent | `mcp/McpToolset.java` (76 tools), `mcp/McpServerRunner.java` | different | New; tools mirror every editor operation, including `disk_diff` and `sync_from_disk` for a file shared with a running window. |
| AI assistant | absent | removed | different | The in-app chat panel was tried and deliberately removed; MCP is the single automation surface, as `AGENTS.md` states. |
| XML source panel | absent | `app/panel/XmlPanel.java` with apply, refresh, copy, select all | different | New. |
| Status bar | absent | `app/chrome/StatusBar.java` | different | New. |
| Dockable panels | fixed `JDesktopPane` with three internal frames and a preview frame | ModernDocking panels with saved `layout.xml` | different | New layout mechanism; resources, structure, items, canvas, inspector, variables, problems and XML panels. |
| Headless UI harness | absent | `app/HeadlessStudio.java` | different | New; drives the same panels for tests and MCP UI description. |

## 2. Missing or different

Ordered by importance, with concrete file references in both trees.

1. Save preview PNG behavior differs. Original `src/vlcskineditor/Main.java` lines 1176-1187 kept the menu item disabled until a layout preview existed and saved exactly the shown layout. Rewrite `src/main/java/dev/zoroaster1x/vlcskin/app/Studio.java` lines 302-316 always offers the dialog and falls back to `session.currentLayout()`, and `snapshot/PreviewSnapshot.java` lines 43-52 renders without checkerboard or overlays. Also original `PreviewWindow.savePNG()` lines 405-420 hides the selection before writing; the rewrite relies on `RenderOptions.withoutOverlays()` (`render/RenderOptions.java` lines 58-61), which disables the selection, hover, pressed and anchor helper overlays.
2. Menu item mnemonics are not translated. Original every menu entry sets a language-file mnemonic (`src/vlcskineditor/Main.java` lines 203-324). Rewrite `src/main/java/dev/zoroaster1x/vlcskin/app/chrome/MenuBarFactory.java` lines 42-141 sets mnemonics only on the top-level menus and uses fixed accelerators, so translated alt keys are lost.
3. Double click no longer opens an editor. Original `src/vlcskineditor/Main.java` lines 1846-1851 opened the item, resource, window or layout dialog. Rewrite `src/main/java/dev/zoroaster1x/vlcskin/app/panel/AbstractTreePanel.java` lines 50-55 and `ResourcesPanel.java` lines 80-87 make double click a selection that fills the inspector. This is a deliberate UX change but a workflow difference.
4. Preferences differ in options. Original `src/vlcskineditor/Config.java` lines 180-298 offered autoupdate, language, one of three Swing LAFs, and a toolbar toggle. Rewrite `src/main/java/dev/zoroaster1x/vlcskin/app/dialog/PreferencesDialog.java` offers theme, language, interface size, a keyboard shortcut editor, canvas background, checkerboard, toolbar visibility and the AI and MCP section; autoupdate lives in the Help menu and is a real release check with the missed patch notes and a SHA-256 verified self update; there is no LAF choice (FlatLaf themes instead).
5. Windows-only and dead code in the original that the rewrite drops: `src/com/ice/jni/registry/*` (JNI registry), `share/ICE_JNIRegistry.dll`, `share/VLCSkinEditor.exe`, `SkinEditorInstaller.iss`, `share/vlcskineditor.jsmooth`; `resources/BitmapFont.java` is dead (never instantiated by `Skin.parseNode()`); `items/RadialSlider.java` refuses editing; the `vlc.onTop()` action is declared at `src/vlcskineditor/ActionEditor.java` line 73 but never added to the popup.
6. Popup menu entries have no editor in the rewrite. `src/main/java/dev/zoroaster1x/vlcskin/model/resource/PopupMenuResource.java` models `MenuItem` and `MenuSeparator` and `format/parse/ResourceParser.java` lines 100-121 parses them, but `app/panel/InspectorPanel.java` lines 114-125 falls through to the "no editable attributes here" note and `mcp/PropertyAccess.java` lines 374-377 allows only the id. The original did not parse the element at all, so this is preserved data without a UI, not a regression.
7. Ini file content cannot be edited in either tree; the rewrite adds the resource and metadata only (`model/resource/IniFileResource.java`, `InspectorPanel.iniForm()` lines 707-712).
8. Bitmap fonts are stored but not rendered in the rewrite, and were not even parsed in the original (`resources/BitmapFont.java` line 33 says "nor are they displayed in the preview"). `mcp/PropertyAccess.java` lines 368-373 exposes id, file and type.
9. Duplicate insert positions differ for items and layouts. Original appended duplicates to the end of the parent list (`src/vlcskineditor/Main.java` lines 1340-1427). Rewrite inserts after the source (`mcp/EditorService.java` lines 480, 896-897).

## 3. Java modernization opportunity

### Language features the code could use better

* Sealed command hierarchy. `src/main/java/dev/zoroaster1x/vlcskin/edit/Command.java` is a plain interface. `ValueCommand`, `AddNodeCommand`, `MoveNodeCommand`, `RemoveNodeCommand`, `ReorderCommand` and the private `EditorSession.NudgeCommand` (`edit/EditorSession.java` lines 192-234) should be sealed with `NudgeCommand` moved into `edit/commands/` and `CommandStack.run/undo/redo` switched over them. The model hierarchy is already ahead here: `model/resource/Resource.java` line 10 is `sealed interface Resource permits AbstractResource` and `model/item/Item.java` line 10 is sealed.
* Records. The code already uses records well (`mcp/ToolOutcome.java`, `format/ParseIssue.java`, `app/panel/TreeRef.java`, `render/RenderOptions.java`, `VltCodec.Contents`, `GlobalVariableCatalog` entries). Remaining value carriers that could become records: `snapshot/PreviewSnapshot.Result` is one, but `edit/SelectionState` and the Lombok model classes cannot (mutable). The Lombok `@Getter/@Setter` model classes are fine to keep, but `config/StudioSettings` could move to a record plus withers if hot-reload semantics were wanted.
* Switch patterns. `mcp/EditorService.java` `setWindowValue()` and `setLayoutValue()` (lines 795-806, 839-851) and the parallel `previous` switches in `setWindowProperty`/`setLayoutProperty` duplicate string dispatch that a single sealed command or an enum keyed map would remove. `render/draw/ItemPainters.java` and `SkinValidator` already use pattern switches and remain the model to follow.
* Text blocks. The writers build static fragments with concatenation, for example the DOCTYPE in `format/SkinWriter.java` and the generated comment in `format/ThemeWriter.java`, and `app/dialog/AboutDialog.java` lines 32-39 builds HTML. Text blocks would make those readable without changing output; the dynamic attribute emission in `format/ItemWriter.java` must stay string built.
* Scoped values. `app/i18n/Messages.java` lines 26-27 keeps `language` and the bundle cache as static mutable state, and `app/Studio.java` swaps `settings` with `replaceSettings()` (line 58) without synchronization. A `ScopedValue` for the active language or settings during a tool call would remove the static state; at minimum the static fields need `volatile` or an immutable holder.
* Sequenced collections. `edit/CommandStack.java` lines 55-63 uses `commands.remove(commands.size() - 1)` and `commands.remove(0)`; `removeLast()` and `removeFirst()` are the direct fits. `config/StudioSettings.remember()` lines 37-43 does `recentFiles.remove(file)` plus `add(0, file)`; `SequencedCollection` accessors make the intent explicit.

### Threading risks

* Off-EDT Swing from session listeners. `edit/EditorSession.fireChanged()` (lines 138-143) runs listeners on the calling thread. `app/panel/Panels.java` line 33 registers `Panels.refresh()`, `app/StudioFrame.java` line 121 registers `onSessionChanged()` (title, menus, toolbar, status bar), and `app/HeadlessStudio.java` lines 105-111 registers another UI listener. All `mcp/EditorService.java` entry points are callable from the MCP server thread, and they call `session.apply(...)` and `fireChanged()`, so a tool call mutates Swing components off the EDT. The `synchronized` methods in `EditorService` serialize calls but do not move listener work onto the EDT. A fix is either `fireChanged()` marshalling listeners through `SwingUtilities.invokeLater`, or listeners that marshal themselves.
* Off-EDT status updates. `app/Studio.java` `status()` lines 88-92 notifies listeners on the caller thread; `checkForUpdates()` lines 363-387 calls `status(...)` from its worker, so `app/chrome/StatusBar.setMessage()` runs off the EDT. `Studio.error()` lines 434-437 opens a `JOptionPane` from whatever thread calls it.
* Off-EDT text area updates. `app/panel/AiPanel.java` `send()` lines 127-137 calls `append()` on the worker thread, and `append()` lines 140-143 mutates the transcript `JTextArea`; only the final `fireChanged()` is marshalled.
* Background work that swaps the session. `app/Studio.java` `importVlt()` lines 225-239 and `exportVltDialog()` lines 261-276 run `EditorService` work inside `ProgressDialog.run()`, which executes on a worker thread (`app/dialog/ProgressDialog.java` lines 44-74). `EditorService.importVlt()` replaces `session` (line 140) while the EDT is inside the modal dialog's event pump, so a listener refresh can read a half-installed session. `ProgressDialog.run()` also calls `worker.join()` on the EDT (line 62), which works only because disposal happens first; a `FutureTask` with `get()` after `dispose()` would make the ordering explicit.
* Unsynchronized shared model state. `EditorSession.revision` is a plain long written in `fireChanged()` (lines 39, 139) and read by `CanvasPanel` on the EDT (line 211). `PreviewVariables` and `SelectionState` are plain objects mutated from MCP threads (`EditorService.setVariables()` lines 916-939, `EditorControl.select()`) while the canvas renders against them. `EditorService.session()` returns the reference outside the monitor (lines 73-79), and `Studio.session()` (lines 50-52) chains calls, so the UI can keep using an old session after `open()` or `importVlt()` swapped it.
* Static state. `Messages.language` and the `BUNDLES` map (`app/i18n/Messages.java` lines 26-27) are unsynchronized and are read from every thread that resolves a label. `SettingsStore.save()` (`app/config/SettingsStore.java` lines 58-65) can be called from the EDT and from MCP preference changes at the same time; two writers can interleave.
* Blocking work on the EDT. `Studio.openFile()` (lines 95-105) parses XML and indexes the document on the EDT, `Studio.save()` (lines 165-179) writes the file on the EDT, and `Studio.applyXml()` (lines 450-466) parses on the EDT. These are the same freezes the original had (`src/vlcskineditor/Main.java` parsed inline during `openFile`), and the existing `ProgressDialog` pattern could cover them.
* What the rewrite fixed. The original updated the preview from a `FrameUpdater` thread (`src/vlcskineditor/FrameUpdater.java` called `repaint()` off the EDT) and refreshed the items tree from a raw `Thread` after a drop (`src/vlcskineditor/ItemTransferHandler.java` lines 149-168). The rewrite replaced both with a Swing timer (`app/panel/CanvasPanel.java` lines 86-92) and a synchronous command (`app/panel/ItemTransferHandler.java` lines 145-148); keep those as the model for the fixes above.
