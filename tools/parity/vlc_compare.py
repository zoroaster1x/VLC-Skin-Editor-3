#!/usr/bin/env python3
"""Compare our renderer against real VLC for one layout, numerically.

Why this exists:
    "It looks like VLC" is not a test. This tool renders the layout with the
    studio (through MCP, with the player variables forced to VLC's idle state),
    captures the same layout from real VLC skins2 on the virtual display, and
    reports the measured pixel difference. The layout VLC opens is the last one
    of a window, so the theme copy passed to VLC has the layout under test
    moved last; nothing else about it changes.

What it produces, in --out-dir:
    ours.png          studio render at zoom 1
    vlc.png           real VLC TopWindow capture
    side-by-side.png  ours | VLC | red-highlighted diff
    report.json       sizes, shift, mismatch count, percentage, bbox
    report.txt        a one line summary

Usage:
    tools/parity/vlc_compare.py --jar build/libs/vlc-skin-studio.jar \
        --theme /path/to/theme.xml --window player --layout main \
        --out-dir build/parity/vlc/player-main

    # Whole-window text can never match FreeType exactly; compare tiles.
    tools/parity/vlc_compare.py ... --mode blocks --block 8 --max-percent 1.0

    # Add masks for areas with live text when doing an exact control check.
    tools/parity/vlc_compare.py ... --mask 0,0,720,30

Without --window/--layout the tool picks the first window and its last layout,
which is what VLC shows on launch. --keep-defaults disables the idle variable
override for experiments.
"""

import argparse
import json
import os
import subprocess
import sys
import time
import xml.etree.ElementTree as ET

HERE = os.path.dirname(os.path.abspath(__file__))
sys.path.insert(0, HERE)

from gui import Gui, load_state  # noqa: E402
from mcp_client import McpClient  # noqa: E402
from parity_log import StepLog  # noqa: E402
from vlc_shot import (find_skin_window, has_flatpak_vlc, launch, make_layout_default,  # noqa: E402
                      stage_theme, top_window_ids)
import pixel_diff  # noqa: E402

from PIL import Image  # noqa: E402

VLC_IDLE_BOOLEANS = {
    "equalizer.isEnabled": False,
    "vlc.hasVout": False,
    "vlc.hasAudio": False,
    "vlc.isFullscreen": False,
    "vlc.isPlaying": False,
    "vlc.isStopped": True,
    "vlc.isPaused": False,
    "vlc.isSeekable": False,
    "vlc.isMute": False,
    "vlc.isOnTop": False,
    "vlc.canRecord": False,
    "vlc.isRecording": False,
    "playlist.isRandom": False,
    "playlist.isLoop": False,
    "playlist.isRepeat": False,
    "dvd.isActive": False,
}

VLC_IDLE_TEXTS = {
    # VLC's own no-input values: time strings show -:--:--, numeric ones 0,
    # stream name and URI are empty, bitrate and sample rate are empty at 0
    # (var_text.cpp and vars/time.cpp).
    "$B": "",
    "$V": "0",
    "$T": "-:--:--",
    "$t": "-:--:--",
    "$L": "-:--:--",
    "$l": "-:--:--",
    "$D": "-:--:--",
    "$d": "-:--:--",
    "$H": "",
    "$N": "",
    "$F": "",
    "$S": "",
    "$R": "1",
}


def first_window_layout(theme):
    tree = ET.parse(theme)
    windows = tree.getroot().findall("Window")
    if not windows:
        raise SystemExit(f"no Window in {theme}")
    window = windows[0]
    layouts = window.findall("Layout")
    if not layouts:
        raise SystemExit(f"no Layout in window {window.get('id')}")
    return window.get("id"), layouts[-1].get("id")


