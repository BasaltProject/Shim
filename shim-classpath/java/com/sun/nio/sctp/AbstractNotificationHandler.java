/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package com.sun.nio.sctp;

public class AbstractNotificationHandler<T> implements com.sun.nio.sctp.NotificationHandler<T> {
  protected AbstractNotificationHandler() {
  }

  public com.sun.nio.sctp.HandlerResult handleNotification(com.sun.nio.sctp.AssociationChangeNotification associationChangeNotification, T t) {
    return com.sun.nio.sctp.HandlerResult.CONTINUE;
  }
  public com.sun.nio.sctp.HandlerResult handleNotification(com.sun.nio.sctp.PeerAddressChangeNotification peerAddressChangeNotification, T t) {
    return com.sun.nio.sctp.HandlerResult.CONTINUE;
  }
  public com.sun.nio.sctp.HandlerResult handleNotification(com.sun.nio.sctp.SendFailedNotification sendFailedNotification, T t) {
    return com.sun.nio.sctp.HandlerResult.CONTINUE;
  }
  public com.sun.nio.sctp.HandlerResult handleNotification(com.sun.nio.sctp.ShutdownNotification shutdownNotification, T t) {
    return com.sun.nio.sctp.HandlerResult.CONTINUE;
  }
  public com.sun.nio.sctp.HandlerResult handleNotification(com.sun.nio.sctp.Notification notification, T t) {
    if (notification instanceof com.sun.nio.sctp.AssociationChangeNotification) {
      return handleNotification((com.sun.nio.sctp.AssociationChangeNotification) notification, t);
    }
    if (notification instanceof com.sun.nio.sctp.PeerAddressChangeNotification) {
      return handleNotification((com.sun.nio.sctp.PeerAddressChangeNotification) notification, t);
    }
    if (notification instanceof com.sun.nio.sctp.SendFailedNotification) {
      return handleNotification((com.sun.nio.sctp.SendFailedNotification) notification, t);
    }
    if (notification instanceof com.sun.nio.sctp.ShutdownNotification) {
      return handleNotification((com.sun.nio.sctp.ShutdownNotification) notification, t);
    }
    return com.sun.nio.sctp.HandlerResult.CONTINUE;
  }
}
