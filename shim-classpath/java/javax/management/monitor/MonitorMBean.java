/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package javax.management.monitor;

public interface MonitorMBean {
  void addObservedObject(javax.management.ObjectName objectName) throws java.lang.IllegalArgumentException;
  boolean containsObservedObject(javax.management.ObjectName objectName);
  long getGranularityPeriod();
  java.lang.String getObservedAttribute();
  javax.management.ObjectName getObservedObject();
  javax.management.ObjectName[] getObservedObjects();
  boolean isActive();
  void removeObservedObject(javax.management.ObjectName objectName);
  void setGranularityPeriod(long l) throws java.lang.IllegalArgumentException;
  void setObservedAttribute(java.lang.String string);
  void setObservedObject(javax.management.ObjectName objectName);
  void start();
  void stop();
}
