/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package javax.management;

public class MalformedObjectNameException extends OperationsException {
  private static final long serialVersionUID = -572689714442915824L;

  public MalformedObjectNameException() {
    super();
  }

  public MalformedObjectNameException(String message) {
    super(message);
  }
}
