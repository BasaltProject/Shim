/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package javax.management.remote;

import javax.management.Notification;

public class JMXConnectionNotification extends Notification {
  private static final long serialVersionUID = -2331308725952627538L;

  public static final String OPENED = "jmx.remote.connection.opened";
  public static final String CLOSED = "jmx.remote.connection.closed";
  public static final String FAILED = "jmx.remote.connection.failed";
  public static final String NOTIFS_LOST = "jmx.remote.connection.notifs.lost";

  private final String connectionId;

  public JMXConnectionNotification(String type, Object source, String connectionId, long sequenceNumber,
      String message, Object userData) {
    super(type, source, Math.max(0, sequenceNumber), System.currentTimeMillis(), message);
    if (type == null || source == null || connectionId == null) {
      throw new NullPointerException("Illegal null argument");
    }
    if (sequenceNumber < 0) {
      throw new IllegalArgumentException("Negative sequence number");
    }
    this.connectionId = connectionId;
    setUserData(userData);
  }

  public String getConnectionId() {
    return connectionId;
  }
}
