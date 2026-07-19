/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package jdk.swing.interop;

public abstract class DragSourceContextWrapper {

  public DragSourceContextWrapper(java.awt.dnd.DragGestureEvent dragGestureEvent) {
  }

  public static int convertModifiersToDropAction(int i, int i2) {
    throw new UnsupportedOperationException();
  }
  public void dragDropFinished(boolean flag, int i, int i2, int i3) {
    throw new UnsupportedOperationException();
  }
  public java.awt.dnd.DragSourceContext getDragSourceContext() {
    throw new UnsupportedOperationException();
  }
  public abstract void quitSecondaryEventLoop();
  protected abstract void setNativeCursor(java.awt.Cursor cursor, int i);
  protected abstract void startDrag(java.awt.datatransfer.Transferable transferable, long[] ls, java.util.Map<java.lang.Long, java.awt.datatransfer.DataFlavor> map);
  public abstract void startSecondaryEventLoop();
}
