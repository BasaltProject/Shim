/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package javax.management;

public class MBeanServerDelegate implements MBeanServerDelegateMBean, NotificationEmitter {
  public static final ObjectName DELEGATE_NAME = delegateName();

  private static ObjectName delegateName() {
    try {
      return new ObjectName("JMImplementation:type=MBeanServerDelegate");
    } catch (MalformedObjectNameException e) {
      throw new Error(e);
    }
  }

  private String mbeanServerId;
  private final NotificationBroadcasterSupport broadcaster = new NotificationBroadcasterSupport();
  private static long oldStamp = 0;
  private final long stamp;
  private long sequenceNumber = 1;

  private static final MBeanNotificationInfo[] kNotifs = {new MBeanNotificationInfo(
      new String[] {MBeanServerNotification.UNREGISTRATION_NOTIFICATION,
          MBeanServerNotification.REGISTRATION_NOTIFICATION},
      MBeanServerNotification.class.getName(), "Notifications sent by the MBeanServerDelegate MBean")};

  public MBeanServerDelegate() {
    stamp = getStamp();
  }

  private static synchronized long getStamp() {
    long s = System.currentTimeMillis();
    if (oldStamp >= s) {
      s = oldStamp + 1;
    }
    oldStamp = s;
    return s;
  }

  @Override
  public synchronized String getMBeanServerId() {
    if (mbeanServerId == null) {
      String localHost;
      try {
        localHost = java.net.InetAddress.getLocalHost().getHostName();
      } catch (java.net.UnknownHostException e) {
        localHost = "localhost";
      }
      mbeanServerId = localHost + "_" + stamp;
    }
    return mbeanServerId;
  }

  @Override
  public String getSpecificationName() {
    return "Java Management Extensions";
  }

  @Override
  public String getSpecificationVersion() {
    return "1.4";
  }

  @Override
  public String getSpecificationVendor() {
    return "Oracle Corporation";
  }

  @Override
  public String getImplementationName() {
    return "JMX";
  }

  @Override
  public String getImplementationVersion() {
    return System.getProperty("java.runtime.version");
  }

  @Override
  public String getImplementationVendor() {
    return "Oracle Corporation";
  }

  @Override
  public MBeanNotificationInfo[] getNotificationInfo() {
    return kNotifs.clone();
  }

  @Override
  public synchronized void addNotificationListener(NotificationListener listener, NotificationFilter filter,
      Object handback) throws IllegalArgumentException {
    broadcaster.addNotificationListener(listener, filter, handback);
  }

  @Override
  public synchronized void removeNotificationListener(NotificationListener listener, NotificationFilter filter,
      Object handback) throws ListenerNotFoundException {
    broadcaster.removeNotificationListener(listener, filter, handback);
  }

  @Override
  public synchronized void removeNotificationListener(NotificationListener listener)
      throws ListenerNotFoundException {
    broadcaster.removeNotificationListener(listener);
  }

  public void sendNotification(Notification notification) {
    if (notification.getSequenceNumber() < 1) {
      synchronized (this) {
        notification.setSequenceNumber(sequenceNumber++);
      }
    }
    broadcaster.sendNotification(notification);
  }
}
