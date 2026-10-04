# Parity lessons and exact commands

Every fact below cost real time to discover. They are here so the next person
or model does not rediscover them. The tool entry points are in
`tools/parity/README.md`; this file is the why and the traps.

## 1. The virtual display

The tests must never touch the operator's session. A rootless podman container
runs Xvfb and the input tools, and shares only the X socket:

```bash
podman run -d --name parity-xvfb --security-opt label=disable \
    -v /tmp/.X11-unix:/tmp/.X11-unix:rw fedora:44 sleep infinity
podman exec parity-xvfb dnf install -y --setopt=install_weak_deps=False \
    Xvfb xdotool openbox wmctrl ImageMagick
podman exec -d -e DISPLAY=:99 parity-xvfb sh -c \
    'Xvfb :99 -screen 0 1920x1080x24 -ac -nolisten tcp & sleep 1; openbox --sm-disable & wait'
```

`tools/parity/display.sh start` does this and writes the state file
`/tmp/vlc-skin-parity/display.env` with the exact `xdotool`, `wmctrl` and
`import` command prefixes (native or `podman exec ...`). All Python helpers
read it.

Traps:

* **Never trust the ambient `DISPLAY`.** This machine is Wayland with an
  Xwayland at `:0`. `import -window <id>` against `:0` does not find the
  window and falls back to ImageMagick's interactive picker, which hangs
  forever. Every tool goes through `Gui.env()`, which sets `DISPLAY` from the
  state file and drops `WAYLAND_DISPLAY`.
* Every subprocess that can prompt or block runs with a timeout (import 20 s,
  flatpak kill 15 s, xdotool 30 s). A hang is a bug in the harness.
* xdotool's `type` treats every following argument as text. Run
  `xdotool type "..."` and `xdotool key Return` as separate commands.
* Window ids are reused across app restarts. Record the TopWindow ids **before**
  launching and only accept a new id; otherwise a capture can target a dead
  window.

## 2. Running VLC for a comparison

The Flatpak sandbox cannot read `/tmp`, so the theme folder is staged under the
Flatpak data directory, whose host path is valid inside the sandbox:

```bash
cp -r /path/to/theme-folder ~/.var/app/org.videolan.VLC/data/parity/theme-folder
DISPLAY=:99 QT_QPA_PLATFORM=xcb flatpak run org.videolan.VLC \
    --no-one-instance -I skins2 \
    --skins2-last="$HOME/.var/app/org.videolan.VLC/data/parity/theme-folder/theme.xml"
```

* Confirm the load in the output: `skins2 interface: skin: <name> author: ...`.
  If the path was unreadable VLC silently falls back to its built-in `subX`
  skin; comparing then measures nothing.
* `flatpak kill org.videolan.VLC` closes it.
* The skin windows are unmanaged: `wmctrl -l` does not list them and
  `xdotool search --name TopWindow` does. Names are `VLC (TopWindow)` for
  normal windows, `VLC (FscWindow)` for the fullscreen controller and
  `VLC (Fullscreen)` for the fullscreen window.
* **An unmapped VLC window sits at `-10,0` with size `10x10`.** If every
  TopWindow is `10x10`, VLC mapped nothing usable; the capture would be
  meaningless. The comparator ignores windows under 40x30 for this reason.

### Opening a specific layout

VLC opens the **last** layout of a window, and secondary windows start hidden
behind a `visible` expression. The comparator therefore copies the theme with
the target layout moved last and the target window forced
`visible="true"` while the others are forced `false`. With that copy, VLC
opens exactly the layout under test. Without it, every capture is the player
window and a comparison against a small window reads as a 80% difference.

### The fullscreen controller is special

`fullscreenController/fcMain` is a real limit: VLC did not map it in any of
these attempts, all from a clean launch:

```bash
# Both leave every VLC window at -10,0 10x10, the unmapped signature.
flatpak run org.videolan.VLC --no-one-instance -I skins2 --skins2-last=...
flatpak run org.videolan.VLC --no-one-instance --fullscreen -I skins2 --skins2-last=...
```

The controller appears only while a video plays fullscreen (the skin's
`visible` expression for it is runtime state, not the XML default). To capture
it, generate a short test clip with ffmpeg, play it with the skin, enter
fullscreen, and move the mouse so the controller does not auto-hide. Until
then, verify that window through the shared renderer paths and by hand with
media. Do not waste time retrying `--fullscreen` without media.

## 3. Comparing our render with VLC

```bash
# One layout, end to end: MCP render, VLC capture, diff, side by side.
tools/parity/vlc_compare.py --jar build/libs/vlc-skin-studio.jar \
    --theme path/to/theme.xml --window player --layout mainPL \
    --out-dir build/parity/vlc/player-mainPL

# Every window and layout of a theme, one summary line each.
tools/parity/compare_all_layouts.py --jar build/libs/vlc-skin-studio.jar \
    --theme build/parity/example/velocity/theme.xml --out build/parity/compare
```

