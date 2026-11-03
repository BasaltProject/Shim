/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */
package java.awt.dnd;

import java.awt.Point;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.util.List;

public class DropTargetDropEvent extends DropTargetEvent {
  private static final long serialVersionUID = -1721911170440459322L;
  private static final Point kOrigin = new Point(0, 0);
  private final Point location;
  private final int actions;
  private final int dropAction;
  private final boolean isLocalTx;

  public DropTargetDropEvent(DropTargetContext dtc, Point cursorLocn, int dropAction, int srcActions) {
    super(dtc);
    if (cursorLocn == null) {
      throw new NullPointerException("cursorLocn");
    }
    check(dropAction, srcActions);
    location = cursorLocn;
    actions = srcActions;
    this.dropAction = dropAction;
    isLocalTx = false;
  }

  public DropTargetDropEvent(DropTargetContext dtc, Point cursorLocn, int dropAction, int srcActions,
      boolean isLocal) {
    super(dtc);
    if (cursorLocn == null) {
      throw new NullPointerException("cursorLocn");
    }
    check(dropAction, srcActions);
    location = cursorLocn;
    actions = srcActions;
    this.dropAction = dropAction;
    isLocalTx = isLocal;
  }

  private static void check(int dropAction, int srcActions) {
    if (dropAction != DnDConstants.ACTION_NONE && dropAction != DnDConstants.ACTION_COPY
        && dropAction != DnDConstants.ACTION_MOVE && dropAction != DnDConstants.ACTION_COPY_OR_MOVE
        && dropAction != DnDConstants.ACTION_LINK) {
      throw new IllegalArgumentException("dropAction = " + dropAction);
    }
    if ((srcActions & ~(DnDConstants.ACTION_COPY_OR_MOVE | DnDConstants.ACTION_LINK)) != 0) {
      throw new IllegalArgumentException("srcActions");
    }
  }

  public Point getLocation() {
    return location;
  }

  public DataFlavor[] getCurrentDataFlavors() {
    return getDropTargetContext().getCurrentDataFlavors();
  }

  public List<DataFlavor> getCurrentDataFlavorsAsList() {
    return getDropTargetContext().getCurrentDataFlavorsAsList();
  }

  public boolean isDataFlavorSupported(DataFlavor df) {
    return getDropTargetContext().isDataFlavorSupported(df);
  }

  public int getSourceActions() {
    return actions;
  }

  public int getDropAction() {
    return dropAction;
  }

  public Transferable getTransferable() {
    return getDropTargetContext().getTransferable();
  }

  public void acceptDrop(int dropAction) {
    getDropTargetContext().acceptDrop(dropAction);
  }

  public void rejectDrop() {
    getDropTargetContext().rejectDrop();
  }

  public void dropComplete(boolean success) {
    getDropTargetContext().dropComplete(success);
  }

  public boolean isLocalTransfer() {
    return isLocalTx;
  }
}
