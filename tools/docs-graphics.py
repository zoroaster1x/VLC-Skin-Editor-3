#!/usr/bin/env python3
"""Draw the handbook diagrams for the built in documentation viewer.

Usage:
    python3 tools/docs-graphics.py OUTDIR

The style matches the desktop app: dark slate panels, VLC orange accent.
Every diagram is generated from code, so it can be fixed or restyled without
redrawing anything by hand.
"""

import sys
from pathlib import Path

from PIL import Image, ImageDraw, ImageFont

BG = (30, 31, 34)
PANEL = (43, 45, 48)
PANEL_ALT = (52, 54, 58)
BORDER = (78, 81, 87)
TEXT = (223, 225, 229)
MUTED = (157, 160, 168)
ACCENT = (224, 108, 56)
ACCENT_DARK = (184, 83, 36)
GREEN = (106, 190, 120)
BLUE = (97, 175, 239)

FONT_DIR = Path("/usr/share/fonts/dejavu-sans-fonts")
FONT = FONT_DIR / "DejaVuSans.ttf"
FONT_BOLD = FONT_DIR / "DejaVuSans-Bold.ttf"

SCALE = 2


def font(size, bold=False):
    return ImageFont.truetype(str(FONT_BOLD if bold else FONT), size * SCALE)


def canvas(width, height):
    image = Image.new("RGB", (width * SCALE, height * SCALE), BG)
    return image, ImageDraw.Draw(image)


def box(draw, x, y, w, h, label, fill=PANEL, outline=BORDER, text_color=TEXT, size=13, bold=False):
    draw.rounded_rectangle([x * SCALE, y * SCALE, (x + w) * SCALE, (y + h) * SCALE],
                           radius=6 * SCALE, fill=fill, outline=outline, width=SCALE)
    if label:
        f = font(size, bold)
        left, top, right, bottom = draw.textbbox((0, 0), label, font=f)
        draw.text((((x + w / 2) * SCALE) - (right - left) / 2, ((y + h / 2) * SCALE) - (bottom - top) / 2 - top),
                  label, font=f, fill=text_color)


def text(draw, x, y, value, color=TEXT, size=13, bold=False, anchor="la"):
    draw.text((x * SCALE, y * SCALE), value, font=font(size, bold), fill=color, anchor=anchor)


def arrow(draw, x1, y1, x2, y2, color=MUTED, width=2):
    draw.line([x1 * SCALE, y1 * SCALE, x2 * SCALE, y2 * SCALE], fill=color, width=width * SCALE)
    import math
    angle = math.atan2(y2 - y1, x2 - x1)
    size = 7 * SCALE
    for delta in (math.radians(155), math.radians(-155)):
        draw.line([x2 * SCALE, y2 * SCALE,
                   (x2 + math.cos(angle + delta) * size / SCALE) * SCALE,
                   (y2 + math.sin(angle + delta) * size / SCALE) * SCALE], fill=color, width=width * SCALE)


def save(image, out, name):
    path = out / name
    image.save(path)
    print("wrote", path, f"{path.stat().st_size // 1024} KiB")


def theme_structure(out):
    image, draw = canvas(760, 300)
    box(draw, 20, 120, 150, 60, "Theme", fill=PANEL_ALT, bold=True)
    box(draw, 250, 60, 160, 60, "Window \"main\"", bold=True)
    box(draw, 250, 180, 160, 60, "Window \"mini\"")
    box(draw, 480, 60, 150, 60, "Layout \"main\"")
    box(draw, 480, 150, 150, 60, "Layout \"playlist\"")
    box(draw, 480, 240, 240, 45, "Items: Button, Slider, ...", fill=PANEL_ALT)
    arrow(draw, 170, 150, 250, 95)
    arrow(draw, 170, 150, 250, 205)
    arrow(draw, 410, 90, 480, 90)
    arrow(draw, 410, 90, 480, 180)
    arrow(draw, 555, 120, 570, 240)
    save(image, out, "theme-structure.png")


