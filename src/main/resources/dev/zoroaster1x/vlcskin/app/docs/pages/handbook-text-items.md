---
title: Text items
section: Making a skin
source: rewritten
---

# Text items

A `Text` item draws one line of text in a font resource. The text can contain variables that VLC replaces with live playback information.

## In the editor

Add Text from the Items panel and fill in the Inspector's Text section: the text itself, the Font, Color, Width, Alignment and Scrolling. Fonts come from the Resources panel's Add font; the built in `defaultfont` needs no file. The Variables panel replaces every `$` token with a sample value in the preview, so you can type `$T` and watch the clock. The walkthrough is in [Step 4: text and variables](step-4-text-and-variables.md).

## Attributes

| Attribute | Default | Meaning |
|---|---|---|
| `text` | empty | The text to draw, including any `$` variables. |
| `font` | required | Font or bitmap font resource id. `defaultfont` uses the built in font. |
| `color` | `#000000` | Text color in `#RRGGBB` form. |
| `width` | `0` | Width in pixels. `0` computes the width from the rendered text. |
| `alignment` | `left` | `left`, `center` or `right`. Centering and right alignment need a fixed `width` to have an effect. |
| `scrolling` | `auto` | What happens when the text is wider than `width`. |
| `focus` | `true` | When false, the control does not take mouse focus, so clicks pass to whatever is behind it. Useful for a title that sits on a draggable background. |
| `help` | empty | Help text exposed through `$H`. |

### Scrolling

| Value | Behaviour |
|---|---|
| `auto` | The text starts scrolling by itself when it does not fit. The user can drag it and click to stop or start the movement. |
| `manual` | The text scrolls only while the user drags it. |
| `none` | No scrolling; the text is clipped. |

## Text variables

A variable is a `$` followed by a letter. VLC replaces it wherever it appears in a `Text` item or in a tooltip attribute. The editor preview replaces the same tokens with the sample values you set in the Variables panel.

| Token | Meaning | Editor sample |
|---|---|---|
| `$B` | Audio stream bitrate in kb/s. | `128` |
| `$V` | Volume in percent. | `50` |
| `$T` | Current playback time, `H:MM:SS`. | `0:55:55` |
| `$t` | Current time, hours shown only when non zero. | `55:55` |
| `$L` | Remaining time, `H:MM:SS`. | `0:44:44` |
| `$l` | Remaining time, hours shown only when non zero. | `44:44` |
| `$D` | Duration of the stream, `H:MM:SS`. | `0:99:99` |
| `$d` | Duration, hours shown only when non zero. | `99:99` |
| `$H` | Help text of the control under the pointer. | `Help text` |
| `$N` | Name of the playing stream. | `Artist - Title` |
| `$F` | Full name with path. | `http://www.example.com/Artist - Title.mp3` |
| `$S` | Audio sample rate in kHz. | `44` |

VLC 2.0 also documents `$R` for the playback rate. The editor keeps it in the text and VLC substitutes it at runtime, but the preview has no sample value for it.

Substitution is a plain text replacement for each known token, never a regular expression. A `$` that does not start a known token stays as typed, so unknown tokens show literally.

## Help and tooltips

Every item accepts `help`. While the pointer is over that item, VLC sets `$H` in any text item to the help string, which is the usual way to build a status line. Variables inside the help string are expanded too, except `$H` itself, to avoid a loop.

`tooltiptext` on buttons, checkboxes, sliders and radial sliders draws a tooltip near the pointer. The theme attribute `tooltipfont` selects the font or bitmap font VLC uses for all tooltips in the theme; it defaults to `defaultfont`.

The editor preview does not draw tooltips; it shows the text variables in the layout and updates them as you change the Variables panel.

Next: [Sliders](sliders.md).
