/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package java.rmi.registry;

import java.net.InetAddress;
import java.rmi.RemoteException;
import java.rmi.server.ObjID;
import java.rmi.server.RMIClientSocketFactory;
import java.rmi.server.RMIServerSocketFactory;
import sun.rmi.registry.RegistryImpl;
import sun.rmi.registry.RegistryImpl_Stub;
import sun.rmi.server.UnicastRef;
import sun.rmi.server.UnicastRef2;

public final class LocateRegistry {
  private LocateRegistry() {
  }

  public static Registry getRegistry() throws RemoteException {
    return getRegistry(null, Registry.REGISTRY_PORT);
  }

  public static Registry getRegistry(int port) throws RemoteException {
    return getRegistry(null, port);
  }

  public static Registry getRegistry(String host) throws RemoteException {
    return getRegistry(host, Registry.REGISTRY_PORT);
  }

  public static Registry getRegistry(String host, int port) throws RemoteException {
    return getRegistry(host, port, null);
  }

  public static Registry getRegistry(String host, int port, RMIClientSocketFactory csf) throws RemoteException {
    Registry registry = null;
    if (port <= 0) {
      port = Registry.REGISTRY_PORT;
    }
    if (host == null || host.length() == 0) {
      try {
        host = InetAddress.getLocalHost().getHostAddress();
      } catch (Exception e) {
        host = "";
      }
    }
    ObjID id = new ObjID(ObjID.REGISTRY_ID);
    UnicastRef ref = csf == null ? new UnicastRef(host, port, id, null) : new UnicastRef2(host, port, id, csf);
    return new RegistryImpl_Stub(ref);
  }

  public static Registry createRegistry(int port) throws RemoteException {
    return new RegistryImpl(port);
  }

  public static Registry createRegistry(int port, RMIClientSocketFactory csf, RMIServerSocketFactory ssf)
      throws RemoteException {
    return new RegistryImpl(port, csf, ssf);
  }
}
