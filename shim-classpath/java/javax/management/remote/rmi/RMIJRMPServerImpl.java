/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package javax.management.remote.rmi;

import java.io.IOException;
import java.rmi.NoSuchObjectException;
import java.rmi.Remote;
import java.rmi.server.RMIClientSocketFactory;
import java.rmi.server.RMIServerSocketFactory;
import java.rmi.server.RemoteObject;
import java.rmi.server.UnicastRemoteObject;
import java.util.Map;
import javax.security.auth.Subject;

public class RMIJRMPServerImpl extends RMIServerImpl {
  private final int port;
  private final RMIClientSocketFactory csf;
  private final RMIServerSocketFactory ssf;
  private final Map<String, ?> env;

  public RMIJRMPServerImpl(int port, RMIClientSocketFactory csf, RMIServerSocketFactory ssf, Map<String, ?> env)
      throws IOException {
    super(env);
    if (port < 0) {
      throw new IllegalArgumentException("Negative port: " + port);
    }
    this.port = port;
    this.csf = csf;
    this.ssf = ssf;
    this.env = (env == null) ? java.util.Collections.<String, Object>emptyMap() : env;
  }

  protected void export() throws IOException {
    exportObject(this);
  }

  private void exportObject(Remote obj) throws IOException {
    if (csf == null && ssf == null) {
      UnicastRemoteObject.exportObject(obj, port);
    } else {
      UnicastRemoteObject.exportObject(obj, port, csf, ssf);
    }
  }

  private void unexport(Remote obj, boolean force) throws NoSuchObjectException {
    UnicastRemoteObject.unexportObject(obj, force);
  }

  protected String getProtocol() {
    return "rmi";
  }

  public Remote toStub() throws IOException {
    return RemoteObject.toStub(this);
  }

  protected RMIConnection makeClient(String connectionId, Subject subject) throws IOException {
    if (connectionId == null) {
      throw new NullPointerException("Null connectionId");
    }
    RMIConnection client = new RMIConnectionImpl(this, connectionId, getDefaultClassLoader(), subject, env);
    exportObject(client);
    return client;
  }

  protected void closeClient(RMIConnection client) throws IOException {
    unexport(client, true);
  }

  protected void closeServer() throws IOException {
    unexport(this, true);
  }
}