def layout_coordinates(out):
    image, draw = canvas(520, 330)
    box(draw, 70, 80, 380, 180, "", fill=PANEL)
    draw.line([70 * SCALE, 80 * SCALE, 450 * SCALE, 80 * SCALE], fill=ACCENT, width=SCALE)
    draw.line([70 * SCALE, 80 * SCALE, 70 * SCALE, 260 * SCALE], fill=ACCENT, width=SCALE)
    text(draw, 78, 60, "x = 0, y = 0", color=ACCENT, bold=True)
    box(draw, 160, 130, 120, 70, "item", fill=PANEL_ALT)
    text(draw, 168, 118, "x, y", color=BLUE)
    text(draw, 286, 150, "width", color=GREEN)
    text(draw, 178, 205, "height", color=GREEN)
    draw.line([70 * SCALE, 280 * SCALE, 450 * SCALE, 280 * SCALE], fill=MUTED, width=SCALE)
    text(draw, 250, 292, "layout width", color=MUTED, anchor="ma")
    draw.line([40 * SCALE, 80 * SCALE, 40 * SCALE, 260 * SCALE], fill=MUTED, width=SCALE)
    text(draw, 30, 170, "layout height", color=MUTED, anchor="mm")
    save(image, out, "layout-coordinates.png")


def anchors(out):
    image, draw = canvas(760, 320)
    text(draw, 30, 20, "Window resized", bold=True, size=15)
    box(draw, 30, 60, 150, 110, "lefttop", fill=PANEL)
    box(draw, 45, 75, 60, 40, "fixed", fill=PANEL_ALT, size=11)
    text(draw, 30, 185, "anchor: lefttop", color=MUTED, size=11)
    text(draw, 30, 200, "stays at its x/y", color=MUTED, size=11)

    box(draw, 260, 60, 190, 140, "rightbottom", fill=PANEL)
    box(draw, 370, 85, 60, 40, "grows", fill=PANEL_ALT, size=11)
    text(draw, 260, 215, "anchor: rightbottom", color=MUTED, size=11)
    text(draw, 260, 230, "keeps the distance to the corner", color=MUTED, size=11)

    box(draw, 520, 60, 220, 90, "lefttop + rightbottom", fill=PANEL)
    text(draw, 520, 165, "two anchors stretch an item", color=MUTED, size=11)
    text(draw, 520, 180, "with the layout", color=MUTED, size=11)
    save(image, out, "anchors.png")


def slider_path(out):
    image, draw = canvas(620, 300)
    box(draw, 40, 60, 540, 160, "", fill=PANEL)
    points = [(90, 140), (200, 90), (350, 190), (520, 120)]
    path = []
    for i in range(101):
        t = i / 100
        x = (1 - t) ** 3 * points[0][0] + 3 * (1 - t) ** 2 * t * points[1][0] \
            + 3 * (1 - t) * t ** 2 * points[2][0] + t ** 3 * points[3][0]
        y = (1 - t) ** 3 * points[0][1] + 3 * (1 - t) ** 2 * t * points[1][1] \
            + 3 * (1 - t) * t ** 2 * points[2][1] + t ** 3 * points[3][1]
        path.append((x * SCALE, y * SCALE))
    draw.line(path, fill=ACCENT, width=3 * SCALE)
    for x, y in points:
        draw.ellipse([(x - 5) * SCALE, (y - 5) * SCALE, (x + 5) * SCALE, (y + 5) * SCALE], fill=BLUE)
    thumb = path[65]
    draw.ellipse([thumb[0] - 8 * SCALE, thumb[1] - 8 * SCALE, thumb[0] + 8 * SCALE, thumb[1] + 8 * SCALE],
                 fill=PANEL_ALT, outline=TEXT, width=SCALE)
    text(draw, 90, 225, "points=\"90,140,200,90,350,190,520,120\"", color=MUTED, size=11)
    text(draw, 90, 245, "value 0.65 places the thumb on the sampled curve", color=MUTED, size=11)
    save(image, out, "slider-path.png")


