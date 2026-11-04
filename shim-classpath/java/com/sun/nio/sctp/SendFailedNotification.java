/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package com.sun.nio.sctp;

public abstract class SendFailedNotification implements com.sun.nio.sctp.Notification {
  protected SendFailedNotification() {
  }

  public abstract java.net.SocketAddress address();
  public abstract com.sun.nio.sctp.Association association();
  public abstract java.nio.ByteBuffer buffer();
  public abstract int errorCode();
  public abstract int streamNumber();
}
