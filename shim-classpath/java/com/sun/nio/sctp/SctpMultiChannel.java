/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package com.sun.nio.sctp;

public abstract class SctpMultiChannel extends java.nio.channels.spi.AbstractSelectableChannel {
  protected SctpMultiChannel(java.nio.channels.spi.SelectorProvider selectorProvider) {
    super(selectorProvider);
  }

  public abstract java.util.Set<com.sun.nio.sctp.Association> associations() throws java.io.IOException;
  public abstract com.sun.nio.sctp.SctpMultiChannel bind(java.net.SocketAddress socketAddress, int i) throws java.io.IOException;
  public final com.sun.nio.sctp.SctpMultiChannel bind(java.net.SocketAddress socketAddress) throws java.io.IOException {
    return bind(socketAddress, 0);
  }
  public abstract com.sun.nio.sctp.SctpMultiChannel bindAddress(java.net.InetAddress inetAddress) throws java.io.IOException;
  public abstract com.sun.nio.sctp.SctpChannel branch(com.sun.nio.sctp.Association association) throws java.io.IOException;
  public abstract java.util.Set<java.net.SocketAddress> getAllLocalAddresses() throws java.io.IOException;
  public abstract <T> T getOption(com.sun.nio.sctp.SctpSocketOption<T> sctpSocketOption, com.sun.nio.sctp.Association association) throws java.io.IOException;
  public abstract java.util.Set<java.net.SocketAddress> getRemoteAddresses(com.sun.nio.sctp.Association association) throws java.io.IOException;
  public static com.sun.nio.sctp.SctpMultiChannel open() throws java.io.IOException {
    return (com.sun.nio.sctp.SctpMultiChannel) sun.nio.ch.SctpProvider.open(2);
  }
  public abstract <T> com.sun.nio.sctp.MessageInfo receive(java.nio.ByteBuffer byteBuffer, T t, com.sun.nio.sctp.NotificationHandler<T> notificationHandler) throws java.io.IOException;
  public abstract int send(java.nio.ByteBuffer byteBuffer, com.sun.nio.sctp.MessageInfo messageInfo) throws java.io.IOException;
  public abstract <T> com.sun.nio.sctp.SctpMultiChannel setOption(com.sun.nio.sctp.SctpSocketOption<T> sctpSocketOption, T t, com.sun.nio.sctp.Association association) throws java.io.IOException;
  public abstract com.sun.nio.sctp.SctpMultiChannel shutdown(com.sun.nio.sctp.Association association) throws java.io.IOException;
  public abstract java.util.Set<com.sun.nio.sctp.SctpSocketOption<?>> supportedOptions();
  public abstract com.sun.nio.sctp.SctpMultiChannel unbindAddress(java.net.InetAddress inetAddress) throws java.io.IOException;
  public final int validOps() {
    return java.nio.channels.SelectionKey.OP_READ | java.nio.channels.SelectionKey.OP_WRITE;
  }
}
