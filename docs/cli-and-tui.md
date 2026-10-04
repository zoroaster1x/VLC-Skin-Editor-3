# CLI and terminal UI

Both run on the same core as the desktop window and need no display.

## CLI

```
vlc-skin-studio <command> [options]

  new        Create a new skin file, empty or from an example
  render     Render a layout to PNG and print or write its geometry
  inspect    Print everything known about a skin as JSON
  validate   Check a skin for errors and warnings
  vlt        Import or export a .vlt theme archive
  tui        Browse and edit a skin in the terminal
  mcp        Run the MCP server over stdio
  examples   List the built in example themes
```

Examples:

```bash
# Start from a generated example with real images.
java -jar vlc-skin-studio.jar new --example neon out/theme.xml

# Validate and render at 2x, with the geometry as JSON.
java -jar vlc-skin-studio.jar validate out/theme.xml
java -jar vlc-skin-studio.jar render out/theme.xml -z 2 -o preview.png --json geometry.json

# Package a theme for VLC.
java -jar vlc-skin-studio.jar vlt export out/theme.xml out/theme.vlt
vlc -I skins2 --skins2-last=out/theme.xml
```

`render` writes a PNG and, with `--json`, a `layout_tree` style description:
every item with id, type, absolute x/y/width/height, z order, visibility, text
and the attributes that matter. A model without image input can reason about
the layout from that alone.

## Terminal UI

```
./run.sh tui out/theme.xml
vlcskin> tree            # geometry as a table
vlcskin> render!         # the preview as truecolor half blocks
vlcskin> set vlc.isPlaying true
vlcskin> show play_btn
vlcskin> validate
```

The TUI is line based on purpose: it works over SSH, in CI logs, and is covered
by tests without a terminal.
