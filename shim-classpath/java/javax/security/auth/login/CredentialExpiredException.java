/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package javax.security.auth.login;

public class CredentialExpiredException extends CredentialException {
  private static final long serialVersionUID = 5344739776805546609L;

  public CredentialExpiredException() {
    super();
  }

  public CredentialExpiredException(String msg) {
    super(msg);
  }
}
