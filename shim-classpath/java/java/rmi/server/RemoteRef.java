/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package java.rmi.server;

public interface RemoteRef extends java.io.Externalizable {
  long serialVersionUID = 3632638527362204081L;
  java.lang.String packagePrefix = "sun.rmi.server";

  void done(java.rmi.server.RemoteCall remoteCall) throws java.rmi.RemoteException;
  java.lang.String getRefClass(java.io.ObjectOutput objectOutput);
  java.lang.Object invoke(java.rmi.Remote remote, java.lang.reflect.Method method, java.lang.Object[] objects, long l) throws java.lang.Exception;
  void invoke(java.rmi.server.RemoteCall remoteCall) throws java.lang.Exception;
  java.rmi.server.RemoteCall newCall(java.rmi.server.RemoteObject remoteObject, java.rmi.server.Operation[] operations, int i, long l) throws java.rmi.RemoteException;
  boolean remoteEquals(java.rmi.server.RemoteRef remoteRef);
  int remoteHashCode();
  java.lang.String remoteToString();
}
