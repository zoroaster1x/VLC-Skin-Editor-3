package dev.zoroaster1x.vlcskin.render;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;

/**
 * The simulated player state the preview uses: slider position, text variables
 * and the boolean variables of skins2. Pure data, no VLC required.
 */
public final class PreviewVariables {

    private final Map<String, Boolean> booleans = new LinkedHashMap<>();
    private final Map<String, String> texts = new LinkedHashMap<>();
    private float sliderValue = 0.5f;

    public PreviewVariables() {
        booleans.put("equalizer.isEnabled", false);
        booleans.put("vlc.hasVout", false);
        booleans.put("vlc.hasAudio", true);
        booleans.put("vlc.isFullscreen", false);
        booleans.put("vlc.isPlaying", false);
        booleans.put("vlc.isStopped", false);
        booleans.put("vlc.isPaused", true);
        booleans.put("vlc.isSeekable", true);
        booleans.put("vlc.isMute", false);
        booleans.put("vlc.isOnTop", false);
        booleans.put("vlc.canRecord", true);
        booleans.put("vlc.isRecording", false);
        booleans.put("playlist.isRandom", false);
        booleans.put("playlist.isLoop", false);
        booleans.put("playlist.isRepeat", true);
        booleans.put("dvd.isActive", false);

        texts.put("$B", "128");
        texts.put("$V", "50");
        texts.put("$T", "0:55:55");
        texts.put("$t", "55:55");
        texts.put("$L", "0:44:44");
        texts.put("$l", "44:44");
        texts.put("$D", "0:99:99");
        texts.put("$d", "99:99");
        texts.put("$H", "Help text");
        texts.put("$N", "Artist - Title");
        texts.put("$F", "http://www.example.com/Artist - Title.mp3");
        texts.put("$S", "44");
        texts.put("$R", "1");
    }

    public Map<String, Boolean> booleans() {
        return booleans;
    }

    public Map<String, String> texts() {
        return texts;
    }

    public boolean getBoolean(String name) {
        return Boolean.TRUE.equals(booleans.get(name));
    }

    public void setBoolean(String name, boolean value) {
        booleans.put(name, value);
    }

    public String getText(String name) {
        return texts.get(name);
    }

    public void setText(String name, String value) {
        texts.put(name, Objects.requireNonNullElse(value, ""));
    }

    public float sliderValue() {
        return sliderValue;
    }

    public void setSliderValue(float value) {
        sliderValue = Math.max(0f, Math.min(1f, value));
    }

    /**
     * Parses a VLC boolean expression against the current state. An unknown
     * identifier makes the whole expression unresolved, and callers that use
     * this as a plain boolean (a checkbox state) read that as false.
     */
    public boolean evaluate(String expression) {
        BooleanExpression.Result result = resolve(expression);
        return result.resolved() && result.value();
    }

    /**
     * The full evaluation result, so a caller can tell false from unresolved.
     */
    public BooleanExpression.Result resolve(String expression) {
        return BooleanExpression.resolve(expression, booleans::get);
    }

    /**
     * VLC's visibility semantics: a control with no visible attribute, or with
     * an expression VLC cannot resolve, is drawn; only a resolved false hides
     * it.
     */
    public boolean visible(String expression) {
        BooleanExpression.Result result = resolve(expression);
        return !result.resolved() || result.value();
    }

    /**
     * Substitutes every text variable such as $T in a text attribute.
     */
    public String substitute(String text) {
        if (text == null) {
            return "";
        }
        String result = text;
        for (Map.Entry<String, String> entry : texts.entrySet()) {
            result = result.replace(entry.getKey(), entry.getValue());
        }
        return result;
    }
}
