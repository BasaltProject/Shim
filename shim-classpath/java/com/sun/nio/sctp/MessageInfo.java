/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package com.sun.nio.sctp;

public abstract class MessageInfo {
  protected MessageInfo() {
  }

  public abstract java.net.SocketAddress address();
  public abstract com.sun.nio.sctp.Association association();
  public abstract int bytes();
  public abstract com.sun.nio.sctp.MessageInfo complete(boolean flag);
  public static com.sun.nio.sctp.MessageInfo createOutgoing(com.sun.nio.sctp.Association association, java.net.SocketAddress address, int streamNumber) {
    if (association == null) {
      throw new IllegalArgumentException("association cannot be null");
    }
    if (streamNumber < 0 || streamNumber > 65536) {
      throw new IllegalArgumentException("Invalid stream number");
    }
    return new sun.nio.ch.SctpMessageInfoImpl(association, address, streamNumber);
  }
  public static com.sun.nio.sctp.MessageInfo createOutgoing(java.net.SocketAddress address, int streamNumber) {
    if (streamNumber < 0 || streamNumber > 65536) {
      throw new IllegalArgumentException("Invalid stream number");
    }
    return new sun.nio.ch.SctpMessageInfoImpl(0, address, streamNumber);
  }
  public abstract boolean isComplete();
  public abstract boolean isUnordered();
  public abstract int payloadProtocolID();
  public abstract com.sun.nio.sctp.MessageInfo payloadProtocolID(int i);
  public abstract int streamNumber();
  public abstract com.sun.nio.sctp.MessageInfo streamNumber(int i);
  public abstract long timeToLive();
  public abstract com.sun.nio.sctp.MessageInfo timeToLive(long l);
  public abstract com.sun.nio.sctp.MessageInfo unordered(boolean flag);
}
