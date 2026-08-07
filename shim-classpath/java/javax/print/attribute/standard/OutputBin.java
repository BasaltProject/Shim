/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package javax.print.attribute.standard;

public class OutputBin extends javax.print.attribute.EnumSyntax implements javax.print.attribute.PrintRequestAttribute, javax.print.attribute.PrintJobAttribute {
  public static final javax.print.attribute.standard.OutputBin TOP = new javax.print.attribute.standard.OutputBin(0);
  public static final javax.print.attribute.standard.OutputBin MIDDLE = new javax.print.attribute.standard.OutputBin(1);
  public static final javax.print.attribute.standard.OutputBin BOTTOM = new javax.print.attribute.standard.OutputBin(2);
  public static final javax.print.attribute.standard.OutputBin SIDE = new javax.print.attribute.standard.OutputBin(3);
  public static final javax.print.attribute.standard.OutputBin LEFT = new javax.print.attribute.standard.OutputBin(4);
  public static final javax.print.attribute.standard.OutputBin RIGHT = new javax.print.attribute.standard.OutputBin(5);
  public static final javax.print.attribute.standard.OutputBin CENTER = new javax.print.attribute.standard.OutputBin(6);
  public static final javax.print.attribute.standard.OutputBin REAR = new javax.print.attribute.standard.OutputBin(7);
  public static final javax.print.attribute.standard.OutputBin FACE_UP = new javax.print.attribute.standard.OutputBin(8);
  public static final javax.print.attribute.standard.OutputBin FACE_DOWN = new javax.print.attribute.standard.OutputBin(9);
  public static final javax.print.attribute.standard.OutputBin LARGE_CAPACITY = new javax.print.attribute.standard.OutputBin(10);
  protected OutputBin(int p0) {
    super(p0);
  }
  public final java.lang.String getName() { return "output-bin"; }
  public final java.lang.Class<? extends javax.print.attribute.Attribute> getCategory() { return javax.print.attribute.standard.OutputBin.class; }
  protected java.lang.String[] getStringTable() { return myStringTable.clone(); }
  protected javax.print.attribute.EnumSyntax[] getEnumValueTable() { return (javax.print.attribute.EnumSyntax[]) myEnumValueTable.clone(); }

  private static final String[] myStringTable = {"top", "middle", "bottom", "side", "left", "right", "center", "rear", "face-up", "face-down", "large-capacity"};

  private static final javax.print.attribute.EnumSyntax[] myEnumValueTable = {TOP, MIDDLE, BOTTOM, SIDE, LEFT, RIGHT, CENTER, REAR, FACE_UP, FACE_DOWN, LARGE_CAPACITY};
}
