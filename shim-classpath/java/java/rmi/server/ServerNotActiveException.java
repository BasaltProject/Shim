/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package java.rmi.server;

public class ServerNotActiveException extends java.lang.Exception {
  private static final long serialVersionUID = 4687940720827538231L;


  public ServerNotActiveException() {
  }
  public ServerNotActiveException(java.lang.String string) {
    super(string);
  }

}
