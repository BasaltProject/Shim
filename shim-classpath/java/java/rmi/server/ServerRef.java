/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package java.rmi.server;

public interface ServerRef extends java.rmi.server.RemoteRef {
  long serialVersionUID = -4557750989390278438L;

  java.rmi.server.RemoteStub exportObject(java.rmi.Remote remote, java.lang.Object object) throws java.rmi.RemoteException;
  java.lang.String getClientHost() throws java.rmi.server.ServerNotActiveException;
}
