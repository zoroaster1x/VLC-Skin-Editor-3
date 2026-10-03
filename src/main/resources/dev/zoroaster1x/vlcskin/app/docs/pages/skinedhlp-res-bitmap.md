---
title: "Bitmap"
source: https://images.videolan.org/vlc/skinedhlp/res-bitmap.html
crawled: 2026-10-03
---

[VLC Skin Editor - Online Help](index.md) > Documentation > Resources > Bitmap

# Bitmap

A Bitmap resource represents an image file that can be used in the skin. Only when you have added an image as a Bitmap resource, items in the skin can use it.

At the moment you can only use PNG images with the Skin Editor, although VLC supports most common graphic formats.

### Attributes

![alphacolor](images/skinedhlp-bitmap-alpha.png)<br>
*Left: original image<br>
Right: how VLC renders it*

**id**<br>
 The name identifying this Bitmap. The skin's items use this ID to refer to the image associated with that Bitmap.<br>
 This ID has to be unique and is required to be set.

**file**<br>
 The path to the image file, relative to the location of the Skin XML file. The Skin Editor automatically allows only PNGs inside the folder of the skin to be chosen.

**alphacolor**<br>
 All pixels of the image that have that color will not be painted, they will be transparent.<br>
 Note that also the transparency mask of the PNG file will be taken into consideration. But at the moment VLC does not support semi-transparency, thus only fully transparent pixels will not be drawn. Semi-transparent pixels will have a black background.

The color has to be set in hexadecimal format. The Skin Editor helps you with setting that color by providing a color chooser.<br>
 By default the alphacolor attribute is set to #000000 (black).

![](images/skinedhlp-bitmap-anim-src.png) ![](images/skinedhlp-bitmap-anim.gif)<br>
 *Left: source image<br>
Right: animation with nbframes=3 and fps=3*

### Animation

To create an animated bitmap all frames have to be stored vertically in the source image. The animation will be repeated endlessly.

**nbframes**<br>
 The number of frames. By default this is set to 1.

**fps**<br>
 The speed of the animation in frames per second. By default this is set to 0.

**See also:**<br>
 [SubBitmap](res-subbitmap.md)

© 2008 All rights reserved to the VideoLAN team.
