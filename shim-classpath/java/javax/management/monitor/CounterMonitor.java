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

public class CounterMonitor extends Monitor implements CounterMonitorMBean {
  private Number modulus = Integer.valueOf(0);
  private Number offset = Integer.valueOf(0);
  private boolean notify = false;
  private boolean differenceMode = false;
  private Number initThreshold = Integer.valueOf(0);

  private static final String[] types = {MonitorNotification.RUNTIME_ERROR, MonitorNotification.OBSERVED_OBJECT_ERROR,
      MonitorNotification.OBSERVED_ATTRIBUTE_ERROR, MonitorNotification.OBSERVED_ATTRIBUTE_TYPE_ERROR,
      MonitorNotification.THRESHOLD_ERROR, MonitorNotification.THRESHOLD_VALUE_EXCEEDED};

  public CounterMonitor() {
    dbgTag = CounterMonitor.class.getName();
  }

  public synchronized void start() {
    startMonitor();
  }

  public synchronized void stop() {
    stopMonitor();
  }

  void initObserved(Observed o) {
    o.threshold = initThreshold;
    o.derivedGauge = null;
    o.derivedGaugeTimeStamp = System.currentTimeMillis();
    o.previous = null;
    o.notified = 0;
  }

  String checkType(Object v) {
    if (v instanceof Byte || v instanceof Short || v instanceof Integer || v instanceof Long) {
      return null;
    }
    return "The observed attribute type should be an integer type.";
  }

  private static long l(Number n) {
    return n.longValue();
  }

  private static Number like(Object sample, long v) {
    if (sample instanceof Byte) {
      return Byte.valueOf((byte) v);
    }
    if (sample instanceof Short) {
      return Short.valueOf((short) v);
    }
    if (sample instanceof Integer) {
      return Integer.valueOf((int) v);
    }
    return Long.valueOf(v);
  }

  void evaluate(Observed o, Object value) {
    long cur = l((Number) value);
    long derived = cur;
    if (differenceMode) {
      if (o.previous == null) {
        o.previous = value;
        o.derivedGauge = like(value, 0);
        o.derivedGaugeTimeStamp = System.currentTimeMillis();
        return;
      }
      derived = cur - l((Number) o.previous);
      if (derived < 0 && l(modulus) != 0) {
        derived += l(modulus);
      }
      o.previous = value;
    }
    o.derivedGauge = like(value, derived);
    o.derivedGaugeTimeStamp = System.currentTimeMillis();
    long threshold = l(o.threshold);
    if (derived >= threshold) {
      if (notify) {
        emit(MonitorNotification.THRESHOLD_VALUE_EXCEEDED, o, "", o.derivedGauge, o.threshold);
      }
      long off = l(offset);
      if (off > 0) {
        long t = threshold;
        while (t <= derived) {
          t += off;
        }
        if (l(modulus) > 0 && t > l(modulus)) {
          t = l(initThreshold);
        }
        o.threshold = like(o.threshold, t);
      }
    }
  }

  public synchronized Number getDerivedGauge(ObjectName object) {
    Observed o = observedFor(object);
    return o == null ? null : (Number) o.derivedGauge;
  }

  public synchronized long getDerivedGaugeTimeStamp(ObjectName object) {
    Observed o = observedFor(object);
    return o == null ? 0 : o.derivedGaugeTimeStamp;
  }

  public synchronized Number getThreshold(ObjectName object) {
    Observed o = observedFor(object);
    return o == null ? null : o.threshold;
  }

  public synchronized Number getDerivedGauge() {
    return getDerivedGauge(getObservedObject());
  }

  public synchronized long getDerivedGaugeTimeStamp() {
    return getDerivedGaugeTimeStamp(getObservedObject());
  }

  public synchronized Number getThreshold() {
    return getThreshold(getObservedObject());
  }

  public synchronized Number getInitThreshold() {
    return initThreshold;
  }

  private static void checkNumber(Number n, String what, boolean checkType) {
    if (n == null) {
      throw new IllegalArgumentException("Null " + what);
    }
    if (checkType && !(n instanceof Byte || n instanceof Short || n instanceof Integer || n instanceof Long)) {
      throw new IllegalArgumentException("Threshold type must be Byte, Short, Integer or Long");
    }
    if (n.longValue() < 0) {
      throw new IllegalArgumentException("Negative " + what);
    }
  }

  public synchronized void setInitThreshold(Number value) throws IllegalArgumentException {
    checkNumber(value, "threshold", true);
    if (value.equals(initThreshold)) {
      return;
    }
    initThreshold = value;
    for (Observed o : observed.values()) {
      o.threshold = value;
    }
  }

  public synchronized void setThreshold(Number value) throws IllegalArgumentException {
    setInitThreshold(value);
  }

  public synchronized Number getOffset() {
    return offset;
  }

  public synchronized void setOffset(Number value) throws IllegalArgumentException {
    checkNumber(value, "offset", false);
    offset = value;
  }

  public synchronized Number getModulus() {
    return modulus;
  }

  public synchronized void setModulus(Number value) throws IllegalArgumentException {
    checkNumber(value, "modulus", false);
    modulus = value;
  }

  public synchronized boolean getNotify() {
    return notify;
  }

  public synchronized void setNotify(boolean value) {
    notify = value;
  }

  public synchronized boolean getDifferenceMode() {
    return differenceMode;
  }

  public synchronized void setDifferenceMode(boolean value) {
    differenceMode = value;
    for (Observed o : observed.values()) {
      o.previous = null;
    }
  }

  @Override
  public MBeanNotificationInfo[] getNotificationInfo() {
    return new MBeanNotificationInfo[] {new MBeanNotificationInfo(types, MonitorNotification.class.getName(),
        "Notifications sent by the CounterMonitor MBean")};
  }
}
