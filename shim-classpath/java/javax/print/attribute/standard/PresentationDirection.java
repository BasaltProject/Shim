/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package javax.print.attribute.standard;

public final class PresentationDirection extends javax.print.attribute.EnumSyntax implements javax.print.attribute.PrintJobAttribute, javax.print.attribute.PrintRequestAttribute {
  public static final javax.print.attribute.standard.PresentationDirection TOBOTTOM_TORIGHT = new javax.print.attribute.standard.PresentationDirection(0);
  public static final javax.print.attribute.standard.PresentationDirection TOBOTTOM_TOLEFT = new javax.print.attribute.standard.PresentationDirection(1);
  public static final javax.print.attribute.standard.PresentationDirection TOTOP_TORIGHT = new javax.print.attribute.standard.PresentationDirection(2);
  public static final javax.print.attribute.standard.PresentationDirection TOTOP_TOLEFT = new javax.print.attribute.standard.PresentationDirection(3);
  public static final javax.print.attribute.standard.PresentationDirection TORIGHT_TOBOTTOM = new javax.print.attribute.standard.PresentationDirection(4);
  public static final javax.print.attribute.standard.PresentationDirection TORIGHT_TOTOP = new javax.print.attribute.standard.PresentationDirection(5);
  public static final javax.print.attribute.standard.PresentationDirection TOLEFT_TOBOTTOM = new javax.print.attribute.standard.PresentationDirection(6);
  public static final javax.print.attribute.standard.PresentationDirection TOLEFT_TOTOP = new javax.print.attribute.standard.PresentationDirection(7);
  PresentationDirection(int p0) {
    super(p0);
  }
  PresentationDirection() {
    super((int) 0);
  }
  public final java.lang.String getName() { return "presentation-direction"; }
  public final java.lang.Class<? extends javax.print.attribute.Attribute> getCategory() { return javax.print.attribute.standard.PresentationDirection.class; }
  protected java.lang.String[] getStringTable() { return myStringTable.clone(); }
  protected javax.print.attribute.EnumSyntax[] getEnumValueTable() { return (javax.print.attribute.EnumSyntax[]) myEnumValueTable.clone(); }

  private static final String[] myStringTable = {"tobottom-toright", "tobottom-toleft", "totop-toright", "totop-toleft", "toright-tobottom", "toright-totop", "toleft-tobottom", "toleft-totop"};

  private static final javax.print.attribute.EnumSyntax[] myEnumValueTable = {TOBOTTOM_TORIGHT, TOBOTTOM_TOLEFT, TOTOP_TORIGHT, TOTOP_TOLEFT, TORIGHT_TOBOTTOM, TORIGHT_TOTOP, TOLEFT_TOBOTTOM, TOLEFT_TOTOP};
}
