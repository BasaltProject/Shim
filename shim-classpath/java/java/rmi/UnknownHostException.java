/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package java.rmi;

public class UnknownHostException extends java.rmi.RemoteException {
  private static final long serialVersionUID = -8152710247442114228L;


  public UnknownHostException(java.lang.String string) {
    super(string);
  }
  public UnknownHostException(java.lang.String string, java.lang.Exception exception) {
    super(string, exception);
  }

}
