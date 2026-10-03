package io.github.zoroaster1x.vlcskin.app.panel;

import io.github.zoroaster1x.vlcskin.app.Studio;
import io.github.zoroaster1x.vlcskin.app.i18n.Messages;
import io.github.zoroaster1x.vlcskin.edit.ValueCommand;
import io.github.zoroaster1x.vlcskin.model.SkinLayout;
import io.github.zoroaster1x.vlcskin.model.item.Item;
import io.github.zoroaster1x.vlcskin.model.item.SliderItem;
import io.github.zoroaster1x.vlcskin.render.BezierPath;
import io.github.zoroaster1x.vlcskin.render.HitTester;
import io.github.zoroaster1x.vlcskin.render.RenderOptions;
import java.awt.CardLayout;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseWheelEvent;
import java.awt.image.BufferedImage;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JToggleButton;
import javax.swing.SwingUtilities;

/**
 * The preview canvas: paints the layout, selects the topmost item under the
 * pointer and moves items or slider control points with undoable commands.
 * The rendered image is cached per document revision, zoom and highlight.
 */
public final class CanvasPanel extends JPanel {

    public enum Tool {
        MOVE,
        PATH
    }

    private final Studio studio;
    private final CardLayout cards = new CardLayout();
    private final JPanel cardHost = new JPanel(cards);
    private final Surface surface;
    private final WelcomeCard welcome;
    private final JPanel controls = new JPanel(new java.awt.FlowLayout(java.awt.FlowLayout.LEFT, 6, 4));
    private final JLabel zoomLabel = new JLabel();
    private final JToggleButton moveButton = new JToggleButton(Messages.get("TOOLBAR_MOVE", "Move"));
    private final JToggleButton pathButton = new JToggleButton(Messages.get("TOOLBAR_PATH", "Path"));
    private Tool tool = Tool.MOVE;
    private Item hover;
    private Item pressed;
    private int frameTick;
    private final javax.swing.Timer animator;
    private boolean dragging;
    private boolean pathDragging;
    private boolean shiftDown;
    private boolean altDown;
    private int dragStartX;
    private int dragStartY;
    private int itemStartX;
    private int itemStartY;
    private int pathIndex = -1;
    private String pathOldPoints;

    private BufferedImage cache;
    private long cacheRevision = -1;
    private int cacheZoom = -1;
    private Item cacheSelection;
    private Item cacheHover;
    private Item cachePressed;
    private int cacheTick = -1;

    public CanvasPanel(Studio studio) {
        this.studio = studio;
        this.welcome = new WelcomeCard(studio);
        this.surface = new Surface();
        setLayout(new java.awt.BorderLayout());
        cardHost.add(surface, "canvas");
        cardHost.add(welcome, "welcome");
        add(cardHost, java.awt.BorderLayout.CENTER);
        add(buildControls(), java.awt.BorderLayout.SOUTH);
        setFocusable(true);
        animator = new javax.swing.Timer(100, e -> {
            frameTick++;
            invalidateCache();
            surface.repaint();
        });
        animator.setCoalesce(true);
        installMouse();
    }

