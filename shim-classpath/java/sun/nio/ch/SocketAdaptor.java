/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package sun.nio.ch;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketAddress;
import java.net.SocketException;
import java.net.SocketOption;
import java.net.StandardSocketOptions;
import java.nio.ByteBuffer;
import java.nio.channels.IllegalBlockingModeException;
import java.nio.channels.SocketChannel;
import java.nio.channels.UnresolvedAddressException;

class SocketAdaptor extends Socket {
  private final SocketChannelImpl sc;
  private volatile int timeout;

  SocketAdaptor(SocketChannelImpl sc) {
    this.sc = sc;
  }

  @Override
  public SocketChannel getChannel() {
    return sc;
  }

  @Override
  public void connect(SocketAddress remote) throws IOException {
    connect(remote, 0);
  }

  @Override
  public void connect(SocketAddress remote, int timeout) throws IOException {
    if (remote == null) {
      throw new IllegalArgumentException("connect: The address can't be null");
    }
    if (timeout < 0) {
      throw new IllegalArgumentException("connect: timeout can't be negative");
    }
    synchronized (sc.blockingLock()) {
      if (!sc.isBlocking()) {
        throw new IllegalBlockingModeException();
      }
      try {
        sc.connect(remote);
      } catch (java.nio.channels.AlreadyConnectedException e) {
        throw new SocketException("Already connected");
      } catch (java.nio.channels.ClosedChannelException e) {
        throw new SocketException("Socket is closed");
      } catch (UnresolvedAddressException e) {
        throw new java.net.UnknownHostException(remote.toString());
      }
    }
  }

  @Override
  public void bind(SocketAddress local) throws IOException {
    try {
      sc.bind(local);
    } catch (java.nio.channels.AlreadyBoundException e) {
      throw new SocketException("Already bound");
    } catch (java.nio.channels.ClosedChannelException e) {
      throw new SocketException("Socket is closed");
    }
  }

  @Override
  public InetAddress getInetAddress() {
    SocketAddress remote = remoteAddress();
    return remote == null ? null : ((InetSocketAddress) remote).getAddress();
  }

  @Override
  public InetAddress getLocalAddress() {
    SocketAddress local = localAddress();
    return local == null ? InetAddress.getLoopbackAddress() : ((InetSocketAddress) local).getAddress();
  }

  @Override
  public int getPort() {
    SocketAddress remote = remoteAddress();
    return remote == null ? 0 : ((InetSocketAddress) remote).getPort();
  }

  @Override
  public int getLocalPort() {
    SocketAddress local = localAddress();
    return local == null ? -1 : ((InetSocketAddress) local).getPort();
  }

  private SocketAddress remoteAddress() {
    try {
      return sc.getRemoteAddress();
    } catch (IOException e) {
      return null;
    }
  }

  private SocketAddress localAddress() {
    try {
      return sc.getLocalAddress();
    } catch (IOException e) {
      return null;
    }
  }

  @Override
  public SocketAddress getRemoteSocketAddress() {
    return remoteAddress();
  }

  @Override
  public SocketAddress getLocalSocketAddress() {
    return localAddress();
  }

  @Override
  public InputStream getInputStream() throws IOException {
    if (!sc.isOpen()) {
      throw new SocketException("Socket is closed");
    }
    if (!sc.isConnected()) {
      throw new SocketException("Socket is not connected");
    }
    return new InputStream() {
      @Override
      public int read() throws IOException {
        byte[] one = new byte[1];
        int n = read(one, 0, 1);
        return n < 0 ? -1 : (one[0] & 0xff);
      }

      @Override
      public int read(byte[] b, int off, int len) throws IOException {
        synchronized (sc.blockingLock()) {
          if (!sc.isBlocking()) {
            throw new IllegalBlockingModeException();
          }
          return sc.read(ByteBuffer.wrap(b, off, len));
        }
      }

      @Override
      public void close() throws IOException {
        sc.close();
      }
    };
  }

