/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package javax.management.remote.rmi;

import java.io.Closeable;
import java.io.IOException;
import java.rmi.Remote;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import javax.management.MBeanServer;
import javax.management.remote.JMXAuthenticator;
import javax.management.remote.JMXConnectorServer;
import javax.security.auth.Subject;

public abstract class RMIServerImpl implements Closeable, RMIServer {
  private final List<WeakRef> clientList = new ArrayList<WeakRef>();
  private ClassLoader cl;
  private MBeanServer mbeanServer;
  private final Map<String, ?> env;
  private RMIConnectorServer connectorServer;
  private static int clientId = 0;

  private static final class WeakRef {
    final java.lang.ref.WeakReference<RMIConnection> ref;

    WeakRef(RMIConnection c) {
      ref = new java.lang.ref.WeakReference<RMIConnection>(c);
    }
  }

  public RMIServerImpl(Map<String, ?> env) {
    this.env = env == null ? java.util.Collections.<String, Object>emptyMap() : env;
  }

  void setRMIConnectorServer(RMIConnectorServer connServer) throws IOException {
    this.connectorServer = connServer;
  }

  protected abstract void export() throws IOException;

  public abstract Remote toStub() throws IOException;

  public synchronized void setDefaultClassLoader(ClassLoader cl) {
    this.cl = cl;
  }

  public synchronized ClassLoader getDefaultClassLoader() {
    return cl;
  }

  public synchronized void setMBeanServer(MBeanServer pmbs) {
    mbeanServer = pmbs;
  }

  public synchronized MBeanServer getMBeanServer() {
    return mbeanServer;
  }

  public String getVersion() {
    return "1.0 java_runtime_25";
  }

  public RMIConnection newClient(Object credentials) throws IOException {
    return doNewClient(credentials);
  }

  RMIConnection doNewClient(Object credentials) throws IOException {
    final boolean tracing = false;
    if (getMBeanServer() == null) {
      throw new IllegalStateException("Not attached to an MBean server");
    }
    Subject subject = null;
    JMXAuthenticator authenticator = (JMXAuthenticator) env.get(JMXConnectorServer.AUTHENTICATOR);
    if (authenticator == null) {
      if (env.get("jmx.remote.x.password.file") != null || env.get("jmx.remote.x.login.config") != null) {
        throw new SecurityException("Authentication not supported without an authenticator");
      }
    } else {
      subject = authenticator.authenticate(credentials);
    }
    String clientHost;
    try {
      clientHost = java.rmi.server.RemoteServer.getClientHost();
    } catch (java.rmi.server.ServerNotActiveException e) {
      clientHost = "";
    }
    final String connectionId = makeConnectionId(getProtocol(), clientHost, subject);
    RMIConnection client = makeClient(connectionId, subject);
    dropDeadReferences();
    synchronized (clientList) {
      clientList.add(new WeakRef(client));
    }
    if (connectorServer != null) {
      connectorServer.connectionOpened(connectionId, "Connection opened", null);
    }
    return client;
  }

  private static synchronized String makeConnectionId(String protocol, String clientHost, Subject subject) {
    StringBuilder buf = new StringBuilder();
    buf.append(protocol).append(":");
    if (clientHost.length() > 0) {
      buf.append("//").append(clientHost);
    }
    buf.append(" ");
    if (subject != null) {
      boolean first = true;
      for (java.security.Principal p : subject.getPrincipals()) {
        if (!first) {
          buf.append(";");
        }
        buf.append(p.getName().replace(' ', '_').replace(';', ':'));
        first = false;
      }
    }
    buf.append(" ");
    buf.append(clientId++);
    return buf.toString();
  }

  protected void clientClosed(RMIConnection client) throws IOException {
    if (client == null) {
      throw new NullPointerException("Null client");
    }
    synchronized (clientList) {
      dropDeadReferences();
      for (java.util.Iterator<WeakRef> it = clientList.iterator(); it.hasNext();) {
        if (it.next().ref.get() == client) {
          it.remove();
          break;
        }
      }
    }
    if (connectorServer != null) {
      connectorServer.connectionClosed(client.getConnectionId(), "Connection closed", null);
    }
  }

  private void dropDeadReferences() {
    synchronized (clientList) {
      for (java.util.Iterator<WeakRef> it = clientList.iterator(); it.hasNext();) {
        if (it.next().ref.get() == null) {
          it.remove();
        }
      }
    }
  }

  protected abstract String getProtocol();

  protected abstract RMIConnection makeClient(String connectionId, Subject subject) throws IOException;

  protected abstract void closeClient(RMIConnection client) throws IOException;

  protected abstract void closeServer() throws IOException;

  public synchronized void close() throws IOException {
    IOException ioException = null;
    try {
      closeServer();
    } catch (IOException e) {
      ioException = e;
    }
    List<RMIConnection> clients = new ArrayList<RMIConnection>();
    synchronized (clientList) {
      for (WeakRef w : clientList) {
        RMIConnection c = w.ref.get();
        if (c != null) {
          clients.add(c);
        }
      }
      clientList.clear();
    }
    for (RMIConnection c : clients) {
      try {
        c.close();
      } catch (IOException e) {
        if (ioException == null) {
          ioException = e;
        }
      }
    }
    if (ioException != null) {
      throw ioException;
    }
  }
}
