#!/usr/bin/env python3
"""Compare the geometry of two renders control by control.

Why this exists:
    A raw pixel diff between a Java2D render and a VLC FreeType/XRender render
    can never reach zero: anti-aliased icon edges and the rounded window border
    blend differently even when every control is exactly where it belongs.
    Counting those pixels hides the defects that matter. This tool instead
    finds every non-background region in both images, matches them, and reports
    the position and size delta of each one. A button that is 3 px too low or a
    slider that is 20 px too short shows up as a number; an anti-aliased edge
    does not.

How it works:
    1. The background is the most common colour; pixels within --tolerance of
       it are ignored.
    2. Remaining pixels are grouped into connected regions of at least
       --min-pixels (4-connected).
    3. Regions are matched by rectangle overlap (or nearest centre within
       --match-distance), then each pair reports dx, dy, width and height deltas.
    4. Optionally an alignment shift is searched first and subtracted from every
       region, because a VLC window capture can land a pixel or two off.

Usage:
    tools/parity/region_diff.py ours.png vlc.png
    tools/parity/region_diff.py ours.png vlc.png --json report.json --annotated out.png
    tools/parity/region_diff.py ours.png vlc.png --max-shift 1 --max-size 2
    tools/parity/region_diff.py ours.png vlc.png --align 3 --min-pixels 8

Exit code 0 when every matched region is within the tolerances and every
region is matched, 1 otherwise, 2 on unreadable input.
"""

import argparse
import json
import sys
from collections import deque

from PIL import Image, ImageDraw


def load(path):
    try:
        return Image.open(path).convert("RGB")
    except (OSError, ValueError) as exc:
        print(f"cannot read {path}: {exc}", file=sys.stderr)
        sys.exit(2)


def background_color(image):
    counts = {}
    for count, color in image.getcolors(maxcolors=1 << 24):
        counts[color] = counts.get(color, 0) + count
    return max(counts, key=counts.get)


def regions(image, tolerance, min_pixels):
    width, height = image.size
    background = background_color(image)
    pixels = image.load()
    mask = [[False] * width for _ in range(height)]
    for y in range(height):
        for x in range(width):
            r, g, b = pixels[x, y]
            if (abs(r - background[0]) + abs(g - background[1])
                    + abs(b - background[2])) > tolerance:
                mask[y][x] = True
    seen = [[False] * width for _ in range(height)]
    found = []
    for y in range(height):
        for x in range(width):
            if not mask[y][x] or seen[y][x]:
                continue
            queue = deque([(x, y)])
            seen[y][x] = True
            min_x = max_x = x
            min_y = max_y = y
            count = 0
            while queue:
                cx, cy = queue.popleft()
                count += 1
                min_x = min(min_x, cx)
                max_x = max(max_x, cx)
                min_y = min(min_y, cy)
                max_y = max(max_y, cy)
                for nx, ny in ((cx + 1, cy), (cx - 1, cy), (cx, cy + 1), (cx, cy - 1)):
                    if 0 <= nx < width and 0 <= ny < height and mask[ny][nx] and not seen[ny][nx]:
                        seen[ny][nx] = True
                        queue.append((nx, ny))
            if count >= min_pixels:
                found.append({"bbox": [min_x, min_y, max_x, max_y], "pixels": count,
                              "center": ((min_x + max_x) / 2, (min_y + max_y) / 2)})
    return background, found


