/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package java.security;

public class KeyStoreException extends GeneralSecurityException {
  private static final long serialVersionUID = -1119353179322377262L;

  public KeyStoreException() {
    super();
  }

  public KeyStoreException(String msg) {
    super(msg);
  }

  public KeyStoreException(String message, Throwable cause) {
    super(message, cause);
  }

  public KeyStoreException(Throwable cause) {
    super(cause);
  }
}
