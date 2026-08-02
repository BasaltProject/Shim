/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package javax.swing.text;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Rectangle;
import java.awt.Shape;
import java.util.ArrayList;
import java.util.List;

public class DefaultHighlighter extends LayeredHighlighter {
  public static final LayeredHighlighter.LayerPainter DefaultPainter = new DefaultHighlightPainter(null);

  private final List<HighlightInfo> highlights = new ArrayList<>();
  private JTextComponent component;
  private boolean drawsLayeredHighlights = true;

  public DefaultHighlighter() { }

  public void paint(Graphics g) {
    if (component == null) {
      return;
    }
    Rectangle a = component.getBounds();
    java.awt.Insets insets = component.getInsets();
    a.x = insets.left;
    a.y = insets.top;
    a.width -= insets.left + insets.right;
    a.height -= insets.top + insets.bottom;
    for (HighlightInfo info : new java.util.ArrayList<HighlightInfo>(highlights)) {
      if (drawsLayeredHighlights && info.painter instanceof LayeredHighlighter.LayerPainter) {
        continue;
      }
      info.painter.paint(g, info.getStartOffset(), info.getEndOffset(), a, component);
    }
  }

  public void install(JTextComponent c) {
    component = c;
    removeAllHighlights();
  }

  public void deinstall(JTextComponent c) { component = null; }

  public Object addHighlight(int p0, int p1, Highlighter.HighlightPainter p) throws BadLocationException {
    if (p0 < 0) {
      throw new BadLocationException("Invalid start offset", p0);
    }
    if (p1 < p0) {
      throw new BadLocationException("Invalid end offset", p1);
    }
    Document doc = component.getDocument();
    HighlightInfo i = new HighlightInfo();
    i.painter = p;
    i.p0 = doc.createPosition(p0);
    i.p1 = doc.createPosition(p1);
    highlights.add(i);
    return i;
  }

  public void removeHighlight(Object tag) { highlights.remove(tag); }

  public void removeAllHighlights() { highlights.clear(); }

  public Highlighter.Highlight[] getHighlights() { return highlights.toArray(new Highlighter.Highlight[0]); }

  public void changeHighlight(Object tag, int p0, int p1) throws BadLocationException {
    if (p0 < 0) {
      throw new BadLocationException("Invalid beginning of the range", p0);
    }
    if (p1 < p0) {
      throw new BadLocationException("Invalid end of the range", p1);
    }
    Document doc = component.getDocument();
    if (tag instanceof HighlightInfo) {
      HighlightInfo info = (HighlightInfo) tag;
      info.p0 = doc.createPosition(p0);
      info.p1 = doc.createPosition(p1);
    }
  }

  public void paintLayeredHighlights(Graphics g, int p0, int p1, Shape viewBounds, JTextComponent editor, View view) {
    java.util.List<HighlightInfo> snapshot = new java.util.ArrayList<HighlightInfo>(highlights);
    java.util.Collections.reverse(snapshot);
    for (HighlightInfo info : snapshot) {
      if (!(drawsLayeredHighlights && info.painter instanceof LayeredHighlighter.LayerPainter)) {
        continue;
      }
      int s = info.getStartOffset();
      int e = info.getEndOffset();
      if (e > p0 && s < p1) {
        ((LayeredHighlighter.LayerPainter) info.painter).paintLayer(g, Math.max(s, p0), Math.min(e, p1), viewBounds,
            editor, view);
      }
    }
  }

  public void setDrawsLayeredHighlights(boolean newValue) { drawsLayeredHighlights = newValue; }

  public boolean getDrawsLayeredHighlights() { return drawsLayeredHighlights; }

  private static final class HighlightInfo implements Highlighter.Highlight {
    Position p0;
    Position p1;
    Highlighter.HighlightPainter painter;

    public int getStartOffset() { return p0.getOffset(); }

    public int getEndOffset() { return p1.getOffset(); }

    public Highlighter.HighlightPainter getPainter() { return painter; }
  }

  public static class DefaultHighlightPainter extends LayeredHighlighter.LayerPainter {
    private final Color color;

    public DefaultHighlightPainter(Color c) { color = c; }

    public Color getColor() { return color; }

    public void paint(Graphics g, int offs0, int offs1, Shape bounds, JTextComponent c) {
      Rectangle alloc = bounds.getBounds();
      try {
        javax.swing.plaf.TextUI mapper = c.getUI();
        Rectangle p0 = mapper.modelToView(c, offs0);
        Rectangle p1 = mapper.modelToView(c, offs1);
        if (p0 == null || p1 == null) {
          return;
        }
        g.setColor(color == null ? c.getSelectionColor() : color);
        if (p0.y == p1.y) {
          Rectangle r = p0.union(p1);
          g.fillRect(r.x, r.y, r.width, r.height);
        } else {
          int p0ToMarginWidth = alloc.x + alloc.width - p0.x;
          g.fillRect(p0.x, p0.y, p0ToMarginWidth, p0.height);
          if ((p0.y + p0.height) != p1.y) {
            g.fillRect(alloc.x, p0.y + p0.height, alloc.width, p1.y - (p0.y + p0.height));
          }
          g.fillRect(alloc.x, p1.y, (p1.x - alloc.x), p1.height);
        }
      } catch (BadLocationException e) {
        // a stale range paints nothing
      }
    }

    public Shape paintLayer(Graphics g, int offs0, int offs1, Shape bounds, JTextComponent c, View view) {
      g.setColor(color == null ? c.getSelectionColor() : color);
      Rectangle r;
      if (offs0 == view.getStartOffset() && offs1 == view.getEndOffset()) {
        r = bounds instanceof Rectangle ? (Rectangle) bounds : bounds.getBounds();
      } else {
        try {
          Shape shape = view.modelToView(offs0, Position.Bias.Forward, offs1, Position.Bias.Backward, bounds);
          r = shape instanceof Rectangle ? (Rectangle) shape : shape.getBounds();
        } catch (BadLocationException e) {
          r = null;
        }
      }
      if (r != null) {
        r.width = Math.max(r.width, 1);
        g.fillRect(r.x, r.y, r.width, r.height);
      }
      return r;
    }
  }
}
