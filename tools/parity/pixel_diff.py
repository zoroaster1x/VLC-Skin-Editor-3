#!/usr/bin/env python3
"""Numeric PNG comparison for renderer verification.

Why this exists:
    Looking at two screenshots and saying "close enough" is not a check. A
    person, and even more so a model, cannot reliably tell a one pixel thumb
    offset from a three pixel one, cannot measure clipped text, and adapts to
    whatever it sees first. This tool turns the comparison into numbers: every
    differing pixel is counted, the mismatch bounding box and the largest
    channel delta are reported, and a highlighted diff image is written for
    review. Test thresholds are then set from measurements, not impressions.

Two comparison modes:
    exact    every pixel matters within --tolerance per channel. Use this for
             controls, images, borders, slider thumbs and backgrounds.
    blocks   both images are averaged over --block sized tiles and the tile
             means are compared. Use this for full frames with text: font
             rasterization can never match between Java2D and FreeType, but a
             shifted or mis-sized control still moves many tiles and fails.

Usage:
    tools/parity/pixel_diff.py ours.png vlc.png
    tools/parity/pixel_diff.py ours.png vlc.png --diff /tmp/diff.png --json /tmp/diff.json
    tools/parity/pixel_diff.py ours.png vlc.png --tolerance 8 --max-percent 0.5
    tools/parity/pixel_diff.py ours.png vlc.png --align 3
    tools/parity/pixel_diff.py ours.png vlc.png --mode blocks --block 8 --max-percent 1.0
    tools/parity/pixel_diff.py ours.png vlc.png --mask 100,10,200,30 --mask 0,0,320,20

Exit code 0 when the images pass the thresholds, 1 when they do not, 2 on a
missing or unreadable file.
"""

import argparse
import json
import os
import sys

from PIL import Image, ImageChops, ImageDraw


def load(path):
    try:
        return Image.open(path).convert("RGBA")
    except (OSError, ValueError) as exc:
        print(f"cannot read {path}: {exc}", file=sys.stderr)
        sys.exit(2)


def apply_masks(image, masks):
    """Paint masked rectangles one flat colour so they stop counting."""
    if not masks:
        return image
    copy = image.copy()
    draw = ImageDraw.Draw(copy)
    for mask in masks:
        draw.rectangle([mask[0], mask[1], mask[0] + mask[2] - 1, mask[1] + mask[3] - 1],
                       fill=(0, 0, 0, 0))
    return copy


def common_size(a, b):
    width = min(a.width, b.width)
    height = min(a.height, b.height)
    return a.crop((0, 0, width, height)), b.crop((0, 0, width, height))


def align_images(a, b, radius):
    """Integer shift of b over a, chosen to minimise mismatches.

    A VLC capture can be off by a pixel from the client-side origin; without
    this, a one pixel offset would make every border pixel differ and drown
    the real findings.
    """
    if radius <= 0:
        return 0, 0, None
    best = None
    for dy in range(-radius, radius + 1):
        for dx in range(-radius, radius + 1):
            width = min(a.width, b.width) - abs(dx)
            height = min(a.height, b.height) - abs(dy)
            if width <= 0 or height <= 0:
                continue
            ax = 0 if dx >= 0 else -dx
            ay = 0 if dy >= 0 else -dy
            bx = 0 if dx <= 0 else dx
            by = 0 if dy <= 0 else dy
            left = min(a.width - ax, b.width - bx, width)
            top = min(a.height - ay, b.height - by, height)
            region_a = a.crop((ax, ay, ax + left, ay + top))
            region_b = b.crop((bx, by, bx + left, by + top))
            bbox = ImageChops.difference(region_a, region_b).getbbox()
            if bbox is None:
                score = 0
            else:
                diff = ImageChops.difference(region_a, region_b).convert("L")
                score = sum(1 for value in diff.getdata() if value > 0)
            if best is None or score < best[0]:
                best = (score, dx, dy)
    if best is None:
        return 0, 0, None
    return best[1], best[2], best[0]


def exact_diff(a, b, tolerance):
    diff = ImageChops.difference(a, b)
    channels = diff.split()
    mask = None
    for channel in channels:
        channel_mask = channel.point(lambda value: 255 if value > tolerance else 0)
        mask = channel_mask if mask is None else ImageChops.lighter(mask, channel_mask)
    mismatched = sum(1 for value in mask.getdata() if value)
    bbox = mask.getbbox()
    max_delta = max((max(channel.getdata()) for channel in channels), default=0)
    return {
        "mode": "exact",
        "pixels": a.width * a.height,
        "mismatched": mismatched,
        "percent": 100.0 * mismatched / max(1, a.width * a.height),
        "max_channel_delta": max_delta,
        "bbox": list(bbox) if bbox else None,
        "mask": mask,
    }


