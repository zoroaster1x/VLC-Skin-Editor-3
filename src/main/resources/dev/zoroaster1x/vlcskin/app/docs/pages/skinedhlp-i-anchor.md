---
title: "Anchor"
source: https://images.videolan.org/vlc/skinedhlp/i-anchor.html
crawled: 2026-10-03
---

[VLC Skin Editor - Online Help](index.md) > Documentation > Items > Anchor

# Anchor

Creates a "magnetic point" (or curve) in a layout. If an Anchor of another window enters into the range of action of this Anchor, the 2 anchors will automatically stick to each other, anchoring the two windows together. Each Anchor has a priority and the Anchor with the highest priority is the "winner", which means that when moving its window all the other anchored windows will move too. To break the effect of 2 anchored windows you need to move the window whose Anchor has the lower priority.

### Attributes

**x and y**<br>
 The coordinates of the anchor in the Layout.

**lefttop**<br>
 Indicates to which corner of the Layout the top-left-hand corner of this Anchor is attached to, in case of resizing. Possible values are 'lefttop', 'leftbottom', 'righttop' and 'rightbottom'.<br>
 Note that there is no "rightbottom" attribute for the Anchors (contrarily to normal controls), because an Anchor is not resizable (even when the anchor is a Bezier curve).

**priority**<br>
 Priority of the Anchor. When the Anchor is moved, attatched Anchors with a lower priority will be moved along. All attachments to Anchors of a higher priority will be dropped.

**points**<br>
 Points defining the Bezier curve followed by the anchor. If you just want a punctual Anchor, like it is used in most cases, set the value to *(0,0)*.

**range**<br>
 The range of effect of the anchor in pixels. Other Anchors inside the range of effect will be attached to the Anchor.

**See also:**<br>
 [Bezier curves](bezier.md)

© 2008 All rights reserved to the VideoLAN team.
