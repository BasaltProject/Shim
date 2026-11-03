/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package sun.rmi.transport;

import java.lang.reflect.Method;
import java.rmi.NoSuchObjectException;
import java.rmi.Remote;
import java.rmi.server.ObjID;
import java.util.HashMap;
import java.util.IdentityHashMap;
import java.util.Map;
import sun.rmi.server.Util;

public final class ObjectTable {
  private ObjectTable() {
  }

  public interface OldDispatch {
    Object VOID = new Object();

    Object dispatch(Object impl, int opnum, long hash, java.io.ObjectInput in) throws Exception;
  }

  public static final class Target {
    public final ObjID id;
    public final Object impl;
    public final Remote stub;
    public final Map<Long, Method> methods;
    public final OldDispatch oldDispatch;
    public final int listenPort;
    public final Object listenKey;

    Target(ObjID id, Object impl, Remote stub, Map<Long, Method> methods, OldDispatch old, int listenPort,
        Object listenKey) {
      this.id = id;
      this.impl = impl;
      this.stub = stub;
      this.methods = methods;
      this.oldDispatch = old;
      this.listenPort = listenPort;
      this.listenKey = listenKey;
    }
  }

  private static final Map<ObjID, Target> byId = new HashMap<ObjID, Target>();
  private static final Map<Object, Target> byImpl = new IdentityHashMap<Object, Target>();

  static {
    DGCImpl.install();
  }

  public static synchronized Target get(ObjID id) {
    return byId.get(id);
  }

  public static synchronized Remote stubFor(Remote impl) {
    Target t = byImpl.get(impl);
    return t == null ? null : t.stub;
  }

  public static synchronized Target targetFor(Object impl) {
    return byImpl.get(impl);
  }

  public static synchronized void put(ObjID id, Object impl, Remote stub, OldDispatch old, int port, Object key)
      throws java.rmi.server.ExportException {
    if (byId.containsKey(id)) {
      throw new java.rmi.server.ExportException("internal error: ObjID already in use");
    }
    if (byImpl.containsKey(impl)) {
      throw new java.rmi.server.ExportException("object already exported");
    }
    Target t = new Target(id, impl, stub, Util.methodsByHash(impl.getClass()), old, port, key);
    byId.put(id, t);
    byImpl.put(impl, t);
  }

  public static Target remove(Object impl) throws NoSuchObjectException {
    Target t;
    synchronized (ObjectTable.class) {
      t = byImpl.remove(impl);
      if (t == null) {
        throw new NoSuchObjectException("object not exported");
      }
      byId.remove(t.id);
    }
    TCPTransport.release(t.listenPort, t.listenKey);
    return t;
  }
}
