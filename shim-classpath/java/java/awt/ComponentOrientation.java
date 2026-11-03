/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package java.awt;

public final class ComponentOrientation implements java.io.Serializable {
  private static final int UNK_BIT = 4;
  private static final int IS_LEFT_TO_RIGHT_MASK = 2;
  private static final int IS_HORIZONTAL_MASK = 1;
  private int orientation;
  public static final java.awt.ComponentOrientation LEFT_TO_RIGHT = new ComponentOrientation(IS_HORIZONTAL_MASK | IS_LEFT_TO_RIGHT_MASK);
  public static final java.awt.ComponentOrientation RIGHT_TO_LEFT = new ComponentOrientation(IS_HORIZONTAL_MASK);
  public static final java.awt.ComponentOrientation UNKNOWN = new ComponentOrientation(IS_HORIZONTAL_MASK | IS_LEFT_TO_RIGHT_MASK | UNK_BIT);
  private ComponentOrientation(int p0) {
    orientation = p0;
  }
  public static java.awt.ComponentOrientation getOrientation(java.util.ResourceBundle p0) {
    ComponentOrientation result = null;
    try {
      result = (ComponentOrientation) p0.getObject("Orientation");
    } catch (Exception e) {
      // fall through
    }
    if (result == null) {
      result = getOrientation(p0.getLocale());
    }
    if (result == null) {
      result = getOrientation(java.util.Locale.getDefault());
    }
    return result;
  }
  public static java.awt.ComponentOrientation getOrientation(java.util.Locale p0) {
    String lang = p0.getLanguage();
    if ("iw".equals(lang) || "he".equals(lang) || "ar".equals(lang) || "fa".equals(lang) || "ur".equals(lang)) {
      return RIGHT_TO_LEFT;
    }
    return LEFT_TO_RIGHT;
  }
  public boolean isHorizontal() { return (orientation & IS_HORIZONTAL_MASK) != 0; }
  public boolean isLeftToRight() { return (orientation & IS_LEFT_TO_RIGHT_MASK) != 0; }
}
