# Development

## Build and run

```bash
./gradlew build                    # compile, test, build the fat jar
./gradlew shadowJar                # the fat jar alone
./gradlew test                     # tests only, screenshots in build/reports
./gradlew uiScreenshots            # regenerate screenshots/ (stills and GIFs)
java -jar build/libs/vlc-skin-studio.jar          # desktop UI
java -jar build/libs/vlc-skin-studio.jar --help   # CLI
./run.sh                           # build once, snapshot the jar, run it
```

Java 25 is required; the build asks Gradle for a Java 25 toolchain. The jar
entry point is compiled for Java 8 on purpose, so an older runtime gets a
dialog with the download links instead of an UnsupportedClassVersionError.

## Tests

132 tests across 33 suites cover round trips, escaping, unknown content, bezier
maths, boolean expressions, rendering, hit testing, bitmap animation, slider
backgrounds, VLT archives (including a zip that bundles further themes), the
editor service, the MCP control surface and log, the three-way disk merge, the
update service, the theme gallery parser and cache, the settings store, the
keymap, the platform paths, the terminal UI, the documentation bundle and
search, the converted translations and the examples. The UI suite builds the
whole panel tree offscreen, paints it in both themes, dispatches real mouse
events to move an item and undo it, and writes screenshots to
`build/reports/screenshots/`.

Against real skins: the VeLoCity theme imports through the VLT codec and
validates clean, and `tools/gallery-conformance.py` sweeps every theme in the
official VideoLAN pack, with `--extra` for themes VLC itself ships. The latest
run imported, validated and rendered all 139 themes; the numbers, the per
theme table and the classification of the validation messages old themes carry
are in [`skin-gallery-report.md`](skin-gallery-report.md). The same work
against real VLC lives in `tools/parity/` (isolated virtual display, xdotool
GUI driving, MCP clients, numeric image and region comparison); its README
documents every command. The feature by feature comparison with the original
editor, including the remaining differences, is in
[`feature-parity.md`](feature-parity.md).

## Architecture

```
src/launcher/java/dev/zoroaster1x/vlcskin/launch/
        Java 8 entry point: checks the runtime, shows install instructions
        when it is older than 25, then reflects into the app.
src/main/java/dev/zoroaster1x/vlcskin/
  model, format, render, edit, action, describe, snapshot, mcp,
  update, cli, tui, example, util
        format, renderer, edit commands, MCP server, updater, CLI, TUI,
        examples. No Swing.
  app   Swing UI on FlatLaf and ModernDocking, panels, dialogs, inspector,
        headless UI harness, MCP UI bridge.
```

The format, render and tooling packages are UI free, so the CLI, TUI and MCP
server run without a display. The app adds the desktop window and never reaches
into model internals directly: every change goes through `EditorService` or an
undoable `ValueCommand`.

`AGENTS.md` is the repository reference: hard rules, data flow, threading, the
native build and the format knowledge that cost real time to find.

## Native image (optional)

GraalVM 25 can build a self-contained binary for the CLI, TUI and MCP entry
points, PNG rendering included. The desktop Swing window stays on the JVM: the
binary prints that pointer when started without a subcommand.

```bash
sdk install java 25.4.4.1+1-graalce
tools/build-native.sh
build/native/vlc-skin-studio --version
build/native/vlc-skin-studio render skin.xml -o preview.png
build/native/vlc-skin-studio mcp
```

Java2D inside the image has no font configuration of its own, so the build
writes `fontconfig.properties` next to the binary with the DejaVu families it
found; `VlcSkinStudio` points `sun.awt.fontconfig` at it. Keep that file and
the `.so` files that native-image places next to the executable.

The binary is about 68 MB. `picocli-codegen` runs at compile time for the CLI
metadata, and the AWT reflection and JNI entries collected with GraalVM's
tracing agent live under `src/main/resources/META-INF/native-image/`.

## Documentation bundle

The in-app documentation is bundled resources, not a web fetch:

* `tools/bundle-docs.py` turns crawled or handbook markdown into the viewer
  bundle under `src/main/resources/dev/zoroaster1x/vlcskin/app/docs/`.
* `tools/docs-graphics.py` generates the nine handbook diagrams with Pillow.
* `tools/dtd-to-markdown.py` generates the format reference from VLC's
  `share/skins2/skin.dtd`.

The reader is `app/docs/`: `DocumentationBundle` lists and loads pages,
`DocumentationSearch` ranks titles, headings and body text, and
`MarkdownRenderer` renders the page with its images.
