/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */
package javax.swing.plaf.nimbus;

import javax.swing.JComponent;

public abstract class State<T extends JComponent> {
  private final String name;

  protected State(String name) {
    this.name = name;
  }

  public String toString() {
    return name;
  }

  protected abstract boolean isInState(T c);
}
