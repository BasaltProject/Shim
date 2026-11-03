/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package javax.management.monitor;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import javax.management.AttributeNotFoundException;
import javax.management.InstanceNotFoundException;
import javax.management.MBeanRegistration;
import javax.management.MBeanServer;
import javax.management.NotificationBroadcasterSupport;
import javax.management.ObjectName;

public abstract class Monitor extends NotificationBroadcasterSupport implements MonitorMBean, MBeanRegistration {
  protected static final int capacityIncrement = 16;
  protected int elementCount = 0;
  protected int alreadyNotified = 0;
  protected int[] alreadyNotifieds = new int[capacityIncrement];
  protected MBeanServer server;
  protected static final int RESET_FLAGS_ALREADY_NOTIFIED = 0;
  protected static final int OBSERVED_OBJECT_ERROR_NOTIFIED = 1;
  protected static final int OBSERVED_ATTRIBUTE_ERROR_NOTIFIED = 2;
  protected static final int OBSERVED_ATTRIBUTE_TYPE_ERROR_NOTIFIED = 4;
  protected static final int RUNTIME_ERROR_NOTIFIED = 8;
  protected String dbgTag = Monitor.class.getName();

  static class Observed {
    final ObjectName name;
    Object derivedGauge;
    long derivedGaugeTimeStamp;
    int notified;
    Object previous;
    Number threshold;
    int status;

    Observed(ObjectName name) {
      this.name = name;
    }
  }

  final Map<ObjectName, Observed> observed = new LinkedHashMap<ObjectName, Observed>();
  private String observedAttribute;
  private long granularityPeriod = 10000;
  private boolean isActive = false;
  private long sequenceNumber = 0;
  private java.util.Timer scheduler;

  public Monitor() {
  }

  public ObjectName preRegister(MBeanServer server, ObjectName name) throws Exception {
    this.server = server;
    return name;
  }

  public void postRegister(Boolean registrationDone) {
  }

  public void preDeregister() throws Exception {
    stop();
  }

  public void postDeregister() {
    this.server = null;
  }

  public synchronized void addObservedObject(ObjectName object) throws IllegalArgumentException {
    if (object == null) {
      throw new IllegalArgumentException("Specified observed object cannot be null");
    }
    if (containsObservedObject(object)) {
      return;
    }
    Observed o = new Observed(object);
    initObserved(o);
    observed.put(object, o);
    elementCount = observed.size();
  }

  public synchronized void removeObservedObject(ObjectName object) {
    if (object == null) {
      return;
    }
    observed.remove(object);
    elementCount = observed.size();
  }

  public synchronized boolean containsObservedObject(ObjectName object) {
    return observed.containsKey(object);
  }

  public synchronized ObjectName[] getObservedObjects() {
    return observed.keySet().toArray(new ObjectName[0]);
  }

  public synchronized ObjectName getObservedObject() {
    return observed.isEmpty() ? null : observed.keySet().iterator().next();
  }

  public synchronized void setObservedObject(ObjectName object) throws IllegalArgumentException {
    if (object == null) {
      throw new IllegalArgumentException("Specified observed object cannot be null");
    }
    ObjectName[] current = getObservedObjects();
    for (ObjectName c : current) {
      if (!c.equals(object)) {
        removeObservedObject(c);
      }
    }
    addObservedObject(object);
  }

  public String getObservedAttribute() {
    synchronized (this) {
      return observedAttribute;
    }
  }

  public void setObservedAttribute(String attribute) throws IllegalArgumentException {
    if (attribute == null) {
      throw new IllegalArgumentException("Specified attribute name cannot be null");
    }
    synchronized (this) {
      if (observedAttribute != null && observedAttribute.equals(attribute)) {
        return;
      }
      observedAttribute = attribute;
      for (Observed o : observed.values()) {
        initObserved(o);
      }
    }
  }

  public synchronized long getGranularityPeriod() {
    return granularityPeriod;
  }

