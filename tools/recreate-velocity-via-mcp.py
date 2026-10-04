#!/usr/bin/env python3
"""Recreate the VeLoCity player window through the VLC Skin Studio MCP server.

This is a real external MCP client over stdio. It opens the original theme,
renders it, then rebuilds the player layout element by element from the same
sprite sheets using only MCP tools: new_skin, add_resource, add_sub_bitmap,
add_item, set_item_property, render_layout, validate_skin and save_skin. Every
milestone is rendered so the result can be reviewed and turned into a GIF.

Usage:
    python3 tools/recreate-velocity-via-mcp.py \
        --jar build/libs/vlc-skin-studio.jar \
        --theme /path/to/velocity/theme.xml \
        --out screenshots

VeLoCity is MIT licensed, Copyright (c) 2022 dmtiir. A copy of the theme ships
as the built in "velocity" example (see src/main/resources/dev/zoroaster1x/vlcskin/example/velocity,
license included there); this script rebuilds the player window from its assets
through the MCP tools as a demonstration.
"""

import argparse
import base64
import json
import re
import shutil
import subprocess
import sys
import xml.etree.ElementTree as ET
from pathlib import Path


class McpClient:
    def __init__(self, jar):
        self.proc = subprocess.Popen(
            ["java", "-jar", str(jar), "mcp"],
            stdin=subprocess.PIPE,
            stdout=subprocess.PIPE,
            stderr=subprocess.DEVNULL,
            text=True,
            bufsize=1,
        )
        self.next_id = 1
        self.initialize()

    def initialize(self):
        self.request(
            "initialize",
            {
                "protocolVersion": "2025-11-25",
                "capabilities": {},
                "clientInfo": {"name": "velocity-recreation", "version": "1.0"},
            },
        )
        self.notify("notifications/initialized")
        tools = self.request("tools/list", {})
        names = [tool["name"] for tool in tools.get("tools", [])]
        if "add_item" not in names:
            raise RuntimeError("the MCP server has no add_item tool")
        return names

    def notify(self, method, params=None):
        message = {"jsonrpc": "2.0", "method": method}
        if params is not None:
            message["params"] = params
        self.proc.stdin.write(json.dumps(message) + "\n")
        self.proc.stdin.flush()

    def request(self, method, params):
        request_id = self.next_id
        self.next_id += 1
        self.proc.stdin.write(
            json.dumps({"jsonrpc": "2.0", "id": request_id, "method": method, "params": params}) + "\n"
        )
        self.proc.stdin.flush()
        while True:
            line = self.proc.stdout.readline()
            if not line:
                raise RuntimeError("the MCP server closed the connection")
            try:
                message = json.loads(line)
            except json.JSONDecodeError:
                continue
            if message.get("id") == request_id:
                if "error" in message:
                    raise RuntimeError(f"{method} failed: {message['error']}")
                return message.get("result", {})

    def call(self, name, arguments=None):
        result = self.request("tools/call", {"name": name, "arguments": arguments or {}})
        if result.get("isError"):
            text = "".join(part.get("text", "") for part in result.get("content", []))
            raise RuntimeError(f"{name} failed: {text}")
        return result

    def call_png(self, name, arguments):
        result = self.call(name, arguments)
        for part in result.get("content", []):
            if part.get("type") == "image":
                return base64.b64decode(part["data"])
        return None

    def call_text(self, name, arguments=None):
        result = self.call(name, arguments)
        return "".join(part.get("text", "") for part in result.get("content", []))

    def close(self):
        try:
            self.proc.terminate()
            self.proc.wait(timeout=10)
        except Exception:
            self.proc.kill()


TAG_TO_TYPE = {
    "Image": "Image",
    "Button": "Button",
    "Checkbox": "Checkbox",
    "Text": "Text",
    "Slider": "Slider",
    "RadialSlider": "RadialSlider",
    "Video": "Video",
    "Group": "Group",
    "Panel": "Panel",
    "Playlist": "Playlist",
    "Playtree": "Playtree",
    "Anchor": "Anchor",
}


def index_assets(theme_path):
    tree = ET.parse(theme_path)
    root = tree.getroot()
    bitmaps = {}
    for bitmap in root.findall("Bitmap"):
        bitmap_id = bitmap.get("id")
        subs = {}
        for sub in bitmap.findall("SubBitmap"):
            subs[sub.get("id")] = sub.attrib
        bitmaps[bitmap_id] = {"file": bitmap.get("file"), "subs": subs}
    fonts = {}
    for font in root.findall("Font"):
        fonts[font.get("id")] = int(font.get("size", "12"))
    return root, bitmaps, fonts


def find_layout(root, window_id, layout_id):
    for window in root.findall("Window"):
        if window.get("id") != window_id:
            continue
        for layout in window.findall("Layout"):
            if layout.get("id") == layout_id:
                return layout
    raise RuntimeError(f"window {window_id} layout {layout_id} not found")


