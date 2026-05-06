/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package java.rmi.server;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.lang.reflect.Proxy;
import java.rmi.NoSuchObjectException;
import java.rmi.Remote;
import sun.rmi.transport.ObjectTable;

public abstract class RemoteObject implements Remote, Serializable {
  protected transient RemoteRef ref;

  private static final long serialVersionUID = -3215090123894869218L;

  protected RemoteObject() {
    ref = null;
  }

  protected RemoteObject(RemoteRef newref) {
    ref = newref;
  }

  public RemoteRef getRef() {
    return ref;
  }

  public static Remote toStub(Remote obj) throws NoSuchObjectException {
    if (obj instanceof RemoteStub || (obj != null && Proxy.isProxyClass(obj.getClass())
        && Proxy.getInvocationHandler(obj) instanceof RemoteObjectInvocationHandler)) {
      return obj;
    }
    Remote stub = ObjectTable.stubFor(obj);
    if (stub == null) {
      throw new NoSuchObjectException("object not exported");
    }
    return stub;
  }

  public int hashCode() {
    return (ref == null) ? super.hashCode() : ref.remoteHashCode();
  }

  public boolean equals(Object obj) {
    if (obj instanceof RemoteObject) {
      if (ref == null) {
        return obj == this;
      } else {
        return ref.remoteEquals(((RemoteObject) obj).ref);
      }
    } else if (obj != null) {
      return obj.equals(this);
    } else {
      return false;
    }
  }

  public String toString() {
    String classname = sun.rmi.server.Util.unqualifiedName(getClass());
    return (ref == null) ? classname : classname + "[" + ref.remoteToString() + "]";
  }

  private void writeObject(ObjectOutputStream out) throws IOException, ClassNotFoundException {
    if (ref == null) {
      throw new java.rmi.MarshalException("Invalid remote object");
    } else {
      String refClassName = ref.getRefClass(out);
      if (refClassName == null || refClassName.length() == 0) {
        out.writeUTF("");
        out.writeObject(ref);
      } else {
        out.writeUTF(refClassName);
        ref.writeExternal(out);
      }
    }
  }

  private void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException {
    String refClassName = in.readUTF();
    if (refClassName == null || refClassName.length() == 0) {
      ref = (RemoteRef) in.readObject();
    } else {
      String internalRefClassName = RemoteRef.packagePrefix + "." + refClassName;
      Class<?> refClass = Class.forName(internalRefClassName);
      try {
        ref = (RemoteRef) refClass.getDeclaredConstructor().newInstance();
      } catch (ReflectiveOperationException e) {
        throw new java.rmi.UnmarshalException("Invalid remote reference class: " + internalRefClassName, e);
      }
      ref.readExternal(in);
    }
  }
}
