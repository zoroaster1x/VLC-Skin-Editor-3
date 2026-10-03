#!/usr/bin/env python3
"""Turn VLC's skins2 skin.dtd into a complete markdown format reference.

Usage:
    python3 tools/dtd-to-markdown.py path/to/skin.dtd out/format-reference.md

The output has one section per element with its children, and a table per
element listing every attribute, its default and whether it is required. This
is generated, so it cannot drift from the DTD VLC actually parses.
"""

import re
import sys
from pathlib import Path

ELEMENT = re.compile(r"<!ELEMENT\s+(\w+)\s+([^>]+)>", re.IGNORECASE | re.DOTALL)
ATTLIST = re.compile(r"<!ATTLIST\s+(\w+)\s+(.*?)>", re.IGNORECASE | re.DOTALL)
ATTR_LINE = re.compile(r"(\w+)\s+(\w+)\s+(#REQUIRED|#IMPLIED|\"[^\"]*\"|'[^']*')", re.IGNORECASE)

ELEMENT_NOTES = {
    "Theme": "The document root. Version must be 2.x for skins2.",
    "ThemeInfo": "Human readable metadata: name, author, email, webpage.",
    "Window": "A top level window VLC creates. Owns layouts and their sizes.",
    "Layout": "One arrangement of items inside a window. VLC switches between layouts.",
    "Group": "Invisible container that moves its children together.",
    "Panel": "Visible container with its own background and size.",
    "Anchor": "Invisible marker used as a reference point for other items.",
    "Button": "Clickable control with up, down and over images plus an action.",
    "Checkbox": "Two state control with its own images and state expression.",
    "Image": "Static picture, optionally clickable through an action.",
    "Text": "A label rendered with a font resource and $ variables.",
    "Slider": "Track and thumb control for a player value. Points describe the track.",
    "RadialSlider": "Circular slider drawn along an arc, same concept as Slider.",
    "SliderBackground": "A bitmap cut into a frame grid, drawn behind a slider.",
    "Video": "The video surface of the player inside the layout.",
    "Playlist": "The playlist view, optionally with a scrollbar slider child.",
    "Playtree": "The same control as Playlist, older spelling with folder rows.",
    "Bitmap": "An image resource, alphacolor keyed and cut into nbframes strips.",
    "SubBitmap": "A named rectangle cut out of a parent Bitmap.",
    "Font": "A TrueType or OpenType font resource with a size.",
    "BitmapFont": "A font drawn from a bitmap, kept as parsed data.",
    "PopupMenu": "Context menu resource with items and separators.",
    "MenuItem": "One entry of a PopupMenu with its action.",
    "MenuSeparator": "A divider inside a PopupMenu.",
    "IniFile": "An ini file resource used by some skins for configuration.",
    "Include": "Pulls another skin file into the theme.",
}

ATTRIBUTE_NOTES = {
    "id": "Unique name in its own namespace. VLC treats the literal none as no id.",
    "x": "Left position in layout pixels, before anchoring.",
    "y": "Top position in layout pixels, before anchoring.",
    "width": "Width in pixels. A negative width stretches to the right edge.",
    "height": "Height in pixels. A negative height stretches to the bottom edge.",
    "minwidth": "Smallest width when the layout shrinks.",
    "maxwidth": "Largest width when the layout grows.",
    "minheight": "Smallest height when the layout shrinks.",
    "maxheight": "Largest height when the layout grows.",
    "lefttop": "Anchor name for the top left corner. Items keep their distance to it.",
    "rightbottom": "Anchor name for the bottom right corner. Defaults to lefttop.",
    "xkeepratio": "Keep the horizontal distance between both anchors when resizing.",
    "ykeepratio": "Keep the vertical distance between both anchors when resizing.",
    "visible": "Boolean expression, for example not vlc.isPlaying. True by default.",
    "help": "Help text shown by the editor and in some VLC views.",
    "tooltiptext": "Tooltip shown when the pointer rests on the item.",
    "up": "Bitmap or sub bitmap drawn in the normal state.",
    "down": "Bitmap or sub bitmap drawn while pressed.",
    "over": "Bitmap or sub bitmap drawn while hovered.",
    "action": "Semicolon separated action chain, for example vlc.play().",
    "state": "Boolean expression or value for a checkbox state.",
    "text": "Label text with $ variables such as $N, $T, $V.",
    "font": "Font resource id used to draw the text.",
    "points": "Slider track control points as x,y pairs, sampled as a bezier.",
    "thickness": "Slider track thickness in pixels.",
    "value": "The player value the slider drives, for example position or volume.",
    "background": "Bitmap id of a slider background or panel background.",
    "nbframes": "Number of equal frames in a bitmap strip or background grid.",
    "fps": "Animation frames per second for a multi frame bitmap.",
    "loop": "Restart the animation after the last frame.",
    "alphacolor": "Pixels with exactly this RGB become fully transparent.",
    "file": "Path to the asset, relative to the skin folder.",
    "size": "Font size in points.",
    "type": "Value kind for a slider, for example position, volume or time.",
    "image": "Bitmap or sub bitmap id drawn behind a control.",
    "nbhoriz": "Number of background frames per row.",
    "nbvert": "Number of background frame rows.",
    "padhoriz": "Pixels between horizontal frames.",
    "padvert": "Pixels between vertical frame rows.",
    "range": "Anchor range in layout pixels.",
    "magnet": "Snapping distance used by the editor when dragging items.",
    "alpha": "Window opacity, 0 to 255.",
    "movealpha": "Opacity of the window while it is being moved.",
    "tooltipfont": "Font resource used for tooltips.",
    "version": "Skin format version, must be 2.x for VLC skins2.",
    "name": "Human readable name shown in VLC and the editor.",
    "author": "Author name kept in ThemeInfo.",
    "email": "Contact address kept in ThemeInfo.",
    "webpage": "Project page kept in ThemeInfo.",
    "loopcount": "How many times a playlist entry repeats.",
    "items": "Children of the element, as listed above.",
}



