/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package javax.print.attribute.standard;

public final class PrinterInfo extends javax.print.attribute.TextSyntax implements javax.print.attribute.PrintServiceAttribute {
  public PrinterInfo(java.lang.String p0, java.util.Locale p1) {
    super(p0, p1);
  }
  public final java.lang.String getName() { return "printer-info"; }
  public boolean equals(java.lang.Object p0) { return super.equals(p0) && p0 instanceof PrinterInfo; }
  public final java.lang.Class<? extends javax.print.attribute.Attribute> getCategory() { return PrinterInfo.class; }
}
