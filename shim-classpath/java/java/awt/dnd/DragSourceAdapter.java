/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package java.awt.dnd;

public abstract class DragSourceAdapter implements java.awt.dnd.DragSourceListener, java.awt.dnd.DragSourceMotionListener {
  protected DragSourceAdapter() {
  }
  public void dragDropEnd(java.awt.dnd.DragSourceDropEvent p0) { }
  public void dragMouseMoved(java.awt.dnd.DragSourceDragEvent p0) { }
  public void dragEnter(java.awt.dnd.DragSourceDragEvent p0) { }
  public void dragOver(java.awt.dnd.DragSourceDragEvent p0) { }
  public void dropActionChanged(java.awt.dnd.DragSourceDragEvent p0) { }
  public void dragExit(java.awt.dnd.DragSourceEvent p0) { }
}
