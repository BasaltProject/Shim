/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package javax.management.remote.rmi;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.net.MalformedURLException;
import java.rmi.Remote;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.RMIClientSocketFactory;
import java.rmi.server.RMIServerSocketFactory;
import java.util.Base64;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import javax.management.MBeanServer;
import javax.management.remote.JMXConnector;
import javax.management.remote.JMXConnectorServer;
import javax.management.remote.JMXServiceURL;

public class RMIConnectorServer extends JMXConnectorServer {
  public static final String JNDI_REBIND_ATTRIBUTE = "jmx.remote.jndi.rebind";
  public static final String RMI_CLIENT_SOCKET_FACTORY_ATTRIBUTE = "jmx.remote.rmi.client.socket.factory";
  public static final String RMI_SERVER_SOCKET_FACTORY_ATTRIBUTE = "jmx.remote.rmi.server.socket.factory";
  public static final String CREDENTIALS_FILTER_PATTERN = "jmx.remote.rmi.server.credentials.filter.pattern";
  public static final String SERIAL_FILTER_PATTERN = "jmx.remote.rmi.server.serial.filter.pattern";

  private static final int CREATED = 0;
  private static final int STARTED = 1;
  private static final int STOPPED = 2;

  private JMXServiceURL address;
  private RMIServerImpl rmiServerImpl;
  private final Map<String, ?> attributes;
  private ClassLoader defaultClassLoader = null;
  private String boundJndiUrl;
  private int state = CREATED;

  public RMIConnectorServer(JMXServiceURL url, Map<String, ?> environment) throws IOException {
    this(url, environment, (MBeanServer) null);
  }

  public RMIConnectorServer(JMXServiceURL url, Map<String, ?> environment, MBeanServer mbeanServer)
      throws IOException {
    this(url, environment, (RMIServerImpl) null, mbeanServer);
  }

  public RMIConnectorServer(JMXServiceURL url, Map<String, ?> environment, RMIServerImpl rmiServerImpl,
      MBeanServer mbeanServer) throws IOException {
    super(mbeanServer);
    if (url == null) {
      throw new IllegalArgumentException("Null JMXServiceURL");
    }
    if (rmiServerImpl == null) {
      final String prt = url.getProtocol();
      if (prt == null || !(prt.equals("rmi") || prt.equals("iiop"))) {
        final String msg = "Invalid protocol type: " + prt;
        throw new MalformedURLException(msg);
      }
      final String urlPath = url.getURLPath();
      if (!urlPath.equals("") && !urlPath.equals("/") && !urlPath.startsWith("/jndi/")) {
        final String msg = "URL path must be empty or start with /jndi/";
        throw new MalformedURLException(msg);
      }
    }
    if (environment == null) {
      this.attributes = Collections.emptyMap();
    } else {
      Map<String, Object> copy = new HashMap<String, Object>(environment);
      this.attributes = Collections.unmodifiableMap(copy);
    }
    this.address = url;
    this.rmiServerImpl = rmiServerImpl;
  }

  public JMXConnector toJMXConnector(Map<String, ?> env) throws IOException {
    if (!isActive()) {
      throw new IllegalStateException("Connector is not active");
    }
    Map<String, Object> usemap = new HashMap<String, Object>(this.attributes == null ? Collections.<String, Object>emptyMap() : this.attributes);
    if (env != null) {
      usemap.putAll(env);
    }
    RMIConnector c = new RMIConnector(address, usemap);
    return c;
  }

  public synchronized void start() throws IOException {
    if (state == STARTED) {
      return;
    } else if (state == STOPPED) {
      throw new IOException("The server has been stopped.");
    }
    if (getMBeanServer() == null) {
      throw new IllegalStateException("This connector server is not attached to an MBean server");
    }
    if (attributes != null) {
      String accessFile = (String) attributes.get("jmx.remote.x.access.file");
      if (accessFile != null && attributes.get(AUTHENTICATOR) != null) {
        throw new IllegalArgumentException("Cannot specify both an authenticator and an access file");
      }
    }
    try {
      if (rmiServerImpl == null) {
        rmiServerImpl = newServer();
      }
      rmiServerImpl.setMBeanServer(getMBeanServer());
      rmiServerImpl.setDefaultClassLoader(defaultClassLoader);
      rmiServerImpl.setRMIConnectorServer(this);
      rmiServerImpl.export();
      final Remote stub = rmiServerImpl.toStub();
      final String urlPath = address.getURLPath();
      if (urlPath.startsWith("/jndi/")) {
        bind(urlPath.substring("/jndi/".length()), stub);
        boundJndiUrl = urlPath.substring("/jndi/".length());
      } else {
        address = encodeStub(address, stub);
      }
      state = STARTED;
    } catch (IOException e) {
      try {
        if (rmiServerImpl != null) {
          rmiServerImpl.close();
        }
      } catch (IOException ex) {
      }
      throw e;
    }
  }

