/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package java.rmi;

public class StubNotFoundException extends java.rmi.RemoteException {
  private static final long serialVersionUID = -7088199405468872373L;


  public StubNotFoundException(java.lang.String string) {
    super(string);
  }
  public StubNotFoundException(java.lang.String string, java.lang.Exception exception) {
    super(string, exception);
  }

}
