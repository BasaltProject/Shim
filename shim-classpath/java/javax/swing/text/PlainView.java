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
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.Shape;
import javax.swing.event.DocumentEvent;

public class PlainView extends View implements TabExpander {
  protected FontMetrics metrics;
  Element longLine;
  Font font;
  Segment lineBuffer;
  int tabSize;
  int tabBase;
  int sel0;
  int sel1;
  Color unselected;
  Color selected;
  int firstLineOffset;

  public PlainView(Element elem) {
    super(elem);
  }

  protected int getTabSize() {
    Integer i = (Integer) getDocument().getProperty(PlainDocument.tabSizeAttribute);
    return i != null ? i.intValue() : 8;
  }

  protected void drawLine(int lineIndex, Graphics g, int x, int y) {
    Element line = getElement().getElement(lineIndex);
    try {
      if (line.isLeaf()) {
        drawElement(lineIndex, line, g, x, y);
      } else {
        int count = line.getElementCount();
        for (int i = 0; i < count; i++) {
          x = drawElement(lineIndex, line.getElement(i), g, x, y);
        }
      }
    } catch (BadLocationException e) {
      throw new StateInvariantError("Can't render line: " + lineIndex);
    }
  }

  protected void drawLine(int lineIndex, Graphics2D g, float x, float y) {
    drawLine(lineIndex, (Graphics) g, (int) x, (int) y);
  }

  private int drawElement(int lineIndex, Element elem, Graphics g, int x, int y) throws BadLocationException {
    int p0 = elem.getStartOffset();
    int p1 = elem.getEndOffset();
    p1 = Math.min(getDocument().getLength(), p1);
    if (lineIndex == 0) {
      x += firstLineOffset;
    }
    if (sel0 == sel1 || selected == unselected) {
      x = callDrawUnselectedText(g, x, y, p0, p1);
    } else if ((p0 >= sel0 && p0 <= sel1) && (p1 >= sel0 && p1 <= sel1)) {
      x = callDrawSelectedText(g, x, y, p0, p1);
    } else if (sel0 >= p0 && sel0 <= p1) {
      if (sel1 >= p0 && sel1 <= p1) {
        x = callDrawUnselectedText(g, x, y, p0, sel0);
        x = callDrawSelectedText(g, x, y, sel0, sel1);
        x = callDrawUnselectedText(g, x, y, sel1, p1);
      } else {
        x = callDrawUnselectedText(g, x, y, p0, sel0);
        x = callDrawSelectedText(g, x, y, sel0, p1);
      }
    } else if (sel1 >= p0 && sel1 <= p1) {
      x = callDrawSelectedText(g, x, y, p0, sel1);
      x = callDrawUnselectedText(g, x, y, sel1, p1);
    } else {
      x = callDrawUnselectedText(g, x, y, p0, p1);
    }
    return x;
  }

  private int callDrawUnselectedText(Graphics g, int x, int y, int p0, int p1) throws BadLocationException {
    return drawUnselectedText(g, x, y, p0, p1);
  }

  private int callDrawSelectedText(Graphics g, int x, int y, int p0, int p1) throws BadLocationException {
    return drawSelectedText(g, x, y, p0, p1);
  }

  protected int drawUnselectedText(Graphics g, int x, int y, int p0, int p1) throws BadLocationException {
    g.setColor(unselected);
    Document doc = getDocument();
    Segment s = new Segment();
    doc.getText(p0, p1 - p0, s);
    return Utilities.drawTabbedText(s, x, y, g, this, p0);
  }

  protected float drawUnselectedText(Graphics2D g, float x, float y, int p0, int p1) throws BadLocationException {
    g.setColor(unselected);
    Document doc = getDocument();
    Segment s = new Segment();
    doc.getText(p0, p1 - p0, s);
    return Utilities.drawTabbedText(s, x, y, g, this, p0);
  }

  protected int drawSelectedText(Graphics g, int x, int y, int p0, int p1) throws BadLocationException {
    g.setColor(selected);
    Document doc = getDocument();
    Segment s = new Segment();
    doc.getText(p0, p1 - p0, s);
    return Utilities.drawTabbedText(s, x, y, g, this, p0);
  }

  protected float drawSelectedText(Graphics2D g, float x, float y, int p0, int p1) throws BadLocationException {
    g.setColor(selected);
    Document doc = getDocument();
    Segment s = new Segment();
    doc.getText(p0, p1 - p0, s);
    return Utilities.drawTabbedText(s, x, y, g, this, p0);
  }

  protected final Segment getLineBuffer() {
    if (lineBuffer == null) {
      lineBuffer = new Segment();
    }
    return lineBuffer;
  }

