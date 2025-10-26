/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package javax.management;

public class BadStringOperationException extends Exception {
  private static final long serialVersionUID = 7802201238441662100L;

  private String op;

  public BadStringOperationException(String message) {
    this.op = message;
  }

  @Override
  public String toString() {
    return "BadStringOperationException: " + op;
  }
}
