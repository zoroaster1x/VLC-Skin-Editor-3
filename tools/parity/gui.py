#!/usr/bin/env python3
"""Drive GUI windows on the parity display, by title and coordinates.

This is the equivalent of a person clicking: every action goes through
xdotool, and every verification is a screenshot of the window (not of the
whole screen), so results are stable no matter what else is open.

It reads the state file written by display.sh, so it works whether xdotool
runs natively or inside the podman container.

Examples:
    tools/parity/gui.py list
    tools/parity/gui.py shot "VLC Skin Studio" build/parity/studio.png
    tools/parity/gui.py click "VLC Skin Studio" 590 100 --shot build/parity/after.png
    tools/parity/gui.py type "VLC Skin Studio" "/tmp/theme.xml"
    tools/parity/gui.py key "VLC Skin Studio" ctrl+s
    tools/parity/gui.py drag "VLC Skin Studio" 700 400 760 460
    tools/parity/gui.py activate "VLC Skin Editor"
    tools/parity/gui.py wait "VLC Skin Editor" --timeout 10
    tools/parity/gui.py geometry "VLC Skin Studio"
"""

import argparse
import os
import shlex
import subprocess
import sys
import time

DEFAULT_STATE = os.path.join(
    os.environ.get("PARITY_STATE_DIR", "/tmp/vlc-skin-parity"), "display.env")


def load_state(path=DEFAULT_STATE):
    if not os.path.exists(path):
        raise SystemExit(f"no display state at {path}; run tools/parity/display.sh start")
    state = {}
    with open(path, encoding="utf-8") as handle:
        for line in handle:
            line = line.strip()
            if not line or line.startswith("#") or "=" not in line:
                continue
            key, value = line.split("=", 1)
            state[key] = value.strip("'\"")
    return state


class Gui:
    def __init__(self, state):
        self.display = state["DISPLAY"]
        self.xdotool = shlex.split(state["PARITY_XDOTOOL"])
        self.wmctrl = shlex.split(state["PARITY_WMCTRL"])
        self.import_cmd = shlex.split(state["PARITY_IMPORT"])

    def env(self):
        """The environment every X command must run with.

        The harness may be started from a Wayland session with its own DISPLAY,
        so xdotool and import would otherwise talk to the operator's desktop
        (or fall back to import's interactive window picker and hang forever).
        """
        environment = dict(os.environ)
        environment["DISPLAY"] = self.display
        environment.pop("WAYLAND_DISPLAY", None)
        return environment

    def _run(self, command, check=True, timeout=30):
        try:
            result = subprocess.run(command, capture_output=True, text=True,
                                    timeout=timeout, env=self.env())
        except subprocess.TimeoutExpired:
            raise SystemExit(f"{' '.join(command)} timed out after {timeout}s")
        if check and result.returncode != 0:
            raise SystemExit(f"{' '.join(command)} failed:\n{result.stderr.strip()}")
        return result.stdout

    def xdo(self, *args, check=True):
        return self._run(self.xdotool + list(args), check=check)

    def windows(self):
        """[(id: int, title: str)] for every managed window."""
        listing = self._run(self.wmctrl + ["-l"], check=False)
        windows = []
        for line in listing.splitlines():
            parts = line.split(None, 3)
            if len(parts) >= 4:
                windows.append((int(parts[0], 16), parts[3]))
        return windows

    def find(self, title):
        """The first window whose title contains the string."""
        for window_id, window_title in self.windows():
            if title.lower() in window_title.lower():
                return window_id, window_title
        return None, None

    def wait_for(self, title, timeout=15.0):
        deadline = time.monotonic() + timeout
        while time.monotonic() < deadline:
            window_id, window_title = self.find(title)
            if window_id is not None:
                return window_id, window_title
            time.sleep(0.25)
        raise SystemExit(f"no window titled like {title!r} after {timeout}s")

    def geometry(self, window_id):
        """(x, y, width, height) of a window from xdotool."""
        output = self.xdo("getwindowgeometry", "--shell", str(window_id))
        values = {}
        for line in output.splitlines():
            if "=" in line:
                key, value = line.split("=", 1)
                values[key] = int(value)
        return values["X"], values["Y"], values["WIDTH"], values["HEIGHT"]

    def activate(self, title):
        window_id, _ = self.wait_for(title, timeout=5)
        self.xdo("windowactivate", "--sync", str(window_id))
        time.sleep(0.3)
        return window_id

    def click(self, title, x, y, button=1, relative=True, repeat=1):
        window_id = self.activate(title)
        if relative:
            wx, wy, _, _ = self.geometry(window_id)
            x, y = wx + x, wy + y
        self.xdo("mousemove", str(x), str(y))
        for _ in range(repeat):
            self.xdo("click", str(button))
            time.sleep(0.08)
        return window_id

    def double_click(self, title, x, y, relative=True):
        return self.click(title, x, y, relative=relative, repeat=2)

    def type_text(self, title, text):
        self.activate(title)
        self.xdo("type", "--delay", "15", text)

    def key(self, title, *keys):
        self.activate(title)
        for key in keys:
            self.xdo("key", key)
            time.sleep(0.1)

    def drag(self, title, x1, y1, x2, y2, relative=True):
        window_id = self.activate(title)
        if relative:
            wx, wy, _, _ = self.geometry(window_id)
            x1, y1, x2, y2 = wx + x1, wy + y1, wx + x2, wy + y2
        self.xdo("mousemove", str(x1), str(y1))
        self.xdo("mousedown", "1")
        self.xdo("mousemove", "--sync", str(x2), str(y2))
        self.xdo("mouseup", "1")

    def move(self, title, x, y, width=None, height=None, relative=False):
        window_id, _ = self.wait_for(title, timeout=5)
        geometry = f"0,{x},{y}" + (f",{width},{height}" if width else ",,")
        self._run(self.wmctrl + ["-i", "-r", hex(window_id), "-e", geometry])

    def shot(self, title, output, root=False):
        if root:
            command = self.import_cmd + ["-window", "root", output]
        else:
            window_id, _ = self.wait_for(title, timeout=5)
            command = self.import_cmd + ["-window", str(window_id), output]
        os.makedirs(os.path.dirname(os.path.abspath(output)), exist_ok=True)
        self._run(command)
        print(output)
    def title(self):
        return self.xdo("getactivewindow", "getwindowname", check=False).strip()


