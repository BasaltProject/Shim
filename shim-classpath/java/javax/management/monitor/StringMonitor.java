/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package javax.management.monitor;

import javax.management.MBeanNotificationInfo;
import javax.management.ObjectName;

public class StringMonitor extends Monitor implements StringMonitorMBean {
  private String stringToCompare = "";
  private boolean notifyMatch = false;
  private boolean notifyDiffer = false;

  private static final int MATCHING = 1;
  private static final int DIFFERING = 2;

  private static final String[] types = {MonitorNotification.RUNTIME_ERROR, MonitorNotification.OBSERVED_OBJECT_ERROR,
      MonitorNotification.OBSERVED_ATTRIBUTE_ERROR, MonitorNotification.OBSERVED_ATTRIBUTE_TYPE_ERROR,
      MonitorNotification.STRING_TO_COMPARE_VALUE_MATCHED, MonitorNotification.STRING_TO_COMPARE_VALUE_DIFFERED};

  public StringMonitor() {
    dbgTag = StringMonitor.class.getName();
  }

  public synchronized void start() {
    startMonitor();
  }

  public synchronized void stop() {
    stopMonitor();
  }

  void initObserved(Observed o) {
    o.derivedGauge = "";
    o.derivedGaugeTimeStamp = System.currentTimeMillis();
    o.status = 0;
    o.notified = 0;
  }

  String checkType(Object v) {
    if (v instanceof String) {
      return null;
    }
    return "The observed attribute type should be a string type.";
  }

  void evaluate(Observed o, Object value) {
    String s = (String) value;
    o.derivedGauge = s;
    o.derivedGaugeTimeStamp = System.currentTimeMillis();
    if (stringToCompare.equals(s)) {
      if (o.status != MATCHING) {
        if (notifyMatch) {
          emit(MonitorNotification.STRING_TO_COMPARE_VALUE_MATCHED, o, "", s, stringToCompare);
        }
        o.status = MATCHING;
      }
    } else {
      if (o.status != DIFFERING) {
        if (notifyDiffer) {
          emit(MonitorNotification.STRING_TO_COMPARE_VALUE_DIFFERED, o, "", s, stringToCompare);
        }
        o.status = DIFFERING;
      }
    }
  }

  public synchronized String getDerivedGauge(ObjectName object) {
    Observed o = observedFor(object);
    return o == null ? null : (String) o.derivedGauge;
  }

  public synchronized long getDerivedGaugeTimeStamp(ObjectName object) {
    Observed o = observedFor(object);
    return o == null ? 0 : o.derivedGaugeTimeStamp;
  }

  public synchronized String getDerivedGauge() {
    return getDerivedGauge(getObservedObject());
  }

  public synchronized long getDerivedGaugeTimeStamp() {
    return getDerivedGaugeTimeStamp(getObservedObject());
  }

  public synchronized String getStringToCompare() {
    return stringToCompare;
  }

  public synchronized void setStringToCompare(String value) throws IllegalArgumentException {
    if (value == null) {
      throw new IllegalArgumentException("Null string to compare");
    }
    if (stringToCompare.equals(value)) {
      return;
    }
    stringToCompare = value;
    for (Observed o : observed.values()) {
      o.status = 0;
    }
  }

  public synchronized boolean getNotifyMatch() {
    return notifyMatch;
  }

  public synchronized void setNotifyMatch(boolean value) {
    notifyMatch = value;
  }

  public synchronized boolean getNotifyDiffer() {
    return notifyDiffer;
  }

  public synchronized void setNotifyDiffer(boolean value) {
    notifyDiffer = value;
  }

  @Override
  public MBeanNotificationInfo[] getNotificationInfo() {
    return new MBeanNotificationInfo[] {new MBeanNotificationInfo(types, MonitorNotification.class.getName(),
        "Notifications sent by the StringMonitor MBean")};
  }
}
