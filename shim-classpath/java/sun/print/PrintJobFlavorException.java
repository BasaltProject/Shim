/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */
package sun.print;

import javax.print.DocFlavor;
import javax.print.FlavorException;
import javax.print.PrintException;

class PrintJobFlavorException extends PrintException implements FlavorException {
  private final DocFlavor flavor;

  PrintJobFlavorException(String message, DocFlavor flavor) {
    super(message);
    this.flavor = flavor;
  }

  public DocFlavor[] getUnsupportedFlavors() {
    return new DocFlavor[] {flavor};
  }
}
