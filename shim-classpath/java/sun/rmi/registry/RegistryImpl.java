/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package sun.rmi.registry;

import java.net.InetAddress;
import java.rmi.AccessException;
import java.rmi.AlreadyBoundException;
import java.rmi.NotBoundException;
import java.rmi.Remote;
import java.rmi.RemoteException;
import java.rmi.registry.Registry;
import java.rmi.server.ObjID;
import java.rmi.server.RMIClientSocketFactory;
import java.rmi.server.RMIServerSocketFactory;
import java.rmi.server.RemoteServer;
import java.util.Hashtable;
import sun.rmi.server.UnicastServerRef;
import sun.rmi.transport.ObjectTable;

public class RegistryImpl extends RemoteServer implements Registry {
  private static final long serialVersionUID = 4666870661827494597L;
  public static final long INTERFACE_HASH = 4905912898345647071L;

  private final Hashtable<String, Remote> bindings = new Hashtable<String, Remote>(101);

  public RegistryImpl(int port) throws RemoteException {
    this(port, null, null);
  }

  public RegistryImpl(int port, RMIClientSocketFactory csf, RMIServerSocketFactory ssf) throws RemoteException {
    UnicastServerRef sref = new UnicastServerRef(port, csf, ssf);
    this.ref = sref;
    ObjectTable.OldDispatch old = new ObjectTable.OldDispatch() {
      public Object dispatch(Object o, int opnum, long hash, java.io.ObjectInput in) throws Exception {
        if (hash != INTERFACE_HASH) {
          throw new java.rmi.server.SkeletonMismatchException("interface hash mismatch");
        }
        RegistryImpl self = (RegistryImpl) o;
        switch (opnum) {
          case 0: {
            String name = (String) in.readObject();
            Remote obj = (Remote) in.readObject();
            self.bind(name, obj);
            return ObjectTable.OldDispatch.VOID;
          }
          case 1:
            return self.list();
          case 2:
            return self.lookup((String) in.readObject());
          case 3: {
            String name = (String) in.readObject();
            Remote obj = (Remote) in.readObject();
            self.rebind(name, obj);
            return ObjectTable.OldDispatch.VOID;
          }
          case 4:
            self.unbind((String) in.readObject());
            return ObjectTable.OldDispatch.VOID;
          default:
            throw new java.rmi.UnmarshalException("invalid method number");
        }
      }
    };
    sref.exportProxy(this, new ObjID(ObjID.REGISTRY_ID), old);
  }

  public RegistryImpl(Remote unused, ObjID id) {
  }

  public Remote lookup(String name) throws RemoteException, NotBoundException {
    synchronized (bindings) {
      Remote obj = bindings.get(name);
      if (obj == null) {
        throw new NotBoundException(name);
      }
      return obj;
    }
  }

  public void bind(String name, Remote obj) throws RemoteException, AlreadyBoundException, AccessException {
    checkAccess("Registry.bind");
    synchronized (bindings) {
      Remote curr = bindings.get(name);
      if (curr != null) {
        throw new AlreadyBoundException(name);
      }
      bindings.put(name, obj);
    }
  }

  public void unbind(String name) throws RemoteException, NotBoundException, AccessException {
    checkAccess("Registry.unbind");
    synchronized (bindings) {
      Remote obj = bindings.get(name);
      if (obj == null) {
        throw new NotBoundException(name);
      }
      bindings.remove(name);
    }
  }

  public void rebind(String name, Remote obj) throws RemoteException, AccessException {
    checkAccess("Registry.rebind");
    bindings.put(name, obj);
  }

  public String[] list() throws RemoteException {
    String[] names;
    synchronized (bindings) {
      int i = bindings.size();
      names = new String[i];
      java.util.Enumeration<String> enum_ = bindings.keys();
      while ((--i) >= 0) {
        names[i] = enum_.nextElement();
      }
    }
    return names;
  }

  public static void checkAccess(String op) throws AccessException {
    try {
      String clientHostName = getClientHost();
      InetAddress clientHost;
      try {
        clientHost = InetAddress.getByName(clientHostName);
      } catch (java.net.UnknownHostException e) {
        throw new AccessException(op + " disallowed; origin " + clientHostName + " is non-local host");
      }
      if (clientHost.isAnyLocalAddress() || clientHost.isLoopbackAddress()) {
        return;
      }
      try {
        if (clientHost.equals(InetAddress.getLocalHost())) {
          return;
        }
      } catch (java.net.UnknownHostException e) {
      }
      java.util.Enumeration<java.net.NetworkInterface> nis = java.net.NetworkInterface.getNetworkInterfaces();
      while (nis != null && nis.hasMoreElements()) {
        java.util.Enumeration<InetAddress> addrs = nis.nextElement().getInetAddresses();
        while (addrs.hasMoreElements()) {
          if (addrs.nextElement().equals(clientHost)) {
            return;
          }
        }
      }
      throw new AccessException(op + " disallowed; origin " + clientHost + " is non-local host");
    } catch (java.rmi.server.ServerNotActiveException ex) {
    } catch (java.io.IOException ex) {
      throw new AccessException(op + " disallowed; origin is unknown", ex);
    }
  }
}
