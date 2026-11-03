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
import java.util.ArrayList;
import java.util.List;
import javax.swing.SizeRequirements;
import javax.swing.event.DocumentEvent;

public class AsyncBoxView extends View {
  private final int axis;
  private final List<ChildState> stats = new ArrayList<ChildState>();
  protected ChildLocator locator;
  private float topInset;
  private float bottomInset;
  private float leftInset;
  private float rightInset;
  private float minorSpan;
  private float majorSpan;
  private boolean estimatedMajorSpan;
  private boolean majorChanged;
  private boolean minorChanged;
  private float minRequest;
  private float prefRequest;
  private float maxRequest;
  private ChildState changing;

  public AsyncBoxView(Element elem, int axis) {
    super(elem);
    this.axis = axis;
    locator = new ChildLocator();
    minorSpan = Short.MAX_VALUE;
  }

  public int getMajorAxis() { return axis; }

  public int getMinorAxis() { return axis == X_AXIS ? Y_AXIS : X_AXIS; }

  public float getTopInset() { return topInset; }

  public void setTopInset(float i) { topInset = i; }

  public float getBottomInset() { return bottomInset; }

  public void setBottomInset(float i) { bottomInset = i; }

  public float getLeftInset() { return leftInset; }

  public void setLeftInset(float i) { leftInset = i; }

  public float getRightInset() { return rightInset; }

  public void setRightInset(float i) { rightInset = i; }

  protected float getInsetSpan(int axis) {
    float margin = axis == X_AXIS ? getLeftInset() + getRightInset() : getTopInset() + getBottomInset();
    return margin;
  }

  protected void setEstimatedMajorSpan(boolean isEstimated) { estimatedMajorSpan = isEstimated; }

  protected boolean getEstimatedMajorSpan() { return estimatedMajorSpan; }

  protected ChildState getChildState(int index) {
    synchronized (stats) {
      if (index >= 0 && index < stats.size()) {
        return stats.get(index);
      }
      return null;
    }
  }

  protected LayoutQueue getLayoutQueue() { return LayoutQueue.getDefaultQueue(); }

  protected ChildState createChildState(View v) { return new ChildState(v); }

  protected synchronized void majorRequirementChange(ChildState cs, float delta) {
    if (!estimatedMajorSpan) {
      majorSpan += delta;
    }
    majorChanged = true;
  }

  protected synchronized void minorRequirementChange(ChildState cs) { minorChanged = true; }

  protected void flushRequirementChanges() {
    View parent;
    boolean horizontal = false;
    boolean vertical = false;
    synchronized (this) {
      if (!majorChanged && !minorChanged) {
        return;
      }
      parent = getParent();
      if (minorChanged) {
        float min = 0;
        float pref = 0;
        float max = Integer.MAX_VALUE;
        int n = getViewCount();
        for (int i = 0; i < n; i++) {
          ChildState cs = getChildState(i);
          min = Math.max(cs.getChildView().getMinimumSpan(getMinorAxis()), min);
          pref = Math.max(cs.getChildView().getPreferredSpan(getMinorAxis()), pref);
        }
        minRequest = min;
        prefRequest = pref;
        maxRequest = max;
        minorChanged = false;
        if (getMinorAxis() == X_AXIS) {
          horizontal = true;
        } else {
          vertical = true;
        }
      }
      if (majorChanged) {
        majorChanged = false;
        if (getMajorAxis() == X_AXIS) {
          horizontal = true;
        } else {
          vertical = true;
        }
      }
    }
    if (parent != null) {
      parent.preferenceChanged(this, horizontal, vertical);
    }
  }

  public void replace(int offset, int length, View[] views) {
    synchronized (stats) {
      for (int i = 0; i < length; i++) {
        ChildState cs = stats.remove(offset);
        float csSpan = cs.getMajorSpan();
        cs.getChildView().setParent(null);
        if (csSpan != 0) {
          majorRequirementChange(cs, -csSpan);
        }
      }
      if (views != null) {
        for (int i = 0; i < views.length; i++) {
          ChildState s = createChildState(views[i]);
          stats.add(offset + i, s);
          s.run();
        }
      }
    }
    flushRequirementChanges();
  }

