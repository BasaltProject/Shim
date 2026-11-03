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
import java.awt.Container;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.Shape;
import java.text.BreakIterator;
import java.util.Locale;
import javax.swing.UIManager;
import javax.swing.event.DocumentEvent;

public class GlyphView extends View implements TabableView, Cloneable {
  private GlyphPainter painter;
  private TabExpander expander;
  private int offset;
  private int length;
  private boolean impliedCR;
  private boolean skipWidth;
  private int x;
  private float minimumSpan = -1;
  private BreakIterator breaker;
  private int[] breakSpots;

  public GlyphView(Element elem) {
    super(elem);
    offset = 0;
    length = 0;
    Element parent = elem.getParentElement();
    AttributeSet a = elem.getAttributes();
    skipWidth = "br".equals(elem.getName());
    impliedCR = (a != null && a.getAttribute(IMPLIED_CR) != null && parent != null && parent.getElementCount() > 1);
  }

  static final Object IMPLIED_CR = "CR";

  protected final Object clone() {
    try {
      return super.clone();
    } catch (CloneNotSupportedException e) {
      throw new InternalError();
    }
  }

  public GlyphPainter getGlyphPainter() { return painter; }

  public void setGlyphPainter(GlyphPainter p) { painter = p; }

  public Segment getText(int p0, int p1) {
    Segment text = new Segment();
    try {
      getDocument().getText(p0, p1 - p0, text);
    } catch (BadLocationException bl) {
      throw new StateInvariantError("GlyphView: Stale view: " + bl);
    }
    return text;
  }

  public Color getBackground() {
    Document doc = getDocument();
    if (doc instanceof StyledDocument) {
      AttributeSet attr = getAttributes();
      if (attr.isDefined(StyleConstants.Background)) {
        return ((StyledDocument) doc).getBackground(attr);
      }
    }
    return null;
  }

  public Color getForeground() {
    Document doc = getDocument();
    if (doc instanceof StyledDocument) {
      AttributeSet attr = getAttributes();
      return ((StyledDocument) doc).getForeground(attr);
    }
    Component c = getContainer();
    return c != null ? c.getForeground() : null;
  }

  public Font getFont() {
    Document doc = getDocument();
    if (doc instanceof StyledDocument) {
      AttributeSet attr = getAttributes();
      return ((StyledDocument) doc).getFont(attr);
    }
    Component c = getContainer();
    return c != null ? c.getFont() : null;
  }

  public boolean isUnderline() { return StyleConstants.isUnderline(getAttributes()); }

  public boolean isStrikeThrough() { return StyleConstants.isStrikeThrough(getAttributes()); }

  public boolean isSubscript() { return StyleConstants.isSubscript(getAttributes()); }

  public boolean isSuperscript() { return StyleConstants.isSuperscript(getAttributes()); }

  public TabExpander getTabExpander() { return expander; }

  private int[] justificationData;

  void setJustificationData(int[] jd) { justificationData = jd; }

  int[] getJustificationDataAt(int pos) {
    int[] jd = justificationData;
    if (jd == null) {
      return null;
    }
    int base = jd[2];
    int start = getStartOffset();
    try {
      int from = Math.max(start, jd[3]);
      int to = Math.min(pos, jd[4]);
      if (to > from) {
        String t = getDocument().getText(from, to - from);
        for (int i = 0; i < t.length(); i++) {
          if (t.charAt(i) == ' ') {
            base++;
          }
        }
      }
    } catch (BadLocationException e) {
      return null;
    }
    return new int[] {jd[0], jd[1], base, jd[3], jd[4]};
  }

  protected void checkPainter() {
    if (painter == null) {
      painter = new DefaultGlyphPainter();
    }
  }

  public float getTabbedSpan(float x, TabExpander e) {
    checkPainter();
    TabExpander old = expander;
    expander = e;
    if (expander != old) {
      preferenceChanged(null, true, false);
    }
    this.x = (int) x;
    int p0 = getStartOffset();
    int p1 = getEndOffset();
    return painter.getSpan(this, p0, p1, expander, x);
  }

  public float getPartialSpan(int p0, int p1) {
    checkPainter();
    return painter.getSpan(this, p0, p1, expander, x);
  }

  public int getStartOffset() {
    Element e = getElement();
    return (length > 0) ? e.getStartOffset() + offset : e.getStartOffset();
  }

  public int getEndOffset() {
    Element e = getElement();
    return (length > 0) ? e.getStartOffset() + offset + length : e.getEndOffset();
  }

