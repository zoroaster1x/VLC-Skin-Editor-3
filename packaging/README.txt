VLC Skin Studio @version@
=========================

Read, edit, validate, render and package VLC skins2 themes: a dockable
desktop UI, a terminal UI, a CLI and an MCP server for AI clients, all on
top of one document model.

Project and documentation:
  https://github.com/zoroaster1x/VLC-Skin-Editor-3

Latest release:
  https://github.com/zoroaster1x/VLC-Skin-Editor-3/releases/latest

This package contains:
  vlc-skin-studio.jar   the application; desktop UI when started without
                        arguments, CLI, terminal UI and MCP server otherwise
  run.bat               launcher for Windows
  run.sh                launcher for Linux and macOS
  README.txt            this file

Requirements
------------
Java 25 or newer. A JRE is enough, for example the Azul Zulu build of
Java 25. VLC itself is only needed for the "Test skin in VLC" menu item.

run.bat and run.sh check for Java before starting. When no Java is found
they stop with:

  JAVA NOT INSTALLED. Please download from: <link>

and the link for your operating system and CPU. The sections below repeat
those links and tell you which file to take.

Windows
-------
1. Download the Azul Zulu JRE 25 installer (.msi) from:
   https://www.azul.com/downloads/?version=java-25-lts&package=jre&os=windows&architecture=x86-64-bit#zulu
   On Windows on ARM, switch Architecture to ARM 64-bit; there is no .msi
   for ARM, take the .zip and note the folder you unpack it into.
2. Run the .msi and click through the installer. On ARM, set JAVA_HOME to
   the unpacked folder instead.
3. Unzip this package and double-click run.bat.

If run.bat still prints JAVA NOT INSTALLED, run "java -version" in a new
Command Prompt. When that fails, Java is not on your PATH: reinstall it or
set JAVA_HOME to the folder that contains bin\java.exe.

macOS
-----
1. Download the Azul Zulu JRE 25 for macOS from:
   https://www.azul.com/downloads/?version=java-25-lts&package=jre&os=macos#zulu
   Apple Silicon (M1 and newer): Architecture ARM 64-bit, take the .dmg.
   Intel: Architecture x86 64-bit, take the .tar.gz or .zip.
   Homebrew instead: brew install --cask zulu@25
2. In Terminal, change into this folder and run:
     chmod +x run.sh
     ./run.sh

If run.sh prints JAVA NOT INSTALLED, install the JRE above, or use SDKMAN:
https://sdkman.io/ then "sdk install java 25-zulu".

Linux
-----
1. Download the Azul Zulu JRE 25 for Linux from:
   https://www.azul.com/downloads/?version=java-25-lts&package=jre&os=linux&architecture=x86-64-bit#zulu
   On ARM machines switch Architecture to ARM 64-bit. Take the .tar.gz, or
   the .deb/.rpm package if your distribution uses them.
2. In a terminal in this folder run:
     chmod +x run.sh
     ./run.sh

If run.sh prints JAVA NOT INSTALLED, install the JRE above, use your
distribution packages, or use SDKMAN: https://sdkman.io/ then
"sdk install java 25-zulu".

Starting the application
------------------------
run.bat and run.sh open the desktop window. Anything you add on the command
line goes to the application, for example:

  run.sh --help                             list every command
  run.sh render theme.xml -o preview.png    render a theme to PNG
  run.sh validate theme.xml                 validate a theme
  run.sh vlt export theme.xml out.vlt       package a theme
  run.sh tui theme.xml                      terminal UI
  run.sh mcp                                MCP server on stdio

If Java 25 is already installed you can also skip the launchers entirely
and double-click vlc-skin-studio.jar, or run:

  java -jar vlc-skin-studio.jar

License
-------
GPL-3.0-or-later. Source, issues and releases:
https://github.com/zoroaster1x/VLC-Skin-Editor-3
