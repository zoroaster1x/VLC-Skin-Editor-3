# AGENTS.md

Rules, architecture and hard-won knowledge for any agent working in this
repository. Read this before touching anything, and update it when you learn
something expensive.

## 1. What this is

VLC Skin Studio reads, edits, validates, renders and packages VLC skins2
themes. It is a modern rewrite of the original VLC Skin Editor by Daniel
Dreibrodt (GPL-2.0-or-later) on Java 25, with:

* a dockable Swing desktop window on FlatLaf and ModernDocking,
* a terminal UI,
* a picocli CLI,
* an MCP server for AI clients,
* a built in, searchable, offline documentation viewer,
* a theme browser over the official VideoLAN skins gallery,
* a GraalVM native build for the CLI, TUI, MCP server and PNG rendering.

A theme is an XML file (usually `theme.xml`, packaged as `<name>.vlt`) that
describes windows, layouts, controls (buttons, sliders, text, video, playlists,
panels, groups), bitmaps, sub bitmaps, fonts, bitmap fonts, popup menus and ini
files. VLC renders it with the `skins2` interface:
`vlc -I skins2 --skins2-last=theme.xml`.

The rewrite deliberately has no in-app AI chat panel. MCP is the single
automation surface, and every user action has a tool. Do not add a second
automation path without removing the first.

## 2. Hard rules

* **The format is the product.** A theme that opens must save back everything it
  carried. Attributes and children the editor does not understand are preserved
  and written again; never drop them, never reorder or rename silently.
* **Never invent VLC semantics.** Defaults, serialization and rendering follow
  the skins2 parser (`modules/gui/skins2/parser`) and the original editor. When
  the two disagree, prefer VLC and write down why in this file.
* **No em dashes** anywhere written here, including docs, commits, comments and
  UI text. Rewrite with a comma, colon, semicolon or full stop. No emoji.
* **No personal data in tracked files.** No home paths, user names or machine
  specific paths. They live in the gitignored `PRIVATE_AGENTS.md` and
  `.testenv`.
* **Code comments are Javadoc shaped.** A block starts with `/**` alone on its
  line, content lines start with ` * `, and ` */` sits alone. Javadoc on
  classes, records, public methods and anything non-obvious. No banner comments
  such as `// ----` and no commented-out code.
* **Keep classes focused.** Past roughly 400 lines, split along the seams the
  package already shows. One god class is a bug. Prefer records, sealed types,
  switch patterns, text blocks and virtual threads where they fit.
* **Verify before claiming done.** Run `./gradlew build`. For UI work, look at a
  fresh screenshot. Report measured numbers and conditions, never intentions.
* **Do not pop windows during tests.** UI tests run through `HeadlessStudio`,
  which paints the real panel tree offscreen and can be fed real mouse events.
  No X server is required.
* **Never let a build corrupt a running app.** `run.sh` snapshots the jar into
  `build/run/` before launching, because Gradle rewrites
  `build/libs/vlc-skin-studio.jar` in place and a JVM that lazily loads a class
  from a truncated jar fails with `ClassNotFoundException`. This once broke
  Jackson saves, ModernDocking layout persistence and the close prompt. Keep the
  snapshot in `run.sh`, and never test by rebuilding under a live window.
* **Built in help opens our own documentation viewer.** Every Help button in the
  application (inspector forms, skin settings, menus) opens
  `DocumentationDialog` on a handbook topic. Only Help > Online help may open
  the VideoLAN website, for upstream reference; the About box links to the
  project page, the releases and the Monero funding section.

## 3. Build, run and tools

