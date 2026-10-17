/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package javax.security.auth.login;

public class AccountExpiredException extends AccountException {
  private static final long serialVersionUID = -6064064890162661560L;

  public AccountExpiredException() {
    super();
  }

  public AccountExpiredException(String msg) {
    super(msg);
  }
}
