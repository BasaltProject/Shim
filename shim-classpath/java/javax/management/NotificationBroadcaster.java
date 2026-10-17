/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package javax.management;

public interface NotificationBroadcaster {
  void addNotificationListener(NotificationListener p0, NotificationFilter p1, Object p2)
      throws IllegalArgumentException;

  void removeNotificationListener(NotificationListener p0) throws ListenerNotFoundException;

  MBeanNotificationInfo[] getNotificationInfo();
}