def render_ours(jar, theme, window, layout, path, idle, log=None):
    with McpClient(jar, png_dir=os.path.dirname(path)) as client:
        text = client.open(theme)
        if "Error" in text:
            raise SystemExit(f"open failed: {text}")
        if log:
            log.step("skin opened in the MCP process")
        if idle:
            client.call("set_variables", {
                "booleans": VLC_IDLE_BOOLEANS,
                "texts": VLC_IDLE_TEXTS,
                "sliderValue": 0,
            })
            if log:
                log.step("player variables forced to the VLC idle state")
        data = client.call_png("render_layout",
                               {"window": window, "layout": layout, "zoom": 1})
        if data is None:
            raise SystemExit("render_layout returned no image")
        os.makedirs(os.path.dirname(os.path.abspath(path)), exist_ok=True)
        with open(path, "wb") as handle:
            handle.write(data)
        if log:
            log.step(f"studio rendered {len(data)} bytes to {path}")


def capture_vlc(gui, state, theme, layout, path, timeout, settle, log=None,
                expected_size=None, window=None):
    adjusted = make_layout_default(theme, layout, window)
    use_flatpak = has_flatpak_vlc()
    staged = stage_theme(adjusted, use_flatpak)
    if log:
        log.step(f"theme staged for VLC at {staged}")
    if use_flatpak:
        command = ["flatpak", "run", "org.videolan.VLC", "--no-one-instance",
                   "-I", "skins2", f"--skins2-last={staged}"]
    else:
        command = ["vlc", "--no-one-instance", "-I", "skins2", f"--skins2-last={staged}"]
    env = dict(os.environ)
    env["DISPLAY"] = state["DISPLAY"]
    env.pop("WAYLAND_DISPLAY", None)
    env["QT_QPA_PLATFORM"] = "xcb"
    before = top_window_ids(gui)
    process = launch(command, env)
    try:
        deadline = time.monotonic() + timeout
        window_id = None
        while time.monotonic() < deadline:
            window_id = find_skin_window(gui, before, expected_size)
            if window_id is not None:
                break
            if process.poll() is not None:
                raise SystemExit(f"VLC exited early with code {process.returncode}")
            time.sleep(0.5)
        if window_id is None:
            raise SystemExit(f"no VLC window after {timeout}s")
        if log:
            _, _, width, height = gui.geometry(window_id)
            log.step(f"VLC window {window_id} up ({width}x{height}), settling {settle}s")
        time.sleep(settle)
        captured = False
        for _ in range(6):
            current = find_skin_window(gui, before, expected_size) or window_id
            try:
                result = subprocess.run(gui.import_cmd + ["-window", str(current), path],
                                        capture_output=True, text=True, timeout=20,
                                        env=gui.env())
            except subprocess.TimeoutExpired:
                result = None
            if result is not None and result.returncode == 0:
                captured = True
                break
            time.sleep(1.0)
        if not captured:
            raise SystemExit("capture failed")
        if log:
            log.step(f"captured to {path}")
    finally:
        process.terminate()
        try:
            process.wait(timeout=10)
        except subprocess.TimeoutExpired:
            process.kill()
        if use_flatpak:
            subprocess.run(["flatpak", "kill", "org.videolan.VLC"],
                           stdout=subprocess.DEVNULL, stderr=subprocess.DEVNULL, timeout=15)


def side_by_side(ours_path, vlc_path, diff_path, output):
    ours = Image.open(ours_path).convert("RGB")
    vlc = Image.open(vlc_path).convert("RGB")
    width = max(ours.width, vlc.width)
    height = max(ours.height, vlc.height)
    canvas = Image.new("RGB", (width * 3 + 20, height), (24, 24, 28))
    canvas.paste(ours, (0, 0))
    canvas.paste(vlc, (width + 10, 0))
    diff = Image.open(diff_path).convert("RGB") if diff_path and os.path.exists(diff_path) else None
    if diff is not None:
        canvas.paste(diff, ((width + 10) * 2, 0))
    canvas.save(output)


