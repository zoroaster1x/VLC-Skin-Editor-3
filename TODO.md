# TODO

Everything still open, why it is open, and how it will be done. Kept honest:
items move to Done only after `./gradlew build` and, where a UI is involved,
after looking at a fresh screenshot or GIF.

## Done since the audit

* Animated bitmaps: `RenderOptions.frameTick`, `ImageStore` frame cycling and
  fps discovery, canvas animation timer, `ImageStoreTest`.
* Nested parent lists fixed in `SkinIndex.parentListOf`/`parentItemOf`;
  delete/duplicate/drag-drop of items inside groups, panels and playtrees now
  target the correct list.
* Playtree rules: one slider, slider cannot be duplicated or dragged away,
  children other than a slider refused.
* Slider background as a child: add, edit, delete, generator and validation;
  the MCP `add_item` accepts a `SliderBackground` with a slider parent.
* Pressed sprite state while the mouse is down; hover already existed.
* Preview PNG export hides selection overlays and appends `.png`
  (`PreviewSnapshot`, `EditorControl.save_preview`).
* Context menus on all three trees, the canvas and the XML editor.
* Duplicate rename prompts for items, resources, windows and layouts.
* Delete confirmations and the "at least one window/layout" guard.
* Anchor `lefttop` editing; sub bitmap `nbframes`/`fps` and parent bounds check
  in both the inspector and the visual editor.
* Bitmap and font file choosers, OTF/Sans fallback note.
* History cap 50 and coalesced arrow-key nudges (`EditorSession.nudge`).
* 21 original translations converted; menus, toolbar, panel titles and common
  dialogs resolve through `Messages`; em dashes removed from inherited bundles.
* MCP control surface: undo/redo/history, selection, nudge, reorder, reparent,
  layout reorder, get/apply XML, reload images, save preview, test in VLC,
  slider background generator, preferences, canvas state, panel focus, resource
  inspection, variables, update check, reset skin, duplicate resource.
* VLC launching understands Flatpak (`VlcFinder` installs the .vlt into the
  sandbox skins folder and runs `flatpak run org.videolan.VLC`).
* Git repository initialised on `main`, initial commit `b4322b5`.

## In flight

* Shell batch subagent (StudioFrame, Studio, chrome, AboutDialog,
  ProgressDialog, StudioSettings, VlcSkinStudio): new-skin path prompt, delete
  dispatch and confirmations from the frame, accelerators (Ctrl+I, Ctrl+G, F1,
  Shift+Ctrl+T/V, Mac Backspace), live toolbar preference, checkerboard menu
  item, toolbar undo/redo enabled state, floatable toolbar persistence, Mac
  menu bar and dock icon, default VLC skins chooser folder, progress dialogs
  for VLT import/export, VLT unpack confirmation and `_unpacked` suffix,
  export success dialog and skipped-file warnings, update check menu and
  startup check, About box year/site/licence.
* Localization sweep subagent (panels, inspector fields, dialogs): replace
  visible strings with `Messages` calls, using original keys where the concept
  matches and English fallbacks elsewhere.

## Next, in order

1. **Finish the host wiring for the new MCP tools.**
   Update `HeadlessStudio` and (after the shell subagent) `StudioFrame` to pass
   the canvas supplier and the panel shower to `SwingUiInspector`, and install
   `SettingsHost` in `VlcSkinStudio` for CLI and MCP runs so `set_preferences`
   works without a window. Then extend `EditorServiceTest` and add
   `EditorControlTest` covering undo/redo, selection, nudge, reorder,
   reparent, XML round trip, preferences via a stub `UiInspector`, and the
   slider background generator.
2. **Flatten the modules.** Done: one `src/main` and `src/test`, a single
   `build.gradle` with the `application` and Shadow plugins, every path in
   `run.sh`, `tools/*.py`, the docs and the CI workflows moved from
   `app/build/...` to `build/...`.
3. **Gallery conformance sweep.** Run `tools/gallery-conformance.py` over the
   120 themes in the official pack plus the themes VLC itself ships
   (`share/skins2/default/theme.xml` and `share/skins2/winamp2.xml`, extracted
   to `/tmp/opencode/skin-gallery/official` and passed with `--extra`). Triage
   every import, validate and render failure; fix parser or renderer bugs;
   rerun until every theme imports and renders. The two built-in themes are
   the official reference for skins2 features; winamp2 ships without its BMP
   assets in the VLC tree, so missing-asset warnings there are expected and
   must be reported as such. Commit `docs/skin-gallery-report.md` and the
   JSON, and keep the raw work folder out of the repository.
4. **VeLoCity recreation over MCP.** Rerun
   `tools/recreate-velocity-via-mcp.py` on the merged jar, review the step
   renders, fix any tool gaps it exposes, then copy
   `velocity-mcp-recreation.gif` and the final still into `screenshots/`.
5. **Regenerate the whole screenshot set** with `uiScreenshots` after the
   merge, look at every GIF and still, and update the README gallery if any
   file name changed.
6. **GraalVM native image.** With GraalVM CE 25 installed, try
   `native-image` on the fat jar for the CLI and MCP entry points (the desktop
   Swing path stays on the JVM). Add the GraalVM Gradle plugin plus reflection
   configuration for Jackson and the MCP SDK if the build succeeds; otherwise
   document exactly where it fails and keep the JVM build as the supported one.
7. **Wire the MCP server into OpenCode and use it.** Add
   `vlc-skin-studio` under `mcp.servers` in
   `/home/zm/.config/opencode/opencode.json` with the merged jar path and the
   `mcp` subcommand, run `opencode mcp list` to confirm it connects, and
   exercise `document_info`, `layout_tree` and `render_layout` through the
   server so the connection is proven, not assumed.
8. **Final verification and commits.** Full `./gradlew build`, CLI smoke
   (`new`, `validate`, `render`, `vlt`), MCP handshake from
   PRIVATE_AGENTS.md, one fresh screenshot review, then one commit per logical
   change with plain subjects and the Zoroaster1x identity.

## Audit items deliberately different, documented in the README

* The in-app update check points at GitHub releases instead of the original's
  dead update server; there is no self-updater because CI cuts releases.
* The toolbar is docked rather than a floating palette; floating dock panels
  cover that need.
* Radial sliders are editable here; the original refused them.
* `@include` is not used in the converted language files (each language is one
  properties bundle).
* Internal frame geometry from the old config is superseded by the ModernDocking
  `layout.xml`.

## Smaller polish, not blocking

* Per-item icons in the items tree (button, slider, checkbox, text, anchor)
  instead of the generic item icon.
* Action editor: grouped submenus and inline editing of a raw action code by
  double click.
* Language default from the system locale on first run.
* Canvas controls label showing "Window / Layout" like the original preview
  title.
* Selecting a window (not a layout) could show the welcome card instead of
  falling back to the first layout.