  protected void updateMetrics() {
    Component host = getContainer();
    Font f = host.getFont();
    if (font != f) {
      font = f;
      metrics = host.getFontMetrics(f);
      calculateLongestLine();
      tabSize = getTabSize() * metrics.charWidth('m');
    }
  }

  public float nextTabStop(float x, int tabOffset) {
    if (tabSize == 0) {
      return x;
    }
    int ntabs = (((int) x) - tabBase) / tabSize;
    return tabBase + ((ntabs + 1) * tabSize);
  }

  public void paint(Graphics g, Shape a) {
    Shape originalA = a;
    Rectangle alloc = (Rectangle) a;
    tabBase = alloc.x;
    JTextComponent host = (JTextComponent) getContainer();
    Highlighter h = host.getHighlighter();
    g.setFont(host.getFont());
    sel0 = host.getSelectionStart();
    sel1 = host.getSelectionEnd();
    unselected = (host.isEnabled()) ? host.getForeground() : host.getDisabledTextColor();
    Caret c = host.getCaret();
    selected = c.isSelectionVisible() && h != null ? host.getSelectedTextColor() : unselected;
    updateMetrics();
    Rectangle clip = g.getClipBounds();
    int fontHeight = metrics.getHeight();
    int heightBelow = (alloc.y + alloc.height) - (clip.y + clip.height);
    int heightAbove = clip.y - alloc.y;
    int linesBelow;
    int linesAbove;
    int linesTotal;
    if (fontHeight > 0) {
      linesBelow = Math.max(0, heightBelow / fontHeight);
      linesAbove = Math.max(0, heightAbove / fontHeight);
      linesTotal = alloc.height / fontHeight;
      if (alloc.height % fontHeight != 0) {
        linesTotal++;
      }
    } else {
      linesBelow = linesAbove = linesTotal = 0;
    }
    Rectangle lineArea = lineToRect(a, linesAbove);
    int y = lineArea.y + metrics.getAscent();
    int x = lineArea.x;
    Element map = getElement();
    int lineCount = map.getElementCount();
    int endLine = Math.min(lineCount, linesTotal - linesBelow);
    lineCount--;
    LayeredHighlighter dh = (h instanceof LayeredHighlighter) ? (LayeredHighlighter) h : null;
    for (int line = linesAbove; line < endLine; line++) {
      if (dh != null) {
        Element lineElement = map.getElement(line);
        if (line == lineCount) {
          dh.paintLayeredHighlights(g, lineElement.getStartOffset(), lineElement.getEndOffset(), originalA, host,
              this);
        } else {
          dh.paintLayeredHighlights(g, lineElement.getStartOffset(), lineElement.getEndOffset() - 1, originalA,
              host, this);
        }
      }
      drawLine(line, g, x, y);
      y += fontHeight;
      if (line == 0) {
        x -= firstLineOffset;
      }
    }
  }

  public float getPreferredSpan(int axis) {
    updateMetrics();
    switch (axis) {
      case View.X_AXIS:
        float span = getLineWidth(longLine);
        return span;
      case View.Y_AXIS:
        return getElement().getElementCount() * metrics.getHeight();
      default:
        throw new IllegalArgumentException("Invalid axis: " + axis);
    }
  }

  public Shape modelToView(int pos, Shape a, Position.Bias b) throws BadLocationException {
    Document doc = getDocument();
    Element map = getElement();
    int lineIndex = map.getElementIndex(pos);
    Rectangle lineArea = lineToRect(a, lineIndex);
    tabBase = lineArea.x;
    Segment line = new Segment();
    int p0 = map.getElement(lineIndex).getStartOffset();
    doc.getText(p0, pos - p0, line);
    int xOffs = Utilities.getTabbedTextWidth(line, metrics, tabBase, this, p0);
    lineArea.x += xOffs;
    lineArea.width = 1;
    lineArea.height = metrics.getHeight();
    return lineArea;
  }

  public int viewToModel(float fx, float fy, Shape a, Position.Bias[] bias) {
    bias[0] = Position.Bias.Forward;
    Rectangle alloc = a.getBounds();
    Document doc = getDocument();
    int x = (int) fx;
    int y = (int) fy;
    if (y < alloc.y) {
      return getStartOffset();
    } else if (y > alloc.y + alloc.height) {
      return getEndOffset() - 1;
    } else {
      Element map = doc.getDefaultRootElement();
      int fontHeight = metrics.getHeight();
      int lineIndex = (fontHeight > 0 ? Math.abs((y - alloc.y) / fontHeight) : map.getElementCount() - 1);
      if (lineIndex >= map.getElementCount()) {
        return getEndOffset() - 1;
      }
      Element line = map.getElement(lineIndex);
      if (lineIndex == 0) {
        alloc.x += firstLineOffset;
        alloc.width -= firstLineOffset;
      }
      if (x < alloc.x) {
        return line.getStartOffset();
      } else if (x > alloc.x + alloc.width) {
        return line.getEndOffset() - 1;
      } else {
        try {
          int p0 = line.getStartOffset();
          int p1 = line.getEndOffset() - 1;
          Segment s = new Segment();
          doc.getText(p0, p1 - p0, s);
          tabBase = alloc.x;
          return p0 + Utilities.getTabbedTextOffset(s, metrics, tabBase, x, this, p0);
        } catch (BadLocationException e) {
          return -1;
        }
      }
    }
  }

