# AGENTS.md

Rules for any agent working in this repository. Read this before touching anything.

## 1. What this is

VLC Skin Studio reads, edits, validates, renders and packages VLC skins2 themes.
It is a modern rewrite of the original VLC Skin Editor by Daniel Dreibrodt
(GPL-2.0-or-later), ported to Java 25 with a dockable Swing UI on FlatLaf, a
terminal UI, a CLI and an MCP server for AI clients.

A theme is an XML file (usually `theme.xml` or `<name>.vlt`) that describes
windows, layouts, controls (buttons, sliders, text, video, playlists), bitmaps,
sub bitmaps, fonts, bitmap fonts, popup menus and ini files. VLC renders it with
the `skins2` interface: `vlc -I skins2 --skins2-last=theme.xml`.

## 2. Hard rules

* **The format is the product.** A theme that opens must save back everything
  it carried. Attributes and children this editor does not know are preserved
  and written again; never drop them and never reorder or rename silently.
* **Never invent VLC semantics.** Defaults, serialization and rendering follow
  the skins2 parser (`modules/gui/skins2/parser`) and the original editor.
  When the two disagree, prefer VLC and write down why in this file.
* **No em dashes** in text written here, including docs, commits and comments.
  Rewrite the sentence with a comma, a colon, a semicolon, or a full stop.
* **No personal data in tracked files.** No home paths, user names or machine
  specific paths. They live in the gitignored `PRIVATE_AGENTS.md` and
  `.testenv`.
* **Verify before claiming done.** Run `./gradlew build`, and for UI work look
  at `build/reports/screenshots/`. Report measured numbers, not intentions.
* **Do not pop up windows during tests.** UI tests run through
  `HeadlessStudio`, which paints the real panel tree offscreen and can be fed
  real mouse events. There is no X server requirement.

## 3. Build and run

Java 25 is required. Gradle comes from the wrapper.

```bash
./gradlew build                       # compile, run every suite, build the jar
./gradlew shadowJar                   # single file jar with all dependencies
./gradlew run                         # open the desktop window
./run.sh                              # build once, then run the desktop app
./run.sh render skin.xml -o p.png     # CLI render
./run.sh mcp                          # MCP server over stdio
./run.sh tui skin.xml                 # terminal UI
```

The fat jar lands at `build/libs/vlc-skin-studio.jar` and its main class is
`dev.zoroaster1x.vlcskin.app.VlcSkinStudio`. Any known subcommand runs the
CLI; anything else opens the window.

GraalVM 25 can build a native CLI, TUI and MCP binary with
`tools/build-native.sh`; the desktop Swing window stays on the JVM. The script
documents the exact flags and the runtime libraries the binary needs.

## 4. Test suites

| Suite | Covers |
|---|---|
| `format/SkinRoundTripTest` | every element survives write, parse and write; escaping; unknown attributes and children kept; `id="none"` becomes a generated unique id |
| `render/BezierPathTest` | the port of VLC's bezier sampler: interpolation, single points, monotonic percentages, parsing and formatting |
| `render/BooleanExpressionTest` | `not`/`and`/`or`, parentheses, unknown identifiers, variable substitution, slider state |
| `render/RendererTest` | the example renders at zoom, corner transparency, topmost hit testing, selection overlay, thumb tracking |
| `render/ImageStoreTest` | bitmap frame cycling, animation fps discovery, tick wrapping |
| `render/SliderBackgroundGeneratorTest` | frame count, horizontal and vertical strips, required middle image |
| `edit/EditorSessionTest` | coalesced nudges, per item undo steps, revision bumps |
| `format/VltCodecTest` | VLT export and import, assets, zip archives accepted, a zip bundling further .vlt files unpacks every theme |
| `mcp/EditorServiceTest` | document info, add/move/edit/undo, unknown attributes and duplicate ids rejected, render PNG and geometry, validation, playlist slider rules, nested parent lists, sub bitmap lifecycle, tool catalog |
| `mcp/EditorControlTest` | undo/redo and history, selection, nudge, reorder and reparent, XML round trip, preferences through a stub UiInspector, slider background generator |
| `app/StudioUiTest` | the whole panel tree paints real pixels in both themes, canvas drag moves items undoably, adding items updates the tree and the UI description |
| `app/i18n/MessagesTest` | the converted original translations load, unknown keys fall back, the language catalog lists the original 21 |

The two tools under `tools/` are the acceptance harnesses:

* `gallery-conformance.py` imports, inspects, validates and renders every theme
  from the official VideoLAN gallery pack and writes
  `docs/skin-gallery-report.md`.
* `recreate-velocity-via-mcp.py` drives the MCP server from an external Python
  client to rebuild the VeLoCity player window from its own assets.

`src/test` writes screenshots to `build/reports/screenshots/`, which is
the review path for UI changes. `HeadlessStudio` drives the same panels the
desktop window docks.

## 5. Layout

