---
title: "Slider"
source: https://images.videolan.org/vlc/skinedhlp/i-slider.html
crawled: 2026-10-03
---

[VLC Skin Editor - Online Help](index.md) > Documentation > Items > Slider

# Slider

A slider controls a certain percentage value of VLC, like the position in the current playback or the volume.<br>

### General Attributes

**ID**<br>
 A unique identifier for the item.

**Visibility**<br>
 A [boolean expression](boolexpr.md) indicating whether the item is shown or hidden.<br>
 *XML-attribute name: visible*

**X and Y**<br>
 The coordinates of the item relative to the parent container (Layout, Panel or Group).

**Lefttop and Rightbottom**<br>
 Indicate to which corners of the parent container the item's corners are attatched to in case of resizing.<br>
 *See also: ["How to create a resizable window"](resizable.md)*

**Keep X/Y ratio**<br>
 Indicate whether the items relative horizontal and vertical positions in the parent container should be maintained when it is resized.<br>
 *See also: ["How to create a resizable window"](resizable.md)*<br>
 *XML-attribute name: xkeepratio & ykeepratio*

**Help text**<br>
 Help text for the current control. The text variable *$H* will be set to this value when the mouse moves over the item.<br>
 *XML-attribute name: help*

### Slider Attributes

*Under construction*

**See also:**<br>
 [Percentage values](percent.md)<br>
 [Bezier curves](bezier.md)<br>
 [Slider background](i-sliderbg.md)

© 2008 All rights reserved to the VideoLAN team.
