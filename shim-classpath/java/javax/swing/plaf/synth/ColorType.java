/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */
package javax.swing.plaf.synth;

public class ColorType {
  public static final ColorType FOREGROUND = new ColorType("Foreground");
  public static final ColorType BACKGROUND = new ColorType("Background");
  public static final ColorType TEXT_FOREGROUND = new ColorType("TextForeground");
  public static final ColorType TEXT_BACKGROUND = new ColorType("TextBackground");
  public static final ColorType FOCUS = new ColorType("Focus");
  public static final int MAX_COUNT = 5;

  private static int nextId;
  private final String description;
  private final int index;

  protected ColorType(String description) {
    if (description == null) {
      throw new NullPointerException("ColorType must have a valid description");
    }
    this.description = description;
    synchronized (ColorType.class) {
      this.index = nextId++;
    }
  }

  public final int getID() {
    return index;
  }

  public String toString() {
    return description;
  }
}
