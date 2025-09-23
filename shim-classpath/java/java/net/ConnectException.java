/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package java.net;

public class ConnectException extends SocketException {
  private static final long serialVersionUID = 1L;

  public ConnectException(String msg) {
    super(msg);
  }

  public ConnectException() { }
}
