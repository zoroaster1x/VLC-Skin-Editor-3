---
title: "Checkbox"
source: https://images.videolan.org/vlc/skinedhlp/i-checkbox.html
crawled: 2026-10-03
---

[VLC Skin Editor - Online Help](index.md) > Documentation > Items > Checkbox

# Checkbox

A Button that changes according to a specified condition. Used e.g. for controls that toggle something, like Play/Pause and un-/mute.<br>
 The Checkbox could also be seen as two buttons, of which one always appears according to the set condition. Thus the Checkbox has two sets of mouse-related states (up, over, down).

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

### Checkbox Attributes

**Condition**<br>
 The [boolean expression](boolexpr.md) defining in which state the checkbox is. If the expression resolves to *true*, the checkbox is in state *two*. Else the checkbox is in state *one*.<br>
*XML-attribute name: state*

**Normal image (1 and 2)**<br>
 The bitmap that is displayed when the checkbox is in its normal state.<br>
*XML-attribute name: up1 & up2*

**Mouse-over image (1 and 2)**<br>
 The bitmap that is displayed when the mouse is over the checkbox.<br>
*XML-attribute name: over1 & over 2*

**Mouse-click image (1 and 2)**<br>
 The bitmap that is displayed when the checkbox is clicked.<br>
*XML-attribute name: down1 & down2*

**Action (1 and 2)**<br>
 The action that is triggered when the checkbox is clicked.

**Tooltiptext (1 and 2)**<br>
 The text that is displayed as a tooltip, when the checkbox is hovered by the mouse. You can also use the available [text variables](textvars.md).

**See also:**<br>
 [Bitmap resources](res-bitmap.md)<br>
 [Button](i-button.md)<br>
 [Boolean Expressions](boolexpr.md)<br>
 [Text variables](textvars.md)

© 2008 All rights reserved to the VideoLAN team.
