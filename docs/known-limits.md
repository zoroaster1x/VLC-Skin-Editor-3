# Known limits

* Localization covers the original editor's surfaces (menus, toolbar, panel
  titles, common dialogs). The newer panels stay English until a translation
  exists; the bundle mechanism is in place, and translations can be dropped
  into the config `lang` folder without rebuilding.
* VLT import and export show a progress dialog but no byte level progress bar.
* Real chart parts, video playback and tooltips are VLC runtime behavior and
  are not simulated beyond the black video rectangle.
* The preview renders fonts with the JVM's font stack; a skin that depends on
  hinting differences may look a pixel or two off from VLC on another platform.
* The GraalVM native image covers the CLI, TUI, MCP server and PNG rendering;
  the desktop window runs on the JVM and the binary prints a pointer when it is
  started without a subcommand. The image needs the `.so` files and the
  generated `fontconfig.properties` next to the executable.