def parse(dtd_text):
    elements = {}
    order = []
    for name, children in ELEMENT.findall(dtd_text):
        elements[name] = {"children": " ".join(children.split()), "attributes": []}
        order.append(name)
    for name, body in ATTLIST.findall(dtd_text):
        if name not in elements:
            elements[name] = {"children": "", "attributes": []}
            order.append(name)
        for attr, kind, default in ATTR_LINE.findall(body):
            default = default.strip("'\"")
            elements[name]["attributes"].append((attr, kind, default))
    return elements, order


def describe_children(children):
    text = children.strip()
    if text.startswith("("):
        return text
    return ""


def main():
    if len(sys.argv) != 3:
        print(__doc__, file=sys.stderr)
        return 2
    source = Path(sys.argv[1])
    target = Path(sys.argv[2])
    elements, order = parse(source.read_text(encoding="utf-8", errors="replace"))

    lines = [
        "---",
        "title: Format reference (under the hood)",
        "section: Appendix: the file format",
        "source: generated",
        "---",
        "",
        "# skins2 format reference",
        "",
        "Generated from VLC's `share/skins2/skin.dtd` by `tools/dtd-to-markdown.py`.",
        "Every element and attribute VLC parses is listed, with the DTD default.",
        "Skins2 version: 2.0.",
        "",
        "## Element hierarchy",
        "",
        "```",
    ]

    def walk(name, depth, seen):
        lines.append("  " * depth + name)
        if name in seen or depth > 4:
            return
        children = re.findall(r"[A-Z]\w*", describe_children(elements.get(name, {}).get("children", "")))
        for child in children:
            if child in elements:
                walk(child, depth + 1, seen | {name})

    walk("Theme", 0, set())
    lines += ["```", "", "## Elements", ""]

    for name in order:
        entry = elements[name]
        lines.append(f"### {name}")
        lines.append("")
        note = ELEMENT_NOTES.get(name)
        if note:
            lines.append(note)
            lines.append("")
        children = describe_children(entry["children"])
        if children and children != "EMPTY":
            lines.append(f"Children: `{children}`")
            lines.append("")
        if entry["attributes"]:
            lines.append("| Attribute | Type | Required or default | Meaning |")
            lines.append("|---|---|---|---|")
            for attr, kind, default in entry["attributes"]:
                requirement = "required" if default == "#REQUIRED" else (
                    "optional" if default == "#IMPLIED" else f"`{default}`")
                meaning = ATTRIBUTE_NOTES.get(attr, "")
                lines.append(f"| `{attr}` | {kind} | {requirement} | {meaning} |")
            lines.append("")
        else:
            lines.append("No attributes.")
            lines.append("")

    target.parent.mkdir(parents=True, exist_ok=True)
    target.write_text("\n".join(lines) + "\n", encoding="utf-8")
    print(f"wrote {target}: {len(order)} elements")
    return 0


if __name__ == "__main__":
    sys.exit(main())
