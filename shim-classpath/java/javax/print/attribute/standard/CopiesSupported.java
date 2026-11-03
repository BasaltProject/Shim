/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package javax.print.attribute.standard;

public final class CopiesSupported extends javax.print.attribute.SetOfIntegerSyntax implements javax.print.attribute.SupportedValuesAttribute {
  public CopiesSupported(int p0) {
    super(p0);
  }
  public CopiesSupported(int p0, int p1) {
    super(p0, p1);
  }
  public final java.lang.String getName() { return "copies-supported"; }
  public boolean equals(java.lang.Object p0) { return super.equals(p0) && p0 instanceof CopiesSupported; }
  public final java.lang.Class<? extends javax.print.attribute.Attribute> getCategory() { return CopiesSupported.class; }
}
