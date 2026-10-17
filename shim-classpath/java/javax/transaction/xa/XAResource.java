/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package javax.transaction.xa;

public interface XAResource {
  int TMENDRSCAN = 8388608;
  int TMFAIL = 536870912;
  int TMJOIN = 2097152;
  int TMNOFLAGS = 0;
  int TMONEPHASE = 1073741824;
  int TMRESUME = 134217728;
  int TMSTARTRSCAN = 16777216;
  int TMSUCCESS = 67108864;
  int TMSUSPEND = 33554432;
  int XA_RDONLY = 3;
  int XA_OK = 0;

  void commit(Xid p0, boolean p1) throws XAException;

  void end(Xid p0, int p1) throws XAException;

  void forget(Xid p0) throws XAException;

  int getTransactionTimeout() throws XAException;

  boolean isSameRM(XAResource p0) throws XAException;

  int prepare(Xid p0) throws XAException;

  Xid[] recover(int p0) throws XAException;

  void rollback(Xid p0) throws XAException;

  boolean setTransactionTimeout(int p0) throws XAException;

  void start(Xid p0, int p1) throws XAException;
}
