/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package java.rmi.server;

import java.io.IOException;
import java.io.ObjectInputFilter;
import java.io.ObjectInputStream;
import java.rmi.NoSuchObjectException;
import java.rmi.Remote;
import java.rmi.RemoteException;
import sun.rmi.server.UnicastServerRef;
import sun.rmi.transport.ObjectTable;

public class UnicastRemoteObject extends RemoteServer {
  private static final long serialVersionUID = 4974527148936298033L;

  private int port = 0;
  private RMIClientSocketFactory csf = null;
  private RMIServerSocketFactory ssf = null;

  protected UnicastRemoteObject() throws RemoteException {
    this(0);
  }

  protected UnicastRemoteObject(int port) throws RemoteException {
    this.port = port;
    exportObject((Remote) this, port);
  }

  protected UnicastRemoteObject(int port, RMIClientSocketFactory csf, RMIServerSocketFactory ssf)
      throws RemoteException {
    this.port = port;
    this.csf = csf;
    this.ssf = ssf;
    exportObject((Remote) this, port, csf, ssf);
  }

  private void readObject(ObjectInputStream in) throws java.io.IOException, ClassNotFoundException {
    in.defaultReadObject();
    reexport();
  }

  public Object clone() throws CloneNotSupportedException {
    try {
      UnicastRemoteObject cloned = (UnicastRemoteObject) super.clone();
      cloned.reexport();
      return cloned;
    } catch (RemoteException e) {
      throw new ServerCloneException("Clone failed", e);
    }
  }

  private void reexport() throws RemoteException {
    if (csf == null && ssf == null) {
      exportObject((Remote) this, port);
    } else {
      exportObject((Remote) this, port, csf, ssf);
    }
  }

  @Deprecated
  public static RemoteStub exportObject(Remote obj) throws RemoteException {
    return (RemoteStub) exportObject(obj, new UnicastServerRef(true));
  }

  public static Remote exportObject(Remote obj, int port) throws RemoteException {
    return exportObject(obj, new UnicastServerRef(port));
  }

  public static Remote exportObject(Remote obj, int port, ObjectInputFilter filter) throws RemoteException {
    return exportObject(obj, new UnicastServerRef(port));
  }

  public static Remote exportObject(Remote obj, int port, RMIClientSocketFactory csf, RMIServerSocketFactory ssf)
      throws RemoteException {
    return exportObject(obj, new UnicastServerRef(port, csf, ssf));
  }

  public static Remote exportObject(Remote obj, int port, RMIClientSocketFactory csf, RMIServerSocketFactory ssf,
      ObjectInputFilter filter) throws RemoteException {
    return exportObject(obj, new UnicastServerRef(port, csf, ssf));
  }

  public static boolean unexportObject(Remote obj, boolean force) throws NoSuchObjectException {
    if (obj == null) {
      throw new NullPointerException("obj");
    }
    ObjectTable.remove(obj);
    return true;
  }

  private static Remote exportObject(Remote obj, UnicastServerRef sref) throws RemoteException {
    if (obj instanceof UnicastRemoteObject) {
      ((UnicastRemoteObject) obj).ref = sref;
    }
    return sref.exportProxy(obj, new ObjID(), null);
  }
}
