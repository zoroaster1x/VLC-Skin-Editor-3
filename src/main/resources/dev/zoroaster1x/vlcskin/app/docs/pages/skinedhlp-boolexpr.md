---
title: "Boolean expressions"
source: https://images.videolan.org/vlc/skinedhlp/boolexpr.html
crawled: 2026-10-03
---

[VLC Skin Editor - Online Help](index.md) > Documentation > Boolean expressions

# Boolean expressions

A boolean expression is a combination of certain boolean variables (variables whose value is either TRUE or FALSE). You can combine such variables via the operators AND, OR and NOT. Also you can nest several boolean expressions by using brackets.

For example the expression "*(not vlc.isPlaying) or vlc.isMute*" would resolve to TRUE if VLC plays nothing or if the playback is muted.

### Boolean variables

Besides the constant boolean values **true** and **false** the following boolean variables can be used in VLC.

- **equalizer.isEnabled**: True if the equalizer audio filter is enabled.
- **vlc.hasVout**: True if a video is being played.
- **vlc.hasAudio**: True if audio is being played.
- **vlc.isFullscreen**: True when the video is in fullscreen mode.
- **vlc.isPlaying**: True when VLC is playing, false otherwise.
- **vlc.isStopped**: True when VLC is stopped, false otherwise.
- **vlc.isPaused**: True when VLC is paused, false otherwise.
- **vlc.isSeekable**: True when the stream is seekable, false otherwise. This one can be used if you want to display a slider only when seeking is allowed.
- **vlc.isMute**: True when the sound is mute (in VLC, not on your OS), false otherwise.
- **vlc.isOnTop**: True when the windows have the "Always on top" status.
- **playlist.isRandom**: True when the playlist items are played in a random order, false otherwise.
- **playlist.isLoop**: True when the playlist is looping, false otherwise.
- **playlist.isRepeat**: True when the current playlist item is being repeated, false otherwise.
- **dvd.isActive**: True when a DVD is currently playing.
- **WindowID.isMaximized**: True when the window whose ID is *WindowID* is maximized, false otherwise.
- **WindowID.isVisible**: True when the window whose ID is *WindowID* is visible, false otherwise.
- **LayoutID.isVisible**: True when the layout whose ID is *LayoutID* is the active layout in its window (even if the window is hidden), false otherwise.

© 2008 All rights reserved to the VideoLAN team.
