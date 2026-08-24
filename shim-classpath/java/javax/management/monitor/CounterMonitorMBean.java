/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package javax.management.monitor;

public interface CounterMonitorMBean extends javax.management.monitor.MonitorMBean {
  java.lang.Number getDerivedGauge();
  java.lang.Number getDerivedGauge(javax.management.ObjectName objectName);
  long getDerivedGaugeTimeStamp();
  long getDerivedGaugeTimeStamp(javax.management.ObjectName objectName);
  boolean getDifferenceMode();
  java.lang.Number getInitThreshold();
  java.lang.Number getModulus();
  boolean getNotify();
  java.lang.Number getOffset();
  java.lang.Number getThreshold();
  java.lang.Number getThreshold(javax.management.ObjectName objectName);
  void setDifferenceMode(boolean flag);
  void setInitThreshold(java.lang.Number number) throws java.lang.IllegalArgumentException;
  void setModulus(java.lang.Number number) throws java.lang.IllegalArgumentException;
  void setNotify(boolean flag);
  void setOffset(java.lang.Number number) throws java.lang.IllegalArgumentException;
  void setThreshold(java.lang.Number number) throws java.lang.IllegalArgumentException;
}
