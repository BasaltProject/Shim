/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package javax.print.attribute.standard;

public final class JobImpressions extends javax.print.attribute.IntegerSyntax implements javax.print.attribute.PrintRequestAttribute, javax.print.attribute.PrintJobAttribute {
  public JobImpressions(int p0) {
    super(p0, 0, Integer.MAX_VALUE);
  }
  public final java.lang.String getName() { return "job-impressions"; }
  public boolean equals(java.lang.Object p0) { return super.equals(p0) && p0 instanceof JobImpressions; }
  public final java.lang.Class<? extends javax.print.attribute.Attribute> getCategory() { return JobImpressions.class; }
}
