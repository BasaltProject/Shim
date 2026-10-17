/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package javax.security.auth.login;

public class AccountNotFoundException extends AccountException {
  private static final long serialVersionUID = 1498349563916294614L;

  public AccountNotFoundException() {
    super();
  }

  public AccountNotFoundException(String msg) {
    super(msg);
  }
}
