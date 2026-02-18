/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package javax.naming;

public class AuthenticationException extends NamingSecurityException {
  private static final long serialVersionUID = 88282518990L;

  public AuthenticationException(String explanation) {
    super(explanation);
  }

  public AuthenticationException() {
    super();
  }
}
