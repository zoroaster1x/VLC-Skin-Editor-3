# Notes on the crawl of the VLC skins2 creation guide

Crawl date: 2026-10-03
Start URL: https://images.videolan.org/vlc/skins2-create.html

## Scope rule applied

Followed every link found on the start page that lives under
images.videolan.org/vlc/ and whose file name starts with `skins2` or
`skineditor`, plus in-page anchors. The start page contains no such link,
so the crawl holds a single page. No other part of the VLC site was
followed or downloaded.

## Pages crawled (1)

| Markdown file | Title | Source URL | HTML bytes | Markdown bytes |
| --- | --- | --- | --- | --- |
| skins2-create.md | HowTo create your own skin | https://images.videolan.org/vlc/skins2-create.html | 92775 | 55176 |

## Structure of the guide

Single self-contained DocBook article. Author: Olivier Teulière. Copyright
2004-2013 the VideoLAN project. License: GNU GPL v2 or later.

Rendered shape: 1 article title, 11 first-level sections, 27 second-level
sections, 139 attribute subsections, 10 note boxes, 141 list items (114 at the
top level), 180 named anchors, 195 occurrences of in-page anchor links, and
6 outbound links.

First-level sections: Basic principles, The bitmaps, The XML file, Actions,
Text variables, Boolean expressions, Percentage variables, Layout model,
Compression, Bezier curves, Tools and advice.

## Images

There are none. The source HTML has no `img` element, no CSS or inline style
image reference, no inline SVG, and no mention of any image file extension, so
images/ is empty: 0 files, 0 bytes. Nothing was skipped or failed.

## Outbound links (status checked 2026-10-03)

| Link | Status | Note |
| --- | --- | --- |
| http://www.gnu.org/copyleft/gpl.html | 200 | redirects to https://www.gnu.org/licenses/gpl-3.0.html |
| http://git.videolan.org/gitweb.cgi?p=vlc.git;a=blob;f=share/skins2/skin.dtd;hb=HEAD | 404 | dead; the DTD now lives at https://code.videolan.org/videolan/vlc/-/raw/master/share/skins2/skin.dtd (verified 200) |
| http://astronomy.swin.edu.au/~pbourke/curves/bezier/ | no response | connection failed or timed out at crawl time |
| https://images.videolan.org/vlc/skins/VLC-curve-maker.exe | 404 | dead; the CurveMaker utility referenced by the guide is no longer hosted |
| http://wiki.videolan.org/DefaultSkinRequirements | 200 | redirects to https |
| http://forum.videolan.org/viewforum.php?f=15 | 200 | redirects to https |

## Matching pages that exist but are not linked from the start page

Not crawled, because the scope rule follows links and skins2-create.html does
not link any of these:

- https://images.videolan.org/vlc/skineditor.html (200, 22397 bytes, title
  "VLC media player - Skin Editor - VideoLAN"). Modern website page for the
  Skin Editor tool. It embeds three screenshots under
  images/screenshots/skineditor/ and links to `skinedhlp`. It does not link
  back to skins2-create.html.
- https://images.videolan.org/vlc/skinedhlp/ (200, title "VLC Skin Editor -
  Online Help"). The Skin Editor manual; its index states it is based in
  parts on Olivier Teulière's skin creation HowTo. Its directory name starts
  with "skined", which is neither the `skins2` nor the `skineditor` prefix.
  It is linked only from skineditor.html.
- Probed and not present (404): /vlc/skins2.html, skins2-config.html,
  skins2-setup.html, skins2-editor.html, skins2-theme.html, skins2-basics.html.
- https://www.videolan.org/vlc/skins2-create.html serves the identical
  92775-byte document, so images.videolan.org is a mirror of the same file.

## Files and reproducibility

```text
skins2-create/
  skins2-create.md          converted guide
  index.md                  page list and section index
  notes.md                  this file
  images/                   empty, the guide references no image
  raw/skins2-create.html    source copy fetched by the crawler
```

- Converter and crawler: /tmp/opencode/work-on-documentation/scripts/crawl_skins2_guide.py
  (Python 3 standard library only; no third-party packages, no headless browser).
- Crawl summary: /tmp/opencode/work-on-documentation/scripts/crawl_summary.json
- Probe copies for the unlinked sibling pages:
  /tmp/opencode/work-on-documentation/scratch/skins2-probes/
- raw/ is kept so the conversion can be diffed against the source.

## Conversion notes

- Every crawled page starts with YAML front matter: title, source, crawled.
- All 180 named anchors are kept as inline `<a id="..."></a>` elements next to
  their heading or paragraph, so all 195 in-page link occurrences resolve.
- External links are absolute. No link pointed to another crawled page, so the
  page body contains no relative `.md` link; index.md links to the page as
  `skins2-create.md`.
- The 10 DocBook note boxes render as `> ### Note` blockquotes; one of them
  sits inside a list item and is indented accordingly.
- Markdown special characters are escaped where the source text uses them
  literally: 2 asterisks, 9 underscores, 1 opening and 1 closing bracket. No
  other escapes were added.
- The source has no `pre`, `table`, `img`, `svg`, `script` or `iframe`
  elements. Fenced code and pipe table support exists in the converter but was
  not needed for this page.
- Text is written as UTF-8; accented characters and the copyright sign are
  preserved.

## Verification performed

- Anchor check: 64 distinct in-page targets, 0 broken.
- index.md: 177 section links, 0 broken.
- Content check: every word token from the HTML text (7464 tokens) appears in
  the Markdown.
- HTML leftovers in the Markdown: only the intended `<a id="..."></a>` tags.

## Failures

None. One HTML page fetched and converted; zero image downloads were attempted
because the page references none. The dead outbound links listed above are
content rot in the original guide, not crawl failures.
