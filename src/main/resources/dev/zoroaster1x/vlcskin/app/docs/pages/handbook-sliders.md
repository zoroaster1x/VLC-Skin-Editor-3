---
title: Sliders
section: Making a skin
source: rewritten
---

# Sliders

A slider reads and writes a percentage variable and draws its thumb somewhere along a bezier path. That path can be a straight line, a circle, or any curve you can express as control points.

## In the editor

Add Slider from the Items panel. The Inspector's Slider section takes the thumb images, the path points and thickness, the value variable and a tooltip. The button Edit path on canvas points you at the Path tool: select the slider, switch to Path in the toolbar or the View menu, then drag the yellow control points. Shift-click adds a point, Alt-click removes one, and every drag is one undo step. Drag Slider position in the Variables panel to see the thumb travel the path before VLC is involved. The full walkthrough is in [Step 5: sliders and backgrounds](step-5-sliders-and-backgrounds.md).

## Attributes

| Attribute | Default | Meaning |
|---|---|---|
| `up` | required | Thumb bitmap in the normal state. |
| `over`, `down` | `none` | Thumb bitmap while hovered or pressed. Missing states fall back to `up`. |
| `points` | required | Control points of the thumb path, for example `(0,0),(50,100),(100,0)`. |
| `thickness` | `10` | Thickness of the path in pixels. VLC uses it to decide whether the pointer is close enough to count as a slider click. |
| `value` | `none` | The percentage variable this slider controls. |
| `background` | `none` | Optional `SliderBackground` child. See [Slider backgrounds](slider-backgrounds.md). |
| `width`, `height` | `-1` | Override the size VLC computes from the curve. `-1` uses the curve size. |
| `tooltiptext` | empty | Tooltip. `$` variables are substituted. |

## The value variable

`value` names one of four percentage variables. The first point of the path is 0 percent and the last is 100 percent.

| Variable | Controls |
|---|---|
| `time` | Playback position in the current stream. |
| `volume` | Volume. |
| `equalizer.band(n)` | One equalizer band, where `n` is `0` to `9`. 0 percent is -20 dB and 100 percent is +20 dB. |
| `equalizer.preamp` | Equalizer preamplification, same range as the bands. |

The ten bands, in order, are 60 Hz, 170 Hz, 310 Hz, 600 Hz, 1 kHz, 3 kHz, 6 kHz, 12 kHz, 14 kHz and 16 kHz.

A slider inside a `Playlist` or `Playtree` does not use a percentage variable; it is driven by the playlist scroll position. The editor does not set a value for one, and a value carried by an old file is preserved as written. See [Playlists and playtrees](playlists-and-playtrees.md).

## Points and the bezier path

`points` is a comma separated list of `(x,y)` pairs, for example `(2,50),(45,120),(88,50)`. Coordinates are relative to the top left corner of the control.

VLC interprets the list as the control points of a bezier curve of degree `n-1`, where `n` is the number of points. It samples the curve at 1024 percentages, rounds each sampled coordinate the way C's `lrintf` does (round half to even), and keeps only the percentages where the rounded pixel changed. A lookup asks for the stored sample nearest the wanted percentage, so drawing and hit testing never evaluate a polynomial at runtime. The editor implements the same algorithm, which is why its thumb positions match VLC's.

A single point such as `(0,0)` is valid; it pins the thumb to one place, which is occasionally useful for a control that only reads a value.

![Slider path](images/handbook-slider-path.png)

## Seeing the thumb move

The XML `value` attribute names the variable VLC binds the slider to; it is not a stored position. The editor preview positions the thumb with a simulated value instead, the single slider position in the Variables panel. It starts at 50 percent. Drag the slider there, or set `sliderValue` between 0 and 1 with the MCP tool `set_variables`, and every slider thumb in the preview follows. This is the quickest way to verify a path before testing in VLC.

## Radial sliders

A radial slider is a knob built from a vertical strip of images.

| Attribute | Default | Meaning |
|---|---|---|
| `sequence` | required | Bitmap whose frames are the knob at successive positions, stacked vertically. |
| `nbimages` | required | Number of frames in the sequence. |
| `minangle` | `0` | Angle in degrees corresponding to 0 percent. |
| `maxangle` | `360` | Angle in degrees corresponding to 100 percent. |
| `value` | `none` | Percentage variable, as for a slider. |

The Inspector edits all five fields: Sequence bitmap, Images, Minimum angle, Maximum angle and Value. The preview selects the frame with `floor(value * nbimages)`, clamped to the strip. VLC uses the same sequence and maps the value between the two angles, which controls how the knob turns on screen.

The validator reports a missing `up` or background image as an error, a non positive `thickness` as a warning, and malformed `points` as an error. None of these stop VLC from loading the theme, but they usually mean something will not draw or click correctly.

Next: [Slider backgrounds](slider-backgrounds.md).
