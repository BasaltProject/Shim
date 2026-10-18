/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 * SPDX-License-Identifier: MIT
 */

package java.beans;

public class PropertyVetoException extends Exception {
  private static final long serialVersionUID = 129596057694162164L;

  private PropertyChangeEvent evt;

  public PropertyVetoException(String mess, PropertyChangeEvent evt) {
    super(mess);
    this.evt = evt;
  }

  public PropertyChangeEvent getPropertyChangeEvent() {
    return evt;
  }
}
