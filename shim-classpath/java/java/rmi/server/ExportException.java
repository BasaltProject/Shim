/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package java.rmi.server;

public class ExportException extends java.rmi.RemoteException {
  private static final long serialVersionUID = -9155485338494060170L;


  public ExportException(java.lang.String string) {
    super(string);
  }
  public ExportException(java.lang.String string, java.lang.Exception exception) {
    super(string, exception);
  }

}
