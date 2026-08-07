/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package javax.print.attribute.standard;

public final class ColorSupported extends javax.print.attribute.EnumSyntax implements javax.print.attribute.PrintServiceAttribute {
  public static final javax.print.attribute.standard.ColorSupported NOT_SUPPORTED = new javax.print.attribute.standard.ColorSupported(0);
  public static final javax.print.attribute.standard.ColorSupported SUPPORTED = new javax.print.attribute.standard.ColorSupported(1);
  protected ColorSupported(int p0) {
    super(p0);
  }
  public final java.lang.String getName() { return "color-supported"; }
  public final java.lang.Class<? extends javax.print.attribute.Attribute> getCategory() { return javax.print.attribute.standard.ColorSupported.class; }
  protected java.lang.String[] getStringTable() { return myStringTable.clone(); }
  protected javax.print.attribute.EnumSyntax[] getEnumValueTable() { return (javax.print.attribute.EnumSyntax[]) myEnumValueTable.clone(); }

  private static final String[] myStringTable = {"not-supported", "supported"};

  private static final javax.print.attribute.EnumSyntax[] myEnumValueTable = {NOT_SUPPORTED, SUPPORTED};
}
