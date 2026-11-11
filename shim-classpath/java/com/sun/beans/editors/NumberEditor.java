/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package com.sun.beans.editors;

import java.beans.PropertyEditorSupport;

public abstract class NumberEditor extends PropertyEditorSupport {
  public String getJavaInitializationString() {
    Object value = getValue();
    return value != null ? value.toString() : "null";
  }
}
