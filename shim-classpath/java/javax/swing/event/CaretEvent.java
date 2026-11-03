/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package javax.swing.event;

public abstract class CaretEvent extends java.util.EventObject {
  public CaretEvent(java.lang.Object p0) {
    super(p0);
  }
  public abstract int getMark();
  public abstract int getDot();
}
