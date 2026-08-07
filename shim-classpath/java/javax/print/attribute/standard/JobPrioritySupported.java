/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package javax.print.attribute.standard;

public final class JobPrioritySupported extends javax.print.attribute.IntegerSyntax implements javax.print.attribute.SupportedValuesAttribute {
  public JobPrioritySupported(int p0) {
    super(p0, 1, 100);
  }
  public final java.lang.String getName() { return "job-priority-supported"; }
  public boolean equals(java.lang.Object p0) { return super.equals(p0) && p0 instanceof JobPrioritySupported; }
  public final java.lang.Class<? extends javax.print.attribute.Attribute> getCategory() { return JobPrioritySupported.class; }
}
