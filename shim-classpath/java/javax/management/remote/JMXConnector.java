/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package javax.management.remote;

public interface JMXConnector extends java.io.Closeable {
  java.lang.String CREDENTIALS = "jmx.remote.credentials";

  void addConnectionNotificationListener(javax.management.NotificationListener notificationListener, javax.management.NotificationFilter notificationFilter, java.lang.Object object);
  void close() throws java.io.IOException;
  void connect() throws java.io.IOException;
  void connect(java.util.Map<java.lang.String, ?> map) throws java.io.IOException;
  java.lang.String getConnectionId() throws java.io.IOException;
  javax.management.MBeanServerConnection getMBeanServerConnection() throws java.io.IOException;
  default javax.management.MBeanServerConnection getMBeanServerConnection(javax.security.auth.Subject subject) throws java.io.IOException {
    throw new UnsupportedOperationException();
  }
  void removeConnectionNotificationListener(javax.management.NotificationListener notificationListener, javax.management.NotificationFilter notificationFilter, java.lang.Object object) throws javax.management.ListenerNotFoundException;
  void removeConnectionNotificationListener(javax.management.NotificationListener notificationListener) throws javax.management.ListenerNotFoundException;
}
