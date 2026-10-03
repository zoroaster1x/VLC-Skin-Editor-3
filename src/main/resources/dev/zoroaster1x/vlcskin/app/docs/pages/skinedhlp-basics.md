---
title: "The basics of creating a skin"
source: https://images.videolan.org/vlc/skinedhlp/basics.html
crawled: 2026-10-03
---

[VLC Skin Editor - Online Help](index.md) > Tutorials > The basics of creating a skin

# The basics of creating a skin

In this tutorial you will learn the basics of creating a skin for VLC.

## Designing process

![Example](images/skinedhlp-airflow-slices.png)<br>
*Example of how your image document could look like. The red lines indicate the slices.*

First of all you have to design your skin in a graphics program like Photoshop or GIMP.

In the designing process you should think of the following components:

- A main window containing:

  - a play/pause button
  - a next and a previous button
  - a faster and a slower button
  - a stop button
  - a time slider
  - a volume slider
  - a mute button
  - text information about the current playback
  - other buttons with specific functions like shuffle,repeat or making other windows and dialogs visible
- A playlist window. Either integrate this into the main window or create a separate window for it. The playlist window should contain:

  - a button to add and a button to remove items from the playlist
  - a button to export and to import playlist files
  - a button to sort the playlist alphabetically
- An equalizer window. This might also be included into the main window or elsewhere. This window should contain:

  - A slider to control the preamplification
  - 10 sliders to control the different bands (60Hz, 170Hz, 310Hz, 600Hz, 1kHz, 3kHz, 6kHz, 12kHz, 14kHz, 16kHz)
  - A button to turn the equalizer on/off

When you create your skin also consider that each control/button has three states: normal, mouseover and clicked.

For example you could create a big image for each window, where the window with it's controls is shown three times. One for each status. Then it is recommended that you split up this image with slices. (Slices refer to a tool in Photoshop, I don't know how or whether at all this is possible in GIMP or other programs). Then save each slice, or subimage, as a PNG file.

## In the editor

Choose to create a new skin and save it in it's own folder. Into this folder you should copy all your PNG images. Then you have to make the available in the skin by clicking on the ![](images/skinedhlp-add-bitmap.png) Add Bitmap button at the bottom of the ![](images/skinedhlp-resources.png) Resources window. Select the PNG files in the file chooser. They will be added automatically as bitmap resources and identified by their file names without the extension.

Then create a new **window** by clicking on the ![](images/skinedhlp-add-window.png) Add Window button at the bottom of the ![](images/skinedhlp-windows.png) Windows window. Choose an id for the window, e.g. "Main" if it is the main window. The X and Y attributes determine where the window appears the first time the skin is opened. If you set drag-drop to true, files can be dropped into this window and will be added to the playlist. If play on drop is true the dropped files will be played immediately after they have been dropped into the window, otherwise the will be just added to the end of the playlist.

Now you'll have to add a **layout** to the window. Layouts determine different appearances of a window. For simple skins there are only needed on layout per window. Note that if you use several layouts, the last in the list will be shown the first time the skin is opened. You can modify the position of a layout in the hierarchy by selecting it an using the ![](images/skinedhlp-move-up.png) Up and ![](images/skinedhlp-move-down.png) Down buttons at the bottom of the ![](images/skinedhlp-windows.png) Windows window. You'll have to choose an id for the layout, you should always select ids that represent what is displayed. Under initial width and height you have to enter the dimensions of the window when this layout is displayed. If your window is not resizable you can ignore the other attributes and leave them at -1.

After you have added the layout it should be selected in the ![](images/skinedhlp-windows.png) Windows window and a preview window of your layout should have opened to the right. It should be filled with a gray/white tile pattern. This pattern symbolizes transparency, so the parts of your layout where you see this pattern will not be visible in VLC.

Now move on to the ![](images/skinedhlp-items.png) Items Window.

All images making up this window but should not trigger any action are represented by "Image" items. Normally a layout has at least one image item that represents the window's background. For each slice of the window that should not have any function, click on the ![](images/skinedhlp-add.png) Add Item button at the bottom of the ![](images/skinedhlp-items.png) Items Window and choose ***Image***. Enter any ID that you feel comfortable with, just note that IDs must be unique. Set the image attribute to the id of the bitmap resource representing this image. Set the X and Y attributes to the position where the image should appear. After having added the image you can also move it around with the cursor if you have selected in the ![](images/skinedhlp-items.png) Items window. The currently selected item is highlighted in the preview window by a red border. By default the *action* attribute of an image is set to *move*. This enables the user to drag the window around by clicking on the image. You can disable this by selecting the action *none*.

Controls that do not change their appearance when a certain status changed are represented by **Button** items. You should use buttons e.g. for the controls to skip to the next/previous item in the playlist etc. For the Play/Pause button you should rather select a *checkbox*, which are considered later on.

Now let us consider the button's attributes:

For the up attribute choose the bitmap resource representing the image of the buttons default state. For the hover attribute choose the bitmap resource representing the image of the buttons state when the mouse is over it. For the down attribute choose the bitmap resource representing the image of the buttons state when it is pressed. The action that should be fired by clicking on the button can be defined with the help of the ActionEditor which can be opened by clicking on the button next to the action-textfield.

**Checkboxes** are similar to buttons only that they have two states. You can choose a boolean variable from [the documentation](boolexpr.md) to set the condition of the checkbox. You have to enter for both states, if the condition resolves to true or to false, the up,hover and down bitmap resources.

To edit the **skin's settings** like author etc. choose Edit ![>](images/skinedhlp-arrowr.png) ![](images/skinedhlp-edit.png)Theme settings from the menubar.

This should be enough as a brief introduction to the usage of the skin editor. For more advanced usage consult the [documentation](index.md). There the Playtree, Video and other items are described in detail.

When you finished your skin choose File ![>](images/skinedhlp-arrowr.png) Export as VLT and submit the generated VLT file at [the skins upload form](http://www.videolan.org/vlc/skins_upload.php) so that it will be added to the skins download page.

© 2008 All rights reserved to the VideoLAN team.