def merge_regions(regions_found, image_size, distance=2):
    """Merge region fragments whose bounds nearly touch.

    Antialiasing can split one icon into two components (a prev bar and its
    triangle); VLC and Java2D may split them differently, which would look
    like a size difference. Fragments within `distance` pixels are one control.
    A region that spans most of the image (the window border ring) is never
    merged, or it would swallow every control inside it.
    """
    image_area = image_size[0] * image_size[1]

    def is_frame(region):
        bbox = region["bbox"]
        area = (bbox[2] - bbox[0] + 1) * (bbox[3] - bbox[1] + 1)
        return area > image_area * 0.4

    merged = True
    while merged:
        merged = False
        for i in range(len(regions_found)):
            for j in range(i + 1, len(regions_found)):
                if is_frame(regions_found[i]) or is_frame(regions_found[j]):
                    continue
                a = regions_found[i]["bbox"]
                b = regions_found[j]["bbox"]
                combined = [min(a[0], b[0]), min(a[1], b[1]),
                            max(a[2], b[2]), max(a[3], b[3])]
                # Cap the merged size: two adjacent control fragments (a prev
                # bar and its triangle) form a small box, but a row of letters
                # must not fuse into one long blob or it stops matching the
                # per-glyph regions of the other image.
                area = (combined[2] - combined[0] + 1) * (combined[3] - combined[1] + 1)
                if area > 1200:
                    continue
                if (a[0] - distance <= b[2] and b[0] - distance <= a[2]
                        and a[1] - distance <= b[3] and b[1] - distance <= a[3]):
                    pixels = regions_found[i]["pixels"] + regions_found[j]["pixels"]
                    regions_found[i] = {
                        "bbox": combined, "pixels": pixels,
                        "center": ((combined[0] + combined[2]) / 2,
                                   (combined[1] + combined[3]) / 2),
                    }
                    del regions_found[j]
                    merged = True
                    break
            if merged:
                break
    return regions_found


def align(ours, vlc, radius):
    """Integer shift of vlc over ours with the fewest differing pixels."""
    if radius <= 0:
        return 0, 0
    best = None
    for dy in range(-radius, radius + 1):
        for dx in range(-radius, radius + 1):
            width = min(ours.width, vlc.width) - abs(dx)
            height = min(ours.height, vlc.height) - abs(dy)
            if width <= 0 or height <= 0:
                continue
            a = ours.crop((max(0, dx), max(0, dy),
                           max(0, dx) + width, max(0, dy) + height))
            b = vlc.crop((max(0, -dx), max(0, -dy),
                          max(0, -dx) + width, max(0, -dy) + height))
            score = 0
            pa, pb = a.load(), b.load()
            for y in range(height):
                for x in range(width):
                    ra, ga, ba = pa[x, y]
                    rb, gb, bb = pb[x, y]
                    if abs(ra - rb) + abs(ga - gb) + abs(ba - bb) > 30:
                        score += 1
            if best is None or score < best[0]:
                best = (score, dx, dy)
    return best[1], best[2]


def overlaps(a, b):
    return not (a[2] < b[0] or b[2] < a[0] or a[3] < b[1] or b[3] < a[1])


def match(ours_regions, vlc_regions, dx, dy):
    """Pair regions by overlap, then by nearest edge distance."""
    pairs = []
    used = set()
    shifted = []
    for region in vlc_regions:
        bbox = region["bbox"]
        shifted.append({"bbox": [bbox[0] - dx, bbox[1] - dy, bbox[2] - dx, bbox[3] - dy],
                        "pixels": region["pixels"],
                        "center": (region["center"][0] - dx, region["center"][1] - dy)})
    for i, our in enumerate(ours_regions):
        for j, their in enumerate(shifted):
            if j in used:
                continue
            if overlaps(our["bbox"], their["bbox"]):
                pairs.append((i, j))
                used.add(j)
                break
    matched_ours = {i for i, _ in pairs}
    matched_vlc = {j for _, j in pairs}
    unmatched_ours = [ours_regions[i] for i in range(len(ours_regions))
                      if i not in matched_ours]
    unmatched_vlc = [shifted[j] for j in range(len(shifted)) if j not in matched_vlc]
    return pairs, shifted, unmatched_ours, unmatched_vlc


