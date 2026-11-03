/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package java.rmi.dgc;

public interface DGC extends java.rmi.Remote {
  void clean(java.rmi.server.ObjID[] objIDs, long l, java.rmi.dgc.VMID vMID, boolean flag) throws java.rmi.RemoteException;
  java.rmi.dgc.Lease dirty(java.rmi.server.ObjID[] objIDs, long l, java.rmi.dgc.Lease lease) throws java.rmi.RemoteException;
}
