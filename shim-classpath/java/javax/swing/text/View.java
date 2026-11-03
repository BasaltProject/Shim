/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */
package javax.swing.text;

import java.awt.Container;
import java.awt.Graphics;
import java.awt.Rectangle;
import java.awt.Shape;
import java.awt.geom.Rectangle2D;
import javax.swing.SwingConstants;
import javax.swing.event.DocumentEvent;

public abstract class View implements SwingConstants {
  public static final int BadBreakWeight = 0;
  public static final int GoodBreakWeight = 1000;
  public static final int ExcellentBreakWeight = 2000;
  public static final int ForcedBreakWeight = 3000;
  public static final int X_AXIS = HORIZONTAL;
  public static final int Y_AXIS = VERTICAL;

  private View parent;
  private Element elem;

  public View(Element elem) {
    this.elem = elem;
  }

  public View getParent() { return parent; }

  public boolean isVisible() { return true; }

  public abstract float getPreferredSpan(int axis);

  public float getMinimumSpan(int axis) {
    int w = getResizeWeight(axis);
    return w == 0 ? getPreferredSpan(axis) : 0;
  }

  public float getMaximumSpan(int axis) {
    int w = getResizeWeight(axis);
    return w == 0 ? getPreferredSpan(axis) : Integer.MAX_VALUE;
  }

  public void preferenceChanged(View child, boolean width, boolean height) {
    View p = getParent();
    if (p != null) {
      p.preferenceChanged(this, width, height);
    }
  }

  public float getAlignment(int axis) { return 0.5f; }

  public abstract void paint(Graphics g, Shape allocation);

  public void setParent(View parent) {
    if (parent == null) {
      for (int i = 0; i < getViewCount(); i++) {
        View v = getView(i);
        if (v.getParent() == this) {
          v.setParent(null);
        }
      }
    }
    this.parent = parent;
  }

  public int getViewCount() { return 0; }

  public View getView(int n) { return null; }

  public void removeAll() { replace(0, getViewCount(), null); }

  public void remove(int i) { replace(i, 1, null); }

  public void insert(int offs, View v) { replace(offs, 0, new View[] {v}); }

  public void append(View v) { replace(getViewCount(), 0, new View[] {v}); }

  public void replace(int offset, int length, View[] views) { }

  public int getViewIndex(int pos, Position.Bias b) { return -1; }

  public Shape getChildAllocation(int index, Shape a) { return null; }

  public int getNextVisualPositionFrom(int pos, Position.Bias b, Shape a, int direction, Position.Bias[] biasRet)
      throws BadLocationException {
    if (pos < -1) {
      throw new BadLocationException("Invalid position", pos);
    }
    biasRet[0] = Position.Bias.Forward;
    switch (direction) {
      case NORTH:
      case SOUTH:
        if (pos == -1) {
          pos = (direction == NORTH) ? Math.max(0, getEndOffset() - 1) : getStartOffset();
        } else {
          JTextComponent target = (JTextComponent) getContainer();
          Caret c = (target != null) ? target.getCaret() : null;
          java.awt.Point mcp = (c != null) ? c.getMagicCaretPosition() : null;
          int x = (mcp == null) ? (int) Math.round(modelToView(pos, a, b).getBounds2D().getX()) : mcp.x;
          pos = (direction == NORTH) ? Utilities.getPositionAbove(target, pos, x)
              : Utilities.getPositionBelow(target, pos, x);
        }
        break;
      case WEST:
        if (pos == -1) {
          pos = Math.max(0, getEndOffset() - 1);
        } else {
          pos = Math.max(0, pos - 1);
        }
        break;
      case EAST:
        if (pos == -1) {
          pos = getStartOffset();
        } else {
          pos = Math.min(pos + 1, getDocument().getLength());
        }
        break;
      default:
        throw new IllegalArgumentException("Bad direction: " + direction);
    }
    return pos;
  }

  public abstract Shape modelToView(int pos, Shape a, Position.Bias b) throws BadLocationException;

