/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package javax.management.monitor;

public interface GaugeMonitorMBean extends javax.management.monitor.MonitorMBean {
  java.lang.Number getDerivedGauge();
  java.lang.Number getDerivedGauge(javax.management.ObjectName objectName);
  long getDerivedGaugeTimeStamp();
  long getDerivedGaugeTimeStamp(javax.management.ObjectName objectName);
  boolean getDifferenceMode();
  java.lang.Number getHighThreshold();
  java.lang.Number getLowThreshold();
  boolean getNotifyHigh();
  boolean getNotifyLow();
  void setDifferenceMode(boolean flag);
  void setNotifyHigh(boolean flag);
  void setNotifyLow(boolean flag);
  void setThresholds(java.lang.Number number, java.lang.Number number2) throws java.lang.IllegalArgumentException;
}
