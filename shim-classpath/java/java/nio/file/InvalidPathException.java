/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package java.nio.file;

import java.util.Objects;

public class InvalidPathException extends IllegalArgumentException {
  private static final long serialVersionUID = 4355821422286746137L;

  private final String input;
  private final int index;

  public InvalidPathException(String input, String reason, int index) {
    super(Objects.requireNonNull(reason));
    this.input = Objects.requireNonNull(input);
    if (index < -1) {
      throw new IllegalArgumentException();
    }
    this.index = index;
  }

  public InvalidPathException(String input, String reason) {
    this(input, reason, -1);
  }

  public String getInput() {
    return input;
  }

  public String getReason() {
    return super.getMessage();
  }

  public int getIndex() {
    return index;
  }

  @Override
  public String getMessage() {
    StringBuilder sb = new StringBuilder();
    sb.append(getReason());
    if (index > -1) {
      sb.append(" at index ");
      sb.append(index);
    }
    sb.append(": ");
    sb.append(input);
    return sb.toString();
  }
}
