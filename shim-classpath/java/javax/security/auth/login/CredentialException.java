/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package javax.security.auth.login;

public class CredentialException extends LoginException {
  private static final long serialVersionUID = -4772893876810601859L;

  public CredentialException() {
    super();
  }

  public CredentialException(String msg) {
    super(msg);
  }
}
