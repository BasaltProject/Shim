/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package javax.print.attribute.standard;

public final class PrinterMoreInfo extends javax.print.attribute.URISyntax implements javax.print.attribute.PrintServiceAttribute {
  public PrinterMoreInfo(java.net.URI p0) {
    super(p0);
  }
  public final java.lang.String getName() { return "printer-more-info"; }
  public boolean equals(java.lang.Object p0) { return super.equals(p0) && p0 instanceof PrinterMoreInfo; }
  public final java.lang.Class<? extends javax.print.attribute.Attribute> getCategory() { return PrinterMoreInfo.class; }
}
