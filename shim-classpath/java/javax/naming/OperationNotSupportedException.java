/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package javax.naming;

public class OperationNotSupportedException extends NamingException {
  private static final long serialVersionUID = 13225273289L;

  public OperationNotSupportedException(String explanation) {
    super(explanation);
  }

  public OperationNotSupportedException() {
    super();
  }
}
