/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package javax.print.attribute.standard;

public final class NumberUpSupported extends javax.print.attribute.SetOfIntegerSyntax implements javax.print.attribute.SupportedValuesAttribute {
  public NumberUpSupported(int[][] p0) {
    super(p0);
  }
  public NumberUpSupported(int p0, int p1) {
    super(p0, p1);
  }
  public NumberUpSupported(int p0) {
    super(p0);
  }
  public final java.lang.String getName() { return "number-up-supported"; }
  public boolean equals(java.lang.Object p0) { return super.equals(p0) && p0 instanceof NumberUpSupported; }
  public final java.lang.Class<? extends javax.print.attribute.Attribute> getCategory() { return NumberUpSupported.class; }
}
