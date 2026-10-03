---
title: Actions and variables
section: Appendix: the format explained
source: rewritten
---

# Actions and variables

This page is the reference for what an action chain can contain and what the boolean and percentage variables mean. The buttons, checkboxes, images and popup menu items that carry actions are edited through the [action editor](step-3-add-and-place-controls.md); the codes below are what it writes.

## Action chains

An action attribute holds one or more action codes separated by semicolons:

```
vlc.play();mywindow.show();mywindow.setLayout(other)
```

The codes run in order, left to right. `none` means do nothing, and empty segments between semicolons are ignored. The editor parses a chain into a list so you can reorder it, and unknown codes are kept exactly as written and written back on save. A theme that carries an action this editor does not know still works, and you can see and edit the raw code in the Actions dialog by double-clicking an entry.

Open the dialog from the action row in the Inspector. It offers Add action, Remove, Up and Down, and a grouped menu of the known codes.

## The catalog

### VLC

| Code | Meaning |
|---|---|
| `vlc.play()` | Play the current item. |
| `vlc.pause()` | Pause. |
| `vlc.stop()` | Stop. |
| `vlc.faster()` | Play faster. |
| `vlc.slower()` | Play slower. |
| `vlc.nextFrame()` | Advance one frame. |
| `vlc.mute()` | Toggle mute. |
| `vlc.volumeUp()` | Raise the volume. |
| `vlc.volumeDown()` | Lower the volume. |
| `vlc.fullscreen()` | Toggle fullscreen. |
| `vlc.snapshot()` | Take a snapshot. |
| `vlc.toggleRecord()` | Start or stop recording. |
| `vlc.minimize()` | Minimize VLC. |
| `vlc.onTop()` | Toggle always on top. |
| `vlc.quit()` | Quit VLC. |
| `equalizer.enable()` | Enable the equalizer filter. |
| `equalizer.disable()` | Disable the equalizer filter. |

### Dialogs

| Code | Dialog |
|---|---|
| `dialogs.changeSkin()` | Change skin. |
| `dialogs.fileSimple()` | Simple open file. |
| `dialogs.file()` | Open file with all options. |
| `dialogs.disc()` | Open disc. |
| `dialogs.net()` | Open network stream. |
| `dialogs.directory()` | Open directory. |
| `dialogs.messages()` | Messages. |
| `dialogs.fileInfo()` | Media information. |
| `dialogs.prefs()` | Preferences. |
| `dialogs.playlist()` | The standard playlist dialog. |
| `dialogs.streamingWizard()` | Streaming wizard. |
| `dialogs.popup()` | Full popup menu. |
| `dialogs.audioPopup()` | Audio popup menu. |
| `dialogs.videoPopup()` | Video popup menu. |
| `dialogs.miscPopup()` | Misc popup menu. |

### Playlist

| Code | Meaning |
|---|---|
| `playlist.add()` | Add an item. |
| `playlist.del()` | Remove the selected items. |
| `playlist.next()` | Next item. |
| `playlist.previous()` | Previous item. |
| `playlist.sort()` | Sort alphabetically. |
| `playlist.load()` | Load a playlist file. |
| `playlist.save()` | Save the playlist. |
| `playlist.setRandom(true)` / `(false)` | Random order on or off. |
| `playlist.setLoop(true)` / `(false)` | Loop at the end on or off. |
| `playlist.setRepeat(true)` / `(false)` | Repeat the current item on or off. |

### DVD

| Code | Meaning |
|---|---|
| `dvd.nextTitle()` | Next title. |
| `dvd.previousTitle()` | Previous title. |
| `dvd.nextChapter()` | Next chapter. |
| `dvd.previousChapter()` | Previous chapter. |
| `dvd.rootMenu()` | Root menu. |

### Skin windows

| Code | Meaning |
|---|---|
| `windowId.show()` | Show that window. |
| `windowId.hide()` | Hide that window. |
| `windowId.maximize()` | Maximize it. |
| `windowId.unmaximize()` | Restore it. |
| `windowId.setLayout(layoutId)` | Switch to one of its layouts. |

Replace `windowId` with the `id` of a `Window` in the theme, and `layoutId` with the id of one of its layouts. For example, a button on the main window can hide the playlist window with `playlist_window.hide()`.

## Boolean expressions

Attributes such as `visible` and a checkbox's `state` accept a boolean expression, not just `true` or `false`. The grammar is small:

- Names, `true` and `false` are values.
- `not` negates the value to its right.
- `and` and `or` combine two values; `not` binds tightest, then `and`, then `or`.
- Parentheses group anything.

```
(not vlc.isPlaying) or vlc.isMute
```

The editor evaluates expressions against its simulated player state. A name it does not know evaluates to false, so a typo hides an item rather than breaking the theme.

### Global variables

| Variable | True when |
|---|---|
| `equalizer.isEnabled` | The equalizer filter is on. |
| `vlc.hasVout` | Video output is present. |
| `vlc.hasAudio` | The stream has audio. |
| `vlc.isFullscreen` | Video is fullscreen. |
| `vlc.isPlaying` | Playback is running. |
| `vlc.isStopped` | Playback is stopped. |
| `vlc.isPaused` | Playback is paused. |
| `vlc.isSeekable` | The stream can be seeked. |
| `vlc.isMute` | The volume is muted. |
| `vlc.isOnTop` | Windows are always on top. |
| `vlc.canRecord` | The stream can be recorded. |
| `vlc.isRecording` | Recording is running. |
| `playlist.isRandom` | Random play is on. |
| `playlist.isLoop` | The playlist loops. |
| `playlist.isRepeat` | The current item repeats. |
| `dvd.isActive` | A DVD is playing. |

Window and layout names can be qualified the same way: `windowId.isVisible`, `windowId.isMaximized` and `layoutId.isActive` are resolved by VLC at runtime. The preview cannot simulate these, because only the fixed list above has a switch in the Variables panel. One older help page spells the layout test `layoutId.isVisible`; VLC's parser is the authority on which one a given VLC version accepts.

## Percentage variables

Sliders use these instead of booleans, and their value runs from 0 to 100 percent in VLC:

| Variable | Controls |
|---|---|
| `time` | Playback position. |
| `volume` | Volume. |
| `equalizer.band(n)` | Equalizer band `n`, from `0` to `9`. |
| `equalizer.preamp` | Equalizer preamplification. |

See [Sliders](sliders.md) for how a slider is bound to one of these. Text items have their own `$` variables, listed in [Text items](text-items.md).

Next: [Format reference](format-reference.md).
