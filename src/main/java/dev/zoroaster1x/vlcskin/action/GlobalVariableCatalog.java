package dev.zoroaster1x.vlcskin.action;

import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

/**
 * Names and labels of the variables the preview can simulate.
 */
public final class GlobalVariableCatalog {

    /**
     * A text variable such as $T with a sample value.
     */
    public record TextVariable(String token, String label, String sample) {
    }

    /**
     * A boolean variable with a label and its default.
     */
    public record BooleanVariable(String name, String label, boolean defaultValue) {
    }

    public static final java.util.List<BooleanVariable> BOOLEANS = java.util.List.of(
            new BooleanVariable("equalizer.isEnabled", "Equalizer enabled", false),
            new BooleanVariable("vlc.hasVout", "Video output present", false),
            new BooleanVariable("vlc.hasAudio", "Has audio", true),
            new BooleanVariable("vlc.isFullscreen", "Fullscreen", false),
            new BooleanVariable("vlc.isPlaying", "Playing", false),
            new BooleanVariable("vlc.isStopped", "Stopped", false),
            new BooleanVariable("vlc.isPaused", "Paused", true),
            new BooleanVariable("vlc.isSeekable", "Seekable", true),
            new BooleanVariable("vlc.isMute", "Mute", false),
            new BooleanVariable("vlc.isOnTop", "Always on top", false),
            new BooleanVariable("vlc.canRecord", "Can record", true),
            new BooleanVariable("vlc.isRecording", "Recording", false),
            new BooleanVariable("playlist.isRandom", "Random", false),
            new BooleanVariable("playlist.isLoop", "Loop", false),
            new BooleanVariable("playlist.isRepeat", "Repeat", true),
            new BooleanVariable("dvd.isActive", "DVD active", false));

    public static final java.util.List<TextVariable> TEXTS = java.util.List.of(
            new TextVariable("$B", "Audio bitrate", "128"),
            new TextVariable("$V", "Volume", "50"),
            new TextVariable("$T", "Time (with hours)", "0:55:55"),
            new TextVariable("$t", "Time", "55:55"),
            new TextVariable("$L", "Remaining time (with hours)", "0:44:44"),
            new TextVariable("$l", "Remaining time", "44:44"),
            new TextVariable("$D", "Duration (with hours)", "0:99:99"),
            new TextVariable("$d", "Duration", "99:99"),
            new TextVariable("$H", "Help text", "Help text"),
            new TextVariable("$N", "Stream title", "Artist - Title"),
            new TextVariable("$F", "Full stream path", "http://www.example.com/Artist - Title.mp3"),
            new TextVariable("$S", "Sample rate", "44"));

    private GlobalVariableCatalog() {
    }

    public static Set<String> knownNames() {
        Set<String> names = new LinkedHashSet<>();
        BOOLEANS.forEach(variable -> names.add(variable.name()));
        TEXTS.forEach(variable -> names.add(variable.token()));
        return names;
    }

    public static Map<String, String> sampleTexts() {
        Map<String, String> samples = new LinkedHashMap<>();
        TEXTS.forEach(variable -> samples.put(variable.token(), variable.sample()));
        return samples;
    }
}
