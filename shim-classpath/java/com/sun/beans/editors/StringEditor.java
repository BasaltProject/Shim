/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package com.sun.beans.editors;

import java.beans.PropertyEditorSupport;

public final class StringEditor extends PropertyEditorSupport {
  public String getJavaInitializationString() {
    Object value = getValue();
    if (value == null) {
      return "null";
    }
    String text = value.toString();
    StringBuilder sb = new StringBuilder("\"");
    for (int i = 0; i < text.length(); i++) {
      char c = text.charAt(i);
      switch (c) {
      case 8: sb.append("\\b"); break;
      case 9: sb.append("\\t"); break;
      case 10: sb.append("\\n"); break;
      case 12: sb.append("\\f"); break;
      case 13: sb.append("\\r"); break;
      case 34: sb.append("\\\""); break;
      case 92: sb.append("\\\\"); break;
      default: sb.append(c); break;
      }
    }
    return sb.append("\"").toString();
  }

  public void setAsText(String text) {
    setValue(text);
  }
}
