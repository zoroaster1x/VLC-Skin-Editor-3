#!/usr/bin/env python3
"""Render a theme with real VLC skins2 on the parity display and screenshot it.

VLC is the gold standard for what a theme should look like, so this script
launches VLC with the skins2 interface, waits for its window, captures it and
then closes VLC again. It works with the Flatpak (detected first) and with a
native `vlc` on PATH.

The theme folder is copied into the VLC Flatpak data directory before launch,
because the sandbox may not be able to read the work tree; the copied path is
used verbatim so VLC can find the assets next to theme.xml.

Usage:
    tools/parity/vlc_shot.py --theme build/parity/work/velocity/theme.xml \
        --out build/parity/shots/velocity-vlc.png
    tools/parity/vlc_shot.py --theme theme.xml --out shot.png \
        --window "VeLoCity" --timeout 25 --native
"""

import argparse
import os
import shutil
import subprocess
import sys
import time

sys.path.insert(0, os.path.dirname(os.path.abspath(__file__)))
from gui import Gui, load_state  # noqa: E402

FLATPAK_ID = "org.videolan.VLC"
FLATPAK_DATA = os.path.join(
    os.environ.get("HOME", ""),
    ".var/app", FLATPAK_ID, "data", "parity")


def has_flatpak_vlc():
    try:
        output = subprocess.run(["flatpak", "list", "--columns=application"],
                                capture_output=True, text=True, timeout=30).stdout
        return FLATPAK_ID in output.split()
    except (OSError, subprocess.TimeoutExpired):
        return False


def stage_theme(theme, use_flatpak):
    """Return the path VLC should load, staging the folder when needed."""
    theme = os.path.abspath(theme)
    folder = os.path.dirname(theme)
    if not use_flatpak:
        return theme
    target = os.path.join(FLATPAK_DATA, os.path.basename(folder))
    if os.path.exists(target):
        shutil.rmtree(target)
    shutil.copytree(folder, target)
    return os.path.join(target, os.path.basename(theme))


def make_layout_default(theme, layout_id, window_id=None):
    """Copy the theme folder with the chosen layout last and its window shown.

    VLC opens the last layout of a window, and secondary windows (small mode,
    fullscreen controller, about) start hidden behind a `visible` expression.
    Reordering and forcing visibility produces a theme copy that VLC opens
    straight into the layout under test.
    """
    import tempfile
    import xml.etree.ElementTree as ET

    folder = os.path.dirname(os.path.abspath(theme))
    work = tempfile.mkdtemp(prefix="vlc-layout-")
    target = os.path.join(work, os.path.basename(folder))
    shutil.copytree(folder, target)
    theme_file = os.path.join(target, os.path.basename(theme))
    tree = ET.parse(theme_file)
    found = False
    for window in tree.getroot().findall("Window"):
        is_target_window = window_id is None or window.get("id") == window_id
        layouts = window.findall("Layout")
        chosen = next((layout for layout in layouts
                       if layout.get("id") == layout_id and is_target_window), None)
        if chosen is not None:
            found = True
            window.remove(chosen)
            window.append(chosen)
    if window_id is not None:
        for window in tree.getroot().findall("Window"):
            if window.get("id") == window_id:
                window.set("visible", "true")
            else:
                window.set("visible", "false")
    if not found:
        raise SystemExit(f"layout {layout_id!r} not found in {theme}")
    tree.write(theme_file, encoding="unicode")
    with open(theme_file, "a", encoding="utf-8") as handle:
        handle.write("\n")
    return theme_file


def launch(command, env):
    return subprocess.Popen(command, env=env, stdout=subprocess.DEVNULL,
                            stderr=subprocess.DEVNULL)


def top_window_ids(gui):
    """Every window titled like a skins2 top window, as ints."""
    output = gui.xdo("search", "--name", "TopWindow", check=False)
    ids = set()
    for line in output.split():
        try:
            ids.add(int(line))
        except ValueError:
            continue
    return ids


