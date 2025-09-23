/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package java.net;

import java.io.Closeable;
import java.io.IOException;
import java.nio.channels.ServerSocketChannel;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import shim.NetHandle;
import shim.NetNative;

public class ServerSocket implements Closeable {
  private final NetHandle net = new NetHandle(false);
  private boolean bound;
  private int timeout;
  private InetAddress localAddress;
  private int localPort = -1;
  private boolean reuseSet;

  public ServerSocket() throws IOException { }

  public ServerSocket(int port) throws IOException {
    this(port, 50, null);
  }

  public ServerSocket(int port, int backlog) throws IOException {
    this(port, backlog, null);
  }

  public ServerSocket(int port, int backlog, InetAddress bindAddr) throws IOException {
    if (port < 0 || port > 0xFFFF) {
      throw new IllegalArgumentException("Port value out of range: " + port);
    }
    if (backlog < 1) {
      backlog = 50;
    }
    try {
      bind(new InetSocketAddress(bindAddr, port), backlog);
    } catch (IOException e) {
      close();
      throw e;
    }
  }

  public void bind(SocketAddress endpoint) throws IOException {
    bind(endpoint, 50);
  }

  public void bind(SocketAddress endpoint, int backlog) throws IOException {
    if (isClosed()) {
      throw new SocketException("Socket is closed");
    }
    if (isBound()) {
      throw new SocketException("Already bound");
    }
    if (endpoint == null) {
      endpoint = new InetSocketAddress(0);
    }
    if (!(endpoint instanceof InetSocketAddress)) {
      throw new IllegalArgumentException("Unsupported address type");
    }
    InetSocketAddress epoint = (InetSocketAddress) endpoint;
    if (epoint.isUnresolved()) {
      throw new SocketException("Unresolved address");
    }
    if (backlog < 1) {
      backlog = 50;
    }
    long h = net.begin();
    try {
      if (!reuseSet && !System.getProperty("os.name", "").startsWith("Windows")) {
        NetNative.setOption(h, NetNative.OPT_REUSEADDR, 1);
      }
      NetNative.bind(h, epoint.getAddress().getAddress(), epoint.getPort());
      NetNative.listen(h, backlog);
      int[] port = new int[1];
      byte[] raw = NetNative.sockName(h, false, port);
      localAddress = epoint.getAddress().isAnyLocalAddress() ? Socket.toAddress(raw) : epoint.getAddress();
      localPort = port[0];
    } finally {
      net.end();
    }
    bound = true;
  }

  public InetAddress getInetAddress() {
    return isBound() ? localAddress : null;
  }

  public int getLocalPort() {
    return isBound() ? localPort : -1;
  }

  public SocketAddress getLocalSocketAddress() {
    if (!isBound()) {
      return null;
    }
    return new InetSocketAddress(getInetAddress(), getLocalPort());
  }

  public Socket accept() throws IOException {
    if (isClosed()) {
      throw new SocketException("Socket is closed");
    }
    if (!isBound()) {
      throw new SocketException("Socket is not bound yet");
    }
    long h = net.begin();
    try {
      byte[][] peer = new byte[1][];
      int[] peerPort = new int[1];
      long client = NetNative.accept(h, timeout, peer, peerPort);
      if (isClosed()) {
        NetNative.close(client);
        throw new SocketException("Socket closed");
      }
      return new Socket(client, peer[0], peerPort[0]);
    } catch (SocketException e) {
      if (isClosed()) {
        throw new SocketException("Socket closed");
      }
      throw e;
    } finally {
      net.end();
    }
  }

  @Override
  public void close() throws IOException {
    net.close();
  }

  public ServerSocketChannel getChannel() {
    return null;
  }

  public boolean isBound() {
    return bound;
  }

  public boolean isClosed() {
    return net.isClosed();
  }

  public synchronized void setSoTimeout(int timeout) throws SocketException {
    if (isClosed()) {
      throw new SocketException("Socket is closed");
    }
    if (timeout < 0) {
      throw new IllegalArgumentException("timeout < 0");
    }
    this.timeout = timeout;
  }

