---
title: Step 5: sliders and backgrounds
section: Making a skin
source: rewritten
---

# Step 5: sliders and backgrounds

A slider reads and writes a player value and draws its thumb somewhere along a path. The path is a bezier curve made of control points, and the editor's Path tool is how you shape it on the canvas.

## Add a slider

Add Slider from the Items panel. The Inspector has a Slider section:

| Field | Meaning |
|---|---|
| Thumb image | The `up` sprite. Required for the slider to draw. |
| Hover image, Clicked image | The `over` and `down` sprites. Empty states fall back to the thumb. |
| Points | The path control points, for example `(0,0),(272,0)`. |
| Thickness | How many pixels from the path still count as a click on the slider. |
| Value | Which player value it drives: `time`, `volume`, `equalizer.preamp` or `equalizer.band(0)` to `equalizer.band(9)`. |
| Tooltip | Text shown while the pointer rests on the slider. |
| Edit path on canvas | A hint button that points you at the Path tool. |

## Edit the path on the canvas

1. Select the slider, either on the canvas or in the Items tree.
2. Switch to the Path tool, from the View menu or the toolbar.
3. The path is drawn with yellow control points. Drag one to move it; the Points field updates as you drag, and the drag lands as one undo step.
4. Shift-click adds a point at the pointer. Alt-click removes the point under the pointer. A path keeps at least two points.
5. Switch back to the Move tool when you are done.

A two point path is a straight line, which is what seek and volume sliders normally use. Three points bend it into a curve, for example a volume slider that follows a circle. The curve is stored as control points, not as samples; VLC and the editor sample the same bezier, so the thumb positions match. The format details are in [Sliders](sliders.md).

## What the slider value does

The Value field names the variable VLC binds the slider to. It is not a stored position:

| Value | What the user changes |
|---|---|
| `time` | Playback position in the current stream. |
| `volume` | Volume. |
| `equalizer.preamp` | Equalizer preamplification. |
| `equalizer.band(0)` to `equalizer.band(9)` | One equalizer band, from 60 Hz to 16 kHz. |

The first path point is 0 percent and the last is 100 percent. A slider inside a Playlist or Playtree has no Value choice; the Inspector shows `Playtree scrolling` with a note, because its thumb always follows the list scroll position.

To see the thumb move before testing in VLC, drag Slider position in the Variables panel. Every slider in the preview follows that one value, which starts at 50.

## The Slider Background Generator

A slider background is one bitmap cut into a grid of frames, one frame per fill level. The wizard builds that bitmap from a few pieces.

1. Select the slider and find the Slider background section in the Inspector.
2. Tick Enabled. The Image field appears set to `none`.
3. Press Generate background strip... to open the wizard.
4. Fill in the pieces:

| Input | Meaning |
|---|---|
| Direction | Left to right for a horizontal slider, Bottom to top for a vertical one. |
| Width / height | The size of the track. |
| Left / right margin, Top / bottom margin | Space kept inside each frame. |
| Background | Optional image behind the track, tiled or stretched. |
| Start edge | Optional image at the start of the fill. |
| Middle (required) | The fill body. The wizard refuses to run without it. |
| End edge | Optional image at the end of the fill. |
| Overlay | Optional image on top of every frame. |
| Tile background, Tile middle | Repeat those images instead of stretching them. |

5. Press Generate and use. The strip is written next to the theme as `<id>_bg.png`, a bitmap resource is registered for it, and the grid fields are set: a horizontal slider gets one column and one row per frame, a vertical slider gets one row and one column per frame, with both paddings zero. Save the theme first so the PNG lands beside it and the resource is registered in the same step.

## The frame grid by hand

If you paint the strip yourself, set the Slider background fields directly:

| Field | Meaning |
|---|---|
| Image | The bitmap holding the whole grid. |
| Horizontal frames, Vertical frames | How many frames across and down. |
| Horizontal padding, Vertical padding | Unused pixels between frames. |

The grid holds `nbhoriz * nbvert` frames, filled left to right and then top to bottom. VLC picks frame `floor(fields * value)`, so frame 0 is empty and the last frame is full. The counting rules and worked examples are in [Slider backgrounds](slider-backgrounds.md).

## Radial sliders

RadialSlider is the knob version. Its Inspector takes a Sequence bitmap (the knob frames stacked vertically), the number of Images, a Minimum angle and a Maximum angle in degrees, and the same Value list. The preview selects the frame with `floor(value * nbimages)`.

Next: [Step 6: animations and playlists](step-6-animations-and-playlists.md).
