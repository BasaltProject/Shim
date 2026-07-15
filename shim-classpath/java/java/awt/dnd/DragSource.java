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
import java.awt.GraphicsEnvironment;
import java.awt.HeadlessException;
import java.awt.Image;
import java.awt.Point;
import java.awt.datatransfer.FlavorMap;
import java.awt.datatransfer.SystemFlavorMap;
import java.awt.datatransfer.Transferable;
import java.io.Serializable;
import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.EventListener;
import java.util.List;

public class DragSource implements Serializable {
  private static final long serialVersionUID = 6236096958971414066L;
  public static final Cursor DefaultCopyDrop = null;
  public static final Cursor DefaultMoveDrop = null;
  public static final Cursor DefaultLinkDrop = null;
  public static final Cursor DefaultCopyNoDrop = null;
  public static final Cursor DefaultMoveNoDrop = null;
  public static final Cursor DefaultLinkNoDrop = null;
  private static DragSource kDefaultDragSource;
  private transient FlavorMap flavorMap = SystemFlavorMap.getDefaultFlavorMap();
  private final List<DragSourceListener> listeners = new ArrayList<DragSourceListener>();
  private final List<DragSourceMotionListener> motionListeners = new ArrayList<DragSourceMotionListener>();

  public static DragSource getDefaultDragSource() {
    if (GraphicsEnvironment.isHeadless()) {
      throw new HeadlessException();
    }
    synchronized (DragSource.class) {
      if (kDefaultDragSource == null) {
        kDefaultDragSource = new DragSource();
      }
      return kDefaultDragSource;
    }
  }

  public static boolean isDragImageSupported() {
    return false;
  }

  public DragSource() throws HeadlessException {
    if (GraphicsEnvironment.isHeadless()) {
      throw new HeadlessException();
    }
  }

  public void startDrag(DragGestureEvent trigger, Cursor dragCursor, Image dragImage, Point imageOffset,
      Transferable transferable, DragSourceListener dsl, FlavorMap flavorMap) throws InvalidDnDOperationException {
    throw new InvalidDnDOperationException("Drag and drop is not supported by this peer");
  }

  public void startDrag(DragGestureEvent trigger, Cursor dragCursor, Transferable transferable,
      DragSourceListener dsl, FlavorMap flavorMap) throws InvalidDnDOperationException {
    startDrag(trigger, dragCursor, null, null, transferable, dsl, flavorMap);
  }

  public void startDrag(DragGestureEvent trigger, Cursor dragCursor, Image dragImage, Point dragOffset,
      Transferable transferable, DragSourceListener dsl) throws InvalidDnDOperationException {
    startDrag(trigger, dragCursor, dragImage, dragOffset, transferable, dsl, flavorMap);
  }

  public void startDrag(DragGestureEvent trigger, Cursor dragCursor, Transferable transferable,
      DragSourceListener dsl) throws InvalidDnDOperationException {
    startDrag(trigger, dragCursor, null, null, transferable, dsl, flavorMap);
  }

  protected DragSourceContext createDragSourceContext(DragGestureEvent dgl, Cursor dragCursor, Image dragImage,
      Point imageOffset, Transferable t, DragSourceListener dsl) {
    return new DragSourceContext(dgl, dragCursor, dragImage, imageOffset, t, dsl);
  }

  public FlavorMap getFlavorMap() {
    return flavorMap;
  }

  public <T extends DragGestureRecognizer> T createDragGestureRecognizer(Class<T> recognizerAbstractClass,
      Component c, int actions, DragGestureListener dgl) {
    if (recognizerAbstractClass.isAssignableFrom(MouseDragGestureRecognizer.class)) {
      return recognizerAbstractClass.cast(new SimpleMouseRecognizer(this, c, actions, dgl));
    }
    return null;
  }

  public DragGestureRecognizer createDefaultDragGestureRecognizer(Component c, int actions,
      DragGestureListener dgl) {
    return createDragGestureRecognizer(MouseDragGestureRecognizer.class, c, actions, dgl);
  }

  public void addDragSourceListener(DragSourceListener dsl) {
    if (dsl != null) {
      synchronized (this) {
        listeners.add(dsl);
      }
    }
  }

  public void removeDragSourceListener(DragSourceListener dsl) {
    if (dsl != null) {
      synchronized (this) {
        listeners.remove(dsl);
      }
    }
  }

  public DragSourceListener[] getDragSourceListeners() {
    return getListeners(DragSourceListener.class);
  }

  public void addDragSourceMotionListener(DragSourceMotionListener dsml) {
    if (dsml != null) {
      synchronized (this) {
        motionListeners.add(dsml);
      }
    }
  }

  public void removeDragSourceMotionListener(DragSourceMotionListener dsml) {
    if (dsml != null) {
      synchronized (this) {
        motionListeners.remove(dsml);
      }
    }
  }

  public DragSourceMotionListener[] getDragSourceMotionListeners() {
    return getListeners(DragSourceMotionListener.class);
  }

