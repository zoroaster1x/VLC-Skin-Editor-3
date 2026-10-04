# Parity harness

Tools for testing VLC Skin Studio against the two things it must match: the
original VLC Skin Editor 0.8.6 and real VLC skins2. Everything here runs on an
isolated virtual display, never on the operator's desktop session, and every
step is scriptable so a finding can be reproduced from a command line.

Read `LESSONS.md` next to this file for the traps that cost real time: the
Flatpak staging path, the unmapped-window signature, the fullscreen controller
limitation, the ambient-DISPLAY trap, and the final verification chain.

The VLC behaviour that these tests assert is documented, with `file:line`
citations, in `docs/skins2-parser.md`, `docs/skins2-rendering.md` and
`docs/skins2-edge-cases.md`. The runnable edge cases live in
`src/test/resources/edge-cases/` and are opened by
`format/EdgeCaseFixturesTest`.

## 1. Set up

### Build the studio jar

```bash
export JAVA_HOME=/path/to/your/jdk-25      # any Java 25 toolchain
./gradlew build
```

Snapshot before long GUI sessions, because a Gradle build rewrites the jar in
place and a JVM that lazily loads a class from a truncated jar fails. `run.sh`
already does this; it only rebuilds the jar when it is missing, so force one
when the sources changed:

```bash
REBUILD=1 ./run.sh render /tmp/theme.xml -o /tmp/render.png
cp build/libs/vlc-skin-studio.jar build/parity/studio.jar
```

### Start the virtual display

```bash
tools/parity/display.sh start            # auto: Xvfb on PATH, else podman
tools/parity/display.sh status           # DISPLAY plus xdotool/wmctrl/import commands
tools/parity/display.sh window-list
tools/parity/display.sh stop
```

Provider details:

* `native`: `Xvfb`, `openbox`, `xdotool`, `wmctrl` and ImageMagick on PATH.
* `container`: a rootless podman container (default name `parity-xvfb`) runs
  Xvfb, xdotool, openbox and ImageMagick; the X socket is shared through
  `/tmp/.X11-unix`, so host apps connect with `DISPLAY=:99` while input and
  screenshots can come from inside the container. The package install happens
  once:

```bash
podman run -d --name parity-xvfb --security-opt label=disable \
    -v /tmp/.X11-unix:/tmp/.X11-unix:rw fedora:44 sleep infinity
podman exec parity-xvfb dnf install -y --setopt=install_weak_deps=False \
    Xvfb xdotool openbox wmctrl ImageMagick
```

The state file lives in `${PARITY_STATE_DIR:-/tmp/vlc-skin-parity}/display.env`.
Shells can source it; the Python helpers read it automatically.

### Get test themes

```bash
tools/parity/gallery_corpus.py --out build/parity/corpus
```

This downloads every theme the official gallery lists (138 archives, about
46 MB) and writes `manifest.json`. The VeLoCity skins used as a rendering
fixture are at `https://github.com/dmtiir/VeLoCity-Skin-for-VLC`; unzip them
next to the corpus and point the tools at the `theme.xml`.

One gallery archive is a Winamp2 skin (`ol bleu.vlt`, BMP files, no
theme.xml). VLC loads it through `share/skins2/winamp2.xml`; the studio does
the same with the bundled copy of that template, so it imports and renders
like every other theme.

### Original editor (for comparison runs)

The original needs a Java 8 toolchain and Ant is not needed; `javac` plus
`jar` are enough:

```bash
git clone https://code.videolan.org/videolan/skin-designer /tmp/parity/skin-designer
cd /tmp/parity/skin-designer
"$JAVA8_HOME/bin/javac" -nowarn -d build/classes $(find src -name '*.java')
cp -r src/vlcskineditor/icons/. build/classes/vlcskineditor/icons/
"$JAVA8_HOME/bin/jar" cfe VLCSkinEditor.jar vlcskineditor.Main -C build/classes .
DISPLAY=:99 "$JAVA8_HOME/bin/java" -jar VLCSkinEditor.jar &
```

It has no MCP and no scripts, so it is driven the way a person drives it:
`tools/parity/gui.py` (xdotool) plus window screenshots. Useful facts learned
the hard way:

