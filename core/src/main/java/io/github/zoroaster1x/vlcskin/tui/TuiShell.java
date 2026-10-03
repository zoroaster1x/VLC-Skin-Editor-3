package io.github.zoroaster1x.vlcskin.tui;

import io.github.zoroaster1x.vlcskin.describe.GeometryNode;
import io.github.zoroaster1x.vlcskin.describe.LayoutDescription;
import io.github.zoroaster1x.vlcskin.edit.EditorSession;
import io.github.zoroaster1x.vlcskin.mcp.EditorService;
import io.github.zoroaster1x.vlcskin.model.SkinLayout;
import io.github.zoroaster1x.vlcskin.model.SkinWindow;
import io.github.zoroaster1x.vlcskin.model.item.Item;
import io.github.zoroaster1x.vlcskin.model.resource.Resource;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * The command driven TUI: no full screen mode, so it stays usable over SSH and
 * is testable by feeding it lines. Output is plain text plus optional ANSI art.
 */
public final class TuiShell {

    private final EditorService service;
    private final boolean color;

    public TuiShell(EditorService service, boolean color) {
        this.service = service;
        this.color = color;
    }

    public EditorService service() {
        return service;
    }

    public String banner() {
        return "VLC Skin Studio TUI. Type help for commands.\n";
    }

    public String execute(String line) {
        if (line == null || line.isBlank()) {
            return "";
        }
        List<String> parts = split(line);
        String command = parts.get(0).toLowerCase();
        try {
            return switch (command) {
                case "help", "?" -> help();
                case "open" -> open(parts);
                case "new" -> service.newSkin(parts.size() > 1 ? parts.get(1) : "Untitled").text() + "\n";
                case "info" -> info();
                case "tree" -> tree(parts);
                case "items" -> items(parts);
                case "show" -> show(parts);
                case "render", "render!" -> render(parts, command.equals("render!"));
                case "vars" -> vars();
                case "set" -> set(parts);
                case "validate" -> service.validate().text() + "\n";
                case "save" -> service.save(parts.size() > 1 ? parts.get(1) : null).text() + "\n";
                case "actions" -> actions();
                case "examples" -> examples();
                case "example" -> example(parts);
                case "quit", "exit" -> "Goodbye.\n";
                default -> "Unknown command \"" + command + "\". Type help.\n";
            };
        } catch (Exception ex) {
            return "Error: " + ex.getMessage() + "\n";
        }
    }

    private String help() {
        return """
                Commands:
                  open <skin.xml>        open a skin
                  new [name]             start a new skin
                  info                   theme metadata and counts
                  tree [window] [layout] layout geometry as a table
                  items [type]           list items
                  show <id>              all attributes of an item
                  render [zoom]          preview as terminal art (render! for color)
                  vars                   preview variables
                  set <name> <value>     set a boolean variable or slider
                  validate               list problems
                  save [path]            save the skin
                  actions                list action codes
                  examples               list built in examples
                  example <id> [folder]  create and open an example
                  help                   this text
                  quit                   leave
                """;
    }

    private String open(List<String> parts) {
        if (parts.size() < 2) {
            return "Usage: open <skin.xml>\n";
        }
        return service.open(parts.get(1)).text() + "\n";
    }

    private String info() {
        Map<String, Object> info = service.documentInfo();
        StringBuilder out = new StringBuilder();
        info.forEach((key, value) -> {
            if (!(value instanceof List)) {
                out.append(String.format("%-14s %s%n", key + ":", value));
            }
        });
        return out.toString();
    }

    private String tree(List<String> parts) {
        String window = parts.size() > 1 ? parts.get(1) : null;
        String layout = parts.size() > 2 ? parts.get(2) : null;
        var outcome = service.layoutTree(window, layout, 1);
        if (!(outcome.structured() instanceof LayoutDescription description)) {
            return outcome.text() + "\n";
        }
        StringBuilder out = new StringBuilder();
        out.append("Layout ").append(description.layoutId()).append("  ")
                .append(description.layoutWidth()).append('x').append(description.layoutHeight()).append('\n');
        out.append(String.format("%-22s %-14s %5s %5s %5s %5s %s%n", "id", "type", "x", "y", "w", "h", "flags"));
        for (GeometryNode node : description.items()) {
            out.append(String.format("%-22s %-14s %5d %5d %5d %5d %s%n",
                    indent(node) + node.id(), node.type(), node.x(), node.y(), node.width(), node.height(),
                    node.visible() ? "" : "hidden"));
        }
        return out.toString();
    }

    private String indent(GeometryNode node) {
        return "  ".repeat(node.depth());
    }

