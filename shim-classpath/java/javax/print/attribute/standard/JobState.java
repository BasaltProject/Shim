/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package javax.print.attribute.standard;

public class JobState extends javax.print.attribute.EnumSyntax implements javax.print.attribute.PrintJobAttribute {
  public static final javax.print.attribute.standard.JobState UNKNOWN = new javax.print.attribute.standard.JobState(0);
  public static final javax.print.attribute.standard.JobState PENDING = new javax.print.attribute.standard.JobState(3);
  public static final javax.print.attribute.standard.JobState PENDING_HELD = new javax.print.attribute.standard.JobState(4);
  public static final javax.print.attribute.standard.JobState PROCESSING = new javax.print.attribute.standard.JobState(5);
  public static final javax.print.attribute.standard.JobState PROCESSING_STOPPED = new javax.print.attribute.standard.JobState(6);
  public static final javax.print.attribute.standard.JobState CANCELED = new javax.print.attribute.standard.JobState(7);
  public static final javax.print.attribute.standard.JobState ABORTED = new javax.print.attribute.standard.JobState(8);
  public static final javax.print.attribute.standard.JobState COMPLETED = new javax.print.attribute.standard.JobState(9);
  protected JobState(int p0) {
    super(p0);
  }
  public final java.lang.String getName() { return "job-state"; }
  public final java.lang.Class<? extends javax.print.attribute.Attribute> getCategory() { return javax.print.attribute.standard.JobState.class; }
  protected java.lang.String[] getStringTable() { return myStringTable.clone(); }
  protected javax.print.attribute.EnumSyntax[] getEnumValueTable() { return (javax.print.attribute.EnumSyntax[]) myEnumValueTable.clone(); }

  private static final String[] myStringTable = {"unknown", null, null, "pending", "pending-held", "processing", "processing-stopped", "canceled", "aborted", "completed"};

  private static final javax.print.attribute.EnumSyntax[] myEnumValueTable = {UNKNOWN, null, null, PENDING, PENDING_HELD, PROCESSING, PROCESSING_STOPPED, CANCELED, ABORTED, COMPLETED};
}
