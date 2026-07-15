/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package java.awt.datatransfer;

public class UnsupportedFlavorException extends Exception {
  private static final long serialVersionUID = 5383814944251665601L;

  public UnsupportedFlavorException(DataFlavor flavor) {
    super((flavor != null) ? flavor.getHumanPresentableName() : null);
  }
}
