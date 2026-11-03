/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package javax.print.attribute.standard;

public final class PrinterMoreInfoManufacturer extends javax.print.attribute.URISyntax implements javax.print.attribute.PrintServiceAttribute {
  public PrinterMoreInfoManufacturer(java.net.URI p0) {
    super(p0);
  }
  public final java.lang.String getName() { return "printer-more-info-manufacturer"; }
  public boolean equals(java.lang.Object p0) { return super.equals(p0) && p0 instanceof PrinterMoreInfoManufacturer; }
  public final java.lang.Class<? extends javax.print.attribute.Attribute> getCategory() { return PrinterMoreInfoManufacturer.class; }
}