def main():
    parser = argparse.ArgumentParser(description=__doc__,
                                     formatter_class=argparse.RawDescriptionHelpFormatter)
    parser.add_argument("--state", default=DEFAULT_STATE, help="display state file")
    sub = parser.add_subparsers(dest="command", required=True)

    sub.add_parser("list")
    sub.add_parser("title")
    sub.add_parser("windows")

    p = sub.add_parser("shot")
    p.add_argument("title")
    p.add_argument("output")
    p.add_argument("--root", action="store_true", help="capture the whole screen")

    p = sub.add_parser("click")
    p.add_argument("title")
    p.add_argument("x", type=int)
    p.add_argument("y", type=int)
    p.add_argument("--button", type=int, default=1)
    p.add_argument("--screen", action="store_true", help="absolute screen coordinates")
    p.add_argument("--repeat", type=int, default=1)
    p.add_argument("--shot", help="screenshot after the click")
    p.add_argument("--sleep", type=float, default=0.5)

    p = sub.add_parser("double-click")
    p.add_argument("title")
    p.add_argument("x", type=int)
    p.add_argument("y", type=int)
    p.add_argument("--shot")
    p.add_argument("--sleep", type=float, default=0.5)

    p = sub.add_parser("type")
    p.add_argument("title")
    p.add_argument("text")

    p = sub.add_parser("key")
    p.add_argument("title")
    p.add_argument("keys", nargs="+")

    p = sub.add_parser("drag")
    p.add_argument("title")
    p.add_argument("x1", type=int)
    p.add_argument("y1", type=int)
    p.add_argument("x2", type=int)
    p.add_argument("y2", type=int)
    p.add_argument("--screen", action="store_true")
    p.add_argument("--shot")

    p = sub.add_parser("activate")
    p.add_argument("title")

    p = sub.add_parser("wait")
    p.add_argument("title")
    p.add_argument("--timeout", type=float, default=15.0)

    p = sub.add_parser("geometry")
    p.add_argument("title")

    p = sub.add_parser("move")
    p.add_argument("title")
    p.add_argument("x", type=int)
    p.add_argument("y", type=int)
    p.add_argument("--width", type=int)
    p.add_argument("--height", type=int)

    args = parser.parse_args()
    gui = Gui(load_state(args.state))

    if args.command == "list":
        for window_id, title in gui.windows():
            print(f"0x{window_id:08x}  {title}")
    elif args.command == "windows":
        for window_id, title in gui.windows():
            x, y, width, height = gui.geometry(window_id)
            print(f"0x{window_id:08x}  {x},{y} {width}x{height}  {title}")
    elif args.command == "title":
        print(gui.title())
    elif args.command == "shot":
        gui.shot(args.title, args.output, root=args.root)
    elif args.command == "click":
        gui.click(args.title, args.x, args.y, button=args.button,
                  relative=not args.screen, repeat=args.repeat)
        if args.shot:
            time.sleep(args.sleep)
            gui.shot(args.title, args.shot)
    elif args.command == "double-click":
        gui.double_click(args.title, args.x, args.y)
        if args.shot:
            time.sleep(args.sleep)
            gui.shot(args.title, args.shot)
    elif args.command == "type":
        gui.type_text(args.title, args.text)
    elif args.command == "key":
        gui.key(args.title, *args.keys)
    elif args.command == "drag":
        gui.drag(args.title, args.x1, args.y1, args.x2, args.y2,
                 relative=not args.screen)
        if args.shot:
            gui.shot(args.title, args.shot)
    elif args.command == "activate":
        gui.activate(args.title)
    elif args.command == "wait":
        window_id, title = gui.wait_for(args.title, timeout=args.timeout)
        print(f"0x{window_id:08x}  {title}")
    elif args.command == "geometry":
        window_id, _ = gui.wait_for(args.title, timeout=5)
        print(gui.geometry(window_id))
    elif args.command == "move":
        gui.move(args.title, args.x, args.y, args.width, args.height)


if __name__ == "__main__":
    main()
