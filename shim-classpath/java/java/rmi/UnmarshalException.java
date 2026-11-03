/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package java.rmi;

public class UnmarshalException extends java.rmi.RemoteException {
  private static final long serialVersionUID = 594380845140740218L;


  public UnmarshalException(java.lang.String string) {
    super(string);
  }
  public UnmarshalException(java.lang.String string, java.lang.Exception exception) {
    super(string, exception);
  }

}
