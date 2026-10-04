package dev.zoroaster1x.vlcskin.app.component;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Component;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.geom.Arc2D;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Path2D;
import java.awt.geom.RoundRectangle2D;
import javax.swing.Icon;
import javax.swing.UIManager;

/**
 * Hand drawn vector icons. They take the component foreground at paint time so
 * one icon works in every theme, and the set stays free of stock icon looks.
 */
public final class Icons {

    private Icons() {
    }

    public static Icon of(String name) {
        return new VectorIcon(name, 16);
    }

    public static Icon of(String name, int size) {
        return new VectorIcon(name, size);
    }

    private record VectorIcon(String name, int size) implements Icon {

        @Override
        public void paintIcon(Component c, Graphics g, int x, int y) {
            Graphics2D g2 = (Graphics2D) g.create(x, y, size, size);
            try {
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                Color color = c != null ? c.getForeground() : UIManager.getColor("Label.foreground");
                if (color == null) {
                    color = Color.GRAY;
                }
                boolean disabled = c != null && !c.isEnabled();
                if (disabled) {
                    color = UIManager.getColor("Label.disabledForeground") != null
                            ? UIManager.getColor("Label.disabledForeground")
                            : new Color(color.getRed(), color.getGreen(), color.getBlue(), 120);
                }
                g2.setColor(color);
                float scale = size / 16f;
                g2.scale(scale, scale);
                g2.setStroke(new BasicStroke(1.4f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
                paint(g2, name);
            } finally {
                g2.dispose();
            }
        }


        private void line(Graphics2D g, double x1, double y1, double x2, double y2) {
            g.draw(new java.awt.geom.Line2D.Double(x1, y1, x2, y2));
        }

        private void paint(Graphics2D g, String name) {
            switch (name) {
                case "new" -> {
                    Path2D page = new Path2D.Float();
                    page.moveTo(3, 1.5);
                    page.lineTo(9.5, 1.5);
                    page.lineTo(13, 5);
                    page.lineTo(13, 14.5);
                    page.lineTo(3, 14.5);
                    page.closePath();
                    g.draw(page);
                    line(g, 9, 1.5, 9, 5);
                    line(g, 9, 5, 13, 5);
                }
                case "open" -> {
                    java.awt.geom.Path2D folder = new java.awt.geom.Path2D.Float();
                    folder.moveTo(1.5, 12.5);
                    folder.lineTo(1.5, 3.5);
                    folder.lineTo(6, 3.5);
                    folder.lineTo(7.5, 5.5);
                    folder.lineTo(14.5, 5.5);
                    folder.lineTo(14.5, 12.5);
                    folder.closePath();
                    g.draw(folder);
                }
                case "save" -> {
                    g.draw(new RoundRectangle2D.Float(2.5f, 2.5f, 11, 11, 2, 2));
                    g.fillRect(5, 3, 6, 4);
                    g.drawRect(5, 9, 6, 4);
                }
                case "undo" -> {
                    g.draw(new Arc2D.Float(3, 4, 10, 9, 30, 150, Arc2D.OPEN));
                    line(g, 3, 4, 3, 8);
                    line(g, 3, 4, 7, 4);
                }
                case "redo" -> {
                    g.draw(new Arc2D.Float(3, 4, 10, 9, 0, 150, Arc2D.OPEN));
                    line(g, 13, 4, 13, 8);
                    line(g, 13, 4, 9, 4);
                }
                case "zoom-in" -> {
                    g.draw(new Ellipse2D.Float(2.5f, 2.5f, 8, 8));
                    line(g, 10, 10, 14, 14);
                    line(g, 5.5f, 6.5f, 9.5f, 6.5f);
                    line(g, 7.5f, 4.5f, 7.5f, 8.5f);
                }
                case "zoom-out" -> {
                    g.draw(new Ellipse2D.Float(2.5f, 2.5f, 8, 8));
                    line(g, 10, 10, 14, 14);
                    line(g, 5.5f, 6.5f, 9.5f, 6.5f);
                }
                case "grid" -> {
                    for (int row = 0; row < 2; row++) {
                        for (int col = 0; col < 2; col++) {
                            if ((row + col) % 2 == 0) {
                                g.fillRect(2 + col * 6, 2 + row * 6, 6, 6);
                            } else {
                                g.drawRect(2 + col * 6, 2 + row * 6, 6, 6);
                            }
                        }
                    }
                }
                case "fit" -> {
                    g.draw(new java.awt.geom.Rectangle2D.Float(4.5f, 4.5f, 7, 7));
                    line(g, 1, 1, 6, 1);
                    line(g, 1, 1, 1, 6);
                    line(g, 15, 1, 10, 1);
                    line(g, 15, 1, 15, 6);
                    line(g, 1, 15, 6, 15);
                    line(g, 1, 15, 1, 10);
                    line(g, 15, 15, 10, 15);
                    line(g, 15, 15, 15, 10);
                }
                case "move" -> {
                    line(g, 8, 2, 8, 14);
                    line(g, 2, 8, 14, 8);
                    line(g, 8, 2, 6, 4);
                    line(g, 8, 2, 10, 4);
                    line(g, 8, 14, 6, 12);
                    line(g, 8, 14, 10, 12);
                    line(g, 2, 8, 4, 6);
                    line(g, 2, 8, 4, 10);
                    line(g, 14, 8, 12, 6);
                    line(g, 14, 8, 12, 10);
                }
                case "path" -> {
                    Path2D curve = new Path2D.Float();
                    curve.moveTo(2, 12);
                    curve.curveTo(5, 2, 9, 14, 14, 4);
                    g.draw(curve);
                    g.fill(new Ellipse2D.Float(0.5f, 10.5f, 3, 3));
                    g.fill(new Ellipse2D.Float(12.5f, 2.5f, 3, 3));
                }
                case "delete" -> {
                    g.draw(new RoundRectangle2D.Float(4, 4.5f, 8, 9, 1.5f, 1.5f));
                    line(g, 2.5f, 4.5f, 13.5f, 4.5f);
                    line(g, 6, 2, 10, 2);
                    line(g, 6.5f, 7, 6.5f, 11);
                    line(g, 9.5f, 7, 9.5f, 11);
                }
                case "duplicate" -> {
                    g.draw(new RoundRectangle2D.Float(2.5f, 2.5f, 8, 8, 1.5f, 1.5f));
                    g.draw(new RoundRectangle2D.Float(5.5f, 5.5f, 8, 8, 1.5f, 1.5f));
                }
                case "up" -> {
                    line(g, 4, 10, 8, 6);
                    line(g, 8, 6, 12, 10);
                }
                case "down" -> {
                    line(g, 4, 6, 8, 10);
                    line(g, 8, 10, 12, 6);
                }
                case "add" -> {
                    line(g, 8, 3, 8, 13);
                    line(g, 3, 8, 13, 8);
                }
                case "refresh" -> {
                    g.draw(new Arc2D.Float(2.5f, 2.5f, 11, 11, 45, 270, Arc2D.OPEN));
                    Path2D arrow = new Path2D.Float();
                    arrow.moveTo(12.5, 2.5);
                    arrow.lineTo(13.5, 6.5);
                    arrow.lineTo(9.5, 6);
                    arrow.closePath();
                    g.fill(arrow);
                }
                case "validate" -> {
                    line(g, 2.5f, 8.5f, 6.5f, 12.5f);
                    line(g, 6.5f, 12.5f, 13.5f, 3.5f);
                }
                case "code" -> {
                    line(g, 5.5f, 4, 2, 8);
                    line(g, 2, 8, 5.5f, 12);
                    line(g, 10.5f, 4, 14, 8);
                    line(g, 14, 8, 10.5f, 12);
                }
                case "image" -> {
                    g.draw(new RoundRectangle2D.Float(1.5f, 3.5f, 13, 9, 2, 2));
                    Path2D hills = new Path2D.Float();
                    hills.moveTo(3, 11.5);
                    hills.lineTo(6, 7.5);
                    hills.lineTo(8.5, 10);
                    hills.lineTo(10.5, 8);
                    hills.lineTo(13, 11.5);
                    g.draw(hills);
                    g.fill(new Ellipse2D.Float(10.5f, 5, 2, 2));
                }
                case "font" -> {
                    line(g, 3.5f, 13, 8, 3);
                    line(g, 8, 3, 12.5f, 13);
                    line(g, 5.5f, 9.5f, 10.5f, 9.5f);
                }
                case "window" -> {
                    g.draw(new RoundRectangle2D.Float(1.5f, 2.5f, 13, 11, 2, 2));
                    line(g, 1.5f, 5.5f, 14.5f, 5.5f);
                }
                case "layout" -> {
                    g.draw(new RoundRectangle2D.Float(1.5f, 2.5f, 13, 11, 2, 2));
                    line(g, 6.5f, 2.5f, 6.5f, 13.5f);
                    line(g, 6.5f, 8, 14.5f, 8);
                }
                case "layers" -> {
                    line(g, 8, 2, 14, 5.5f);
                    line(g, 14, 5.5f, 8, 9);
                    line(g, 8, 9, 2, 5.5f);
                    line(g, 2, 5.5f, 8, 2);
                    line(g, 3, 9, 8, 12);
                    line(g, 8, 12, 13, 9);
                }
                case "text" -> {
                    line(g, 3.5f, 4, 12.5f, 4);
                    line(g, 8, 4, 8, 13);
                }
                case "button" -> g.draw(new RoundRectangle2D.Float(1.5f, 4.5f, 13, 7, 4, 4));
                case "slider" -> {
                    line(g, 2, 8, 14, 8);
                    g.fill(new Ellipse2D.Float(6, 5.5f, 5, 5));
                }
                case "checkbox" -> {
                    g.draw(new RoundRectangle2D.Float(2.5f, 2.5f, 11, 11, 2.5f, 2.5f));
                    line(g, 5, 8, 7.5f, 10.5f);
                    line(g, 7.5f, 10.5f, 11.5f, 5.5f);
                }
                case "anchor" -> {
                    g.draw(new Ellipse2D.Float(5.5f, 1.5f, 5, 5));
                    line(g, 8, 6.5f, 8, 13);
                    g.draw(new Arc2D.Float(3, 9, 10, 6, 0, 180, Arc2D.OPEN));
                }
                case "panel" -> g.draw(new java.awt.geom.RoundRectangle2D.Float(1.5f, 2.5f, 13, 11, 2, 2));
                case "group" -> {
                    g.drawRect(2, 2, 5, 5);
                    g.drawRect(9, 9, 5, 5);
                }
                case "video" -> {
                    g.draw(new RoundRectangle2D.Float(1.5f, 3.5f, 13, 9, 2, 2));
                    Path2D play = new Path2D.Float();
                    play.moveTo(6.5f, 5.5f);
                    play.lineTo(10.5f, 8);
                    play.lineTo(6.5f, 10.5f);
                    play.closePath();
                    g.fill(play);
                }
                case "playlist" -> {
                    line(g, 2, 4, 10, 4);
                    line(g, 2, 8, 10, 8);
                    line(g, 2, 12, 8, 12);
                    g.fill(new Ellipse2D.Float(10.5f, 2.5f, 3, 3));
                }
                case "light" -> {
                    g.draw(new Ellipse2D.Float(5, 5, 6, 6));
                    for (int i = 0; i < 8; i++) {
                        double angle = Math.PI * i / 4;
                        line(g, (int) (8 + Math.cos(angle) * 5), (int) (8 + Math.sin(angle) * 5),
                                (int) (8 + Math.cos(angle) * 7), (int) (8 + Math.sin(angle) * 7));
                    }
                }
                case "dark" -> {
                    g.draw(new Arc2D.Float(3, 2, 11, 12, 60, 280, Arc2D.OPEN));
                    g.fill(new Ellipse2D.Float(7, 3, 6, 6));
                }
                case "chat" -> {
                    g.draw(new RoundRectangle2D.Float(1.5f, 2.5f, 13, 9, 4, 4));
                    line(g, 5, 11.5f, 4, 14.5f);
                    line(g, 4, 14.5f, 8, 11.5f);
                }
                case "mcp" -> {
                    g.fill(new Ellipse2D.Float(5.5f, 1, 5, 5));
                    g.fill(new Ellipse2D.Float(1, 9.5f, 5, 5));
                    g.fill(new Ellipse2D.Float(10, 9.5f, 5, 5));
                    line(g, 8, 6, 4, 9.5f);
                    line(g, 8, 6, 12, 9.5f);
                }
                case "help" -> {
                    g.draw(new Ellipse2D.Float(1.5f, 1.5f, 13, 13));
                    g.draw(new Arc2D.Float(5.5f, 4, 5, 5, 200, 220, Arc2D.OPEN));
                    g.fill(new Ellipse2D.Float(7, 10.5f, 2, 2));
                }
                case "info" -> {
                    g.draw(new Ellipse2D.Float(1.5f, 1.5f, 13, 13));
                    line(g, 8, 7, 8, 11.5f);
                    g.fill(new Ellipse2D.Float(7, 4, 2, 2));
                }
                default -> {
                    g.draw(new RoundRectangle2D.Float(2.5f, 2.5f, 11, 11, 3, 3));
                }
            }
        }

        @Override
        public int getIconWidth() {
            return size;
        }

        @Override
        public int getIconHeight() {
            return size;
        }
    }
}
