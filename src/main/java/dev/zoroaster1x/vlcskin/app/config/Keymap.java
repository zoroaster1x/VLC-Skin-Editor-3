package dev.zoroaster1x.vlcskin.app.config;

import dev.zoroaster1x.vlcskin.util.Platform;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import javax.swing.KeyStroke;

/**
 * The actions whose keyboard shortcuts can be changed in Preferences, plus
 * parsing and formatting for the stored form. A stroke is stored as the string
 * KeyStroke understands ("control shift S"); an empty string means deliberately
 * unbound, a missing entry means the default.
 */
public final class Keymap {

    /**
     * One changeable shortcut: an id, the label key it maps to, and the
     * default stroke.
     */
    public record Binding(String id, String labelKey, String fallback, String defaultStroke) {
    }

    public static final List<Binding> BINDINGS = List.of(
            new Binding("file.new", "MENU_FILE_NEW", "New", "control N"),
            new Binding("file.open", "MENU_FILE_OPEN", "Open...", "control O"),
            new Binding("file.save", "MENU_FILE_SAVE", "Save", "control S"),
            new Binding("file.browse", "APP_GALLERY_MENU", "Browse themes...", "control B"),
            new Binding("file.exportVlt", "MENU_FILE_VLT", "Export as VLT...", "control shift V"),
            new Binding("file.testVlc", "MENU_FILE_TEST", "Test skin in VLC", "control shift T"),
            new Binding("edit.undo", "MENU_EDIT_UNDO", "Undo", "control Z"),
            new Binding("edit.redo", "MENU_EDIT_REDO", "Redo", "control Y"),
            new Binding("edit.skinSettings", "MENU_EDIT_THEME", "Skin settings", "control I"),
            new Binding("edit.variables", "MENU_EDIT_VARS", "Global variables", "control G"),
            new Binding("edit.duplicate", null, "Duplicate item", "control D"),
            new Binding("edit.delete", null, "Delete selected item", "DELETE"),
            new Binding("item.moveUp", null, "Move item up", "control UP"),
            new Binding("item.moveDown", null, "Move item down", "control DOWN"),
            new Binding("item.moveLeft", null, "Move item left", "control LEFT"),
            new Binding("item.moveRight", null, "Move item right", "control RIGHT"),
            new Binding("view.zoomIn", null, "Zoom in", "control EQUALS"),
            new Binding("view.zoomOut", null, "Zoom out", "control MINUS"),
            new Binding("view.fit", null, "Fit layout in canvas", "control 0"));

    private Keymap() {
    }

    public static String defaultStroke(String id) {
        for (Binding binding : BINDINGS) {
            if (binding.id().equals(id)) {
                return binding.defaultStroke();
            }
        }
        return null;
    }

    /**
     * The effective stroke for an id: the custom value when one is set (an
     * empty string means unbound), otherwise the default.
     */
    public static String stroke(Map<String, String> keys, String id) {
        if (keys != null && keys.containsKey(id)) {
            return keys.get(id);
        }
        return defaultStroke(id);
    }

    public static KeyStroke keyStroke(Map<String, String> keys, String id) {
        return parse(stroke(keys, id));
    }

    /**
     * Stores a new stroke for an id. A value equal to the default is removed,
     * so the stored map only carries real changes.
     */
    public static void set(Map<String, String> keys, String id, String value) {
        String normalized = normalize(value);
        if (normalized == null || normalized.isBlank()) {
            keys.put(id, "");
            return;
        }
        if (normalize(defaultStroke(id)).equals(normalized)) {
            keys.remove(id);
        } else {
            keys.put(id, normalized);
        }
    }

    public static void resetAll(Map<String, String> keys) {
        keys.clear();
    }

    /**
     * What the user may type: "Ctrl+Shift+S", "ctrl s" and "DEL" all work.
     * Modifier tokens are lowercased and key names are uppercased, because
     * KeyStroke.getKeyStroke accepts "control S" but not "control s".
     */
    public static String normalize(String text) {
        if (text == null) {
            return "";
        }
        java.util.List<String> tokens = new java.util.ArrayList<>();
        String key = null;
        for (String part : text.strip().replace("+", " ").split("\\s+")) {
            if (part.isBlank()) {
                continue;
            }
            switch (part.toLowerCase(Locale.ROOT)) {
                case "ctrl", "control" -> tokens.add("control");
                case "cmd", "command", "meta" -> tokens.add("meta");
                case "alt", "option" -> tokens.add("alt");
                case "shift" -> tokens.add("shift");
                default -> key = canonicalKey(part);
            }
        }
        if (key != null) {
            tokens.add(key);
        }
        return String.join(" ", tokens);
    }

    private static String canonicalKey(String part) {
        String upper = part.toUpperCase(Locale.ROOT);
        return switch (upper) {
            case "ESC" -> "ESCAPE";
            case "RETURN" -> "ENTER";
            case "DEL" -> "DELETE";
            case "INS" -> "INSERT";
            case "PGUP" -> "PAGE_UP";
            case "PGDN" -> "PAGE_DOWN";
            default -> upper;
        };
    }

    public static KeyStroke parse(String text) {
        String normalized = normalize(text);
        if (normalized.isBlank() || "none".equals(normalized)) {
            return null;
        }
        try {
            return KeyStroke.getKeyStroke(normalized);
        } catch (RuntimeException ex) {
            return null;
        }
    }

    /**
     * Human readable form for buttons and the menu tooltip: Ctrl+Shift+S.
     */
    public static String describe(String text) {
        KeyStroke stroke = parse(text);
        if (stroke == null) {
            return "";
        }
        StringBuilder label = new StringBuilder();
        int modifiers = stroke.getModifiers();
        if ((modifiers & InputEvent.SHIFT_DOWN_MASK) != 0) {
            label.append("Shift+");
        }
        if ((modifiers & macShortcutMask()) != 0) {
            label.append(Platform.isMac() ? "Cmd+" : "Ctrl+");
        }
        if ((modifiers & InputEvent.ALT_DOWN_MASK) != 0) {
            label.append("Alt+");
        }
        if (stroke.getKeyCode() == 0) {
            label.append(Character.toUpperCase(stroke.getKeyChar()));
        } else {
            String key = KeyEvent.getKeyText(stroke.getKeyCode());
            label.append(key.length() == 1 ? key.toUpperCase(Locale.ROOT) : key);
        }
        return label.toString();
    }

    /**
     * A snapshot of the stored map, for tests and persistence.
     */
    public static Map<String, String> copyOf(Map<String, String> keys) {
        return keys == null ? Map.of() : new LinkedHashMap<>(keys);
    }

    private static int macShortcutMask() {
        return Platform.isMac() ? InputEvent.META_DOWN_MASK : InputEvent.CTRL_DOWN_MASK;
    }

}
