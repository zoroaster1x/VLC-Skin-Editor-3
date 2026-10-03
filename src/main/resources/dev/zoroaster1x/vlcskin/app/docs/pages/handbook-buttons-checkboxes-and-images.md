---
title: Buttons, checkboxes and images
section: Making a skin
source: rewritten
---

# Buttons, checkboxes and images

These three elements share one input model: each draws a bitmap depending on the pointer state, and a click can run an action chain. The editor preview shows the sprite states while you interact on the canvas; the actions themselves run only inside VLC.

## In the editor

Add the control from the Items panel, select it, and work through the Inspector.

- A Button takes Normal image, Hover image and Clicked image, an Action and a Tooltip.
- A Checkbox takes a State expression and two sprite groups, State 1 and State 2, each with its own normal, hover and clicked images, action and tooltip.
- An Image takes a Bitmap, a Resize mode, a Click action, an Any action for the general action field, a Double click action and a Cover art checkbox.

The image fields are drop-downs of the bitmaps and sub bitmaps in the theme. The Action row has a small button that opens the [action editor](step-3-add-and-place-controls.md); you can also type a chain into the field.

Use the Variables panel to test the states before VLC ever sees the theme. For a play and pause button, set the State expression to `vlc.isPlaying` and tick Playing in the panel: state 2, the pause sprite, appears; untick it and the play sprite returns.

## Button

A button has three states: up, over and down.

| Attribute | Default | Meaning |
|---|---|---|
| `up` | required | Bitmap shown in the normal state. |
| `over` | `none` | Bitmap shown while the pointer is over the button. |
| `down` | `none` | Bitmap shown while the button is pressed. |
| `action` | `none` | Action chain run on click. See [Actions and variables](actions-and-variables.md). |
| `tooltiptext` | empty | Tooltip text. The `$` variables are substituted. |
| `help` | empty | Help text exposed through the `$H` variable. |

When `over` or `down` is `none`, the button falls back to `up` for that state, so a one-sprite button is valid. Any sprite can point at a bitmap with several frames, and the preview animates it.

## Checkbox

A checkbox is a button with two states, each with its own sprite triple.

| Attribute | Default | Meaning |
|---|---|---|
| `state` | required | Boolean expression. False selects state 1, true selects state 2. |
| `up1`, `down1`, `over1` | up1 required, others `none` | Sprites for state 1. |
| `action1` | `none` | Action chain run when the box is clicked while it is in state 1, so it moves to state 2. |
| `tooltiptext1` | empty | Tooltip for state 1. |
| `up2`, `down2`, `over2` | up2 required, others `none` | Sprites for state 2. |
| `action2` | `none` | Action chain run when the box is clicked while it is in state 2, so it moves back to state 1. |
| `tooltiptext2` | empty | Tooltip for state 2. |

The classic example is a play and pause control. While playback is running, `state="vlc.isPlaying"` selects state 2, the pause sprite, and `action2` calls `vlc.pause()`. When VLC is paused the expression is false, state 1 shows the play sprite, and `action1` resumes.

## Image

An image is a single bitmap with optional click behaviour.

| Attribute | Default | Meaning |
|---|---|---|
| `image` | required | Bitmap or sub bitmap to draw. |
| `width`, `height` | `-1` | Override the bitmap size. `-1` uses the bitmap's own size. |
| `action` | `none` | Action on click. Also accepts the window actions `move`, `resizeE`, `resizeS` and `resizeSE`; see [Layouts and anchors](layouts-and-anchors.md). |
| `action2` | `none` | Action on double click. |
| `resize` | `mosaic` | How VLC resizes the image: `mosaic` tiles it, `scale` stretches it, `scale2` scales it while keeping the aspect ratio and leaves the leftover border transparent. |
| `art` | `false` | When true, VLC reuses the element to draw the cover art of the playing media. `scale2` is the usual companion. |

A background image with `action="move"` is how most themes let the user drag the window. The editor draws images at their intrinsic bitmap size, so the `width` and `height` overrides and the resize modes are VLC runtime behaviour; the validator warns when `resize` is none of the three known values.

## Visibility and tooltips

Every item in this group accepts the common `visible` attribute, which is a boolean expression rather than a plain flag. For example, `visible="not vlc.isPlaying"` hides the element unless playback is stopped. An expression that cannot be resolved evaluates to false in the editor preview.

The tooltip and help attributes accept the text variables listed in [Text items](text-items.md), so a button tooltip can read `Volume: $V%`. The editor preview does not draw tooltips; `$H` in a Text item still follows the item under the pointer.

Next: [Text items](text-items.md).