def referenced_assets(layout):
    images = set()
    fonts = set()
    for element in layout.iter():
        if element.get("image"):
            images.add(element.get("image"))
        if element.get("up"):
            images.add(element.get("up"))
        if element.get("down"):
            images.add(element.get("down"))
        if element.get("over"):
            images.add(element.get("over"))
        for name in ("up1", "up2", "down1", "down2", "over1", "over2", "sequence",
                     "itemimage", "openimage", "closedimage", "bgimage"):
            if element.get(name):
                images.add(element.get(name))
        if element.get("font"):
            fonts.add(element.get("font"))
    return images, fonts


def bitmap_of(image_id, bitmaps):
    for bitmap_id, bitmap in bitmaps.items():
        if image_id == bitmap_id or image_id in bitmap["subs"]:
            return bitmap_id
    return None


class Recreation:
    def __init__(self, client, out, recreation_dir, theme_dir):
        self.client = client
        self.out = out
        self.recreation_dir = recreation_dir
        self.theme_dir = theme_dir
        self.steps = []
        self.items_since_step = 0
        self.step_every = 4

    def step(self, caption):
        self.steps.append(caption)
        png = self.client.call_png("render_layout", {"zoom": 2})
        if png is not None:
            target = self.out / f"velocity-mcp-step-{len(self.steps):02d}.png"
            target.write_bytes(png)
            print(f"  step {len(self.steps):02d}: {caption}")
        self.items_since_step = 0

    def maybe_step(self, caption):
        self.items_since_step += 1
        if self.items_since_step >= self.step_every:
            self.step(caption)

    def add_assets(self, images, fonts, bitmaps):
        used_bitmaps = []
        for image_id in sorted(images):
            bitmap_id = bitmap_of(image_id, bitmaps)
            if bitmap_id and bitmap_id not in used_bitmaps:
                used_bitmaps.append(bitmap_id)
        for bitmap_id in used_bitmaps:
            source = self.theme_dir / bitmaps[bitmap_id]["file"]
            if not source.exists():
                continue
            shutil.copy2(source, self.recreation_dir / source.name)
            self.client.call("add_resource",
                             {"type": "bitmap", "id": bitmap_id, "file": source.name})
            for sub_id, sub in bitmaps[bitmap_id]["subs"].items():
                self.client.call("add_sub_bitmap", {
                    "bitmap": bitmap_id,
                    "id": sub_id,
                    "x": int(sub.get("x", 0)),
                    "y": int(sub.get("y", 0)),
                    "width": int(sub.get("width", 1)),
                    "height": int(sub.get("height", 1)),
                })
        for font_id in sorted(fonts):
            source = self.theme_dir / "roboto.ttf"
            if not source.exists():
                continue
            target = self.recreation_dir / source.name
            if not target.exists():
                shutil.copy2(source, target)
            self.client.call("add_resource",
                             {"type": "font", "id": font_id, "file": source.name})
            self.client.call("set_resource_property",
                             {"id": font_id, "name": "size", "value": "12"})

    def add_element(self, element, parent_id, default_step):
        tag = element.tag
        item_type = TAG_TO_TYPE.get(tag)
        if item_type is None:
            return parent_id
        if tag == "Slider":
            slider_id = self.add_slider(element, parent_id)
            self.maybe_step(f"{tag} placed")
            return slider_id
        props = {name: value for name, value in element.attrib.items() if name not in ("x", "y")}
        arguments = {
            "type": item_type,
            "x": int(element.get("x", 0)),
            "y": int(element.get("y", 0)),
            "properties": props,
        }
        if parent_id:
            arguments["parent"] = parent_id
        result = self.client.call("add_item", arguments)
        created_id = None
        for part in result.get("content", []):
            text = part.get("text", "")
            match = re.search(r"Added [^\"]*\"([^\"]+)\"", text)
            if match:
                created_id = match.group(1)
        if created_id is None:
            created_id = element.get("id")
        if tag in ("Playlist", "Playtree"):
            self.sync_playlist_slider(created_id, element)
        else:
            for child in list(element):
                self.add_element(child, created_id, default_step)
        self.maybe_step(f"{tag} {element.get('id', '')} placed")
        return created_id

    def sync_playlist_slider(self, playlist_id, element):
        """A playlist is created with its scroll slider; copy the original attributes onto it."""
        slider = element.find("Slider")
        if slider is None:
            return
        description = json.loads(self.client.call_text("layout_tree", {"window": "main", "layout": "main"}))
        created_slider = None
        for node in description.get("items", []):
            if node.get("parentId") == playlist_id and node.get("type") == "Slider":
                created_slider = node.get("id")
                break
        if created_slider is None:
            return
        for name, value in slider.attrib.items():
            if name == "id":
                continue
            self.client.call("set_item_property",
                             {"id": created_slider, "name": name, "value": value})
        background = slider.find("SliderBackground")
        if background is not None:
            self.client.call("add_item", {
                "type": "SliderBackground",
                "parent": created_slider,
                "properties": background.attrib,
            })

    def add_slider(self, element, parent_id):
        props = {name: value for name, value in element.attrib.items() if name not in ("x", "y")}
        arguments = {
            "type": "Slider",
            "x": int(element.get("x", 0)),
            "y": int(element.get("y", 0)),
            "properties": props,
        }
        if parent_id:
            arguments["parent"] = parent_id
        else:
            # A top level slider needs a parent that can hold a slider; the
            # layout root is only entered when the playlist owns one, so this
            # path exists for completeness.
            pass
        result = self.client.call("add_item", arguments)
        slider_id = None
        for part in result.get("content", []):
            match = re.search(r"Added [^\"]*\"([^\"]+)\"", part.get("text", ""))
            if match:
                slider_id = match.group(1)
        background = element.find("SliderBackground")
        if background is not None and slider_id:
            args = {"type": "SliderBackground", "parent": slider_id,
                    "properties": background.attrib}
            self.client.call("add_item", args)
        return slider_id


