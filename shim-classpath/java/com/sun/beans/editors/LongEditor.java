/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package com.sun.beans.editors;

public final class LongEditor extends NumberEditor {
  public String getJavaInitializationString() {
    Object value = getValue();
    return value != null ? value + "L" : "null";
  }

  public void setAsText(String text) throws IllegalArgumentException {
    setValue(text == null ? null : Long.decode(text));
  }
}
