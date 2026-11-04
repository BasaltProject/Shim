/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package javax.security.sasl;

public class AuthenticationException extends SaslException {
  private static final long serialVersionUID = -3579708765071815007L;

  public AuthenticationException() {
    super();
  }

  public AuthenticationException(String detail) {
    super(detail);
  }

  public AuthenticationException(String detail, Throwable ex) {
    super(detail, ex);
  }
}
