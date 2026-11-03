/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */
package javax.swing.text;

import java.awt.Color;
import java.awt.Component;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Rectangle;
import java.awt.Shape;
import javax.swing.event.DocumentEvent;

public class WrappedPlainView extends BoxView implements TabExpander {
  FontMetrics metrics;
  Segment lineBuffer;
  boolean widthChanging;
  int tabBase;
  int tabSize;
  boolean wordWrap;
  int sel0;
  int sel1;
  Color unselected;
  Color selected;

  public WrappedPlainView(Element elem) {
    this(elem, false);
  }

  public WrappedPlainView(Element elem, boolean wordWrap) {
    super(elem, Y_AXIS);
    this.wordWrap = wordWrap;
  }

  protected int getTabSize() {
    Integer i = (Integer) getDocument().getProperty(PlainDocument.tabSizeAttribute);
    return i != null ? i.intValue() : 8;
  }

  protected void drawLine(int p0, int p1, Graphics g, int x, int y) {
    Element lineMap = getElement();
    Element line = lineMap.getElement(lineMap.getElementIndex(p0));
    try {
      if (line.isLeaf()) {
        drawText(line, p0, p1, g, x, y);
      } else {
        int idx = line.getElementIndex(p0);
        int lastIdx = line.getElementIndex(p1);
        for (; idx <= lastIdx; idx++) {
          Element elem = line.getElement(idx);
          int start = Math.max(elem.getStartOffset(), p0);
          int end = Math.min(elem.getEndOffset(), p1);
          x = drawText(elem, start, end, g, x, y);
        }
      }
    } catch (BadLocationException e) {
      throw new StateInvariantError("Can't render: " + p0 + "," + p1);
    }
  }

  private int drawText(Element elem, int p0, int p1, Graphics g, int x, int y) throws BadLocationException {
    p1 = Math.min(getDocument().getLength(), p1);
    if (sel0 == sel1 || selected == unselected) {
      return drawUnselectedText(g, x, y, p0, p1);
    }
    if ((p0 >= sel0 && p0 <= sel1) && (p1 >= sel0 && p1 <= sel1)) {
      return drawSelectedText(g, x, y, p0, p1);
    }
    if (sel0 >= p0 && sel0 <= p1) {
      if (sel1 >= p0 && sel1 <= p1) {
        x = drawUnselectedText(g, x, y, p0, sel0);
        x = drawSelectedText(g, x, y, sel0, sel1);
        x = drawUnselectedText(g, x, y, sel1, p1);
      } else {
        x = drawUnselectedText(g, x, y, p0, sel0);
        x = drawSelectedText(g, x, y, sel0, p1);
      }
    } else if (sel1 >= p0 && sel1 <= p1) {
      x = drawSelectedText(g, x, y, p0, sel1);
      x = drawUnselectedText(g, x, y, sel1, p1);
    } else {
      x = drawUnselectedText(g, x, y, p0, p1);
    }
    return x;
  }

  protected int drawUnselectedText(Graphics g, int x, int y, int p0, int p1) throws BadLocationException {
    g.setColor(unselected);
    Document doc = getDocument();
    Segment segment = new Segment();
    doc.getText(p0, p1 - p0, segment);
    return Utilities.drawTabbedText(segment, x, y, g, this, p0);
  }

  protected int drawSelectedText(Graphics g, int x, int y, int p0, int p1) throws BadLocationException {
    g.setColor(selected);
    Document doc = getDocument();
    Segment segment = new Segment();
    doc.getText(p0, p1 - p0, segment);
    return Utilities.drawTabbedText(segment, x, y, g, this, p0);
  }

  protected final Segment getLineBuffer() {
    if (lineBuffer == null) {
      lineBuffer = new Segment();
    }
    return lineBuffer;
  }

  protected int calculateBreakPosition(int p0, int p1) {
    int p;
    Segment segment = new Segment();
    loadText(segment, p0, p1);
    int currentWidth = getWidth();
    if (wordWrap) {
      p = p0 + Utilities.getBreakLocation(segment, metrics, tabBase, tabBase + currentWidth, this, p0);
    } else {
      p = p0 + Utilities.getTabbedTextOffset(segment, metrics, tabBase, tabBase + currentWidth, this, p0, false);
      if (p == p0 && p0 < p1) {
        p = p0 + 1;
      }
    }
    return p;
  }

