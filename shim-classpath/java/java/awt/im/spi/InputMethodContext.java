/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package java.awt.im.spi;

public interface InputMethodContext extends java.awt.im.InputMethodRequests {
  void dispatchInputMethodEvent(int p0, java.text.AttributedCharacterIterator p1, int p2, java.awt.font.TextHitInfo p3, java.awt.font.TextHitInfo p4);
  javax.swing.JFrame createInputMethodJFrame(java.lang.String p0, boolean p1);
  void enableClientWindowNotification(java.awt.im.spi.InputMethod p0, boolean p1);
  java.awt.Window createInputMethodWindow(java.lang.String p0, boolean p1);
}
