/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package javax.print.attribute.standard;

public abstract class Media extends javax.print.attribute.EnumSyntax implements javax.print.attribute.DocAttribute, javax.print.attribute.PrintRequestAttribute, javax.print.attribute.PrintJobAttribute {
  protected Media(int p0) {
    super(p0);
  }
  public final java.lang.String getName() { return "media"; }
  public boolean equals(java.lang.Object p0) { return (p0 != null && p0 instanceof Media && p0.getClass() == this.getClass() && ((Media) p0).getValue() == this.getValue()); }
  public final java.lang.Class<? extends javax.print.attribute.Attribute> getCategory() { return Media.class; }
}
