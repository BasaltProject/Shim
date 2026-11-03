/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package java.awt;

public final class SystemColor extends java.awt.Color implements java.io.Serializable {
  public static final int DESKTOP = 0;
  public static final int ACTIVE_CAPTION = 1;
  public static final int ACTIVE_CAPTION_TEXT = 2;
  public static final int ACTIVE_CAPTION_BORDER = 3;
  public static final int INACTIVE_CAPTION = 4;
  public static final int INACTIVE_CAPTION_TEXT = 5;
  public static final int INACTIVE_CAPTION_BORDER = 6;
  public static final int WINDOW = 7;
  public static final int WINDOW_BORDER = 8;
  public static final int WINDOW_TEXT = 9;
  public static final int MENU = 10;
  public static final int MENU_TEXT = 11;
  public static final int TEXT = 12;
  public static final int TEXT_TEXT = 13;
  public static final int TEXT_HIGHLIGHT = 14;
  public static final int TEXT_HIGHLIGHT_TEXT = 15;
  public static final int TEXT_INACTIVE_TEXT = 16;
  public static final int CONTROL = 17;
  public static final int CONTROL_TEXT = 18;
  public static final int CONTROL_HIGHLIGHT = 19;
  public static final int CONTROL_LT_HIGHLIGHT = 20;
  public static final int CONTROL_SHADOW = 21;
  public static final int CONTROL_DK_SHADOW = 22;
  public static final int SCROLLBAR = 23;
  public static final int INFO = 24;
  public static final int INFO_TEXT = 25;
  public static final int NUM_COLORS = 26;
  private static final int[] kValues = {
        0xFF005C5C,
        0xFF000080,
        0xFFFFFFFF,
        0xFFC0C0C0,
        0xFF808080,
        0xFFC0C0C0,
        0xFFC0C0C0,
        0xFFFFFFFF,
        0xFF000000,
        0xFF000000,
        0xFFC0C0C0,
        0xFF000000,
        0xFFC0C0C0,
        0xFF000000,
        0xFF000080,
        0xFFFFFFFF,
        0xFF808080,
        0xFFC0C0C0,
        0xFF000000,
        0xFFFFFFFF,
        0xFFE0E0E0,
        0xFF808080,
        0xFF000000,
        0xFFE0E0E0,
        0xFFE0E000,
        0xFF000000,
  };
  private int index;

  private SystemColor(byte index) {
    super(kValues[index]);
    this.index = index;
  }
  public static final java.awt.SystemColor desktop = new java.awt.SystemColor((byte) 0);
  public static final java.awt.SystemColor activeCaption = new java.awt.SystemColor((byte) 1);
  public static final java.awt.SystemColor activeCaptionText = new java.awt.SystemColor((byte) 2);
  public static final java.awt.SystemColor activeCaptionBorder = new java.awt.SystemColor((byte) 3);
  public static final java.awt.SystemColor inactiveCaption = new java.awt.SystemColor((byte) 4);
  public static final java.awt.SystemColor inactiveCaptionText = new java.awt.SystemColor((byte) 5);
  public static final java.awt.SystemColor inactiveCaptionBorder = new java.awt.SystemColor((byte) 6);
  public static final java.awt.SystemColor window = new java.awt.SystemColor((byte) 7);
  public static final java.awt.SystemColor windowBorder = new java.awt.SystemColor((byte) 8);
  public static final java.awt.SystemColor windowText = new java.awt.SystemColor((byte) 9);
  public static final java.awt.SystemColor menu = new java.awt.SystemColor((byte) 10);
  public static final java.awt.SystemColor menuText = new java.awt.SystemColor((byte) 11);
  public static final java.awt.SystemColor text = new java.awt.SystemColor((byte) 12);
  public static final java.awt.SystemColor textText = new java.awt.SystemColor((byte) 13);
  public static final java.awt.SystemColor textHighlight = new java.awt.SystemColor((byte) 14);
  public static final java.awt.SystemColor textHighlightText = new java.awt.SystemColor((byte) 15);
  public static final java.awt.SystemColor textInactiveText = new java.awt.SystemColor((byte) 16);
  public static final java.awt.SystemColor control = new java.awt.SystemColor((byte) 17);
  public static final java.awt.SystemColor controlText = new java.awt.SystemColor((byte) 18);
  public static final java.awt.SystemColor controlHighlight = new java.awt.SystemColor((byte) 19);
  public static final java.awt.SystemColor controlLtHighlight = new java.awt.SystemColor((byte) 20);
  public static final java.awt.SystemColor controlShadow = new java.awt.SystemColor((byte) 21);
  public static final java.awt.SystemColor controlDkShadow = new java.awt.SystemColor((byte) 22);
  public static final java.awt.SystemColor scrollbar = new java.awt.SystemColor((byte) 23);
  public static final java.awt.SystemColor info = new java.awt.SystemColor((byte) 24);
  public static final java.awt.SystemColor infoText = new java.awt.SystemColor((byte) 25);
  SystemColor() {
    super((int) 0);
  }
  public java.lang.String toString() { return getClass().getName() + "[i=" + index + "]"; }
}
