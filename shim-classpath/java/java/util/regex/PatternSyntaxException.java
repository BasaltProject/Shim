/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package java.util.regex;

import java.util.Objects;

public class PatternSyntaxException extends IllegalArgumentException {
  private static final long serialVersionUID = -3864639126226059218L;

  private final String desc;
  private final String pattern;
  private final int index;

  public PatternSyntaxException(String desc, String regex, int index) {
    super(desc);
    this.desc = Objects.requireNonNull(desc, "desc");
    this.pattern = regex;
    this.index = index;
  }

  public int getIndex() {
    return index;
  }

  public String getDescription() {
    return desc;
  }

  public String getPattern() {
    return pattern;
  }

  @Override
  public String getMessage() {
    StringBuilder sb = new StringBuilder();
    sb.append(desc);
    if (index >= 0) {
      sb.append(" near index ").append(index);
    }
    sb.append(System.lineSeparator());
    sb.append(pattern);
    if (index >= 0) {
      sb.append(System.lineSeparator());
      for (int i = 0; i < index; i++) {
        sb.append(' ');
      }
      sb.append('^');
    }
    return sb.toString();
  }
}
