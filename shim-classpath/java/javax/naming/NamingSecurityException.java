/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package javax.naming;

public class NamingSecurityException extends NamingException {
  private static final long serialVersionUID = 26567409268L;

  public NamingSecurityException(String explanation) {
    super(explanation);
  }

  public NamingSecurityException() {
    super();
  }
}
