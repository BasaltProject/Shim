/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package javax.naming.ldap;

public interface UnsolicitedNotificationListener extends javax.naming.event.NamingListener {
  void notificationReceived(javax.naming.ldap.UnsolicitedNotificationEvent unsolicitedNotificationEvent);
}
