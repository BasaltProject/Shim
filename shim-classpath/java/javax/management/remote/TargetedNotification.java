/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package javax.management.remote;

import java.io.Serializable;
import javax.management.Notification;

public class TargetedNotification implements Serializable {
  private static final long serialVersionUID = 7676132089779300926L;

  private Notification notif;
  private final Integer id;

  public TargetedNotification(Notification notification, Integer listenerID) {
    if (notification == null) {
      throw new IllegalArgumentException("Invalid notification: null");
    }
    if (listenerID == null) {
      throw new IllegalArgumentException("Invalid listener ID: null");
    }
    this.notif = notification;
    this.id = listenerID;
  }

  public Notification getNotification() {
    return notif;
  }

  public Integer getListenerID() {
    return id;
  }

  public String toString() {
    return "{" + notif + ", " + id + "}";
  }
}
