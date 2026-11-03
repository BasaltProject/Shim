/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package jdk.swing.interop;

public abstract class DropTargetContextWrapper {

  public DropTargetContextWrapper() {
  }

  public abstract void acceptDrag(int i);
  public abstract void acceptDrop(int i);
  public abstract void dropComplete(boolean flag);
  public abstract java.awt.dnd.DropTarget getDropTarget();
  public abstract int getTargetActions();
  public abstract java.awt.datatransfer.DataFlavor[] getTransferDataFlavors();
  public abstract java.awt.datatransfer.Transferable getTransferable() throws java.awt.dnd.InvalidDnDOperationException;
  public abstract boolean isTransferableJVMLocal();
  public abstract void rejectDrag();
  public abstract void rejectDrop();
  public void reset(java.awt.dnd.DropTargetContext dropTargetContext) {
    throw new UnsupportedOperationException();
  }
  public void setDropTargetContext(java.awt.dnd.DropTargetContext dropTargetContext, jdk.swing.interop.DropTargetContextWrapper dropTargetContextWrapper) {
    throw new UnsupportedOperationException();
  }
  public abstract void setTargetActions(int i);
}
