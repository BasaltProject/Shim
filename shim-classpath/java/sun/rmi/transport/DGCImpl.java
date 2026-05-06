/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package sun.rmi.transport;

import java.rmi.RemoteException;
import java.rmi.dgc.DGC;
import java.rmi.dgc.Lease;
import java.rmi.dgc.VMID;
import java.rmi.server.ObjID;

final class DGCImpl implements DGC {
  private static final long LEASE_VALUE = Long.getLong("java.rmi.dgc.leaseValue", 600000L);
  private static final long HASH_INTERFACE = -669196253586618813L;

  static void install() {
    try {
      DGCImpl impl = new DGCImpl();
      ObjectTable.OldDispatch old = new ObjectTable.OldDispatch() {
        public Object dispatch(Object o, int opnum, long hash, java.io.ObjectInput in) throws Exception {
          if (hash != HASH_INTERFACE) {
            throw new java.rmi.server.SkeletonMismatchException("interface hash mismatch");
          }
          DGCImpl self = (DGCImpl) o;
          switch (opnum) {
            case 0: {
              ObjID[] ids = (ObjID[]) in.readObject();
              long seq = in.readLong();
              VMID vmid = (VMID) in.readObject();
              boolean strong = in.readBoolean();
              self.clean(ids, seq, vmid, strong);
              return ObjectTable.OldDispatch.VOID;
            }
            case 1: {
              ObjID[] ids = (ObjID[]) in.readObject();
              long seq = in.readLong();
              Lease lease = (Lease) in.readObject();
              return self.dirty(ids, seq, lease);
            }
            default:
              throw new java.rmi.UnmarshalException("invalid method number");
          }
        }
      };
      ObjectTable.put(new ObjID(ObjID.DGC_ID), impl, null, old, 0, null);
    } catch (RemoteException e) {
      throw new IllegalStateException(e);
    }
  }

  public Lease dirty(ObjID[] ids, long sequenceNum, Lease lease) {
    VMID vmid = lease.getVMID();
    if (vmid == null) {
      vmid = new VMID();
    }
    return new Lease(vmid, LEASE_VALUE);
  }

  public void clean(ObjID[] ids, long sequenceNum, VMID vmid, boolean strong) {
  }
}
