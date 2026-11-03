/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package javax.print.attribute.standard;

public final class Severity extends javax.print.attribute.EnumSyntax implements javax.print.attribute.Attribute {
  public static final javax.print.attribute.standard.Severity REPORT = new javax.print.attribute.standard.Severity(0);
  public static final javax.print.attribute.standard.Severity WARNING = new javax.print.attribute.standard.Severity(1);
  public static final javax.print.attribute.standard.Severity ERROR = new javax.print.attribute.standard.Severity(2);
  protected Severity(int p0) {
    super(p0);
  }
  public final java.lang.String getName() { return "severity"; }
  public final java.lang.Class<? extends javax.print.attribute.Attribute> getCategory() { return javax.print.attribute.standard.Severity.class; }
  protected java.lang.String[] getStringTable() { return myStringTable.clone(); }
  protected javax.print.attribute.EnumSyntax[] getEnumValueTable() { return (javax.print.attribute.EnumSyntax[]) myEnumValueTable.clone(); }

  private static final String[] myStringTable = {"report", "warning", "error"};

  private static final javax.print.attribute.EnumSyntax[] myEnumValueTable = {REPORT, WARNING, ERROR};
}
