/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package java.rmi;

public class AccessException extends java.rmi.RemoteException {
  private static final long serialVersionUID = 6314925228044966088L;


  public AccessException(java.lang.String string) {
    super(string);
  }
  public AccessException(java.lang.String string, java.lang.Exception exception) {
    super(string, exception);
  }

}
