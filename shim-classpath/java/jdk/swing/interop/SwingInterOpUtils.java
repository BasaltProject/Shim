/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package jdk.swing.interop;

public class SwingInterOpUtils {
  public static final int GRAB_EVENT_MASK = Integer.MIN_VALUE;

  public SwingInterOpUtils() {
  }

  public static void grab(java.awt.Toolkit toolkit, java.awt.Window window) {
    throw new UnsupportedOperationException();
  }
  public static boolean isUngrabEvent(java.awt.AWTEvent aWTEvent) {
    throw new UnsupportedOperationException();
  }
  public static void postEvent(java.lang.Object object, java.awt.AWTEvent aWTEvent) {
    throw new UnsupportedOperationException();
  }
  public static void ungrab(java.awt.Toolkit toolkit, java.awt.Window window) {
    throw new UnsupportedOperationException();
  }
}
