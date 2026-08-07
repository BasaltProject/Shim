/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package javax.print.attribute.standard;

public final class PrinterMessageFromOperator extends javax.print.attribute.TextSyntax implements javax.print.attribute.PrintServiceAttribute {
  public PrinterMessageFromOperator(java.lang.String p0, java.util.Locale p1) {
    super(p0, p1);
  }
  public final java.lang.String getName() { return "printer-message-from-operator"; }
  public boolean equals(java.lang.Object p0) { return super.equals(p0) && p0 instanceof PrinterMessageFromOperator; }
  public final java.lang.Class<? extends javax.print.attribute.Attribute> getCategory() { return PrinterMessageFromOperator.class; }
}
