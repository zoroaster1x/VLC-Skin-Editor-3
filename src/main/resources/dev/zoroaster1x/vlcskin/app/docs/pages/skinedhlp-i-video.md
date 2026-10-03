---
title: "Video"
source: https://images.videolan.org/vlc/skinedhlp/i-video.html
crawled: 2026-10-03
---

[VLC Skin Editor - Online Help](index.md) > Documentation > Items > Video

# Video

Displays the video inside the skin.<br>
 *Note:*<br>
 This is not yet fully perfect in VLC. E.g. if you change the layout the video will display in a native window and will, even after having changed back to the former layout, not return into the skin.

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

### Video Attributes

**width** and **height**<br>
Initial width and height of the video display

**autoresize**<br>
Sets whether the skin should be resized to fit the video, if it is larger than the current width and height.

© 2008 All rights reserved to the VideoLAN team.
