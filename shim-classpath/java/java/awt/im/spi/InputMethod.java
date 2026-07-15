/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package java.awt.im.spi;

public interface InputMethod {
  java.util.Locale getLocale();
  boolean setLocale(java.util.Locale p0);
  void deactivate(boolean p0);
  void dispose();
  void setInputMethodContext(java.awt.im.spi.InputMethodContext p0);
  void notifyClientWindowChange(java.awt.Rectangle p0);
  void hideWindows();
  java.lang.Object getControlObject();
  void removeNotify();
  void dispatchEvent(java.awt.AWTEvent p0);
  void endComposition();
  void activate();
  void setCharacterSubsets(java.lang.Character.Subset[] p0);
  void setCompositionEnabled(boolean p0);
  boolean isCompositionEnabled();
  void reconvert();
}
