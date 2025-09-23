/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package sun.nio.ch;

import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketAddress;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.nio.channels.IllegalBlockingModeException;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;

class ServerSocketAdaptor extends ServerSocket {
  private final ServerSocketChannelImpl ssc;
  private volatile int timeout;

  ServerSocketAdaptor(ServerSocketChannelImpl ssc) throws IOException {
    this.ssc = ssc;
  }

  @Override
  public void bind(SocketAddress local) throws IOException {
    bind(local, 50);
  }

  @Override
  public void bind(SocketAddress local, int backlog) throws IOException {
    if (local == null) {
      local = new InetSocketAddress(0);
    }
    try {
      ssc.bind(local, backlog);
    } catch (java.nio.channels.AlreadyBoundException e) {
      throw new SocketException("Already bound");
    } catch (java.nio.channels.ClosedChannelException e) {
      throw new SocketException("Socket is closed");
    }
  }

  @Override
  public InetAddress getInetAddress() {
    try {
      SocketAddress a = ssc.getLocalAddress();
      return a == null ? null : ((InetSocketAddress) a).getAddress();
    } catch (IOException e) {
      return null;
    }
  }

  @Override
  public int getLocalPort() {
    try {
      SocketAddress a = ssc.getLocalAddress();
      return a == null ? -1 : ((InetSocketAddress) a).getPort();
    } catch (IOException e) {
      return -1;
    }
  }

  @Override
  public SocketAddress getLocalSocketAddress() {
    try {
      return ssc.getLocalAddress();
    } catch (IOException e) {
      return null;
    }
  }

  @Override
  public Socket accept() throws IOException {
    synchronized (ssc.blockingLock()) {
      if (!ssc.isBlocking()) {
        throw new IllegalBlockingModeException();
      }
      try {
        SocketChannel sc = ssc.accept();
        return sc.socket();
      } catch (java.nio.channels.ClosedChannelException e) {
        throw new SocketException("Socket is closed");
      } catch (java.nio.channels.NotYetBoundException e) {
        throw new SocketException("Socket is not bound yet");
      }
    }
  }

  @Override
  public void close() throws IOException {
    ssc.close();
  }

  @Override
  public ServerSocketChannel getChannel() {
    return ssc;
  }

  @Override
  public boolean isBound() {
    try {
      return ssc.getLocalAddress() != null;
    } catch (IOException e) {
      return false;
    }
  }

  @Override
  public boolean isClosed() {
    return !ssc.isOpen();
  }

  @Override
  public void setSoTimeout(int timeout) throws SocketException {
    this.timeout = timeout;
  }

  @Override
  public int getSoTimeout() throws SocketException {
    return timeout;
  }

  @Override
  public void setReuseAddress(boolean on) throws SocketException {
    try {
      ssc.setOption(java.net.StandardSocketOptions.SO_REUSEADDR, on);
    } catch (IOException e) {
      throw new SocketException(e.getMessage());
    }
  }

  @Override
  public boolean getReuseAddress() throws SocketException {
    try {
      return ssc.getOption(java.net.StandardSocketOptions.SO_REUSEADDR);
    } catch (IOException e) {
      throw new SocketException(e.getMessage());
    }
  }

  @Override
  public String toString() {
    if (!isBound()) {
      return "ServerSocket[unbound]";
    }
    return "ServerSocket[addr=" + getInetAddress() + ",localport=" + getLocalPort() + "]";
  }
}
