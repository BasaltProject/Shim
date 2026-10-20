/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package javax.naming;

public class InsufficientResourcesException extends NamingException {
  private static final long serialVersionUID = 40954810088L;

  public InsufficientResourcesException(String explanation) {
    super(explanation);
  }

  public InsufficientResourcesException() {
    super();
  }
}
