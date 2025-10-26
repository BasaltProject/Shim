/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package javax.management;

public class OperationsException extends JMException {
  private static final long serialVersionUID = -4967597595580536216L;

  public OperationsException() {
    super();
  }

  public OperationsException(String message) {
    super(message);
  }
}
