/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package com.sun.beans.editors;

import java.beans.PropertyEditorSupport;

public final class EnumEditor extends PropertyEditorSupport {
  private final Class<?> type;
  private final Object[] constants;
  private final String[] tags;

  public EnumEditor(Class<?> type) {
    this.type = type;
    this.constants = type.getEnumConstants();
    this.tags = new String[constants.length];
    for (int i = 0; i < constants.length; i++) {
      tags[i] = ((Enum<?>) constants[i]).name();
    }
  }

  public Object getValue() {
    return super.getValue();
  }

  public void setValue(Object value) {
    if (value != null && !type.isInstance(value)) {
      throw new IllegalArgumentException("Unsupported value: " + value);
    }
    super.setValue(value);
  }

  public String getAsText() {
    Object value = getValue();
    return value == null ? null : ((Enum<?>) value).name();
  }

  public void setAsText(String text) {
    for (int i = 0; i < tags.length; i++) {
      if (tags[i].equals(text)) {
        super.setValue(constants[i]);
        return;
      }
    }
    super.setValue(null);
  }

  public String[] getTags() {
    return tags.clone();
  }

  public String getJavaInitializationString() {
    Object value = getValue();
    return value == null ? "null" : type.getName().replace((char) 36, (char) 46) + "." + ((Enum<?>) value).name();
  }
}