  @SuppressWarnings("unchecked")
  public synchronized <T extends EventListener> T[] getListeners(Class<T> listenerType) {
    List<? extends EventListener> src = listenerType == DragSourceListener.class ? listeners
        : listenerType == DragSourceMotionListener.class ? motionListeners : null;
    if (src == null) {
      return (T[]) java.lang.reflect.Array.newInstance(listenerType, 0);
    }
    T[] out = (T[]) java.lang.reflect.Array.newInstance(listenerType, src.size());
    for (int i = 0; i < out.length; i++) {
      out[i] = listenerType.cast(src.get(i));
    }
    return out;
  }

  void processDragEnter(DragSourceDragEvent dsde) {
    for (DragSourceListener l : getDragSourceListeners()) {
      l.dragEnter(dsde);
    }
  }

  void processDragOver(DragSourceDragEvent dsde) {
    for (DragSourceListener l : getDragSourceListeners()) {
      l.dragOver(dsde);
    }
  }

  void processDropActionChanged(DragSourceDragEvent dsde) {
    for (DragSourceListener l : getDragSourceListeners()) {
      l.dropActionChanged(dsde);
    }
  }

  void processDragExit(DragSourceEvent dse) {
    for (DragSourceListener l : getDragSourceListeners()) {
      l.dragExit(dse);
    }
  }

  void processDragDropEnd(DragSourceDropEvent dsde) {
    for (DragSourceListener l : getDragSourceListeners()) {
      l.dragDropEnd(dsde);
    }
  }

  void processDragMouseMoved(DragSourceDragEvent dsde) {
    for (DragSourceMotionListener l : getDragSourceMotionListeners()) {
      l.dragMouseMoved(dsde);
    }
  }

  public static int getDragThreshold() {
    String prop = System.getProperty("awt.dnd.drag.threshold");
    if (prop != null) {
      try {
        int value = Integer.parseInt(prop);
        if (value > 0) {
          return value;
        }
      } catch (NumberFormatException e) {
        // fall through
      }
    }
    return 8;
  }

  private static final class SimpleMouseRecognizer extends MouseDragGestureRecognizer {
    private static final long serialVersionUID = 1L;
    private int motionThreshold = getDragThreshold();
    private int pressedModifiers;
    private Point pressedPoint;

    SimpleMouseRecognizer(DragSource ds, Component c, int act, DragGestureListener dgl) {
      super(ds, c, act, dgl);
    }

    public void mousePressed(java.awt.event.MouseEvent e) {
      events.clear();
      if (mapAction(e.getModifiersEx()) != DnDConstants.ACTION_NONE) {
        pressedPoint = e.getPoint();
        pressedModifiers = e.getModifiersEx();
        appendEvent(e);
      }
    }

    public void mouseReleased(java.awt.event.MouseEvent e) {
      events.clear();
    }

    public void mouseDragged(java.awt.event.MouseEvent e) {
      if (!events.isEmpty()) {
        int dop = mapAction(e.getModifiersEx());
        if (dop == DnDConstants.ACTION_NONE) {
          return;
        }
        java.awt.event.MouseEvent trigger = (java.awt.event.MouseEvent) events.get(0);
        Point origin = trigger.getPoint();
        Point current = e.getPoint();
        int dx = Math.abs(origin.x - current.x);
        int dy = Math.abs(origin.y - current.y);
        if (dx > motionThreshold || dy > motionThreshold) {
          fireDragGestureRecognized(dop, ((java.awt.event.MouseEvent) getTriggerEvent()).getPoint());
        } else {
          appendEvent(e);
        }
      }
    }

    private int mapAction(int modifiersEx) {
      int mask = java.awt.event.InputEvent.BUTTON1_DOWN_MASK | java.awt.event.InputEvent.BUTTON2_DOWN_MASK
          | java.awt.event.InputEvent.BUTTON3_DOWN_MASK;
      if ((modifiersEx & mask) == 0) {
        return DnDConstants.ACTION_NONE;
      }
      boolean shift = (modifiersEx & java.awt.event.InputEvent.SHIFT_DOWN_MASK) != 0;
      boolean ctrl = (modifiersEx & java.awt.event.InputEvent.CTRL_DOWN_MASK) != 0;
      int action;
      if (shift && ctrl) {
        action = DnDConstants.ACTION_LINK;
      } else if (ctrl) {
        action = DnDConstants.ACTION_COPY;
      } else if (shift) {
        action = DnDConstants.ACTION_MOVE;
      } else {
        action = (getSourceActions() & DnDConstants.ACTION_MOVE) != 0 ? DnDConstants.ACTION_MOVE
            : (getSourceActions() & DnDConstants.ACTION_COPY) != 0 ? DnDConstants.ACTION_COPY
            : (getSourceActions() & DnDConstants.ACTION_LINK) != 0 ? DnDConstants.ACTION_LINK
            : DnDConstants.ACTION_NONE;
      }
      return (action & getSourceActions()) != 0 ? action : DnDConstants.ACTION_NONE;
    }
  }
}