  protected void loadChildren(ViewFactory f) {
    Element e = getElement();
    int n = e.getElementCount();
    if (n > 0) {
      View[] added = new View[n];
      for (int i = 0; i < n; i++) {
        added[i] = new WrappedLine(e.getElement(i));
      }
      replace(0, 0, added);
    }
  }

  void updateChildren(DocumentEvent e, Shape a) {
    Element elem = getElement();
    DocumentEvent.ElementChange ec = e.getChange(elem);
    if (ec != null) {
      Element[] removedElems = ec.getChildrenRemoved();
      Element[] addedElems = ec.getChildrenAdded();
      View[] added = new View[addedElems.length];
      for (int i = 0; i < addedElems.length; i++) {
        added[i] = new WrappedLine(addedElems[i]);
      }
      replace(ec.getIndex(), removedElems.length, added);
      if (a != null) {
        preferenceChanged(null, true, true);
        getContainer().repaint();
      }
    }
    updateMetrics();
  }

  final void loadText(Segment segment, int p0, int p1) {
    try {
      Document doc = getDocument();
      doc.getText(p0, p1 - p0, segment);
    } catch (BadLocationException bl) {
      throw new StateInvariantError("Can't get line text");
    }
  }

  final void updateMetrics() {
    Component host = getContainer();
    Font f = host.getFont();
    metrics = host.getFontMetrics(f);
    tabSize = getTabSize() * metrics.charWidth('m');
  }

  public float nextTabStop(float x, int tabOffset) {
    if (tabSize == 0) {
      return x;
    }
    int ntabs = (((int) x) - tabBase) / tabSize;
    return tabBase + ((ntabs + 1) * tabSize);
  }

  public void paint(Graphics g, Shape a) {
    Rectangle alloc = (Rectangle) a;
    tabBase = alloc.x;
    JTextComponent host = (JTextComponent) getContainer();
    sel0 = host.getSelectionStart();
    sel1 = host.getSelectionEnd();
    unselected = (host.isEnabled()) ? host.getForeground() : host.getDisabledTextColor();
    Caret c = host.getCaret();
    selected = c.isSelectionVisible() && host.getHighlighter() != null ? host.getSelectedTextColor() : unselected;
    g.setFont(host.getFont());
    super.paint(g, a);
  }

  public void setSize(float width, float height) {
    updateMetrics();
    if ((int) width != getWidth()) {
      preferenceChanged(null, true, true);
      widthChanging = true;
    }
    super.setSize(width, height);
    widthChanging = false;
  }

  public float getPreferredSpan(int axis) {
    updateMetrics();
    return super.getPreferredSpan(axis);
  }

  public float getMinimumSpan(int axis) {
    updateMetrics();
    return super.getMinimumSpan(axis);
  }

  public float getMaximumSpan(int axis) {
    updateMetrics();
    return super.getMaximumSpan(axis);
  }

  public void insertUpdate(DocumentEvent e, Shape a, ViewFactory f) {
    updateChildren(e, a);
    Rectangle alloc = ((a != null) && isAllocationValid()) ? getInsideAllocation(a) : null;
    int pos = e.getOffset();
    View v = getViewAtPosition(pos, alloc);
    if (v != null) {
      v.insertUpdate(e, alloc, f);
    }
  }

  public void removeUpdate(DocumentEvent e, Shape a, ViewFactory f) {
    updateChildren(e, a);
    Rectangle alloc = ((a != null) && isAllocationValid()) ? getInsideAllocation(a) : null;
    int pos = e.getOffset();
    View v = getViewAtPosition(pos, alloc);
    if (v != null) {
      v.removeUpdate(e, alloc, f);
    }
  }

  public void changedUpdate(DocumentEvent e, Shape a, ViewFactory f) {
    updateChildren(e, a);
  }

  class WrappedLine extends View {
    int lineCount;

    WrappedLine(Element elem) {
      super(elem);
      lineCount = -1;
    }

    public float getPreferredSpan(int axis) {
      switch (axis) {
        case View.X_AXIS:
          float width = getWidth();
          if (width == Integer.MAX_VALUE) {
            return 100f;
          }
          return width;
        case View.Y_AXIS:
          if (lineCount < 0 || widthChanging) {
            breakLines(getStartOffset());
          }
          return lineCount * metrics.getHeight();
        default:
          throw new IllegalArgumentException("Invalid axis: " + axis);
      }
    }

