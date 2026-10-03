---
title: "Window"
source: https://images.videolan.org/vlc/skinedhlp/window.html
crawled: 2026-10-03
---

[VLC Skin Editor - Online Help](index.md) > Documentation > Window

# Window

In VLC's Skins2-system windows are stored in Window-objects. The skin's items are not stored directly in a window but rather in a layout, which in turn is stored in a window. A window can have several layouts. When the skin is opened for the first time, the layout that is last in the layout list shows up by default. Thus you can change the default layout of a window by moving it to the bottom with the  ![](images/skinedhlp-move-down.png) Move Down button.

### Attributes

**id**<br>
 The ID by which this window is referred to. Must be set and has to be unique.

**visible**<br>
 Boolean expression indicating whether the window should appear when VLC is started. Since VLC remembers the skin's windows' position and visibility, this attribute will only be used the first time VLC is started with this skin.

**x and y**<br>
 The initial position of the window on the screen. As it is true with the visible attribute, also the x and y attributes will only be taken into consideration the first time the skin is loaded.

**dragdrop**<br>
 Boolean value that indicates whether it is allowed to drang and drop files onto this window.

**playondrop**<br>
 When dragdrop is true, this boolean value determines whether dropped files will be played immediately.

**See also:**<br>
 [Layout](layout.md), [How to create resizable windows](resizable.md)

© 2008 All rights reserved to the VideoLAN team.
