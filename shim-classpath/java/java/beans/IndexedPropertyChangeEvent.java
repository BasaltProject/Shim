/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 * SPDX-License-Identifier: MIT
 */

package java.beans;

public class IndexedPropertyChangeEvent extends PropertyChangeEvent {
  private static final long serialVersionUID = -320227448495806870L;

  private int index;

  public IndexedPropertyChangeEvent(Object source, String propertyName, Object oldValue, Object newValue,
      int index) {
    super(source, propertyName, oldValue, newValue);
    this.index = index;
  }

  public int getIndex() {
    return index;
  }

  @Override
  void appendTo(StringBuilder sb) {
    sb.append("; index=").append(getIndex());
  }
}