  public void paint(Graphics g, Shape a) {
    checkPainter();
    Container c = getContainer();
    int p0 = getStartOffset();
    int p1 = getEndOffset();
    Rectangle alloc = (a instanceof Rectangle) ? (Rectangle) a : a.getBounds();
    Color bg = getBackground();
    Color fg = getForeground();
    if (c != null && !c.isEnabled()) {
      fg = (c instanceof JTextComponent ? ((JTextComponent) c).getDisabledTextColor()
          : UIManager.getColor("textInactiveText"));
    }
    if (bg != null) {
      g.setColor(bg);
      g.fillRect(alloc.x, alloc.y, alloc.width, alloc.height);
    }
    if (c instanceof JTextComponent) {
      JTextComponent tc = (JTextComponent) c;
      Highlighter h = tc.getHighlighter();
      if (h instanceof LayeredHighlighter) {
        ((LayeredHighlighter) h).paintLayeredHighlights(g, p0, p1, a, tc, this);
      }
    }
    g.setColor(fg);
    painter.paint(this, g, a, p0, p1);
  }

  public float getPreferredSpan(int axis) {
    if (impliedCR) {
      return 0;
    }
    checkPainter();
    switch (axis) {
      case View.X_AXIS:
        if (skipWidth) {
          return 0;
        }
        return painter.getSpan(this, getStartOffset(), getEndOffset(), expander, this.x);
      case View.Y_AXIS:
        float h = painter.getHeight(this);
        if (isSuperscript()) {
          h += h / 3;
        }
        return h;
      default:
        throw new IllegalArgumentException("Invalid axis: " + axis);
    }
  }

  public float getMinimumSpan(int axis) {
    switch (axis) {
      case View.X_AXIS:
        if (minimumSpan < 0) {
          minimumSpan = 0;
          int p0 = getStartOffset();
          int p1 = getEndOffset();
          while (p1 > p0) {
            int breakSpot = getBreakSpot(p0, p1);
            if (breakSpot == BreakIterator.DONE) {
              breakSpot = p0;
            }
            minimumSpan = Math.max(minimumSpan, getPartialSpan(breakSpot, p1));
            p1 = breakSpot - 1;
          }
        }
        return minimumSpan;
      case View.Y_AXIS:
        return super.getMinimumSpan(axis);
      default:
        throw new IllegalArgumentException("Invalid axis: " + axis);
    }
  }

  public float getAlignment(int axis) {
    checkPainter();
    if (axis == View.Y_AXIS) {
      boolean sup = isSuperscript();
      boolean sub = isSubscript();
      float h = painter.getHeight(this);
      float d = painter.getDescent(this);
      float a = painter.getAscent(this);
      float align;
      if (sup) {
        align = 1.0f;
      } else if (sub) {
        align = (h > 0) ? (h - (d + (a / 2))) / h : 0;
      } else {
        align = (h > 0) ? (h - d) / h : 0;
      }
      return align;
    }
    return super.getAlignment(axis);
  }

  public Shape modelToView(int pos, Shape a, Position.Bias b) throws BadLocationException {
    checkPainter();
    return painter.modelToView(this, pos, b, a);
  }

  public int viewToModel(float x, float y, Shape a, Position.Bias[] biasReturn) {
    checkPainter();
    return painter.viewToModel(this, x, y, a, biasReturn);
  }

  public int getBreakWeight(int axis, float pos, float len) {
    checkPainter();
    if (axis == View.X_AXIS) {
      this.x = (int) pos;
      int p0 = getStartOffset();
      int p1 = painter.getBoundedPosition(this, p0, pos, len);
      if (p1 == p0) {
        return View.BadBreakWeight;
      }
      if (getBreakSpot(p0, p1) != BreakIterator.DONE) {
        return View.ExcellentBreakWeight;
      }
      return View.GoodBreakWeight;
    }
    return super.getBreakWeight(axis, pos, len);
  }

  public View breakView(int axis, int p0, float pos, float len) {
    if (axis == View.X_AXIS) {
      checkPainter();
      int p1 = painter.getBoundedPosition(this, p0, pos, len);
      int breakSpot = getBreakSpot(p0, p1);
      if (breakSpot != BreakIterator.DONE) {
        p1 = breakSpot;
      }
      if (p0 == getStartOffset() && p1 == getEndOffset()) {
        return this;
      }
      return createFragment(p0, p1);
    }
    return this;
  }

