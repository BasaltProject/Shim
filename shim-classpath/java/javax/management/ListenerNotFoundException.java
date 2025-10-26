/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package javax.management;

public class ListenerNotFoundException extends OperationsException {
  private static final long serialVersionUID = -7242605822448519061L;

  public ListenerNotFoundException() {
    super();
  }

  public ListenerNotFoundException(String message) {
    super(message);
  }
}