```
src/main/java/dev/zoroaster1x/vlcskin/
  model/            theme, windows, layouts, items, resources, SkinIndex
  model/item/       one class per control, sealed Item hierarchy
  model/resource/   bitmap, sub bitmap, font, bitmap font, popup menu, ini
  format/           SkinParser, SkinWriter, ThemeWriter, ItemWriter, ResourceWriter,
                    SkinValidator, VltCodec, XmlSupport
  format/parse/     Attributes, ParseContext, ItemParser, ResourceParser, WindowParser
  render/           SkinRenderer, ImageStore, BezierPath, Bounds, HitTester,
                    PreviewVariables, BooleanExpression, SliderGeometry,
                    SliderBackgroundGenerator
  render/draw/      one drawer per control
  edit/             EditorSession, CommandStack, ValueCommand, DeepCopy, ItemFactory,
                    SelectionState, commands/
  action/           ActionCatalog, ActionChain, GlobalVariableCatalog
  describe/         LayoutDescriber, LayoutDescription, GeometryNode for LLM readers
  snapshot/         PreviewSnapshot, UiInspector
  mcp/              EditorService, McpToolset, McpServerRunner, PropertyAccess, Schema
  ai/               AiAssistant (OpenAI compatible, uses the MCP toolset)
  cli/              picocli commands
  tui/              TuiShell, TuiLoop, AsciiRenderer
  example/          ExampleSkins with generated assets
src/main/java/dev/zoroaster1x/vlcskin/app/
  VlcSkinStudio     entry point, CLI dispatch or window
  Studio            session, settings and operations
  StudioFrame       menus, toolbar, ModernDocking layout, status bar
  HeadlessStudio    the same panels without a window, for tests
  panel/            resources, structure, items, canvas, inspector, variables,
                    problems, XML, AI, welcome
  inspector/        InspectorFields widget factories
  dialog/           theme settings, action editor, sub bitmap editor, slider
                    background generator, about
  component/        Icons (hand drawn vector icons)
  snapshot/         SwingUiInspector (UI description and screenshot for MCP)
  theme/            ThemeManager (FlatLaf themes and the orange accent)
  config/           StudioSettings, SettingsStore
```

Keep classes focused. If a file grows past roughly 400 lines, split it along
the seams the package already shows. One god class is a bug.

## 6. Format knowledge worth keeping

These cost real time to find. They are handled now, so treat them as
regressions to avoid, not as work to do.

* `id` attributes default to `none` in the DTD, and VLC treats that as no id.
  When loading, an absent or `none` id gets a generated unique editor id so the
  trees can select; generated ids are written back like the original editor did.
* Item ids are one namespace, resource ids another, window ids another, and
  layout ids are unique inside their window. A window and its layout may share
  a name (the example does); never look an id up without its kind.
* Unknown attributes and unknown child elements are preserved verbatim and
  written again. `SkinNode.foreignAttributes()` and `unknownChildren()` hold
  them; every writer calls them last.
* Serialization omits attributes at their defaults. The defaults are the DTD
  ones plus the original editor's quirks where they differ. Important cases:
  `visible` defaults to the string `"true"`, `rightbottom` defaults to
  `"lefttop"` (not `"rightbottom"`), `Layout` width and height are required,
  `Anchor.range` is written now (the original parsed it but dropped it).
* `Playlist` and `Playtree` are one control in VLC. The editor models them with
  `PlaytreeItem.playlistSyntax`, which decides the element name and hides the
  folder rows. A playlist slider always follows the playlist scroll position,
  so its `value` is not serialized.
* `Slider.points` is `(x,y),(x,y)` and VLC samples it as a Bezier of degree
  n-1 at 1024 percentages, keeps only the percentages where the rounded pixel
  changed, and at lookup returns the nearest stored sample. Coordinates round
  with `lrintf` (round half to even). `BezierPath` reproduces all of that.
* Slider thumb placement uses the simulated slider value, not the XML `value`
  attribute. `PreviewVariables.sliderValue` is the single source; the variables
  panel and MCP `set_variables` drive it.
* A slider background is one bitmap cut into `nbhoriz x nbvert` frames; the
  frame index is `floor(fields * value)`, filled left to right, top to bottom,
  with `padhoriz`/`padvert` between frames. Empty images and one frame images
  are the common cases and must not divide by zero.
* Bitmap decoding follows VLC's `FileBitmap`: decode to RGBA, premultiply is
  not kept (the renderer uses straight ARGB), a pixel whose RGB equals
  `alphacolor` becomes fully transparent regardless of its alpha, and every
  other pixel keeps its alpha. `nbframes` cuts the image into equal horizontal
  strips; frame 0 is what static controls draw.
* Fonts: `defaultfont` is Sans Serif 12. A `Font` resource loads the TTF/OTF
  relative to the skin folder at its `size`. A missing font falls back to Sans
  Serif rather than failing the render.
* Boolean attributes such as `visible` and `state` are expressions: `not`,
  `and`, `or`, parentheses, and names like `vlc.isPlaying`, `playlist.isRepeat`,
  `equalizer.isEnabled`. Unknown identifiers evaluate to false. Text variables
  are `$B $V $T $t $L $l $D $d $H $N $F $S` and substitute with `String.replace`
  (never regex; `$` in a value must stay literal).
