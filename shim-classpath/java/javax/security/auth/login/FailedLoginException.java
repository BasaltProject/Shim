/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package javax.security.auth.login;

public class FailedLoginException extends LoginException {
  private static final long serialVersionUID = 802556922354616286L;

  public FailedLoginException() {
    super();
  }

  public FailedLoginException(String msg) {
    super(msg);
  }
}
