/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package sun.management;

import java.lang.management.MemoryMXBean;
import java.lang.management.MemoryUsage;
import java.util.concurrent.CopyOnWriteArrayList;
import javax.management.ListenerNotFoundException;
import javax.management.MBeanNotificationInfo;
import javax.management.NotificationEmitter;
import javax.management.NotificationFilter;
import javax.management.NotificationListener;
import javax.management.ObjectName;

public class MemoryImpl implements MemoryMXBean, NotificationEmitter {
  private volatile boolean verbose;
  private final CopyOnWriteArrayList<NotificationListener> listeners = new CopyOnWriteArrayList<NotificationListener>();

  @Override
  public void addNotificationListener(NotificationListener listener, NotificationFilter filter, Object handback) {
    if (listener == null) throw new IllegalArgumentException("Listener can't be null");
    listeners.add(listener);
  }

  @Override
  public void removeNotificationListener(NotificationListener listener) throws ListenerNotFoundException {
    if (!listeners.remove(listener)) throw new ListenerNotFoundException("Listener not registered");
  }

  @Override
  public void removeNotificationListener(NotificationListener listener, NotificationFilter filter, Object handback)
      throws ListenerNotFoundException {
    removeNotificationListener(listener);
  }

  @Override
  public MBeanNotificationInfo[] getNotificationInfo() {
    return new MBeanNotificationInfo[0];
  }

  @Override
  public int getObjectPendingFinalizationCount() {
    return 0;
  }

  @Override
  public MemoryUsage getHeapMemoryUsage() {
    Runtime r = Runtime.getRuntime();
    long committed = r.totalMemory();
    long used = Math.min(committed, committed - r.freeMemory());
    long max = r.maxMemory();
    return new MemoryUsage(committed, used, committed, Math.max(max, committed));
  }

  @Override
  public MemoryUsage getNonHeapMemoryUsage() {
    return new MemoryUsage(0, 0, 0, -1);
  }

  @Override
  public boolean isVerbose() {
    return verbose;
  }

  @Override
  public void setVerbose(boolean value) {
    verbose = value;
  }

  @Override
  public void gc() {
    Runtime.getRuntime().gc();
  }

  @Override
  public ObjectName getObjectName() {
    try {
      return new ObjectName("java.lang:type=Memory");
    } catch (javax.management.MalformedObjectNameException e) {
      throw new IllegalArgumentException(e);
    }
  }
}