    public void paint(Graphics g, Shape a) {
      Rectangle alloc = (Rectangle) a;
      int y = alloc.y + metrics.getAscent();
      int x = alloc.x;
      JTextComponent host = (JTextComponent) getContainer();
      Highlighter h = host.getHighlighter();
      LayeredHighlighter dh = (h instanceof LayeredHighlighter) ? (LayeredHighlighter) h : null;
      int start = getStartOffset();
      int end = getEndOffset();
      int p0 = start;
      int p1;
      while (p0 < end) {
        p1 = calculateBreakPosition(p0, end);
        if (dh != null) {
          if (p1 == end) {
            dh.paintLayeredHighlights(g, p0, p1, a, host, this);
          } else {
            dh.paintLayeredHighlights(g, p0, p1 - 1, a, host, this);
          }
        }
        drawLine(p0, p1, g, x, y);
        p0 = (p1 == p0) ? end : p1;
        y += metrics.getHeight();
      }
    }

    public Shape modelToView(int pos, Shape a, Position.Bias b) throws BadLocationException {
      Rectangle alloc = a.getBounds();
      alloc.height = metrics.getHeight();
      alloc.width = 1;
      int p0 = getStartOffset();
      if (pos < p0 || pos > getEndOffset()) {
        throw new BadLocationException("Position out of range", pos);
      }
      int testP = (b == Position.Bias.Forward) ? pos : Math.max(p0, pos - 1);
      int line = 0;
      Segment seg = new Segment();
      while (p0 < getEndOffset()) {
        int p1 = calculateBreakPosition(p0, getEndOffset());
        if ((testP >= p0 && testP < p1) || (p1 >= getEndOffset() && pos >= p0)) {
          seg = new Segment();
          loadText(seg, p0, pos);
          alloc.x += Utilities.getTabbedTextWidth(seg, metrics, alloc.x, WrappedPlainView.this, p0);
          alloc.y += line * metrics.getHeight();
          return alloc;
        }
        if (p1 == p0) {
          break;
        }
        line += 1;
        p0 = p1;
      }
      throw new BadLocationException("Position out of range", pos);
    }

    public int viewToModel(float fx, float fy, Shape a, Position.Bias[] bias) {
      bias[0] = Position.Bias.Forward;
      Rectangle alloc = (Rectangle) a;
      int x = (int) fx;
      int y = (int) fy;
      if (y < alloc.y) {
        return getStartOffset();
      } else if (y > alloc.y + alloc.height) {
        return getEndOffset() - 1;
      } else {
        alloc.height = metrics.getHeight();
        int p0 = getStartOffset();
        int p1 = getEndOffset();
        while (p0 < p1) {
          int p = calculateBreakPosition(p0, p1);
          if (y >= alloc.y && y < alloc.y + alloc.height) {
            if (x < alloc.x) {
              return p0;
            } else if (x > alloc.x + alloc.width) {
              return p - 1;
            } else {
              Segment segment = new Segment();
              loadText(segment, p0, p);
              int n = Utilities.getTabbedTextOffset(segment, metrics, alloc.x, x, WrappedPlainView.this, p0, false);
              return Math.min(p0 + n, p - 1);
            }
          }
          alloc.y += alloc.height;
          p0 = (p == p0) ? p1 : p;
        }
        return getEndOffset() - 1;
      }
    }

    void update(DocumentEvent ev, Shape a) {
      int oldCount = lineCount;
      breakLines(ev.getOffset());
      if (oldCount != lineCount) {
        WrappedPlainView.this.preferenceChanged(this, false, true);
        getContainer().repaint();
      } else if (a != null) {
        Component c = getContainer();
        Rectangle alloc = (Rectangle) a;
        c.repaint(alloc.x, alloc.y, alloc.width, alloc.height);
      }
    }

    final int breakLines(int p0) {
      int p0Orig = p0;
      int pend = getEndOffset();
      int nlines = 0;
      p0 = getStartOffset();
      while (p0 < pend) {
        int p1 = calculateBreakPosition(p0, pend);
        nlines += 1;
        p0 = (p1 == p0) ? pend : p1;
      }
      lineCount = nlines;
      return pend;
    }

    public void insertUpdate(DocumentEvent e, Shape a, ViewFactory f) {
      update(e, a);
    }

    public void removeUpdate(DocumentEvent e, Shape a, ViewFactory f) {
      update(e, a);
    }
  }
}
