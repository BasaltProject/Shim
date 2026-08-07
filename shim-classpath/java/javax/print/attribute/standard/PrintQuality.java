/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package javax.print.attribute.standard;

public class PrintQuality extends javax.print.attribute.EnumSyntax implements javax.print.attribute.DocAttribute, javax.print.attribute.PrintRequestAttribute, javax.print.attribute.PrintJobAttribute {
  public static final javax.print.attribute.standard.PrintQuality DRAFT = new javax.print.attribute.standard.PrintQuality(3);
  public static final javax.print.attribute.standard.PrintQuality NORMAL = new javax.print.attribute.standard.PrintQuality(4);
  public static final javax.print.attribute.standard.PrintQuality HIGH = new javax.print.attribute.standard.PrintQuality(5);
  protected PrintQuality(int p0) {
    super(p0);
  }
  public final java.lang.String getName() { return "print-quality"; }
  protected int getOffset() { return 3; }
  public final java.lang.Class<? extends javax.print.attribute.Attribute> getCategory() { return javax.print.attribute.standard.PrintQuality.class; }
  protected java.lang.String[] getStringTable() { return myStringTable.clone(); }
  protected javax.print.attribute.EnumSyntax[] getEnumValueTable() { return (javax.print.attribute.EnumSyntax[]) myEnumValueTable.clone(); }

  private static final String[] myStringTable = {"draft", "normal", "high"};

  private static final javax.print.attribute.EnumSyntax[] myEnumValueTable = {DRAFT, NORMAL, HIGH};
}
