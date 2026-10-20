/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package javax.naming;

public class InterruptedNamingException extends NamingException {
  private static final long serialVersionUID = 13877257414L;

  public InterruptedNamingException(String explanation) {
    super(explanation);
  }

  public InterruptedNamingException() {
    super();
  }
}
