package dev.zoroaster1x.vlcskin.app.i18n;

import dev.zoroaster1x.vlcskin.app.panel.CanvasPanel;

/**
 * Panel titles in one place so the desktop dock and the headless layout always
 * agree and pick up translations.
 */
public final class PanelTitles {

    private PanelTitles() {
    }

    public static String resources() {
        return Messages.get("WIN_RES_TITLE", "Resources");
    }

    public static String windows() {
        return Messages.get("WIN_WIN_TITLE", "Windows and layouts");
    }

    public static String items() {
        return Messages.get("WIN_ITEMS_TITLE", "Items");
    }

    public static String canvas() {
        return Messages.get("WIN_PREVIEW_TITLE", "Canvas");
    }

    public static String inspector() {
        return Messages.get("WIN_ITEM_GENERAL", "Inspector");
    }

    public static String variables() {
        return Messages.get("MENU_EDIT_VARS", "Variables");
    }

    public static String problems() {
        return Messages.get("WIN_PROBLEMS_TITLE", "Problems");
    }

    public static String xml() {
        return Messages.get("WIN_XML_TITLE", "Skin XML");
    }

    public static String toolbarLabel(CanvasPanel.Tool tool) {
        return tool == CanvasPanel.Tool.MOVE
                ? Messages.get("TOOLBAR_MOVE", "Move tool")
                : Messages.get("TOOLBAR_PATH", "Path tool");
    }
}
