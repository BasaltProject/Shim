/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */
package javax.swing.text;

import java.awt.Graphics;
import java.awt.Rectangle;
import java.awt.Shape;
import java.awt.font.TextAttribute;
import javax.swing.SizeRequirements;
import javax.swing.event.DocumentEvent;

public class ParagraphView extends FlowView implements TabExpander {
  private int justification;
  private float lineSpacing;
  protected int firstLineIndent = 0;
  private int tabBase;
  static char[] tabChars = {'\t'};
  static char[] tabDecimalChars = {'\t', '.'};

  public ParagraphView(Element elem) {
    super(elem, View.Y_AXIS);
    setPropertiesFromAttributes();
  }

  protected void setJustification(int j) { justification = j; }

  protected void setLineSpacing(float ls) { lineSpacing = ls; }

  protected void setFirstLineIndent(float fi) { firstLineIndent = (int) fi; }

  protected void setPropertiesFromAttributes() {
    AttributeSet attr = getAttributes();
    if (attr != null) {
      setParagraphInsets(attr);
      Integer a = (Integer) attr.getAttribute(StyleConstants.Alignment);
      int alignment;
      if (a == null) {
        Document doc = getElement().getDocument();
        Object o = doc.getProperty(TextAttribute.RUN_DIRECTION);
        if ((o != null) && o.equals(TextAttribute.RUN_DIRECTION_RTL)) {
          alignment = StyleConstants.ALIGN_RIGHT;
        } else {
          alignment = StyleConstants.ALIGN_LEFT;
        }
      } else {
        alignment = a.intValue();
      }
      setJustification(alignment);
      setLineSpacing(StyleConstants.getLineSpacing(attr));
      setFirstLineIndent(StyleConstants.getFirstLineIndent(attr));
    }
  }

  protected int getLayoutViewCount() { return layoutPool.getViewCount(); }

  protected View getLayoutView(int index) { return layoutPool.getView(index); }

  protected int getNextNorthSouthVisualPositionFrom(int pos, Position.Bias b, Shape a, int direction,
      Position.Bias[] biasRet) throws BadLocationException {
    int vIndex;
    if (pos == -1) {
      vIndex = (direction == NORTH) ? getViewCount() - 1 : 0;
    } else {
      if (b == Position.Bias.Backward && pos > 0) {
        vIndex = getViewIndexAtPosition(pos - 1);
      } else {
        vIndex = getViewIndexAtPosition(pos);
      }
      if (direction == NORTH) {
        if (vIndex == 0) {
          return -1;
        }
        vIndex--;
      } else if (++vIndex >= getViewCount()) {
        return -1;
      }
    }
    JTextComponent text = (JTextComponent) getContainer();
    Caret c = text.getCaret();
    java.awt.Point magicPoint = (c != null) ? c.getMagicCaretPosition() : null;
    int x;
    if (magicPoint == null) {
      Shape posBounds;
      try {
        posBounds = text.getUI().modelToView(text, pos, b);
      } catch (BadLocationException exc) {
        posBounds = null;
      }
      x = posBounds == null ? 0 : posBounds.getBounds().x;
    } else {
      x = magicPoint.x;
    }
    return getClosestPositionTo(pos, b, a, direction, biasRet, vIndex, x);
  }

  protected int getClosestPositionTo(int pos, Position.Bias b, Shape a, int direction, Position.Bias[] biasRet,
      int rowIndex, int x) throws BadLocationException {
    JTextComponent text = (JTextComponent) getContainer();
    Document doc = getDocument();
    AbstractDocument aDoc = (doc instanceof AbstractDocument) ? (AbstractDocument) doc : null;
    View row = getView(rowIndex);
    int lastPos = -1;
    biasRet[0] = Position.Bias.Forward;
    for (int vc = 0, numViews = row.getViewCount(); vc < numViews; vc++) {
      View v = row.getView(vc);
      int start = v.getStartOffset();
      Shape childAlloc = row.getChildAllocation(vc, a);
      if (childAlloc.getBounds().x + childAlloc.getBounds().width > x) {
        pos = v.viewToModel(x, 0, childAlloc, biasRet);
        return pos;
      }
      lastPos = Math.max(start, v.getEndOffset() - 1);
    }
    return lastPos == -1 ? getStartOffset() : lastPos;
  }

