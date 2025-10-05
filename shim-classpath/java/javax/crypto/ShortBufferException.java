/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package javax.crypto;

public class ShortBufferException extends java.security.GeneralSecurityException {
  private static final long serialVersionUID = 8427718640832943747L;

  public ShortBufferException() {
    super();
  }

  public ShortBufferException(String msg) {
    super(msg);
  }
}
