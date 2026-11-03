/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package javax.print.attribute.standard;

public class PDLOverrideSupported extends javax.print.attribute.EnumSyntax implements javax.print.attribute.PrintServiceAttribute {
  public static final javax.print.attribute.standard.PDLOverrideSupported NOT_ATTEMPTED = new javax.print.attribute.standard.PDLOverrideSupported(0);
  public static final javax.print.attribute.standard.PDLOverrideSupported ATTEMPTED = new javax.print.attribute.standard.PDLOverrideSupported(1);
  protected PDLOverrideSupported(int p0) {
    super(p0);
  }
  public final java.lang.String getName() { return "pdl-override-supported"; }
  public final java.lang.Class<? extends javax.print.attribute.Attribute> getCategory() { return javax.print.attribute.standard.PDLOverrideSupported.class; }
  protected java.lang.String[] getStringTable() { return myStringTable.clone(); }
  protected javax.print.attribute.EnumSyntax[] getEnumValueTable() { return (javax.print.attribute.EnumSyntax[]) myEnumValueTable.clone(); }

  private static final String[] myStringTable = {"not-attempted", "attempted"};

  private static final javax.print.attribute.EnumSyntax[] myEnumValueTable = {NOT_ATTEMPTED, ATTEMPTED};
}
