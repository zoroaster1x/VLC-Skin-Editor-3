---
title: "Text"
source: https://images.videolan.org/vlc/skinedhlp/i-text.html
crawled: 2026-10-03
---

[VLC Skin Editor - Online Help](index.md) > Documentation > Items > Text

# Text

Displays a text in the skin.

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

### Text Attributes

**Font**<br>
 The font used to render the text.

 **Text**<br>
 The text to be displayed.

**Color**<br>
 The color of the text in hexadecimal format.

**Width**<br>
 Width of the text in pixels. If set to "0", the width is automatically calculated to fit with the current text.

**Alignment**<br>
 Alignment of the text inside the control. The *right* and *center* alignments are computed using the *width* attribute. Thus if *width* is set to 0, *center* and *right* alignment won't have an effect.

**Scrolling**<br>
 Scrolling behaviour of the text (only when it doesn't fit in the width of the control). Possible values are *auto*, *manual* and *none*. If this attribute is set to *auto*, the text automatically starts scrolling. The user can drag the text, and click on it to start/stop the scrolling. If this attribute is set to *manual*, the text only scrolls when dragged by the user. If this attribute is set to *none*, no scrolling is possible at all.

**See also:**<br>
 [Text variables](textvars.md)

© 2008 All rights reserved to the VideoLAN team.
