/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package javax.print.attribute.standard;

public class MediaTray extends javax.print.attribute.standard.Media implements javax.print.attribute.Attribute {
  public static final javax.print.attribute.standard.MediaTray TOP = new javax.print.attribute.standard.MediaTray(0);
  public static final javax.print.attribute.standard.MediaTray MIDDLE = new javax.print.attribute.standard.MediaTray(1);
  public static final javax.print.attribute.standard.MediaTray BOTTOM = new javax.print.attribute.standard.MediaTray(2);
  public static final javax.print.attribute.standard.MediaTray ENVELOPE = new javax.print.attribute.standard.MediaTray(3);
  public static final javax.print.attribute.standard.MediaTray MANUAL = new javax.print.attribute.standard.MediaTray(4);
  public static final javax.print.attribute.standard.MediaTray LARGE_CAPACITY = new javax.print.attribute.standard.MediaTray(5);
  public static final javax.print.attribute.standard.MediaTray MAIN = new javax.print.attribute.standard.MediaTray(6);
  public static final javax.print.attribute.standard.MediaTray SIDE = new javax.print.attribute.standard.MediaTray(7);
  protected MediaTray(int p0) {
    super(p0);
  }
  protected java.lang.String[] getStringTable() { return myStringTable.clone(); }
  protected javax.print.attribute.EnumSyntax[] getEnumValueTable() { return (javax.print.attribute.EnumSyntax[]) myEnumValueTable.clone(); }

  private static final String[] myStringTable = {"top", "middle", "bottom", "envelope", "manual", "large-capacity", "main", "side"};

  private static final javax.print.attribute.EnumSyntax[] myEnumValueTable = {TOP, MIDDLE, BOTTOM, ENVELOPE, MANUAL, LARGE_CAPACITY, MAIN, SIDE};
}
