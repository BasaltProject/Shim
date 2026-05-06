/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package java.rmi.server;

public class SkeletonNotFoundException extends java.rmi.RemoteException {
  private static final long serialVersionUID = -7860299673822761231L;


  public SkeletonNotFoundException(java.lang.String string) {
    super(string);
  }
  public SkeletonNotFoundException(java.lang.String string, java.lang.Exception exception) {
    super(string, exception);
  }

}
