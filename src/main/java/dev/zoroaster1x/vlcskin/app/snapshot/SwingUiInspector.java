package dev.zoroaster1x.vlcskin.app.snapshot;

import dev.zoroaster1x.vlcskin.app.Studio;
import dev.zoroaster1x.vlcskin.model.SkinLayout;
import dev.zoroaster1x.vlcskin.snapshot.UiInspector;
import dev.zoroaster1x.vlcskin.util.Json;
import java.awt.Component;
import java.awt.Container;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;
import javax.imageio.ImageIO;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JTabbedPane;
import javax.swing.SwingUtilities;

/**
 * Reports the live Swing tree as data and paints it to a PNG. Works for the
 * real window and for the headless assembly, so the MCP server can always
 * describe the UI.
 */
public final class SwingUiInspector implements UiInspector {

    private final Supplier<Component> rootSupplier;
    private final Studio studio;
    private final String label;
    private final Supplier<dev.zoroaster1x.vlcskin.app.panel.CanvasPanel> canvasSupplier;
    private final java.util.function.Consumer<String> panelShower;

    public SwingUiInspector(Supplier<Component> rootSupplier, Studio studio, String label) {
        this(rootSupplier, studio, label, null, null);
    }

    public SwingUiInspector(Supplier<Component> rootSupplier, Studio studio, String label,
                            Supplier<dev.zoroaster1x.vlcskin.app.panel.CanvasPanel> canvasSupplier,
                            java.util.function.Consumer<String> panelShower) {
        this.rootSupplier = rootSupplier;
        this.studio = studio;
        this.label = label;
        this.canvasSupplier = canvasSupplier;
        this.panelShower = panelShower;
    }

    @Override
    public boolean applyTheme(String themeId) {
        if (!dev.zoroaster1x.vlcskin.app.theme.ThemeManager.themes().containsKey(themeId)) {
            return false;
        }
        dev.zoroaster1x.vlcskin.app.theme.ThemeManager.apply(themeId);
        studio.settings().setTheme(themeId);
        Component root = rootSupplier.get();
        if (root != null) {
            SwingUtilities.updateComponentTreeUI(root);
            root.repaint();
        }
        studio.session().fireChanged();
        return true;
    }

    @Override
    public boolean showPanel(String panelName) {
        if (panelShower == null) {
            return false;
        }
        panelShower.accept(panelName);
        return true;
    }

    @Override
    public boolean setCanvas(Integer zoom, String tool, Boolean checkerboard) {
        if (canvasSupplier == null) {
            if (zoom != null) {
                studio.settings().setCanvasZoom(Math.max(1, Math.min(16, zoom)));
                studio.saveSettings();
                return true;
            }
            if (checkerboard != null) {
                studio.settings().setCheckerboard(checkerboard);
                studio.saveSettings();
                return true;
            }
            return false;
        }
        var canvas = canvasSupplier.get();
        if (canvas == null) {
            return false;
        }
        if (zoom != null) {
            studio.settings().setCanvasZoom(Math.max(1, Math.min(16, zoom)));
        }
        if (tool != null) {
            canvas.setTool("path".equalsIgnoreCase(tool)
                    ? dev.zoroaster1x.vlcskin.app.panel.CanvasPanel.Tool.PATH
                    : dev.zoroaster1x.vlcskin.app.panel.CanvasPanel.Tool.MOVE);
        }
        if (checkerboard != null) {
            studio.settings().setCheckerboard(checkerboard);
        }
        canvas.refresh();
        studio.saveSettings();
        return true;
    }

    @Override
    public boolean openSettings() {
        new dev.zoroaster1x.vlcskin.app.dialog.ThemeSettingsDialog(studio).setVisible(true);
        return true;
    }

    @Override
    public java.util.Map<String, String> preferences() {
        var settings = studio.settings();
        java.util.Map<String, String> values = new java.util.LinkedHashMap<>();
        values.put("theme", settings.getTheme());
        values.put("language", settings.getLanguage());
        values.put("checkerboard", Boolean.toString(settings.isCheckerboard()));
        values.put("showToolbar", Boolean.toString(settings.isShowToolbar()));
        values.put("canvasZoom", Integer.toString(settings.getCanvasZoom()));
        return values;
    }

    @Override
    public boolean setPreference(String key, String value) {
        var settings = studio.settings();
        switch (key.toLowerCase(java.util.Locale.ROOT)) {
            case "theme" -> {
                return applyTheme(value);
            }
            case "language" -> {
                dev.zoroaster1x.vlcskin.app.i18n.Messages.setLanguage(value);
                settings.setLanguage(value);
            }
            case "checkerboard" -> {
                settings.setCheckerboard(Boolean.parseBoolean(value));
                studio.session().fireChanged();
            }
            case "showtoolbar" -> settings.setShowToolbar(Boolean.parseBoolean(value));
            case "canvaszoom" -> settings.setCanvasZoom(Math.max(1, Math.min(16, Integer.parseInt(value))));
            default -> {
                return false;
            }
        }
        studio.saveSettings();
        return true;
    }

