/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package javax.print.attribute.standard;

public final class JobHoldUntil extends javax.print.attribute.DateTimeSyntax implements javax.print.attribute.PrintRequestAttribute, javax.print.attribute.PrintJobAttribute {
  public JobHoldUntil(java.util.Date p0) {
    super(p0);
  }
  public final java.lang.String getName() { return "job-hold-until"; }
  public boolean equals(java.lang.Object p0) { return super.equals(p0) && p0 instanceof JobHoldUntil; }
  public final java.lang.Class<? extends javax.print.attribute.Attribute> getCategory() { return JobHoldUntil.class; }
}
