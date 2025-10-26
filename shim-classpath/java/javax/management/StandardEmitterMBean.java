/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package javax.management;

public class StandardEmitterMBean extends StandardMBean implements NotificationEmitter {
  private final NotificationEmitter emitter;
  private final MBeanNotificationInfo[] notificationInfo;

  public <T> StandardEmitterMBean(T implementation, Class<T> mbeanInterface, NotificationEmitter emitter) {
    this(implementation, mbeanInterface, false, emitter);
  }

  public <T> StandardEmitterMBean(T implementation, Class<T> mbeanInterface, boolean isMXBean,
      NotificationEmitter emitter) {
    super(implementation, mbeanInterface, isMXBean);
    if (emitter == null) {
      throw new IllegalArgumentException("Null emitter");
    }
    this.emitter = emitter;
    MBeanNotificationInfo[] infos = emitter.getNotificationInfo();
    this.notificationInfo = infos == null || infos.length == 0 ? new MBeanNotificationInfo[0] : infos.clone();
  }

  protected StandardEmitterMBean(Class<?> mbeanInterface, NotificationEmitter emitter) {
    this(mbeanInterface, false, emitter);
  }

  protected StandardEmitterMBean(Class<?> mbeanInterface, boolean isMXBean, NotificationEmitter emitter) {
    super(mbeanInterface, isMXBean);
    if (emitter == null) {
      throw new IllegalArgumentException("Null emitter");
    }
    this.emitter = emitter;
    MBeanNotificationInfo[] infos = emitter.getNotificationInfo();
    this.notificationInfo = infos == null || infos.length == 0 ? new MBeanNotificationInfo[0] : infos.clone();
  }

  @Override
  public void removeNotificationListener(NotificationListener listener) throws ListenerNotFoundException {
    emitter.removeNotificationListener(listener);
  }

  @Override
  public void removeNotificationListener(NotificationListener listener, NotificationFilter filter, Object handback)
      throws ListenerNotFoundException {
    emitter.removeNotificationListener(listener, filter, handback);
  }

  @Override
  public void addNotificationListener(NotificationListener listener, NotificationFilter filter, Object handback) {
    emitter.addNotificationListener(listener, filter, handback);
  }

  @Override
  public MBeanNotificationInfo[] getNotificationInfo() {
    return notificationInfo.length == 0 ? notificationInfo : notificationInfo.clone();
  }

  public void sendNotification(Notification n) {
    if (emitter instanceof NotificationBroadcasterSupport) {
      ((NotificationBroadcasterSupport) emitter).sendNotification(n);
    } else {
      throw new ClassCastException("Cannot sendNotification when emitter is not an instance of "
          + "NotificationBroadcasterSupport: " + emitter.getClass().getName());
    }
  }

  @Override
  public MBeanInfo getMBeanInfo() {
    MBeanInfo base = super.getMBeanInfo();
    return new MBeanInfo(base.getClassName(), base.getDescription(), base.getAttributes(), base.getConstructors(),
        base.getOperations(), getNotificationInfo(), base.getDescriptor());
  }
}