  protected boolean flipEastAndWestAtEnds(int position, Position.Bias bias) { return false; }

  public int getFlowSpan(int index) {
    View child = getView(index);
    int adjust = 0;
    if (child instanceof Row) {
      Row row = (Row) child;
      adjust = row.getLeftInset() + row.getRightInset();
    }
    return (layoutSpan == Integer.MAX_VALUE) ? layoutSpan : (layoutSpan - adjust);
  }

  public int getFlowStart(int index) {
    View child = getView(index);
    int adjust = 0;
    if (child instanceof Row) {
      Row row = (Row) child;
      adjust = row.getLeftInset();
    }
    return tabBase + adjust;
  }

  protected View createRow() { return new Row(getElement()); }

  public float nextTabStop(float x, int tabOffset) {
    if (justification != StyleConstants.ALIGN_LEFT) {
      return x + 10.0f;
    }
    x -= tabBase;
    TabSet tabs = getTabSet();
    if (tabs == null) {
      return (float) (tabBase + (((int) x / 72 + 1) * 72));
    }
    TabStop tab = tabs.getTabAfter(x + .01f);
    if (tab == null) {
      return tabBase + x + 5.0f;
    }
    int alignment = tab.getAlignment();
    int offset;
    switch (alignment) {
      default:
      case TabStop.ALIGN_LEFT:
        return tabBase + tab.getPosition();
      case TabStop.ALIGN_BAR:
        return tabBase + tab.getPosition();
      case TabStop.ALIGN_RIGHT:
      case TabStop.ALIGN_CENTER:
        offset = findOffsetToCharactersInString(tabChars, tabOffset + 1);
        break;
      case TabStop.ALIGN_DECIMAL:
        offset = findOffsetToCharactersInString(tabDecimalChars, tabOffset + 1);
        break;
    }
    if (offset == -1) {
      offset = getEndOffset();
    }
    float charsSize = getPartialSize(tabOffset + 1, offset);
    switch (alignment) {
      case TabStop.ALIGN_RIGHT:
      case TabStop.ALIGN_DECIMAL:
        return tabBase + Math.max(x, tab.getPosition() - charsSize);
      case TabStop.ALIGN_CENTER:
        return tabBase + Math.max(x, tab.getPosition() - charsSize / 2.0f);
      default:
        return tabBase + tab.getPosition();
    }
  }

  protected TabSet getTabSet() { return StyleConstants.getTabSet(getElement().getAttributes()); }

  protected float getPartialSize(int startOffset, int endOffset) {
    float size = 0.0f;
    int viewIndex;
    int numViews = getViewCount();
    View view;
    int viewEnd;
    int tempEnd;
    viewIndex = getElement().getElementIndex(startOffset);
    numViews = layoutPool.getViewCount();
    while (startOffset < endOffset && viewIndex < numViews) {
      view = layoutPool.getView(viewIndex++);
      viewEnd = view.getEndOffset();
      tempEnd = Math.min(endOffset, viewEnd);
      if (view instanceof TabableView) {
        size += ((TabableView) view).getPartialSpan(startOffset, tempEnd);
      } else if (startOffset == view.getStartOffset() && tempEnd == view.getEndOffset()) {
        size += view.getPreferredSpan(View.X_AXIS);
      } else {
        return 0.0f;
      }
      startOffset = viewEnd;
    }
    return size;
  }

  protected int findOffsetToCharactersInString(char[] string, int start) {
    int stringLength = string.length;
    int end = getEndOffset();
    Segment seg = new Segment();
    try {
      getDocument().getText(start, end - start, seg);
    } catch (BadLocationException ble) {
      return -1;
    }
    for (int counter = seg.offset, maxCounter = seg.offset + seg.count; counter < maxCounter; counter++) {
      char currentChar = seg.array[counter];
      for (int subCounter = 0; subCounter < stringLength; subCounter++) {
        if (currentChar == string[subCounter]) {
          return counter - seg.offset + start;
        }
      }
    }
    return -1;
  }