def block_diff(a, b, block, tolerance):
    tiles = 0
    bad = 0
    worst = 0
    bad_bbox = None
    mask = Image.new("L", a.size, 0)
    for y in range(0, a.height, block):
        for x in range(0, a.width, block):
            width = min(block, a.width - x)
            height = min(block, a.height - y)
            if width <= 0 or height <= 0:
                continue
            tiles += 1
            region_a = a.crop((x, y, x + width, y + height))
            region_b = b.crop((x, y, x + width, y + height))
            delta = 0.0
            for channel_a, channel_b in zip(region_a.split(), region_b.split()):
                mean_a = sum(channel_a.getdata()) / (width * height)
                mean_b = sum(channel_b.getdata()) / (width * height)
                delta = max(delta, abs(mean_a - mean_b))
            if delta > tolerance:
                bad += 1
                worst = max(worst, delta)
                ImageDraw.Draw(mask).rectangle([x, y, x + width - 1, y + height - 1], fill=255)
                if bad_bbox is None:
                    bad_bbox = [x, y, x + width, y + height]
                else:
                    bad_bbox = [min(bad_bbox[0], x), min(bad_bbox[1], y),
                                max(bad_bbox[2], x + width), max(bad_bbox[3], y + height)]
    return {
        "mode": f"blocks/{block}",
        "pixels": a.width * a.height,
        "mismatched": bad,
        "percent": 100.0 * bad / max(1, tiles),
        "max_channel_delta": round(worst, 1),
        "bbox": bad_bbox,
        "mask": mask,
    }


def write_diff_image(image, mask, path):
    """Expected in grey, differing pixels and tiles in red."""
    grey = image.convert("L").convert("RGB")
    overlay = Image.new("RGB", image.size, (255, 0, 0))
    grey.paste(overlay, (0, 0), mask)
    grey.save(path)


def main():
    parser = argparse.ArgumentParser(description=__doc__,
                                     formatter_class=argparse.RawDescriptionHelpFormatter)
    parser.add_argument("expected", help="reference PNG, for example the VLC capture")
    parser.add_argument("actual", help="PNG under test, for example our render")
    parser.add_argument("--diff", help="write a red-highlighted diff image here")
    parser.add_argument("--json", help="write the metrics as JSON here")
    parser.add_argument("--mode", choices=["exact", "blocks"], default="exact")
    parser.add_argument("--block", type=int, default=8, help="tile size for blocks mode")
    parser.add_argument("--tolerance", type=int, default=2,
                        help="per channel delta treated as equal, and the block mean delta")
    parser.add_argument("--max-percent", type=float, default=0.0,
                        help="fail above this percentage of differing pixels")
    parser.add_argument("--align", type=int, default=0,
                        help="search this many pixels of shift before comparing")
    parser.add_argument("--allow-crop", action="store_true",
                        help="compare the common area when the sizes differ")
    parser.add_argument("--mask", action="append", default=[],
                        help="x,y,w,h region to ignore, repeatable")
    args = parser.parse_args()

    masks = []
    for spec in args.mask:
        parts = [int(part) for part in spec.replace(" ", "").split(",")]
        if len(parts) != 4:
            raise SystemExit(f"bad --mask {spec!r}, expected x,y,w,h")
        masks.append(parts)

    a = apply_masks(load(args.expected), masks)
    b = apply_masks(load(args.actual), masks)
    size_mismatch = (a.width, a.height) != (b.width, b.height)
    if size_mismatch and not args.allow_crop:
        print(f"size differs: expected {a.width}x{a.height}, actual {b.width}x{b.height}",
              file=sys.stderr)
        return 1
    a, b = common_size(a, b)

    shift = (0, 0, None)
    if args.align > 0:
        shift = align_images(a, b, args.align)
        dx, dy = shift[0], shift[1]
        if dx or dy:
            a = a.crop((max(0, dx), max(0, dy), a.width - max(0, -dx), a.height - max(0, -dy)))
            b = b.crop((max(0, -dx), max(0, -dy), b.width - max(0, dx), b.height - max(0, dy)))

    if args.mode == "exact":
        result = exact_diff(a, b, args.tolerance)
    else:
        result = block_diff(a, b, args.block, args.tolerance)
    mask = result.pop("mask")
    result["shift"] = {"dx": shift[0], "dy": shift[1]}
    result["size_mismatch"] = size_mismatch
    result["expected"] = args.expected
    result["actual"] = args.actual

    if args.diff:
        write_diff_image(a, mask, args.diff)
        result["diff_image"] = args.diff

    print(json.dumps(result, indent=2, default=str))
    if args.json:
        with open(args.json, "w", encoding="utf-8") as handle:
            json.dump(result, handle, indent=2, default=str)
    failed = result["percent"] > args.max_percent
    if failed:
        print(f"FAIL: {result['percent']:.3f}% differs, budget {args.max_percent}%",
              file=sys.stderr)
    return 1 if failed else 0


if __name__ == "__main__":
    sys.exit(main())
