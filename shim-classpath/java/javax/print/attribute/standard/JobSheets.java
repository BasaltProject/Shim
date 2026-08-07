/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package javax.print.attribute.standard;

public class JobSheets extends javax.print.attribute.EnumSyntax implements javax.print.attribute.PrintRequestAttribute, javax.print.attribute.PrintJobAttribute {
  public static final javax.print.attribute.standard.JobSheets NONE = new javax.print.attribute.standard.JobSheets(0);
  public static final javax.print.attribute.standard.JobSheets STANDARD = new javax.print.attribute.standard.JobSheets(1);
  protected JobSheets(int p0) {
    super(p0);
  }
  public final java.lang.String getName() { return "job-sheets"; }
  public final java.lang.Class<? extends javax.print.attribute.Attribute> getCategory() { return javax.print.attribute.standard.JobSheets.class; }
  protected java.lang.String[] getStringTable() { return myStringTable.clone(); }
  protected javax.print.attribute.EnumSyntax[] getEnumValueTable() { return (javax.print.attribute.EnumSyntax[]) myEnumValueTable.clone(); }

  private static final String[] myStringTable = {"none", "standard"};

  private static final javax.print.attribute.EnumSyntax[] myEnumValueTable = {NONE, STANDARD};
}
