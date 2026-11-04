/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package com.sun.tools.jconsole;

public interface JConsoleContext {
  java.lang.String CONNECTION_STATE_PROPERTY = "connectionState";

  void addPropertyChangeListener(java.beans.PropertyChangeListener propertyChangeListener);
  com.sun.tools.jconsole.JConsoleContext.ConnectionState getConnectionState();
  javax.management.MBeanServerConnection getMBeanServerConnection();
  void removePropertyChangeListener(java.beans.PropertyChangeListener propertyChangeListener);

  public enum ConnectionState {
    CONNECTED, DISCONNECTED, CONNECTING;


  }
}
