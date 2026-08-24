/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package javax.management.timer;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Vector;
import javax.management.InstanceNotFoundException;
import javax.management.MBeanRegistration;
import javax.management.MBeanServer;
import javax.management.NotificationBroadcasterSupport;
import javax.management.ObjectName;

public class Timer extends NotificationBroadcasterSupport implements TimerMBean, MBeanRegistration {
  public static final long ONE_SECOND = 1000L;
  public static final long ONE_MINUTE = 60 * ONE_SECOND;
  public static final long ONE_HOUR = 60 * ONE_MINUTE;
  public static final long ONE_DAY = 24 * ONE_HOUR;
  public static final long ONE_WEEK = 7 * ONE_DAY;

  private static final class Entry {
    final Integer id;
    final String type;
    final String message;
    final Object userData;
    Date date;
    final long period;
    long remaining;
    final boolean fixedRate;

    Entry(Integer id, String type, String message, Object userData, Date date, long period, long nb,
        boolean fixedRate) {
      this.id = id;
      this.type = type;
      this.message = message;
      this.userData = userData;
      this.date = date;
      this.period = period;
      this.remaining = nb;
      this.fixedRate = fixedRate;
    }
  }

  private final Map<Integer, Entry> table = new HashMap<Integer, Entry>();
  private transient volatile boolean isActive = false;
  private transient boolean sendPastNotifications = false;
  private transient long sequenceNumber = 0;
  private transient int counter = 0;
  private transient java.util.Timer scheduler;

  public Timer() {
  }

  public ObjectName preRegister(MBeanServer server, ObjectName name) throws Exception {
    return name;
  }

  public void postRegister(Boolean registrationDone) {
  }

  public synchronized void preDeregister() throws Exception {
    if (isActive()) {
      stop();
    }
  }

  public void postDeregister() {
  }

  public synchronized void start() {
    if (isActive) {
      return;
    }
    isActive = true;
    long now = System.currentTimeMillis();
    for (Entry e : new Vector<Entry>(table.values())) {
      if (e.date.getTime() < now) {
        handlePast(e, now);
      }
    }
    scheduler = new java.util.Timer("jmx.timer", true);
    for (Entry e : new Vector<Entry>(table.values())) {
      schedule(e);
    }
  }

  public synchronized void stop() {
    isActive = false;
    if (scheduler != null) {
      scheduler.cancel();
      scheduler = null;
    }
  }

  private void handlePast(Entry e, long now) {
    if (sendPastNotifications) {
      return;
    }
    if (e.period > 0) {
      long missed = (now - e.date.getTime()) / e.period + 1;
      if (e.remaining > 0) {
        if (missed >= e.remaining) {
          table.remove(e.id);
          return;
        }
        e.remaining -= missed;
      }
      e.date = new Date(e.date.getTime() + missed * e.period);
    } else {
      table.remove(e.id);
    }
  }

  private void schedule(final Entry e) {
    if (scheduler == null || !table.containsKey(e.id)) {
      return;
    }
    long delay = Math.max(0, e.date.getTime() - System.currentTimeMillis());
    try {
      scheduler.schedule(new java.util.TimerTask() {
        @Override
        public void run() {
          fire(e);
        }
      }, delay);
    } catch (IllegalStateException ex) {
    }
  }

  private void fire(Entry e) {
    TimerNotificationHolder n;
    synchronized (this) {
      if (!isActive || table.get(e.id) != e) {
        return;
      }
      n = new TimerNotificationHolder(new TimerNotification(e.type, this, ++sequenceNumber,
          System.currentTimeMillis(), e.message, e.id));
      n.notification.setUserData(e.userData);
      if (e.period > 0 && (e.remaining == 0 || e.remaining > 1)) {
        if (e.remaining > 1) {
          e.remaining--;
        }
        long base = e.fixedRate ? e.date.getTime() : System.currentTimeMillis();
        e.date = new Date(base + e.period);
        schedule(e);
      } else {
        table.remove(e.id);
      }
    }
    sendNotification(n.notification);
  }

  private static final class TimerNotificationHolder {
    final TimerNotification notification;

    TimerNotificationHolder(TimerNotification n) {
      notification = n;
    }
  }

