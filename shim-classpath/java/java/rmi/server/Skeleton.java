/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package java.rmi.server;

public interface Skeleton {
  void dispatch(java.rmi.Remote remote, java.rmi.server.RemoteCall remoteCall, int i, long l) throws java.lang.Exception;
  java.rmi.server.Operation[] getOperations();
}
