/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package java.awt;

import java.awt.event.KeyEvent;

public class MenuShortcut implements java.io.Serializable {
  int key;
  boolean usesShift;

  public MenuShortcut(int key) { this(key, false); }

  public MenuShortcut(int key, boolean useShiftModifier) {
    this.key = key;
    this.usesShift = useShiftModifier;
  }

  public int getKey() { return key; }

  public boolean usesShiftModifier() { return usesShift; }

  public boolean equals(MenuShortcut s) { return (s != null && (s.getKey() == key) && (s.usesShiftModifier() == usesShift)); }

  public boolean equals(Object obj) {
    if (obj instanceof MenuShortcut) {
      return equals((MenuShortcut) obj);
    }
    return false;
  }

  public int hashCode() { return (usesShift) ? (~key) : key; }

  public String toString() {
    int modifiers = 0;
    if (!GraphicsEnvironment.isHeadless()) {
      modifiers = Toolkit.getDefaultToolkit().getMenuShortcutKeyMaskEx();
    }
    if (usesShiftModifier()) {
      modifiers |= java.awt.event.InputEvent.SHIFT_DOWN_MASK;
    }
    return KeyEvent.getModifiersExText(modifiers) + "+" + KeyEvent.getKeyText(key);
  }

  protected String paramString() {
    String str = "key=" + key;
    if (usesShiftModifier()) {
      str += ",usesShiftModifier";
    }
    return str;
  }
}
