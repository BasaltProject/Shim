/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package javax.management;

public class BadAttributeValueExpException extends Exception {
  private static final long serialVersionUID = -3105272988410493376L;

  private Object val;

  public BadAttributeValueExpException(Object val) {
    this.val = val == null ? null : val.toString();
  }

  @Override
  public String toString() {
    return "BadAttributeValueException: " + val;
  }
}
