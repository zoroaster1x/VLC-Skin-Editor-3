---
title: "Image"
source: https://images.videolan.org/vlc/skinedhlp/i-image.html
crawled: 2026-10-03
---

[VLC Skin Editor - Online Help](index.md) > Documentation > Items > Image

# Image

Displays an image in the skin. Mostly used for the background of the player. Clicking on an Image item can either move or resize the skin. Rightclicking shows the menu.

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

### Image Attributes

**Image**<br>
 The bitmap that is displayed.

 **Resize**<br>
 Sets how the image should be resized. *Mosaic* repeats the image just like in a mosaic. When you chose *scale* the image will be stretched to fit.

**Action**<br>
 The action that is triggered when you click on the image. *Move* enables the user to move the window by dragging the image. The *resize* actions enable the user to resize the window into the given direction (East, South or South-East) by dragging the image.

**Double-click action**<br>
 The action that is triggered when the image is double-clicked.<br>
*XML-attribute name: action2*

**See also:**<br>
 [Bitmap resources](res-bitmap.md)

© 2008 All rights reserved to the VideoLAN team.
