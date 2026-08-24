/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package javax.management.remote;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import javax.management.MBeanNotificationInfo;
import javax.management.MBeanRegistration;
import javax.management.MBeanServer;
import javax.management.NotificationBroadcasterSupport;
import javax.management.ObjectName;

public abstract class JMXConnectorServer extends NotificationBroadcasterSupport
    implements JMXConnectorServerMBean, MBeanRegistration, JMXAddressable {
  public static final String AUTHENTICATOR = "jmx.remote.authenticator";

  private MBeanServer mbs;
  private final List<String> connectionIds = new ArrayList<String>();
  private long sequenceNumber = 1;

  public JMXConnectorServer() {
    this(null);
  }

  public JMXConnectorServer(MBeanServer mbeanServer) {
    mbs = mbeanServer;
  }

  public synchronized MBeanServer getMBeanServer() {
    return mbs;
  }

  public synchronized void setMBeanServerForwarder(MBeanServerForwarder mbsf) {
    if (mbsf == null) {
      throw new IllegalArgumentException("Invalid null argument: mbsf");
    }
    if (mbs != null) {
      mbsf.setMBeanServer(mbs);
    }
    mbs = mbsf;
  }

  public String[] getConnectionIds() {
    synchronized (connectionIds) {
      return connectionIds.toArray(new String[connectionIds.size()]);
    }
  }

  public JMXConnector toJMXConnector(Map<String, ?> env) throws IOException {
    if (!isActive()) {
      throw new IllegalStateException("Connector is not active");
    }
    JMXServiceURL addr = getAddress();
    return JMXConnectorFactory.newJMXConnector(addr, env);
  }

  public MBeanNotificationInfo[] getNotificationInfo() {
    final String[] types = {JMXConnectionNotification.OPENED, JMXConnectionNotification.CLOSED,
        JMXConnectionNotification.FAILED};
    return new MBeanNotificationInfo[] {new MBeanNotificationInfo(types, JMXConnectionNotification.class.getName(),
        "A client connection has been opened or closed")};
  }

  protected void connectionOpened(String connectionId, String message, Object userData) {
    if (connectionId == null) {
      throw new NullPointerException("Illegal null argument");
    }
    synchronized (connectionIds) {
      connectionIds.add(connectionId);
    }
    sendNotification(JMXConnectionNotification.OPENED, connectionId, message, userData);
  }

  protected void connectionClosed(String connectionId, String message, Object userData) {
    if (connectionId == null) {
      throw new NullPointerException("Illegal null argument");
    }
    synchronized (connectionIds) {
      connectionIds.remove(connectionId);
    }
    sendNotification(JMXConnectionNotification.CLOSED, connectionId, message, userData);
  }

  protected void connectionFailed(String connectionId, String message, Object userData) {
    if (connectionId == null) {
      throw new NullPointerException("Illegal null argument");
    }
    synchronized (connectionIds) {
      connectionIds.remove(connectionId);
    }
    sendNotification(JMXConnectionNotification.FAILED, connectionId, message, userData);
  }

  private void sendNotification(String type, String connectionId, String message, Object userData) {
    long seq;
    synchronized (this) {
      seq = sequenceNumber++;
    }
    sendNotification(new JMXConnectionNotification(type, getNotificationSource(), connectionId, seq, message,
        userData));
  }

  private Object getNotificationSource() {
    return objectName != null ? objectName : this;
  }

  private ObjectName objectName;

  public synchronized ObjectName preRegister(MBeanServer server, ObjectName name) {
    if (name == null) {
      throw new NullPointerException("Null object name");
    }
    objectName = name;
    return name;
  }

  public void postRegister(Boolean registrationDone) {
  }

  public synchronized void preDeregister() throws Exception {
    if (objectName != null && isActive()) {
      stop();
    }
  }

  public void postDeregister() {
    objectName = null;
  }
}
