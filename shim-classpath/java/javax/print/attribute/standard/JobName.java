/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package javax.print.attribute.standard;

public final class JobName extends javax.print.attribute.TextSyntax implements javax.print.attribute.PrintRequestAttribute, javax.print.attribute.PrintJobAttribute {
  public JobName(java.lang.String p0, java.util.Locale p1) {
    super(p0, p1);
  }
  public final java.lang.String getName() { return "job-name"; }
  public boolean equals(java.lang.Object p0) { return super.equals(p0) && p0 instanceof JobName; }
  public final java.lang.Class<? extends javax.print.attribute.Attribute> getCategory() { return JobName.class; }
}
