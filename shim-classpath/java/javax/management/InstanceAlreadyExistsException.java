/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package javax.management;

public class InstanceAlreadyExistsException extends OperationsException {
  private static final long serialVersionUID = 8893743928912733931L;

  public InstanceAlreadyExistsException() {
    super();
  }

  public InstanceAlreadyExistsException(String message) {
    super(message);
  }
}
