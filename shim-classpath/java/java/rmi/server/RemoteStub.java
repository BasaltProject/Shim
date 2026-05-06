/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package java.rmi.server;

@Deprecated
public abstract class RemoteStub extends RemoteObject {
  private static final long serialVersionUID = -1585587260594494182L;

  protected RemoteStub() {
    super();
  }

  protected RemoteStub(RemoteRef ref) {
    super(ref);
  }

  @Deprecated
  protected static void setRef(RemoteStub stub, RemoteRef ref) {
    throw new UnsupportedOperationException();
  }
}
