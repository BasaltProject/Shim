/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package javax.print.attribute.standard;

public class Finishings extends javax.print.attribute.EnumSyntax implements javax.print.attribute.DocAttribute, javax.print.attribute.PrintRequestAttribute, javax.print.attribute.PrintJobAttribute {
  public static final javax.print.attribute.standard.Finishings NONE = new javax.print.attribute.standard.Finishings(3);
  public static final javax.print.attribute.standard.Finishings STAPLE = new javax.print.attribute.standard.Finishings(4);
  public static final javax.print.attribute.standard.Finishings COVER = new javax.print.attribute.standard.Finishings(6);
  public static final javax.print.attribute.standard.Finishings BIND = new javax.print.attribute.standard.Finishings(7);
  public static final javax.print.attribute.standard.Finishings SADDLE_STITCH = new javax.print.attribute.standard.Finishings(8);
  public static final javax.print.attribute.standard.Finishings EDGE_STITCH = new javax.print.attribute.standard.Finishings(9);
  public static final javax.print.attribute.standard.Finishings STAPLE_TOP_LEFT = new javax.print.attribute.standard.Finishings(20);
  public static final javax.print.attribute.standard.Finishings STAPLE_BOTTOM_LEFT = new javax.print.attribute.standard.Finishings(21);
  public static final javax.print.attribute.standard.Finishings STAPLE_TOP_RIGHT = new javax.print.attribute.standard.Finishings(22);
  public static final javax.print.attribute.standard.Finishings STAPLE_BOTTOM_RIGHT = new javax.print.attribute.standard.Finishings(23);
  public static final javax.print.attribute.standard.Finishings EDGE_STITCH_LEFT = new javax.print.attribute.standard.Finishings(24);
  public static final javax.print.attribute.standard.Finishings EDGE_STITCH_TOP = new javax.print.attribute.standard.Finishings(25);
  public static final javax.print.attribute.standard.Finishings EDGE_STITCH_RIGHT = new javax.print.attribute.standard.Finishings(26);
  public static final javax.print.attribute.standard.Finishings EDGE_STITCH_BOTTOM = new javax.print.attribute.standard.Finishings(27);
  public static final javax.print.attribute.standard.Finishings STAPLE_DUAL_LEFT = new javax.print.attribute.standard.Finishings(28);
  public static final javax.print.attribute.standard.Finishings STAPLE_DUAL_TOP = new javax.print.attribute.standard.Finishings(29);
  public static final javax.print.attribute.standard.Finishings STAPLE_DUAL_RIGHT = new javax.print.attribute.standard.Finishings(30);
  public static final javax.print.attribute.standard.Finishings STAPLE_DUAL_BOTTOM = new javax.print.attribute.standard.Finishings(31);
  protected Finishings(int p0) {
    super(p0);
  }
  public final java.lang.String getName() { return "finishings"; }
  protected int getOffset() { return 3; }
  public final java.lang.Class<? extends javax.print.attribute.Attribute> getCategory() { return javax.print.attribute.standard.Finishings.class; }
  protected java.lang.String[] getStringTable() { return myStringTable.clone(); }
  protected javax.print.attribute.EnumSyntax[] getEnumValueTable() { return (javax.print.attribute.EnumSyntax[]) myEnumValueTable.clone(); }

  private static final String[] myStringTable = {"none", "staple", null, "cover", "bind", "saddle-stitch", "edge-stitch", null, null, null, null, null, null, null, null, null, null, "staple-top-left", "staple-bottom-left", "staple-top-right", "staple-bottom-right", "edge-stitch-left", "edge-stitch-top", "edge-stitch-right", "edge-stitch-bottom", "staple-dual-left", "staple-dual-top", "staple-dual-right", "staple-dual-bottom"};

  private static final javax.print.attribute.EnumSyntax[] myEnumValueTable = {NONE, STAPLE, null, COVER, BIND, SADDLE_STITCH, EDGE_STITCH, null, null, null, null, null, null, null, null, null, null, STAPLE_TOP_LEFT, STAPLE_BOTTOM_LEFT, STAPLE_TOP_RIGHT, STAPLE_BOTTOM_RIGHT, EDGE_STITCH_LEFT, EDGE_STITCH_TOP, EDGE_STITCH_RIGHT, EDGE_STITCH_BOTTOM, STAPLE_DUAL_LEFT, STAPLE_DUAL_TOP, STAPLE_DUAL_RIGHT, STAPLE_DUAL_BOTTOM};
}
