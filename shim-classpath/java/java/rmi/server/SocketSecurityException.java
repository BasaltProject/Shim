/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package java.rmi.server;

public class SocketSecurityException extends java.rmi.server.ExportException {
  private static final long serialVersionUID = -7622072999407781979L;


  public SocketSecurityException(java.lang.String string) {
    super(string);
  }
  public SocketSecurityException(java.lang.String string, java.lang.Exception exception) {
    super(string, exception);
  }

}
