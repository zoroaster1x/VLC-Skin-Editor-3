# VLC Skin Studio {{VERSION}}

Read, edit, validate, render and package VLC skins2 themes. A dockable desktop UI on FlatLaf, a terminal UI, a CLI and an MCP server for AI clients share one document model. Unknown attributes and elements are preserved on save, so a theme from any VLC version opens and writes back without losing data. Requires Java 25.

{{ATTESTATION}}

## Changes since {{PREVIOUS}}

{{CHANGES}}

## Commits

{{COMMITS}}

**Full changelog:** {{COMPARE}}

## Install

1. Download `vlc-skin-studio-{{VERSION}}.zip` and `SHA256SUMS` from the assets below.
2. Check the download: `sha256sum -c SHA256SUMS`.
3. Unzip the package. It contains `vlc-skin-studio.jar`, `run.bat` for Windows, `run.sh` for Linux and macOS, and `README.txt` with the same instructions.

Windows: install the Azul Zulu JRE 25 `.msi` from <https://www.azul.com/downloads/?version=java-25-lts&package=jre&os=windows&architecture=x86-64-bit#zulu>, then double-click `run.bat`.

macOS: install the Azul Zulu JRE 25 from <https://www.azul.com/downloads/?version=java-25-lts&package=jre&os=macos#zulu> (ARM 64-bit on Apple Silicon, x86 64-bit on Intel), then run `./run.sh` in Terminal.

Linux: install the Azul Zulu JRE 25 from <https://www.azul.com/downloads/?version=java-25-lts&package=jre&os=linux#zulu> (x86 64-bit or ARM 64-bit), then run `./run.sh`.

Without arguments `run.bat` and `run.sh` open the desktop window; anything you add goes to the application, so `run.sh --help` lists the CLI, the terminal UI and the MCP server. If Java 25 is already installed you can also double-click `vlc-skin-studio.jar` or run `java -jar vlc-skin-studio.jar` directly. VLC itself is only needed for the "Test skin in VLC" menu item.

## Documentation

The controls, the CLI and MCP reference and the honest known limits live in the [README](https://github.com/zoroaster1x/VLC-Skin-Editor-3#readme).

## Funding

If this project saves you time, consider supporting its development. Every contribution goes toward maintenance, new format coverage and the long tail of edge cases that make real skins painful to edit.

**Monero (XMR):**

```
8BdxmQSniku4dBJXWPXeXvgjztmj5nmvWQqeCrVvCtYciusbAyo4rqrGCefTfQ4gGaVZmLN7VgLiYUYyBdYFEwHn1UWPjWs
```

> **Tip:** You can easily purchase Litecoin using [Cake Wallet](https://cakewallet.com/) and then, within the app, create a Monero wallet and exchange the Litecoin into it, pointed at the address above.

Crypto isn't your thing? Starring the repository, filing clear bug reports with a sample skin, and telling other skinners about VLC Skin Studio all help just as much.

## License

GPL-3.0-or-later. Copyright (C) 2026 Zoroaster1x.
