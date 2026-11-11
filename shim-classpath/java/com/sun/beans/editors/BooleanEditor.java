/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package com.sun.beans.editors;

import java.beans.PropertyEditorSupport;

public final class BooleanEditor extends PropertyEditorSupport {
  public String getJavaInitializationString() {
    Object value = getValue();
    return value != null ? value.toString() : "null";
  }

  public String getAsText() {
    Object value = getValue();
    return value instanceof Boolean ? (((Boolean) value) ? "True" : "False") : null;
  }

  public void setAsText(String text) throws IllegalArgumentException {
    if (text == null) {
      setValue(null);
    } else if (text.equalsIgnoreCase("true")) {
      setValue(Boolean.TRUE);
    } else if (text.equalsIgnoreCase("false")) {
      setValue(Boolean.FALSE);
    } else {
      throw new IllegalArgumentException(text);
    }
  }

  public String[] getTags() {
    return new String[] {"True", "False"};
  }
}