  protected void loadChildren(ViewFactory f) {
    Element elem = getElement();
    int n = elem.getElementCount();
    View[] added = new View[n];
    for (int i = 0; i < n; i++) {
      added[i] = f.create(elem.getElement(i));
    }
    replace(0, getViewCount(), added);
  }

  protected int getViewIndexAtPosition(int pos, Position.Bias b) {
    boolean isBackward = b == Position.Bias.Backward;
    pos = isBackward ? Math.max(0, pos - 1) : pos;
    return getElement().getElementIndex(pos);
  }

  protected void updateLayout(DocumentEvent.ElementChange ec, DocumentEvent e, Shape a) {
    if (ec != null) {
      int index = Math.max(ec.getIndex() - 1, 0);
      ChildState cs = getChildState(index);
      locator.childChanged(cs);
    }
  }

  public void setParent(View parent) {
    super.setParent(parent);
    if (parent != null && getViewCount() == 0) {
      loadChildren(getViewFactory());
    }
  }

  public synchronized void preferenceChanged(View child, boolean width, boolean height) {
    if (child == null) {
      if (getParent() != null) {
        getParent().preferenceChanged(this, width, height);
      }
    } else {
      if (changing != null && changing.getChildView() == child) {
        changing.preferenceChanged(width, height);
        return;
      }
      int index = getViewIndex(child.getStartOffset(), Position.Bias.Forward);
      ChildState cs = getChildState(index);
      if (cs != null) {
        cs.preferenceChanged(width, height);
        cs.run();
        flushRequirementChanges();
      }
    }
  }

  public void setSize(float width, float height) {
    setSpanOnAxis(X_AXIS, width);
    setSpanOnAxis(Y_AXIS, height);
  }

  void setSpanOnAxis(int axis, float span) {
    float targetSpan = span - getInsetSpan(axis);
    if (axis == getMajorAxis()) {
      return;
    }
    if (targetSpan != minorSpan) {
      minorSpan = targetSpan;
      int n = getViewCount();
      for (int i = 0; i < n; i++) {
        ChildState cs = getChildState(i);
        cs.childSizeValid = false;
        cs.run();
      }
      flushRequirementChanges();
    }
  }

  public float getPreferredSpan(int axis) {
    float margin = getInsetSpan(axis);
    if (axis == this.axis) {
      return majorSpan + margin;
    }
    return prefRequest + margin;
  }

  public float getMinimumSpan(int axis) {
    if (axis == this.axis) {
      return getPreferredSpan(axis);
    }
    return minRequest + getInsetSpan(axis);
  }

  public float getMaximumSpan(int axis) {
    if (axis == this.axis) {
      return getPreferredSpan(axis);
    }
    return Integer.MAX_VALUE;
  }

  public int getViewCount() {
    synchronized (stats) {
      return stats.size();
    }
  }

  public View getView(int n) {
    ChildState cs = getChildState(n);
    return cs != null ? cs.getChildView() : null;
  }

  public Shape getChildAllocation(int index, Shape a) {
    Shape ca = locator.getChildAllocation(index, a);
    return ca;
  }

  public int getViewIndex(int pos, Position.Bias b) { return getViewIndexAtPosition(pos, b); }

  public void paint(Graphics g, Shape alloc) {
    synchronized (locator) {
      locator.setAllocation(alloc);
      locator.paintChildren(g);
    }
  }

  public Shape modelToView(int pos, Shape a, Position.Bias b) throws BadLocationException {
    int index = getViewIndex(pos, b);
    Shape ca = locator.getChildAllocation(index, a);
    ChildState cs = getChildState(index);
    synchronized (cs) {
      View cv = cs.getChildView();
      return cv.modelToView(pos, ca, b);
    }
  }

  public int viewToModel(float x, float y, Shape a, Position.Bias[] biasReturn) {
    int pos;
    int index;
    Shape ca;
    synchronized (locator) {
      index = locator.getViewIndexAtPoint(x, y, a);
      ca = locator.getChildAllocation(index, a);
    }
    ChildState cs = getChildState(index);
    synchronized (cs) {
      View v = cs.getChildView();
      pos = v.viewToModel(x, y, ca, biasReturn);
    }
    return pos;
  }