* It always starts with a Welcome dialog on the display centre; click "Open an
  existing skin" and type the full path into the file chooser's File Name
  field, then press Return.
* Ctrl+S saves in place with no Save As dialog, so work on copies.
* `.vlt` files unpack to `<name>_unpacked/` next to the archive.
* The items tree is the third internal frame; double click opens an item
  editor, and the pencil button below the tree opens the selected one.
* The window title is `<path> - VLC Skin Editor 0.8.6.dev`, so address it with
  the substring `VLC Skin Editor`.

### Real VLC

VLC is the gold standard for what a theme should look like. The Flatpak
sandbox cannot read `/tmp`, so stage the theme folder into the Flatpak data
directory first; its host path is valid inside the sandbox because the sandbox
home is the real home:

```bash
cp -r /tmp/theme-folder ~/.var/app/org.videolan.VLC/data/parity/theme-folder
DISPLAY=:99 QT_QPA_PLATFORM=xcb flatpak run org.videolan.VLC \
    --no-one-instance -I skins2 \
    --skins2-last="$HOME/.var/app/org.videolan.VLC/data/parity/theme-folder/theme.xml"
```

* Confirm the load in the output: `skins2 interface: skin: <name> author: ...`.
  When the path is not readable, VLC silently falls back to the built-in
  `subX` skin, which is the trap that makes comparisons meaningless.
* The skin window is unmanaged: `wmctrl -l` does not list it. Find it with
  `xdotool search --name TopWindow` (class `Vlc`, title `VLC (TopWindow)`) and
  take its geometry with `xdotool getwindowgeometry --shell <id>`.
* `QT_QPA_PLATFORM=xcb` and an unset `WAYLAND_DISPLAY` keep Qt on the virtual
  X display.
* Close it with `flatpak kill org.videolan.VLC`.

The scripted version of all of this is:

```bash
tools/parity/vlc_shot.py --theme build/parity/work/velocity/theme.xml \
    --out build/parity/shots/velocity-vlc.png
```

## 2. Daily commands

```bash
# What windows exist, where, at what size.
tools/parity/gui.py windows

# Bring a window forward, click, screenshot the window only.
tools/parity/gui.py activate "VLC Skin Studio"
tools/parity/gui.py click "VLC Skin Studio" 590 100 --shot build/parity/shots/after.png

# Type into the focused field, send chords, drag on the canvas.
tools/parity/gui.py type "VLC Skin Editor 0.8.6.dev" "/tmp/theme.xml"
tools/parity/gui.py key "VLC Skin Studio" ctrl+s
tools/parity/gui.py drag "VLC Skin Studio" 700 400 760 460 --shot drag.png

# Whole screen when window tiling itself is under test.
tools/parity/display.sh screenshot root build/parity/shots/screen.png
```

Rules that keep runs reproducible:

* Address windows by a title substring, never by window id; ids change between
  runs. `gui.py` re-activates the window before every action.
* Coordinates in `gui.py` are window relative by default. Add `--screen` for
  absolute ones, and keep the display size fixed at `1920x1080x24`.
* A click script should screenshot straight after the click (`--shot`), so the
  evidence and the action live in one command line.
* xdotool's `type` treats every following argument as text. Run
  `xdotool type "..."` and `xdotool key Return` as two commands, or the key
  name ends up in the text field.

## 3. Scripted edits through MCP

`mcp_client.py` speaks stdio JSON-RPC to `java -jar ... mcp`. One shot:

```bash
python3 tools/parity/mcp_client.py --jar build/parity/studio.jar --open theme.xml \
    call document_info
```

A whole scenario, with placeholders, PNG capture and expectations:

```bash
tools/parity/scenario.py --jar build/parity/studio.jar \
    --scenario path/to/scenario.json \
    --corpus build/parity/corpus --work build/parity/runs
```

A scenario is a JSON file with `open`, `steps` and `expect`; `scenario.py
--help` and its docstring document the shape. Every step logs to
`<work>/<name>/results.jsonl`, images land beside it, and a non-zero exit means
a step errored or an expectation failed. See `scenario.py --help` for the
schema.

## 4. Output accuracy checks

