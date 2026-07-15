/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */
package java.awt.dnd;

import java.awt.Component;
import java.awt.Cursor;
import java.awt.Image;
import java.awt.Point;
import java.awt.datatransfer.Transferable;
import java.awt.event.InputEvent;
import java.util.ArrayList;
import java.util.Collections;
import java.util.EventObject;
import java.util.Iterator;
import java.util.List;

public class DragGestureEvent extends EventObject {
  private static final long serialVersionUID = 9080172649166731306L;
  private final Component component;
  private final Point origin;
  private final int action;
  private List<InputEvent> events;

  public DragGestureEvent(DragGestureRecognizer dgr, int act, Point ori, List<? extends InputEvent> evs) {
    super(dgr);
    if ((component = dgr.getComponent()) == null) {
      throw new IllegalArgumentException("null component");
    }
    if (dgr.getDragSource() == null) {
      throw new IllegalArgumentException("null DragSource");
    }
    if (evs == null || evs.isEmpty()) {
      throw new IllegalArgumentException("null or empty list of events");
    }
    if (act != DnDConstants.ACTION_COPY && act != DnDConstants.ACTION_MOVE && act != DnDConstants.ACTION_LINK) {
      throw new IllegalArgumentException("bad action");
    }
    if (ori == null) {
      throw new IllegalArgumentException("null origin");
    }
    events = new ArrayList<InputEvent>(evs);
    action = act;
    origin = ori;
  }

  public DragGestureRecognizer getSourceAsDragGestureRecognizer() {
    return (DragGestureRecognizer) getSource();
  }

  public Component getComponent() {
    return component;
  }

  public DragSource getDragSource() {
    return ((DragGestureRecognizer) getSource()).getDragSource();
  }

  public Point getDragOrigin() {
    return origin;
  }

  @SuppressWarnings("unchecked")
  public Iterator<InputEvent> iterator() {
    return events.iterator();
  }

  public Object[] toArray() {
    return events.toArray();
  }

  public Object[] toArray(Object[] array) {
    return events.toArray(array);
  }

  public int getDragAction() {
    return action;
  }

  public InputEvent getTriggerEvent() {
    return getSourceAsDragGestureRecognizer().getTriggerEvent();
  }

  public void startDrag(Cursor dragCursor, Transferable transferable) throws InvalidDnDOperationException {
    getDragSource().startDrag(this, dragCursor, transferable, null);
  }

  public void startDrag(Cursor dragCursor, Transferable transferable, DragSourceListener dsl)
      throws InvalidDnDOperationException {
    getDragSource().startDrag(this, dragCursor, transferable, dsl);
  }

  public void startDrag(Cursor dragCursor, Image dragImage, Point imageOffset, Transferable transferable,
      DragSourceListener dsl) throws InvalidDnDOperationException {
    getDragSource().startDrag(this, dragCursor, dragImage, imageOffset, transferable, dsl);
  }
}
