/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package javax.print.attribute.standard;

public final class Chromaticity extends javax.print.attribute.EnumSyntax implements javax.print.attribute.DocAttribute, javax.print.attribute.PrintRequestAttribute, javax.print.attribute.PrintJobAttribute {
  public static final javax.print.attribute.standard.Chromaticity MONOCHROME = new javax.print.attribute.standard.Chromaticity(0);
  public static final javax.print.attribute.standard.Chromaticity COLOR = new javax.print.attribute.standard.Chromaticity(1);
  protected Chromaticity(int p0) {
    super(p0);
  }
  public final java.lang.String getName() { return "chromaticity"; }
  public final java.lang.Class<? extends javax.print.attribute.Attribute> getCategory() { return javax.print.attribute.standard.Chromaticity.class; }
  protected java.lang.String[] getStringTable() { return myStringTable.clone(); }
  protected javax.print.attribute.EnumSyntax[] getEnumValueTable() { return (javax.print.attribute.EnumSyntax[]) myEnumValueTable.clone(); }

  private static final String[] myStringTable = {"monochrome", "color"};

  private static final javax.print.attribute.EnumSyntax[] myEnumValueTable = {MONOCHROME, COLOR};
}
