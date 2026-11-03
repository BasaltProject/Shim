/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package javax.print.attribute.standard;

public final class PrinterResolution extends javax.print.attribute.ResolutionSyntax implements javax.print.attribute.DocAttribute, javax.print.attribute.PrintRequestAttribute, javax.print.attribute.PrintJobAttribute {
  public PrinterResolution(int p0, int p1, int p2) {
    super(p0, p1, p2);
  }
  public final java.lang.String getName() { return "printer-resolution"; }
  public boolean equals(java.lang.Object p0) { return super.equals(p0) && p0 instanceof PrinterResolution; }
  public final java.lang.Class<? extends javax.print.attribute.Attribute> getCategory() { return PrinterResolution.class; }
}
