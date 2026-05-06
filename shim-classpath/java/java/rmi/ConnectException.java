/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package java.rmi;

public class ConnectException extends java.rmi.RemoteException {
  private static final long serialVersionUID = 4863550261346652506L;


  public ConnectException(java.lang.String string) {
    super(string);
  }
  public ConnectException(java.lang.String string, java.lang.Exception exception) {
    super(string, exception);
  }

}
