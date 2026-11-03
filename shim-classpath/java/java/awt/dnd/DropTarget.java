/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */
package java.awt.dnd;

import java.awt.Component;
import java.awt.GraphicsEnvironment;
import java.awt.HeadlessException;
import java.awt.Point;
import java.awt.datatransfer.FlavorMap;
import java.awt.datatransfer.SystemFlavorMap;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.Serializable;
import java.util.TooManyListenersException;

public class DropTarget implements DropTargetListener, Serializable {
  private static final long serialVersionUID = -6283860791671019047L;
  private DropTargetContext dropTargetContext = createDropTargetContext();
  private Component component;
  private transient FlavorMap flavorMap;
  private int actions = DnDConstants.ACTION_COPY_OR_MOVE;
  private boolean active = true;
  private transient DropTargetListener dtListener;
  private transient DropTargetAutoScroller autoScroller;

  public DropTarget(Component c, int ops, DropTargetListener dtl, boolean act, FlavorMap fm)
      throws HeadlessException {
    if (GraphicsEnvironment.isHeadless()) {
      throw new HeadlessException();
    }
    component = c;
    setDefaultActions(ops);
    if (dtl != null) {
      try {
        addDropTargetListener(dtl);
      } catch (TooManyListenersException tmle) {
        // unreachable
      }
    }
    if (c != null) {
      c.setDropTarget(this);
      setActive(act);
    }
    flavorMap = fm != null ? fm : SystemFlavorMap.getDefaultFlavorMap();
  }

  public DropTarget(Component c, int ops, DropTargetListener dtl, boolean act) throws HeadlessException {
    this(c, ops, dtl, act, null);
  }

  public DropTarget() throws HeadlessException {
    this(null, DnDConstants.ACTION_COPY_OR_MOVE, null, true, null);
  }

  public DropTarget(Component c, DropTargetListener dtl) throws HeadlessException {
    this(c, DnDConstants.ACTION_COPY_OR_MOVE, dtl, true, null);
  }

  public DropTarget(Component c, int ops, DropTargetListener dtl) throws HeadlessException {
    this(c, ops, dtl, true);
  }

  public synchronized void setComponent(Component c) {
    if (component == c || (component != null && component.equals(c))) {
      return;
    }
    Component old = component;
    if (old != null) {
      removeNotify();
      old.setDropTarget(null);
    }
    if ((component = c) != null) {
      try {
        c.setDropTarget(this);
      } catch (Exception e) {
        if (old != null) {
          old.setDropTarget(this);
        }
      }
    }
  }

  public synchronized Component getComponent() {
    return component;
  }

  public void setDefaultActions(int ops) {
    actions = ops & (DnDConstants.ACTION_COPY_OR_MOVE | DnDConstants.ACTION_REFERENCE);
  }

  public int getDefaultActions() {
    return actions;
  }

  public synchronized void setActive(boolean isActive) {
    active = isActive;
  }

  public boolean isActive() {
    return active;
  }

  public synchronized void addDropTargetListener(DropTargetListener dtl) throws TooManyListenersException {
    if (dtl == null) {
      return;
    }
    if (equals(dtl)) {
      throw new IllegalArgumentException("DropTarget may not be its own Listener");
    }
    if (dtListener == null) {
      dtListener = dtl;
    } else {
      throw new TooManyListenersException();
    }
  }

  public synchronized void removeDropTargetListener(DropTargetListener dtl) {
    if (dtl != null && dtListener != null) {
      if (dtListener.equals(dtl)) {
        dtListener = null;
      } else {
        throw new IllegalArgumentException("listener mismatch");
      }
    }
  }

  public synchronized void dragEnter(DropTargetDragEvent dtde) {
    if (!active) {
      return;
    }
    if (dtListener != null) {
      dtListener.dragEnter(dtde);
    } else {
      dtde.getDropTargetContext().setTargetActions(DnDConstants.ACTION_NONE);
    }
    initializeAutoscrolling(dtde.getLocation());
  }

  public synchronized void dragOver(DropTargetDragEvent dtde) {
    if (!active) {
      return;
    }
    if (dtListener != null && active) {
      dtListener.dragOver(dtde);
    }
    updateAutoscroll(dtde.getLocation());
  }

  public synchronized void dropActionChanged(DropTargetDragEvent dtde) {
    if (!active) {
      return;
    }
    if (dtListener != null) {
      dtListener.dropActionChanged(dtde);
    }
    updateAutoscroll(dtde.getLocation());
  }

  public synchronized void dragExit(DropTargetEvent dte) {
    if (!active) {
      return;
    }
    if (dtListener != null && active) {
      dtListener.dragExit(dte);
    }
    clearAutoscroll();
  }

  public synchronized void drop(DropTargetDropEvent dtde) {
    clearAutoscroll();
    if (dtListener != null && active) {
      dtListener.drop(dtde);
    } else {
      dtde.rejectDrop();
    }
  }

  public FlavorMap getFlavorMap() {
    return flavorMap;
  }

  public void setFlavorMap(FlavorMap fm) {
    flavorMap = fm == null ? SystemFlavorMap.getDefaultFlavorMap() : fm;
  }

  public void addNotify() {
  }

  public void removeNotify() {
  }

  public DropTargetContext getDropTargetContext() {
    return dropTargetContext;
  }

  protected DropTargetContext createDropTargetContext() {
    return new DropTargetContext(this);
  }

  protected DropTargetAutoScroller createDropTargetAutoScroller(Component c, Point p) {
    return new DropTargetAutoScroller(c, p);
  }

  protected void initializeAutoscrolling(Point p) {
    if (component == null || !(component instanceof java.awt.dnd.Autoscroll)) {
      return;
    }
    autoScroller = createDropTargetAutoScroller(component, p);
  }

  protected void updateAutoscroll(Point dragCursorLocn) {
    if (autoScroller != null) {
      autoScroller.updateLocation(dragCursorLocn);
    }
  }

  protected void clearAutoscroll() {
    if (autoScroller != null) {
      autoScroller.stop();
      autoScroller = null;
    }
  }

  protected static class DropTargetAutoScroller implements ActionListener {
    private final Component component;
    private Point locn;
    private Point prev;

    protected DropTargetAutoScroller(Component c, Point p) {
      component = c;
      locn = p;
      prev = p;
    }

    protected synchronized void updateLocation(Point newLocn) {
      prev = locn;
      locn = new Point(newLocn);
    }

    protected void stop() {
    }

    public synchronized void actionPerformed(ActionEvent e) {
      if (component instanceof java.awt.dnd.Autoscroll && !locn.equals(prev)) {
        ((java.awt.dnd.Autoscroll) component).autoscroll(locn);
      }
      prev = locn;
    }
  }
}
