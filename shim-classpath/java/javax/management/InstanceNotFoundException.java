/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package javax.management;

public class InstanceNotFoundException extends OperationsException {
  private static final long serialVersionUID = -882579438394773049L;

  public InstanceNotFoundException() {
    super();
  }

  public InstanceNotFoundException(String message) {
    super(message);
  }
}
