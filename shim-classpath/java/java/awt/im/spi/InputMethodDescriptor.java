/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package java.awt.im.spi;

public interface InputMethodDescriptor {
  java.util.Locale[] getAvailableLocales() throws java.awt.AWTException;
  boolean hasDynamicLocaleList();
  java.lang.String getInputMethodDisplayName(java.util.Locale p0, java.util.Locale p1);
  java.awt.Image getInputMethodIcon(java.util.Locale p0);
  java.awt.im.spi.InputMethod createInputMethod() throws java.lang.Exception;
}
