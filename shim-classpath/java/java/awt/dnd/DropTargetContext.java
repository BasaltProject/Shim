/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */
package java.awt.dnd;

import java.awt.Component;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.io.IOException;
import java.io.Serializable;
import java.util.Arrays;
import java.util.List;

public final class DropTargetContext implements Serializable {
  private static final long serialVersionUID = -634158968993743371L;
  private final DropTarget dropTarget;
  private transient int targetActions = DnDConstants.ACTION_NONE;
  private transient Transferable transferable;

  DropTargetContext(DropTarget dt) {
    super();
    dropTarget = dt;
  }

  public DropTarget getDropTarget() {
    return dropTarget;
  }

  public Component getComponent() {
    return dropTarget.getComponent();
  }

  protected void setTargetActions(int actions) {
    targetActions = actions;
  }

  protected int getTargetActions() {
    return targetActions;
  }

  public void dropComplete(boolean success) throws InvalidDnDOperationException {
    throw new InvalidDnDOperationException("No drop current");
  }

  protected void acceptDrag(int dragOperation) {
    targetActions = dragOperation;
  }

  protected void rejectDrag() {
    targetActions = DnDConstants.ACTION_NONE;
  }

  protected void acceptDrop(int dropOperation) {
    targetActions = dropOperation;
  }

  protected void rejectDrop() {
    targetActions = DnDConstants.ACTION_NONE;
  }

  protected DataFlavor[] getCurrentDataFlavors() {
    return transferable == null ? new DataFlavor[0] : transferable.getTransferDataFlavors();
  }

  protected List<DataFlavor> getCurrentDataFlavorsAsList() {
    return Arrays.asList(getCurrentDataFlavors());
  }

  protected boolean isDataFlavorSupported(DataFlavor df) {
    return getCurrentDataFlavorsAsList().contains(df);
  }

  protected Transferable getTransferable() throws InvalidDnDOperationException {
    if (transferable == null) {
      throw new InvalidDnDOperationException();
    }
    return transferable;
  }

  protected Transferable createTransferableProxy(Transferable t, boolean local) {
    return new TransferableProxy(t, local);
  }

  protected class TransferableProxy implements Transferable {
    protected Transferable transferable;
    protected boolean isLocal;

    TransferableProxy(Transferable t, boolean local) {
      transferable = t;
      isLocal = local;
    }

    public DataFlavor[] getTransferDataFlavors() {
      return transferable.getTransferDataFlavors();
    }

    public boolean isDataFlavorSupported(DataFlavor flavor) {
      return transferable.isDataFlavorSupported(flavor);
    }

    public Object getTransferData(DataFlavor df) throws UnsupportedFlavorException, IOException {
      return transferable.getTransferData(df);
    }
  }
}