    @Override
    public boolean available() {
        return rootSupplier.get() != null;
    }

    @Override
    public String summary() {
        String file = studio.session().file() == null ? "untitled" : studio.session().file().getFileName().toString();
        return label + " showing " + file + (studio.session().isDirty() ? " (unsaved)" : "");
    }

    @Override
    public String describeUi() {
        Component root = rootSupplier.get();
        if (root == null) {
            return "{}";
        }
        Map<String, Object> description = new LinkedHashMap<>();
        description.put("window", root instanceof java.awt.Window window
                ? Map.of("title", window instanceof java.awt.Frame frame ? frame.getTitle() : label,
                        "width", window.getWidth(), "height", window.getHeight())
                : Map.of("title", label, "width", root.getWidth(), "height", root.getHeight()));
        description.put("theme", studio.settings().getTheme());
        description.put("dark", studio.isDarkTheme());
        description.put("file", studio.session().file() == null ? null : studio.session().file().toString());
        description.put("dirty", studio.session().isDirty());
        description.put("panels", panels(root));
        description.put("selection", selection());
        description.put("canvas", canvasInfo());
        description.put("problems", problemCount());
        return Json.write(description);
    }

    private List<Map<String, Object>> panels(Component root) {
        List<Map<String, Object>> panels = new ArrayList<>();
        walk(root, root, panels);
        return panels;
    }

    private void walk(Component root, Component component, List<Map<String, Object>> panels) {
        Object name = null;
        if (component instanceof JComponent jComponent) {
            name = jComponent.getClientProperty("panelName");
        }
        if (name != null) {
            Rectangle bounds = SwingUtilities.convertRectangle(component.getParent(), component.getBounds(), root);
            Map<String, Object> entry = new LinkedHashMap<>();
            entry.put("name", name.toString());
            entry.put("class", component.getClass().getSimpleName());
            entry.put("x", bounds.x);
            entry.put("y", bounds.y);
            entry.put("width", bounds.width);
            entry.put("height", bounds.height);
            entry.put("visible", component.isVisible());
            entry.put("text", firstText(component));
            entry.put("proportionOfWindow", root.getWidth() == 0 || root.getHeight() == 0 ? 0
                    : Math.round(100.0 * bounds.width * bounds.height / (root.getWidth() * (double) root.getHeight())));
            panels.add(entry);
        }
        if (component instanceof Container container) {
            for (Component child : container.getComponents()) {
                walk(root, child, panels);
            }
        }
    }

    private String firstText(Component component) {
        if (component instanceof JLabel label) {
            return label.getText();
        }
        if (component instanceof JTabbedPane tabs) {
            return "tabs: " + tabs.getTabCount();
        }
        if (component instanceof Container container) {
            for (Component child : container.getComponents()) {
                String text = firstText(child);
                if (text != null && !text.isBlank()) {
                    return text;
                }
            }
        }
        return null;
    }

    private Map<String, Object> selection() {
        Map<String, Object> selection = new LinkedHashMap<>();
        var state = studio.session().selection();
        selection.put("window", state.windowId());
        selection.put("layout", state.layoutId());
        selection.put("item", state.itemId());
        selection.put("resource", state.resourceId());
        return selection;
    }

    private Map<String, Object> canvasInfo() {
        Map<String, Object> canvas = new LinkedHashMap<>();
        SkinLayout layout = studio.session().currentLayout();
        if (layout != null) {
            canvas.put("width", layout.getWidth());
            canvas.put("height", layout.getHeight());
            canvas.put("zoom", studio.settings().getCanvasZoom());
            canvas.put("items", layout.getItems().size());
        }
        return canvas;
    }

    private int problemCount() {
        return dev.zoroaster1x.vlcskin.format.SkinValidator.validate(studio.session().theme(),
                studio.session().file() == null ? null : studio.session().file().getParent()).size();
    }

    @Override
    public byte[] screenshotPng() {
        Component root = rootSupplier.get();
        if (root == null || root.getWidth() <= 0 || root.getHeight() <= 0) {
            return null;
        }
        try {
            if (!SwingUtilities.isEventDispatchThread()) {
                BufferedImage[] holder = new BufferedImage[1];
                SwingUtilities.invokeAndWait(() -> holder[0] = paint(root));
                return encode(holder[0]);
            }
            return encode(paint(root));
        } catch (Exception ex) {
            return null;
        }
    }

    private BufferedImage paint(Component root) {
        BufferedImage image = new BufferedImage(root.getWidth(), root.getHeight(), BufferedImage.TYPE_INT_RGB);
        Graphics2D g = image.createGraphics();
        try {
            root.paint(g);
        } finally {
            g.dispose();
        }
        return image;
    }

    private byte[] encode(BufferedImage image) throws Exception {
        if (image == null) {
            return null;
        }
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        ImageIO.write(image, "png", out);
        return out.toByteArray();
    }
}
