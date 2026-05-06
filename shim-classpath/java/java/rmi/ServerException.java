/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package java.rmi;

public class ServerException extends java.rmi.RemoteException {
  private static final long serialVersionUID = -4775845313121906682L;


  public ServerException(java.lang.String string) {
    super(string);
  }
  public ServerException(java.lang.String string, java.lang.Exception exception) {
    super(string, exception);
  }

}
