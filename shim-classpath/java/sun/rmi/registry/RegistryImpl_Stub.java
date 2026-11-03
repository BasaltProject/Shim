/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package sun.rmi.registry;

import java.rmi.AccessException;
import java.rmi.AlreadyBoundException;
import java.rmi.NotBoundException;
import java.rmi.Remote;
import java.rmi.RemoteException;
import java.rmi.registry.Registry;
import java.rmi.server.Operation;
import java.rmi.server.RemoteCall;
import java.rmi.server.RemoteRef;
import java.rmi.server.RemoteStub;

public final class RegistryImpl_Stub extends RemoteStub implements Registry, Remote {
  private static final long serialVersionUID = 2L;
  private static final long interfaceHash = 4905912898345647071L;

  private static final Operation[] operations = {new Operation("void bind(java.lang.String, java.rmi.Remote)"),
      new Operation("java.lang.String list()[]"), new Operation("java.rmi.Remote lookup(java.lang.String)"),
      new Operation("void rebind(java.lang.String, java.rmi.Remote)"), new Operation("void unbind(java.lang.String)")};

  public RegistryImpl_Stub() {
    super();
  }

  public RegistryImpl_Stub(RemoteRef ref) {
    super(ref);
  }

  public void bind(String name, Remote obj) throws RemoteException, AlreadyBoundException, AccessException {
    try {
      RemoteCall call = ref.newCall(this, operations, 0, interfaceHash);
      try {
        java.io.ObjectOutput out = call.getOutputStream();
        out.writeObject(name);
        out.writeObject(obj);
      } catch (java.io.IOException e) {
        throw new java.rmi.MarshalException("error marshalling arguments", e);
      }
      ref.invoke(call);
      ref.done(call);
    } catch (RuntimeException e) {
      throw e;
    } catch (RemoteException e) {
      throw e;
    } catch (AlreadyBoundException e) {
      throw e;
    } catch (Exception e) {
      throw new java.rmi.UnexpectedException("undeclared checked exception", e);
    }
  }

  public String[] list() throws RemoteException, AccessException {
    try {
      RemoteCall call = ref.newCall(this, operations, 1, interfaceHash);
      ref.invoke(call);
      String[] result;
      try {
        java.io.ObjectInput in = call.getInputStream();
        result = (String[]) in.readObject();
      } catch (java.io.IOException e) {
        throw new java.rmi.UnmarshalException("error unmarshalling return", e);
      } catch (ClassNotFoundException e) {
        throw new java.rmi.UnmarshalException("error unmarshalling return", e);
      } finally {
        ref.done(call);
      }
      return result;
    } catch (RuntimeException e) {
      throw e;
    } catch (RemoteException e) {
      throw e;
    } catch (Exception e) {
      throw new java.rmi.UnexpectedException("undeclared checked exception", e);
    }
  }

  public Remote lookup(String name) throws RemoteException, NotBoundException, AccessException {
    try {
      RemoteCall call = ref.newCall(this, operations, 2, interfaceHash);
      try {
        java.io.ObjectOutput out = call.getOutputStream();
        out.writeObject(name);
      } catch (java.io.IOException e) {
        throw new java.rmi.MarshalException("error marshalling arguments", e);
      }
      ref.invoke(call);
      Remote result;
      try {
        java.io.ObjectInput in = call.getInputStream();
        result = (Remote) in.readObject();
      } catch (java.io.IOException e) {
        throw new java.rmi.UnmarshalException("error unmarshalling return", e);
      } catch (ClassNotFoundException e) {
        throw new java.rmi.UnmarshalException("error unmarshalling return", e);
      } finally {
        ref.done(call);
      }
      return result;
    } catch (RuntimeException e) {
      throw e;
    } catch (RemoteException e) {
      throw e;
    } catch (NotBoundException e) {
      throw e;
    } catch (Exception e) {
      throw new java.rmi.UnexpectedException("undeclared checked exception", e);
    }
  }

  public void rebind(String name, Remote obj) throws RemoteException, AccessException {
    try {
      RemoteCall call = ref.newCall(this, operations, 3, interfaceHash);
      try {
        java.io.ObjectOutput out = call.getOutputStream();
        out.writeObject(name);
        out.writeObject(obj);
      } catch (java.io.IOException e) {
        throw new java.rmi.MarshalException("error marshalling arguments", e);
      }
      ref.invoke(call);
      ref.done(call);
    } catch (RuntimeException e) {
      throw e;
    } catch (RemoteException e) {
      throw e;
    } catch (Exception e) {
      throw new java.rmi.UnexpectedException("undeclared checked exception", e);
    }
  }

  public void unbind(String name) throws RemoteException, NotBoundException, AccessException {
    try {
      RemoteCall call = ref.newCall(this, operations, 4, interfaceHash);
      try {
        java.io.ObjectOutput out = call.getOutputStream();
        out.writeObject(name);
      } catch (java.io.IOException e) {
        throw new java.rmi.MarshalException("error marshalling arguments", e);
      }
      ref.invoke(call);
      ref.done(call);
    } catch (RuntimeException e) {
      throw e;
    } catch (RemoteException e) {
      throw e;
    } catch (NotBoundException e) {
      throw e;
    } catch (Exception e) {
      throw new java.rmi.UnexpectedException("undeclared checked exception", e);
    }
  }
}
