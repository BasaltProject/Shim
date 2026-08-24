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

public class GaugeMonitor extends Monitor implements GaugeMonitorMBean {
  private Number highThreshold = Integer.valueOf(0);
  private Number lowThreshold = Integer.valueOf(0);
  private boolean notifyHigh = false;
  private boolean notifyLow = false;
  private boolean differenceMode = false;

  private static final int RISING = 1;
  private static final int FALLING = 2;
  private static final int INITIAL = 0;

  private static final String[] types = {MonitorNotification.RUNTIME_ERROR, MonitorNotification.OBSERVED_OBJECT_ERROR,
      MonitorNotification.OBSERVED_ATTRIBUTE_ERROR, MonitorNotification.OBSERVED_ATTRIBUTE_TYPE_ERROR,
      MonitorNotification.THRESHOLD_ERROR, MonitorNotification.THRESHOLD_HIGH_VALUE_EXCEEDED,
      MonitorNotification.THRESHOLD_LOW_VALUE_EXCEEDED};

  public GaugeMonitor() {
    dbgTag = GaugeMonitor.class.getName();
  }

  public synchronized void start() {
    startMonitor();
  }

  public synchronized void stop() {
    stopMonitor();
  }

  void initObserved(Observed o) {
    o.derivedGauge = null;
    o.derivedGaugeTimeStamp = System.currentTimeMillis();
    o.previous = null;
    o.status = INITIAL;
    o.notified = 0;
  }

  String checkType(Object v) {
    if (v instanceof Byte || v instanceof Short || v instanceof Integer || v instanceof Long || v instanceof Float
        || v instanceof Double) {
      return null;
    }
    return "The observed attribute type should be a number type.";
  }

  private static Number like(Object sample, double v) {
    if (sample instanceof Byte) {
      return Byte.valueOf((byte) v);
    }
    if (sample instanceof Short) {
      return Short.valueOf((short) v);
    }
    if (sample instanceof Integer) {
      return Integer.valueOf((int) v);
    }
    if (sample instanceof Long) {
      return Long.valueOf((long) v);
    }
    if (sample instanceof Float) {
      return Float.valueOf((float) v);
    }
    return Double.valueOf(v);
  }

  void evaluate(Observed o, Object value) {
    double cur = ((Number) value).doubleValue();
    double derived = cur;
    if (differenceMode) {
      if (o.previous == null) {
        o.previous = value;
        o.derivedGauge = like(value, 0);
        o.derivedGaugeTimeStamp = System.currentTimeMillis();
        return;
      }
      derived = cur - ((Number) o.previous).doubleValue();
      o.previous = value;
    }
    o.derivedGauge = like(value, derived);
    o.derivedGaugeTimeStamp = System.currentTimeMillis();
    double high = highThreshold.doubleValue();
    double low = lowThreshold.doubleValue();
    if (o.status == INITIAL) {
      if (derived >= high) {
        if (notifyHigh) {
          emit(MonitorNotification.THRESHOLD_HIGH_VALUE_EXCEEDED, o, "", o.derivedGauge, highThreshold);
        }
        o.status = FALLING;
      } else if (derived <= low) {
        if (notifyLow) {
          emit(MonitorNotification.THRESHOLD_LOW_VALUE_EXCEEDED, o, "", o.derivedGauge, lowThreshold);
        }
        o.status = RISING;
      } else {
        o.status = RISING;
      }
    } else if (o.status == RISING) {
      if (derived >= high) {
        if (notifyHigh) {
          emit(MonitorNotification.THRESHOLD_HIGH_VALUE_EXCEEDED, o, "", o.derivedGauge, highThreshold);
        }
        o.status = FALLING;
      }
    } else {
      if (derived <= low) {
        if (notifyLow) {
          emit(MonitorNotification.THRESHOLD_LOW_VALUE_EXCEEDED, o, "", o.derivedGauge, lowThreshold);
        }
        o.status = RISING;
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

  public synchronized Number getDerivedGauge() {
    return getDerivedGauge(getObservedObject());
  }

  public synchronized long getDerivedGaugeTimeStamp() {
    return getDerivedGaugeTimeStamp(getObservedObject());
  }

  public synchronized Number getHighThreshold() {
    return highThreshold;
  }

  public synchronized Number getLowThreshold() {
    return lowThreshold;
  }

  public synchronized void setThresholds(Number highValue, Number lowValue) throws IllegalArgumentException {
    if (highValue == null || lowValue == null) {
      throw new IllegalArgumentException("Null threshold value");
    }
    if (highValue.getClass() != lowValue.getClass()) {
      throw new IllegalArgumentException("Different type threshold values");
    }
    if (isFirstStrictlyGreaterThanLast(lowValue, highValue, highValue.getClass().getName())) {
      throw new IllegalArgumentException("High threshold less than low threshold");
    }
    if (highThreshold.equals(highValue) && lowThreshold.equals(lowValue)) {
      return;
    }
    highThreshold = highValue;
    lowThreshold = lowValue;
    for (Observed o : observed.values()) {
      o.status = INITIAL;
    }
  }

  private static boolean isFirstStrictlyGreaterThanLast(Number a, Number b, String className) {
    if (a instanceof Float || a instanceof Double) {
      return a.doubleValue() > b.doubleValue();
    }
    return a.longValue() > b.longValue();
  }

  public synchronized boolean getNotifyHigh() {
    return notifyHigh;
  }

  public synchronized void setNotifyHigh(boolean value) {
    notifyHigh = value;
  }

  public synchronized boolean getNotifyLow() {
    return notifyLow;
  }

  public synchronized void setNotifyLow(boolean value) {
    notifyLow = value;
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
        "Notifications sent by the GaugeMonitor MBean")};
  }
}
