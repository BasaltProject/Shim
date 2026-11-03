/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package java.rmi;

public class MarshalException extends java.rmi.RemoteException {
  private static final long serialVersionUID = 6223554758134037936L;


  public MarshalException(java.lang.String string) {
    super(string);
  }
  public MarshalException(java.lang.String string, java.lang.Exception exception) {
    super(string, exception);
  }

}
