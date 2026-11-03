/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package javax.print.attribute.standard;

public final class Sides extends javax.print.attribute.EnumSyntax implements javax.print.attribute.DocAttribute, javax.print.attribute.PrintRequestAttribute, javax.print.attribute.PrintJobAttribute {
  public static final javax.print.attribute.standard.Sides ONE_SIDED = new javax.print.attribute.standard.Sides(0);
  public static final javax.print.attribute.standard.Sides TWO_SIDED_LONG_EDGE = new javax.print.attribute.standard.Sides(1);
  public static final javax.print.attribute.standard.Sides TWO_SIDED_SHORT_EDGE = new javax.print.attribute.standard.Sides(2);
  public static final javax.print.attribute.standard.Sides DUPLEX = TWO_SIDED_LONG_EDGE;
  public static final javax.print.attribute.standard.Sides TUMBLE = TWO_SIDED_SHORT_EDGE;
  protected Sides(int p0) {
    super(p0);
  }
  public final java.lang.String getName() { return "sides"; }
  public final java.lang.Class<? extends javax.print.attribute.Attribute> getCategory() { return javax.print.attribute.standard.Sides.class; }
  protected java.lang.String[] getStringTable() { return myStringTable.clone(); }
  protected javax.print.attribute.EnumSyntax[] getEnumValueTable() { return (javax.print.attribute.EnumSyntax[]) myEnumValueTable.clone(); }

  private static final String[] myStringTable = {"one-sided", "two-sided-long-edge", "two-sided-short-edge"};

  private static final javax.print.attribute.EnumSyntax[] myEnumValueTable = {ONE_SIDED, TWO_SIDED_LONG_EDGE, TWO_SIDED_SHORT_EDGE};
}
