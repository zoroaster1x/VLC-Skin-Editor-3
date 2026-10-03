---
title: Popup menus and ini files
section: Appendix: the format explained
source: rewritten
---

# Popup menus and ini files

Two theme level resources exist mainly for VLC itself: `PopupMenu`, which defines a named menu, and `IniFile`, which points at a helper configuration file. This page is reference; the editor keeps both intact through a load and save cycle, but only the Skin XML panel edits menu entries.

## PopupMenu

| Attribute | Default | Meaning |
|---|---|---|
| `id` | required | Name of the menu. |

A popup menu must contain at least one entry. Two child elements are allowed:

| Child | Attributes | Meaning |
|---|---|---|
| `MenuItem` | `label` required, `action` default `none` | A clickable line. |
| `MenuSeparator` | none | A divider line. |

The `action` of a menu item uses the same codes as a button action, so a menu can call `vlc.play()`, `dialogs.prefs()`, a window action, or a chain of several codes separated by semicolons. See [Actions and variables](actions-and-variables.md) for the catalog.

```xml
<PopupMenu id="main_menu">
  <MenuItem label="Play" action="vlc.play()"/>
  <MenuSeparator/>
  <MenuItem label="Preferences" action="dialogs.prefs()"/>
</PopupMenu>
```

### What the editor can edit

The editor parses the whole menu into its model: every entry, its label and action, and every separator. All of it is written back on save, including attributes and child elements it does not recognize.

The dedicated UI is thin. The Resources panel lists a popup menu under Other so you can select it, and the Inspector shows a note that the resource has no editable attributes there. The `id` can be changed through the MCP tool `set_resource_property`. Entry editing has no form; use the Skin XML panel, edit the `PopupMenu` block and press Apply. Over MCP, `add_resource` can create an empty popup menu but nothing adds entries to it, so an existing or hand-written menu is the practical path.

## IniFile

| Attribute | Default | Meaning |
|---|---|---|
| `id` | required | Name of the ini file resource. |
| `file` | required | Path to the file, relative to the theme folder. |

VLC reads the ini file for defaults when it loads the theme. The editor keeps the reference so a round trip never drops it, and the Inspector edits both `id` and `file`. The Resources panel shows it under Other. The editor does not read or display the contents of the ini file itself.

Next: [Actions and variables](actions-and-variables.md).
