# Notes

Crawl of the skins and add-ons part of the modern VLC user documentation.
4 pages, 3 images, 1 dead external link, no missing images.

## Crawl scope and method

Start page: <https://vlc-user-documentation.readthedocs.io/en/latest/addons/skins.html>

Pages crawled (4):

| Page | Local file | Why it was crawled |
| --- | --- | --- |
| addons/skins.html | [addons-skins.md](addons-skins.md) | Start page |
| addons/extensions.html | [addons-extensions.md](addons-extensions.md) | Previous link of the start page, and Add-ons toctree sibling |
| addons/index.html | [addons-index.md](addons-index.md) | Add-ons toctree parent, contains the links to all skins and extension sections |
| support/index.html | [support-index.md](support-index.md) | Next link of the start page |

Not crawled, on purpose: support/faq/index.html and support/gethelp.html are linked from
support/index.html but are not about skins or add-ons. Links to them stay absolute. The
top level sections (Getting Started, User Guides, Settings, Tips and Tricks, Glossary) were
not in scope either. No page below addons/ other than the three above exists in this tree.

Method: curl for fetching pages and images, python3 standard library (html.parser) for
parsing and conversion. No browser, no external packages. Raw HTML files and the image
manifest were working files and were removed from the output folder after conversion.

The source is the Read the Docs "latest" build of VLC User Documentation 1.0.0 (Sphinx,
copyright 2019, revision 6bc77f9e). The content is old relative to current VLC releases.

## Structure

- addons/index.html -> addons-index.md: "Add-ons" H1, one note admonition, a five item
  list about add-ons in general, and a toctree listing Extensions and Skins with their
  section links.
- addons/extensions.html -> addons-extensions.md: H1 plus 3 H2 sections ("Installing
  Extensions from our website", "Installing Extensions diretly from VLC", "Installing
  Extensions in lua"), one note admonition, two ordered lists, one image.
- addons/skins.html -> addons-skins.md: H1 plus 3 H2 sections ("How to use skins",
  "How to create your own skin", "Troubleshooting"), two images, two note admonitions,
  one bullet list with the install paths per platform.
- support/index.html -> support-index.md: H1, one intro sentence, a two item toctree.

Conversion details: admonitions became bold labelled paragraphs ("**Note**"), Sphinx
headerlink anchors were dropped, guilabel and menuselection spans were flattened to plain
text, file paths were kept as inline code, and all links between crawled pages became
relative .md links (8 of them, including the section anchors) while everything else stays
absolute. The pages contain no pipe tables and no code blocks; the converter supports both
but they were not needed.

## Images

3 unique images, all downloaded successfully into images/ with their original names:

| File | Size (bytes) | Dimensions | Referenced by |
| --- | --- | --- | --- |
| skins.PNG | 43031 | 790x736 | addons-skins.md |
| choose_skins.PNG | 38067 | 797x736 | addons-skins.md |
| plugins&extensions.PNG | 16651 | 535x498 | addons-extensions.md |

Missing images: none. Every img src in the four article bodies returned HTTP 200. The
ampersand in plugins&extensions.PNG is kept in the file name and in the markdown
reference; CommonMark treats it literally because no valid entity ends there. Theme assets
(favicon, Read the Docs toolbar icons) were not downloaded because they are not page
content.

## Dead links

Checked 2026-10-03 with curl, following redirects.

- Broken: <https://www.videolan.org/vlc/skins_upload.php> returns 404. This is the "upload"
  link on addons-skins.md used to submit a newly created skin. The page no longer exists
  on videolan.org. All other outbound links returned 200:
  - <https://www.videolan.org/vlc/skins.php>
  - <https://www.videolan.org/vlc/download-skins2-go.php?url=vlc-skins.zip>
  - <https://www.videolan.org/vlc/skineditor.html>
  - <https://www.videolan.org/vlc/skins2-create.html>
  - <https://forum.videolan.org/viewforum.php?f=15>
  - <https://addons.videolan.org/> and <http://addons.videolan.org> (redirects to https)
  - <https://www.lua.org>
  - <https://vlc-user-documentation.readthedocs.io/en/latest/support/faq/index.html>
  - <https://vlc-user-documentation.readthedocs.io/en/latest/support/gethelp.html>
- Internal: every link to addons-*.md resolves to a file in this folder. No broken
  internal links.

## Notable content for a skins editor

- Skin packages are .VLT files, usually distributed inside zip archives. Bulk download:
  the vlc-skins.zip archive linked from addons-skins.md. Individual skins and a gallery:
  the skins website.
- Install paths:
  - Windows: C:\Program Files\VideoLAN\VLC\skins
  - Linux/Unix: ~/.local/share/vlc/skins2
  - macOS: skins are not supported. This is one bullet in a list and easy to miss.
- Enabling a skin: Tools > Preferences > Interface, switch the look and feel from
  "Use native style" to "Use custom skin", then use Choose to select the .VLT file.
  VLC must be restarted before the change is visible.
- Troubleshooting: a skin that renders incorrectly may simply be broken; switch back to
  "Use native style" and restart VLC.
- Authoring: the VLC Skin Editor (skineditor.html) creates skins without deep knowledge of
  the skin system. The Skins2 documentation (skins2-create.html) is the deeper reference
  for how skins are built. Questions go to the skins forum (viewforum.php?f=15).
- Submitting a finished skin: the user documentation points at the upload form, which is
  now a 404. Use the forum or the skins website instead.
- Coverage gap: this documentation tree contains no skins2 XML details, no .VLT internals,
  no editor walkthrough and no theme API reference. For a skins editor the useful page is
  addons-skins.md; everything deeper lives on the external Skins2 documentation page, not
  in this documentation.
- Faithful imperfections kept from the source: "diretly" in an Extensions heading and in
  its anchor, "After youve", and smaller typos ("withe", "avaliable", "infromal", "upto")
  in Extensions and Support.
- Staleness: the docs date from 2019 and are labeled version 1.0.0. The paths and the
  Preferences UI reflect VLC 3.x. Verify paths and labels against the current VLC before
  relying on them, especially if VLC 4.x is in use.
