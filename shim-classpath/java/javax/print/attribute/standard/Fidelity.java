/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package javax.print.attribute.standard;

public final class Fidelity extends javax.print.attribute.EnumSyntax implements javax.print.attribute.PrintJobAttribute, javax.print.attribute.PrintRequestAttribute {
  public static final javax.print.attribute.standard.Fidelity FIDELITY_TRUE = new javax.print.attribute.standard.Fidelity(0);
  public static final javax.print.attribute.standard.Fidelity FIDELITY_FALSE = new javax.print.attribute.standard.Fidelity(1);
  protected Fidelity(int p0) {
    super(p0);
  }
  public final java.lang.String getName() { return "ipp-attribute-fidelity"; }
  public final java.lang.Class<? extends javax.print.attribute.Attribute> getCategory() { return javax.print.attribute.standard.Fidelity.class; }
  protected java.lang.String[] getStringTable() { return myStringTable.clone(); }
  protected javax.print.attribute.EnumSyntax[] getEnumValueTable() { return (javax.print.attribute.EnumSyntax[]) myEnumValueTable.clone(); }

  private static final String[] myStringTable = {"true", "false"};

  private static final javax.print.attribute.EnumSyntax[] myEnumValueTable = {FIDELITY_TRUE, FIDELITY_FALSE};
}
