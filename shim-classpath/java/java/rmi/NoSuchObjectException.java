/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package java.rmi;

public class NoSuchObjectException extends java.rmi.RemoteException {
  private static final long serialVersionUID = 6619395951570472985L;


  public NoSuchObjectException(java.lang.String string) {
    super(string);
  }

}
