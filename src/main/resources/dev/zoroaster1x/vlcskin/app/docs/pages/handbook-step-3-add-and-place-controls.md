---
title: Step 3: add and place controls
section: Making a skin
source: rewritten
---

# Step 3: add and place controls

Everything on a layout is an item. The Items panel is the catalog and the outline; the canvas is where you place things and see the result.

## Adding items

1. Select the layout in the Windows and layouts tree so the canvas shows it.
2. In the Items panel, press the Add item button, or right-click in the tree and pick Add item. The menu offers Anchor, Button, Checkbox, Group, Image, Panel, Playlist, Playtree, Radial slider, Slider, Text and Video.
3. The new item is inserted at `10, 10`, on the layout itself or inside the selected container. If a Group or Panel is selected it becomes the parent; a Slider added while a Playlist or Playtree is selected becomes that list's scrollbar. A SliderBackground is not in the menu; it is enabled from the slider's Inspector.

New items have generated ids and no bitmap reference yet. Give them a bitmap in the Inspector, or point the fields at resources you added first.

## Selecting and dragging on the canvas

- The Move tool is selected by default. A click selects the topmost item under the pointer; a click on empty space clears the selection. The selected item is outlined and the Inspector follows it.
- Drag a selected item to move it. The drag lands as one undo step, no matter how far you drag.
- The canvas context menu on an item offers Duplicate, Delete and Bring to front, plus Fit window on any point of the surface.

## Position and size in the Inspector

The General section of every item starts with:

| Field | Meaning |
|---|---|
| ID | The item name. Item ids share one namespace across the whole theme. |
| X, Y | Position in pixels, relative to the parent container. |
| Visible | A boolean expression such as `vlc.isPlaying` or `not vlc.isPaused`. |
| Left top anchor, Right bottom anchor | Which container corners the item follows when the layout resizes. |
| Keep x ratio, Keep y ratio | Keep the relative position on that axis instead of following an anchor. |
| Help | Help text for the `$H` variable. |

Width and height live in the type's own section when the type has them: Panel, Video, Playlist and Text. An Image or a Button takes its size from the bitmap.

## Arrow key nudges

With an item selected, Ctrl and an arrow key moves it one pixel. Repeated nudges of the same item coalesce into a single undo step, so a long run of arrow presses does not flood the history. The same four moves are in the Edit menu as Move selected item entries.

## Duplicate, delete and reorder

- Ctrl+D or Edit > Duplicate item copies the selection. A prompt asks for the rename pattern of the copy; `%oldid%` is replaced by the old id, and the default pattern is `%oldid%_copy`. The duplicate keeps every attribute except the id.
- Delete removes the selection after a confirmation. It acts on the tree with keyboard focus, so the same key deletes an item, a resource, a layout or a window depending on where you are. The editor refuses to delete the last window or the last layout, and it refuses to delete a resource that items still reference.
- Move up and Move down on the Items toolbar change the z order. Bring to front on the canvas context menu does the same for the topmost position.

## Drag and drop between groups and panels

Items can be reorganized by dragging in the Items tree:

- Drop on a Group or Panel to move the item inside it.
- Drop between two rows to insert it at that position in the list.
- A node can never be dropped into its own subtree, and a Slider refuses to take children.
- A playlist's slider and a slider background have a fixed place and cannot be dragged at all.

Dropping onto a leaf item, such as a Button, inserts the dragged item next to it rather than inside it.

## The action editor

Buttons, checkboxes and images run actions. Select the item and find the Action row in the Inspector: the text field holds the action chain, and the round button at its right opens the Actions dialog.

- The list shows each code with its description, for example `Play [vlc.play()]`.
- Add action opens grouped menus: VLC, Dialogs, Playlist, DVD and Skin windows. Boolean actions ask whether to activate or deactivate the flag. Window actions ask which window, and `setLayout` asks for the window and then one of its layouts.
- Remove deletes the selected row; Up and Down reorder it. The chain runs in order, left to right.
- Double-click a row to edit the raw code by hand. This is how you keep or build a code the catalog does not list; unknown codes are preserved as written.
- Do nothing clears the chain, Cancel leaves the old value, OK applies the list.

You can also type into the Inspector's action field directly. The full code catalog and the boolean expressions used by Visible and State are in [Actions and variables](actions-and-variables.md).

## Tooltips and help text

- `tooltiptext` exists on Button, Checkbox, Slider and Radial slider. It draws a small tooltip near the pointer in VLC. The editor preview does not draw tooltips.
- `help` exists on every item as a General field. While the pointer is over that item in VLC, any Text item that contains `$H` shows its help string, which is the usual way to build a status line.
- Both accept the `$` text variables, so `Volume: $V%` works in a tooltip.

Next: [Step 4: text and variables](step-4-text-and-variables.md).