The comparator forces the preview variables to VLC's no-media state before
rendering, otherwise the two sides show different slider thumbs and sample
text. The values come from `var_text.cpp` and `vars/time.cpp`: time strings are
`-:--:--` with no input, bitrate and sample rate are empty at zero, volume is
`0`, stream name and URI are empty.

How to read the numbers:

* `pixel_diff.py --mode exact` counts pixels; anti-aliased edges and font
  ink dominate. Use it for controls or with masks.
* `pixel_diff.py --mode blocks` averages 8x8 tiles; a shifted control moves
  whole tiles, font ink moves one or two.
* `region_diff.py` is the one to trust for geometry: it reports dx, dy, width
  and height per non-background region and ignores ink. A control that is
  3 px low is a number there; an anti-aliased edge is not.
* Text can never reach zero. The studio renders `defaultfont` with VLC's
  bundled FreeSans, so metrics and breaks match, but FreeType and Java2D
  rasterize glyph edges differently. Expect a handful of regions to differ
  only in ink and treat them as passing.

Measured on VeLoCity Dark (VLC 4.0.0-dev, flatpak, Xvfb):

| Layout | Blocks differing | Region problems |
|---|---|---|
| player/main | 0.109% | 0 |
| player/mainPL | 0.237% | 5, text ink |
| sPlayer/sPL | 0.684% | 5, text ink |
| sPlayer/sMain | 1.215% | 0 |
| about/aMain | 2.024% | 2, text ink |
| fullscreenController/fcMain | not capturable idle | see above |

### What the region check caught that eyeballing did not

Before the sub-bitmap fix, `sPlayer/sMain` looked "roughly right" but the diff
was 7.3% of blocks: the theme's time strip is a **SubBitmap** of a larger
sheet, and using the whole parent drew a 380 wide grey bar across the buttons.
The region report showed a 250 px wide region where VLC had 203 px. The fix is
in `SliderGeometry.backgroundImage`. This is the whole argument for numeric
comparison: the human eye accepted the wrong image.

## 4. Verifying the theme files

```bash
tools/parity/compare_xml.py original/theme.xml roundtrip.xml
python3 tools/gallery-conformance.py --jar build/libs/vlc-skin-studio.jar \
    --work build/parity/sweep --out build/parity/sweep/report \
    --no-pack --vlts build/parity/corpus --jobs 8
```

The gallery corpus is 138 archives (46 MB); the sweep result to hold is
`139/139 imported and rendered` (the corpus contains one archive that bundles a
second theme). One gallery entry is a Winamp2 skin with no `theme.xml`; VLC
renders it through `share/skins2/winamp2.xml` and so does the studio after the
Winamp2 fallback was added.

## 5. The final verification chain

Before a release or a large commit, run exactly this and read every line:

```bash
export JAVA_HOME=/path/to/your/jdk-25
./gradlew build                      # compile, every suite, the fat jar
VELOCITY_THEME=src/main/resources/dev/zoroaster1x/vlcskin/example/velocity/theme.xml \
    ./gradlew uiScreenshots          # refresh screenshots/ including velocity-*
python3 tools/parity/compare_all_layouts.py \
    --jar build/libs/vlc-skin-studio.jar \
    --theme src/main/resources/dev/zoroaster1x/vlcskin/example/velocity/theme.xml \
    --out build/parity/compare
python3 tools/gallery-conformance.py \
    --jar build/libs/vlc-skin-studio.jar \
    --work build/parity/sweep --out build/parity/sweep/report \
    --no-pack --vlts build/parity/corpus --jobs 8
```

Every tool logs each step with a timestamp and a duration on stderr
(`[vlc-compare +10.1s (+6.4s)] captured to ...`), so a slow phase is visible
without a profiler. The CLI and TUI share the `--verbose` flag and print the
same kind of lines.

## 6. MCP sessions and shared files

The MCP server is a separate process; it cannot see the desktop window's
unsaved memory. What it can do:

* It logs every call with a timestamp and duration to `<cache>/mcp.log` and
  updates `mcp-status.json` (`AppPaths.cacheDir()`, platform correct). The MCP
  activity panel tails both live; Preferences shows running/calls/last tool.
* When the file changed on disk, the next tool result starts with a `[NOTICE]`
  line naming the changed line counts. `disk_diff` lists the changed lines and
  `sync_from_disk` three-way merges them into the session (`XmlMerger`); a
  conflict keeps the session's version and is reported. Never tell a model to
  reload or overwrite instead: that discards one side's work.
* Handshake check after a rebuild:

```bash
printf '%s\n' \
  '{"jsonrpc":"2.0","id":1,"method":"initialize","params":{"protocolVersion":"2025-11-25","capabilities":{},"clientInfo":{"name":"probe","version":"1"}}}' \
  '{"jsonrpc":"2.0","method":"notifications/initialized"}' \
  '{"jsonrpc":"2.0","id":2,"method":"tools/list","params":{}}' \
  | java -jar build/libs/vlc-skin-studio.jar mcp | tail -1 | head -c 200
```

The server can be disabled in Preferences; `mcp` then exits 3 unless
`--force` is passed.
