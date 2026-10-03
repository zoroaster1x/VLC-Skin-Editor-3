---
title: "Percentage variables"
source: https://images.videolan.org/vlc/skinedhlp/percent.html
crawled: 2026-10-03
---

[VLC Skin Editor - Online Help](index.md) > Documentation > Percentage variables

# Percentage variables

These variables serve as values for Slider items and determine their state and effect.

### Playback control

**time**<br>
 This variable controls the current position of the playback of the current file, track or stream.

### Audio control

**volume**<br>
 This variable controls the volume of the current playback.

**equalizer.band(n)**<br>
 This set of variables control the amplification factors of the 10 frequency bands of the equalizer.<br>
 When the equalizer audio filter is enabled, the value of the slider corresponds to the amplification factor of the n'th frequency band: 0% mean -20 dB, and 100% mean +20 dB.<br>
 n has to be an integer from 0 to 9. The correspoding frequencies to the bands 0 - 9 are:<br>
 0 - 60Hz<br>
 1 - 170Hz<br>
 2 - 310Hz<br>
 3 - 600Hz<br>
 4 - 1kHz,<br>
 5 - 3kHz<br>
 6 - 6kHz<br>
 7 - 12kHz<br>
 8 - 14kHz<br>
 9 - 16kHz

**equalizer.preamp**<br>
 This variable controls the preamplification value of the equalizer audio filter, when it is activated. Analogue to the equalizer bands, 0% mean -20 dB and 100% mean +20dB.

**See also:**<br>
 [Slider](i-slider.md)

© 2008 All rights reserved to the VideoLAN team.
