#!/usr/bin/env python3
"""Conformance sweep over every skin from the official VLC skins gallery.

Downloads the daily pack from https://images.videolan.org/vlc/skins.html,
then for every .vlt theme it runs the VLC Skin Studio CLI end to end:

  vlt import  -> unpack the theme and its assets
  inspect     -> parse the document info as JSON
  validate    -> collect format issues
  render      -> render the first layout to a PNG

Every failure is recorded with the exact command output; the sweep never stops
on one bad skin. The result is written as docs/skin-gallery-report.md plus a
machine readable docs/skin-gallery-report.json.

Usage:
    python3 tools/gallery-conformance.py --jar build/libs/vlc-skin-studio.jar \
        [--pack-url URL] [--work /tmp/opencode/skin-gallery] [--jobs 4] \
        [--limit 20]
"""

import argparse
import concurrent.futures
import json
import os
import re
import shutil
import subprocess
import sys
import time
import urllib.request
import zipfile
from pathlib import Path

PAGE = "https://images.videolan.org/vlc/skins.html"
DEFAULT_PACK = "https://www.videolan.org/vlc/skins2/vlc-skins.zip"


def download(url, target):
    if target.exists() and target.stat().st_size > 0:
        return target
    target.parent.mkdir(parents=True, exist_ok=True)
    print(f"downloading {url}")
    with urllib.request.urlopen(url, timeout=180) as response, target.open("wb") as handle:
        shutil.copyfileobj(response, handle)
    return target


def run(command, cwd, timeout):
    started = time.monotonic()
    try:
        result = subprocess.run(command, cwd=cwd, capture_output=True, text=True, timeout=timeout)
        return {
            "code": result.returncode,
            "stdout": result.stdout,
            "stderr": result.stderr,
            "ms": int((time.monotonic() - started) * 1000),
        }
    except subprocess.TimeoutExpired:
        return {"code": -1, "stdout": "", "stderr": f"timeout after {timeout}s", "ms": timeout * 1000}


def check_skin(jar, vlt, work_root):
    name = vlt.stem
    work = work_root / "work" / name
    if work.exists():
        shutil.rmtree(work)
    work.mkdir(parents=True, exist_ok=True)
    record = {"skin": name, "vlt": vlt.name, "size": vlt.stat().st_size, "source": "gallery pack"}
    try:
        imported = run(["java", "-jar", str(jar), "vlt", "import", str(vlt), str(work)], work_root, 180)
        record["import_ms"] = imported["ms"]
        theme = work / "theme.xml"
        if imported["code"] != 0 or not theme.exists():
            record["status"] = "import failed"
            record["error"] = (imported["stderr"] or imported["stdout"]).strip()[:400]
            return record
        return check_theme(jar, theme, name, work, record)
    except Exception as exc:  # one bad skin must not stop the sweep
        record["status"] = "exception"
        record["error"] = str(exc)[:400]
        return record


def check_official(jar, theme_source, work_root):
    """Check a theme that ships with VLC itself, no VLT import needed."""
    source_dir = theme_source.parent
    name = "vlc-" + source_dir.name if theme_source.name == "theme.xml" else "vlc-" + theme_source.stem
    work = work_root / "work" / name
    if work.exists():
        shutil.rmtree(work)
    shutil.copytree(source_dir, work)
    record = {"skin": name, "vlt": theme_source.name, "size": theme_source.stat().st_size,
              "source": "VLC built in"}
    try:
        return check_theme(jar, work / theme_source.name, name, work, record)
    except Exception as exc:
        record["status"] = "exception"
        record["error"] = str(exc)[:400]
        return record


def check_theme(jar, theme, name, work, record):
    record["assets"] = len(list(work.iterdir()))
    inspected = run(["java", "-jar", str(jar), "inspect", str(theme)], work, 60)
    if inspected["code"] != 0:
        record["status"] = "inspect failed"
        record["error"] = (inspected["stderr"] or inspected["stdout"]).strip()[:400]
        return record
    info = json.loads(inspected["stdout"])
    record["windows"] = info.get("windows", 0)
    record["layouts"] = info.get("layouts", 0)
    record["items"] = info.get("items", 0)

    validated = run(["java", "-jar", str(jar), "validate", str(theme)], work, 60)
    match = re.search(r"(\d+) issues, (\d+) errors", validated["stdout"])
    record["issues"] = int(match.group(1)) if match else -1
    record["errors"] = int(match.group(2)) if match else -1
    record["problems"] = [
        line for line in validated["stdout"].splitlines()
        if ": ERROR" in line or ": WARNING" in line
    ][:5]

    first = next(
        (entry for entry in info.get("layoutsDetail", []) if entry.get("layout")), None
    )
    if first:
        window = first.get("window") or ""
        layout = first.get("layout") or ""
        rendered = run(
            ["java", "-jar", str(jar), "render", str(theme),
             "-w", window, "-l", layout, "-z", "1",
             "-o", str(work / "render.png"), "--quiet"],
            work, 120)
        record["render_ms"] = rendered["ms"]
        png = work / "render.png"
        if rendered["code"] == 0 and png.exists() and png.stat().st_size > 200:
            record["status"] = "ok"
            record["render_bytes"] = png.stat().st_size
        else:
            record["status"] = "render failed"
            record["error"] = (rendered["stderr"] or rendered["stdout"]).strip()[:400]
    else:
        record["status"] = "no layout"
    return record


