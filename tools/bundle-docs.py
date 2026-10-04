#!/usr/bin/env python3
"""Bundle the crawled VLC documentation into the app resources.

Reads a source folder of crawled markdown and images (the default is
``build/docs-source``), prunes unused images, optionally downsizes large ones,
rewrites image links, and writes the viewer bundle to
src/main/resources/dev/zoroaster1x/vlcskin/app/docs/.

Usage:
    python3 tools/bundle-docs.py [--source build/docs-source]
"""

import argparse
import hashlib
import json
import re
import shutil
import subprocess
import sys
from pathlib import Path

ROOT = Path(__file__).resolve().parent.parent
TARGET = ROOT / "src/main/resources/dev/zoroaster1x/vlcskin/app/docs"

AREAS = [
    ("handbook", "Reference", "Reference"),
    ("skinedhlp", "Original Skin Editor help", "Original help"),
    ("skins2-create", "Creating skins2 themes", "Creating skins"),
    ("readthedocs", "VLC user documentation", "VLC docs"),
]

IMAGE_EXTENSIONS = {".png", ".jpg", ".jpeg", ".gif", ".bmp", ".svg", ".webp"}


def slug(text):
    return re.sub(r"[^a-z0-9]+", "-", text.lower()).strip("-")


def find_image(source_dir, name):
    """The crawlers may have kept images in the area folder or images/ below it."""
    cleaned = name.replace("\\", "/").lstrip("./")
    candidates = [source_dir / cleaned, source_dir / "images" / Path(cleaned).name]
    for candidate in candidates:
        if candidate.is_file():
            return candidate
    for candidate in source_dir.rglob(Path(cleaned).name):
        if candidate.is_file() and candidate.name != "index.md":
            return candidate
    return None


def resize(image, destination):
    destination.parent.mkdir(parents=True, exist_ok=True)
    magick = shutil.which("magick") or shutil.which("convert")
    if magick and image.suffix.lower() in {".png", ".jpg", ".jpeg"}:
        result = subprocess.run(
            [magick, str(image), "-resize", "1200x1200>", "-strip", str(destination)],
            capture_output=True)
        if result.returncode == 0 and destination.is_file() and destination.stat().st_size > 0:
            return
    shutil.copy2(image, destination)


def main():
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument("--source", type=Path, default=Path("build/docs-source"))
    args = parser.parse_args()

    if not args.source.is_dir():
        print(f"source folder not found: {args.source}", file=sys.stderr)
        return 1

    pages_dir = TARGET / "pages"
    images_dir = TARGET / "images"
    for folder in (pages_dir, images_dir):
        if folder.exists():
            shutil.rmtree(folder)
        folder.mkdir(parents=True)

    topics = []
    digest = hashlib.sha256()
    used_images = {}

    for area, section, _label in AREAS:
        source = args.source / area
        if not source.is_dir():
            continue
        markdown_files = sorted(path for path in source.rglob("*.md")
                                if path.is_file() and path.name != "notes.md")
        if not markdown_files:
            continue
        for file in markdown_files:
            text = file.read_text(encoding="utf-8")
            digest.update(file.name.encode("utf-8"))
            digest.update(text.encode("utf-8", "replace"))
            stem = slug(f"{area}-{file.stem}") if file.stem not in ("index", "notes") \
                else slug(f"{area}-{file.stem}")
            page_name = f"{stem}.md"

            def rewrite(match):
                raw = match.group(1)
                if raw.startswith(("http://", "https://", "data:")):
                    return match.group(0)
                image = find_image(source, raw)
                if image is None:
                    return match.group(0)
                stored = used_images.get(str(image))
                if stored is None:
                    target_name = f"{area}-{slug(image.stem)}{image.suffix.lower()}"
                    resize(image, images_dir / target_name)
                    stored = target_name
                    used_images[str(image)] = stored
                return f"](images/{stored})"

            text = re.sub(r"\]\(([^)]+\.(?:png|jpe?g|gif|bmp|svg|webp))\)", rewrite, text)
            (pages_dir / page_name).write_text(text, encoding="utf-8")

            title = file.stem.replace("-", " ").replace("_", " ").strip() or area
            title_match = re.search(r"^title\s*:\s*(.+)$", text, re.IGNORECASE | re.MULTILINE)
            if title_match:
                title = title_match.group(1).strip().strip('"').strip("'")
            page_section = section
            section_match = re.search(r"^section\s*:\s*(.+)$", text, re.IGNORECASE | re.MULTILINE)
            if section_match:
                page_section = section_match.group(1).strip().strip('"').strip("'")
            source_url = ""
            url_match = re.search(r"^(?:source|url|sourceUrl)\s*[:=]\s*(\S+)", text,
                                  re.IGNORECASE | re.MULTILINE)
            if url_match:
                source_url = url_match.group(1).strip().strip('"').strip("'")
            topics.append({
                "id": stem,
                "section": page_section,
                "title": title[:1].upper() + title[1:],
                "file": f"pages/{page_name}",
                "sourceUrl": source_url,
            })

    if not topics:
        print("no crawled markdown found; nothing bundled", file=sys.stderr)
        return 1

    section_order = [
        "Start here",
        "Making a skin",
        "Extras and tools",
        "Troubleshooting",
        "Appendix: the format explained",
        "Original Skin Editor help",
        "Creating skins2 themes",
        "VLC user documentation",
    ]

    def sort_key(topic):
        section = topic["section"]
        section_index = section_order.index(section) if section in section_order else len(section_order)
        is_index = 0 if topic["id"].endswith("-index") else 1
        return (section_index, is_index, topic["title"].lower())

    topics.sort(key=sort_key)

    (TARGET / "toc.json").write_text(json.dumps(topics, indent=2), encoding="utf-8")

    files = ["toc.json"]
    files += [f"pages/{path.name}" for path in sorted(pages_dir.glob("*.md"))]
    files += [f"images/{path.name}" for path in sorted(images_dir.iterdir()) if path.is_file()]
    (TARGET / "files.txt").write_text("\n".join(files) + "\n", encoding="utf-8")
    (TARGET / "version.txt").write_text(digest.hexdigest()[:16] + "\n", encoding="utf-8")

    print(f"bundled {len(topics)} pages, {len(files) - 1 - len(topics)} images into {TARGET}")
    return 0


if __name__ == "__main__":
    sys.exit(main())
