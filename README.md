# VLC Skin Studio

A modern editor for VLC skins2 themes: dockable desktop UI, live preview, a
terminal UI, a CLI and an MCP server so AI clients can inspect and build skins
with you. It is a from scratch port of the original VLC Skin Editor (0.8.6)
to Java 25.

Project: <https://github.com/zoroaster1x/VLC-Skin-Editor-3> ·
[Latest release](https://github.com/zoroaster1x/VLC-Skin-Editor-3/releases/latest) ·
[Issues](https://github.com/zoroaster1x/VLC-Skin-Editor-3/issues)

**[Install](#install)** · **[What it does](#what-it-does)** · **[Requirements](#requirements)** · **[Quick start](#quick-start)** · **[Screenshots](#screenshots)** · **[Documentation](#documentation)** · **[License](#license)** · **[Credits](#credits)** · **[Funding](#funding)**

![Dark theme](screenshots/studio-dark-overview.png)

![Light theme](screenshots/studio-light-overview.png)

## Install

Download the [latest release](https://github.com/zoroaster1x/VLC-Skin-Editor-3/releases/latest). It ships `vlc-skin-studio-<version>.zip` with the application jar, `run.bat` for Windows, `run.sh` for Linux and macOS, and a `README.txt` with the same instructions; the bare `vlc-skin-studio.jar` is attached as well.

* **Windows**: install the Azul Zulu JRE 25 `.msi` from [the Azul download page](https://www.azul.com/downloads/?version=java-25-lts&package=jre&os=windows&architecture=x86-64-bit#zulu), unzip the package and double-click `run.bat`. On Windows on ARM take the ARM 64-bit `.zip` and set `JAVA_HOME` to the unpacked folder.
* **macOS**: install the Azul Zulu JRE 25 from [the Azul download page](https://www.azul.com/downloads/?version=java-25-lts&package=jre&os=macos#zulu), ARM 64-bit on Apple Silicon or x86 64-bit on Intel (`brew install --cask zulu@25` also works), then run `chmod +x run.sh && ./run.sh` in Terminal.
* **Linux**: install the Azul Zulu JRE 25 from [the Azul download page](https://www.azul.com/downloads/?version=java-25-lts&package=jre&os=linux#zulu), x86 64-bit or ARM 64-bit, as `.tar.gz`, `.deb` or `.rpm`, then run `chmod +x run.sh && ./run.sh`.

`run.bat` and `run.sh` check for Java first and stop with `JAVA NOT INSTALLED. Please download from ...` and the right link when it is missing. They pass every argument through, so `run.sh --help` lists the CLI, the terminal UI and the MCP server, `run.sh render theme.xml -o preview.png` renders a theme, and `run.sh mcp` starts the MCP server. If Java 25 is already installed you can also skip the launchers and double-click `vlc-skin-studio.jar`, or run `java -jar vlc-skin-studio.jar`.

## What it does

* Opens and saves VLC skins2 themes (`.xml`, `.vlt`) with every control skins2
  supports: windows, layouts, anchors, buttons, checkboxes, images, text,
  sliders, slider backgrounds, radial sliders, video areas, playlists,
  playtrees, panels and groups.
* Draws the layout the way VLC does, using VLC's own rules (bezier slider
  paths, slider background frame grids, alphacolor keying, boolean `visible`
  expressions, `$T` and friends). The preview is a real Java2D render, not a
  mockup.
* Keeps everything it does not understand: unknown attributes and elements are
  preserved and written back. A theme from any VLC version opens and saves
  without losing data.
* Animates multi-frame bitmaps in the preview at their fps, like VLC does.
* Drag and drop in the items tree moves controls between groups and panels;
  the canvas drags them with the mouse and every move is undoable.
* Packages and unpacks `.vlt` archives (gzip tar, plus plain zip archives found
  in the wild) with all referenced assets.
* Validates ids, references, sizes, colors and files before VLC has to.
* Browses the official VideoLAN skins gallery and imports a theme in one click,
  preview included, under File > Browse themes; the same is available over MCP.
* Speaks the original editor's language files: 21 translations converted from
  the original VLC Skin Editor bundles cover menus, toolbar, panel titles and
  the common dialogs, and new surfaces fall back to English until translated.
* Gives an AI the same controls through MCP: `layout_tree` describes geometry
  as data for models without vision, `render_layout` returns a PNG and the same
  data for models with vision, and the editing tools change the open document
  with undo.
* Checks the GitHub releases on startup (can be turned off), shows the notes of
  every release you missed and installs the jar after checking SHA-256.

## Requirements

* Java 25. The release launchers check for it and point at the Azul Zulu JRE
  download when it is missing; a JRE is enough. The build asks Gradle for a
  Java 25 toolchain. The jar entry point is compiled for Java 8 on purpose: on
  an older runtime it shows a dialog with the download links instead of an
  UnsupportedClassVersionError.
* VLC only if you want the "Test skin in VLC" menu item.

## Quick start

```bash
./gradlew build                    # compile, test, build the fat jar
./gradlew releaseZip               # package the release zip with the launchers
java -jar build/libs/vlc-skin-studio.jar          # desktop UI
java -jar build/libs/vlc-skin-studio.jar --help   # CLI
./run.sh                           # the same, builds on first run
```

The desktop window opens with the welcome card: create a skin, open one, or
generate the built in example with real assets. Panels are dockable; drag them
anywhere, float them, or restore the layout on the next start.

Inside the theme there is a generated example that looks like this:

![Example preview](screenshots/example-neon.png)

The desktop UI, the CLI, the terminal UI and the MCP server share one document
model. The links below cover all four.

## Screenshots

Dragging the play button on the canvas, one undo step:

![Select drag undo](screenshots/select-drag-undo.gif)

Editing a slider's bezier path with the path tool:

![Slider path editing](screenshots/slider-path-editing.gif)

Simulating the player state and watching the preview follow:

![Preview variables](screenshots/preview-variables.gif)

Canvas zoom, themes and a full tour:

![Canvas zoom](screenshots/canvas-zoom.gif)

![Theme switch](screenshots/theme-switch.gif)

![Full tour](screenshots/full-tour.gif)

A contact sheet of every frame in the tour lives beside these files as
`full-tour-frames.png`. All of these are generated by the headless test
harness with real mouse events, not mockups; regenerate them with
`./gradlew uiScreenshots`.

The renderer is checked against real themes. The VeLoCity theme (MIT, by
dmtiir) imports through the VLT codec, validates clean and renders like this:

![VeLoCity player](screenshots/velocity-player-main.png)

![VeLoCity playlist](screenshots/velocity-player-mainPL.png)

The VeLoCity player window rebuilt from its own sprite sheets by an external
MCP client, one tool call per element:

![VeLoCity MCP recreation](screenshots/velocity-mcp-recreation.gif)

The script that does it is `tools/recreate-velocity-via-mcp.py`; the conformance
sweep over every theme in the official VideoLAN gallery pack is
`tools/gallery-conformance.py`, and its report lives in
`docs/skin-gallery-report.md`.

## Documentation

Help > Documentation (F1) opens the built in guide. It ships inside the jar,
works offline, and its search covers titles, headings and body text.

* [Desktop UI](docs/desktop-ui.md): panels, shortcuts and themes.
* [CLI and terminal UI](docs/cli-and-tui.md): every command with examples.
* [MCP server](docs/mcp.md): the tool catalog and client setup.
* [Format support](docs/format-support.md): what is read, written and kept.
* [Known limits](docs/known-limits.md): honest boundaries.
* [Development](docs/development.md): build, tests, architecture and native
  images.
* [Skin gallery report](docs/skin-gallery-report.md) and
  [feature parity](docs/feature-parity.md): conformance numbers and the
  comparison with the original editor.

## License

GPL-3.0-or-later. This is a derivative of the original VLC Skin Editor by
Daniel Dreibrodt (GPL-2.0-or-later) and reads the VLC skins2 format, whose
implementation in VLC is GPL-2.0-or-later. See `LICENSE` for the full text and
`NOTICE.md` for the author attribution terms added under GPLv3 section 7(b).

## Credits

* The original VLC Skin Editor 0.8.6 by Daniel Dreibrodt, the reference for
  every dialog and for the generated XML.
* The VLC team for skins2 and its parser, the source of truth for rendering
  and format behavior.
* FlatLaf, ModernDocking, RSyntaxTextArea, MigLayout, Jackson, picocli, JLine,
  Apache Commons Compress and the official MCP Java SDK.

## Funding

If this saves you time, consider supporting development. Every contribution
goes toward maintenance and the long tail of skin edge cases.

**Monero (XMR):**

```
8BdxmQSniku4dBJXWPXeXvgjztmj5nmvWQqeCrVvCtYciusbAyo4rqrGCefTfQ4gGaVZmLN7VgLiYUYyBdYFEwHn1UWPjWs
```

Crypto is not your thing? Starring the repository, filing clear bug reports
with a sample skin, and telling other skinners all help just as much.
