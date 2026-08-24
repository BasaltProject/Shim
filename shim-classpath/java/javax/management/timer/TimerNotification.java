/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package javax.management.timer;

import javax.management.Notification;

public class TimerNotification extends Notification {
  private static final long serialVersionUID = 1798492029603825750L;

  private Integer notificationID;

  public TimerNotification(String type, Object source, long sequenceNumber, long timeStamp, String msg, Integer id) {
    super(type, source, sequenceNumber, timeStamp, msg);
    this.notificationID = id;
  }

  public Integer getNotificationID() {
    return notificationID;
  }
}