### Pixels, numbers and why eyeballing is not a check

A person, and even more so a model, looking at two screenshots cannot tell a
one pixel thumb offset from a three pixel one, cannot measure clipped text, and
adapts to whatever it saw first. The harness therefore compares numerically:

```bash
# Exact, per-pixel comparison with a tolerance and an alignment search.
tools/parity/pixel_diff.py ours.png vlc.png --align 3 --tolerance 24 \
    --diff build/parity/diff.png --json build/parity/diff.json

# Block means, for full frames where font rasterization can never match.
tools/parity/pixel_diff.py ours.png vlc.png --mode blocks --block 8 --tolerance 6

# Geometry: region position and size deltas, one line per control.
tools/parity/region_diff.py ours.png vlc.png --max-shift 1 --max-size 2
```

`pixel_diff.py` counts differing pixels, prints the mismatch bounding box and
the largest channel delta, and writes a red-highlighted diff image.
`region_diff.py` ignores anti-aliased edges entirely: it finds non-background
regions (a button, a slider, a text run) in both images, matches them and
reports dx, dy, width and height per region. A control that is 3 px too low
shows up as a number; an anti-aliased edge does not. Adjacent fragments of one
control (a prev bar and its triangle) are merged; text runs are not, so font
ink differences do not fuse into a fake mismatch.

The end to end comparator does all of it for a layout:

```bash
tools/parity/vlc_compare.py --jar build/libs/vlc-skin-studio.jar \
    --theme path/to/theme.xml --window player --layout main \
    --out-dir build/parity/vlc/player-main
```

It renders the layout with MCP (player variables forced to VLC's no-media
values), copies the theme with the layout last and its window forced visible
(secondary windows start hidden behind a `visible` expression), launches VLC,
captures the skins2 top window by size, and writes `ours.png`, `vlc.png`,
`diff.png`, `side-by-side.png`, `report.json` and `report.txt`. Every step is
logged with a timestamp and a duration on stderr, for example:

```
[vlc-compare +1.7s (+0.2s)] studio rendered 2558 bytes to .../ours.png
[vlc-compare +3.7s (+1.5s)] VLC window 10485769 up (320x140), settling 6.0s
[vlc-compare +10.1s (+6.4s)] captured to .../vlc.png
```

A whole theme at once:

```bash
tools/parity/compare_all_layouts.py --jar build/libs/vlc-skin-studio.jar \
    --theme build/parity/example/velocity/theme.xml --out build/parity/compare
```

Measured results against VLC (VLC 4.0.0-dev, flatpak, virtual display):

| Layout | Blocks differing | Region problems |
|---|---|---|
| player/main | 0.109% | 0 |
| player/mainPL | 0.237% | 5, all text ink |
| sPlayer/sPL | 0.684% | 5, text ink |
| sPlayer/sMain | 1.215% | 0 |
| about/aMain | 2.024% | 2, text ink |
| fullscreenController/fcMain | not capturable idle | VLC only maps the fullscreen controller while a video plays fullscreen, so this window is verified through the same renderer paths as the others and by hand with media |

Text glyph ink can never be pixel identical between Java2D and VLC's FreeType
renderer; the studio does render `defaultfont` with VLC's own FreeSans
(bundled, see `src/main/resources/dev/zoroaster1x/vlcskin/render/fonts`), so
metrics and line breaks match.

### Theme round trip

```bash
tools/parity/compare_xml.py original/theme.xml edited/theme.xml
tools/parity/compare_xml.py --comments original/theme.xml edited/theme.xml
```

Comments are ignored by default because the original editor rewrites its
"Created using" banner on every save. Attribute order never matters; element
order, attributes and text do.

### Whole gallery sweep

```bash
python3 tools/gallery-conformance.py --jar build/parity/studio.jar \
    --work build/parity/sweep --out build/parity/sweep/report \
    --no-pack --vlts build/parity/corpus --jobs 8
```

Every `.vlt` is imported, inspected, validated and rendered. The report names
each failure with the exact command output. Add `--extra path/to/theme.xml` for
themes that ship with VLC itself. The expected result on the gallery corpus is
139/139 imported and rendered.

