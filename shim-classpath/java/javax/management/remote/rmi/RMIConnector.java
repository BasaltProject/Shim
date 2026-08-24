/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package javax.management.remote.rmi;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.Serializable;
import java.net.MalformedURLException;
import java.rmi.MarshalledObject;
import java.rmi.Remote;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.management.Attribute;
import javax.management.AttributeList;
import javax.management.AttributeNotFoundException;
import javax.management.InstanceAlreadyExistsException;
import javax.management.InstanceNotFoundException;
import javax.management.IntrospectionException;
import javax.management.InvalidAttributeValueException;
import javax.management.ListenerNotFoundException;
import javax.management.MBeanException;
import javax.management.MBeanInfo;
import javax.management.MBeanRegistrationException;
import javax.management.MBeanServerConnection;
import javax.management.NotCompliantMBeanException;
import javax.management.Notification;
import javax.management.NotificationBroadcasterSupport;
import javax.management.NotificationFilter;
import javax.management.NotificationListener;
import javax.management.ObjectInstance;
import javax.management.ObjectName;
import javax.management.QueryExp;
import javax.management.ReflectionException;
import javax.management.remote.JMXAddressable;
import javax.management.remote.JMXConnectionNotification;
import javax.management.remote.JMXConnector;
import javax.management.remote.JMXServiceURL;
import javax.management.remote.NotificationResult;
import javax.management.remote.TargetedNotification;
import javax.security.auth.Subject;

public class RMIConnector implements JMXConnector, Serializable, JMXAddressable {
  private static final long serialVersionUID = 817323035842634473L;

  private final RMIServer rmiServer;
  private final JMXServiceURL jmxServiceURL;
  private transient Map<String, Object> env;
  private transient RMIConnection connection;
  private transient String connectionId;
  private transient boolean connected;
  private transient boolean terminated;
  private transient MBeanServerConnection mbsc;
  private transient long clientNotifSeqNo = 0;
  private transient Thread fetcher;
  private transient NotificationBroadcasterSupport connectionBroadcaster;
  private transient long connectionNotifSeq = 0;
  private transient final Map<Integer, Registered> registered = new HashMap<Integer, Registered>();
  private transient ClassLoader defaultClassLoader;

  private static final class Registered {
    final ObjectName name;
    final NotificationListener listener;
    final NotificationFilter filter;
    final Object handback;

    Registered(ObjectName name, NotificationListener listener, NotificationFilter filter, Object handback) {
      this.name = name;
      this.listener = listener;
      this.filter = filter;
      this.handback = handback;
    }
  }

  public RMIConnector(RMIServer rmiServer, Map<String, ?> environment) {
    this(rmiServer, null, environment);
  }

  public RMIConnector(JMXServiceURL url, Map<String, ?> environment) {
    this(null, url, environment);
  }

  private RMIConnector(RMIServer rmiServer, JMXServiceURL address, Map<String, ?> environment) {
    if (rmiServer == null && address == null) {
      throw new IllegalArgumentException("rmiServer and jmxServiceURL both null");
    }
    this.rmiServer = rmiServer;
    this.jmxServiceURL = address;
    if (environment == null) {
      this.env = Collections.emptyMap();
    } else {
      this.env = Collections.unmodifiableMap(new HashMap<String, Object>(environment));
    }
    this.connectionBroadcaster = new NotificationBroadcasterSupport();
  }

  public JMXServiceURL getAddress() {
    return jmxServiceURL;
  }

  public void connect() throws IOException {
    connect(null);
  }

  public synchronized void connect(Map<String, ?> environment) throws IOException {
    if (terminated) {
      throw new IOException("Connector closed");
    }
    if (connected) {
      return;
    }
    Map<String, Object> usemap = new HashMap<String, Object>(this.env == null ? Collections.<String, Object>emptyMap() : this.env);
    if (environment != null) {
      usemap.putAll(environment);
    }
    this.env = usemap;
    Object cl = usemap.get(JMXConnectorFactoryKeys.DEFAULT_CLASS_LOADER);
    if (cl instanceof ClassLoader) {
      defaultClassLoader = (ClassLoader) cl;
    }
    RMIServer stub = rmiServer;
    if (stub == null) {
      stub = findRMIServer(jmxServiceURL, usemap);
    }
    Object credentials = usemap.get(CREDENTIALS);
    try {
      connection = stub.newClient(credentials);
      connectionId = connection.getConnectionId();
    } catch (SecurityException e) {
      throw e;
    }
    connected = true;
    mbsc = new RemoteMBeanServerConnection();
    sendConnectionNotification(JMXConnectionNotification.OPENED, "Successful connection");
  }

