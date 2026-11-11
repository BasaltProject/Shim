/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 * SPDX-License-Identifier: MIT
 */

package com.sun.jndi.rmi.registry;

import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import javax.naming.NamingException;
import javax.naming.Reference;

public class ReferenceWrapper extends UnicastRemoteObject implements RemoteReference {
  private static final long serialVersionUID = 6078186197417641456L;

  protected Reference wrappee;

  public ReferenceWrapper(Reference wrappee) throws NamingException, RemoteException {
    this.wrappee = wrappee;
  }

  @Override
  public Reference getReference() throws RemoteException {
    return wrappee;
  }
}
