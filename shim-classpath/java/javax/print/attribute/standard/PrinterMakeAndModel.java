/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package javax.print.attribute.standard;

public final class PrinterMakeAndModel extends javax.print.attribute.TextSyntax implements javax.print.attribute.PrintServiceAttribute {
  public PrinterMakeAndModel(java.lang.String p0, java.util.Locale p1) {
    super(p0, p1);
  }
  public final java.lang.String getName() { return "printer-make-and-model"; }
  public boolean equals(java.lang.Object p0) { return super.equals(p0) && p0 instanceof PrinterMakeAndModel; }
  public final java.lang.Class<? extends javax.print.attribute.Attribute> getCategory() { return PrinterMakeAndModel.class; }
}