  protected float getTabBase() { return (float) tabBase; }

  public void paint(Graphics g, Shape a) {
    Rectangle alloc = (a instanceof Rectangle) ? (Rectangle) a : a.getBounds();
    tabBase = alloc.x + getLeftInset();
    super.paint(g, a);
  }

  public float getAlignment(int axis) {
    switch (axis) {
      case Y_AXIS:
        float a = 0.5f;
        if (getViewCount() != 0) {
          int paragraphSpan = (int) getPreferredSpan(View.Y_AXIS);
          View v = getView(0);
          int rowSpan = (int) v.getPreferredSpan(View.Y_AXIS);
          a = (paragraphSpan != 0) ? ((float) (rowSpan / 2)) / paragraphSpan : 0;
        }
        return a;
      case X_AXIS:
        return 0.5f;
      default:
        throw new IllegalArgumentException("Invalid axis: " + axis);
    }
  }

  public View breakView(int axis, float len, Shape a) {
    if (axis == View.Y_AXIS) {
      return this;
    }
    return this;
  }

  public int getBreakWeight(int axis, float len) {
    if (axis == View.Y_AXIS) {
      return BadBreakWeight;
    }
    return BadBreakWeight;
  }

  public void changedUpdate(DocumentEvent changes, Shape a, ViewFactory f) {
    setPropertiesFromAttributes();
    layoutChanged(X_AXIS);
    layoutChanged(Y_AXIS);
    super.changedUpdate(changes, a, f);
  }

  protected SizeRequirements calculateMinorAxisRequirements(int axis, SizeRequirements r) {
    r = super.calculateMinorAxisRequirements(axis, r);
    float min = 0;
    float glue = 0;
    int n = getLayoutViewCount();
    for (int i = 0; i < n; i++) {
      View v = getLayoutView(i);
      float span = v.getMinimumSpan(axis);
      if (v.getBreakWeight(axis, 0, v.getMaximumSpan(axis)) > View.BadBreakWeight) {
        min = Math.max(min, glue + span);
        glue = span;
      } else {
        glue += span;
        min = Math.max(min, glue);
      }
    }
    r.minimum = Math.max(r.minimum, (int) min);
    r.preferred = Math.max(r.minimum, r.preferred);
    r.maximum = Math.max(r.preferred, r.maximum);
    return r;
  }

  class Row extends BoxView {
    Row(Element elem) {
      super(elem, View.X_AXIS);
    }

    protected void loadChildren(ViewFactory f) { }

    public AttributeSet getAttributes() {
      View p = getParent();
      return (p != null) ? p.getAttributes() : null;
    }

    public float getAlignment(int axis) {
      if (axis == View.X_AXIS) {
        switch (justification) {
          case StyleConstants.ALIGN_LEFT:
            return 0;
          case StyleConstants.ALIGN_RIGHT:
            return 1;
          case StyleConstants.ALIGN_CENTER:
            return 0.5f;
          case StyleConstants.ALIGN_JUSTIFIED:
            return 0.5f;
          default:
            break;
        }
      }
      return super.getAlignment(axis);
    }

    public Shape modelToView(int pos, Shape a, Position.Bias b) throws BadLocationException {
      Rectangle r = a.getBounds();
      View v = getViewAtPosition(pos, r);
      if ((v != null) && (!v.getElement().isLeaf())) {
        return super.modelToView(pos, a, b);
      }
      r = a.getBounds();
      int height = r.height;
      int y = r.y;
      Shape loc = super.modelToView(pos, a, b);
      r = loc.getBounds();
      r.height = height;
      r.y = y;
      return r;
    }

