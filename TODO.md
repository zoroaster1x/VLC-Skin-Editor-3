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
* Git repository initialised on `main`, first commit `b4322b5`.
* The shell batch: new-skin path prompt, delete dispatch from the frame with
  confirmations, accelerators, live toolbar preferences, floating toolbar
  persistence, Mac menu bar and dock icon, default VLC skins chooser folder,
  progress dialogs for VLT import/export, `_unpacked` suffix, export skipped
  file warnings, update check menu and startup check, About box.
* The localization sweep: about 390 panel, inspector and dialog strings now
  resolve through `Messages`, reusing 171 original keys with English fallbacks
  for the newer widgets.
* Host wiring: `StudioFrame` and `HeadlessStudio` pass the canvas supplier and
  the panel shower to `SwingUiInspector`; MCP runs install `SettingsHost`
  through `McpCommand.HOST`, so `get_preferences` and `set_preferences` work
  without a window.
* Smaller polish: per-item icons in the items tree, grouped action editor with
  double-click raw code editing, a "Window / Layout" canvas label, the welcome
  card when a window rather than a layout is selected, and a first-run language
  default from the system locale.
* One Gradle module: `src/main` and `src/test`, one `build.gradle` with the
  application and Shadow plugins, every path in `run.sh`, `tools/*.py`, the
  docs and the CI workflows moved from `app/build/...` to `build/...`.
* Gallery conformance: 123 of 123 themes imported, inspected, validated and
  rendered, including the two themes VLC itself ships (`share/skins2/default`
  and `share/skins2/winamp2`). The first clean sweep caught two import bugs:
  a gallery zip that bundles two further `.vlt` archives, and zips that store
  the whole theme under `CoolSkin/theme.xml`; `VltCodec.unpack` now handles
  both and regression tests cover them. The report classifies the 133
  validation errors the old themes already carry (duplicate ids, missing
  resource references); none of them stop an import or a render.
* VeLoCity recreation over MCP: external Python client rebuilt the player
  window from its own assets in 13 steps, validation clean;
  `velocity-mcp-recreation.gif` and the final still are in `screenshots/`.
* Screenshots regenerated with the merged build, 29 files, dark and light
  overviews reviewed (new canvas label and per-item icons visible).
* GraalVM native image: `tools/build-native.sh` builds a 66 MB CLI/TUI/MCP
  binary with picocli reflection metadata generated at compile time and AWT
  reflection configuration for the renderer.
* MCP server wired into OpenCode under `mcp.servers` and exercised live from
  the session: examples, add/move/edit/undo, variables, nudge, reorder,
  reparent, slider background generation, validation and PNG previews.
* The package root is `dev.zoroaster1x` end to end: Java packages, imports,
  the conversion bundles under `src/main/resources`, the Gradle group and main
  class, the native-image metadata folder and every doc path.
* The MCP stdio server exits when its client closes stdin instead of waiting
  for a signal, which matters for scripted clients and leaks no processes.
* The native image carries GraalVM agent metadata for AWT (reflection, JNI and
  resources) plus `tools/generate-fontconfig.py`, which writes the
  `fontconfig.properties` Java2D needs next to the binary. `new`, `inspect`,
  `validate`, `vlt`, `render`, the MCP `render_layout` and the MCP server all
  work in the image. The desktop window stays on the JVM; a native launch
  without a subcommand prints that pointer. The Swing path was traced and its
  metadata collected, but the X11 toolkit did not paint the frame reliably, so
  the JVM remains the supported GUI.

## Next, in order

1. **Verify the native binary** on this machine: `--version`, a render of the
   neon example, and an MCP handshake (`initialize`, `tools/list`,
   `get_preferences`, `render_layout`). GraalVM's tracing agent has collected
   the AWT reflection and JNI metadata; if the renderer still refuses inside
   the image, copy the relevant traced entries into the checked-in configs and
   rebuild. Document the exact result in the README either way.
2. **Final verification and commits.** Full `./gradlew build`, CLI smoke
   (`new`, `validate`, `render`, `vlt`), MCP handshake from
   `PRIVATE_AGENTS.md`, one fresh screenshot review, then one commit per
   logical change with plain subjects and the Zoroaster1x identity.

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
