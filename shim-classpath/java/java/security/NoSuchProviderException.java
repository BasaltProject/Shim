/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package java.security;

public class NoSuchProviderException extends GeneralSecurityException {
  private static final long serialVersionUID = 8488111756688534474L;

  public NoSuchProviderException() {
    super();
  }

  public NoSuchProviderException(String msg) {
    super(msg);
  }
}
