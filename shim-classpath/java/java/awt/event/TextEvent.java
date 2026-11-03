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

public class TextEvent extends java.awt.AWTEvent {
  public static final int TEXT_FIRST = 900;
  public static final int TEXT_LAST = 900;
  public static final int TEXT_VALUE_CHANGED = 900;
  public TextEvent(java.lang.Object p0, int p1) {
    super(p0, p1);
  }
  public java.lang.String paramString() {
    return id == TEXT_VALUE_CHANGED ? "TEXT_VALUE_CHANGED" : "unknown type";
  }
}
