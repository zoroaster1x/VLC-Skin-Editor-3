# Notes on the skinedhlp crawl

Crawled on 2026-10-03 from https://images.videolan.org/vlc/skinedhlp/ with curl. HTML was parsed and converted with a Python 3 standard library script (crawl.py in this folder).

## Counts

- Pages crawled: 25
- Images downloaded: 23
- Dead in-scope HTML links: 1
- Images that failed to download: 0

## Navigation structure

- The site is flat: all pages live directly in /vlc/skinedhlp/ and link to each other with plain file names.
- Every content page begins with a breadcrumb line (converted from div.header) that ends at index.html.
- index.html groups the pages into three sections: Tutorials, Documentation (with Resources and Items subsections) and Links (external).
- All internal links were rewritten to relative .md files. All external links stay absolute.
- index.html shows the SliderBackground Generator and preview tutorial links with a strikethrough (s tags). sbgwizard.html still exists and is crawled, preview.html is dead (see below).

## Pages discovered

- VLC Skin Editor - Online Help (https://images.videolan.org/vlc/skinedhlp/index.html) (entry page)
- The basics of creating a skin (https://images.videolan.org/vlc/skinedhlp/basics.html)
- How to create a resizable window (https://images.videolan.org/vlc/skinedhlp/resizable.html)
- How to use the SliderBackground Generator (https://images.videolan.org/vlc/skinedhlp/sbgwizard.html)
- Theme settings (https://images.videolan.org/vlc/skinedhlp/theme.html)
- Bitmap (https://images.videolan.org/vlc/skinedhlp/res-bitmap.html)
- SubBitmap (https://images.videolan.org/vlc/skinedhlp/res-subbitmap.html)
- Font (https://images.videolan.org/vlc/skinedhlp/res-font.html)
- Window (https://images.videolan.org/vlc/skinedhlp/window.html)
- Layout (https://images.videolan.org/vlc/skinedhlp/layout.html)
- Anchor (https://images.videolan.org/vlc/skinedhlp/i-anchor.html)
- Button (https://images.videolan.org/vlc/skinedhlp/i-button.html)
- Checkbox (https://images.videolan.org/vlc/skinedhlp/i-checkbox.html)
- Group (https://images.videolan.org/vlc/skinedhlp/i-group.html)
- Image (https://images.videolan.org/vlc/skinedhlp/i-image.html)
- Panel (https://images.videolan.org/vlc/skinedhlp/i-panel.html)
- Playtree (https://images.videolan.org/vlc/skinedhlp/i-playtree.html)
- Slider (https://images.videolan.org/vlc/skinedhlp/i-slider.html)
- Slider Background (https://images.videolan.org/vlc/skinedhlp/i-sliderbg.html)
- Text (https://images.videolan.org/vlc/skinedhlp/i-text.html)
- Video (https://images.videolan.org/vlc/skinedhlp/i-video.html)
- Text Variables (https://images.videolan.org/vlc/skinedhlp/textvars.html)
- Percentage variables (https://images.videolan.org/vlc/skinedhlp/percent.html)
- Boolean expressions (https://images.videolan.org/vlc/skinedhlp/boolexpr.html)
- Bezier curves (https://images.videolan.org/vlc/skinedhlp/bezier.html)

## Stub or placeholder pages

- https://images.videolan.org/vlc/skinedhlp/bezier.html contains only the plain text "YET TO BE WRITTEN" and no navigation or markup.

## Dead or missing pages

- https://images.videolan.org/vlc/skinedhlp/preview.html returned HTTP 404 (content type text/html). Linked from: https://images.videolan.org/vlc/skinedhlp/index.html

## Malformed markup in the original

- The two Links entries at the bottom of index.html use unquoted href attributes followed by a stray double quote (href=//www.videolan.org/vlc/skins2-create.html">). The stray quote was stripped during conversion; both targets return HTTP 200.
- index.html leaves the Items list item unclosed, so in the raw HTML the Text variables, Percentage variables, Boolean expressions and Bezier curves items sit inside it. Browsers treat them as siblings at the Documentation level, and implied list item closing was applied so the markdown matches the browser rendering.
- Several item pages leave p elements unclosed (for example i-slider.html places an h3 inside a p). The converter renders blocks independently, which preserves the reading order.

## Broken images

- None. Every image referenced by the crawled pages downloaded successfully.

## Image handling

- All images are in images/ under their original file names.
- No file name collisions occurred, so no page prefixes were needed.
- Images referenced by more than one page are stored once: 3 such images.
- style.css (shared by all pages, not stored) references these background images, which were downloaded even though no img tag uses them:
  - https://images.videolan.org/images/skinedhlp/bg.png -> images/bg.png

## External links (left absolute)

- http://www.videolan.org/vlc/skins_upload.php (from https://images.videolan.org/vlc/skinedhlp/basics.html)
- https://forum.videolan.org/memberlist.php?mode=viewprofile&u=55010 (from https://images.videolan.org/vlc/skinedhlp/index.html)
- https://forum.videolan.org/viewforum.php?f=15 (from https://images.videolan.org/vlc/skinedhlp/index.html)
- https://www.videolan.org/vlc/skins.html (from https://images.videolan.org/vlc/skinedhlp/index.html)
- https://www.videolan.org/vlc/skins2-create.html (from https://images.videolan.org/vlc/skinedhlp/index.html)

## Conversion conventions

- Each markdown file starts with YAML front matter: title, source URL, crawled date (2026-10-03).
- index.md contains both the converted index page and the site table of contents, because the task asked for the converted index.html and for a TOC under the same file name.
- HTML dl/dd definition lists (used on the basics page for indentation) became nested bullet lists. dt terms would become bold lines.
- br became an inline <br> tag so line breaks survive markdown cleanup.
- Tables (none on this site at crawl time) would become pipe tables with the first row used as header.
- pre blocks (none on this site at crawl time) would become fenced code blocks.
- Links to crawled pages are relative .md links; fragments are preserved; everything else stays absolute.
- Images that downloaded successfully are relative links under images/; any that failed keep their absolute URL.
- Values of unquoted HTML attributes that carried a stray trailing quote (malformed markup on index.html) were repaired before URL resolution.
- Named anchors (a name) on the resizable tutorial were kept as inline <a id="..."></a> tags so the fragment links inside it still resolve.
