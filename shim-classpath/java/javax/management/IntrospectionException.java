/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package javax.management;

public class IntrospectionException extends OperationsException {
  private static final long serialVersionUID = 1054516935875481725L;

  public IntrospectionException() {
    super();
  }

  public IntrospectionException(String message) {
    super(message);
  }
}
