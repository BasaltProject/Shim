/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package javax.print.attribute.standard;

public final class DialogTypeSelection extends javax.print.attribute.EnumSyntax implements javax.print.attribute.PrintRequestAttribute {
  public static final javax.print.attribute.standard.DialogTypeSelection NATIVE = new javax.print.attribute.standard.DialogTypeSelection(0);
  public static final javax.print.attribute.standard.DialogTypeSelection COMMON = new javax.print.attribute.standard.DialogTypeSelection(1);
  protected DialogTypeSelection(int p0) {
    super(p0);
  }
  public final java.lang.String getName() { return "dialog-type-selection"; }
  public final java.lang.Class<? extends javax.print.attribute.Attribute> getCategory() { return javax.print.attribute.standard.DialogTypeSelection.class; }
  protected java.lang.String[] getStringTable() { return myStringTable.clone(); }
  protected javax.print.attribute.EnumSyntax[] getEnumValueTable() { return (javax.print.attribute.EnumSyntax[]) myEnumValueTable.clone(); }

  private static final String[] myStringTable = {"native", "common"};

  private static final javax.print.attribute.EnumSyntax[] myEnumValueTable = {NATIVE, COMMON};
}
