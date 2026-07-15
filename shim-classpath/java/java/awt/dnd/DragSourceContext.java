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
import java.io.Serializable;
import java.util.TooManyListenersException;

public class DragSourceContext implements DragSourceListener, DragSourceMotionListener, Serializable {
  private static final long serialVersionUID = -115407898692194719L;
  protected static final int DEFAULT = 0;
  protected static final int ENTER = 1;
  protected static final int OVER = 2;
  protected static final int CHANGED = 3;
  private transient DragSourceListener listener;
  private Cursor cursor;
  private transient Transferable transferable;
  private transient DragGestureEvent trigger;
  private transient boolean useCustomCursor;
  private final int sourceActions;

  public DragSourceContext(DragGestureEvent trigger, Cursor dragCursor, Image dragImage, Point offset,
      Transferable t, DragSourceListener dsl) {
    if (trigger == null) {
      throw new NullPointerException("Trigger can't be null");
    }
    if (trigger.getDragSource() == null) {
      throw new IllegalArgumentException("DragSource");
    }
    if (trigger.getComponent() == null) {
      throw new IllegalArgumentException("Component");
    }
    if (trigger.getSourceAsDragGestureRecognizer().getSourceActions() == DnDConstants.ACTION_NONE) {
      throw new IllegalArgumentException("source actions");
    }
    if (trigger.getDragAction() == DnDConstants.ACTION_NONE) {
      throw new IllegalArgumentException("no drag action");
    }
    if (t == null) {
      throw new NullPointerException("Transferable");
    }
    if (dragImage != null && offset == null) {
      throw new NullPointerException("offset");
    }
    this.trigger = trigger;
    this.cursor = dragCursor;
    this.transferable = t;
    this.listener = dsl;
    this.sourceActions = trigger.getSourceAsDragGestureRecognizer().getSourceActions();
    useCustomCursor = dragCursor != null;
    updateCurrentCursor(trigger.getDragAction(), sourceActions, DEFAULT);
  }

  public DragSource getDragSource() {
    return trigger.getDragSource();
  }

  public Component getComponent() {
    return trigger.getComponent();
  }

  public DragGestureEvent getTrigger() {
    return trigger;
  }

  public int getSourceActions() {
    return sourceActions;
  }

  public synchronized void setCursor(Cursor c) {
    useCustomCursor = c != null;
    setCursorImpl(c);
  }

  public Cursor getCursor() {
    return cursor;
  }

  public synchronized void addDragSourceListener(DragSourceListener dsl) throws TooManyListenersException {
    if (dsl == null) {
      return;
    }
    if (equals(dsl)) {
      throw new IllegalArgumentException("DragSourceContext may not be its own listener");
    }
    if (listener != null) {
      throw new TooManyListenersException();
    }
    listener = dsl;
  }

  public synchronized void removeDragSourceListener(DragSourceListener dsl) {
    if (listener != null && listener.equals(dsl)) {
      listener = null;
    } else {
      throw new IllegalArgumentException();
    }
  }

  public void transferablesFlavorsChanged() {
  }

  public void dragEnter(DragSourceDragEvent dsde) {
    DragSourceListener dsl = listener;
    if (dsl != null) {
      dsl.dragEnter(dsde);
    }
    getDragSource().processDragEnter(dsde);
    updateCurrentCursor(getSourceActions(), dsde.getTargetActions(), ENTER);
  }

  public void dragOver(DragSourceDragEvent dsde) {
    DragSourceListener dsl = listener;
    if (dsl != null) {
      dsl.dragOver(dsde);
    }
    getDragSource().processDragOver(dsde);
    updateCurrentCursor(getSourceActions(), dsde.getTargetActions(), OVER);
  }

  public void dragExit(DragSourceEvent dse) {
    DragSourceListener dsl = listener;
    if (dsl != null) {
      dsl.dragExit(dse);
    }
    getDragSource().processDragExit(dse);
    updateCurrentCursor(DnDConstants.ACTION_NONE, DnDConstants.ACTION_NONE, DEFAULT);
  }

  public void dropActionChanged(DragSourceDragEvent dsde) {
    DragSourceListener dsl = listener;
    if (dsl != null) {
      dsl.dropActionChanged(dsde);
    }
    getDragSource().processDropActionChanged(dsde);
    updateCurrentCursor(getSourceActions(), dsde.getTargetActions(), CHANGED);
  }

  public void dragDropEnd(DragSourceDropEvent dsde) {
    DragSourceListener dsl = listener;
    if (dsl != null) {
      dsl.dragDropEnd(dsde);
    }
    getDragSource().processDragDropEnd(dsde);
  }

  public void dragMouseMoved(DragSourceDragEvent dsde) {
    getDragSource().processDragMouseMoved(dsde);
  }

  public Transferable getTransferable() {
    return transferable;
  }

  private void setCursorImpl(Cursor c) {
    if (cursor == null || !cursor.equals(c)) {
      cursor = c;
    }
  }

  protected synchronized void updateCurrentCursor(int sourceAct, int targetAct, int status) {
    if (useCustomCursor) {
      return;
    }
    if (status != DEFAULT && status != ENTER && status != OVER && status != CHANGED) {
      throw new RuntimeException("invalid status");
    }
    int ra = sourceAct & targetAct;
    Cursor c = null;
    if (ra == DnDConstants.ACTION_NONE) {
      if ((sourceAct & DnDConstants.ACTION_LINK) == DnDConstants.ACTION_LINK) {
        c = DragSource.DefaultLinkNoDrop;
      } else if ((sourceAct & DnDConstants.ACTION_MOVE) == DnDConstants.ACTION_MOVE) {
        c = DragSource.DefaultMoveNoDrop;
      } else {
        c = DragSource.DefaultCopyNoDrop;
      }
    } else if ((ra & DnDConstants.ACTION_LINK) == DnDConstants.ACTION_LINK) {
      c = DragSource.DefaultLinkDrop;
    } else if ((ra & DnDConstants.ACTION_MOVE) == DnDConstants.ACTION_MOVE) {
      c = DragSource.DefaultMoveDrop;
    } else {
      c = DragSource.DefaultCopyDrop;
    }
    setCursorImpl(c);
  }
}
