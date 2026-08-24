/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package javax.management.remote.rmi;

public interface RMIServer extends java.rmi.Remote {
  java.lang.String getVersion() throws java.rmi.RemoteException;
  javax.management.remote.rmi.RMIConnection newClient(java.lang.Object object) throws java.io.IOException;
}