  public synchronized int getSoTimeout() throws IOException {
    if (isClosed()) {
      throw new SocketException("Socket is closed");
    }
    return timeout;
  }

  private void setIntOption(int option, int value) throws SocketException {
    if (isClosed()) {
      throw new SocketException("Socket is closed");
    }
    try {
      long h = net.begin();
      try {
        NetNative.setOption(h, option, value);
      } finally {
        net.end();
      }
    } catch (SocketException e) {
      throw e;
    } catch (IOException e) {
      throw new SocketException(e.getMessage());
    }
  }

  private int getIntOption(int option) throws SocketException {
    if (isClosed()) {
      throw new SocketException("Socket is closed");
    }
    try {
      long h = net.begin();
      try {
        return NetNative.getOption(h, option);
      } finally {
        net.end();
      }
    } catch (SocketException e) {
      throw e;
    } catch (IOException e) {
      throw new SocketException(e.getMessage());
    }
  }

  public void setReuseAddress(boolean on) throws SocketException {
    reuseSet = true;
    setIntOption(NetNative.OPT_REUSEADDR, on ? 1 : 0);
  }

  public boolean getReuseAddress() throws SocketException {
    return getIntOption(NetNative.OPT_REUSEADDR) != 0;
  }

  @Override
  public String toString() {
    if (!isBound()) {
      return "ServerSocket[unbound]";
    }
    return "ServerSocket[addr=" + localAddress + ",localport=" + localPort + "]";
  }

  public static synchronized void setSocketFactory(SocketImplFactory fac) throws IOException {
    throw new SocketException("factory already defined");
  }

  public void setReceiveBufferSize(int size) throws SocketException {
    if (size <= 0) {
      throw new IllegalArgumentException("negative receive size");
    }
    setIntOption(NetNative.OPT_RCVBUF, size);
  }

  public int getReceiveBufferSize() throws SocketException {
    return getIntOption(NetNative.OPT_RCVBUF);
  }

  public void setPerformancePreferences(int connectionTime, int latency, int bandwidth) { }

  public <T> ServerSocket setOption(SocketOption<T> name, T value) throws IOException {
    if (name == null) {
      throw new NullPointerException();
    }
    if (isClosed()) {
      throw new SocketException("Socket is closed");
    }
    if (name == StandardSocketOptions.SO_RCVBUF) {
      setReceiveBufferSize((Integer) value);
    } else if (name == StandardSocketOptions.SO_REUSEADDR) {
      setReuseAddress((Boolean) value);
    } else if (name == StandardSocketOptions.SO_REUSEPORT) {
      setIntOption(NetNative.OPT_REUSEPORT, ((Boolean) value) ? 1 : 0);
    } else {
      throw new UnsupportedOperationException("'" + name + "' not supported");
    }
    return this;
  }

  @SuppressWarnings("unchecked")
  public <T> T getOption(SocketOption<T> name) throws IOException {
    if (name == null) {
      throw new NullPointerException();
    }
    if (isClosed()) {
      throw new SocketException("Socket is closed");
    }
    if (name == StandardSocketOptions.SO_RCVBUF) {
      return (T) Integer.valueOf(getReceiveBufferSize());
    } else if (name == StandardSocketOptions.SO_REUSEADDR) {
      return (T) Boolean.valueOf(getReuseAddress());
    } else if (name == StandardSocketOptions.SO_REUSEPORT) {
      return (T) Boolean.valueOf(getIntOption(NetNative.OPT_REUSEPORT) != 0);
    }
    throw new UnsupportedOperationException("'" + name + "' not supported");
  }

  public Set<SocketOption<?>> supportedOptions() {
    Set<SocketOption<?>> options = new HashSet<SocketOption<?>>();
    options.add(StandardSocketOptions.SO_RCVBUF);
    options.add(StandardSocketOptions.SO_REUSEADDR);
    options.add(StandardSocketOptions.SO_REUSEPORT);
    return Collections.unmodifiableSet(options);
  }
}