  private static final class JMXConnectorFactoryKeys {
    static final String DEFAULT_CLASS_LOADER = "jmx.remote.default.class.loader";
  }

  private RMIServer findRMIServer(JMXServiceURL directoryURL, Map<String, Object> environment) throws IOException {
    String path = directoryURL.getURLPath();
    int end = path.indexOf(';');
    if (end < 0) {
      end = path.length();
    }
    if (path.startsWith("/jndi/")) {
      return lookupJndi(path.substring("/jndi/".length(), end));
    } else if (path.startsWith("/stub/")) {
      return decodeStub(path.substring("/stub/".length(), end));
    } else if (path.startsWith("/ior/")) {
      throw new MalformedURLException("IIOP is not supported: " + path);
    }
    throw new MalformedURLException("URL path must begin with /jndi/ or /stub/ or /ior/: " + path);
  }

  private RMIServer lookupJndi(String jndiUrl) throws IOException {
    java.net.URI uri;
    try {
      uri = new java.net.URI(jndiUrl);
    } catch (java.net.URISyntaxException e) {
      throw new MalformedURLException("Invalid JNDI URL: " + jndiUrl);
    }
    String name = uri.getPath() == null ? "" : uri.getPath();
    if (name.startsWith("/")) {
      name = name.substring(1);
    }
    int port = uri.getPort();
    String host = uri.getHost();
    Registry registry = LocateRegistry.getRegistry(host == null ? "localhost" : host,
        port <= 0 ? Registry.REGISTRY_PORT : port);
    try {
      Remote r = registry.lookup(name);
      return (RMIServer) r;
    } catch (java.rmi.NotBoundException e) {
      throw new IOException("Cannot find the JMX connector stub: " + name, e);
    }
  }

  private RMIServer decodeStub(String encoded) throws IOException {
    byte[] bytes;
    try {
      bytes = Base64.getUrlDecoder().decode(encoded);
    } catch (IllegalArgumentException e) {
      bytes = Base64.getDecoder().decode(encoded.replace('-', '+').replace('_', '/'));
    }
    ObjectInputStream in = new ObjectInputStream(new ByteArrayInputStream(bytes));
    try {
      return (RMIServer) in.readObject();
    } catch (ClassNotFoundException e) {
      throw new IOException("Cannot decode the stub: " + e);
    }
  }

  public synchronized String getConnectionId() throws IOException {
    if (terminated || !connected) {
      return null;
    }
    return connection.getConnectionId();
  }

  public synchronized MBeanServerConnection getMBeanServerConnection() throws IOException {
    return getMBeanServerConnection(null);
  }

  public synchronized MBeanServerConnection getMBeanServerConnection(Subject delegationSubject) throws IOException {
    if (terminated) {
      throw new IOException("Connection closed");
    } else if (!connected) {
      throw new IOException("Not connected");
    }
    return mbsc;
  }

  public synchronized void close() throws IOException {
    if (terminated) {
      return;
    }
    terminated = true;
    if (fetcher != null) {
      fetcher.interrupt();
    }
    IOException failure = null;
    if (connection != null) {
      try {
        connection.close();
      } catch (IOException e) {
        failure = e;
      }
    }
    if (connected) {
      sendConnectionNotification(JMXConnectionNotification.CLOSED, "Client has been closed");
    }
    connected = false;
    if (failure != null) {
      throw failure;
    }
  }

  private void sendConnectionNotification(String type, String message) {
    long seq;
    synchronized (this) {
      seq = connectionNotifSeq++;
    }
    Notification n = new JMXConnectionNotification(type, this, connectionId == null ? "" : connectionId, seq, message, null);
    connectionBroadcaster.sendNotification(n);
  }

  public void addConnectionNotificationListener(NotificationListener listener, NotificationFilter filter,
      Object handback) {
    if (listener == null) {
      throw new NullPointerException("listener");
    }
    connectionBroadcaster.addNotificationListener(listener, filter, handback);
  }

  public void removeConnectionNotificationListener(NotificationListener listener) throws ListenerNotFoundException {
    if (listener == null) {
      throw new NullPointerException("listener");
    }
    connectionBroadcaster.removeNotificationListener(listener);
  }

