/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package javax.security.auth.login;

public class LoginException extends java.security.GeneralSecurityException {
  private static final long serialVersionUID = -4679091624035232488L;

  public LoginException() {
    super();
  }

  public LoginException(String msg) {
    super(msg);
  }
}
