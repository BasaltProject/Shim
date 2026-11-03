/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package javax.print.attribute.standard;

public final class OrientationRequested extends javax.print.attribute.EnumSyntax implements javax.print.attribute.DocAttribute, javax.print.attribute.PrintRequestAttribute, javax.print.attribute.PrintJobAttribute {
  public static final javax.print.attribute.standard.OrientationRequested PORTRAIT = new javax.print.attribute.standard.OrientationRequested(3);
  public static final javax.print.attribute.standard.OrientationRequested LANDSCAPE = new javax.print.attribute.standard.OrientationRequested(4);
  public static final javax.print.attribute.standard.OrientationRequested REVERSE_LANDSCAPE = new javax.print.attribute.standard.OrientationRequested(5);
  public static final javax.print.attribute.standard.OrientationRequested REVERSE_PORTRAIT = new javax.print.attribute.standard.OrientationRequested(6);
  protected OrientationRequested(int p0) {
    super(p0);
  }
  public final java.lang.String getName() { return "orientation-requested"; }
  protected int getOffset() { return 3; }
  public final java.lang.Class<? extends javax.print.attribute.Attribute> getCategory() { return javax.print.attribute.standard.OrientationRequested.class; }
  protected java.lang.String[] getStringTable() { return myStringTable.clone(); }
  protected javax.print.attribute.EnumSyntax[] getEnumValueTable() { return (javax.print.attribute.EnumSyntax[]) myEnumValueTable.clone(); }

  private static final String[] myStringTable = {"portrait", "landscape", "reverse-landscape", "reverse-portrait"};

  private static final javax.print.attribute.EnumSyntax[] myEnumValueTable = {PORTRAIT, LANDSCAPE, REVERSE_LANDSCAPE, REVERSE_PORTRAIT};
}