  public synchronized void setGranularityPeriod(long period) throws IllegalArgumentException {
    if (period <= 0) {
      throw new IllegalArgumentException("Nonpositive granularity period");
    }
    if (granularityPeriod == period) {
      return;
    }
    granularityPeriod = period;
    if (isActive) {
      stop();
      start();
    }
  }

  public synchronized boolean isActive() {
    return isActive;
  }

  void startMonitor() {
    synchronized (this) {
      if (isActive) {
        return;
      }
      isActive = true;
      scheduler = new java.util.Timer("jmx.monitor", true);
      scheduler.schedule(new java.util.TimerTask() {
        @Override
        public void run() {
          poll();
        }
      }, granularityPeriod, granularityPeriod);
    }
  }

  void stopMonitor() {
    synchronized (this) {
      isActive = false;
      if (scheduler != null) {
        scheduler.cancel();
        scheduler = null;
      }
    }
  }

  void poll() {
    List<Observed> snapshot;
    MBeanServer srv;
    String attr;
    synchronized (this) {
      if (!isActive) {
        return;
      }
      snapshot = new ArrayList<Observed>(observed.values());
      srv = server;
      attr = observedAttribute;
    }
    for (Observed o : snapshot) {
      pollOne(srv, attr, o);
    }
  }

  private void pollOne(MBeanServer srv, String attr, Observed o) {
    String[] error = null;
    Object value = null;
    try {
      if (srv == null || attr == null) {
        error = new String[] {MonitorNotification.OBSERVED_ATTRIBUTE_ERROR,
            "The observed attribute should be accessible."};
      } else {
        value = srv.getAttribute(o.name, attr);
      }
    } catch (InstanceNotFoundException e) {
      error = new String[] {MonitorNotification.OBSERVED_OBJECT_ERROR, "The observed object should be accessible."};
    } catch (AttributeNotFoundException e) {
      error = new String[] {MonitorNotification.OBSERVED_ATTRIBUTE_ERROR,
          "The observed attribute should be accessible."};
    } catch (Exception e) {
      error = new String[] {MonitorNotification.RUNTIME_ERROR, e.toString()};
    }
    synchronized (this) {
      if (!isActive || !observed.containsKey(o.name)) {
        return;
      }
      if (error != null) {
        int flag = error[0].equals(MonitorNotification.OBSERVED_OBJECT_ERROR) ? OBSERVED_OBJECT_ERROR_NOTIFIED
            : error[0].equals(MonitorNotification.OBSERVED_ATTRIBUTE_ERROR) ? OBSERVED_ATTRIBUTE_ERROR_NOTIFIED
            : RUNTIME_ERROR_NOTIFIED;
        if ((o.notified & flag) == 0) {
          o.notified |= flag;
          emit(error[0], o, error[1], null, null);
        }
        return;
      }
      String typeError = checkType(value);
      if (typeError != null) {
        if ((o.notified & OBSERVED_ATTRIBUTE_TYPE_ERROR_NOTIFIED) == 0) {
          o.notified |= OBSERVED_ATTRIBUTE_TYPE_ERROR_NOTIFIED;
          emit(MonitorNotification.OBSERVED_ATTRIBUTE_TYPE_ERROR, o, typeError, null, null);
        }
        return;
      }
      o.notified = 0;
      try {
        evaluate(o, value);
      } catch (RuntimeException e) {
        emit(MonitorNotification.RUNTIME_ERROR, o, e.toString(), null, null);
      }
    }
  }

  void emit(String type, Observed o, String msg, Object derivedGauge, Object trigger) {
    sendNotification(new MonitorNotification(type, this, ++sequenceNumber, System.currentTimeMillis(), msg, o.name,
        observedAttribute, derivedGauge, trigger));
  }

  abstract void initObserved(Observed o);

  abstract String checkType(Object value);

  abstract void evaluate(Observed o, Object value);

  synchronized Observed observedFor(ObjectName name) {
    return observed.get(name);
  }

  public abstract void start();

  public abstract void stop();
}
