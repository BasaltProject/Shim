/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package java.awt;

public class Cursor implements java.io.Serializable {
  public static final int DEFAULT_CURSOR = 0;
  public static final int CROSSHAIR_CURSOR = 1;
  public static final int TEXT_CURSOR = 2;
  public static final int WAIT_CURSOR = 3;
  public static final int SW_RESIZE_CURSOR = 4;
  public static final int SE_RESIZE_CURSOR = 5;
  public static final int NW_RESIZE_CURSOR = 6;
  public static final int NE_RESIZE_CURSOR = 7;
  public static final int N_RESIZE_CURSOR = 8;
  public static final int S_RESIZE_CURSOR = 9;
  public static final int W_RESIZE_CURSOR = 10;
  public static final int E_RESIZE_CURSOR = 11;
  public static final int HAND_CURSOR = 12;
  public static final int MOVE_CURSOR = 13;
  protected static java.awt.Cursor[] predefined;
  public static final int CUSTOM_CURSOR = -1;
  protected java.lang.String name;
  int type = DEFAULT_CURSOR;
  public Cursor(int p0) {
    if (p0 < Cursor.DEFAULT_CURSOR || p0 > Cursor.MOVE_CURSOR) {
      throw new IllegalArgumentException("illegal cursor type");
    }
    this.type = p0;
    this.name = "Cursor " + p0;
  }
  protected Cursor(java.lang.String p0) {
    this.type = CUSTOM_CURSOR;
    this.name = p0;
  }
  public java.lang.String getName() { return name; }
  public java.lang.String toString() { return getClass().getName() + "[" + getName() + "]"; }
  public int getType() { return type; }
  public static java.awt.Cursor getPredefinedCursor(int p0) {
    if (p0 < Cursor.DEFAULT_CURSOR || p0 > Cursor.MOVE_CURSOR) {
      throw new IllegalArgumentException("illegal cursor type");
    }
    synchronized (Cursor.class) {
      if (predefined == null) {
        predefined = new Cursor[14];
      }
      Cursor c = predefined[p0];
      if (c == null) {
        c = new Cursor(p0);
        predefined[p0] = c;
      }
      return c;
    }
  }
  public static java.awt.Cursor getSystemCustomCursor(java.lang.String p0) throws java.awt.AWTException, java.awt.HeadlessException { throw new java.awt.HeadlessException(); }
  public static java.awt.Cursor getDefaultCursor() { return getPredefinedCursor(Cursor.DEFAULT_CURSOR); }
}