  public int getNextVisualPositionFrom(int pos, Position.Bias b, Shape a, int direction, Position.Bias[] biasRet)
      throws BadLocationException {
    if (pos < -1) {
      throw new BadLocationException("invalid position", pos);
    }
    return Utilities.getNextVisualPositionFrom(this, pos, b, a, direction, biasRet);
  }

  public class ChildLocator {
    protected ChildState lastValidOffset;
    protected Rectangle lastAlloc = new Rectangle();
    protected Rectangle childAlloc = new Rectangle();

    public ChildLocator() {
    }

    public synchronized void childChanged(ChildState cs) {
      if (lastValidOffset == null) {
        lastValidOffset = cs;
      } else if (cs.getChildView().getStartOffset() < lastValidOffset.getChildView().getStartOffset()) {
        lastValidOffset = cs;
      }
    }

    protected synchronized void setAllocation(Shape a) {
      if (a instanceof Rectangle) {
        lastAlloc.setBounds((Rectangle) a);
      } else {
        lastAlloc.setBounds(a.getBounds());
      }
      setSize(lastAlloc.width, lastAlloc.height);
    }

    public synchronized int getViewIndexAtPoint(float x, float y, Shape a) {
      setAllocation(a);
      float targetOffset = getMajorAxis() == X_AXIS ? x - lastAlloc.x - getLeftInset() : y - lastAlloc.y - getTopInset();
      return getViewIndexAtVisualOffset(targetOffset);
    }

    protected int getViewIndexAtVisualOffset(float targetOffset) {
      int n = getViewCount();
      if (n > 0) {
        float offset = 0;
        for (int i = 0; i < n; i++) {
          ChildState cs = getChildState(i);
          cs.setMajorOffset(offset);
          offset += cs.getMajorSpan();
          if (targetOffset < offset) {
            return i;
          }
        }
        return n - 1;
      }
      return -1;
    }

    protected Shape getChildAllocation(int index) {
      ChildState cs = getChildState(index);
      float offset = 0;
      for (int i = 0; i < index; i++) {
        offset += getChildState(i).getMajorSpan();
      }
      cs.setMajorOffset(offset);
      setChildAllocation(cs);
      return childAlloc;
    }

    private void setChildAllocation(ChildState cs) {
      if (getMajorAxis() == X_AXIS) {
        childAlloc.x = lastAlloc.x + (int) getLeftInset() + (int) cs.getMajorOffset();
        childAlloc.y = lastAlloc.y + (int) getTopInset() + (int) cs.getMinorOffset();
        childAlloc.width = (int) cs.getMajorSpan();
        childAlloc.height = (int) cs.getMinorSpan();
      } else {
        childAlloc.y = lastAlloc.y + (int) getTopInset() + (int) cs.getMajorOffset();
        childAlloc.x = lastAlloc.x + (int) getLeftInset() + (int) cs.getMinorOffset();
        childAlloc.height = (int) cs.getMajorSpan();
        childAlloc.width = (int) cs.getMinorSpan();
      }
    }

    public synchronized Shape getChildAllocation(int index, Shape a) {
      if (a == null) {
        return null;
      }
      setAllocation(a);
      ChildState cs = getChildState(index);
      float offset = 0;
      for (int i = 0; i < index; i++) {
        offset += getChildState(i).getMajorSpan();
      }
      cs.setMajorOffset(offset);
      Rectangle r = new Rectangle();
      if (getMajorAxis() == X_AXIS) {
        r.x = lastAlloc.x + (int) getLeftInset() + (int) cs.getMajorOffset();
        r.y = lastAlloc.y + (int) getTopInset() + (int) cs.getMinorOffset();
        r.width = (int) cs.getMajorSpan();
        r.height = (int) cs.getMinorSpan();
      } else {
        r.y = lastAlloc.y + (int) getTopInset() + (int) cs.getMajorOffset();
        r.x = lastAlloc.x + (int) getLeftInset() + (int) cs.getMinorOffset();
        r.height = (int) cs.getMajorSpan();
        r.width = (int) cs.getMinorSpan();
      }
      return r;
    }

