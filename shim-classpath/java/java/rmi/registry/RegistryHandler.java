/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package java.rmi.registry;

public interface RegistryHandler {
  java.rmi.registry.Registry registryImpl(int i) throws java.rmi.RemoteException;
  java.rmi.registry.Registry registryStub(java.lang.String string, int i) throws java.rmi.RemoteException, java.rmi.UnknownHostException;
}
