---
title: HowTo create your own skin
source: https://images.videolan.org/vlc/skins2-create.html
crawled: 2026-10-03
---

<a id="skin-creation-howto"></a>
# HowTo create your own skin

### Olivier Teulière

Copyright © 2004-2013 the VideoLAN project

<a id="idm9"></a>

Permission is granted to copy, distribute and/or modify this document under the terms of the GNU General Public License as published by the Free Software Foundation; either version 2 of the License, or (at your option) any later version. The text of the license can be found on [the GNU website](http://www.gnu.org/copyleft/gpl.html).

**Abstract**

Skin creation HowTo

---

**Table of Contents**

- [Basic principles](#idm22)
- [The bitmaps](#idm33)
- [The XML file](#idm44)
  - [Theme](#Theme)
  - [ThemeInfo](#ThemeInfo)
  - [Bitmap](#Bitmap)
  - [SubBitmap](#SubBitmap)
  - [Font](#Font)
  - [BitmapFont](#BitmapFont)
  - [Window](#Window)
  - [Layout](#Layout)
  - [Group](#Group)
  - [Panel](#Panel)
  - [Anchor](#Anchor)
  - [Common attributes](#commattr)
  - [Image](#Image)
  - [Button](#Button)
  - [Checkbox](#Checkbox)
  - [Text](#Text)
  - [Slider](#Slider)
  - [SliderBackground](#SliderBackground)
  - [RadialSlider](#RadialSlider)
  - [Video](#Video)
  - [Playlist](#Playlist)
  - [Playtree](#Playtree)
- [Actions](#actions)
- [Text variables](#textvars)
- [Boolean expressions](#boolexpr)
- [Percentage variables](#percent)
- [Layout model](#layoutmodel)
- [Compression](#idm1175)
- [Bezier curves](#bezier)
- [Tools and advice](#idm1209)
  - [Generating Bezier curves](#CurveMaker)
  - [Using VLC warning messages](#idm1221)
  - [Relative paths](#idm1229)
  - [Get inspiration](#idm1232)
  - [Submit your skin!](#idm1236)

<a id="idm22"></a>
## Basic principles

A skin (or theme, the two words have almost the same meaning) for VLC is made of:

- many PNG (Portable Network Graphics) files, for the images of the different states of the controls,
- font files in the TTF (TrueType Font) format,
- a XML file describing the logical structure of the skin (which bitmaps correspond to which controls, where to place buttons, sliders, and so on) and its behaviour (what happens when the user clicks on a button, etc.).

Those of you who have already made skins for other software should have no difficulty to understand how VLC skins work.

<a id="idm33"></a>
## The bitmaps

Basically, you need one bitmap file (in PNG format) by state of control. For example, with a [Image](#Image) control you need 1 image, with a [Button](#Button) control you need 3 images (for the up, down and mouseover states). The same bitmap file can be used for many controls, using [sub-bitmaps](#SubBitmap).

The PNG format allows setting a transparency mask, which will be used wherever the image needs to be displayed (only non-transparent parts will be drawn). In addition, you can also specify a transparency color in the XML file: the bitmap will be considered as transparent wherever this color appears in the bitmap file.

> ### Note
>
> Both the transparency mask and the transparent color will be taken into acount, so if the transparency mask is correctly set in the bitmap file you need to choose a unused color for the transparency color.

> ### Note
>
> Starting from VLC 0.8.5, it is not anymore necessary to use PNG format for the bitmap files: most common formats are supported. Using PNG format is still recommended, for compatibility with older VLC versions.

<a id="idm44"></a>
## The XML file

XML is a markup language, like HTML. It won't be explained here any further, please use Google if you don't know what XML is. You'll see, it is rather easy to understand.

The XML file used for the VLC skins follows a predefined DTD. You can find this DTD [in VLC Git](http://git.videolan.org/gitweb.cgi?p=vlc.git;a=blob;f=share/skins2/skin.dtd;hb=HEAD), and its reading is strongly advised, since it contains the default values used for the parameters. A skin that does not follow the DTD with which VLC was compiled won't be loaded by VLC (and it might even crash it...).

For a better undestanding of what follows, you should have a look at the DTD and/or at an example of valid XML skin.

OK, let's go for an enumeration of the different tags and their attributes:

<a id="Theme"></a>
### Theme

Main tag, for global attributes

<a id="themeversion"></a>
#### version

Version of the DTD used when making the skin, such as "2.0" (you can find the version in the DTD itself). This number might be used in the future to provide a better backward compatibility with older skins.

*Required.*

<a id="themetooltipfont"></a>
#### tooltipfont

Identifiant of a [Font](#Font) or [BitmapFont](#BitmapFont), used for the tooltips (beware that any character not present in a [BitmapFont](#BitmapFont) will be printed as a space, so will be invisible). The default value uses a font provided with VLC, so you don't need to provide it with your skin.

Default value: defaultfont

<a id="thememagnet"></a>
#### magnet

Allows to select the range of action (in pixels) of magnetism with borders of the screen: when the distance between the border of the screen and an anchor of a window is less than this value, the window will stick to the border. 0 disables magnetism with the screen borders.

Default value: 15

<a id="themealpha"></a>
#### alpha

Sets the alpha transparency of the windows. The value must be between 1 (nearly total transparency) and 255 (total opacity). Low values should be avoided.

> ### Note
>
> This only works if transparency is not disabled in the preferences of the skins2 module.

Default value: 255

<a id="thememovealpha"></a>
#### movealpha

Sets the alpha transparency of the windows when they are moving. Same range as [alpha](#themealpha).

> ### Note
>
> This only works if transparency is not disabled in the preferences of the skins2 module.

Default value: 255

<a id="ThemeInfo"></a>
### ThemeInfo

You can enter here some information about you (but this information is currently unused by VLC...)

<a id="themeinfoname"></a>
#### name

Skin name. Not supported yet.

*Implied.*

<a id="themeinfoauthor"></a>
#### author

Author of the skin. Not supported yet.

*Implied.*

<a id="themeinfoemail"></a>
#### email

Email of the author. Not supported yet.

*Implied.*

<a id="themeinfowebpage"></a>
#### webpage

Web page in relation with the skin. Not supported yet.

Default value: http://www.videolan.org/vlc/

<a id="Bitmap"></a>
### Bitmap

Associates a bitmap file (usually in PNG format) with an identifiant (=name) that will be used by the various controls. Obviously, you need one Bitmap tag for each bitmap file you have.

<a id="bitmapid"></a>
#### id

Identifiant of the bitmap that will be used with controls. Two bitmaps cannot have the same id.

*Required.*

<a id="bitmapfile"></a>
#### file

Indicates the path and name of the bitmap file used. This path can be absolute (but you should definitely avoid it), or relative to the path of the XML file.

*Required.*

<a id="bitmapalphacolor"></a>
#### alphacolor

Transparency color of the bitmap. It must be indicated with the following format: "#RRGGBB", where RR stands for the hexadecimal value of the red component, GG for the green one, and BB for the blue one.

> ### Note
>
> If your PNG file specifies a transparency mask, it will be taken into account too.

Default value: #000000

<a id="bitmapnbframes"></a>
#### nbframes

This attribute is needed to define animated bitmaps; it is the number of frames (images) contained in your animation. All the different frames are just images laid vertically in the bitmap. Animated GIFs are not supported at the moment. (since VLC 0.8.5)

Default value: 1

<a id="bitmapfps"></a>
#### fps

Only used in animated bitmaps; it is the number of frames (images) per seconds of the animation. (since VLC 0.8.5)

Default value: 0

<a id="bitmaploop"></a>
#### loop

Only used in animated bitmaps; it is the number of loops before animation stops. Default value 0 means animation doesn't stop. Otherwise, animation stops on the last frame after the number of loops has been reached (since VLC 1.1)

Default value: 0

<a id="SubBitmap"></a>
### SubBitmap

Declares a portion of bitmap, that will be used with controls in the same way as a regular Bitmap. A SubBitmap tag can only be placed inside a [Bitmap](#Bitmap) tag, and references implicitly the same file. SubBitmaps are very convenient when a file contains images for several controls. (This tag was not available before VLC 0.8.5).

<a id="subbitmapid"></a>
#### id

Identifiant of the portion of bitmap that will be used with controls. It must be unique in the whole skin.

*Required.*

<a id="subbitmapx"></a>
#### x

Horizontal offset of the sub-bitmap (in pixels), relative to the "parent" bitmap.

*Required.*

<a id="subbitmapy"></a>
#### y

Vertical offset of the sub-bitmap (in pixels), relative to the "parent" bitmap.

*Required.*

<a id="subbitmapwidth"></a>
#### width

Width of the sub-bitmap, in pixels.

*Required.*

<a id="subbitmapheight"></a>
#### height

Height of the SubBitmap, in pixels.

*Required.*

<a id="subbitmapnbframes"></a>
#### nbframes

Same as in [Bitmap](#bitmapnbframes) tag.

Default value: 1

<a id="subbitmapfps"></a>
#### fps

Same as in [Bitmap](#bitmapfps) tag.

<a id="subbitmaploop"></a>
#### loop

Same as in [Bitmap](#bitmaploop) tag.

<a id="Font"></a>
### Font

Declares a font to be used in a [Text](#Text) or [Playtree](#Playtree).

<a id="fontid"></a>
#### id

Identifiant of the font that will be used with controls.

*Required.*

<a id="fontfile"></a>
#### file

This is the file containing a TrueType font.

*Required.*

<a id="fontsize"></a>
#### size

This is the size of the font, in points.

Default value: 12

<a id="BitmapFont"></a>
### BitmapFont

<a id="bitmapfontid"></a>
#### id

Identifiant of the font that will be used with controls.

*Required.*

<a id="bitmapfontfile"></a>
#### file

This is the file containing a bitmap font, à la Winamp.

*Required.*

<a id="bitmapfonttype"></a>
#### type

Type of font, one of "digits" or "text".

Default value: digits

<a id="Window"></a>
### Window

A window that will appear on screen.

<a id="windowid"></a>
#### id

Name of the window (it may be used for actions). Two windows cannot have the same id.

Default value: none

As of vlc1.1, a special id has been added to provide a skinnable fullscreen controller. This value id="fullscreenController" allows the window to be displayed only in fullscreen mode. Display is toggled by pressing the 'i' hotkey or clicking the mouse middle button

<a id="windowvisible"></a>
#### visible

Indicates whether the window should appear when VLC is started. Since VLC remembers the skin windows position and visibility, this attribute will only be used the first time the skin is started.

Default value: true

<a id="windowx"></a>
#### x

Initial left position of the window.

Default value: 0

<a id="windowy"></a>
#### y

Initial top position of the window.

Default value: 0

<a id="windowdragdrop"></a>
#### dragdrop

Indicates whether drag and drop of media files is allowed on this window.

Default value: true

<a id="windowplayondrop"></a>
#### playondrop

Indicates whether a dropped file is played directly (true) or only enqueued (false). This attribute has no effect if [dragdrop](#windowdragdrop) is set to "false".

Default value: true

<a id="Layout"></a>
### Layout

A layout is one aspect of a window, i.e. a set of controls and anchors. A window can have many layouts, but only one will be visible at any time.

<a id="layoutid"></a>
#### id

Name of the layout (it may be used for actions). Two layouts cannot have the same id.

Default value: none

<a id="layoutwidth"></a>
#### width

Initial width of the layout. This value is required since VLC is not (yet?) able to calculate it using the sizes and positions of the controls.

*Required.*

<a id="layoutheight"></a>
#### height

Initial height of the layout. This value is required since VLC is not (yet?) able to calculate it using the sizes and positions of the controls.

*Required.*

<a id="minwidth"></a>
#### minwidth

Minimum width of the layout. This value is only used when resizing the layout. If this value is set to "-1", the initial width (as specified by the [width](#layoutwidth) attribute) will be used as minimum width.

Default value: -1

<a id="maxwidth"></a>
#### maxwidth

Maximum width of the layout. This value is only used when resizing the layout. If this value is set to "-1", the initial width (as specified by the [width](#layoutwidth) attribute) will be used as maximum width.

Default value: -1

<a id="minheight"></a>
#### minheight

Minimum height of the layout. This value is only used when resizing the layout. If this value is set to "-1", the initial height (as specified by the [height](#layoutheight) attribute) will be used as minimum height.

Default value: -1

<a id="maxheight"></a>
#### maxheight

Maximum height of the layout. This value is only used when resizing the layout. If this value is set to "-1", the initial height (as specified by the [height](#layoutheight) attribute) will be used as maximum height.

Default value: -1

<a id="Group"></a>
### Group

Add an offset to the elements it contains. A Group is only supposed to ease the job of the skin designer, who can adjust the position of a group of controls without modifying all the coordinates, but you can ignore it if you want (only one Group is necessary, inside the [Layout](#Layout) tag). Group tags can be nested. Note that Group elements are deprecated, since [Panel](#Panel) elements are more powerful.

<a id="groupx"></a>
#### x

Horizontal offset, relative to the container box (see the [Layout model](#layoutmodel) for more details).

Default value: 0

<a id="groupy"></a>
#### y

Vertical offset, relative to the container box (see the [Layout model](#layoutmodel) for more details).

Default value: 0

<a id="Panel"></a>
### Panel

A Panel can be seen as an enhanced [Group](#Layout). It also adds an offset to the elements it contains, but in addition it becomes their reference for the lefttop, rightbottom, xkeepratio and ykeepratio attributes. Panel tags can be nested. Since VLC 0.9.0.

See the [common attributes](#commattr).

<a id="panelx"></a>
#### x

Same as the [x](#x) attribute of the [common attributes](#commattr).

<a id="panely"></a>
#### y

Same as the [y](#y) attribute of the [common attributes](#commattr).

<a id="panelwidth"></a>
#### width

Initial width of this container box (see the [Layout model](#layoutmodel) for more details).

*Required.*

<a id="panelheight"></a>
#### height

Initial height of this container box (see the [Layout model](#layoutmodel) for more details).

*Required.*

<a id="panellefttop"></a>
#### lefttop

Same as the [lefttop](#lefttop) attribute of the [common attributes](#commattr).

<a id="panelrightbottom"></a>
#### rightbottom

Same as the [rightbottom](#rightbottom) attribute of the [common attributes](#commattr).

<a id="panelxkeepratio"></a>
#### xkeepratio

Same as the [xkeepratio](#xkeepratio) attribute of the [common attributes](#commattr).

<a id="panelykeepratio"></a>
#### ykeepratio

Same as the [ykeepratio](#ykeepratio) attribute of the [common attributes](#commattr).

<a id="Anchor"></a>
### Anchor

Create a "magnetic point" (or curve) in the current window. If an anchor of another window enters in the range of action of this anchor, the 2 anchors will automatically be on the same place, and the windows are "sticked". Each anchor has a priority ([priority](#anchorpriority) attribute), and the anchor with the highest priority is the winner, which means that when moving its window all the other anchored windows will move too. To break the effect of 2 anchored windows, you need to move the window whose anchor has the lower priority.

<a id="anchorx"></a>
#### x

Is it really necessary to explain?

Default value: 0

<a id="anchory"></a>
#### y

...

Default value: 0

<a id="anchorlefttop"></a>
#### lefttop

Indicate to which corner of the Layout the top-left-hand corner of this anchor is attached, in case of resizing. Possible values are 'lefttop', 'leftbottom', 'righttop' and 'rightbottom'. Available since VLC 0.8.6.

Note that there is no "rightbottom" attribute for the anchors (contrarily to normal controls), because an anchor is not resizable (even when the anchor is not ponctual and follows a Bezier curve).

Default value: lefttop

<a id="anchorpriority"></a>
#### priority

Priority of anchor (see the previous description).

*Required.*

<a id="anchorpoints"></a>
#### points

Points defining the [Bezier curve](#bezier) followed by the anchor.

> ### Note
>
> You don't need to change this parameter if all you want is a ponctual anchor.

Default value: (0,0)

<a id="anchorrange"></a>
#### range

Range of action of the anchor, in pixels.

Default value: 10

<a id="commattr"></a>
### Common attributes

The following attributes are common to all the controls (Image, Button, Checkbox, Text, Slider, RadialSlider, Playlist, Playtree, Video)

<a id="attrid"></a>
#### id

Identifiant of the control. Currently unused.

Default value: none

<a id="visible"></a>
#### visible

See [Boolean expressions](#boolexpr).

Default value: true

<a id="x"></a>
#### x

Horizontal offset of the control, relative to the container box (see the [Layout model](#layoutmodel)) or to the parent [Group](#Group).

Default value: 0

<a id="y"></a>
#### y

Vertical offset of the control, relative to the container box (see the [Layout model](#layoutmodel)) or to the parent [Group](#Group).

Default value: 0

<a id="lefttop"></a>
#### lefttop

Indicate to which corner of the container box the top-left-hand corner of this control is attached, in case of resizing. Possible values are 'lefttop', 'leftbottom', 'righttop' and 'rightbottom'. See the [Layout model](#layoutmodel) for more details.

Default value: lefttop

<a id="rightbottom"></a>
#### rightbottom

Indicate to which corner of the container box the bottom-right-hand corner of this control is attached, in case of resizing. See the [Layout model](#layoutmodel) for more details.

Default value: lefttop

<a id="xkeepratio"></a>
#### xkeepratio

When set to true, the behaviour of the horizontal resizing is changed. Instead of taking into account the [lefttop](#lefttop) and [rightbottom](#rightbottom) attributes to determine how the control will be moved/resized, only its initial position inside the container box matters. For example, if initially the space to the left of the control is twice as big as the one to its right, this will stay the same during any horizontal resizing. The width of the control stays constant.

This attribute can be particularly useful to keep a control centered in the container box, without resizing it (to resize it, you would rather use the lefttop/rightbottom attributes). See the [Layout model](#layoutmodel) for more details. Available since VLC 0.8.6.

Default value: false

<a id="ykeepratio"></a>
#### ykeepratio

When set to true, the behaviour of the vertical resizing is changed. Instead of taking into account the [lefttop](#lefttop) and [rightbottom](#rightbottom) attributes to determine how the control will be moved/resized, only its initial position inside the [Layout](#Layout) matters. For example, if initially the space to the top of the control is twice as big as the one to its bottom, this will stay the same during any vertical resizing. The height of the control stays constant.

This attribute can be particularly useful to keep a control centered in the container box, without resizing it (to resize it, you would rather use the lefttop/rightbottom attributes). See the [Layout model](#layoutmodel) for more details. Available since VLC 0.8.6.

Default value: false

<a id="help"></a>
#### help

Help text for the current control. The variable '$H' will be expanded to this value when the mouse hovers the current control (see [Text variables](#textvars)).

Default value:

The vlc2.0 extends some of these parameters and adds five more parameters to ease up positioning windows within a given screen and widgets within their own containers (layout, panel, ...). As extension, the x, y, width and height can now be given in pixels (default) or in percentage of their container. For instance, x="10" or x="10px" is construed as 10 pixels, and x="10%" is construed as 10% of the width of the screen for windows or their container for widgets.

<a id="position"></a>
#### position

Relative placement of windows or widget within their respective container(screen for windows, layout or panel for widgets). This placement is given with a geographical qualifier. Possible values are "Center", "North", "NorthWest", ....The skin engine computes the size of the element (from the width and height parameters) then places it at the center or sticks it to a given border depending on the geographical qualifer.

Default value: -1(N/A)

<a id="xmargin"></a>
#### xmargin

Coupled with position, xmargin allows for a horizontal margin instead of just sticking to the border. It can be given in pixels or in percentage of the width of the container.

Default value: 0

<a id="ymargin"></a>
#### ymargin

Coupled with position, ymargin allows for a vertical margin instead of just sticking to the border. It can be given in pixels or in percentage of the height of the container.

Default value: 0

<a id="xoffset"></a>
#### xoffset

Coupled with position and xmargin, this parameter moves the widget horizontally by this offset from the position computed above. From VLC2.1.0 on, xoffset can directly be used without position, in which case percentage is computed against the current object instead of the upper container

Default value: 0

<a id="yoffset"></a>
#### yoffset

Coupled with position and ymargin, this parameter moves the widget vertically by this offset from the position computed above. From VLC2.1.0 on, yoffset can directly be used without position, in which case percentage is computed against the current object instead of the upper container

Default value: 0

<a id="Image"></a>
### Image

Create a simple image. Particularly useful for backgrounds.

See the [Common attributes](#commattr).

<a id="image"></a>
#### image

Identifiant of a [Bitmap](#Bitmap).

*Required.*

<a id="imagewidth"></a>
#### width

Width of the image. If set, this width supersedes the actual width of the image. The initial resizing takes the resize policy into account

Default value: -1

<a id="imageheight"></a>
#### height

Height of the image. If set, this height supersedes the actual height of the image. The initial resizing takes the resize policy into account

Default value: -1

<a id="imageresize"></a>
#### resize

Since VLC 0.8.2. Specify the behaviour of the image when it is resized. Possible values are 'mosaic' (the image is repeated as many times as necessary to reach the wanted dimensions) and 'scale' (the image is actually rescaled). Beware that the 'scale' behaviour is much slower than the 'mosaic' one, so make sure to use it only when it's really needed.

In VLC 2.0, a third value 'scale2' is available to scale an image, yet preserving its aspect ratio. The image is centered and scaled either heightwise or widthwise to fill up as much area as possible. Extra borders are made transparent.

Default value: mosaic.

<a id="imageaction"></a>
#### action

Action triggered by a click on the control. Possible values are 'move', to move the window, 'resizeE', to resize horizontally, 'resizeS' to resize vertically, and 'resizeSE' to resize both horizontally and vertically. Mnemonics: S, E and SE stand for South, East, and South-East. The 'resizeS' and 'resizeE' actions are available since VLC 0.8.5 only.

Default value: none

<a id="imageaction2"></a>
#### action2

Action triggered by a double-click on the control. See [Actions](#actions) for a list of possible actions. (Since VLC 0.8.5).

Default value: none

<a id="imageart"></a>
#### art

if set to true, the skin engine reuses the control to display the art file of the resource currently being played back. Usually, resize="scale2" is desirable, for art to be rendered in the best possible way. (from VLC 2.0 on).

Default value: false

<a id="Button"></a>
### Button

Create a button.

See the [common attributes](#commattr).

<a id="buttonup"></a>
#### up

Identifiant of a [Bitmap](#Bitmap), used when the button is up.

*Required.*

<a id="buttondown"></a>
#### down

Identifiant of a [Bitmap](#Bitmap), used when the button is down.

Default value: none

<a id="buttonover"></a>
#### over

Identifiant of a [Bitmap](#Bitmap), used when the mouse is over the button.

Default value: none

<a id="buttonaction"></a>
#### action

Action executed when the button is clicked. See [Actions](#actions) for a list of possible actions.

Default value: none

<a id="buttontooltiptext"></a>
#### tooltiptext

Tooltip associated with the button. See also [Text variables](#textvars).

Default value:

<a id="Checkbox"></a>
### Checkbox

Create a checkbox, i.e. a button with 2 states (checked/unchecked). So you need 6 images for a full-featured checkbox: each state has a basic image ('up' state), an image for the control being hovered by the mouse ('over' state) and an image corresponding to a click not yet released ('down' state). If you supply only the basic images, the other ones will be identical.

See the [common attributes](#commattr).

<a id="up1"></a>
#### up1

Identifiant of a [Bitmap](#Bitmap), used when the checkbox is up in the first state.

*Required.*

<a id="down1"></a>
#### down1

Identifiant of a [Bitmap](#Bitmap), used when the checkbox is down in the first state.

Default value: none

<a id="over1"></a>
#### over1

Identifiant of a [Bitmap](#Bitmap), used when the mouse is over the checkbox in the first state.

Default value: none

<a id="up2"></a>
#### up2

Identifiant of a [Bitmap](#Bitmap), used when the checkbox is up in the second state.

*Required.*

<a id="down2"></a>
#### down2

Identifiant of a [Bitmap](#Bitmap), used when the checkbox is down in the second state.

Default value: none

<a id="over2"></a>
#### over2

Identifiant of a [Bitmap](#Bitmap), used when the mouse is over the checkbox in the second state.

Default value: none

<a id="state"></a>
#### state

[Boolean expression](#boolexpr) specifying the state of the checkbox: if the expression resolves to 'false', the first state will be used, and if it resolves to 'true' the second state will be used. Example for a checkbox showing/hiding a window whose [id](#windowid) is "playlist\_window": state="playlist\_window.isVisible" (or state="not playlist\_window.isVisible", depending on the states you chose).

*Required.*

<a id="action1"></a>
#### action1

Action executed when the checkbox is clicked (state 1 to state 2). See [Actions](#actions) for a list of possible actions.

Default value: none

<a id="action2"></a>
#### action2

Action executed when the checkbox is clicked (state 2 to state 1). See [Actions](#actions) for a list of possible actions.

Default value: none

<a id="tooltiptext1"></a>
#### tooltiptext1

Tooltip associated with the checkbox in state 1. See also [Text variables](#textvars).

Default value:

<a id="tooltiptext2"></a>
#### tooltiptext2

Tooltip associated with the checkbox in state 2. See also [Text variables](#textvars).

Default value:

<a id="Text"></a>
### Text

Control to display some text.

See the [common attributes](#commattr).

<a id="textfont"></a>
#### font

Identifiant of a [Font](#Font) or [BitmapFont](#BitmapFont) (beware that any character not present in the [BitmapFont](#BitmapFont) will be printed as a space, so will be invisible).

*Required.*

<a id="texttext"></a>
#### text

Text to display. See also [Text variables](#textvars).

Default value:

<a id="textcolor"></a>
#### color

Color of the text, using the #RRGGBB format.

Default value: #000000

<a id="textwidth"></a>
#### width

Width of the text in pixels. If set to "0", the width is automatically calculated to fit with the current text.

Default value: 0

<a id="textalignment"></a>
#### alignment

Alignment of the text inside the control. Possible values are 'left', 'center' and 'right'. The 'width' and 'center' alignments are computed using the width of the control (as given by the [width](#textwidth) attribute). Available since VLC 0.8.5.

Default value: left

<a id="focus"></a>
#### focus

indicates if the control is eligible for mouse focus or not. If focus is set to false, it is as though the control did not exist when it comes to mouse focus. This allows for instance displaying a dynamic text in the title bar, yet opting for being able to move the window rather than manage scrolling of lengthy text. Available in VLC 2.0

Default value: true

<a id="textscrolling"></a>
#### scrolling

Scrolling behaviour of the text (only when it doesn't fit in the [width](#textwidth) of the control). Possible values are 'auto', 'manual' and 'none'. If this attribute is set to 'auto', the text automatically starts scrolling. The user can drag the text, and click on it to start/stop the scrolling. If this attribute is set to 'manual', the text only scrolls when dragged by the user. If this attribute is set to 'none', no scrolling is possible at all. Available since VLC 0.8.5.

Default value: auto

<a id="Slider"></a>
### Slider

Create a slider. This element can be used alone, or can contain a [SliderBackground](#SliderBackground) element.

See the [common attributes](#commattr).

<a id="sliderup"></a>
#### up

Identifiant of a [Bitmap](#Bitmap), used when the slider cursor is up.

*Required.*

<a id="sliderdown"></a>
#### down

Identifiant of a [Bitmap](#Bitmap), used when the slider cursor is down.

Default value: none

<a id="sliderover"></a>
#### over

Identifiant of a [Bitmap](#Bitmap), used when the mouse is over the slider cursor.

Default value: none

<a id="sliderpoints"></a>
#### points

Points defining the [Bezier curve](#bezier) followed by the slider cursor.

Default value: none

<a id="sliderthickness"></a>
#### thickness

Thickness of the slider curve. This attribute is used to determine whether the mouse is over the slider (hence whether a mouse click will have an effect on the cursor position).

Default value: 10

<a id="slidervalue"></a>
#### value

Variable controlled by the slider. This must be a [percentage variable](#percent), e.g "volume" or "time" (only exception: the [Slider](#Slider) defined inside the [Playtree](#Playtree) tag does not need to set this attribute).

Default value: none

<a id="sliderwidth"></a>
#### width

Width of the slider. If set, this width supersedes the actual width of the slider computed from the curve width. (new after VLC 2.1.0)

Default value: -1

<a id="sliderheight"></a>
#### height

Height of the slider. If set, this height supersedes the actual height of the slider computed from the curve height. (new after VLC 2.1.0)

Default value: -1

<a id="slidertooltiptext"></a>
#### tooltiptext

Tooltip associated with the slider. See also [Text variables](#textvars).

Default value:

<a id="SliderBackground"></a>
### SliderBackground

Set of background images associated to a slider (it must be a sub-element of a [Slider](#Slider)). The displayed image depends on the value of the corresponding slider; if the SliderBackground contains n images, the image #m will be displayed, where m = n \* (slider value). A SliderBackground actually contains a single image, which is divided into a grid to build all the sub-images. All the sub-images of the grid have the same size, and can be separated by unused pixel lines or rows if needed (this is called "padding").

See the [common attributes](#commattr).

<a id="sbg_image"></a>
#### image

Identifiant of a [Bitmap](#Bitmap); image containing the sub-images used to draw the background of the slider.

*Required.*

<a id="nbhoriz"></a>
#### nbhoriz

Number of sub-images in the horizontal direction.

Default value: 1

<a id="nbvert"></a>
#### nbvert

Number of sub-images in the vertical direction.

Default value: 1

<a id="padhoriz"></a>
#### padhoriz

Horizontal padding: number of unused pixel rows between two sub-images.

Default value: 0

<a id="padvert"></a>
#### padvert

Vertical padding: number of unused pixel lines between two sub-images.

Default value: 0

<a id="RadialSlider"></a>
### RadialSlider

Create a circular slider from a list of images with the different possible positions.

See the [common attributes](#commattr).

<a id="sequence"></a>
#### sequence

Identifiant of a [Bitmap](#Bitmap) containing the list of images of the different positions of the slider, concatenated vertically.

*Required.*

<a id="nbimages"></a>
#### nbimages

Number of elementary images contained in the sequence.

*Required.*

<a id="minangle"></a>
#### minangle

Minimum angle of the rotation, corresponding to 0%.

Default value: 0

<a id="maxangle"></a>
#### maxangle

Maximum angle of the rotation, corresponding to 100%.

Default value: 360

<a id="radialslidervalue"></a>
#### value

Variable controlled by the slider. This must be a [percentage variable](#percent), e.g "volume" or "time".

Default value: none

<a id="radialslidertooltiptext"></a>
#### tooltiptext

Tooltip associated with the slider. See also [Text variables](#textvars).

Default value:

<a id="Video"></a>
### Video

Control containing a video. This allows skinable video outputs!

> ### Note
>
> This control is still under development and its behaviour may change a lot in the future.

<a id="videowidth"></a>
#### width

Initial width of the control, in pixels.

Default value: 0

<a id="videoheight"></a>
#### height

Initial height of the control, in pixels.

Default value: 0

<a id="videoautoresize"></a>
#### autoresize

Indicate whether the layout should be automatically resized to fit the dimensions of the played video.

Default value: true

<a id="Playlist"></a>
### Playlist

This tag used to create a playlist. This tag is deprecated, you should now use [Playtree](#Playtree)

<a id="Playtree"></a>
### Playtree

Create a playlist. This tag must contain a [Slider](#Slider) tag (to allow scrolling in the playlist).

See the [common attributes](#commattr).

<a id="playlistwidth"></a>
#### width

Width of the playlist, in pixels. If playlist items are wider, the end of the name will be replaced with '...'.

Default value: 0

<a id="playlistheight"></a>
#### height

Height of the playlist, in pixels.

Default value: 0

<a id="playlistfont"></a>
#### font

Identifiant of a [Font](#Font) tag.

*Required.*

<a id="var"></a>
#### var

Type of playlist. Currently, only "playlist" is recognized, so don't bother with this attribute :)

Default value: playlist

<a id="bgimage"></a>
#### bgimage

Identifiant of a [Bitmap](#Bitmap), used as the background image. When no bitmap is specified, the background will be filled using the [bgcolor1](#bgcolor1) and [bgcolor2](#bgcolor2) attributes.

Default value: none

<a id="fgcolor"></a>
#### fgcolor

Foreground color of the playlist items.

Default value: #000000

<a id="playcolor"></a>
#### playcolor

Foreground color of the item currently played.

Default value: #FF0000

<a id="selcolor"></a>
#### selcolor

Background color of selected items.

Default value: #0000FF

<a id="bgcolor1"></a>
#### bgcolor1

Background color for odd playlist items. This attribute is ignored if the [bgimage](#bgimage) one is used.

Default value: #FFFFFF

<a id="bgcolor2"></a>
#### bgcolor2

Background color for even playlist items. This attribute is ignored if the [bgimage](#bgimage) one is used.

Default value: #FFFFFF

<a id="flat"></a>
#### flat

Boolean to indicate whether the playlist should use the tree structure or be completely "flat" (only show the leafs of the tree).

A flat playtree will work like old-style playlists.

Default value: false

<a id="itemimage"></a>
#### itemimage

Identifiant of a [Bitmap](#Bitmap) shown to the left of a leaf (playlist item).

<a id="openimage"></a>
#### openimage

Identifiant of a [Bitmap](#Bitmap) shown to the left of a node, when it is expanded.

<a id="closedimage"></a>
#### closedimage

Identifiant of a [Bitmap](#Bitmap) shown to the left of a node, when it is retracted.

<a id="actions"></a>
## Actions

There is a predefined list of actions:

- *none*: Do nothing
- *dialogs.changeSkin()*: Show a dialog box to load a new skin. This does the same as the predefined hotkey Ctrl+S.
- *dialogs.fileSimple()*: Show the simple "Open File" dialog box.
- *dialogs.file()*: Show the "Open File" dialog box, with many options (stream output, subtitles, etc...).
- *dialogs.directory()*: Show the "Open Directory" dialog box (new after VLC 0.8.2).
- *dialogs.disc()*: Show the "Open Disc" dialog box.
- *dialogs.net()*: Show the "Open Network Stream" dialog box.
- *dialogs.messages()*: Show the "Meessage" dialog box (which gives error/warning/debug messages).
- *dialogs.prefs()*: Show the "Preferences" dialog box.
- *dialogs.fileInfo()*: Show the "File Info" dialog box.
- *dialogs.playlist()*: Show the "standard" (not skinned) playlist dialog (since VLC 0.8.6).
- *dialogs.streamingWizard()*: Show the "Streaming Wizard" dialog box (new after VLC 0.8.2).
- *dialogs.popup()*: Show the full popup menu, (already available with a right-click on a [Image](#Image) control).
- *dialogs.audioPopup()*: Show the audio settings popup menu (since VLC 0.8.6).
- *dialogs.videoPopup()*: Show the video settings popup menu (since VLC 0.8.6).
- *dialogs.miscPopup()*: Show a popup menu containing playback control and general options (since VLC 0.8.6).
- *equalizer.enable()*: Enable the equalizer audio filter (since VLC 0.8.5).
- *equalizer.disable()*: Disable the equalizer audio filter (since VLC 0.8.5).
- *vlc.play()*: Play the current playlist item.
- *vlc.pause()*: Pause the current playlist item.
- *vlc.stop()*: Stop the current playlist item.
- *vlc.faster()*: Play the current playlist item faster.
- *vlc.slower()*: Play the current playlist item slower.
- *vlc.mute()*: Toggle mute/un-mute.
- *vlc.volumeUp()*: Increase the volume (since VLC 0.8.2).
- *vlc.volumeDown()*: Reduce the volume (since VLC 0.8.2).
- *vlc.fullscreen()*: Toggle the fullscreen mode.
- *vlc.snapshot()*: Take a snapshot (since VLC 0.8.5).
- *vlc.toggleRecord()*: Start/Stop recording (since VLC 1.1).
- *vlc.nextFrame()*: Advance one frame at a time (since VLC 1.1).
- *vlc.onTop()*: Toggle the "Always on top" status (since VLC 0.8.0).
- *vlc.minimize()*: Minimize VLC (since VLC 0.8.0)
- *vlc.quit()*: Quit VLC.
- *playlist.add()*: Add a new item to the playlist.
- *playlist.del()*: Remove the selected items from the playlist.
- *playlist.next()*: Go to the next playlist item.
- *playlist.previous()*: Go to the previous playlist item.
- *playlist.sort()*: Sort the playlist alphabetically.
- *playlist.setRandom(true)*: Play the playlist items in random order.
- *playlist.setRandom(false)*: Play the playlist items in the playlist order.
- *playlist.setLoop(true)*: Loop at playlist end.
- *playlist.setLoop(false)*: Do not loop at playlist end.
- *playlist.setRepeat(true)*: Repeat the current playlist item (since VLC 0.8.0).
- *playlist.setRepeat(false)*: Stop repeating the current playlist item (since VLC 0.8.0).
- *playlist.load()*: Load an external playlist file (since VLC 0.8.0).
- *playlist.save()*: Save the current playlist (since VLC 0.8.0).
- <a id="dvdactions"></a> *dvd.nextTitle()*: Go to the next title of the DVD (since VLC 0.8.5).
- *dvd.previousTitle()*: Go to the previous title of the DVD (since VLC 0.8.5).
- *dvd.nextChapter()*: Go to the next chapter of the DVD (since VLC 0.8.5).
- *dvd.previousChapter()*: Go to the previous chapter of the DVD (since VLC 0.8.5).
- *dvd.rootMenu()*: Go to the root menu of the DVD (since VLC 0.8.5).
- *WindowID.show()*: Show the [Window](#Window) whose [id](#windowid) attribute is 'WindowID'.
- *WindowID.hide()*: Hide the [Window](#Window) whose [id](#windowid) attribute is 'WindowID'.
- *WindowID.maximize()*: Maximize the [Window](#Window) whose [id](#windowid) attribute is 'WindowID'. Since VLC 0.9.0.
- *WindowID.unmaximize()*: Unmaximize the [Window](#Window) whose [id](#windowid) attribute is 'WindowID'. Since VLC 0.9.0.
- *WindowID.setLayout(LayoutID)*: Change the layout of the [Window](#Window) whose [id](#windowid) attribute is 'WindowID', using the [Layout](#Layout) whose [id](#layoutid) attribute is 'LayoutID'.

It is possible to run several actions at once, by separating them with ';'. Example: action="playlist\_id.hide(); main\_window\_id.setLayout(main\_layout)"

<a id="textvars"></a>
## Text variables

When specifying the [text](#texttext) attribute of the [Text](#Text) control or any tooltip attribute, you can insert escape sequences which will be expanded dynamically. An escape sequence always starts with the '$' character, followed by one or more predefined letters. Here is the list of accepted escape sequences:

- *$B*: Get the audio stream bitrate (in kb/s).
- *$V*: Value of the volume (from 0 to 100 --> useful for a percentage).
- *$R*: Value of the playback speed rate. Available from VLC 2.0
- *$T*: Current time (the output format is H:MM:SS).
- *$L*: Remaining time, when available (the output format is H:MM:SS).
- *$D*: Duration of the stream, when available (the output format is H:MM:SS).
- *$t, $l, $d*: Same as $T, $L and $D, except that the hour in the format is displayed only if its value is different from 0. These text variables were needed to support Winamp2 skins, but it is advised to use the uppercase ones. (Since VLC 0.8.5).
- *$H*: Value of the [help](#help) attribute of the control that is under the mouse. The main use of this escape character is to create a kind of status bar, providing contextual help.

  > ### Note
  >
  > Escape sequences in the help string are also transformed (except $H itself, it would generate an infinite loop!).
- *$N*: Name of the stream that is being played.
- *$F*: Full name (with path) of the stream that is being played.
- *$S*: Get the audio sample rate (in kHz).

Example of [tooltiptext](#slidertooltiptext) value for a slider controlling the volume: "Volume: $V%".

<a id="boolexpr"></a>
## Boolean expressions

Some attributes in the XML file require a boolean value, such as "true" or "false". But sometimes, you may need something more complex than a mere "static" boolean value. For example, let's say that you want a [Text](#Text) control to display "Pause" whenever the stream is paused. Wouldn't it be nice to show this control only when the stream is paused, and to hide it the rest of the time? Well, guess what: this is possible. You only have to set the [visible](#visible) attribute of the [Text](#Text) control to "vlc.isPaused".

Here is a list of all the dynamic statuses that can be used in boolean expressions:

- *equalizer.isEnabled*: True if the equalizer audio filter is enabled (since VLC 0.8.5).
- *vlc.hasVout*: True if a video is being played (since VLC 0.8.5).
- *vlc.hasAudio*: True if audio is being played (since VLC 0.8.5).
- *vlc.isFullscreen*: True when the video is in fullscreen mode (since VLC 0.8.5).
- *vlc.isPlaying*: True when VLC is playing, false otherwise.
- *vlc.isStopped*: True when VLC is stopped, false otherwise.
- *vlc.isPaused*: True when VLC is paused, false otherwise.
- *vlc.isSeekable*: True when the stream is seekable, false otherwise. This one can be used if you want to display a slider only when seeking is allowed.
- *vlc.canRecord*: True if the stream can be recorded, false otherwise. (since vlc1.1)
- *vlc.isRecording*: True if the stream is being recorded, false otherwise. (since vlc1.1)
- *vlc.isMute*: True when the sound is mute (in VLC, not on your OS), false otherwise.
- *vlc.isOnTop*: True when the windows have the "Always on top" status (since VLC 0.8.0).
- *playlist.isRandom*: True when the playlist items are played in a random order, false otherwise.
- *playlist.isLoop*: True when the playlist is looping, false otherwise.
- *playlist.isRepeat*: True when the current playlist item is being repeated, false otherwise (since VLC 0.8.0).
- *dvd.isActive*: True when a DVD is currently playing. This variable can be used to display buttons associated to the [dvd.\* actions](#dvdactions) only when needed (since VLC 0.8.5).
- *WindowID.isMaximized*: True when the window whose [id](#windowid) is "WindowID" is maximized, false otherwise (since VLC 0.9.0).
- *WindowID.isVisible*: True when the window whose [id](#windowid) is "WindowID" is visible, false otherwise.
- *LayoutID.isActive*: True when the layout whose [id](#layoutid) is "LayoutID" is the active layout in its window (even if the window is hidden), false otherwise (since VLC 0.8.6).

In addition to these dynamic values, you can use the constant values "true" and "false". And of course, any combination is allowed, using the "and", "or" and "not" operators. For example, supposing that you have a window named "playlist\_window", the following expression is valid (even though completely stupid):

visible="(true) and ((not playlist\_window.isVisible) and (not vlc.isStopped or false))"

<a id="percent"></a>
## Percentage variables

Every [slider](#Slider) is associated to a percentage variable, which corresponds to the position of the cursor (between 0 and 100%)

Here is a list of predefined percentage variables:

- *equalizer.band(n)*: Where 'n' is an integer between 0 and 9. When the equalizer audio filter is enabled, this value corresponds to the amplification factor of the n'th frequency band: 0% means -20 dB, and 100% means +20 dB. The frequencies corresponding to the 10 bands are: 60Hz, 170Hz, 310Hz, 600Hz, 1kHz, 3kHz, 6kHz, 12kHz, 14kHz, 16kHz. (Since VLC 0.8.5)
- *equalizer.preamp*: Preamplification value (if the equalizer audio filter is enabled). A value of 0% means -20 dB, and 100% means +20 dB. (Since VLC 0.8.5)
- *time*: Position of the stream being played.
- *volume*: Value of the volume.

<a id="layoutmodel"></a>
## Layout model

Placing the controls on a window is easy, using their x and y attributes, but these positions become insufficient for a resizable window. Some controls (or groups of controls) should stay centered in the window, others should follow the right side of the window, others should change their size automatically, etc... To handle these various behaviours, the layout model followed by the skins engine is a model based on nested boxes.

There are 2 kinds of boxes:

- *simple boxes*: These boxes cannot contain other boxes. All the visible controls are simple boxes: Image, Button, Checkbox, Text, Slider, RadialSlider, Playlist, Playtree, Video.
- *container boxes*: These boxes can contain other boxes. Only two XML tags create container boxes: [Panel](#Panel) and [Layout](#Layout). The Layout tag is necessarily the top-level box for the current layout, and cannot be contained, but Panel elements can be contained.

A box inside a container box always defines how it should react when its container box is resized. Two different mechanisms are provided: corners anchoring (useful when resizing of the inner box is wanted, for example) and constant ratio (mainly useful to keep the inner box centered inside its parent):

- *corners anchoring*: The top-left-hand corner (TL) and the bottom-right-hand corner (BR) of the inner box are "tied" to corners of the container box (TL, TR, BL or BR). When any resizing occurs, tied corners move together, which can move or resize the inner box. For example, if the TL corner of the inner box is tied to the TL corner of the container (let's write it TL/TL), and if the BR corner of the inner box is also tied to the TL corner of the container box (BR/TL), the inner box will not be resized, and will always stay at the same place (this is the default behaviour). If we have TL/TL and BR/BL, the inner box is resized vertically when its container is resized. If we have TL/TR and BR/TR, the inner box moves with the TR corner of its container. We could even define TL/BR and BR/TL, in which case increasing the size of the container box would shrink the size of the inner box... until it disappears completely!

  This mechanism is controlled by the [lefttop](#lefttop) and [rightbottom](#rightbottom) attributes of the controls.
- *constant ratio*: When a box doesn't fill completely its container box, there is space on the top, bottom, left and right of the inner box. It is possible to force the ratio between the space on the top and the space on the bottom (or the one on the left and the one on the right) to be constant. Any resizing of the container box will then move the inner box accordingly, and the size of the inner box will never change (it overrides the corners anchoring mechanism). The horizontal and vertical ratios being independent, it is for example possible to keep only the horizontal ratio constant, in which case the inner box can still resize vertically (depending on its attributes for the corners anchoring, of course).

  This mechanism is controlled by the [xkeepratio](#xkeepratio) and [ykeepratio](#ykeepratio) attributes of the controls.

<a id="idm1175"></a>
## Compression

When designing a new skin, it can be useful to organize your files in different directories. For example, you can have one directory for the fonts, another one for the images of the main window, yet another one for the images of the playlist window, etc...

But such a structure is not very convenient when it comes to distributing the skin. To address this problem, VLC is able to load skins directly from a .tar.gz archive containing all your files, along with directory information. When loading the skin, the files will be extracted in a temporary directory, then VLC will recursively search a file named "theme.xml", and the first one to be found will be used to load the skin. Of course, the temporary directory will be removed when you quit VLC or change the interface.

On Linux, creating a .tar.gz archive is rather straight-forward using the 'tar' and 'gzip' commands. On Windows, it can be done using Winzip, UltimateZip, or any other (well, almost) compression software...

Since VLC 0.8.5, it is also possible to archive your skin in a zip file.

It is advised to rename your .tar.gz (or .zip) archive with the .vlt extension, for 2 main reasons:

- on some systems, the "Change skin" dialog box only displays files which have a .vlt or .xml extension,
- in the future, .vlt files could be associated to VLC in such a way that double-clicking a .vlt file would automatically load the skin in VLC.

<a id="bezier"></a>
## Bezier curves

One cool thing with VLC sliders is that they are not necessarily rectilinear, but they can follow any Bezier curve. So if you want to have a slider moving on a half-circle, or even doing a loop, you can!

This is not the place to explain how Bezier curves work (see [http://astronomy.swin.edu.au/~pbourke/curves/bezier/](http://astronomy.swin.edu.au/~pbourke/curves/bezier/) for a nice introduction), the main thing to know is that a Bezier curve can be characterized by a set of points. Once you have them (thanks to the [CurveMaker](#CurveMaker) utility for example), you just need to enter the list of points in the [points](#sliderpoints) attribute. Here is an example with 3 points: points="(2,50),(45,120),(88,50)".

Bezier curves can be used with the [Slider](#Slider) and [Anchor](#Anchor) tags:

- For sliders, it defines the curve followed by the cursor of the slider. This curve is of course invisible, so if you want a visible background for your [Slider](#Slider) you need to provide it yourself using a [SliderBackground](#SliderBackground) or [Image](#Image) tag.
- For anchors, the use of Bezier curves is more anecdotic. Its purpose is to have non-ponctual anchor, the whole curve becoming the anchor. In this case, a ponctual anchor (and only a ponctual one) can be attracted by any point of the curve, if it is in its range of action. In fact, you can consider the curve as an easy way to define at once many anchors that share the same properties (except their position, of course :)).

> ### Note
>
> The coordinates are relative to the upper-left corner of the control (i.e. to its [x](#x) and [y](#y) attributes).

<a id="idm1209"></a>
## Tools and advice

<a id="CurveMaker"></a>
### Generating Bezier curves

To generate easily Bezier curves, you can use the [CurveMaker](https://images.videolan.org/vlc/skins/VLC-curve-maker.exe) utility (sorry, this is for Windows users only). Basically, you add and remove points at will, and you can move them to see how the curve evolves. When you have reached the perfect curve, you just have to copy-paste the list of abscissas and ordinates to form the [points](#sliderpoints) attribute of your [Slider](#Slider) or [Anchor](#Anchor). The curve-maker also allows to load a .bmp file, this could be useful if you want to follow a specific pattern of a slider, for example.

> ### Note
>
> This tool was made for the first version of the skins and has not been modified since then. This explains why it does not use PNG files and why it does not generate directly the value of the [points](#sliderpoints) attribute.

<a id="idm1221"></a>
### Using VLC warning messages

VLC is able to give warnings and error messages about a loaded skin if it finds problems in the XML file. This can be very helpful to detect syntax errors (a tag which is never closed for example), or incorrect values of attributes. Here is how to see these messages:

- on Linux, simply start VLC from a console with the following command-line: 'vlc -I skins2 -v' (you can use -vv if you want to see debug messages too),
- on Windows it is more difficult to use the same method (but you can use it with a rxvt console, in the Cygwin environment). Then another solution is to start VLC with a command-line such as 'vlc -I skins2 -v --extraintf logger'. This should open both VLC and a log window containing the messages. What's more, the logs should be saved in a file named 'vlc-log.txt', in VLC installation directory. The relevant lines are those starting with "\[00000178\] skins2 interface" (the number may be different).

<a id="idm1229"></a>
### Relative paths

For the Bitmap tags, do not use absolute paths but relative paths (they are relative to the XML file directory), so that your skin can be reused by anybody without a particular file structure.

<a id="idm1232"></a>
### Get inspiration

In order to use plainly the possibilities given, you should look at how existing skins are made, it may give you ideas for your own skins... You can also consult [on the wiki](http://wiki.videolan.org/DefaultSkinRequirements) a list of features usually expected from a skin.

<a id="idm1236"></a>
### Submit your skin!

Once your skin is finished, you can share it with other people. The easiest way is probably to send it by email to vlc -at- videolan -dot- org, so that we can put it on the website with the other ones. You can also post it in the [skins forum](http://forum.videolan.org/viewforum.php?f=15). Feel free to ask for support there if you have any problems...
