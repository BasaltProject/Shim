/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package java.rmi.registry;

import java.rmi.AccessException;
import java.rmi.AlreadyBoundException;
import java.rmi.NotBoundException;
import java.rmi.Remote;
import java.rmi.RemoteException;

public interface Registry extends Remote {
  int REGISTRY_PORT = 1099;

  Remote lookup(String name) throws RemoteException, NotBoundException, AccessException;

  void bind(String name, Remote obj) throws RemoteException, AlreadyBoundException, AccessException;

  void unbind(String name) throws RemoteException, NotBoundException, AccessException;

  void rebind(String name, Remote obj) throws RemoteException, AccessException;

  String[] list() throws RemoteException, AccessException;
}
