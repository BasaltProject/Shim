/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package javax.management.monitor;

public interface StringMonitorMBean extends javax.management.monitor.MonitorMBean {
  java.lang.String getDerivedGauge();
  java.lang.String getDerivedGauge(javax.management.ObjectName objectName);
  long getDerivedGaugeTimeStamp();
  long getDerivedGaugeTimeStamp(javax.management.ObjectName objectName);
  boolean getNotifyDiffer();
  boolean getNotifyMatch();
  java.lang.String getStringToCompare();
  void setNotifyDiffer(boolean flag);
  void setNotifyMatch(boolean flag);
  void setStringToCompare(java.lang.String string) throws java.lang.IllegalArgumentException;
}
