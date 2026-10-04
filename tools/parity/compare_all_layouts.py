#!/usr/bin/env python3
"""Compare every layout of a theme against VLC and print a one line summary.

Usage:
    tools/parity/compare_all_layouts.py --jar build/libs/vlc-skin-studio.jar \
        --theme path/to/theme.xml --out build/parity/compare
"""

import argparse
import os
import subprocess
import sys
import xml.etree.ElementTree as ET

HERE = os.path.dirname(os.path.abspath(__file__))


def layouts(theme):
    tree = ET.parse(theme)
    for window in tree.getroot().findall("Window"):
        for layout in window.findall("Layout"):
            yield window.get("id"), layout.get("id")


def main():
    parser = argparse.ArgumentParser(description=__doc__,
                                     formatter_class=argparse.RawDescriptionHelpFormatter)
    parser.add_argument("--jar", required=True)
    parser.add_argument("--theme", required=True)
    parser.add_argument("--out", required=True)
    parser.add_argument("--mode", default="blocks")
    parser.add_argument("--block", type=int, default=8)
    parser.add_argument("--tolerance", type=int, default=6)
    args = parser.parse_args()

    failures = 0
    for window, layout in layouts(args.theme):
        out_dir = os.path.join(args.out, f"{window}-{layout}")
        command = [
            sys.executable, os.path.join(HERE, "vlc_compare.py"),
            "--jar", args.jar, "--theme", args.theme,
            "--window", window, "--layout", layout,
            "--out-dir", out_dir, "--mode", args.mode,
            "--block", str(args.block), "--tolerance", str(args.tolerance),
            "--max-percent", "100",
        ]
        result = subprocess.run(command, capture_output=True, text=True)
        summary = ""
        report = os.path.join(out_dir, "report.txt")
        if os.path.exists(report):
            summary = open(report, encoding="utf-8").read().strip()
        if result.returncode != 0 or not summary:
            failures += 1
            print(f"{window}/{layout}: FAILED ({result.stderr.strip()[:200]})")
            continue
        print(summary)
        region = subprocess.run(
            [sys.executable, os.path.join(HERE, "region_diff.py"),
             os.path.join(out_dir, "ours.png"), os.path.join(out_dir, "vlc.png"),
             "--json", os.path.join(out_dir, "regions.json"),
             "--max-shift", "1", "--max-size", "2"],
            capture_output=True, text=True)
        tail = [line for line in region.stdout.splitlines() if "region problem" in line]
        print("    " + (tail[0] if tail else "region check failed"))
    return 1 if failures else 0


if __name__ == "__main__":
    sys.exit(main())
