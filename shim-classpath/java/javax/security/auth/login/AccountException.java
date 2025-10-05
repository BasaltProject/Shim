/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package javax.security.auth.login;

public class AccountException extends LoginException {
  private static final long serialVersionUID = -2112878680072211787L;

  public AccountException() {
    super();
  }

  public AccountException(String msg) {
    super(msg);
  }
}