  public void removeConnectionNotificationListener(NotificationListener listener, NotificationFilter filter,
      Object handback) throws ListenerNotFoundException {
    if (listener == null) {
      throw new NullPointerException("listener");
    }
    connectionBroadcaster.removeNotificationListener(listener, filter, handback);
  }

  public String toString() {
    StringBuilder buf = new StringBuilder(this.getClass().getName());
    buf.append(":");
    if (rmiServer != null) {
      buf.append(" rmiServer=").append(rmiServer.toString());
    }
    if (jmxServiceURL != null) {
      if (rmiServer != null) {
        buf.append(",");
      }
      buf.append(" jmxServiceURL=").append(jmxServiceURL.toString());
    }
    return buf.toString();
  }

  private void startFetcher() {
    if (fetcher != null) {
      return;
    }
    fetcher = new Thread(new Runnable() {
      public void run() {
        long seq = 0;
        while (!terminated) {
          try {
            NotificationResult r = connection.fetchNotifications(seq, 100, 1000);
            seq = r.getNextSequenceNumber();
            for (TargetedNotification tn : r.getTargetedNotifications()) {
              Registered reg;
              synchronized (registered) {
                reg = registered.get(tn.getListenerID());
              }
              if (reg != null) {
                try {
                  reg.listener.handleNotification(tn.getNotification(), reg.handback);
                } catch (RuntimeException e) {
                }
              }
            }
          } catch (IOException e) {
            if (!terminated) {
              sendConnectionNotification(JMXConnectionNotification.FAILED, e.toString());
            }
            return;
          } catch (RuntimeException e) {
            return;
          }
        }
      }
    }, "RMI Notification Fetcher");
    fetcher.setDaemon(true);
    fetcher.start();
  }

  private final class RemoteMBeanServerConnection implements MBeanServerConnection {
    private void open() throws IOException {
      if (terminated) {
        throw new IOException("The client has been closed.");
      }
    }

    private MarshalledObject<?> wrap(Object o) throws IOException {
      return new MarshalledObject<Object>(o);
    }

    public ObjectInstance createMBean(String className, ObjectName name) throws ReflectionException,
        InstanceAlreadyExistsException, MBeanRegistrationException, MBeanException, NotCompliantMBeanException,
        IOException {
      open();
      return connection.createMBean(className, name, (Subject) null);
    }

    public ObjectInstance createMBean(String className, ObjectName name, ObjectName loaderName)
        throws ReflectionException, InstanceAlreadyExistsException, MBeanRegistrationException, MBeanException,
        NotCompliantMBeanException, InstanceNotFoundException, IOException {
      open();
      return connection.createMBean(className, name, loaderName, (Subject) null);
    }

    public ObjectInstance createMBean(String className, ObjectName name, Object[] params, String[] signature)
        throws ReflectionException, InstanceAlreadyExistsException, MBeanRegistrationException, MBeanException,
        NotCompliantMBeanException, IOException {
      open();
      return connection.createMBean(className, name, wrap(params), signature, null);
    }

    public ObjectInstance createMBean(String className, ObjectName name, ObjectName loaderName, Object[] params,
        String[] signature) throws ReflectionException, InstanceAlreadyExistsException, MBeanRegistrationException,
        MBeanException, NotCompliantMBeanException, InstanceNotFoundException, IOException {
      open();
      return connection.createMBean(className, name, loaderName, wrap(params), signature, null);
    }

    public void unregisterMBean(ObjectName name) throws InstanceNotFoundException, MBeanRegistrationException,
        IOException {
      open();
      connection.unregisterMBean(name, null);
    }

    public ObjectInstance getObjectInstance(ObjectName name) throws InstanceNotFoundException, IOException {
      open();
      return connection.getObjectInstance(name, null);
    }

    public Set<ObjectInstance> queryMBeans(ObjectName name, QueryExp query) throws IOException {
      open();
      return connection.queryMBeans(name, wrap(query), null);
    }

    public Set<ObjectName> queryNames(ObjectName name, QueryExp query) throws IOException {
      open();
      return connection.queryNames(name, wrap(query), null);
    }

    public boolean isRegistered(ObjectName name) throws IOException {
      open();
      return connection.isRegistered(name, null);
    }

    public Integer getMBeanCount() throws IOException {
      open();
      return connection.getMBeanCount(null);
    }

