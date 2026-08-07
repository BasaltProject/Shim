/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package javax.print.attribute.standard;

public final class PrinterState extends javax.print.attribute.EnumSyntax implements javax.print.attribute.PrintServiceAttribute {
  public static final javax.print.attribute.standard.PrinterState UNKNOWN = new javax.print.attribute.standard.PrinterState(0);
  public static final javax.print.attribute.standard.PrinterState IDLE = new javax.print.attribute.standard.PrinterState(3);
  public static final javax.print.attribute.standard.PrinterState PROCESSING = new javax.print.attribute.standard.PrinterState(4);
  public static final javax.print.attribute.standard.PrinterState STOPPED = new javax.print.attribute.standard.PrinterState(5);
  protected PrinterState(int p0) {
    super(p0);
  }
  public final java.lang.String getName() { return "printer-state"; }
  public final java.lang.Class<? extends javax.print.attribute.Attribute> getCategory() { return javax.print.attribute.standard.PrinterState.class; }
  protected java.lang.String[] getStringTable() { return myStringTable.clone(); }
  protected javax.print.attribute.EnumSyntax[] getEnumValueTable() { return (javax.print.attribute.EnumSyntax[]) myEnumValueTable.clone(); }

  private static final String[] myStringTable = {"unknown", null, null, "idle", "processing", "stopped"};

  private static final javax.print.attribute.EnumSyntax[] myEnumValueTable = {UNKNOWN, null, null, IDLE, PROCESSING, STOPPED};
}
