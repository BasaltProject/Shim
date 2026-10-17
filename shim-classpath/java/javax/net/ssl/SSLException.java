/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package javax.net.ssl;

public class SSLException extends java.io.IOException {
  private static final long serialVersionUID = 4511006460650708967L;

  public SSLException(String reason) {
    super(reason);
  }

  public SSLException(String message, Throwable cause) {
    super(message);
    initCause(cause);
  }

  public SSLException(Throwable cause) {
    super(cause);
  }
}
