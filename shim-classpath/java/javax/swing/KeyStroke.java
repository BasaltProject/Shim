/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package javax.swing;

import java.awt.AWTKeyStroke;
import java.awt.event.KeyEvent;

public class KeyStroke extends AWTKeyStroke {
  private static final long serialVersionUID = -9060180771037902530L;

  static {
    AWTKeyStroke.registerSubclass(KeyStroke.class);
  }

  private KeyStroke() { }

  private KeyStroke(char keyChar, int keyCode, int modifiers, boolean onKeyRelease) {
    super(keyChar, keyCode, modifiers, onKeyRelease);
  }

  public static KeyStroke getKeyStroke(char keyChar) {
    return (KeyStroke) AWTKeyStroke.getAWTKeyStroke(keyChar);
  }

  @Deprecated
  public static KeyStroke getKeyStroke(char keyChar, boolean onKeyRelease) {
    return new KeyStroke(keyChar, KeyEvent.VK_UNDEFINED, 0, onKeyRelease);
  }

  public static KeyStroke getKeyStroke(Character keyChar, int modifiers) {
    return (KeyStroke) AWTKeyStroke.getAWTKeyStroke(keyChar, modifiers);
  }

  public static KeyStroke getKeyStroke(int keyCode, int modifiers, boolean onKeyRelease) {
    return (KeyStroke) AWTKeyStroke.getAWTKeyStroke(keyCode, modifiers, onKeyRelease);
  }

  public static KeyStroke getKeyStroke(int keyCode, int modifiers) {
    return (KeyStroke) AWTKeyStroke.getAWTKeyStroke(keyCode, modifiers);
  }

  public static KeyStroke getKeyStrokeForEvent(KeyEvent anEvent) {
    return (KeyStroke) AWTKeyStroke.getAWTKeyStrokeForEvent(anEvent);
  }

  public static KeyStroke getKeyStroke(String s) {
    if (s == null || s.length() == 0) {
      return null;
    }
    try {
      return (KeyStroke) AWTKeyStroke.getAWTKeyStroke(s);
    } catch (IllegalArgumentException e) {
      return null;
    }
  }
}
