/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package javax.print.attribute.standard;

public final class PrinterIsAcceptingJobs extends javax.print.attribute.EnumSyntax implements javax.print.attribute.PrintServiceAttribute {
  public static final javax.print.attribute.standard.PrinterIsAcceptingJobs NOT_ACCEPTING_JOBS = new javax.print.attribute.standard.PrinterIsAcceptingJobs(0);
  public static final javax.print.attribute.standard.PrinterIsAcceptingJobs ACCEPTING_JOBS = new javax.print.attribute.standard.PrinterIsAcceptingJobs(1);
  protected PrinterIsAcceptingJobs(int p0) {
    super(p0);
  }
  public final java.lang.String getName() { return "printer-is-accepting-jobs"; }
  public final java.lang.Class<? extends javax.print.attribute.Attribute> getCategory() { return javax.print.attribute.standard.PrinterIsAcceptingJobs.class; }
  protected java.lang.String[] getStringTable() { return myStringTable.clone(); }
  protected javax.print.attribute.EnumSyntax[] getEnumValueTable() { return (javax.print.attribute.EnumSyntax[]) myEnumValueTable.clone(); }

  private static final String[] myStringTable = {"not-accepting-jobs", "accepting-jobs"};

  private static final javax.print.attribute.EnumSyntax[] myEnumValueTable = {NOT_ACCEPTING_JOBS, ACCEPTING_JOBS};
}
