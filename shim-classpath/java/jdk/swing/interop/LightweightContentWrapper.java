/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package jdk.swing.interop;

public abstract class LightweightContentWrapper {

  public LightweightContentWrapper() {
  }

  public abstract void addDropTarget(java.awt.dnd.DropTarget dropTarget);
  public abstract <T extends java.awt.dnd.DragGestureRecognizer> T createDragGestureRecognizer(java.lang.Class<T> p0, java.awt.dnd.DragSource dragSource, java.awt.Component component, int i, java.awt.dnd.DragGestureListener dragGestureListener);
  public abstract jdk.swing.interop.DragSourceContextWrapper createDragSourceContext(java.awt.dnd.DragGestureEvent dragGestureEvent) throws java.awt.dnd.InvalidDnDOperationException;
  public abstract void focusGrabbed();
  public abstract void focusUngrabbed();
  public abstract javax.swing.JComponent getComponent();
  public abstract void imageBufferReset(int[] is, int i, int i2, int i3, int i4, int i5, double d, double d2);
  public abstract void imageBufferReset(int[] is, int i, int i2, int i3, int i4, int i5);
  public abstract void imageReshaped(int i, int i2, int i3, int i4);
  public abstract void imageUpdated(int i, int i2, int i3, int i4);
  public abstract void maximumSizeChanged(int i, int i2);
  public abstract void minimumSizeChanged(int i, int i2);
  public abstract void paintLock();
  public abstract void paintUnlock();
  public abstract void preferredSizeChanged(int i, int i2);
  public abstract void removeDropTarget(java.awt.dnd.DropTarget dropTarget);
}