def find_skin_window(gui, exclude=(), expected_size=None):
    """The skins2 top window: class Vlc, title 'VLC (TopWindow)', unmanaged.

    wmctrl does not list it because the WM does not manage it, so search with
    xdotool. `exclude` holds ids that existed before the launch, so a stale id
    from a previous instance cannot be picked. `expected_size` is (width,
    height) of our own render: with several windows open, the captured one must
    be the window for the layout under test, not the largest.
    """
    best = None
    for window_id in top_window_ids(gui) - set(exclude):
        try:
            _, _, width, height = gui.geometry(window_id)
        except (ValueError, KeyError):
            continue
        if width <= 40 or height <= 30:
            continue
        if expected_size is not None:
            score = (abs(width - expected_size[0]) + abs(height - expected_size[1]),
                     -width * height)
        else:
            score = (0, -width * height)
        if best is None or score < best[0]:
            best = (score, window_id)
    return best[1] if best else None


def main():
    parser = argparse.ArgumentParser(description=__doc__,
                                     formatter_class=argparse.RawDescriptionHelpFormatter)
    parser.add_argument("--theme", required=True, help="theme.xml or .vlt to load")
    parser.add_argument("--out", required=True, help="screenshot output path")
    parser.add_argument("--layout", help="layout id to open (moved last, VLC opens the last one)")
    parser.add_argument("--window", default="VLC", help="title substring to capture")
    parser.add_argument("--timeout", type=float, default=30.0)
    parser.add_argument("--settle", type=float, default=6.0,
                        help="seconds to let the skin paint before the shot")
    parser.add_argument("--native", action="store_true", help="use vlc from PATH")
    parser.add_argument("--keep-running", action="store_true")
    parser.add_argument("extra", nargs="*", help="extra VLC arguments")
    args = parser.parse_args()

    state = load_state()
    gui = Gui(state)
    use_flatpak = not args.native and has_flatpak_vlc()
    if not use_flatpak and not args.native:
        print("no Flatpak VLC found, falling back to vlc on PATH", file=sys.stderr)

    theme = stage_theme(args.theme, use_flatpak)
    if use_flatpak:
        command = ["flatpak", "run", FLATPAK_ID, "--no-one-instance",
                   "-I", "skins2", f"--skins2-last={theme}"]
    else:
        command = ["vlc", "--no-one-instance", "-I", "skins2", f"--skins2-last={theme}"]
    command += args.extra

    env = dict(os.environ)
    env["DISPLAY"] = state["DISPLAY"]
    env.pop("WAYLAND_DISPLAY", None)
    env["QT_QPA_PLATFORM"] = "xcb"
    print("launching: " + " ".join(command), file=sys.stderr)
    before = top_window_ids(gui)
    process = launch(command, env)
    try:
        window_id = None
        deadline = time.monotonic() + args.timeout
        while time.monotonic() < deadline:
            window_id = find_skin_window(gui, before)
            if window_id is not None:
                break
            if process.poll() is not None:
                raise SystemExit(f"VLC exited early with code {process.returncode}")
            time.sleep(0.5)
        if window_id is None:
            raise SystemExit(f"no VLC skin window after {args.timeout}s")
        time.sleep(args.settle)
        os.makedirs(os.path.dirname(os.path.abspath(args.out)), exist_ok=True)
        # VLC can recreate its top window while the skin settles; re-find it
        # right before the capture and retry, or import races a dying window.
        captured = False
        for attempt in range(6):
            current = find_skin_window(gui, before) or window_id
            try:
                result = subprocess.run(gui.import_cmd + ["-window", str(current), args.out],
                                        capture_output=True, text=True, timeout=20,
                                        env=gui.env())
            except subprocess.TimeoutExpired:
                result = None
            if result is not None and result.returncode == 0:
                captured = True
                break
            time.sleep(1.0)
        if not captured:
            raise SystemExit("could not capture the VLC window")
        print(args.out)
    finally:
        if not args.keep_running:
            process.terminate()
            try:
                process.wait(timeout=10)
            except subprocess.TimeoutExpired:
                process.kill()
            subprocess.run(["flatpak", "kill", FLATPAK_ID],
                           stdout=subprocess.DEVNULL, stderr=subprocess.DEVNULL, timeout=15)


if __name__ == "__main__":
    main()
