/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package javax.print.attribute.standard;

public final class Destination extends javax.print.attribute.URISyntax implements javax.print.attribute.PrintJobAttribute, javax.print.attribute.PrintRequestAttribute {
  public Destination(java.net.URI p0) {
    super(p0);
  }
  public final java.lang.String getName() { return "spool-data-destination"; }
  public boolean equals(java.lang.Object p0) { return super.equals(p0) && p0 instanceof Destination; }
  public final java.lang.Class<? extends javax.print.attribute.Attribute> getCategory() { return Destination.class; }
}
