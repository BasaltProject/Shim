/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package javax.management;

public class BadBinaryOpValueExpException extends Exception {
  private static final long serialVersionUID = 5068475589449021227L;

  private ValueExp exp;

  public BadBinaryOpValueExpException(ValueExp exp) {
    this.exp = exp;
  }

  public ValueExp getExp() {
    return exp;
  }

  @Override
  public String toString() {
    return "BadBinaryOpValueExpException: " + exp;
  }
}
