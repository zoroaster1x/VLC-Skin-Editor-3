# DESIGN.md

Design direction for VLC Skin Studio. This file is the soul the UI rules are
applied on top of; read it with the antislop filter.

## What it is

A creative desktop tool for people who build VLC skins: they spend hours with
windows open, dragging small controls, comparing previews. It is used by
enthusiasts, theme authors and, now, AI agents. It is not a landing page and
not a dashboard: it is a workshop.

## Identity

* The accent is **VLC orange** (`#E06C38`), the color of the cone. It marks the
  active tab, focus rings, the dirty indicator and selection on the canvas.
  Blue is not used as an accent anywhere; blue only appears when a user's own
  skin art brings it.
* The surrounding UI is a neutral graphite (dark) or paper (light) so the
  user's skin art is the only strong color on screen. The preview is the hero.
* Panels are rounded (10 px controls, 12 px buttons), flat, and separated by
  space and hairlines instead of borders and shadows. Shadows are reserved for
  floating dock panels and dialogs, which genuinely float above the window.

## Typography

System UI font (Segoe UI / Noto Sans / SF) at default sizes, bold for panel
titles and section labels. Monospace only in the XML editor, where alignment
matters. There is no decorative typeface: the app should feel native next to
the user's IDE.

## Dials

**ENERGY 2 / RHYTHM 2 / MOTION 1.**

* Energy 2: a real toolbar, real menus, a dense three tree layout; confident
  but not loud.
* Rhythm 2: the layout is deliberately uneven, a narrow tool rail on the left,
  a wide canvas, a properties column on the right, a problems strip below.
* Motion 1: hover states, focus rings and a live canvas. No decorative
  animation. The skin preview itself may animate in VLC, not in the editor.

## Decisions and their reasons

* **Dockable panels** because theme work alternates between the item tree, the
  canvas and properties; users must be able to put them where their screen
  allows.
* **Canvas checkerboard off by default in PNG exports, on in the editor** so
  transparency is visible while editing but a saved preview composites onto
  whatever the author needs.
* **Inspector commits through the undo stack** because a properties panel that
  cannot be undone is a trap.
* **Hand drawn vector icons** so the tool has no borrowed icon-set look and the
  icons follow the theme at paint time.
* **Empty state is a start card, not a blank canvas**: new, open and the two
  generated examples are one click away.
* **Problems panel next to the canvas** because validation feedback belongs
  where the work is, not in a modal.

## Anti-slop notes

No gradients, no glow, no glassmorphism, no capsule badges, no fake stats, no
AI sparkle icon (the AI panel uses a speech bubble). The word "seamless" does
not appear in the UI. Numbers shown in the UI are real: sizes, counts, zoom,
positions.
