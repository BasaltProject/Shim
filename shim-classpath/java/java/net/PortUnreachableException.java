/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package java.net;

public class PortUnreachableException extends SocketException {
  private static final long serialVersionUID = 8462541992376507323L;

  public PortUnreachableException(String msg) {
    super(msg);
  }

  public PortUnreachableException() { }
}
