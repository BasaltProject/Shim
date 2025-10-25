/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package javax.security.auth.login;

public class CredentialNotFoundException extends CredentialException {
  private static final long serialVersionUID = -7780200375415428163L;

  public CredentialNotFoundException() {
    super();
  }

  public CredentialNotFoundException(String msg) {
    super(msg);
  }
}
