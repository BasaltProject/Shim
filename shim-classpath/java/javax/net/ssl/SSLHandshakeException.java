/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package javax.net.ssl;

public class SSLHandshakeException extends SSLException {
  private static final long serialVersionUID = -5045881315018326890L;

  public SSLHandshakeException(String reason) {
    super(reason);
  }

  public SSLHandshakeException(String message, Throwable cause) {
    super(message);
    initCause(cause);
  }
}
