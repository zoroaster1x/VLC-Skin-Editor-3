---
title: "SubBitmap"
source: https://images.videolan.org/vlc/skinedhlp/res-subbitmap.html
crawled: 2026-10-03
---

[VLC Skin Editor - Online Help](index.md) > Documentation > Resources > SubBitmap

# SubBitmap

A SubBitmap represents a portion of an existing Bitmap resource that can be used as a seperate image. It is a resource that has to be added to an already existing Bitmap.

### Attributes

**id**<br>
 The name identifying this SubBitmap. The skin's items use this ID to refer to the image associated with that SubBitmap.<br>
 This ID has to be unique and is required to be set.

**x, y, width and height**<br>
 These attributes define the position and dimension of the sub-image taken from the parent Bitmap. You can easily set those attributes via the SubBitmap-Editor right to the SubBitmap settings window.<br>
 These attributes are essential and have to be set.

### Animation

 Analogous to the animation of normal Bitmap resources, SubBitmaps also have the attributes **nbframes** and **fps**.

**See also:**<br>
 [Bitmap](res-bitmap.md)

© 2008 All rights reserved to the VideoLAN team.