    private String items(List<String> parts) {
        String type = parts.size() > 1 ? parts.get(1) : null;
        var outcome = service.listItems(type, null, null);
        StringBuilder out = new StringBuilder(outcome.text()).append('\n');
        if (outcome.structured() instanceof Map<?, ?> map && map.get("items") instanceof List<?> list) {
            for (Object entry : list) {
                if (entry instanceof Map<?, ?> item) {
                    out.append(String.format("%-22s %-14s %s,%s%n", item.get("id"), item.get("type"),
                            item.get("x"), item.get("y")));
                }
            }
        }
        return out.toString();
    }

    private String show(List<String> parts) {
        if (parts.size() < 2) {
            return "Usage: show <id>\n";
        }
        return service.getItem(parts.get(1)).text() + "\n";
    }

    private String render(List<String> parts, boolean forceColor) {
        int zoom = 1;
        for (int i = 1; i < parts.size(); i++) {
            try {
                zoom = Integer.parseInt(parts.get(i));
            } catch (NumberFormatException ignored) {
                // Not a zoom argument; ignore.
            }
        }
        var outcome = service.renderLayout(null, null, Math.max(1, Math.min(8, zoom)), null);
        if (outcome.error() || !outcome.hasPng()) {
            return outcome.text() + "\n";
        }
        try {
            BufferedImage image = javax.imageio.ImageIO.read(new java.io.ByteArrayInputStream(outcome.png()));
            int columns = 96;
            return color || forceColor ? AsciiRenderer.renderColor(image, columns)
                    : AsciiRenderer.renderMono(image, columns);
        } catch (Exception ex) {
            return "Could not decode the render: " + ex.getMessage() + "\n";
        }
    }

    private String vars() {
        EditorSession session = service.session();
        StringBuilder out = new StringBuilder();
        session.variables().booleans().forEach((name, value) -> out.append(String.format("%-24s %s%n", name, value)));
        out.append(String.format("%-24s %s%n", "sliderValue", session.variables().sliderValue()));
        session.variables().texts().forEach((name, value) -> out.append(String.format("%-24s %s%n", name, value)));
        return out.toString();
    }

    private String set(List<String> parts) {
        if (parts.size() < 3) {
            return "Usage: set <name> <value>\n";
        }
        String name = parts.get(1);
        String value = String.join(" ", parts.subList(2, parts.size()));
        EditorSession session = service.session();
        if ("sliderValue".equalsIgnoreCase(name) || "slider".equalsIgnoreCase(name)) {
            session.variables().setSliderValue(Float.parseFloat(value));
            session.fireChanged();
            return "sliderValue = " + session.variables().sliderValue() + "\n";
        }
        if (session.variables().booleans().containsKey(name)) {
            session.variables().setBoolean(name, Boolean.parseBoolean(value));
            session.fireChanged();
            return name + " = " + session.variables().getBoolean(name) + "\n";
        }
        if (session.variables().texts().containsKey(name)) {
            session.variables().setText(name, value);
            session.fireChanged();
            return name + " = " + session.variables().getText(name) + "\n";
        }
        return "Unknown variable \"" + name + "\"\n";
    }

    private String actions() {
        StringBuilder out = new StringBuilder();
        for (var action : io.github.zoroaster1x.vlcskin.action.ActionCatalog.actions()) {
            out.append(String.format("%-32s %s%n", action.code(), action.display()));
        }
        return out.toString();
    }

    private String examples() {
        return service.listExamples().text() + "\n";
    }

    private String example(List<String> parts) {
        if (parts.size() < 2) {
            return "Usage: example <id> [folder]\n";
        }
        return service.createExample(parts.get(1), parts.size() > 2 ? parts.get(2) : null).text() + "\n";
    }

    private static List<String> split(String line) {
        List<String> parts = new ArrayList<>();
        StringBuilder current = new StringBuilder();
        boolean quoted = false;
        for (char c : line.trim().toCharArray()) {
            if (c == '"') {
                quoted = !quoted;
            } else if (Character.isWhitespace(c) && !quoted) {
                if (!current.isEmpty()) {
                    parts.add(current.toString());
                    current.setLength(0);
                }
            } else {
                current.append(c);
            }
        }
        if (!current.isEmpty()) {
            parts.add(current.toString());
        }
        return parts;
    }

    /**
     * Items in the current layout, for completion.
     */
    public List<String> itemIds() {
        List<String> ids = new ArrayList<>();
        for (Item item : service.session().index().allItems()) {
            ids.add(item.getId());
        }
        return ids;
    }

    /**
     * Resource ids, for completion.
     */
    public List<String> resourceIds() {
        List<String> ids = new ArrayList<>();
        for (Resource resource : service.session().theme().getResources()) {
            ids.add(resource.getId());
        }
        return ids;
    }

    /**
     * Window and layout ids, for completion.
     */
    public List<String> layoutIds() {
        List<String> ids = new ArrayList<>();
        for (SkinWindow window : service.session().theme().getWindows()) {
            ids.add(window.getId());
            for (SkinLayout layout : window.getLayouts()) {
                ids.add(layout.getId());
            }
        }
        return ids;
    }
}