  public synchronized Integer addNotification(String type, String message, Object userData, Date date, long period,
      long nbOccurences, boolean fixedRate) throws IllegalArgumentException {
    if (date == null) {
      throw new IllegalArgumentException("Timer notification date cannot be null.");
    }
    if ((period < 0) || (nbOccurences < 0)) {
      throw new IllegalArgumentException("Negative values for the periodicity");
    }
    Date d = new Date(date.getTime());
    long now = System.currentTimeMillis();
    if (d.getTime() < now && isActive && !sendPastNotifications) {
      if (period > 0) {
        long missed = (now - d.getTime()) / period + 1;
        d = new Date(d.getTime() + missed * period);
        if (nbOccurences > 0) {
          if (missed >= nbOccurences) {
            Integer id = ++counter;
            return id;
          }
          nbOccurences -= missed;
        }
      } else {
        Integer id = ++counter;
        return id;
      }
    }
    Integer id = ++counter;
    Entry e = new Entry(id, type, message, userData, d, period, nbOccurences, fixedRate);
    table.put(id, e);
    if (isActive) {
      schedule(e);
    }
    return id;
  }

  public synchronized Integer addNotification(String type, String message, Object userData, Date date, long period,
      long nbOccurences) throws IllegalArgumentException {
    return addNotification(type, message, userData, date, period, nbOccurences, false);
  }

  public synchronized Integer addNotification(String type, String message, Object userData, Date date, long period)
      throws IllegalArgumentException {
    return addNotification(type, message, userData, date, period, 0);
  }

  public synchronized Integer addNotification(String type, String message, Object userData, Date date)
      throws IllegalArgumentException {
    return addNotification(type, message, userData, date, 0, 0);
  }

  public synchronized void removeNotification(Integer id) throws InstanceNotFoundException {
    if (table.remove(id) == null) {
      throw new InstanceNotFoundException("Timer notification to remove not in the list of notifications");
    }
  }

  public synchronized void removeNotifications(String type) throws InstanceNotFoundException {
    Vector<Integer> ids = getNotificationIDs(type);
    if (ids.isEmpty()) {
      throw new InstanceNotFoundException("Timer notifications to remove not in the list of notifications");
    }
    for (Integer id : ids) {
      table.remove(id);
    }
  }

  public synchronized void removeAllNotifications() {
    table.clear();
    counter = 0;
  }

  public int getNbNotifications() {
    synchronized (this) {
      return table.size();
    }
  }

  public synchronized Vector<Integer> getAllNotificationIDs() {
    return new Vector<Integer>(table.keySet());
  }

  public synchronized Vector<Integer> getNotificationIDs(String type) {
    Vector<Integer> out = new Vector<Integer>();
    for (Entry e : table.values()) {
      if (type == null ? e.type == null : type.equals(e.type)) {
        out.add(e.id);
      }
    }
    return out;
  }

  public synchronized String getNotificationType(Integer id) {
    Entry e = table.get(id);
    return e == null ? null : e.type;
  }

  public synchronized String getNotificationMessage(Integer id) {
    Entry e = table.get(id);
    return e == null ? null : e.message;
  }

  public synchronized Object getNotificationUserData(Integer id) {
    Entry e = table.get(id);
    return e == null ? null : e.userData;
  }

  public synchronized Date getDate(Integer id) {
    Entry e = table.get(id);
    return e == null ? null : new Date(e.date.getTime());
  }

  public synchronized Long getPeriod(Integer id) {
    Entry e = table.get(id);
    return e == null ? null : Long.valueOf(e.period);
  }

  public synchronized Long getNbOccurences(Integer id) {
    Entry e = table.get(id);
    return e == null ? null : Long.valueOf(e.remaining);
  }

  public synchronized Boolean getFixedRate(Integer id) {
    Entry e = table.get(id);
    return e == null ? null : Boolean.valueOf(e.fixedRate);
  }

  public boolean getSendPastNotifications() {
    return sendPastNotifications;
  }

  public void setSendPastNotifications(boolean value) {
    sendPastNotifications = value;
  }

  public boolean isActive() {
    return isActive;
  }

  public synchronized boolean isEmpty() {
    return table.isEmpty();
  }
}
