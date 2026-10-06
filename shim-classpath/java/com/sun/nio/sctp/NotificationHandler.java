/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package com.sun.nio.sctp;

public interface NotificationHandler<T> {
  com.sun.nio.sctp.HandlerResult handleNotification(com.sun.nio.sctp.Notification notification, T t);
}
