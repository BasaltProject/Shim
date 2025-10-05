/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package javax.crypto;

public class NoSuchPaddingException extends java.security.GeneralSecurityException {
  private static final long serialVersionUID = -4572885201200175466L;

  public NoSuchPaddingException() {
    super();
  }

  public NoSuchPaddingException(String msg) {
    super(msg);
  }
}
