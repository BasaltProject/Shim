/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package java.rmi;

public class ServerError extends java.rmi.RemoteException {
  private static final long serialVersionUID = 8455284893909696482L;


  public ServerError(java.lang.String string, java.lang.Error error) {
    super(string, error);
  }

}
