/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package javax.naming.ldap;

public class UnsolicitedNotificationEvent extends java.util.EventObject {
  private static final long serialVersionUID = -2382603380799883705L;

  private UnsolicitedNotification notice;

  public UnsolicitedNotificationEvent(Object src, UnsolicitedNotification notice) {
    super(src);
    this.notice = notice;
  }

  public UnsolicitedNotification getNotification() {
    return notice;
  }

  public void dispatch(UnsolicitedNotificationListener listener) {
    listener.notificationReceived(this);
  }
}
