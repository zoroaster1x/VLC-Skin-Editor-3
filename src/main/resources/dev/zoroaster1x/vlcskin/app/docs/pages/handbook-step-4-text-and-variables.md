---
title: Step 4: text and variables
section: Making a skin
source: rewritten
---

# Step 4: text and variables

Text items are the clock, the title and the status line of a skin. They draw one line in a font resource and expand `$` variables to live playback values. The Variables panel is the fake player you test them against.

## Add a Text item

Add Text from the Items panel, then fill in the Inspector:

| Field | Meaning |
|---|---|
| Text | The string to draw, `$` variables included. |
| Font | A font resource id, or `defaultfont` for the built in FreeSans 12. |
| Color | `#RRGGBB`. |
| Width | Fixed width in pixels; `0` lets the text decide its own width. |
| Alignment | `left`, `center` or `right`. Centering and right alignment only take effect with a fixed width. |
| Scrolling | What happens when the text is wider than the width: `none` clips it, `auto` scrolls by itself, `manual` scrolls only while the user drags it. |

Typical first text: set Text to `$N` for the stream title, or `$T` for the time, and watch the canvas as you type.

## Add a font resource

1. Save the theme first; font paths are stored relative to the theme file.
2. In the Resources panel press Add font, or right-click and pick Add font. The chooser takes `.ttf` and `.otf`; several files can be selected at once.
3. Select the font in the tree and set its ID and Size in the Inspector. Choose TTF or OTF reopens the file chooser.

`defaultfont` is built in and needs no file. A missing or unreadable font file falls back to Sans Serif at the declared size so the preview keeps drawing; the validator reports the missing file as a warning. A BitmapFont resource loads a Winamp style glyph sheet instead; its Type field is `digits` or `text`, and the preview draws it with a system font, so judge the real look in VLC.

## The Variables panel

The Variables panel simulates the player state. Nothing here touches VLC; it only changes what the canvas draws. Every change repaints the preview immediately.

- Slider position: one slider from 0 to 100, starting at 50. It moves every slider thumb in the preview.
- The sixteen booleans: Equalizer enabled, Video output present, Has audio, Fullscreen, Playing, Stopped, Paused, Seekable, Mute, Always on top, Can record, Recording, Random, Loop, Repeat and DVD active. Their defaults are shown checked or unchecked in the panel; the tooltip on each box holds the code name.
- The thirteen text variables: each row is the token, its label and a sample value you can edit.

Use Playing and Paused to test a play or pause button, Mute for a speaker toggle, and the slider position to walk a seek slider along its path.

## The $ tokens

These are the tokens VLC substitutes in a Text item or a tooltip. The editor preview substitutes the same tokens with the sample values from the Variables panel.

| Token | Meaning | Editor sample |
|---|---|---|
| `$B` | Audio stream bitrate in kb/s. | `128` |
| `$V` | Volume in percent. | `50` |
| `$T` | Current playback time, `H:MM:SS`. | `0:55:55` |
| `$t` | Current time, hours only when non zero. | `55:55` |
| `$L` | Remaining time, `H:MM:SS`. | `0:44:44` |
| `$l` | Remaining time, hours only when non zero. | `44:44` |
| `$D` | Duration of the stream, `H:MM:SS`. | `0:99:99` |
| `$d` | Duration, hours only when non zero. | `99:99` |
| `$H` | Help text of the item under the pointer. | `Help text` |
| `$N` | Name of the playing stream. | `Artist - Title` |
| `$F` | Full name with path. | `http://www.example.com/Artist - Title.mp3` |
| `$S` | Audio sample rate in kHz. | `44` |
| `$R` | Playback rate. | `1` |

Substitution is a plain text replacement for each known token, never a regular expression. A `$` that does not start a known token stays as typed.

## Boolean fields

Visible on any item and State on a checkbox accept boolean expressions, not just `true` and `false`:

```
(not vlc.isPlaying) or vlc.isMute
```

The grammar is `not`, `and`, `or` and parentheses, with names such as `vlc.isPlaying`, `playlist.isRepeat` and `equalizer.isEnabled`. A name the editor does not know evaluates to false, so a typo hides the item rather than breaking the theme. The Variables panel switches the sixteen names the preview understands.

The full Text attribute table and the tooltip rules are in [Text items](text-items.md); the expression and action reference is in [Actions and variables](actions-and-variables.md).

Next: [Step 5: sliders and backgrounds](step-5-sliders-and-backgrounds.md).
