#!/usr/bin/env python3
"""Download the official VLC skins gallery as a local test corpus.

The gallery page lists every theme in a JavaScript call per entry
(`showSkinBox(...)`), with the archive name. This script parses that page and
downloads each archive into one folder, writing a manifest next to it.

Usage:
    tools/parity/gallery_corpus.py --out build/parity/corpus
    tools/parity/gallery_corpus.py --out corpus --limit 10
    tools/parity/gallery_corpus.py --out corpus --names "VeLoCity"
"""

import argparse
import concurrent.futures
import html
import json
import os
import re
import sys
import urllib.parse
import urllib.request

PAGE_URL = "https://images.videolan.org/vlc/skins.html"
DOWNLOAD_URL = "https://www.videolan.org/vlc/skins2/"

TOKEN = r"(?:[^'\\]|\\.)*"
ENTRY = re.compile(
    r"showSkinBox\((?P<id>\d+),'(?P<name>" + TOKEN + r")','(?P<author>" + TOKEN + r")',"
    r"'(?P<date>" + TOKEN + r")','?(?P<size>\d+)'?,'(?P<filename>" + TOKEN + r")',"
    r"'(?P<size_str>" + TOKEN + r")',[-\d.]+,[-\d.]+,'(?P<preview>" + TOKEN + r")',"
    r"'" + TOKEN + r"','(?P<version>" + TOKEN + r")'\)"
)


def unescape(value):
    return html.unescape(value.replace("\\'", "'"))


def parse_page(text):
    themes = []
    for match in ENTRY.finditer(text):
        themes.append({
            "id": match.group("id"),
            "name": unescape(match.group("name")),
            "author": unescape(match.group("author")),
            "date": unescape(match.group("date")),
            "version": match.group("version"),
            "filename": unescape(match.group("filename")),
            "size": int(match.group("size")),
        })
    return themes


def download(theme, out_dir, base_url):
    target = os.path.join(out_dir, theme["filename"])
    if os.path.exists(target) and os.path.getsize(target) == theme["size"]:
        return theme, "cached"
    url = base_url + urllib.parse.quote(theme["filename"])
    try:
        request = urllib.request.Request(url, headers={"User-Agent": "vlc-skin-studio-parity"})
        with urllib.request.urlopen(request, timeout=60) as response, open(target + ".part", "wb") as handle:
            while True:
                chunk = response.read(1 << 16)
                if not chunk:
                    break
                handle.write(chunk)
        os.replace(target + ".part", target)
        return theme, "ok"
    except Exception as exc:  # noqa: BLE001 - one bad theme must not stop the rest
        return theme, f"failed: {exc}"


def main():
    parser = argparse.ArgumentParser(description=__doc__,
                                     formatter_class=argparse.RawDescriptionHelpFormatter)
    parser.add_argument("--out", required=True, help="folder to download into")
    parser.add_argument("--page-url", default=PAGE_URL)
    parser.add_argument("--base-url", default=DOWNLOAD_URL)
    parser.add_argument("--jobs", type=int, default=8)
    parser.add_argument("--limit", type=int, help="only download the first N themes")
    parser.add_argument("--names", help="only themes whose name contains this (case insensitive)")
    args = parser.parse_args()

    os.makedirs(args.out, exist_ok=True)
    request = urllib.request.Request(args.page_url, headers={"User-Agent": "vlc-skin-studio-parity"})
    with urllib.request.urlopen(request, timeout=60) as response:
        page = response.read().decode("utf-8", "replace")

    themes = parse_page(page)
    if args.names:
        needle = args.names.lower()
        themes = [theme for theme in themes if needle in theme["name"].lower()]
    if args.limit:
        themes = themes[:args.limit]
    if not themes:
        print("no themes matched", file=sys.stderr)
        return 1
    print(f"gallery lists {len(themes)} matching themes", file=sys.stderr)

    failures = 0
    with concurrent.futures.ThreadPoolExecutor(max_workers=args.jobs) as pool:
        for theme, status in pool.map(lambda t: download(t, args.out, args.base_url), themes):
            theme["download"] = status
            if status.startswith("failed"):
                failures += 1
                print(f"  {status}: {theme['name']} ({theme['filename']})", file=sys.stderr)

    manifest = os.path.join(args.out, "manifest.json")
    with open(manifest, "w", encoding="utf-8") as handle:
        json.dump(themes, handle, indent=2, ensure_ascii=False)
    present = sum(1 for theme in themes
                  if os.path.exists(os.path.join(args.out, theme["filename"])))
    print(f"{present}/{len(themes)} archives present, manifest at {manifest}")
    return 1 if failures else 0


if __name__ == "__main__":
    sys.exit(main())
