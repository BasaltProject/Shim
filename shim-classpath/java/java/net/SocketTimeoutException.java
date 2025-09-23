/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package java.net;

import java.io.InterruptedIOException;

public class SocketTimeoutException extends InterruptedIOException {
  private static final long serialVersionUID = -8846654841826352300L;

  public SocketTimeoutException(String msg) {
    super(msg);
  }

  public SocketTimeoutException() { }
}
