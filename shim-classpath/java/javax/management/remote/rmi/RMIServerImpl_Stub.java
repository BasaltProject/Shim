/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package javax.management.remote.rmi;

public final class RMIServerImpl_Stub extends java.rmi.server.RemoteStub implements javax.management.remote.rmi.RMIServer {
  private static final long serialVersionUID = 2L;


  public RMIServerImpl_Stub(java.rmi.server.RemoteRef remoteRef) {
    super(remoteRef);
  }

  public java.lang.String getVersion() throws java.rmi.RemoteException {
    try {
      return (java.lang.String) call("getVersion", new Class<?>[] {}, new Object[] {});
    } catch (Exception e) {
      if (e instanceof RuntimeException) {
        throw (RuntimeException) e;
      }
      if (e instanceof java.rmi.RemoteException) {
        throw (java.rmi.RemoteException) e;
      }
      throw new java.rmi.UnexpectedException("undeclared checked exception", e);
    }
  }
  public javax.management.remote.rmi.RMIConnection newClient(java.lang.Object object) throws java.io.IOException {
    try {
      return (javax.management.remote.rmi.RMIConnection) call("newClient", new Class<?>[] {java.lang.Object.class}, new Object[] {object});
    } catch (Exception e) {
      if (e instanceof RuntimeException) {
        throw (RuntimeException) e;
      }
      if (e instanceof java.io.IOException) {
        throw (java.io.IOException) e;
      }
      throw new java.rmi.UnexpectedException("undeclared checked exception", e);
    }
  }

  private Object call(String name, Class<?>[] types, Object[] args) throws Exception {
    java.lang.reflect.Method m = RMIServer.class.getMethod(name, types);
    return ref.invoke(this, m, args, sun.rmi.server.Util.computeMethodHash(m));
  }
}
