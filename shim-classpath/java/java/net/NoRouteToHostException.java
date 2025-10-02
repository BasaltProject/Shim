/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package java.net;

public class NoRouteToHostException extends SocketException {
  private static final long serialVersionUID = -1897550894873493790L;

  public NoRouteToHostException(String msg) {
    super(msg);
  }

  public NoRouteToHostException() { }
}
