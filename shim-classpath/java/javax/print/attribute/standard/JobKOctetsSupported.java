/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package javax.print.attribute.standard;

public final class JobKOctetsSupported extends javax.print.attribute.SetOfIntegerSyntax implements javax.print.attribute.SupportedValuesAttribute {
  public JobKOctetsSupported(int p0, int p1) {
    super(p0, p1);
  }
  public final java.lang.String getName() { return "job-k-octets-supported"; }
  public boolean equals(java.lang.Object p0) { return super.equals(p0) && p0 instanceof JobKOctetsSupported; }
  public final java.lang.Class<? extends javax.print.attribute.Attribute> getCategory() { return JobKOctetsSupported.class; }
}