    public synchronized void paintChildren(Graphics g) {
      Rectangle clip = g.getClipBounds();
      float targetOffset = getMajorAxis() == X_AXIS ? clip.x - lastAlloc.x - getLeftInset()
          : clip.y - lastAlloc.y - getTopInset();
      int index = getViewIndexAtVisualOffset(targetOffset);
      int n = getViewCount();
      for (int i = Math.max(index, 0); i < n; i++) {
        Shape ca = getChildAllocation(i, lastAlloc);
        ChildState cs = getChildState(i);
        cs.getChildView().paint(g, ca);
        Rectangle r = ca.getBounds();
        if (!clip.intersects(r) && i > index) {
          break;
        }
      }
    }
  }

  public class ChildState implements Runnable {
    private final View child;
    private float minorOffset;
    private float majorOffset;
    private float majorSpan;
    private float min;
    private float pref;
    private float max;
    private boolean childSizeValid;
    private boolean minorValid;
    private boolean majorValid;

    public ChildState(View v) {
      child = v;
      minorValid = false;
      majorValid = false;
      childSizeValid = false;
      child.setParent(AsyncBoxView.this);
    }

    public View getChildView() { return child; }

    public void run() {
      AbstractDocument doc = getDocument() instanceof AbstractDocument ? (AbstractDocument) getDocument() : null;
      if (doc != null) {
        doc.readLock();
      }
      try {
        synchronized (AsyncBoxView.this) {
          changing = this;
        }
        try {
          updateChild();
        } finally {
          synchronized (AsyncBoxView.this) {
            changing = null;
          }
        }
        updateChild();
      } finally {
        if (doc != null) {
          doc.readUnlock();
        }
      }
    }

    void updateChild() {
      boolean minorUpdated = false;
      synchronized (this) {
        if (!minorValid) {
          int minorAxis = getMinorAxis();
          min = child.getMinimumSpan(minorAxis);
          pref = child.getPreferredSpan(minorAxis);
          max = child.getResizeWeight(minorAxis) > 0 ? child.getMaximumSpan(minorAxis) : pref;
          minorValid = true;
          minorUpdated = true;
        }
      }
      if (minorUpdated) {
        minorRequirementChange(this);
      }
      boolean majorUpdated = false;
      float delta = 0;
      synchronized (this) {
        if (!majorValid) {
          float old = majorSpan;
          majorSpan = child.getPreferredSpan(getMajorAxis());
          delta = majorSpan - old;
          majorValid = true;
          majorUpdated = true;
        }
      }
      if (majorUpdated) {
        majorRequirementChange(this, delta);
        locator.childChanged(this);
      }
      synchronized (this) {
        if (!childSizeValid) {
          float w;
          float h;
          if (getMajorAxis() == X_AXIS) {
            w = getMajorSpan();
            h = getMinorSpan();
          } else {
            w = getMinorSpan();
            h = getMajorSpan();
          }
          childSizeValid = true;
          child.setSize(w, h);
        }
      }
    }

    public float getMajorSpan() { return majorSpan; }

    public float getMinorSpan() {
      if (max < AsyncBoxView.this.minorSpan) {
        return max;
      }
      return Math.max(min, AsyncBoxView.this.minorSpan);
    }

    public float getMinorOffset() {
      if (max < AsyncBoxView.this.minorSpan) {
        float align = child.getAlignment(getMinorAxis());
        minorOffset = (AsyncBoxView.this.minorSpan - max) * align;
      } else {
        minorOffset = 0;
      }
      return minorOffset;
    }

    public float getMajorOffset() { return majorOffset; }

    public void setMajorOffset(float offs) { majorOffset = offs; }

    public void preferenceChanged(boolean width, boolean height) {
      if (getMinorAxis() == X_AXIS) {
        if (width) {
          minorValid = false;
        }
        if (height) {
          majorValid = false;
        }
      } else {
        if (width) {
          majorValid = false;
        }
        if (height) {
          minorValid = false;
        }
      }
      childSizeValid = false;
    }

    public boolean isLayoutValid() { return minorValid && majorValid && childSizeValid; }
  }
}
