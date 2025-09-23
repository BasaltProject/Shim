/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package shim;

import java.io.IOException;

public final class NetNative {
  private NetNative() { }

  public static final int OPT_REUSEADDR = 1;
  public static final int OPT_KEEPALIVE = 2;
  public static final int OPT_TCP_NODELAY = 3;
  public static final int OPT_RCVBUF = 4;
  public static final int OPT_SNDBUF = 5;
  public static final int OPT_LINGER = 6;
  public static final int OPT_BROADCAST = 7;
  public static final int OPT_TOS = 8;
  public static final int OPT_OOBINLINE = 9;
  public static final int OPT_REUSEPORT = 10;
  public static final int OPT_MULTICAST_TTL = 11;
  public static final int OPT_MULTICAST_LOOP = 12;
  public static final int OPT_MULTICAST_IF = 13;

  public static native void init();

  public static native long socket(boolean preferV6, boolean datagram) throws IOException;

  public static native void bind(long handle, byte[] addr, int port) throws IOException;

  public static native void listen(long handle, int backlog) throws IOException;

  public static native long accept(long handle, int timeoutMs, byte[][] peerAddr, int[] peerPort) throws IOException;

  public static native void connect(long handle, byte[] addr, int port, int timeoutMs) throws IOException;

  public static native int read(long handle, byte[] buffer, int off, int len, int timeoutMs) throws IOException;

  public static native void write(long handle, byte[] buffer, int off, int len) throws IOException;

  public static native int sendTo(long handle, byte[] buffer, int off, int len, byte[] addr, int port)
      throws IOException;

  public static native int recvFrom(long handle, byte[] buffer, int off, int len, int timeoutMs, byte[][] srcAddr,
      int[] srcPort, boolean peek) throws IOException;

  public static native int available(long handle);

  public static native void sendUrgent(long handle, int data) throws IOException;

  public static native void shutdown(long handle, int how);

  public static native void close(long handle);

  public static native byte[] sockName(long handle, boolean peer, int[] port);

  public static native void setOption(long handle, int option, int value) throws IOException;

  public static native int getOption(long handle, int option) throws IOException;

  public static native void membership(long handle, byte[] group, int ifIndex, boolean join) throws IOException;

  public static native byte[][] lookup(String host);

  public static native String reverse(byte[] addr);

  public static native String localHostName();

  public static native Object[] interfaces();

  public static native void setBlocking(long handle, boolean blocking);

  public static native int readNb(long handle, byte[] buffer, int off, int len) throws IOException;

  public static native int writeNb(long handle, byte[] buffer, int off, int len) throws IOException;

  public static native long acceptNb(long handle, byte[][] peerAddr, int[] peerPort) throws IOException;

  public static native boolean connectNb(long handle, byte[] addr, int port) throws IOException;

  public static native boolean finishConnect(long handle, int timeoutMs) throws IOException;

  public static native int sendToNb(long handle, byte[] buffer, int off, int len, byte[] addr, int port)
      throws IOException;

  public static native int recvFromNb(long handle, byte[] buffer, int off, int len, byte[][] srcAddr, int[] srcPort)
      throws IOException;

  public static native int poll(long[] handles, int[] events, int[] revents, int count, int timeoutMs)
      throws IOException;

  public static native void socketPair(long[] out) throws IOException;

  public static native void disconnect(long handle) throws IOException;
}
