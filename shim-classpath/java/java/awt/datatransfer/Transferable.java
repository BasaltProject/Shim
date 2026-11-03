/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package java.awt.datatransfer;

public interface Transferable {
  java.awt.datatransfer.DataFlavor[] getTransferDataFlavors();
  boolean isDataFlavorSupported(java.awt.datatransfer.DataFlavor p0);
  java.lang.Object getTransferData(java.awt.datatransfer.DataFlavor p0) throws java.awt.datatransfer.UnsupportedFlavorException, java.io.IOException;
}