    private JPanel buildControls() {
        controls.setOpaque(false);
        moveButton.setSelected(true);
        moveButton.setToolTipText(Messages.get("TOOLBAR_MOVE", "Select and move items"));
        pathButton.setToolTipText(Messages.get("TOOLBAR_PATH",
                "Drag slider control points; shift adds a point, alt removes one"));
        moveButton.addActionListener(e -> {
            setTool(Tool.MOVE);
            syncToolButtons();
        });
        pathButton.addActionListener(e -> {
            setTool(Tool.PATH);
            syncToolButtons();
        });
        JButton zoomOut = new JButton(io.github.zoroaster1x.vlcskin.app.component.Icons.of("zoom-out", 14));
        zoomOut.setToolTipText(Messages.get("APP_CANVAS_ZOOM_OUT", "Zoom out"));
        zoomOut.addActionListener(e -> zoomOut());
        JButton zoomIn = new JButton(io.github.zoroaster1x.vlcskin.app.component.Icons.of("zoom-in", 14));
        zoomIn.setToolTipText(Messages.get("APP_CANVAS_ZOOM_IN", "Zoom in"));
        zoomIn.addActionListener(e -> zoomIn());
        JButton fit = new JButton(Messages.get("APP_CANVAS_FIT", "Fit"));
        fit.addActionListener(e -> fitToWindow());
        zoomLabel.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 8, 0, 8));
        controls.add(moveButton);
        controls.add(pathButton);
        controls.add(javax.swing.Box.createHorizontalStrut(12));
        controls.add(zoomOut);
        controls.add(zoomLabel);
        controls.add(zoomIn);
        controls.add(fit);
        updateZoomLabel();
        return controls;
    }

    private void updateZoomLabel() {
        zoomLabel.setText(Messages.format("APP_CANVAS_ZOOM", "Zoom %ix", studio.settings().getCanvasZoom()));
    }

    private void syncToolButtons() {
        moveButton.setSelected(tool == Tool.MOVE);
        pathButton.setSelected(tool == Tool.PATH);
    }

    public Tool tool() {
        return tool;
    }

    public void setTool(Tool tool) {
        this.tool = tool;
        setCursor(Cursor.getPredefinedCursor(tool == Tool.MOVE ? Cursor.DEFAULT_CURSOR : Cursor.CROSSHAIR_CURSOR));
        surface.setCursor(getCursor());
        if (moveButton != null) {
            syncToolButtons();
        }
    }

    /**
     * Shows the canvas or the welcome card and repaints.
     */
    public void refresh() {
        SkinLayout layout = studio.session().currentLayout();
        cards.show(cardHost, layout == null ? "welcome" : "canvas");
        controls.setVisible(layout != null);
        if (layout == null) {
            welcome.refresh();
            animator.stop();
        } else {
            int fps = io.github.zoroaster1x.vlcskin.render.ImageStore.animationFps(studio.session().index());
            if (fps > 0) {
                animator.setDelay(Math.max(16, 1000 / fps));
                animator.start();
            } else {
                animator.stop();
                frameTick = 0;
            }
        }
        updateZoomLabel();
        invalidateCache();
        revalidate();
        repaint();
    }

    private void invalidateCache() {
        cache = null;
        cacheRevision = -1;
    }

    private RenderOptions options() {
        int zoom = Math.max(1, studio.settings().getCanvasZoom());
        return studio.session().renderOptions()
                .withZoom(zoom)
                .withHover(hover)
                .withPressed(pressed)
                .withCheckerboard(studio.settings().isCheckerboard())
                .withFrameTick(frameTick);
    }

    private BufferedImage image(SkinLayout layout, RenderOptions options) {
        long revision = studio.session().revision();
        if (cache != null && cacheRevision == revision && cacheZoom == options.zoom()
                && cacheSelection == options.selection() && cacheHover == options.hover()
                && cachePressed == options.pressed() && cacheTick == options.frameTick()) {
            return cache;
        }
        cache = studio.session().renderer().render(layout, options);
        cacheRevision = revision;
        cacheZoom = options.zoom();
        cacheSelection = options.selection();
        cacheHover = options.hover();
        cachePressed = options.pressed();
        cacheTick = options.frameTick();
        return cache;
    }

    private Point toLayout(Point point) {
        Rectangle bounds = canvasBounds();
        int zoom = Math.max(1, studio.settings().getCanvasZoom());
        return new Point((point.x - bounds.x) / zoom, (point.y - bounds.y) / zoom);
    }

    private void installMouse() {
        MouseAdapter adapter = new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                surface.requestFocusInWindow();
                if (SwingUtilities.isRightMouseButton(e)) {
                    showContextMenu(e);
                    return;
                }
                shiftDown = e.isShiftDown();
                altDown = e.isAltDown();
                Point layoutPoint = toLayout(e.getPoint());
                Item hit = hitTest(layoutPoint);
                if (hit == null) {
                    studio.session().selection().clearItem();
                    studio.session().fireChanged();
                    return;
                }
                studio.session().selection().selectItem(hit.getId());
                if (tool == Tool.PATH) {
                    studio.session().fireChanged();
                    beginPathDrag(hit, layoutPoint);
                    return;
                }
                if (tool == Tool.MOVE) {
                    pressed = hit;
                    invalidateCache();
                }
                studio.session().fireChanged();
                dragging = true;
                dragStartX = layoutPoint.x;
                dragStartY = layoutPoint.y;
                itemStartX = hit.getX();
                itemStartY = hit.getY();
            }

            @Override
            public void mouseDragged(MouseEvent e) {
                Point layoutPoint = toLayout(e.getPoint());
                if (pathDragging) {
                    updatePathDrag(layoutPoint);
                    return;
                }
                if (!dragging) {
                    return;
                }
                Item selected = studio.session().selection().item(studio.session().index());
                if (selected == null) {
                    return;
                }
                selected.setX(itemStartX + layoutPoint.x - dragStartX);
                selected.setY(itemStartY + layoutPoint.y - dragStartY);
                invalidateCache();
                surface.repaint();
            }

            @Override
            public void mouseReleased(MouseEvent e) {
                if (pressed != null) {
                    pressed = null;
                    invalidateCache();
                }
                if (pathDragging || pathIndex >= 0) {
                    endPathDrag();
                    return;
                }
                if (!dragging) {
                    return;
                }
                dragging = false;
                Item selected = studio.session().selection().item(studio.session().index());
                if (selected == null) {
                    return;
                }
                int newX = selected.getX();
                int newY = selected.getY();
                if (newX != itemStartX || newY != itemStartY) {
                    selected.setX(itemStartX);
                    selected.setY(itemStartY);
                    studio.session().apply(ValueCommand.builder("Move " + selected.type().displayName())
                            .set(itemStartX, newX, selected::setX)
                            .set(itemStartY, newY, selected::setY)
                            .build());
                }
                studio.session().fireChanged();
            }

            @Override
            public void mouseMoved(MouseEvent e) {
                Item hit = hitTest(toLayout(e.getPoint()));
                if (hit != hover) {
                    hover = hit;
                    invalidateCache();
                    surface.repaint();
                }
                if (tool == Tool.PATH && hit instanceof SliderItem) {
                    surface.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
                } else {
                    surface.setCursor(Cursor.getDefaultCursor());
                }
            }

            @Override
            public void mouseWheelMoved(MouseWheelEvent e) {
                if (!e.isControlDown()) {
                    return;
                }
                int zoom = studio.settings().getCanvasZoom();
                zoom = Math.max(1, Math.min(16, zoom + (e.getWheelRotation() < 0 ? 1 : -1)));
                studio.settings().setCanvasZoom(zoom);
                refresh();
            }
        };
        surface.addMouseListener(adapter);
        surface.addMouseMotionListener(adapter);
        surface.addMouseWheelListener(adapter);
    }

    private Item hitTest(Point point) {
        SkinLayout layout = studio.session().currentLayout();
        if (layout == null) {
            return null;
        }
        HitTester tester = new HitTester(studio.session().index(), studio.session().images(),
                studio.session().variables());
        return tester.topmost(layout, point.x, point.y);
    }

    private void beginPathDrag(Item hit, Point point) {
        if (!(hit instanceof SliderItem slider)) {
            return;
        }
        BezierPath path = parsePoints(slider);
        if (path == null) {
            return;
        }
        for (int i = 0; i < path.controlCount(); i++) {
            int dx = path.controlX(i) + slider.getX() - point.x;
            int dy = path.controlY(i) + slider.getY() - point.y;
            if (dx * dx + dy * dy <= 16) {
                if (altDown) {
                    removeControlPoint(slider, path, i);
                } else {
                    pathIndex = i;
                    pathOldPoints = slider.getPoints();
                    pathDragging = true;
                }
                return;
            }
        }
        if (shiftDown) {
            addControlPoint(slider, path, point.x - slider.getX(), point.y - slider.getY());
        }
    }

    private BezierPath parsePoints(SliderItem slider) {
        try {
            return BezierPath.parse(slider.getPoints());
        } catch (IllegalArgumentException ex) {
            return null;
        }
    }

    private void addControlPoint(SliderItem slider, BezierPath path, int x, int y) {
        int[] xs = new int[path.controlCount() + 1];
        int[] ys = new int[path.controlCount() + 1];
        for (int i = 0; i < path.controlCount(); i++) {
            xs[i] = path.controlX(i);
            ys[i] = path.controlY(i);
        }
        xs[xs.length - 1] = x;
        ys[ys.length - 1] = y;
        String newPoints = BezierPath.format(xs, ys);
        String oldPoints = slider.getPoints();
        studio.session().apply(ValueCommand.builder("Add slider point")
                .step(() -> slider.setPoints(newPoints), () -> slider.setPoints(oldPoints))
                .build());
    }

    private void removeControlPoint(SliderItem slider, BezierPath path, int index) {
        if (path.controlCount() <= 2) {
            return;
        }
        int[] xs = new int[path.controlCount() - 1];
        int[] ys = new int[path.controlCount() - 1];
        int cursor = 0;
        for (int i = 0; i < path.controlCount(); i++) {
            if (i == index) {
                continue;
            }
            xs[cursor] = path.controlX(i);
            ys[cursor] = path.controlY(i);
            cursor++;
        }
        String newPoints = BezierPath.format(xs, ys);
        String oldPoints = slider.getPoints();
        studio.session().apply(ValueCommand.builder("Remove slider point")
                .step(() -> slider.setPoints(newPoints), () -> slider.setPoints(oldPoints))
                .build());
    }

    private void updatePathDrag(Point point) {
        Item selected = studio.session().selection().item(studio.session().index());
        if (!(selected instanceof SliderItem slider)) {
            return;
        }
        BezierPath path = parsePoints(slider);
        if (path == null || pathIndex < 0 || pathIndex >= path.controlCount()) {
            return;
        }
        int[] xs = new int[path.controlCount()];
        int[] ys = new int[path.controlCount()];
        for (int i = 0; i < path.controlCount(); i++) {
            xs[i] = path.controlX(i);
            ys[i] = path.controlY(i);
        }
        xs[pathIndex] = point.x - slider.getX();
        ys[pathIndex] = point.y - slider.getY();
        slider.setPoints(BezierPath.format(xs, ys));
        invalidateCache();
        surface.repaint();
    }

    private void endPathDrag() {
        Item selected = studio.session().selection().item(studio.session().index());
        if (selected instanceof SliderItem slider && pathOldPoints != null
                && !pathOldPoints.equals(slider.getPoints())) {
            String newPoints = slider.getPoints();
            String oldPoints = pathOldPoints;
            slider.setPoints(oldPoints);
            studio.session().apply(ValueCommand.builder("Move slider point")
                    .step(() -> slider.setPoints(newPoints), () -> slider.setPoints(oldPoints))
                    .build());
        }
        pathDragging = false;
        pathIndex = -1;
        pathOldPoints = null;
    }

    private void showContextMenu(MouseEvent event) {
        Point layoutPoint = toLayout(event.getPoint());
        Item hit = hitTest(layoutPoint);
        if (hit != null) {
            studio.session().selection().selectItem(hit.getId());
            studio.session().fireChanged();
        }
        javax.swing.JPopupMenu menu = new javax.swing.JPopupMenu();
        if (hit != null) {
            javax.swing.JMenuItem duplicate = new javax.swing.JMenuItem(Messages.get("WIN_ITEMS_COPY", "Duplicate"));
            duplicate.addActionListener(e -> studio.service().duplicateItem(hit.getId()));
            javax.swing.JMenuItem delete = new javax.swing.JMenuItem(Messages.get("WIN_ITEMS_DELETE", "Delete"));
            delete.addActionListener(e -> studio.service().deleteItem(hit.getId()));
            javax.swing.JMenuItem front = new javax.swing.JMenuItem(Messages.get("APP_CANVAS_FRONT", "Bring to front"));
            front.addActionListener(e -> bringToFront(hit));
            menu.add(duplicate);
            menu.add(delete);
            menu.add(front);
            menu.addSeparator();
        }
        javax.swing.JMenuItem fit = new javax.swing.JMenuItem(Messages.get("APP_CANVAS_FIT_WINDOW", "Fit window"));
        fit.addActionListener(e -> fitToWindow());
        menu.add(fit);
        menu.show(surface, event.getX(), event.getY());
    }

    /**
     * Moves an item to the end of its list, which is the top of the z order.
     */
    public void bringToFront(Item item) {
        java.util.List<Item> list = studio.session().index().parentListOf(item.getId());
        if (list != null && list.indexOf(item) < list.size() - 1) {
            int from = list.indexOf(item);
            studio.session().apply(new io.github.zoroaster1x.vlcskin.edit.commands.ReorderCommand<>(
                    list, from, list.size() - 1, "Bring " + item.type().displayName() + " to front"));
        }
    }

    public void fitToWindow() {
        SkinLayout layout = studio.session().currentLayout();
        if (layout == null) {
            return;
        }
        int zoom = Math.max(1, Math.min(16,
                Math.min(surface.getWidth() / Math.max(1, layout.getWidth()),
                        surface.getHeight() / Math.max(1, layout.getHeight()))));
        studio.settings().setCanvasZoom(zoom);
        refresh();
    }

    public void zoomIn() {
        studio.settings().setCanvasZoom(Math.min(16, studio.settings().getCanvasZoom() + 1));
        refresh();
    }

    public void zoomOut() {
        studio.settings().setCanvasZoom(Math.max(1, studio.settings().getCanvasZoom() - 1));
        refresh();
    }

    public JPanel canvasComponent() {
        return surface;
    }

    /**
     * The centered canvas rectangle, in component pixels.
     */
    public Rectangle canvasBounds() {
        SkinLayout layout = studio.session().currentLayout();
        if (layout == null) {
            return new Rectangle(0, 0, 0, 0);
        }
        int zoom = Math.max(1, studio.settings().getCanvasZoom());
        Dimension size = studio.session().renderer().sizeOf(layout, zoom);
        int width = surface.getWidth() > 0 ? surface.getWidth() : getWidth();
        int height = surface.getHeight() > 0 ? surface.getHeight() : getHeight();
        int x = Math.max(0, (width - size.width) / 2);
        int y = Math.max(0, (height - size.height) / 2);
        return new Rectangle(x, y, size.width, size.height);
    }

    /**
     * The painted surface; the layout image lives here, not on the card panel.
     */
    private final class Surface extends JPanel {

        Surface() {
            setOpaque(true);
            setBackground(new Color(0x16, 0x18, 0x1D));
            setFocusable(true);
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            SkinLayout layout = studio.session().currentLayout();
            if (layout == null) {
                return;
            }
            RenderOptions options = options();
            Rectangle bounds = new Rectangle(0, 0, 0, 0);
            Dimension size = studio.session().renderer().sizeOf(layout, options.zoom());
            int x = Math.max(0, (getWidth() - size.width) / 2);
            int y = Math.max(0, (getHeight() - size.height) / 2);
            bounds.setBounds(x, y, size.width, size.height);
            BufferedImage image = image(layout, options);
            g.drawImage(image, bounds.x, bounds.y, null);
            g.setColor(new Color(255, 255, 255, 24));
            g.drawRect(bounds.x - 1, bounds.y - 1, bounds.width + 1, bounds.height + 1);
        }
    }
}