def main():
    parser = argparse.ArgumentParser(description=__doc__,
                                     formatter_class=argparse.RawDescriptionHelpFormatter)
    parser.add_argument("--jar", required=True)
    parser.add_argument("--theme", required=True)
    parser.add_argument("--window")
    parser.add_argument("--layout")
    parser.add_argument("--out-dir", required=True)
    parser.add_argument("--timeout", type=float, default=40.0)
    parser.add_argument("--settle", type=float, default=6.0)
    parser.add_argument("--mode", choices=["exact", "blocks"], default="exact")
    parser.add_argument("--block", type=int, default=8)
    parser.add_argument("--tolerance", type=int, default=2)
    parser.add_argument("--max-percent", type=float, default=0.0)
    parser.add_argument("--align", type=int, default=2)
    parser.add_argument("--mask", action="append", default=[])
    parser.add_argument("--keep-defaults", action="store_true",
                        help="do not force VLC idle player variables")
    args = parser.parse_args()

    os.makedirs(args.out_dir, exist_ok=True)
    log = StepLog("vlc-compare")
    window, layout = args.window, args.layout
    if not window or not layout:
        default_window, default_layout = first_window_layout(args.theme)
        window = window or default_window
        layout = layout or default_layout
    log.step(f"layout {window}/{layout}, outputs in {args.out_dir}")

    ours_path = os.path.join(args.out_dir, "ours.png")
    vlc_path = os.path.join(args.out_dir, "vlc.png")
    diff_path = os.path.join(args.out_dir, "diff.png")

    render_ours(args.jar, args.theme, window, layout, ours_path,
                idle=not args.keep_defaults, log=log)
    log.step("studio render done")
    state = load_state()
    gui = Gui(state)
    with Image.open(ours_path) as rendered:
        expected_size = rendered.size
    capture_vlc(gui, state, args.theme, layout, vlc_path, args.timeout, args.settle,
                log=log, expected_size=expected_size, window=window)
    log.step("VLC capture done")

    masks = []
    for spec in args.mask:
        masks.append([int(part) for part in spec.replace(" ", "").split(",")])

    a = pixel_diff.apply_masks(pixel_diff.load(ours_path), masks)
    b = pixel_diff.apply_masks(pixel_diff.load(vlc_path), masks)
    size_mismatch = (a.width, a.height) != (b.width, b.height)
    a, b = pixel_diff.common_size(a, b)
    shift = pixel_diff.align_images(a, b, args.align)
    dx, dy = shift[0], shift[1]
    if dx or dy:
        a = a.crop((max(0, dx), max(0, dy), a.width - max(0, -dx), a.height - max(0, -dy)))
        b = b.crop((max(0, -dx), max(0, -dy), b.width - max(0, dx), b.height - max(0, dy)))
    if args.mode == "exact":
        result = pixel_diff.exact_diff(a, b, args.tolerance)
    else:
        result = pixel_diff.block_diff(a, b, args.block, args.tolerance)
    mask = result.pop("mask")
    pixel_diff.write_diff_image(a, mask, diff_path)

    side_by_side(ours_path, vlc_path, diff_path, os.path.join(args.out_dir, "side-by-side.png"))
    result.update({
        "theme": os.path.abspath(args.theme),
        "window": window,
        "layout": layout,
        "shift": {"dx": dx, "dy": dy},
        "size_mismatch": size_mismatch,
        "ours": ours_path,
        "vlc": vlc_path,
        "diff": diff_path,
        "side_by_side": os.path.join(args.out_dir, "side-by-side.png"),
    })
    with open(os.path.join(args.out_dir, "report.json"), "w", encoding="utf-8") as handle:
        json.dump(result, handle, indent=2)
    summary = (f"{window}/{layout}: {result['percent']:.3f}% differ "
               f"({result['mismatched']} of {result['pixels']} {result['mode']}, "
               f"max delta {result['max_channel_delta']}, shift {dx},{dy})")
    log.finish(summary)
    with open(os.path.join(args.out_dir, "report.txt"), "w", encoding="utf-8") as handle:
        handle.write(summary + "\n")
    print(summary)
    if size_mismatch:
        print(f"note: sizes differ, compared the common area", file=sys.stderr)
    return 1 if result["percent"] > args.max_percent else 0


if __name__ == "__main__":
    sys.exit(main())