def build(client, root, bitmaps, fonts, theme_dir, recreation_dir, out):
    recreation = Recreation(client, out, recreation_dir, theme_dir)
    layout = find_layout(root, "player", "main")
    images, used_fonts = referenced_assets(layout)
    recreation.step_every = max(2, len(list(layout.iter())) // 8)

    client.call("open_skin", {"path": str(theme_dir / "theme.xml")})
    recreation.step("original VeLoCity player rendered through MCP")

    client.call("new_skin", {"name": "VeLoCity MCP recreation"})
    client.call("set_theme_property", {"name": "author", "value": "recreated through MCP"})
    client.call("set_layout_property",
                {"window": "main", "layout": "main", "name": "width", "value": "720"})
    client.call("set_layout_property",
                {"window": "main", "layout": "main", "name": "height", "value": "481"})
    recreation.step("new skin with the same 720x481 layout size")

    recreation.add_assets(images, used_fonts, bitmaps)
    recreation.step("every used sprite sheet, sub bitmap and font added")

    for child in list(layout):
        recreation.add_element(child, None, recreation.step_every)
    recreation.step("every layout element recreated from the original attributes")

    validation = client.call_text("validate_skin")
    print("  validation: " + validation.splitlines()[0])
    client.call("save_skin", {"path": str(recreation_dir / "theme.xml")})
    recreation.step("validated and saved: " + (recreation_dir / "theme.xml").name)
    return len(recreation.steps)


def build_gif(out, steps):
    if shutil.which("ffmpeg") is None:
        print("ffmpeg is not installed; the PNG steps are still written")
        return
    listing = out / "velocity-mcp-frames.txt"
    with listing.open("w", encoding="utf-8") as handle:
        for index in range(1, steps + 1):
            frame = out / f"velocity-mcp-step-{index:02d}.png"
            if frame.exists():
                handle.write(f"file '{frame.name}'\nduration 1.6\n")
        handle.write(f"file '{out / f'velocity-mcp-step-{steps:02d}.png'}'\n")
    subprocess.run(
        [
            "ffmpeg", "-y", "-loglevel", "error",
            "-f", "concat", "-safe", "0", "-i", str(listing),
            "-vf", "scale=900:-1:flags=neighbor,fps=30",
            "-loop", "0", str(out / "velocity-mcp-recreation.gif"),
        ],
        check=True,
    )
    listing.unlink()
    print("wrote velocity-mcp-recreation.gif")


def main():
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument("--jar", required=True, type=Path)
    parser.add_argument("--theme", required=True, type=Path, help="VeLoCity theme.xml")
    parser.add_argument("--out", required=True, type=Path)
    args = parser.parse_args()

    theme_dir = args.theme.parent
    recreation_dir = args.out / "velocity-recreation"
    if recreation_dir.exists():
        shutil.rmtree(recreation_dir)
    recreation_dir.mkdir(parents=True, exist_ok=True)

    root, bitmaps, fonts = index_assets(args.theme)
    print(f"VeLoCity: {len(bitmaps)} bitmaps, {len(fonts)} fonts")

    final = args.out / "velocity-mcp-recreated.png"
    if final.exists():
        final.unlink()

    client = McpClient(args.jar)
    try:
        steps = build(client, root, bitmaps, fonts, theme_dir, recreation_dir, args.out)
    finally:
        client.close()

    frames = sorted(args.out.glob("velocity-mcp-step-*.png"))
    if frames:
        shutil.copy2(frames[-1], final)
        print(f"final recreation: {final}")
    build_gif(args.out, steps)


if __name__ == "__main__":
    sys.exit(main())
