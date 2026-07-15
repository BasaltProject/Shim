/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package java.awt.event;

import java.awt.*;
import java.awt.event.*;
import java.awt.geom.*;

public abstract class InputEvent extends java.awt.event.ComponentEvent {
  static final int JDK_1_3_MODIFIERS = 64 - 1;
  static final int HIGH_MODIFIERS = ~(64 - 1);
  long when;
  int modifiers;
  public static final int SHIFT_MASK = 1;
  public static final int CTRL_MASK = 2;
  public static final int META_MASK = 4;
  public static final int ALT_MASK = 8;
  public static final int ALT_GRAPH_MASK = 32;
  public static final int BUTTON1_MASK = 16;
  public static final int BUTTON2_MASK = 8;
  public static final int BUTTON3_MASK = 4;
  public static final int SHIFT_DOWN_MASK = 64;
  public static final int CTRL_DOWN_MASK = 128;
  public static final int META_DOWN_MASK = 256;
  public static final int ALT_DOWN_MASK = 512;
  public static final int BUTTON1_DOWN_MASK = 1024;
  public static final int BUTTON2_DOWN_MASK = 2048;
  public static final int BUTTON3_DOWN_MASK = 4096;
  public static final int ALT_GRAPH_DOWN_MASK = 8192;
  InputEvent(java.awt.Component source, int id, long when, int modifiers) {
    super(source, id);
    this.when = when;
    this.modifiers = modifiers;
  }
  final void convertToExtended() {
    if ((modifiers & SHIFT_MASK) != 0) {
      modifiers |= SHIFT_DOWN_MASK;
    }
    if ((modifiers & CTRL_MASK) != 0) {
      modifiers |= CTRL_DOWN_MASK;
    }
    if ((modifiers & META_MASK) != 0) {
      modifiers |= META_DOWN_MASK;
    }
    if ((modifiers & ALT_MASK) != 0) {
      modifiers |= ALT_DOWN_MASK;
    }
    if ((modifiers & ALT_GRAPH_MASK) != 0) {
      modifiers |= ALT_GRAPH_DOWN_MASK;
    }
    if ((modifiers & BUTTON1_MASK) != 0) {
      modifiers |= BUTTON1_DOWN_MASK;
    }
  }
  final void convertToLegacy(boolean withButton1) {
    if ((modifiers & SHIFT_DOWN_MASK) != 0) {
      modifiers |= SHIFT_MASK;
    }
    if ((modifiers & CTRL_DOWN_MASK) != 0) {
      modifiers |= CTRL_MASK;
    }
    if ((modifiers & META_DOWN_MASK) != 0) {
      modifiers |= META_MASK;
    }
    if ((modifiers & ALT_DOWN_MASK) != 0) {
      modifiers |= ALT_MASK;
    }
    if ((modifiers & ALT_GRAPH_DOWN_MASK) != 0) {
      modifiers |= ALT_GRAPH_MASK;
    }
    if (withButton1 && (modifiers & BUTTON1_DOWN_MASK) != 0) {
      modifiers |= BUTTON1_MASK;
    }
  }
  final void synchronizeModifiers() {
    if (getModifiers() != 0 && getModifiersEx() == 0) {
      convertToExtended();
    } else if (getModifiers() == 0 && getModifiersEx() != 0) {
      convertToLegacy(true);
    }
  }
  public int getModifiers() {
    return modifiers & JDK_1_3_MODIFIERS;
  }
  public void consume() {
    consumed = true;
  }
  public boolean isConsumed() {
    return consumed;
  }
  public long getWhen() {
    return when;
  }
  public int getModifiersEx() {
    return modifiers & ~JDK_1_3_MODIFIERS;
  }
  public static java.lang.String getModifiersExText(int p0) {
    StringBuilder buf = new StringBuilder();
    if ((p0 & META_DOWN_MASK) != 0) {
      buf.append("Meta+");
    }
    if ((p0 & CTRL_DOWN_MASK) != 0) {
      buf.append("Ctrl+");
    }
    if ((p0 & ALT_DOWN_MASK) != 0) {
      buf.append("Alt+");
    }
    if ((p0 & SHIFT_DOWN_MASK) != 0) {
      buf.append("Shift+");
    }
    if ((p0 & ALT_GRAPH_DOWN_MASK) != 0) {
      buf.append("Alt Graph+");
    }
    int b = 1;
    for (int mask : new int[] {BUTTON1_DOWN_MASK, BUTTON2_DOWN_MASK, BUTTON3_DOWN_MASK}) {
      if ((p0 & mask) != 0) {
        buf.append("Button" + b + "+");
      }
      b++;
    }
    if (buf.length() > 0) {
      buf.setLength(buf.length() - 1);
    }
    return buf.toString();
  }
  public static int getMaskForButton(int p0) {
    if (p0 <= 0 || p0 > 3) {
      throw new IllegalArgumentException("button doesn't exist " + p0);
    }
    return p0 == 1 ? BUTTON1_DOWN_MASK : p0 == 2 ? BUTTON2_DOWN_MASK : BUTTON3_DOWN_MASK;
  }
  public boolean isShiftDown() {
    return (modifiers & SHIFT_DOWN_MASK) != 0;
  }
  public boolean isControlDown() {
    return (modifiers & CTRL_DOWN_MASK) != 0;
  }
  public boolean isMetaDown() {
    return (modifiers & META_DOWN_MASK) != 0;
  }
  public boolean isAltDown() {
    return (modifiers & ALT_DOWN_MASK) != 0;
  }
  public boolean isAltGraphDown() {
    return (modifiers & ALT_GRAPH_DOWN_MASK) != 0;
  }
}
