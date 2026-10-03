---
title: Documentation viewer
section: Extras and tools
source: rewritten
---

# Documentation viewer

Help > Documentation, or F1, opens the bundled handbook in its own window. Everything except the Open online button works offline, because the pages and their diagrams ship inside the application.

## The window

The viewer has a search side on the left and the page on the right.

- The search field accepts a word or several words. Results are ranked and update as you type; pressing Enter searches immediately.
- The result list starts as the Topics list: every page with its title and section.
- A result cell names the page, the section path and the heading it lives under, the line and how many matches the page contains, plus a short snippet.
- Back and Forward move through the pages you visited in this window. They start disabled and enable as you navigate.
- Topics clears the search and returns to the full list.
- Open online opens the original web page for topics that came from VLC's sites. Handbook pages have no online original, so the button has nothing to open there.

The page pane renders the markdown, including tables, code blocks and images. The breadcrumb above it shows `section > title`, and the footer repeats the section and title on the left and the source URL on the right.

## How search works

- Every whitespace-separated term must appear somewhere in the page: title, section, headings or body.
- Pages are ranked by where the terms land: a title match weighs most, then a heading, then the section field, then each occurrence in the text.
- The viewer highlights the matching terms in the result list and in the opened page.
- Clicking a result opens the page; a relative link inside a page, such as the link from one chapter to the next, also opens inside the viewer. External links open in your browser.

## What is bundled

The viewer carries four collections, each with its own section group:

| Section | Content |
|---|---|
| Start here, Making a skin, Extras and tools, Troubleshooting | This handbook, written for VLC Skin Studio. |
| Appendix: the format explained | The reference pages: the element and attribute definitions, theme structure, layout and anchor rules, the action and expression catalogs. |
| Original help | The crawled help pages of the original VLC Skin Editor, element by element. |
| Creating skins | Olivier Teuliere's original skins2 creation guide. |
| VLC docs | The relevant pages of VLC's own user documentation. |

The three legacy groups are kept as a searchable archive. They document the same format from older angles and are useful when a term does not appear in this handbook; the language is the original authors' and their screenshots show the old editor.

The handbook section of each page comes from its front matter, so the viewer groups pages exactly the way the index does. If the bundle is ever missing from a build, the viewer says The documentation bundle is not part of this build.

Next: [MCP for automation](mcp-for-automation.md).