  private int getBreakSpot(int p0, int p1) {
    if (breakSpots == null) {
      int start = getStartOffset();
      int end = getEndOffset();
      int[] bs = new int[end - start + 1];
      int ix = 0;
      Element parent = getElement().getParentElement();
      int pstart = (parent == null ? start : parent.getStartOffset());
      int pend = (parent == null ? end : parent.getEndOffset());
      Segment s = getText(pstart, pend);
      s.first();
      if (breaker == null) {
        Container c = getContainer();
        breaker = BreakIterator.getLineInstance(c != null ? c.getLocale() : Locale.getDefault());
      }
      breaker.setText(s);
      int startFrom = end + (pend > end ? 1 : 0);
      for (;;) {
        startFrom = breaker.preceding(s.offset + (startFrom - pstart)) + (pstart - s.offset);
        if (startFrom > start) {
          bs[ix++] = startFrom;
        } else {
          break;
        }
      }
      breakSpots = new int[ix];
      System.arraycopy(bs, 0, breakSpots, 0, ix);
    }
    int breakSpot = BreakIterator.DONE;
    for (int i = 0; i < breakSpots.length; i++) {
      int bsp = breakSpots[i];
      if (bsp <= p1) {
        if (bsp > p0) {
          breakSpot = bsp;
        }
        break;
      }
    }
    return breakSpot;
  }

  public View createFragment(int p0, int p1) {
    checkPainter();
    Element elem = getElement();
    GlyphView v = (GlyphView) clone();
    v.offset = p0 - elem.getStartOffset();
    v.length = p1 - p0;
    v.painter = painter.getPainter(v, p0, p1);
    v.minimumSpan = -1;
    v.breakSpots = null;
    return v;
  }

  public int getNextVisualPositionFrom(int pos, Position.Bias b, Shape a, int direction, Position.Bias[] biasRet)
      throws BadLocationException {
    if (pos < -1) {
      throw new BadLocationException("invalid position", pos);
    }
    checkPainter();
    return painter.getNextVisualPositionFrom(this, pos, b, a, direction, biasRet);
  }

  public void insertUpdate(DocumentEvent e, Shape a, ViewFactory f) {
    minimumSpan = -1;
    breakSpots = null;
    super.insertUpdate(e, a, f);
  }

  public void removeUpdate(DocumentEvent e, Shape a, ViewFactory f) {
    minimumSpan = -1;
    breakSpots = null;
    super.removeUpdate(e, a, f);
  }

  public void changedUpdate(DocumentEvent e, Shape a, ViewFactory f) {
    minimumSpan = -1;
    breakSpots = null;
    painter = null;
    preferenceChanged(null, true, true);
    super.changedUpdate(e, a, f);
  }

  public static abstract class GlyphPainter {
    public abstract float getSpan(GlyphView v, int p0, int p1, TabExpander e, float x);

    public abstract float getHeight(GlyphView v);

    public abstract float getAscent(GlyphView v);

    public abstract float getDescent(GlyphView v);

    public abstract void paint(GlyphView v, Graphics g, Shape a, int p0, int p1);

    public abstract Shape modelToView(GlyphView v, int pos, Position.Bias bias, Shape a)
        throws BadLocationException;

    public abstract int viewToModel(GlyphView v, float x, float y, Shape a, Position.Bias[] biasReturn);

    public abstract int getBoundedPosition(GlyphView v, int p0, float x, float len);

    public GlyphPainter getPainter(GlyphView v, int p0, int p1) { return this; }

    public int getNextVisualPositionFrom(GlyphView v, int pos, Position.Bias b, Shape a, int direction,
        Position.Bias[] biasRet) throws BadLocationException {
      int startOffset = v.getStartOffset();
      int endOffset = v.getEndOffset();
      switch (direction) {
        case View.NORTH:
        case View.SOUTH:
          if (pos != -1) {
            return -1;
          }
          Container container = v.getContainer();
          if (container instanceof JTextComponent) {
            Caret c = ((JTextComponent) container).getCaret();
            Point magicPoint = (c != null) ? c.getMagicCaretPosition() : null;
            int x;
            if (magicPoint == null) {
              Shape posBounds;
              try {
                posBounds = v.modelToView(pos, a, Position.Bias.Forward);
              } catch (BadLocationException exc) {
                posBounds = null;
              }
              x = (posBounds == null) ? 0 : posBounds.getBounds().x;
            } else {
              x = magicPoint.x;
            }
            return v.viewToModel(x, 0f, a, biasRet);
          }
          break;
        case View.EAST:
          if (startOffset == v.getDocument().getLength()) {
            if (pos == -1) {
              return startOffset;
            }
            return -1;
          }
          if (pos == -1) {
            return startOffset;
          }
          if (pos == endOffset) {
            return -1;
          }
          if (++pos == endOffset) {
            return -1;
          }
          return pos;
        case View.WEST:
          if (startOffset == v.getDocument().getLength()) {
            if (pos == -1) {
              return startOffset;
            }
            return -1;
          }
          if (pos == -1) {
            return endOffset - 1;
          }
          if (pos == startOffset) {
            return -1;
          }
          return pos - 1;
        default:
          throw new IllegalArgumentException("Bad direction: " + direction);
      }
      return pos;
    }
  }

