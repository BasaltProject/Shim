/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */
package java.awt.dnd;

import java.awt.Component;
import java.awt.Point;
import java.awt.event.InputEvent;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.TooManyListenersException;

public abstract class DragGestureRecognizer implements Serializable {
  private static final long serialVersionUID = 8996673345831063337L;
  protected DragSource dragSource;
  protected Component component;
  protected transient DragGestureListener dragGestureListener;
  protected int sourceActions;
  protected ArrayList<InputEvent> events = new ArrayList<InputEvent>(1);

  protected DragGestureRecognizer(DragSource ds, Component c, int sa, DragGestureListener dgl) {
    super();
    if (ds == null) {
      throw new IllegalArgumentException("null DragSource");
    }
    dragSource = ds;
    component = c;
    sourceActions = sa & (DnDConstants.ACTION_COPY_OR_MOVE | DnDConstants.ACTION_LINK);
    try {
      if (dgl != null) {
        addDragGestureListener(dgl);
      }
    } catch (TooManyListenersException tmle) {
      // single listener only
    }
  }

  protected DragGestureRecognizer(DragSource ds, Component c, int sa) {
    this(ds, c, sa, null);
  }

  protected DragGestureRecognizer(DragSource ds, Component c) {
    this(ds, c, DnDConstants.ACTION_NONE);
  }

  protected DragGestureRecognizer(DragSource ds) {
    this(ds, null);
  }

  protected abstract void registerListeners();

  protected abstract void unregisterListeners();

  public DragSource getDragSource() {
    return dragSource;
  }

  public synchronized Component getComponent() {
    return component;
  }

  public synchronized void setComponent(Component c) {
    if (component != null && dragGestureListener != null) {
      unregisterListeners();
    }
    component = c;
    if (component != null && dragGestureListener != null) {
      registerListeners();
    }
  }

  public synchronized int getSourceActions() {
    return sourceActions;
  }

  public synchronized void setSourceActions(int actions) {
    sourceActions = actions & (DnDConstants.ACTION_COPY_OR_MOVE | DnDConstants.ACTION_LINK);
  }

  public InputEvent getTriggerEvent() {
    return events.isEmpty() ? null : events.get(0);
  }

  public void resetRecognizer() {
    events.clear();
  }

  public synchronized void addDragGestureListener(DragGestureListener dgl) throws TooManyListenersException {
    if (dragGestureListener != null) {
      throw new TooManyListenersException();
    }
    dragGestureListener = dgl;
    if (component != null) {
      registerListeners();
    }
  }

  public synchronized void removeDragGestureListener(DragGestureListener dgl) {
    if (dragGestureListener == null || !dragGestureListener.equals(dgl)) {
      throw new IllegalArgumentException();
    }
    dragGestureListener = null;
    if (component != null) {
      unregisterListeners();
    }
  }

  protected synchronized void fireDragGestureRecognized(int dragAction, Point p) {
    try {
      if (dragGestureListener != null) {
        dragGestureListener.dragGestureRecognized(new DragGestureEvent(this, dragAction, p, events));
      }
    } finally {
      events.clear();
    }
  }

  protected synchronized void appendEvent(InputEvent awtie) {
    events.add(awtie);
  }
}
