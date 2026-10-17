/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package javax.management;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.Executor;

public class NotificationBroadcasterSupport implements NotificationEmitter {
  private static final class ListenerInfo {
    final NotificationListener listener;
    final NotificationFilter filter;
    final Object handback;

    ListenerInfo(NotificationListener listener, NotificationFilter filter, Object handback) {
      this.listener = listener;
      this.filter = filter;
      this.handback = handback;
    }

    boolean matches(NotificationListener l, NotificationFilter f, Object h) {
      return listener == l && filter == f && handback == h;
    }
  }

  private final Executor executor;
  private final MBeanNotificationInfo[] notifInfo;
  private final List<ListenerInfo> listenerList = new CopyOnWriteArrayList<ListenerInfo>();

  public NotificationBroadcasterSupport() {
    this(null, (MBeanNotificationInfo[]) null);
  }

  public NotificationBroadcasterSupport(Executor executor) {
    this(executor, (MBeanNotificationInfo[]) null);
  }

  public NotificationBroadcasterSupport(MBeanNotificationInfo... info) {
    this(null, info);
  }

  public NotificationBroadcasterSupport(Executor executor, MBeanNotificationInfo... info) {
    this.executor = executor;
    this.notifInfo = info == null ? new MBeanNotificationInfo[0] : info.clone();
  }

  @Override
  public void addNotificationListener(NotificationListener listener, NotificationFilter filter, Object handback) {
    if (listener == null) {
      throw new IllegalArgumentException("Listener can't be null");
    }
    listenerList.add(new ListenerInfo(listener, filter, handback));
  }

  @Override
  public void removeNotificationListener(NotificationListener listener) throws ListenerNotFoundException {
    boolean removed = false;
    for (ListenerInfo li : listenerList) {
      if (li.listener == listener) {
        listenerList.remove(li);
        removed = true;
      }
    }
    if (!removed) {
      throw new ListenerNotFoundException("Listener not registered");
    }
  }

  @Override
  public void removeNotificationListener(NotificationListener listener, NotificationFilter filter, Object handback)
      throws ListenerNotFoundException {
    for (ListenerInfo li : listenerList) {
      if (li.matches(listener, filter, handback)) {
        listenerList.remove(li);
        return;
      }
    }
    throw new ListenerNotFoundException("Listener not registered (with this filter and handback)");
  }

  @Override
  public MBeanNotificationInfo[] getNotificationInfo() {
    return notifInfo.length == 0 ? notifInfo : notifInfo.clone();
  }

  public void sendNotification(Notification notification) {
    if (notification == null) {
      return;
    }
    for (final ListenerInfo li : listenerList) {
      boolean enabled;
      try {
        enabled = li.filter == null || li.filter.isNotificationEnabled(notification);
      } catch (Exception e) {
        continue;
      }
      if (!enabled) {
        continue;
      }
      final Notification n = notification;
      Runnable job = new Runnable() {
        @Override
        public void run() {
          try {
            handleNotification(li.listener, n, li.handback);
          } catch (Exception e) {
            // listener failures never reach the sender
          }
        }
      };
      if (executor == null) {
        job.run();
      } else {
        executor.execute(job);
      }
    }
  }

  protected void handleNotification(NotificationListener listener, Notification notif, Object handback) {
    listener.handleNotification(notif, handback);
  }
}