    public Object getAttribute(ObjectName name, String attribute) throws MBeanException, AttributeNotFoundException,
        InstanceNotFoundException, ReflectionException, IOException {
      open();
      return connection.getAttribute(name, attribute, null);
    }

    public AttributeList getAttributes(ObjectName name, String[] attributes) throws InstanceNotFoundException,
        ReflectionException, IOException {
      open();
      return connection.getAttributes(name, attributes, null);
    }

    public void setAttribute(ObjectName name, Attribute attribute) throws InstanceNotFoundException,
        AttributeNotFoundException, InvalidAttributeValueException, MBeanException, ReflectionException, IOException {
      open();
      connection.setAttribute(name, wrap(attribute), null);
    }

    public AttributeList setAttributes(ObjectName name, AttributeList attributes) throws InstanceNotFoundException,
        ReflectionException, IOException {
      open();
      return connection.setAttributes(name, wrap(attributes), null);
    }

    public Object invoke(ObjectName name, String operationName, Object[] params, String[] signature)
        throws InstanceNotFoundException, MBeanException, ReflectionException, IOException {
      open();
      return connection.invoke(name, operationName, wrap(params), signature, null);
    }

    public String getDefaultDomain() throws IOException {
      open();
      return connection.getDefaultDomain(null);
    }

    public String[] getDomains() throws IOException {
      open();
      return connection.getDomains(null);
    }

    public MBeanInfo getMBeanInfo(ObjectName name) throws InstanceNotFoundException, IntrospectionException,
        ReflectionException, IOException {
      open();
      return connection.getMBeanInfo(name, null);
    }

    public boolean isInstanceOf(ObjectName name, String className) throws InstanceNotFoundException, IOException {
      open();
      return connection.isInstanceOf(name, className, null);
    }

    public void addNotificationListener(ObjectName name, NotificationListener listener, NotificationFilter filter,
        Object handback) throws InstanceNotFoundException, IOException {
      open();
      if (listener == null) {
        throw new NullPointerException("Null listener");
      }
      Integer[] ids = connection.addNotificationListeners(new ObjectName[] {name},
          new MarshalledObject[] {wrap(filter)}, new Subject[] {null});
      synchronized (registered) {
        registered.put(ids[0], new Registered(name, listener, filter, handback));
      }
      startFetcher();
    }

    public void addNotificationListener(ObjectName name, ObjectName listener, NotificationFilter filter,
        Object handback) throws InstanceNotFoundException, IOException {
      open();
      connection.addNotificationListener(name, listener, wrap(filter), wrap(handback), null);
    }

    public void removeNotificationListener(ObjectName name, ObjectName listener) throws InstanceNotFoundException,
        ListenerNotFoundException, IOException {
      open();
      connection.removeNotificationListener(name, listener, null);
    }

    public void removeNotificationListener(ObjectName name, ObjectName listener, NotificationFilter filter,
        Object handback) throws InstanceNotFoundException, ListenerNotFoundException, IOException {
      open();
      connection.removeNotificationListener(name, listener, wrap(filter), wrap(handback), null);
    }

    public void removeNotificationListener(ObjectName name, NotificationListener listener)
        throws InstanceNotFoundException, ListenerNotFoundException, IOException {
      open();
      removeLocal(name, listener, null, null, false);
    }

    public void removeNotificationListener(ObjectName name, NotificationListener listener, NotificationFilter filter,
        Object handback) throws InstanceNotFoundException, ListenerNotFoundException, IOException {
      open();
      removeLocal(name, listener, filter, handback, true);
    }

    private void removeLocal(ObjectName name, NotificationListener listener, NotificationFilter filter,
        Object handback, boolean exact) throws InstanceNotFoundException, ListenerNotFoundException, IOException {
      List<Integer> ids = new ArrayList<Integer>();
      synchronized (registered) {
        for (Map.Entry<Integer, Registered> e : registered.entrySet()) {
          Registered r = e.getValue();
          if (r.name.equals(name) && r.listener == listener
              && (!exact || (r.filter == filter && r.handback == handback))) {
            ids.add(e.getKey());
          }
        }
      }
      if (ids.isEmpty()) {
        throw new ListenerNotFoundException("Unknown listener");
      }
      connection.removeNotificationListeners(name, ids.toArray(new Integer[0]), null);
      synchronized (registered) {
        for (Integer id : ids) {
          registered.remove(id);
        }
      }
    }
  }
}