  @Override
  public OutputStream getOutputStream() throws IOException {
    if (!sc.isOpen()) {
      throw new SocketException("Socket is closed");
    }
    if (!sc.isConnected()) {
      throw new SocketException("Socket is not connected");
    }
    return new OutputStream() {
      @Override
      public void write(int b) throws IOException {
        write(new byte[] {(byte) b}, 0, 1);
      }

      @Override
      public void write(byte[] b, int off, int len) throws IOException {
        synchronized (sc.blockingLock()) {
          if (!sc.isBlocking()) {
            throw new IllegalBlockingModeException();
          }
          sc.write(ByteBuffer.wrap(b, off, len));
        }
      }

      @Override
      public void close() throws IOException {
        sc.close();
      }
    };
  }

  private <T> void setOpt(SocketOption<T> name, T value) throws SocketException {
    try {
      sc.setOption(name, value);
    } catch (IOException e) {
      throw new SocketException(e.getMessage());
    }
  }

  private <T> T getOpt(SocketOption<T> name) throws SocketException {
    try {
      return sc.getOption(name);
    } catch (IOException e) {
      throw new SocketException(e.getMessage());
    }
  }

  @Override
  public void setTcpNoDelay(boolean on) throws SocketException {
    setOpt(StandardSocketOptions.TCP_NODELAY, on);
  }

  @Override
  public boolean getTcpNoDelay() throws SocketException {
    return getOpt(StandardSocketOptions.TCP_NODELAY);
  }

  @Override
  public void setSoLinger(boolean on, int linger) throws SocketException {
    setOpt(StandardSocketOptions.SO_LINGER, on ? linger : -1);
  }

  @Override
  public int getSoLinger() throws SocketException {
    return getOpt(StandardSocketOptions.SO_LINGER);
  }

  @Override
  public void setKeepAlive(boolean on) throws SocketException {
    setOpt(StandardSocketOptions.SO_KEEPALIVE, on);
  }

  @Override
  public boolean getKeepAlive() throws SocketException {
    return getOpt(StandardSocketOptions.SO_KEEPALIVE);
  }

  @Override
  public void setSendBufferSize(int size) throws SocketException {
    setOpt(StandardSocketOptions.SO_SNDBUF, size);
  }

  @Override
  public int getSendBufferSize() throws SocketException {
    return getOpt(StandardSocketOptions.SO_SNDBUF);
  }

  @Override
  public void setReceiveBufferSize(int size) throws SocketException {
    setOpt(StandardSocketOptions.SO_RCVBUF, size);
  }

  @Override
  public int getReceiveBufferSize() throws SocketException {
    return getOpt(StandardSocketOptions.SO_RCVBUF);
  }

  @Override
  public void setReuseAddress(boolean on) throws SocketException {
    setOpt(StandardSocketOptions.SO_REUSEADDR, on);
  }

  @Override
  public boolean getReuseAddress() throws SocketException {
    return getOpt(StandardSocketOptions.SO_REUSEADDR);
  }

  @Override
  public void setSoTimeout(int timeout) throws SocketException {
    if (timeout < 0) {
      throw new IllegalArgumentException("timeout can't be negative");
    }
    this.timeout = timeout;
  }

  @Override
  public int getSoTimeout() throws SocketException {
    return timeout;
  }

  @Override
  public void shutdownInput() throws IOException {
    try {
      sc.shutdownInput();
    } catch (java.nio.channels.ClosedChannelException e) {
      throw new SocketException("Socket is closed");
    } catch (java.nio.channels.NotYetConnectedException e) {
      throw new SocketException("Socket is not connected");
    }
  }

  @Override
  public void shutdownOutput() throws IOException {
    try {
      sc.shutdownOutput();
    } catch (java.nio.channels.ClosedChannelException e) {
      throw new SocketException("Socket is closed");
    } catch (java.nio.channels.NotYetConnectedException e) {
      throw new SocketException("Socket is not connected");
    }
  }

  @Override
  public String toString() {
    if (sc.isConnected()) {
      return "Socket[addr=" + getInetAddress() + ",port=" + getPort() + ",localport=" + getLocalPort() + "]";
    }
    return "Socket[unconnected]";
  }

  @Override
  public boolean isConnected() {
    return sc.isConnected();
  }

  @Override
  public boolean isBound() {
    return localAddress() != null;
  }

  @Override
  public boolean isClosed() {
    return !sc.isOpen();
  }

  @Override
  public boolean isInputShutdown() {
    return false;
  }

  @Override
  public boolean isOutputShutdown() {
    return false;
  }

  @Override
  public void close() throws IOException {
    sc.close();
  }
}
