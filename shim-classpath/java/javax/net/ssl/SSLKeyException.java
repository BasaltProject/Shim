/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package javax.net.ssl;

public class SSLKeyException extends SSLException {
  private static final long serialVersionUID = -8071664081941937874L;

  public SSLKeyException(String reason) {
    super(reason);
  }

  public SSLKeyException(String message, Throwable cause) {
    super(message);
    initCause(cause);
  }
}