  private void bind(String jndiUrl, Remote stub) throws IOException {
    java.net.URI uri;
    try {
      uri = new java.net.URI(jndiUrl);
    } catch (java.net.URISyntaxException e) {
      throw new MalformedURLException("Invalid JNDI URL: " + jndiUrl);
    }
    String name = uri.getPath();
    if (name != null && name.startsWith("/")) {
      name = name.substring(1);
    }
    int port = uri.getPort();
    String host = uri.getHost();
    Registry registry = LocateRegistry.getRegistry(host == null ? "localhost" : host, port <= 0 ? Registry.REGISTRY_PORT : port);
    boolean rebind = Boolean.parseBoolean(String.valueOf(attributes.get(JNDI_REBIND_ATTRIBUTE)));
    try {
      if (rebind) {
        registry.rebind(name, stub);
      } else {
        registry.bind(name, stub);
      }
    } catch (java.rmi.AlreadyBoundException e) {
      throw new IOException("Cannot bind to URL [" + jndiUrl + "]: " + e);
    }
  }

  private JMXServiceURL encodeStub(JMXServiceURL base, Remote stub) throws IOException {
    ByteArrayOutputStream bout = new ByteArrayOutputStream();
    ObjectOutputStream oout = new ObjectOutputStream(bout);
    oout.writeObject(stub);
    oout.close();
    String path = "/stub/" + Base64.getUrlEncoder().encodeToString(bout.toByteArray());
    String host = base.getHost();
    if (host == null || host.length() == 0) {
      host = java.net.InetAddress.getLocalHost().getHostAddress();
    }
    return new JMXServiceURL(base.getProtocol(), host, base.getPort(), path);
  }

  private RMIServerImpl newServer() throws IOException {
    final int port = address.getPort() < 0 ? 0 : address.getPort();
    RMIClientSocketFactory csf = (RMIClientSocketFactory) attributes.get(RMI_CLIENT_SOCKET_FACTORY_ATTRIBUTE);
    RMIServerSocketFactory ssf = (RMIServerSocketFactory) attributes.get(RMI_SERVER_SOCKET_FACTORY_ATTRIBUTE);
    return new RMIJRMPServerImpl(0, csf, ssf, attributes);
  }

  public synchronized void stop() throws IOException {
    if (state == STOPPED) {
      return;
    }
    state = STOPPED;
    IOException exception = null;
    if (boundJndiUrl != null) {
      try {
        java.net.URI uri = new java.net.URI(boundJndiUrl);
        String name = uri.getPath().startsWith("/") ? uri.getPath().substring(1) : uri.getPath();
        int port = uri.getPort();
        Registry registry = LocateRegistry.getRegistry(uri.getHost() == null ? "localhost" : uri.getHost(),
            port <= 0 ? Registry.REGISTRY_PORT : port);
        registry.unbind(name);
      } catch (Exception e) {
      }
    }
    if (rmiServerImpl != null) {
      try {
        rmiServerImpl.close();
      } catch (IOException e) {
        exception = e;
      }
    }
    if (exception != null) {
      throw exception;
    }
  }

  public synchronized boolean isActive() {
    return state == STARTED;
  }

  public JMXServiceURL getAddress() {
    if (!isActive()) {
      return null;
    }
    return address;
  }

  public Map<String, ?> getAttributes() {
    Map<String, Object> map = new HashMap<String, Object>(attributes);
    return Collections.unmodifiableMap(map);
  }

  @Override
  public synchronized void setMBeanServerForwarder(javax.management.remote.MBeanServerForwarder mbsf) {
    super.setMBeanServerForwarder(mbsf);
    if (rmiServerImpl != null) {
      rmiServerImpl.setMBeanServer(getMBeanServer());
    }
  }

  @Override
  protected void connectionOpened(String connectionId, String message, Object userData) {
    super.connectionOpened(connectionId, message, userData);
  }

  @Override
  protected void connectionClosed(String connectionId, String message, Object userData) {
    super.connectionClosed(connectionId, message, userData);
  }

  @Override
  protected void connectionFailed(String connectionId, String message, Object userData) {
    super.connectionFailed(connectionId, message, userData);
  }
}
