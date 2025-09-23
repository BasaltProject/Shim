/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package sun.nio.ch;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.nio.ByteBuffer;
import java.nio.channels.DatagramChannel;
import java.nio.channels.IllegalBlockingModeException;

class DatagramAdaptor extends DatagramSocket {
  private final DatagramChannelImpl dc;
  private volatile int timeout;

  DatagramAdaptor(DatagramChannelImpl dc) throws IllegalStateException {
    super(new DummyImpl());
    this.dc = dc;
  }

  private static final class DummyImpl extends java.net.DatagramSocketImpl {
    protected void create() { }
    protected void bind(int lport, InetAddress laddr) { }
    protected void send(DatagramPacket p) { }
    protected int peek(InetAddress i) { return 0; }
    protected int peekData(DatagramPacket p) { return 0; }
    protected void receive(DatagramPacket p) { }
    protected void join(InetAddress inetaddr) { }
    protected void leave(InetAddress inetaddr) { }
    protected void joinGroup(SocketAddress mcastaddr, java.net.NetworkInterface netIf) { }
    protected void leaveGroup(SocketAddress mcastaddr, java.net.NetworkInterface netIf) { }
    protected void close() { }
    public void setOption(int optID, Object value) { }
    public Object getOption(int optID) {
      return null;
    }
  }

  @Override
  public DatagramChannel getChannel() {
    return dc;
  }

  @Override
  public void bind(SocketAddress local) throws SocketException {
    try {
      dc.bind(local);
    } catch (java.nio.channels.AlreadyBoundException e) {
      throw new SocketException("Already bound");
    } catch (IOException e) {
      throw new SocketException(e.getMessage());
    }
  }

  @Override
  public void connect(SocketAddress remote) throws SocketException {
    try {
      dc.connect(remote);
    } catch (IOException e) {
      throw new SocketException(e.getMessage());
    }
  }

  @Override
  public void connect(InetAddress address, int port) {
    try {
      dc.connect(new InetSocketAddress(address, port));
    } catch (IOException e) {
      throw new IllegalStateException(e);
    }
  }

  @Override
  public void disconnect() {
    try {
      dc.disconnect();
    } catch (IOException e) {
      throw new IllegalStateException(e);
    }
  }

  @Override
  public boolean isBound() {
    try {
      return dc.getLocalAddress() != null;
    } catch (IOException e) {
      return false;
    }
  }

  @Override
  public boolean isConnected() {
    return dc.isConnected();
  }

  @Override
  public InetAddress getInetAddress() {
    try {
      InetSocketAddress remote = (InetSocketAddress) dc.getRemoteAddress();
      return remote == null ? null : remote.getAddress();
    } catch (IOException e) {
      return null;
    }
  }

  @Override
  public int getPort() {
    try {
      InetSocketAddress remote = (InetSocketAddress) dc.getRemoteAddress();
      return remote == null ? -1 : remote.getPort();
    } catch (IOException e) {
      return -1;
    }
  }

  @Override
  public SocketAddress getRemoteSocketAddress() {
    try {
      return dc.getRemoteAddress();
    } catch (IOException e) {
      return null;
    }
  }

  @Override
  public SocketAddress getLocalSocketAddress() {
    try {
      return dc.getLocalAddress();
    } catch (IOException e) {
      return null;
    }
  }

  @Override
  public InetAddress getLocalAddress() {
    SocketAddress a = getLocalSocketAddress();
    return a == null ? null : ((InetSocketAddress) a).getAddress();
  }

  @Override
  public int getLocalPort() {
    SocketAddress a = getLocalSocketAddress();
    return a == null ? -1 : ((InetSocketAddress) a).getPort();
  }

  @Override
  public void send(DatagramPacket p) throws IOException {
    synchronized (dc.blockingLock()) {
      if (!dc.isBlocking()) {
        throw new IllegalBlockingModeException();
      }
      dc.send(ByteBuffer.wrap(p.getData(), p.getOffset(), p.getLength()), p.getSocketAddress());
    }
  }

  @Override
  public void receive(DatagramPacket p) throws IOException {
    synchronized (dc.blockingLock()) {
      if (!dc.isBlocking()) {
        throw new IllegalBlockingModeException();
      }
      ByteBuffer bb = ByteBuffer.wrap(p.getData(), p.getOffset(), p.getData().length - p.getOffset());
      SocketAddress sender = dc.receive(bb);
      p.setSocketAddress(sender);
      p.setLength(bb.position() - p.getOffset());
    }
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
  public void close() {
    try {
      dc.close();
    } catch (IOException ignore) { }
  }

  @Override
  public boolean isClosed() {
    return !dc.isOpen();
  }
}
