/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 * SPDX-License-Identifier: MIT
 */

package com.sun.jndi.rmi.registry;

import java.rmi.Remote;
import java.rmi.RemoteException;
import javax.naming.NamingException;

public interface RemoteReference extends Remote {
  javax.naming.Reference getReference() throws NamingException, RemoteException;
}