### Hidden VLC features that have their own checks

* Semicolon resource fallbacks (`up="volume_on;volume_on2"`): VLC uses the
  first resource that exists. Covered by `model/SkinIndexTest` and visible in
  the Winamp2 template.
* IniFile constants as colors (`color="pledit.text.normal"`): VLC registers
  `<ini id>.<section>.<key>` lowercased and resolves colors through it. Covered
  by `render/VlcSemanticsTest.iniFileConstantsResolveColors`.
* Winamp2 archives (`main.bmp`, no theme.xml): VLC falls back to
  `share/skins2/winamp2.xml`; the studio bundles a copy. Covered by
  `format/VltCodecTest.winamp2ArchivesGainTheBundledTemplate`.
* Playlist content: with no media VLC shows its two tree nodes "Playlist" and
  "Media Library", and its scroll position starts at 1.0. The preview matches.
  Covered by `render/VlcSemanticsTest.playlistShowsVlcIdleRowsAndScrollPosition`.
* Edge cases: run `./gradlew test --tests '*EdgeCaseFixturesTest*'` to open all
  53 fixtures in `src/test/resources/edge-cases/`.

## 4b. MCP: status, log and shared-file changes

The MCP server is a separate process, so the window cannot share its memory,
but it can share files:

* Every run appends timestamped lines to `<cache>/mcp.log`
  (`~/.cache/vlc-skin-studio/mcp.log` on Linux) and updates
  `<cache>/mcp-status.json` with pid, version, call count, last tool and last
  call time. `View > Panels > MCP activity` tails both live.
* Preferences has an "AI and MCP" section: enable/disable the server (the
  `mcp` command refuses to start when disabled unless `--force` is passed),
  the exact command to give an AI client, a Copy button, and the live status.
* Tool results can start with a `[NOTICE]` line when the file changed outside
  the server. The AI then calls `disk_diff` for the changed lines and
  `sync_from_disk` to three-way merge them into its document; edits only one
  side made are applied, a conflict keeps the AI's version and is reported.
  The merge is `format/XmlMerger`, covered by `format/XmlMergerTest` and
  `mcp/EditorDiskSyncTest`.

Watch a server by hand:

```bash
java -jar build/libs/vlc-skin-studio.jar mcp -f theme.xml
tail -f ~/.cache/vlc-skin-studio/mcp.log
```

## 4c. CLI and TUI logging

`--verbose` (or `-v`) turns on timestamped debug lines on stderr for every
subcommand, without touching the machine readable stdout:

```bash
./run.sh -v render theme.xml -o preview.png
# 16:31:36.264 [INFO] [main] command: -v render theme.xml -o preview.png
# 16:31:36.573 [DEBUG] [main] parsed in 298 ms: Neon player
# 16:31:36.601 [DEBUG] [main] rendered main/main at zoom 1 in 22 ms, 2558 bytes to /.../preview.png
# 16:31:36.608 [INFO] [main] command finished with 0 in 344 ms
```

The TUI logs every entered command the same way and its banner names the open
document, so a session can be reviewed from the log later.

## 5. Layout of a run

```
build/parity/
  studio.jar              snapshot under test
  corpus/                 gallery archives plus manifest.json
  runs/<scenario>/        results.jsonl, PNGs
  sweep/                  imported themes plus report.md/report.json
  shots/                  GUI and VLC screenshots
```

Nothing in `build/` is tracked, so runs stay out of the repository.

## 6. Where the VLC truth comes from

The references and fixtures were read from VLC revision
`e77e49b5dff7484dbf70cd1540a6a568e1890280` (4.0.0-dev, 2026-10-02):

* Parser and format: `docs/skins2-parser.md`
* Rendering: `docs/skins2-rendering.md`
* Edge cases and fixtures: `docs/skins2-edge-cases.md`,
  `src/test/resources/edge-cases/`

Re-clone the source to check a citation:

```bash
git clone --depth 1 https://github.com/videolan/vlc.git /tmp/parity/vlc
git -C /tmp/parity/vlc log -1 --format=%H
```

For a smaller checkout use a sparse clone with
`git sparse-checkout set modules/gui/skins2 share/skins2`.
