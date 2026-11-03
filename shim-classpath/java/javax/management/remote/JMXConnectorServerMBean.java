/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package javax.management.remote;

public interface JMXConnectorServerMBean {
  javax.management.remote.JMXServiceURL getAddress();
  java.util.Map<java.lang.String, ?> getAttributes();
  java.lang.String[] getConnectionIds();
  boolean isActive();
  void setMBeanServerForwarder(javax.management.remote.MBeanServerForwarder mBeanServerForwarder);
  void start() throws java.io.IOException;
  void stop() throws java.io.IOException;
  javax.management.remote.JMXConnector toJMXConnector(java.util.Map<java.lang.String, ?> map) throws java.io.IOException;
}
