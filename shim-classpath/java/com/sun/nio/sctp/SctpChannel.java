/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package com.sun.nio.sctp;

public abstract class SctpChannel extends java.nio.channels.spi.AbstractSelectableChannel {
  protected SctpChannel(java.nio.channels.spi.SelectorProvider selectorProvider) {
    super(selectorProvider);
  }

  public abstract com.sun.nio.sctp.Association association() throws java.io.IOException;
  public abstract com.sun.nio.sctp.SctpChannel bind(java.net.SocketAddress socketAddress) throws java.io.IOException;
  public abstract com.sun.nio.sctp.SctpChannel bindAddress(java.net.InetAddress inetAddress) throws java.io.IOException;
  public abstract boolean connect(java.net.SocketAddress socketAddress, int i, int i2) throws java.io.IOException;
  public abstract boolean connect(java.net.SocketAddress socketAddress) throws java.io.IOException;
  public abstract boolean finishConnect() throws java.io.IOException;
  public abstract java.util.Set<java.net.SocketAddress> getAllLocalAddresses() throws java.io.IOException;
  public abstract <T> T getOption(com.sun.nio.sctp.SctpSocketOption<T> sctpSocketOption) throws java.io.IOException;
  public abstract java.util.Set<java.net.SocketAddress> getRemoteAddresses() throws java.io.IOException;
  public abstract boolean isConnectionPending();
  public static com.sun.nio.sctp.SctpChannel open() throws java.io.IOException {
    return (com.sun.nio.sctp.SctpChannel) sun.nio.ch.SctpProvider.open(0);
  }
  public static com.sun.nio.sctp.SctpChannel open(java.net.SocketAddress remote, int maxOutStreams, int maxInStreams) throws java.io.IOException {
    com.sun.nio.sctp.SctpChannel ch = open();
    try {
      ch.connect(remote, maxOutStreams, maxInStreams);
    } catch (java.io.IOException | RuntimeException e) {
      ch.close();
      throw e;
    }
    return ch;
  }
  public abstract <T> com.sun.nio.sctp.MessageInfo receive(java.nio.ByteBuffer byteBuffer, T t, com.sun.nio.sctp.NotificationHandler<T> notificationHandler) throws java.io.IOException;
  public abstract int send(java.nio.ByteBuffer byteBuffer, com.sun.nio.sctp.MessageInfo messageInfo) throws java.io.IOException;
  public abstract <T> com.sun.nio.sctp.SctpChannel setOption(com.sun.nio.sctp.SctpSocketOption<T> sctpSocketOption, T t) throws java.io.IOException;
  public abstract com.sun.nio.sctp.SctpChannel shutdown() throws java.io.IOException;
  public abstract java.util.Set<com.sun.nio.sctp.SctpSocketOption<?>> supportedOptions();
  public abstract com.sun.nio.sctp.SctpChannel unbindAddress(java.net.InetAddress inetAddress) throws java.io.IOException;
  public final int validOps() {
    return java.nio.channels.SelectionKey.OP_CONNECT | java.nio.channels.SelectionKey.OP_READ | java.nio.channels.SelectionKey.OP_WRITE;
  }
}
