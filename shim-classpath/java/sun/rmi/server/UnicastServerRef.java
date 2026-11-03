/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package sun.rmi.server;

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.rmi.server.ExportException;
import java.rmi.server.ObjID;
import java.rmi.server.RMIClientSocketFactory;
import java.rmi.server.RMIServerSocketFactory;
import java.rmi.server.RemoteStub;
import java.rmi.server.ServerNotActiveException;
import java.rmi.server.ServerRef;
import sun.rmi.transport.ObjectTable;
import sun.rmi.transport.TCPTransport;

public class UnicastServerRef extends UnicastRef implements ServerRef {
  private static final long serialVersionUID = -7384275867073752268L;

  protected transient RMIServerSocketFactory ssf;
  private transient int requestedPort;
  private transient boolean forceStubUse;

  public UnicastServerRef() {
  }

  public UnicastServerRef(int port) {
    this(port, null, null);
  }

  public UnicastServerRef(boolean forceStubUse) {
    this(0, null, null);
    this.forceStubUse = forceStubUse;
  }

  public UnicastServerRef(int port, RMIClientSocketFactory csf, RMIServerSocketFactory ssf) {
    super(null, port, null, csf);
    this.ssf = ssf;
    this.requestedPort = port;
  }

  public RemoteStub exportObject(Remote obj, Object data) throws RemoteException {
    throw new java.rmi.StubNotFoundException("Stub class not found: " + obj.getClass().getName() + "_Stub");
  }

  public Remote exportProxy(Remote obj, ObjID id, ObjectTable.OldDispatch old) throws RemoteException {
    Object[] key = new Object[1];
    int actual = TCPTransport.listen(requestedPort, ssf, key);
    boolean extended = csf != null || ssf != null;
    String h = TCPTransport.localHost();
    UnicastRef ref = extended ? new UnicastRef2(h, actual, id, csf) : new UnicastRef(h, actual, id, null);
    ref.setLocal(true);
    Remote stub;
    try {
      stub = Util.createStub(obj.getClass(), ref);
      ObjectTable.put(id, obj, stub, old, actual, key[0]);
    } catch (RemoteException e) {
      TCPTransport.release(actual, key[0]);
      throw e;
    }
    this.host = h;
    this.port = actual;
    this.objID = id;
    return stub;
  }

  public String getClientHost() throws ServerNotActiveException {
    String h = TCPTransport.currentClientHost();
    if (h == null) {
      throw new ServerNotActiveException("not in a remote call");
    }
    return h;
  }

  @Override
  public String getRefClass(java.io.ObjectOutput out) {
    return "UnicastServerRef";
  }
}