* Actions are a semicolon separated chain of code strings such as
  `vlc.play()`, `dialogs.prefs()`, `playlist.setRandom(true)`,
  `mywindow.show()`, `mywindow.setLayout(other)`. Unknown codes are kept.
  The catalog lives in `ActionCatalog`.
* VLT archives are gzipped tar with `theme.xml` and every referenced asset.
  Some files in the wild are plain zip; `VltCodec` accepts both and refuses
  entries that would escape the target folder. One gallery entry (the Ecco
  ColdBlue/FreshGreen download) is a zip that bundles further `.vlt` archives;
  `unpack` recurses and writes each bundled theme into its own subfolder.
* `Theme version` must be 2.x for skins2. A newer minor version is a warning,
  not a hard stop; the editor preserves what it cannot interpret.

## 7. UI patterns that earned their place

* Panels are dockable with ModernDocking, so the user can move the resources,
  structure, items, canvas, inspector, variables, problems, XML and AI panels
  anywhere, including floating windows. Window layout persists to
  `layout.xml` next to the settings.
* Every edit goes through `EditorService` or `ValueCommand` on
  `EditorSession.apply`, so undo, the dirty flag and the MCP tools all see the
  same change. Direct model mutation outside a command is a bug unless it is
  followed by `touch()`.
* The trees guard against selection echo: during `Panels.refresh()` the tree
  listeners are suppressed through the refresh guard, or programmatic
  selection refreshes forever.
* The canvas caches its rendered image keyed by document revision, zoom,
  selection, hover, pressed state and animation tick. `EditorSession.revision()`
  bumps on every change.
* Animated bitmaps (nbframes greater than one) run through an animation tick on
  `RenderOptions`; `CanvasPanel` starts a Swing timer at the fastest bitmap fps
  and stops it when no animated bitmap is in the theme. Static renders and PNG
  exports always use tick 0.
* Context menus exist on all three trees (add, edit, duplicate with a rename
  pattern, delete, move, reload), the canvas (duplicate, delete, bring to
  front, fit) and the XML editor (apply, refresh, copy, select all).
* The inspector rebuilds only when the focused node changes; commits use the
  same undoable setters, so the form does not lose focus while typing.
* Arrow key nudges of the same item coalesce into one undo step
  (`EditorSession.nudge`), like the original editor.
* Dragging in the items tree moves nodes between groups and panels; a playlist
  slider and a slider background refuse to be dragged, a slider refuses to
  receive children, and a node can never be dropped into its own subtree.
* `SwingUiInspector` describes the panel tree as JSON (name, class, bounds,
  proportion of the window, first text) and paints it to PNG. `describe_editor_ui`
  and `screenshot_editor` in MCP use it. Tests use the same code path through
  `HeadlessStudio`, so the described UI is the shipped UI.
* The welcome card is the empty state; the problems list is the error state;
  the status bar always names the current file, selection, zoom and dirty
  state.

## 8. Verification discipline

* `./gradlew build` is the floor. It runs every suite and builds the fat jar.
* UI changes need a screenshot: run `test`, then look at
  `build/reports/screenshots/studio-dark.png` and `studio-light.png`.
  Both themes are part of the change.
* CLI changes need a real run:
  `./run.sh new --example neon /tmp/neon/theme.xml`,
  `./run.sh validate /tmp/neon/theme.xml`, `./run.sh render ...`.
* MCP changes need a handshake: start `./run.sh mcp` and send `initialize`,
  `tools/list` and one `tools/call` over stdio. `EditorServiceTest` covers the
  handlers; the handshake covers the transport.
* Report numbers with conditions, for example "28 core tests, 6 UI tests,
  render 68 ms for the example at zoom 1".

## 9. Commits

* Subject: plain and descriptive, no `feat:` or `docs:` prefixes, no emoji.
* Body: terse bullets with a component prefix, one line each, only when the
  change needs one.
* One logical change per commit. Push only when asked, and then push.
* Commit as `Zoroaster1x <232890170+zoroaster1x@users.noreply.github.com>`,
  no Co-Authored-By lines.

## 10. Releases

* **A release is cut by CI, never by hand.** Bump `version` in
  `gradle.properties`, then make the head commit with the marker in its
  message: `VLC Skin Studio 1.0.1 [release] 1.0.1`. The marker version must
  equal `gradle.properties`.
* The Release workflow builds, runs the suites, generates the notes from every
  commit since the previous release tag followed by
  `.github/release-template.md` (which carries the funding block), attests the
  provenance of `build/libs/vlc-skin-studio.jar`, and publishes the release
  with that jar plus a SHA-256 checksum file.
* A push without the marker builds and tests only, which is the normal case.
  `workflow_dispatch` cuts a release for the current version.
* Manual fallback, only when CI cannot run:
  `gh release create 1.0.1 --target master build/libs/vlc-skin-studio.jar`
  (attestation is then missing, so say so in the report).

## 11. Local test configuration

If a file named `PRIVATE_AGENTS.md` exists beside this one, read it first: it
holds this machine's real paths (SDKMAN, the reference clones, VLC, the
opencode config) and is gitignored. This file and every other tracked file
must stay free of them.