  static final class DefaultGlyphPainter extends GlyphPainter {
    private FontMetrics metrics;
    private Font font;

    private void sync(GlyphView v) {
      Font f = v.getFont();
      if (metrics == null || !f.equals(font)) {
        Container c = v.getContainer();
        metrics = (c != null) ? c.getFontMetrics(f) : java.awt.Toolkit.getDefaultToolkit().getFontMetrics(f);
        font = f;
      }
    }

    public float getSpan(GlyphView v, int p0, int p1, TabExpander e, float x) {
      sync(v);
      Segment text = v.getText(p0, p1);
      return Utilities.getTabbedTextWidth(text, metrics, (int) x, e, p0);
    }

    public float getHeight(GlyphView v) {
      sync(v);
      return metrics.getHeight();
    }

    public float getAscent(GlyphView v) {
      sync(v);
      return metrics.getAscent();
    }

    public float getDescent(GlyphView v) {
      sync(v);
      return metrics.getDescent();
    }

    public void paint(GlyphView v, Graphics g, Shape a, int p0, int p1) {
      sync(v);
      Segment text;
      TabExpander expander = v.getTabExpander();
      Rectangle alloc = (a instanceof Rectangle) ? (Rectangle) a : a.getBounds();
      int x = alloc.x;
      int y = alloc.y + metrics.getHeight() - metrics.getDescent();
      if (v.isSuperscript()) {
        y -= metrics.getAscent() / 2;
      } else if (v.isSubscript()) {
        y += metrics.getDescent() / 2;
      }
      g.setFont(font);
      text = v.getText(p0, p1);
      int width = Utilities.getTabbedTextWidth(text, metrics, x, expander, p0);
      int[] jd = v.getJustificationDataAt(p0);
      if (jd != null) {
        width = (int) Utilities.justifiedWidth(text, metrics, x, expander, p0, jd);
        Utilities.justifiedDraw(text, x, y, g, expander, p0, jd);
      } else {
        Utilities.drawTabbedText(text, x, y, g, expander, p0);
      }
      if (v.isStrikeThrough()) {
        int yTmp = y - (int) (metrics.getAscent() * 0.3f);
        g.drawLine(x, yTmp, x + width, yTmp);
      }
      if (v.isUnderline()) {
        int yTmp = y + 1;
        g.drawLine(x, yTmp, x + width, yTmp);
      }
    }

    public Shape modelToView(GlyphView v, int pos, Position.Bias bias, Shape a) throws BadLocationException {
      sync(v);
      Rectangle alloc = (a instanceof Rectangle) ? (Rectangle) a : a.getBounds();
      int p0 = v.getStartOffset();
      int p1 = v.getEndOffset();
      TabExpander expander = v.getTabExpander();
      if (pos == p1) {
        return new Rectangle(alloc.x + alloc.width, alloc.y, 0, metrics.getHeight());
      }
      if ((pos >= p0) && (pos <= p1)) {
        Segment text = v.getText(p0, pos);
        int[] jd = v.getJustificationDataAt(p0);
        int width = jd != null ? (int) Utilities.justifiedWidth(text, metrics, alloc.x, expander, p0, jd)
            : Utilities.getTabbedTextWidth(text, metrics, alloc.x, expander, p0);
        return new Rectangle(alloc.x + width, alloc.y, 0, metrics.getHeight());
      }
      throw new BadLocationException("modelToView - can't convert", p1);
    }

    public int viewToModel(GlyphView v, float x, float y, Shape a, Position.Bias[] biasReturn) {
      sync(v);
      Rectangle alloc = (a instanceof Rectangle) ? (Rectangle) a : a.getBounds();
      int p0 = v.getStartOffset();
      int p1 = v.getEndOffset();
      TabExpander expander = v.getTabExpander();
      Segment text = v.getText(p0, p1);
      int[] jd = v.getJustificationDataAt(p0);
      int offs = jd != null ? Utilities.justifiedOffset(text, metrics, alloc.x, (int) x, expander, p0, true, jd)
          : Utilities.getTabbedTextOffset(text, metrics, alloc.x, (int) x, expander, p0, true);
      int retValue = p0 + offs;
      if (retValue == p1) {
        retValue--;
      }
      biasReturn[0] = Position.Bias.Forward;
      return retValue;
    }

    public int getBoundedPosition(GlyphView v, int p0, float x, float len) {
      sync(v);
      Segment text = v.getText(p0, v.getEndOffset());
      int offs = Utilities.getTabbedTextOffset(text, metrics, x, x + len, v.getTabExpander(), p0, false);
      return p0 + offs;
    }
  }
}
