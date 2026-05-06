/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package java.rmi;

public class UnexpectedException extends java.rmi.RemoteException {
  private static final long serialVersionUID = 1800467484195073863L;


  public UnexpectedException(java.lang.String string) {
    super(string);
  }
  public UnexpectedException(java.lang.String string, java.lang.Exception exception) {
    super(string, exception);
  }

}
