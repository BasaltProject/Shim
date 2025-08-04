/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package java.net;

public class URISyntaxException extends Exception {
  private final String input;
  private final int index;

  public URISyntaxException(String input, String reason, int index) {
    super(reason);
    if (input == null || reason == null) throw new NullPointerException();
    if (index < -1) throw new IllegalArgumentException();
    this.input = input;
    this.index = index;
  }

  public URISyntaxException(String input, String reason) {
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
      sb.append(" at index ").append(index);
    }
    sb.append(": ").append(input);
    return sb.toString();
  }
}
