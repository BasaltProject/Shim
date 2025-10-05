/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package javax.net.ssl;

public class SSLProtocolException extends SSLException {
  private static final long serialVersionUID = 5445067063799134928L;

  public SSLProtocolException(String reason) {
    super(reason);
  }

  public SSLProtocolException(String message, Throwable cause) {
    super(message);
    initCause(cause);
  }
}
