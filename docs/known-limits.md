# Known limits

* Localization covers the original editor's surfaces (menus, toolbar, panel
  titles, common dialogs). The newer panels stay English until a translation
  exists; the bundle mechanism is in place, and translations can be dropped
  into the config `lang` folder without rebuilding.
* VLT import and export show a progress dialog but no byte level progress bar.
* Real chart parts, video playback and tooltips are VLC runtime behavior and
  are not simulated beyond the black video rectangle.
* The preview is a static frame at the authored layout size, which is what VLC
  shows before any resize and what PNG export needs. That means:
  * `lefttop`/`rightbottom` anchors and `xkeepratio`/`ykeepratio` do not move
    controls, because they only matter after a window resize.
  * image `resize` modes and `art="true"` cover art are not simulated; the
    base image draws at its intrinsic size.
  * text `scrolling="auto"` draws the frame at scroll position 0 instead of
    animating.
  * one global slider value drives every slider in the preview; VLC tracks
    time, volume and equalizer sliders separately.
  * the playlist shows VLC's idle rows "Playlist" and "Media Library" rather
    than a live playlist.
* Fonts render through the JVM font stack. `defaultfont` uses VLC's bundled
  FreeSans so metrics match, but glyph anti-aliasing can still differ from
  FreeType by a pixel or two; `tools/parity/region_diff.py` checks geometry,
  not ink.
* Unknown child elements are preserved verbatim and keep their relative order,
  but are written after the known children of their parent. VLC ignores them,
  so only a hand inspection of the file sees the canonical position.
* Bitmap fonts, IniFile contents and PopupMenu entries are parsed, preserved
  and edited for their attributes, but their contents have no dedicated editor.
* The GraalVM native image covers the CLI, TUI, MCP server and PNG rendering;
  the desktop window runs on the JVM and the binary prints a pointer when it is
  started without a subcommand. The image needs the `.so` files and the
  generated `fontconfig.properties` next to the executable.
* The MCP server is a separate process from the desktop window: both may edit
  the same file, and the server detects that change with `disk_diff` and
  `sync_from_disk`; it cannot see unsaved in-memory edits in the window.
