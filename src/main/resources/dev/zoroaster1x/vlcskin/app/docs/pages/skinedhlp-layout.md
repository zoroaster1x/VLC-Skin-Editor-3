---
title: "Layout"
source: https://images.videolan.org/vlc/skinedhlp/layout.html
crawled: 2026-10-03
---

[VLC Skin Editor - Online Help](index.md) > Documentation > Layout

# Layout

A Layout defines the content of a window throught its contained items. A window may have several layouts, of which only one is visible at a time. All layouts should have the same size, as graphical problems might occur otherwise.

### Attributes

**id**<br>
 The ID by which this layout is referred to. Must be set and has to be unique.

**width**<br>
 The initial width of the layout. *Required*

**height**<br>
 The initial height of the layout. *Required*

**minwidth**<br>
 Minimum width of the layout. This value is only used when resizing the layout. If this value is set to "-1", the initial width (as specified by the width attribute) will be used as minimum width.<br>
 Default value: -1

**minheight**<br>
 Minimum height of the layout. This value is only used when resizing the layout. If this value is set to "-1", the initial height (as specified by the height attribute) will be used as minimum height.<br>
 Default value: -1

**maxwidth**<br>
 Maximum width of the layout. This value is only used when resizing the layout. If this value is set to "-1", the initial width (as specified by the width attribute) will be used as maximum width.<br>
 Default value: -1

**maxheight**<br>
 Maximum height of the layout. This value is only used when resizing the layout. If this value is set to "-1", the initial height (as specified by the height attribute) will be used as maximum height.<br>
 Default value: -1

**See also:**<br>
 [Window](window.md), [How to create resizable windows](resizable.md)

© 2008 All rights reserved to the VideoLAN team.