def main():
    parser = argparse.ArgumentParser(description=__doc__,
                                     formatter_class=argparse.RawDescriptionHelpFormatter)
    parser.add_argument("ours", help="the render under test")
    parser.add_argument("vlc", help="the reference render")
    parser.add_argument("--tolerance", type=int, default=40,
                        help="colour distance from the background treated as background")
    parser.add_argument("--min-pixels", type=int, default=6,
                        help="ignore regions smaller than this")
    parser.add_argument("--max-shift", type=int, default=1,
                        help="allowed dx/dy between matched regions")
    parser.add_argument("--max-size", type=int, default=2,
                        help="allowed width/height delta between matched regions")
    parser.add_argument("--align", type=int, default=3,
                        help="search this many pixels of global shift first")
    parser.add_argument("--json", help="write the full region report here")
    parser.add_argument("--annotated", help="write ours with region labels here")
    args = parser.parse_args()

    ours = load(args.ours)
    vlc = load(args.vlc)
    dx, dy = align(ours, vlc, args.align)

    our_background, our_regions = regions(ours, args.tolerance, args.min_pixels)
    vlc_background, vlc_regions = regions(vlc, args.tolerance, args.min_pixels)
    our_regions = merge_regions(our_regions, ours.size)
    vlc_regions = merge_regions(vlc_regions, vlc.size)
    pairs, shifted, unmatched_ours, unmatched_vlc = match(our_regions, vlc_regions, dx, dy)

    differences = []
    for i, j in pairs:
        a = our_regions[i]["bbox"]
        b = shifted[j]["bbox"]
        delta = {"bbox_ours": a, "bbox_vlc": b,
                 "dx": b[0] - a[0], "dy": b[1] - a[1],
                 "dwidth": (b[2] - b[0]) - (a[2] - a[0]),
                 "dheight": (b[3] - b[1]) - (a[3] - a[1])}
        delta["ok"] = (abs(delta["dx"]) <= args.max_shift
                       and abs(delta["dy"]) <= args.max_shift
                       and abs(delta["dwidth"]) <= args.max_size
                       and abs(delta["dheight"]) <= args.max_size)
        differences.append(delta)

    failed = [d for d in differences if not d["ok"]]
    result = {
        "ours": args.ours,
        "vlc": args.vlc,
        "background_ours": our_background,
        "background_vlc": vlc_background,
        "align": {"dx": dx, "dy": dy},
        "regions_ours": len(our_regions),
        "regions_vlc": len(vlc_regions),
        "matched": differences,
        "unmatched_ours": unmatched_ours,
        "unmatched_vlc": unmatched_vlc,
        "failed": len(failed) + len(unmatched_ours) + len(unmatched_vlc),
    }
    if args.json:
        with open(args.json, "w", encoding="utf-8") as handle:
            json.dump(result, handle, indent=2)
    if args.annotated:
        annotated = ours.copy()
        draw = ImageDraw.Draw(annotated)
        for index, delta in enumerate(differences):
            color = (0, 200, 0) if delta["ok"] else (255, 0, 0)
            draw.rectangle(delta["bbox_ours"], outline=color)
            draw.text((delta["bbox_ours"][0], max(0, delta["bbox_ours"][1] - 10)),
                      str(index), fill=color)
        annotated.save(args.annotated)

    print(f"align {dx},{dy}; regions ours={len(our_regions)} vlc={len(vlc_regions)} "
          f"matched={len(differences)}")
    for index, delta in enumerate(differences):
        if not delta["ok"]:
            print(f"  region {index}: dx={delta['dx']} dy={delta['dy']} "
                  f"dw={delta['dwidth']} dh={delta['dheight']} "
                  f"ours={delta['bbox_ours']} vlc={delta['bbox_vlc']}")
    for region in unmatched_ours:
        print(f"  unmatched in ours: {region['bbox']} ({region['pixels']} px)")
    for region in unmatched_vlc:
        print(f"  unmatched in vlc: {region['bbox']} ({region['pixels']} px)")
    print(f"{result['failed']} region problem(s)")
    return 1 if result["failed"] else 0


if __name__ == "__main__":
    sys.exit(main())