    public int getStartOffset() {
      int offs = Integer.MAX_VALUE;
      int n = getViewCount();
      for (int i = 0; i < n; i++) {
        View v = getView(i);
        offs = Math.min(offs, v.getStartOffset());
      }
      return offs;
    }

    public int getEndOffset() {
      int offs = 0;
      int n = getViewCount();
      for (int i = 0; i < n; i++) {
        View v = getView(i);
        offs = Math.max(offs, v.getEndOffset());
      }
      return offs;
    }

    protected void layoutMinorAxis(int targetSpan, int axis, int[] offsets, int[] spans) {
      baselineLayout(targetSpan, axis, offsets, spans);
    }

    protected SizeRequirements calculateMinorAxisRequirements(int axis, SizeRequirements r) {
      return baselineRequirements(axis, r);
    }

    public float getMaximumSpan(int axis) {
      if (axis == View.X_AXIS && justification == StyleConstants.ALIGN_JUSTIFIED) {
        return Float.MAX_VALUE;
      }
      return super.getMaximumSpan(axis);
    }

    protected void layoutMajorAxis(int targetSpan, int axis, int[] offsets, int[] spans) {
      super.layoutMajorAxis(targetSpan, axis, offsets, spans);
      int n = getViewCount();
      for (int i = 0; i < n; i++) {
        View v = getView(i);
        if (v instanceof GlyphView) {
          ((GlyphView) v).setJustificationData(null);
        }
      }
      if (justification != StyleConstants.ALIGN_JUSTIFIED || n == 0) {
        return;
      }
      View parent = getParent();
      if (parent == null || parent.getView(parent.getViewCount() - 1) == this) {
        return;
      }
      int rs = getStartOffset();
      int re = getEndOffset();
      String text;
      try {
        text = getDocument().getText(rs, re - rs);
      } catch (BadLocationException e) {
        return;
      }
      int a = 0;
      while (a < text.length() && (text.charAt(a) == ' ' || text.charAt(a) == '\t')) {
        a++;
      }
      int b = text.length() - 1;
      while (b >= a && (text.charAt(b) == ' ' || text.charAt(b) == '\n' || text.charAt(b) == '\t')) {
        b--;
      }
      if (b < a || text.charAt(text.length() - 1) == '\n') {
        return;
      }
      int total = 0;
      for (int i = a; i <= b; i++) {
        if (text.charAt(i) == ' ') {
          total++;
        }
      }
      if (total == 0) {
        return;
      }
      int sum = 0;
      for (int i = 0; i < n; i++) {
        sum += spans[i];
      }
      int extra = targetSpan - sum;
      if (extra <= 0) {
        return;
      }
      int addon = extra / total;
      int leftover = extra % total;
      int jStart = rs + a;
      int jEnd = rs + b + 1;
      int spaceIdx = 0;
      int shift = 0;
      for (int i = 0; i < n; i++) {
        View v = getView(i);
        offsets[i] += shift;
        if (v instanceof GlyphView) {
          int from = Math.max(v.getStartOffset(), jStart);
          int to = Math.min(v.getEndOffset(), jEnd);
          int cnt = 0;
          int own = 0;
          for (int p = from; p < to; p++) {
            if (text.charAt(p - rs) == ' ') {
              own += addon + (spaceIdx + cnt < leftover ? 1 : 0);
              cnt++;
            }
          }
          ((GlyphView) v).setJustificationData(new int[] {addon, leftover, spaceIdx, jStart, jEnd});
          spans[i] += own;
          shift += own;
          spaceIdx += cnt;
        }
      }
    }

    protected short getBottomInset() {
      short inset = super.getBottomInset();
      if (lineSpacing > 0) {
        SizeRequirements r = baselineRequirements(View.Y_AXIS, null);
        inset += (short) (lineSpacing * r.preferred);
      }
      return inset;
    }

    protected short getLeftInset() {
      View parent = getParent();
      int adjust = 0;
      if (parent != null) {
        if (this == parent.getView(0)) {
          adjust = firstLineIndent;
        }
      }
      return (short) (super.getLeftInset() + adjust);
    }
  }
}