  public void insertUpdate(DocumentEvent changes, Shape a, ViewFactory f) {
    updateDamage(changes, a, f);
  }

  public void removeUpdate(DocumentEvent changes, Shape a, ViewFactory f) {
    updateDamage(changes, a, f);
  }

  public void changedUpdate(DocumentEvent changes, Shape a, ViewFactory f) {
    updateDamage(changes, a, f);
  }

  public void setSize(float width, float height) {
    super.setSize(width, height);
    updateMetrics();
  }

  protected Rectangle lineToRect(Shape a, int line) {
    Rectangle r = null;
    updateMetrics();
    if (metrics != null) {
      Rectangle alloc = a.getBounds();
      if (line == 0) {
        alloc.x += firstLineOffset;
        alloc.width -= firstLineOffset;
      }
      r = new Rectangle(alloc.x, alloc.y + (line * metrics.getHeight()), alloc.width, metrics.getHeight());
    }
    return r;
  }

  protected void updateDamage(DocumentEvent changes, Shape a, ViewFactory f) {
    Component host = getContainer();
    updateMetrics();
    Element elem = getElement();
    DocumentEvent.ElementChange ec = changes.getChange(elem);
    Element[] added = (ec != null) ? ec.getChildrenAdded() : null;
    Element[] removed = (ec != null) ? ec.getChildrenRemoved() : null;
    if (((added != null) && (added.length > 0)) || ((removed != null) && (removed.length > 0))) {
      if (added != null) {
        int currWide = getLineWidth(longLine);
        for (int i = 0; i < added.length; i++) {
          int w = getLineWidth(added[i]);
          if (w > currWide) {
            currWide = w;
            longLine = added[i];
          }
        }
      }
      if (removed != null) {
        for (int i = 0; i < removed.length; i++) {
          if (removed[i] == longLine) {
            calculateLongestLine();
            break;
          }
        }
      }
      preferenceChanged(null, true, true);
      host.repaint();
    } else {
      Element map = getElement();
      int line = map.getElementIndex(changes.getOffset());
      damageLineRange(line, line, a, host);
      if (changes.getType() == DocumentEvent.EventType.INSERT) {
        int w = getLineWidth(longLine);
        Element e = map.getElement(line);
        if (e == longLine) {
          preferenceChanged(null, true, false);
        } else if (getLineWidth(e) > w) {
          longLine = e;
          preferenceChanged(null, true, false);
        }
      } else if (changes.getType() == DocumentEvent.EventType.REMOVE) {
        if (map.getElement(line) == longLine) {
          calculateLongestLine();
          preferenceChanged(null, true, false);
        }
      }
    }
  }

  protected void damageLineRange(int line0, int line1, Shape a, Component host) {
    if (a != null) {
      Rectangle area0 = lineToRect(a, line0);
      Rectangle area1 = lineToRect(a, line1);
      if ((area0 != null) && (area1 != null)) {
        Rectangle damage = area0.union(area1);
        host.repaint(damage.x, damage.y, damage.width, damage.height);
      } else {
        host.repaint();
      }
    }
  }

  private void calculateLongestLine() {
    Component c = getContainer();
    metrics = c.getFontMetrics(c.getFont());
    Document doc = getDocument();
    Element lines = getElement();
    int n = lines.getElementCount();
    int maxWidth = -1;
    longLine = null;
    for (int i = 0; i < n; i++) {
      Element line = lines.getElement(i);
      int w = getLineWidth(line);
      if (w > maxWidth) {
        maxWidth = w;
        longLine = line;
      }
    }
  }

  private int getLineWidth(Element line) {
    if (line == null) {
      return 0;
    }
    int p0 = line.getStartOffset();
    int p1 = line.getEndOffset();
    int w;
    Segment s = new Segment();
    try {
      line.getDocument().getText(p0, p1 - p0, s);
      w = Utilities.getTabbedTextWidth(s, metrics, tabBase, this, p0);
    } catch (BadLocationException ble) {
      w = 0;
    }
    return w;
  }
}
