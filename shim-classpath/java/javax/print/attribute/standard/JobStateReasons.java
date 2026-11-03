/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package javax.print.attribute.standard;

public final class JobStateReasons extends java.util.HashSet<javax.print.attribute.standard.JobStateReason> implements javax.print.attribute.PrintJobAttribute {
  public JobStateReasons() {
  }
  public JobStateReasons(java.util.Collection<javax.print.attribute.standard.JobStateReason> p0) {
    super(p0);
  }
  public JobStateReasons(int p0, float p1) {
    super(p0, p1);
  }
  public JobStateReasons(int p0) {
    super(p0);
  }
  public final java.lang.String getName() { return "job-state-reasons"; }
  public boolean add(javax.print.attribute.standard.JobStateReason p0) { if (p0 == null) {
    throw new NullPointerException();
  }
  return super.add(p0); }
  public final java.lang.Class<? extends javax.print.attribute.Attribute> getCategory() { return JobStateReasons.class; }
}