Java 25 is required. Gradle comes from the wrapper, Java from SDKMAN (see
`PRIVATE_AGENTS.md` for this machine's paths).

```bash
./gradlew build                       # compile, run every suite, build the jar
./gradlew shadowJar                   # single file jar with all dependencies
./gradlew releaseZip                  # release zip with jar, launchers, README.txt
./gradlew test                        # tests only
./gradlew uiScreenshots               # regenerate screenshots/ (stills and GIFs)
./gradlew run                         # open the desktop window
./run.sh                              # build once, snapshot the jar, run the GUI
./run.sh render skin.xml -o p.png     # CLI render
./run.sh new --example neon dir/theme.xml
./run.sh validate dir/theme.xml
./run.sh vlt import skin.vlt out/     # unpack and open
./run.sh vlt export dir/theme.xml out.vlt
./run.sh inspect dir/theme.xml        # document info as JSON
./run.sh mcp                          # MCP server over stdio
./run.sh tui dir/theme.xml            # terminal UI
```

The fat jar lands at `build/libs/vlc-skin-studio.jar`. Its manifest main class
is `dev.zoroaster1x.vlcskin.launch.Launcher`, a source set compiled for Java 8
so an old JVM can load it and show install instructions; it reflects into
`dev.zoroaster1x.vlcskin.app.VlcSkinStudio` on Java 25. The native image keeps
`VlcSkinStudio` as its entry point. Any known subcommand runs the CLI; anything
else opens the window with an optional file argument.

`releaseZip` writes `build/distributions/vlc-skin-studio-<version>.zip` with the
fat jar, `run.bat`, `run.sh` and `README.txt` from `packaging/`. The launchers
check for Java 25, print `JAVA NOT INSTALLED. Please download from ...` with the
Azul Zulu JRE link for the detected OS and CPU when it is missing, and pass
their arguments through. The zip is the only release download; `SHA256SUMS`
carries its checksum.

Tooling under `tools/`:

* `build-native.sh`: GraalVM 25 native build of CLI, TUI, MCP and rendering.
  Writes `fontconfig.properties` next to the binary; keep the `.so` files too.
* `gallery-conformance.py`: imports, inspects, validates and renders every theme
  from the official gallery pack plus `--extra` themes, writes
  `docs/skin-gallery-report.md` and `.json`.
* `parity/`: the end to end verification harness: an isolated virtual display
  (`display.sh`), xdotool GUI driving (`gui.py`), MCP stdio clients
  (`mcp_client.py`, `scenario.py`), the official gallery corpus
  (`gallery_corpus.py`), numeric image comparison (`pixel_diff.py`,
  `region_diff.py`), the VLC golden comparator (`vlc_shot.py`,
  `vlc_compare.py`, `compare_all_layouts.py`) and step timing
  (`parity_log.py`). `tools/parity/README.md` documents every command and the
  traps; `tools/parity/LESSONS.md` collects every fact that cost real time,
  including the VLC staging recipe and the fullscreen-controller limitation.
  Read both before comparing anything against VLC or the original editor.
* `recreate-velocity-via-mcp.py`: external MCP client that rebuilds the
  VeLoCity player window from its own assets, step by step, with a GIF.
* `bundle-docs.py`: turns crawled documentation into the viewer bundle under
  `src/main/resources/dev/zoroaster1x/vlcskin/app/docs/`.
* `docs-graphics.py`: regenerates the nine handbook diagrams with Pillow.
* `dtd-to-markdown.py`: generates the element and attribute reference from
  VLC's `share/skins2/skin.dtd`.

GraalVM native build, when to use it: CLI, TUI or MCP startup speed and single
file distribution only. The desktop window stays on the JVM because the X11
toolkit metadata needed to paint it reliably does not exist yet; the native
binary prints a pointer instead. Native builds take minutes, so do not use them
for iteration. The JVM build is the supported one.

## 4. Repository layout

```
src/main/java/dev/zoroaster1x/vlcskin/
  model/            theme, windows, layouts, items, resources, SkinIndex
  model/item/       one class per control, sealed Item hierarchy
  model/resource/   bitmap, sub bitmap, font, bitmap font, popup menu, ini
  format/           SkinParser, SkinWriter, ThemeWriter, ItemWriter,
                    ResourceWriter, SkinValidator, VltCodec, XmlSupport,
                    XmlMerger (three way disk merge)
  format/parse/     Attributes, ParseContext, ItemParser, ResourceParser,
                    WindowParser
  render/           SkinRenderer, ImageStore, BezierPath, Bounds, HitTester,
                    PreviewVariables, BooleanExpression, SliderGeometry,
                    SliderBackgroundGenerator, RenderOptions
  render/draw/      one drawer per control, Colors (ini constants)
  edit/             EditorSession, CommandStack, ValueCommand, DeepCopy,
                    ItemFactory, SelectionState, commands/
  action/           ActionCatalog, ActionChain, GlobalVariableCatalog
  describe/         LayoutDescriber, LayoutDescription, GeometryNode
  snapshot/         PreviewSnapshot, UiInspector
  gallery/          GalleryTheme, ThemeGalleryClient (official skins gallery)
  mcp/              EditorService, EditorControl, McpToolset, McpServerRunner,
                    McpLog (activity file and status), PropertyAccess, Schema,
                    ToolOutcome
  cli/              picocli commands: new, render, inspect, validate, vlt,
                    examples, mcp, tui
  tui/              TuiShell, TuiLoop, AsciiRenderer
  util/             Json, XmlWriter, XmlEscape, VlcFinder, Platform, Log,
                    AppLog (daily logs and session zip)
  example/          ExampleSkins with generated assets plus the bundled
                    VeLoCity Dark theme
src/main/java/dev/zoroaster1x/vlcskin/app/
  VlcSkinStudio     entry point, CLI dispatch or window
  Studio            session, settings, operations, status listeners
  StudioFrame       menus, toolbar, ModernDocking layout, status bar, exit
  HeadlessStudio    the same panels without a window, for tests and MCP
  chrome/           ChromeActions, MenuBarFactory, ToolBarFactory, StatusBar
  panel/            resources, structure, items, canvas, inspector, variables,
                    problems, XML, MCP activity, welcome
  inspector/        InspectorFields widget factories
  dialog/           theme settings, preferences, action editor, sub bitmap
                    editor, slider background generator, theme browser,
                    documentation viewer, progress, about, keymap editor
  docs/             DocumentationBundle, DocumentationSearch,
                    MarkdownRenderer, DocumentationPanel
  component/        Icons (hand drawn vector icons)
  snapshot/         SwingUiInspector, SettingsHost (UI and prefs for MCP)
  theme/            ThemeManager (FlatLaf themes, VLC orange accent, font scale)
  config/           StudioSettings, SettingsStore, AppPaths, Keymap
  i18n/             Messages, PanelTitles, TypeNames (original translations)
src/main/resources/dev/zoroaster1x/vlcskin/
  app/messages/     21 converted original translations
  app/docs/         bundled handbook, format reference and archives
  render/fonts/     FreeSans.ttf, VLC's defaultfont, with its license note
  format/winamp2.xml  VLC's Winamp2 template for archives without theme.xml
  example/velocity/   the bundled VeLoCity Dark theme and its MIT license
src/test/java/...   one suite per area, see section 6
src/test/resources/edge-cases/  53 VLC edge case themes from docs/skins2-edge-cases.md
packaging/          run.bat, run.sh, README.txt that releaseZip ships to users
tools/              build, gallery, docs, screenshot and parity harnesses
docs/               skin-gallery-report.md/.json, feature-parity.md,
                    skins2-parser.md, skins2-rendering.md, skins2-edge-cases.md
screenshots/        README stills and GIFs, regenerated by uiScreenshots
.github/            test and release workflows, release template
```

## 5. Architecture and data flow

* `EditorSession` owns one document: theme, file, index, image store, renderer,
  undo history, selection, preview variables and the listener list. Every
  mutation goes through `apply(Command)` or `ValueCommand` on the session, so
  undo, the dirty flag, the canvas cache and MCP all see the same change. Direct
  model mutation outside a command is a bug unless it is followed by `touch()`.
* `EditorService` (in `mcp/`) is the synchronized API over the session, shared by
  the desktop window, the CLI and the MCP server. Undo, redo, XML round trips
  and structural edits live here. `EditorControl` adds the desktop-side
  operations: history state, selection, nudge, reorder, reparent, preview save,
  VLC launch, slider background wizard, preferences, canvas state, panel focus,
  the theme gallery and the documentation viewer.
* Session replacement is always in place. Opening a file, importing a VLT,
  starting a theme or an example calls `EditorSession.replace(...)` so the
  object identity, and therefore every registered listener, survives. Creating
  a new `EditorSession` on open once detached the whole UI and made open, new
  and examples look dead. Do not reintroduce that.
* `EditorService.setDispatcher` installs the event thread marshaller. `Studio`
  passes a dispatcher that runs listener notifications inline on the event
  thread and through `SwingUtilities.invokeLater` otherwise. The core default
  runs inline so headless tests stay deterministic. `EditorSession.revision` is
  volatile; `SelectionState` fields are volatile; `SettingsStore` synchronizes
  load and save around a static lock and writes atomically with a backup.
* `SwingUiInspector` describes the live panel tree as JSON (name, class, bounds,
  proportion, first text) and paints it to PNG. MCP `describe_editor_ui` and
  `screenshot_editor` use it, and the same code path runs in tests through
  `HeadlessStudio`, so the described UI is the shipped UI.
* MCP is stdio, one process, tools registered from `McpToolset`. Every tool
  handler is covered by tests. `McpCommand.HOST` is a static hook the desktop
  entry point fills with `SettingsHost` so a standalone MCP process reads and
  writes the real preferences. The server exits when stdin closes and stays
  headless. `tools/listChanged` stays false on purpose.
* The renderer follows VLC, never a mockup: alphacolor keying that ignores
  source alpha, bezier sampling at 1024 percentages with `lrintf` rounding,
  slider background frame grids, animation ticks, boolean expressions and `$`
  text variables. `RenderOptions.withoutOverlays()` disables selection,
  hover, pressed and anchor helpers for exported images.
* The updater lives in `update/UpdateService`: it reads the GitHub releases,
  sorts them against `Version.VERSION`, collects the notes of every missed
  release and downloads the release zip only after its SHA-256 matches the
  release `SHA256SUMS`, then extracts `vlc-skin-studio.jar` from it. Jar-only
  releases from before the zip are still supported. Unix replaces the running
  jar in place; Windows writes a batch helper that waits for the JVM to exit,
  swaps the file and restarts. `Version.VERSION` is expanded from
  `gradle.properties` into a resource, the single source the release marker,
  the About box and the updater share.
* The update prompt (`app/dialog/UpdateDialog`) never installs on its own. It
  shows the newest release date, how many updates and roughly how many days
  behind the copy is, then the changes and commits of every missed release in a
  scrollable text area; install and funding are kept only for the newest one,
  so a long history does not repeat intro, attestation, install and license
  blocks. The buttons are update now and close, and close keeps the old
  version. `Studio.checkForUpdates` runs on startup and every 30 minutes while
  `autoUpdate` is on, and remembers the version it already offered so the
  periodic check does not nag.
* Java 25 features in use: records for value types, sealed `Item` and
  `Resource` hierarchies, pattern switches in drawers and parsers, text blocks
  in generated documents, virtual threads for the update check and the progress
  dialog, sequenced collection methods in the command stack and recent files.

## 6. Test suites

`./gradlew build` runs all of them. Current state: 142 tests in 36 suites.

| Suite | Covers |
|---|---|
| `format/SkinRoundTripTest` | every element survives write, parse and write; escaping; unknown attributes and children kept; `id="none"` becomes a generated unique id; playlist slider value is not written |
| `format/VltCodecTest` | VLT export and import, assets, zip archives accepted, a zip bundling further `.vlt` files unpacks every theme, Winamp2 archives fall back to the bundled template, one theme.xml entry per export |
| `format/XmlMergerTest` | three way merge: disjoint edits, identical edits, conflicts keep ours and report, insertions and deletions |
| `format/EdgeCaseFixturesTest` | opens all 53 VLC edge case fixtures in `src/test/resources/edge-cases/` without throwing |
| `render/BezierPathTest` | the port of VLC's bezier sampler: interpolation, single points, monotonic percentages, parsing and formatting |
| `render/BooleanExpressionTest` | `not`/`and`/`or`, parentheses, unknown identifiers, variable substitution, resolved-vs-unknown results |
| `render/RendererTest` | the example renders at zoom, corner transparency, topmost hit testing, selection overlay, thumb tracking |
| `render/VlcSemanticsTest` | hidden items are not drawn, unresolved visibility stays visible, text clipping and alignment, radial frame formula, ini constants, playlist idle rows and scroll |
| `render/ImageStoreTest` | bitmap frame cycling, animation fps discovery, tick wrapping |
| `render/SliderBackgroundGeneratorTest` | frame count, horizontal and vertical strips, required middle image |
| `model/SkinIndexTest` | copy id patterns keep every letter, uniqueness, semicolon resource fallback |
| `edit/EditorSessionTest` | coalesced nudges, per item undo steps, revision bumps |
| `example/ExampleSkinsTest` | every built in example parses; VeLoCity ships its license and four windows |
| `mcp/EditorServiceTest` | document info, add/move/edit/undo, unknown attributes and duplicate ids rejected, render PNG and geometry, validation, playlist slider rules, nested parent lists, sub bitmap lifecycle, tool catalog |
| `mcp/EditorControlTest` | undo/redo and history, selection, nudge, reorder and reparent, XML round trip, preferences through a stub `UiInspector`, slider background generator, sub bitmap duplication |
| `mcp/EditorDiskSyncTest` | disk change notice, `disk_diff`, `sync_from_disk` three way merge keeps both sides |
| `mcp/McpLogTest` | the activity log and status file the MCP panel reads |
| `mcp/HelpToolsTest` | documentation topic list, ranked search and the full format reference read |
| `gallery/ThemeGalleryClientTest` | parsing the gallery's `showSkinBox` rows, escaped apostrophes in names |
| `gallery/ThemeGalleryCacheTest` | the list, preview and archive caches, served by an in-process HTTP server |
| `app/config/SettingsStoreTest` | atomic saves, backup recovery from a corrupt file, first-run language default |
| `app/config/AppPathsTest` | platform config, cache and data roots and their derived folders |
| `app/config/KeymapTest` | shortcut parsing, defaults, overrides and conflict-free descriptions |
| `app/docs/DocumentationTest` | the bundle exposes topics, every page loads, ranked search over headings and text, markdown and image rewriting, term highlighting |
| `app/StudioUiTest` | the whole panel tree paints real pixels in both themes, canvas drag moves items undoably, adding items updates the tree and the UI description, extreme theme numbers open, interface scale, ctrl-wheel zoom |
| `app/dialog/UpdateDialogTest` | the updates-behind and days-behind line, singular and unknown-date wording, and the trimmed release summary that keeps changes and commits for every release and install plus funding only for the newest |
| `app/UiLayoutAuditTest` | no clipped labels, buttons, squashed controls or overlapping siblings at three window sizes and at 150 percent font scale |
| `tui/TuiShellTest` | the terminal UI banner, info, help, items, tree, show, validate and render answers |
| `app/i18n/MessagesTest` | the converted original translations load, unknown keys fall back, the language catalog lists the original 21 |
| `app/panel/WelcomeCardTest` | example folder resolution: last writable folder, flatpak paths skipped, config directory fallback |
| `app/theme/ThemeManagerTest` | the pinned light or dark canvas backdrop and its normalization |
| `launch/LauncherTest` | the Java 8 entry point's version parsing and install message |
| `update/UpdateServiceTest` | version order, missed releases oldest first over a local HTTP server, zip download and extraction with SHA-256 (plus the jar-only fallback), Unix replacement, Windows helper shape |

`src/test` writes `build/reports/screenshots/`; `./gradlew uiScreenshots`
writes the README set to `screenshots/`. `HeadlessStudio` drives the same panels
the desktop window docks.

## 7. Format knowledge worth keeping

These cost real time to find. They are handled, so treat them as regressions to
avoid.

* `id` attributes default to `none` in the DTD, and VLC treats that as no id. On
  load, an absent or `none` id gets a generated unique editor id so the trees
  can select; generated ids are written back, like the original did.
* Item ids are one namespace, resource ids another, window ids another, and
  layout ids are unique inside their window. A window and its layout may share a
  name (the example does). Never look an id up without its kind. Sub bitmaps and
  bitmaps share the image lookup namespace; `SkinIndex.findImage` returns an
  `ImageRef(bitmap, sub)`.
* Unknown attributes and child elements are preserved verbatim and written
  again. `SkinNode.foreignAttributes()` and `unknownChildren()` hold them; every
  writer calls them last.
* Serialization omits attributes at their defaults: the DTD ones plus the
  original editor's quirks. `visible` defaults to the string `"true"`,
  `rightbottom` defaults to `"lefttop"` (not `"rightbottom"`), `Layout` width
  and height are required, `Anchor.range` is written now (the original parsed it
  but dropped it).
* `Playlist` and `Playtree` are one control in VLC. `PlaytreeItem.playlistSyntax`
  decides the element name and hides folder rows. A playlist slider always
  follows the playlist scroll position, so its `value` is not serialized.
* `Slider.points` is `(x,y),(x,y)` and VLC samples it as a Bezier of degree n-1
  at 1024 percentages, keeps only the percentages where the rounded pixel
  changed, and at lookup returns the nearest stored sample. Coordinates round
  with `lrintf` (round half to even). `BezierPath` reproduces all of that.
* Slider thumb placement uses the simulated slider value, not the XML `value`
  attribute. `PreviewVariables.sliderValue` is the single source; the variables
  panel and MCP `set_variables` drive it.
* A slider background is one bitmap cut into `nbhoriz x nbvert` frames; frames
  run left to right, top to bottom, with `padhoriz`/`padvert` between them.
  Empty and one frame images are common and must not divide by zero. The frame
  index is `(int)(value * (fields - 1))`, not `floor(fields * value)`, because
  VLC's `CtrlSliderBg::onUpdate` uses the last frame index. The thumb sits at
  `pos + point - image/2` with integer division, and a background whose `image`
  is a SubBitmap cuts frames from that sub rectangle, never from the whole
  parent sheet (the VeLoCity time strip is a 204 wide SubBitmap of a 380 wide
  sheet; ignoring that draws a 380 wide grey bar over the buttons).
* Bitmap decoding follows VLC's `FileBitmap`: decode to RGBA, keep straight
  ARGB (no premultiply), a pixel whose RGB equals `alphacolor` becomes fully
  transparent regardless of its alpha, every other pixel keeps its alpha.
  `nbframes` cuts the image into equal horizontal strips; frame 0 is what static
  controls draw. `fps` drives the canvas animation timer; exports use tick 0.
* Fonts: `defaultfont` is Sans Serif 12. A `Font` resource loads the TTF/OTF
  relative to the skin folder at its `size`. A missing font falls back to Sans
  Serif rather than failing the render. Bitmap fonts are parsed, preserved and
  previewed with the fallback font, not rendered from their sheet.
* Boolean attributes such as `visible` and `state` are expressions: `not`,
  `and`, `or`, parentheses, names like `vlc.isPlaying`, `playlist.isRepeat`,
  `equalizer.isEnabled`. Unknown identifiers evaluate to false. Text variables
  are `$B $V $T $t $L $l $D $d $H $N $F $S` and substitute with
  `String.replace` (never regex; `$` in a value must stay literal).
* Actions are a semicolon separated chain such as `vlc.play()`,
  `dialogs.prefs()`, `playlist.setRandom(true)`, `mywindow.show()`,
  `mywindow.setLayout(other)`. Unknown codes are kept. The catalog lives in
  `ActionCatalog`.
* VLT archives are gzipped tar with `theme.xml` and every referenced asset.
  Files in the wild are also plain zip; `VltCodec` accepts both and refuses
  entries that would escape the target folder. One gallery entry is a zip that
  bundles two further `.vlt` files; `unpack` recurses into nested archives.
  Some zips store the whole theme under `CoolSkin/`; `extract` strips that
  common prefix so assets land beside `theme.xml`.
* `Theme version` must be 2.x for skins2. A newer minor version is a warning,
  not a hard stop; the editor preserves what it cannot interpret.
* Real gallery themes carry years of quirks: duplicate item ids, duplicate
  resource ids, sub bitmap ids colliding with bitmap ids, missing referenced
  files, non positive panel sizes, playtrees without sliders, colors that are
  not `#RRGGBB`. VLC tolerates the duplicates by first match. The validator
  reports them, the gallery report classifies them, and none of them stop an
  import or a render. Do not "fix" a theme on import.
* VLC 3 rejects `--skins2-systray`; do not pass it. Launching a theme:
  `vlc -I skins2 --skins2-last=<archive>` for a native install, or
  `flatpak run org.videolan.VLC --no-one-instance -I skins2
  --skins2-last=<archive>` for Flatpak. A Flatpak sandbox sees `$HOME` as the
  real home and `XDG_DATA_HOME` as
  `~/.var/app/org.videolan.VLC/data`, so the host path of an archive installed
  there is already valid inside the sandbox. Never pass `$HOME` unexpanded.
* Resource references are fallback lists: `"id1;id2"` resolves to the first
  resource that exists, like VLC's `IDmap::find_first_object`. `SkinIndex`
  tries each semicolon separated segment and the validator follows.
* IniFile resources register constants `<ini id>.<section>.<key>` lowercased;
  a color value that names one resolves through them (`draw/Colors`), the way
  VLC's `getColor` does. The Winamp2 playlist colors depend on this.
* An archive without `theme.xml` but with `main.bmp` is a Winamp2 skin: VLC
  renders it through `share/skins2/winamp2.xml`, and the studio bundles a copy
  of that template and unpacks the BMPs next to it.
* `defaultfont` is FreeSans, bundled from VLC's resource path, so text metrics
  match VLC's renderer instead of Java's platform font.
* The preview draws VLC's idle playlist: the rows "Playlist" and "Media
  Library", and the nested scroll slider at position 1.0 because `VarTree`
  starts fully scrolled. A playlist slider's x and y are layout absolute, not
  relative to the playtree.
* Unknown child elements are preserved verbatim in their relative order but
  written after the known children of their parent, and unknown attributes
  keep their order. This is the one deliberate exception to "never reorder";
  VLC ignores those elements, so only hand inspection sees it.

## 8. Application UI patterns that earned their place

* Panels are dockable with ModernDocking: resources, structure, items, canvas,
  inspector, variables, problems, XML. The canvas docks first so resources sit
  left, the inspector right and the canvas centre. `View > Reset panel layout`
  deletes `layout.xml` and re-docks the default. `View > Panels` and each
  panel's right-click menu can show, hide, float and reset; hidden panels come
  back through `Docking.display`.
* Every dockable is a `DockablePanel implements Scrollable` that tracks the
  viewport in both directions. ModernDocking wraps panels in a scroll pane
  sized to their preferred size; without tracking, a large tree gets nested
  scrollbars, wheel events behave oddly and split drags ratchet the widths.
* The canvas is a `JScrollPane` around a `Surface implements Scrollable`, like
  the original's preview. Scrollbars and the wheel move a zoomed theme, Fit
  sizes it to the viewport, Ctrl+wheel zooms, the middle button or a left drag
  on the empty background pans without touching items, and a plain click on the
  background clears the selection. The empty layout paints a hint card.
* Animated bitmaps run through the animation tick; the canvas starts a timer at
  the fastest bitmap fps and stops it when nothing animates.
* Context menus exist on all three trees (add, edit, duplicate with a rename
  pattern, delete, move, reload), the canvas (duplicate, delete, bring to front,
  fit) and the XML editor (apply, refresh, copy, select all). The action editor
  groups codes into submenus and supports double click raw code editing.
* Selection is one namespace-aware state. `selectItem` clears the selected
  resource and the other way around; selecting a window clears the items tree
  and the preview shows the welcome card. Trees suppress their listeners during
  `Panels.refresh()` and preserve expanded branches through
  `expandedKeys()`/`restoreExpansion()`, so clicking a bitmap never collapses
  the tree.
* The inspector rebuilds only when the focused node changes. Its form tracks the
  panel width (`FormPanel implements Scrollable`, horizontal scrollbar never),
  notes wrap in `NotePane`, fields carry small preferred widths so nothing
  spills, and wheel steps are 16/140. Bitmaps and sub bitmaps show a checkerboard
  preview with size, frames and alphacolor.
* The welcome card is the empty state and scales to the dock width with wrapping
  text; the problems list is the error state; the status bar always names the
  file, selection, zoom and dirty state. New themes select their first layout
  and show the empty layout hint.
* The close path asks `Save changes before closing?` with Yes/No/Cancel when the
  session is dirty, saves through `Studio.save()` (which prompts for a path on
  an untitled skin), persists geometry and toolbar state, saves the docking
  layout inside `try/catch Throwable`, then disposes. A persistence failure must
  never block closing.
* The theme browser (`File > Browse themes`) lists the official gallery,
  filters by name or author, shows previews, downloads and opens a theme, and
  caches the list for a day, every preview by URL and every archive, with a
  stale list as offline fallback. Failures surface in a dialog, not only in the
  status line.
* The documentation viewer (`Help > Documentation`, F1) shows the bundled
  handbook with rendered markdown and images. Search ranks page titles, headings
  and body text, shows the section path, line and hit count, highlights terms in
  the page, and ignores one letter terms. `Open online` is disabled for pages
  with no source URL. Back and Forward navigate, Topics returns to the list.
* Built in help buttons never open the Website; they call
  `DocumentationDialog.openTopic` with a handbook topic. Only Help > Online
  help and About may leave the app.
* The inspector form is a two column MigLayout with capped, shrinkable editors
  and wrapping notes; it has no horizontal scrollbar, so nothing runs off the
  panel edge at any dock width or font scale. `UiLayoutAuditTest` fails if a
  label, button or control clips or overlaps at 1440x900, 1180x780, 980x660 or
  150 percent scale.
* Canvas controls: Move and Curve are icon toggle buttons, zoom is a combo
  plus minus, plus and Fit. The selected item's out-of-layout part gets a
  dashed accent outline, because VLC clips there and the canvas pad is not
  part of the theme.
* View > Canvas background offers follow-the-theme, light and dark.
  Preferences has interface size (75 to 200 percent, live), the keyboard
  shortcut editor (`Keymap`, stored in `StudioSettings.keys`), and the AI and
  MCP section (enable, command to copy, live status).
* The MCP activity panel tails the timestamped server log and status file, so
  the AI's calls are visible in the window even though the server is a
  separate process.

## 9. MCP rules

* Every user action has a tool, and every tool has a test. When a UI feature
  lands, add or extend the tool and its coverage. The catalog is `McpToolset`,
  handlers are `EditorService` and `EditorControl`.
* Tool names are lower snake case verbs. Arguments are documented in the schema.
  Errors are messages a model can act on, never stack traces.
* `render_layout` returns geometry JSON plus a PNG; `layout_tree` is the
  text-only path. Image results are base64 PNG content blocks.
* A standalone `mcp` process is headless, exits on stdin EOF and reads
  preferences through `McpCommand.HOST` when the desktop entry point installed
  `SettingsHost`. `listChanged` stays false.
* The server can be disabled in Preferences (`mcpEnabled`); the `mcp` command
  refuses to start then unless `--force` is passed. `McpLog` writes
  `<cache>/mcp.log` and `mcp-status.json` on start and on every call; the MCP
  activity panel and the Preferences status line read them.
* Every tool response can start with a `[NOTICE]` line when the file changed on
  disk outside the server. `disk_diff` lists the changed lines and
  `sync_from_disk` three way merges them into the session (`XmlMerger`);
  changes only one side made are applied, a conflict keeps the session's
  version and is reported. Never go back to telling the model to reload or
  overwrite; that discards a side. `EditorDiskSyncTest` and `XmlMergerTest`
  pin the behaviour.
* The initialize instructions explain that the user shares the file and how to
  react to a notice. Every call is timed and logged, including errors.
* The MCP server is registered in the OpenCode config under `mcp.servers`. After
  rebuilding the jar, reconnect from `/mcps`. `opencode mcp list` in that shell
  needs `XDG_CONFIG_HOME` set (see `PRIVATE_AGENTS.md`).

## 10. Threading and state rules

* Session change notifications go through the dispatcher: event thread inline,
  otherwise `SwingUtilities.invokeLater`. `Studio.status` and `Studio.error`
  marshal to the event thread too. The AI transcript is gone with the AI panel.
* Background work uses virtual threads: the update check, the progress dialog
  (`FutureTask` with disposal in `done()`), and the theme browser and gallery
  client executors. Never block the event thread on file or network work that
  can run behind a dialog.
* `EditorService` entry points are synchronized; keep it that way so the
  desktop window and an MCP thread can share one session.
* `Messages` keeps a volatile language and a synchronized bundle cache, and
  user translations can be dropped into the config `lang` folder without a
  rebuild.
* `SettingsStore` serializes saves and writes a temp file plus atomic move, with
  `settings.json.bak` as recovery.
* `EditorSession.revision` and the change journal are atomic
  (`AtomicLong`, `ConcurrentLinkedDeque`), `dirty` is volatile and the
  `ImageStore` caches are `ConcurrentHashMap`s, because the canvas renders on
  the event thread while MCP can render on the server thread.
* `EditorControl` marshals every UI touching tool call onto the event thread
  with `invokeAndWait`; a modal dialog blocks the call until the user answers,
  exactly like a person clicking the command. `SwingUiInspector` uses
  `invokeAndWait` for the same reason.
* `AppPaths` is the only place that decides where the application writes on
  disk (platform config, cache and data roots); `Platform` is the only place
  that decides the operating system. Do not add another `.config`, `.cache` or
  `os.name` check anywhere else.
* All logs append to the daily file and the live session zip under the config
  `logs/` folder (`AppLog`); `logback.xml` sends SDK logging to stderr, never
  to stdout, because MCP stdout is the protocol channel.

## 11. Native image notes

* Scope: CLI, TUI, MCP and PNG rendering. The desktop window stays on the JVM;
  without subcommand the native binary prints a pointer.
* `tools/build-native.sh` uses picocli codegen metadata (annotation processor),
  the traced AWT reachability metadata and `jni-config.json` /
  `reflect-config.json` under
  `src/main/resources/META-INF/native-image/dev.zoroaster1x/vlc-skin-studio/`.
* Java2D inside the image needs a font configuration. `tools/generate-fontconfig.py`
  writes `fontconfig.properties` next to the binary from the DejaVu families it
  finds, and `VlcSkinStudio` points `sun.awt.fontconfig` at it. Keep both the
  `.so` files and the properties file next to the executable.
* Native builds take minutes. Use JVM builds for iteration and native builds
  only for release verification.

## 12. Documentation system

* Content lives in the viewer bundle, not on the web. The handbook is rewritten
  for the application and the UI: getting started, window tour, menus and
  shortcuts, a seven step skin build, extras, troubleshooting and an appendix
  with the format explanation. The old crawled pages (original editor help,
  skins2 creation guide, VLC user documentation) ship as clearly separated
  archives because they are often outdated.
* `format-reference.md` is generated from VLC's `skin.dtd` with meanings, and is
  the appendix, not the front door.
* The nine diagrams come from `tools/docs-graphics.py`, so they can be fixed or
  restyled in code. If documentation needs a picture, generate it.
* Rebuild the bundle with `tools/bundle-docs.py` after editing crawled or
  handbook markdown, then commit the resources. The bundler prunes meta notes,
  resizes large images, rewrites image paths into `images/`, and orders sections
  as Start here, Making a skin, Extras and tools, Troubleshooting, Appendix,
  then the archives.

## 13. Verification discipline

* `./gradlew build` is the floor. It runs every suite and builds the fat jar.
* UI changes need a screenshot. Run `./gradlew test` for
  `build/reports/screenshots/`, or `./gradlew uiScreenshots` for the README set.
  Look at both themes. For live behaviour, a programmatic probe on the display
  (create `StudioFrame`, call actions, print titles, tree rows, selections) is
  allowed and has caught listener bugs; do not leave probes in the repository.
* Canvas and docking changes need a window check: new skin, example, open,
  import, zoom, scroll, pan, reset layout.
* CLI changes need a real run: `new`, `validate`, `render`, `vlt import`,
  `vlt export`, `inspect`.
* MCP changes need a handshake: `initialize`, `tools/list`, and one
  `tools/call` over stdio. Then exercise the tool through a real client when
  possible.
* VLC launch changes must actually launch VLC and read its log; skins2 prints
  the loaded skin name. Use `--no-one-instance` for the Flatpak.
* Gallery changes run `python3 tools/gallery-conformance.py --jar
  build/libs/vlc-skin-studio.jar --extra <official themes>`; the expected result
  is every theme imported and rendered.
* Report numbers with conditions, for example "78 tests in 16 suites, render
  68 ms for the example at zoom 1, 123 gallery themes rendered".
* Do not test by rebuilding while a window is open unless it came from the
  `run.sh` snapshot.

## 14. Commits

* Subject: plain and descriptive, no `feat:` or `docs:` prefixes, no emoji.
* Body: terse bullets with a component prefix, one line each, only when the
  change needs one.
* One logical change per commit. Push only when asked, and then push.
* Commit as `Zoroaster1x <232890170+zoroaster1x@users.noreply.github.com>`,
  no Co-Authored-By lines.

## 15. Releases

* **A release is cut by CI, never by hand.** Bump `version` in
  `gradle.properties`, then make the head commit with the marker in its
  subject: `VLC Skin Studio 1.0.1 [release] 1.0.1`. The marker version must
  equal `gradle.properties`. Only the subject counts; a body that mentions the
  marker does not release.
* The Release workflow builds, runs the suites, generates the notes from every
  commit since the previous release tag with a link per commit plus a full
  changelog compare link, followed by `.github/release-template.md` (which
  carries the funding block), attests the provenance of the dist zip, and
  publishes two assets: `vlc-skin-studio-<version>.zip` (jar, `run.bat`,
  `run.sh`, `README.txt`) and `SHA256SUMS` with the zip checksum. The zip is
  the only download; the in-app updater reads that first SHA-256, verifies the
  zip and extracts the jar.
* **Never hard wrap a line in `.github/release-template.md` or in the notes
  the workflow generates.** A GitHub release body turns every source newline
  into a line break, so an 80 column template renders with a break after every
  line. Paragraphs are one long line; only list items and code blocks may break.
* Each workflow starts with a cheap marker job that checks the commit subject;
  without the marker the Release workflow skips its heavy job and the Tests
  workflow runs, with the marker it is the other way around, so one commit
  never runs the same suite twice and no job pretends to release.
  `workflow_dispatch` cuts a release for the current version.
* Manual fallback, only when CI cannot run:
  `gh release create 1.0.1 --target main
  build/distributions/vlc-skin-studio-1.0.1.zip` plus a `SHA256SUMS` for it
  (attestation is then missing, so say so in the report).

## 16. Known traps

* Rebuilding the jar under a running app (see section 2). Use `run.sh`.
* Replacing the `EditorSession` object instead of replacing the document in
  place (see section 5). It silently detaches the UI.
* Passing `$HOME` unexpanded or the wrong sandbox path to Flatpak VLC.
* Adding a second scrollbar layer around dockables, or setting sizes during
  layout. Both ratchet or jitter.
* Rendering without stripping the YAML front matter. The viewer does it in
  `MarkdownRenderer.stripFrontMatter`.
* Trusting the legacy help pages. They are archived reference; the handbook is
  the product.
* Hard wrapping release notes. GitHub release bodies turn each source newline
  into a visible break; see section 15.

## 17. Local test configuration

If a file named `PRIVATE_AGENTS.md` exists beside this one, read it first: it
holds this machine's real paths (SDKMAN, the reference clones, VLC, the
OpenCode config) and is gitignored. This file and every other tracked file must
stay free of them.
