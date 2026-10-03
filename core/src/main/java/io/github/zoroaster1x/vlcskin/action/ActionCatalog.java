package io.github.zoroaster1x.vlcskin.action;

import java.util.List;

/**
 * The actions VLC understands, grouped the way the editor offers them.
 */
public final class ActionCatalog {

    public enum Kind {
        STATIC,
        WINDOW,
        WINDOW_STATE,
        WINDOW_LAYOUT,
        BOOLEAN
    }

    /**
     * One selectable action. WINDOW kinds need a window id, BOOLEAN a flag.
     */
    public record Action(String code, String display, Kind kind) {

        public boolean needsWindow() {
            return kind == Kind.WINDOW || kind == Kind.WINDOW_STATE || kind == Kind.WINDOW_LAYOUT;
        }
    }

    public static final String GROUP_VLC = "VLC";
    public static final String GROUP_DIALOGS = "Dialogs";
    public static final String GROUP_PLAYLIST = "Playlist";
    public static final String GROUP_DVD = "DVD";
    public static final String GROUP_SKIN = "Skin windows";

    private static final List<Action> ACTIONS = List.of(
            new Action("vlc.play()", "Play", Kind.STATIC),
            new Action("vlc.pause()", "Pause", Kind.STATIC),
            new Action("vlc.stop()", "Stop", Kind.STATIC),
            new Action("vlc.faster()", "Faster", Kind.STATIC),
            new Action("vlc.slower()", "Slower", Kind.STATIC),
            new Action("vlc.nextFrame()", "Next frame", Kind.STATIC),
            new Action("vlc.mute()", "Mute", Kind.STATIC),
            new Action("vlc.volumeUp()", "Volume up", Kind.STATIC),
            new Action("vlc.volumeDown()", "Volume down", Kind.STATIC),
            new Action("vlc.fullscreen()", "Fullscreen", Kind.STATIC),
            new Action("vlc.snapshot()", "Take snapshot", Kind.STATIC),
            new Action("vlc.toggleRecord()", "Toggle recording", Kind.STATIC),
            new Action("vlc.minimize()", "Minimize", Kind.STATIC),
            new Action("vlc.onTop()", "Toggle always on top", Kind.STATIC),
            new Action("vlc.quit()", "Quit VLC", Kind.STATIC),
            new Action("equalizer.enable()", "Enable equalizer", Kind.STATIC),
            new Action("equalizer.disable()", "Disable equalizer", Kind.STATIC),

            new Action("dialogs.changeSkin()", "Change skin dialog", Kind.STATIC),
            new Action("dialogs.fileSimple()", "Simple open dialog", Kind.STATIC),
            new Action("dialogs.file()", "Open file dialog", Kind.STATIC),
            new Action("dialogs.disc()", "Open disc dialog", Kind.STATIC),
            new Action("dialogs.net()", "Open network dialog", Kind.STATIC),
            new Action("dialogs.directory()", "Open directory dialog", Kind.STATIC),
            new Action("dialogs.messages()", "Messages dialog", Kind.STATIC),
            new Action("dialogs.fileInfo()", "Media information", Kind.STATIC),
            new Action("dialogs.prefs()", "Preferences", Kind.STATIC),
            new Action("dialogs.playlist()", "Playlist dialog", Kind.STATIC),
            new Action("dialogs.streamingWizard()", "Streaming wizard", Kind.STATIC),
            new Action("dialogs.popup()", "Popup dialog", Kind.STATIC),
            new Action("dialogs.audioPopup()", "Audio popup", Kind.STATIC),
            new Action("dialogs.videoPopup()", "Video popup", Kind.STATIC),
            new Action("dialogs.miscPopup()", "Misc popup", Kind.STATIC),

            new Action("playlist.add()", "Add to playlist", Kind.STATIC),
            new Action("playlist.del()", "Remove from playlist", Kind.STATIC),
            new Action("playlist.next()", "Next item", Kind.STATIC),
            new Action("playlist.previous()", "Previous item", Kind.STATIC),
            new Action("playlist.setRandom()", "Random", Kind.BOOLEAN),
            new Action("playlist.setLoop()", "Loop", Kind.BOOLEAN),
            new Action("playlist.setRepeat()", "Repeat", Kind.BOOLEAN),
            new Action("playlist.sort()", "Sort playlist", Kind.STATIC),
            new Action("playlist.load()", "Load playlist", Kind.STATIC),
            new Action("playlist.save()", "Save playlist", Kind.STATIC),

            new Action("dvd.nextTitle()", "Next title", Kind.STATIC),
            new Action("dvd.previousTitle()", "Previous title", Kind.STATIC),
            new Action("dvd.nextChapter()", "Next chapter", Kind.STATIC),
            new Action("dvd.previousChapter()", "Previous chapter", Kind.STATIC),
            new Action("dvd.rootMenu()", "Root menu", Kind.STATIC),

            new Action(".show()", "Show window", Kind.WINDOW),
            new Action(".hide()", "Hide window", Kind.WINDOW),
            new Action(".maximize()", "Maximize window", Kind.WINDOW_STATE),
            new Action(".unmaximize()", "Unmaximize window", Kind.WINDOW_STATE),
            new Action(".setLayout()", "Switch layout", Kind.WINDOW_LAYOUT)
    );

    private ActionCatalog() {
    }

    public static List<Action> actions() {
        return ACTIONS;
    }

    public static List<Action> inGroup(String group) {
        return switch (group) {
            case GROUP_VLC -> ACTIONS.stream().filter(a -> !a.code().contains(".") || a.code().startsWith("vlc.")).toList();
            case GROUP_DIALOGS -> ACTIONS.stream().filter(a -> a.code().startsWith("dialogs.")).toList();
            case GROUP_PLAYLIST -> ACTIONS.stream().filter(a -> a.code().startsWith("playlist.")).toList();
            case GROUP_DVD -> ACTIONS.stream().filter(a -> a.code().startsWith("dvd.")).toList();
            default -> List.of();
        };
    }

    /**
     * A human sentence for one action code, or the code itself when unknown.
     */
    public static String describe(String code) {
        if (code == null || code.isBlank() || "none".equals(code)) {
            return "Do nothing";
        }
        for (Action action : ACTIONS) {
            if (action.code().equals(code)) {
                return action.display();
            }
        }
        if (code.endsWith(".show()")) {
            return "Show window " + code.substring(0, code.length() - ".show()".length());
        }
        if (code.endsWith(".hide()")) {
            return "Hide window " + code.substring(0, code.length() - ".hide()".length());
        }
        if (code.endsWith(".maximize()")) {
            return "Maximize window " + code.substring(0, code.length() - ".maximize()".length());
        }
        if (code.endsWith(".unmaximize()")) {
            return "Unmaximize window " + code.substring(0, code.length() - ".unmaximize()".length());
        }
        if (code.contains(".setLayout(")) {
            return "Switch layout: " + code;
        }
        return code;
    }
}
