/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package javax.net.ssl;

public class SSLPeerUnverifiedException extends SSLException {
  private static final long serialVersionUID = -8919512675000600547L;

  public SSLPeerUnverifiedException(String reason) {
    super(reason);
  }

  public SSLPeerUnverifiedException(String message, Throwable cause) {
    super(message);
    initCause(cause);
  }
}
