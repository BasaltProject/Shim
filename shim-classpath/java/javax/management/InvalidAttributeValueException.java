/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package javax.management;

public class InvalidAttributeValueException extends OperationsException {
  private static final long serialVersionUID = 2164571879317142449L;

  public InvalidAttributeValueException() {
    super();
  }

  public InvalidAttributeValueException(String message) {
    super(message);
  }
}