  public Shape modelToView(int p0, Position.Bias b0, int p1, Position.Bias b1, Shape a) throws BadLocationException {
    Shape s0 = modelToView(p0, a, b0);
    Shape s1;
    if (p1 == getEndOffset()) {
      try {
        s1 = modelToView(p1, a, b1);
      } catch (BadLocationException ble) {
        s1 = null;
      }
      if (s1 == null) {
        Rectangle2D alloc = (a instanceof Rectangle2D) ? (Rectangle2D) a : a.getBounds2D();
        s1 = new Rectangle2D.Double(alloc.getMaxX(), alloc.getMinY(), 0, alloc.getHeight());
      }
    } else {
      s1 = modelToView(p1, a, b1);
    }
    Rectangle2D r0 = s0.getBounds2D();
    Rectangle2D r1 = (s1 instanceof Rectangle2D) ? (Rectangle2D) s1 : s1.getBounds2D();
    if (r0.getY() != r1.getY()) {
      Rectangle2D alloc = (a instanceof Rectangle2D) ? (Rectangle2D) a : a.getBounds2D();
      r0.setRect(alloc.getX(), r0.getY(), alloc.getWidth(), r0.getHeight());
    }
    r0.add(r1);
    return r0;
  }

  public abstract int viewToModel(float x, float y, Shape a, Position.Bias[] biasReturn);

  public int viewToModel(float x, float y, Shape a) {
    sharedBiasReturn[0] = Position.Bias.Forward;
    return viewToModel(x, y, a, sharedBiasReturn);
  }

  private static final Position.Bias[] sharedBiasReturn = new Position.Bias[1];

  public void insertUpdate(DocumentEvent e, Shape a, ViewFactory f) {
    if (getViewCount() > 0) {
      Element elem = getElement();
      DocumentEvent.ElementChange ec = e.getChange(elem);
      if (ec != null) {
        if (!updateChildren(ec, e, f)) {
          ec = null;
        }
      }
      forwardUpdate(ec, e, a, f);
      updateLayout(ec, e, a);
    }
  }

  public void removeUpdate(DocumentEvent e, Shape a, ViewFactory f) {
    if (getViewCount() > 0) {
      Element elem = getElement();
      DocumentEvent.ElementChange ec = e.getChange(elem);
      if (ec != null) {
        if (!updateChildren(ec, e, f)) {
          ec = null;
        }
      }
      forwardUpdate(ec, e, a, f);
      updateLayout(ec, e, a);
    }
  }

  public void changedUpdate(DocumentEvent e, Shape a, ViewFactory f) {
    if (getViewCount() > 0) {
      Element elem = getElement();
      DocumentEvent.ElementChange ec = e.getChange(elem);
      if (ec != null) {
        if (!updateChildren(ec, e, f)) {
          ec = null;
        }
      }
      forwardUpdate(ec, e, a, f);
      updateLayout(ec, e, a);
    }
  }

  public Document getDocument() { return elem.getDocument(); }

  public int getStartOffset() { return elem.getStartOffset(); }

  public int getEndOffset() { return elem.getEndOffset(); }

  public Element getElement() { return elem; }

  public Graphics getGraphics() {
    Container c = getContainer();
    return c == null ? null : c.getGraphics();
  }

  public AttributeSet getAttributes() { return elem.getAttributes(); }

  public View breakView(int axis, int offset, float pos, float len) { return this; }

  public View createFragment(int p0, int p1) { return this; }

  public int getBreakWeight(int axis, float pos, float len) {
    return len > getPreferredSpan(axis) ? GoodBreakWeight : BadBreakWeight;
  }

  public int getResizeWeight(int axis) { return 0; }

  public void setSize(float width, float height) { }

  public Container getContainer() {
    View v = getParent();
    return v != null ? v.getContainer() : null;
  }

  public ViewFactory getViewFactory() {
    View v = getParent();
    return v != null ? v.getViewFactory() : null;
  }

  public String getToolTipText(float x, float y, Shape allocation) {
    int viewIndex = getViewIndex(x, y, allocation);
    if (viewIndex >= 0) {
      allocation = getChildAllocation(viewIndex, allocation);
      Rectangle rect = (allocation instanceof Rectangle) ? (Rectangle) allocation : allocation.getBounds();
      if (rect.contains(x, y)) {
        return getView(viewIndex).getToolTipText(x, y, allocation);
      }
    }
    return null;
  }

