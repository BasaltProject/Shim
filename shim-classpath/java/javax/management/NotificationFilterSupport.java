/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package javax.management;

import java.util.Vector;

public class NotificationFilterSupport implements NotificationFilter {
  private static final long serialVersionUID = 6579080007561786969L;

  private final Vector<String> enabledTypes = new Vector<String>();

  public NotificationFilterSupport() {
  }

  @Override
  public synchronized boolean isNotificationEnabled(Notification notification) {
    String type = notification.getType();
    if (type == null) {
      return false;
    }
    for (String prefix : enabledTypes) {
      if (type.startsWith(prefix)) {
        return true;
      }
    }
    return false;
  }

  public synchronized void enableType(String prefix) throws IllegalArgumentException {
    if (prefix == null) {
      throw new IllegalArgumentException("The prefix cannot be null.");
    }
    if (!enabledTypes.contains(prefix)) {
      enabledTypes.addElement(prefix);
    }
  }

  public synchronized void disableType(String prefix) {
    enabledTypes.remove(prefix);
  }

  public synchronized void disableAllTypes() {
    enabledTypes.clear();
  }

  public synchronized Vector<String> getEnabledTypes() {
    return enabledTypes;
  }
}
