---
title: "Button"
source: https://images.videolan.org/vlc/skinedhlp/i-button.html
crawled: 2026-10-03
---

[VLC Skin Editor - Online Help](index.md) > Documentation > Items > Button

# Button

A clickable button, that triggers a certain action. A button has three states: Up, Over and Down. These correspond to the mouse interaction with the button.

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

### Button Attributes

**Normal image**<br>
 The bitmap that is displayed when the button is in its normal state.<br>
*XML-attribute name: up*

**Mouse-over image**<br>
 The bitmap that is displayed when the mouse is over the button.<br>
*XML-attribute name: over*

**Mouse-click image**<br>
 The bitmap that is displayed when the button is clicked.<br>
*XML-attribute name: down*

**Action**<br>
 The action that is triggered when the button is clicked.

**Tooltiptext**<br>
 The text that is displayed as a tooltip, when the button is hovered by the mouse. You can also use the available [text variables](textvars.md).

**See also:**<br>
 [Bitmap resources](res-bitmap.md)<br>
 [Text variables](textvars.md)

© 2008 All rights reserved to the VideoLAN team.