def write_report(records, out_dir):
    total = len(records)
    ok = sum(1 for record in records if record.get("status") == "ok")
    failed = [record for record in records if record.get("status") != "ok"]
    errors = sum(record.get("errors", 0) for record in records if record.get("errors", 0) > 0)
    issues = sum(record.get("issues", 0) for record in records)
    items = sum(record.get("items", 0) for record in records)
    render_ms = [record["render_ms"] for record in records if record.get("render_ms")]

    lines = []
    lines.append("# VLC skins gallery conformance report")
    lines.append("")
    lines.append("Every theme in the official pack at "
                 "https://images.videolan.org/vlc/skins.html was imported, "
                 "inspected, validated and rendered by the CLI.")
    lines.append("")
    lines.append(f"* Themes: {total}")
    lines.append(f"* Imported and rendered: {ok}")
    lines.append(f"* Not rendered: {len(failed)}")
    lines.append(f"* Layout items seen: {items}")
    lines.append(f"* Validation issues: {issues} (errors: {errors})")
    if render_ms:
        lines.append(f"* Render time: median {sorted(render_ms)[len(render_ms) // 2]} ms, "
                     f"max {max(render_ms)} ms")
    lines.append("")
    if failed:
        lines.append("## Not rendered")
        lines.append("")
        lines.append("| Theme | Status | Detail |")
        lines.append("|---|---|---|")
        for record in failed:
            detail = (record.get("error") or "").replace("|", "\\|").replace("\n", " ")[:180]
            lines.append(f"| {record['skin']} | {record['status']} | {detail} |")
        lines.append("")
    lines.append("## Per theme")
    lines.append("")
    lines.append("| Theme | Windows | Layouts | Items | Errors | Issues | Render ms | Status |")
    lines.append("|---|---|---|---|---|---|---|---|")
    for record in sorted(records, key=lambda entry: entry["skin"].lower()):
        lines.append("| {skin} | {windows} | {layouts} | {items} | {errors} | {issues} | {ms} | {status} |".format(
            skin=record["skin"],
            windows=record.get("windows", ""),
            layouts=record.get("layouts", ""),
            items=record.get("items", ""),
            errors=record.get("errors", ""),
            issues=record.get("issues", ""),
            ms=record.get("render_ms", ""),
            status=record["status"],
        ))
    lines.append("")
    lines.append("VeLoCity is MIT licensed and is checked separately by "
                 "tools/recreate-velocity-via-mcp.py; this report covers the "
                 "VideoLAN gallery only.")
    report = out_dir / "skin-gallery-report.md"
    report.write_text("\n".join(lines) + "\n", encoding="utf-8")
    (out_dir / "skin-gallery-report.json").write_text(
        json.dumps(records, indent=2), encoding="utf-8")
    print(f"wrote {report}")
    print(f"themes={total} ok={ok} failed={len(failed)} errors={errors}")


def main():
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument("--jar", required=True, type=Path)
    parser.add_argument("--work", type=Path, default=Path("/tmp/opencode/skin-gallery"))
    parser.add_argument("--out", type=Path, default=Path("docs"))
    parser.add_argument("--pack-url", default=DEFAULT_PACK)
    parser.add_argument("--jobs", type=int, default=4)
    parser.add_argument("--limit", type=int, default=0)
    parser.add_argument("--extra", type=Path, action="append", default=[],
                        help="A theme.xml or a folder containing one, repeatable; "
                             "use this for the themes VLC itself ships")
    parser.add_argument("--no-pack", action="store_true",
                        help="Only check --extra themes, skip the gallery pack")
    args = parser.parse_args()

    if not args.jar.exists():
        print(f"jar not found: {args.jar}", file=sys.stderr)
        return 1
    args.jar = args.jar.resolve()
    args.out.mkdir(parents=True, exist_ok=True)
    args.work.mkdir(parents=True, exist_ok=True)

    records = []
    if not args.no_pack:
        pack = download(args.pack_url, args.work / "vlc-skins.zip")
        unpacked = args.work / "skins"
        if not unpacked.exists():
            print(f"unpacking {pack}")
            unpacked.mkdir(parents=True, exist_ok=True)
            with zipfile.ZipFile(pack) as archive:
                archive.extractall(unpacked)
        vlts = sorted(unpacked.rglob("*.vlt"))
        if args.limit:
            vlts = vlts[:args.limit]
        print(f"{len(vlts)} gallery themes to check with {args.jobs} workers")
        with concurrent.futures.ThreadPoolExecutor(max_workers=args.jobs) as pool:
            futures = {pool.submit(check_skin, args.jar, vlt, args.work): vlt for vlt in vlts}
            for future in concurrent.futures.as_completed(futures):
                record = future.result()
                records.append(record)
                print(f"  {record['skin']}: {record['status']} "
                      f"(errors={record.get('errors', '?')})", flush=True)

    for extra in args.extra:
        theme = extra / "theme.xml" if extra.is_dir() else extra
        if not theme.exists():
            print(f"extra theme not found: {theme}", file=sys.stderr)
            continue
        print(f"checking official theme {theme}")
        record = check_official(args.jar, theme, args.work)
        records.append(record)
        print(f"  {record['skin']}: {record['status']} (errors={record.get('errors', '?')})", flush=True)

    if not records:
        print("nothing to check", file=sys.stderr)
        return 1
    write_report(records, args.out)
    return 0


if __name__ == "__main__":
    sys.exit(main())