  public int getViewIndex(float x, float y, Shape allocation) {
    for (int counter = getViewCount() - 1; counter >= 0; counter--) {
      Shape childAllocation = getChildAllocation(counter, allocation);
      if (childAllocation != null) {
        Rectangle rect = (childAllocation instanceof Rectangle) ? (Rectangle) childAllocation
            : childAllocation.getBounds();
        if (rect.contains(x, y)) {
          return counter;
        }
      }
    }
    return -1;
  }

  protected boolean updateChildren(DocumentEvent.ElementChange ec, DocumentEvent e, ViewFactory f) {
    Element[] removedElems = ec.getChildrenRemoved();
    Element[] addedElems = ec.getChildrenAdded();
    View[] added = null;
    if (addedElems != null) {
      added = new View[addedElems.length];
      for (int i = 0; i < addedElems.length; i++) {
        added[i] = f.create(addedElems[i]);
      }
    }
    int nremoved = 0;
    int index = ec.getIndex();
    if (removedElems != null) {
      nremoved = removedElems.length;
    }
    replace(index, nremoved, added);
    return true;
  }

  protected void forwardUpdate(DocumentEvent.ElementChange ec, DocumentEvent e, Shape a, ViewFactory f) {
    calculateUpdateIndexes(e);
    int hole1 = lastUpdateIndex + 1;
    int hole2 = hole1;
    Element[] addedElems = (ec != null) ? ec.getChildrenAdded() : null;
    if ((addedElems != null) && (addedElems.length > 0)) {
      hole1 = ec.getIndex();
      hole2 = hole1 + addedElems.length - 1;
    }
    for (int i = firstUpdateIndex; i <= lastUpdateIndex; i++) {
      if (!((i >= hole1) && (i <= hole2))) {
        View v = getView(i);
        if (v != null) {
          Shape childAlloc = getChildAllocation(i, a);
          forwardUpdateToView(v, e, childAlloc, f);
        }
      }
    }
  }

  void calculateUpdateIndexes(DocumentEvent e) {
    int pos = e.getOffset();
    firstUpdateIndex = getViewIndex(pos, Position.Bias.Forward);
    if (firstUpdateIndex == -1 && e.getType() == DocumentEvent.EventType.REMOVE && pos >= getEndOffset()) {
      firstUpdateIndex = getViewCount() - 1;
    }
    lastUpdateIndex = firstUpdateIndex;
    View v = (firstUpdateIndex >= 0) ? getView(firstUpdateIndex) : null;
    if (v != null) {
      if ((v.getStartOffset() == pos) && (pos > 0)) {
        firstUpdateIndex = Math.max(firstUpdateIndex - 1, 0);
      }
    }
    if (e.getType() != DocumentEvent.EventType.REMOVE) {
      lastUpdateIndex = getViewIndex(pos + e.getLength(), Position.Bias.Forward);
      if (lastUpdateIndex < 0) {
        lastUpdateIndex = getViewCount() - 1;
      }
    }
    firstUpdateIndex = Math.max(firstUpdateIndex, 0);
  }

  int firstUpdateIndex;
  int lastUpdateIndex;

  protected void forwardUpdateToView(View v, DocumentEvent e, Shape a, ViewFactory f) {
    View parent = v.getParent();
    v.setParent(this);
    DocumentEvent.EventType t = e.getType();
    if (t == DocumentEvent.EventType.INSERT) {
      v.insertUpdate(e, a, f);
    } else if (t == DocumentEvent.EventType.REMOVE) {
      v.removeUpdate(e, a, f);
    } else {
      v.changedUpdate(e, a, f);
    }
    v.setParent(parent);
  }

  protected void updateLayout(DocumentEvent.ElementChange ec, DocumentEvent e, Shape a) {
    if ((ec != null) && (a != null)) {
      preferenceChanged(null, true, true);
      Container host = getContainer();
      if (host != null) {
        host.repaint();
      }
    }
  }

  public Shape modelToView(int pos, Shape a) throws BadLocationException {
    return modelToView(pos, a, Position.Bias.Forward);
  }
}
