/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package javax.security.auth.login;

public class AccountLockedException extends AccountException {
  private static final long serialVersionUID = 8280345554014066334L;

  public AccountLockedException() {
    super();
  }

  public AccountLockedException(String msg) {
    super(msg);
  }
}
