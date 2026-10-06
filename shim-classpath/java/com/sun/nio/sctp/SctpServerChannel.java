/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package com.sun.nio.sctp;

public abstract class SctpServerChannel extends java.nio.channels.spi.AbstractSelectableChannel {
  protected SctpServerChannel(java.nio.channels.spi.SelectorProvider selectorProvider) {
    super(selectorProvider);
  }

  public abstract com.sun.nio.sctp.SctpChannel accept() throws java.io.IOException;
  public abstract com.sun.nio.sctp.SctpServerChannel bind(java.net.SocketAddress socketAddress, int i) throws java.io.IOException;
  public final com.sun.nio.sctp.SctpServerChannel bind(java.net.SocketAddress socketAddress) throws java.io.IOException {
    return bind(socketAddress, 0);
  }
  public abstract com.sun.nio.sctp.SctpServerChannel bindAddress(java.net.InetAddress inetAddress) throws java.io.IOException;
  public abstract java.util.Set<java.net.SocketAddress> getAllLocalAddresses() throws java.io.IOException;
  public abstract <T> T getOption(com.sun.nio.sctp.SctpSocketOption<T> sctpSocketOption) throws java.io.IOException;
  public static com.sun.nio.sctp.SctpServerChannel open() throws java.io.IOException {
    return (com.sun.nio.sctp.SctpServerChannel) sun.nio.ch.SctpProvider.open(1);
  }
  public abstract <T> com.sun.nio.sctp.SctpServerChannel setOption(com.sun.nio.sctp.SctpSocketOption<T> sctpSocketOption, T t) throws java.io.IOException;
  public abstract java.util.Set<com.sun.nio.sctp.SctpSocketOption<?>> supportedOptions();
  public abstract com.sun.nio.sctp.SctpServerChannel unbindAddress(java.net.InetAddress inetAddress) throws java.io.IOException;
  public final int validOps() {
    return java.nio.channels.SelectionKey.OP_ACCEPT;
  }
}
