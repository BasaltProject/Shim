/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package java.rmi;

public class ConnectIOException extends java.rmi.RemoteException {
  private static final long serialVersionUID = -8087809532704668744L;


  public ConnectIOException(java.lang.String string) {
    super(string);
  }
  public ConnectIOException(java.lang.String string, java.lang.Exception exception) {
    super(string, exception);
  }

}