def slider_background(out):
    image, draw = canvas(680, 340)
    text(draw, 30, 20, "One bitmap, nbhoriz x nbvert frames", bold=True, size=15)
    for row in range(3):
        for column in range(4):
            index = row * 4 + column
            fill = ACCENT if index == 6 else PANEL_ALT
            box(draw, 40 + column * 152, 60 + row * 80, 130, 60,
                f"frame {index}", fill=fill, size=12)
    text(draw, 40, 310, "value 0.55 -> floor(12 * 0.55) = frame 6", color=MUTED, size=12)
    save(image, out, "slider-background.png")


def bitmap_frames(out):
    image, draw = canvas(700, 230)
    text(draw, 30, 20, "A strip is cut into nbframes equal frames", bold=True, size=15)
    colors = [PANEL_ALT, (60, 90, 120), (90, 70, 110), (70, 110, 80)]
    for i, color in enumerate(colors):
        box(draw, 40 + i * 160, 70, 150, 80, f"frame {i}", fill=color, size=12)
    text(draw, 40, 170, "nbframes=\"4\" fps=\"10\" loop=\"1\"", color=MUTED, size=12)
    save(image, out, "bitmap-frames.png")


def alphacolor(out):
    image, draw = canvas(620, 260)
    text(draw, 30, 20, "alphacolor keys out one RGB value", bold=True, size=15)
    key = (255, 0, 255)
    for row in range(4):
        for column in range(6):
            is_key = (row + column) % 5 == 0
            color = key if is_key else (60 + column * 20, 70 + row * 20, 90)
            draw.rectangle([(40 + column * 70) * SCALE, (60 + row * 50) * SCALE,
                            (100 + column * 70) * SCALE, (100 + row * 50) * SCALE],
                           fill=color, outline=BORDER, width=SCALE)
            if is_key:
                draw.line([(40 + column * 70) * SCALE, (60 + row * 50) * SCALE,
                           (100 + column * 70) * SCALE, (100 + row * 50) * SCALE], fill=TEXT, width=2 * SCALE)
    text(draw, 40, 225, "alphacolor=\"#FF00FF\" makes those pixels fully transparent", color=MUTED, size=12)
    save(image, out, "alphacolor.png")


def playtree(out):
    image, draw = canvas(420, 260)
    box(draw, 40, 50, 320, 180, "", fill=PANEL)
    text(draw, 60, 65, "Playlist", bold=True)
    for i, label in enumerate(["Artist - Title", "folder", "  track one", "  track two"]):
        text(draw, 60 + (12 if label.startswith(" ") else 0), 95 + i * 24, label.strip(),
             color=MUTED if label == "folder" else TEXT, size=12)
    box(draw, 250, 190, 90, 26, "Slider", fill=ACCENT_DARK, size=11)
    arrow(draw, 295, 190, 295, 165)
    text(draw, 40, 240, "the slider follows the playlist scroll position", color=MUTED, size=11)
    save(image, out, "playtree.png")


def vlt_archive(out):
    image, draw = canvas(560, 280)
    box(draw, 40, 50, 220, 190, "", fill=PANEL)
    text(draw, 60, 65, "theme.vlt", bold=True)
    for i, label in enumerate(["theme.xml", "main.png", "play.png", "font.ttf", "buttons.png"]):
        text(draw, 60, 95 + i * 24, label, color=MUTED if i else ACCENT, size=12)
    text(draw, 300, 90, "gzipped tar, or a plain zip", size=12)
    text(draw, 300, 115, "every referenced asset", color=MUTED, size=12)
    text(draw, 300, 140, "nested .vlt bundles unpack", color=MUTED, size=12)
    text(draw, 300, 165, "into one folder per theme", color=MUTED, size=12)
    save(image, out, "vlt-archive.png")


def main():
    if len(sys.argv) != 2:
        print(__doc__, file=sys.stderr)
        return 2
    out = Path(sys.argv[1])
    out.mkdir(parents=True, exist_ok=True)
    theme_structure(out)
    layout_coordinates(out)
    anchors(out)
    slider_path(out)
    slider_background(out)
    bitmap_frames(out)
    alphacolor(out)
    playtree(out)
    vlt_archive(out)
    return 0


if __name__ == "__main__":
    sys.exit(main())
