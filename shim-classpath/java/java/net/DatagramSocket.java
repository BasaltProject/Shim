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
import java.nio.channels.DatagramChannel;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import shim.NetHandle;
import shim.NetNative;
import shim.SocketOptionSupport;

public class DatagramSocket implements Closeable {
  private final NetHandle net = new NetHandle(true);
  private boolean bound;
  private boolean connected;
  private InetAddress connectedAddress;
  private int connectedPort = -1;
  private InetAddress localAddress;
  private int localPort = -1;
  private int timeout;

  public DatagramSocket() throws SocketException {
    this(new InetSocketAddress(0));
  }

  protected DatagramSocket(DatagramSocketImpl impl) {
    if (impl == null) {
      throw new NullPointerException();
    }
  }

  public DatagramSocket(SocketAddress bindaddr) throws SocketException {
    if (bindaddr != null) {
      try {
        bind(bindaddr);
      } finally {
        if (!isBound()) {
          close();
        }
      }
    }
  }

  public DatagramSocket(int port) throws SocketException {
    this(port, null);
  }

  public DatagramSocket(int port, InetAddress laddr) throws SocketException {
    this(new InetSocketAddress(laddr, port));
  }

  private static InetAddress datagramAddress(byte[] raw) {
    if (raw != null && raw.length == 16) {
      boolean zero = true;
      for (byte b : raw) {
        zero &= b == 0;
      }
      if (zero) {
        try {
          return InetAddress.getByAddress(raw);
        } catch (UnknownHostException e) {
          return null;
        }
      }
    }
    return Socket.toAddress(raw);
  }

  public synchronized void bind(SocketAddress addr) throws SocketException {
    if (isClosed()) {
      throw new SocketException("Socket is closed");
    }
    if (isBound()) {
      throw new SocketException("Already bound");
    }
    if (addr == null) {
      addr = new InetSocketAddress(0);
    }
    if (!(addr instanceof InetSocketAddress)) {
      throw new IllegalArgumentException("Unsupported address type!");
    }
    InetSocketAddress epoint = (InetSocketAddress) addr;
    if (epoint.isUnresolved()) {
      throw new SocketException("Unresolved address");
    }
    try {
      long h = net.begin();
      try {
        NetNative.bind(h, epoint.getAddress().getAddress(), epoint.getPort());
        int[] port = new int[1];
        byte[] raw = NetNative.sockName(h, false, port);
        localAddress = datagramAddress(raw);
        localPort = port[0];
      } finally {
        net.end();
      }
    } catch (SocketException e) {
      throw e;
    } catch (IOException e) {
      throw new SocketException(e.getMessage());
    }
    bound = true;
  }

  public void connect(InetAddress address, int port) {
    if (address == null) {
      throw new IllegalArgumentException("Address can't be null");
    }
    if (port < 0 || port > 0xFFFF) {
      throw new IllegalArgumentException("connect: " + port);
    }
    try {
      connectInternal(address, port);
    } catch (SocketException se) {
      throw new UncheckedIOException("connect failed", se);
    }
  }

  public void connect(SocketAddress addr) throws SocketException {
    if (addr == null) {
      throw new IllegalArgumentException("Address can't be null");
    }
    if (!(addr instanceof InetSocketAddress)) {
      throw new IllegalArgumentException("Unsupported address type");
    }
    InetSocketAddress epoint = (InetSocketAddress) addr;
    if (epoint.isUnresolved()) {
      throw new SocketException("Unresolved address");
    }
    connectInternal(epoint.getAddress(), epoint.getPort());
  }

  private static final class UncheckedIOException extends RuntimeException {
    UncheckedIOException(String m, Throwable c) {
      super(m, c);
    }
  }

  private synchronized void connectInternal(InetAddress address, int port) throws SocketException {
    if (isClosed()) {
      throw new SocketException("Socket is closed");
    }
    try {
      if (!isBound()) {
        bind(new InetSocketAddress(0));
      }
      long h = net.begin();
      try {
        NetNative.connect(h, address.getAddress(), port, 0);
      } finally {
        net.end();
      }
    } catch (SocketException e) {
      throw e;
    } catch (IOException e) {
      throw new SocketException(e.getMessage());
    }
    connectedAddress = address;
    connectedPort = port;
    connected = true;
  }

  public void disconnect() {
    synchronized (this) {
      if (isClosed() || !connected) {
        return;
      }
      connected = false;
      connectedAddress = null;
      connectedPort = -1;
    }
  }

  public boolean isBound() {
    return bound;
  }

  public boolean isConnected() {
    return connected;
  }

  public InetAddress getInetAddress() {
    return connectedAddress;
  }

  public int getPort() {
    return connectedPort;
  }

  public SocketAddress getRemoteSocketAddress() {
    if (!isConnected()) {
      return null;
    }
    return new InetSocketAddress(getInetAddress(), getPort());
  }

  public SocketAddress getLocalSocketAddress() {
    if (isClosed() || !isBound()) {
      return null;
    }
    return new InetSocketAddress(getLocalAddress(), getLocalPort());
  }

  public void send(DatagramPacket p) throws IOException {
    synchronized (p) {
      if (isClosed()) {
        throw new SocketException("Socket closed");
      }
      InetAddress packetAddress = p.getAddress();
      int packetPort = p.getPort();
      if (connected) {
        if (packetAddress == null) {
          p.setAddress(connectedAddress);
          p.setPort(connectedPort);
        } else if (!connectedAddress.equals(packetAddress) || connectedPort != packetPort) {
          throw new IllegalArgumentException("Connected and packet address differ");
        }
      } else if (packetAddress == null) {
        throw new IllegalArgumentException("Address not set");
      } else if (packetPort < 0) {
        throw new IllegalArgumentException("port out of range");
      }
      if (!isBound()) {
        bind(new InetSocketAddress(0));
      }
      long h = net.begin();
      try {
        if (connected) {
          NetNative.sendTo(h, p.getData(), p.getOffset(), p.getLength(), null, 0);
        } else {
          NetNative.sendTo(h, p.getData(), p.getOffset(), p.getLength(), p.getAddress().getAddress(), p.getPort());
        }
      } finally {
        net.end();
      }
    }
  }

  public synchronized void receive(DatagramPacket p) throws IOException {
    if (isClosed()) {
      throw new SocketException("Socket is closed");
    }
    if (!isBound()) {
      bind(new InetSocketAddress(0));
    }
    long h = net.begin();
    try {
      byte[][] src = new byte[1][];
      int[] srcPort = new int[1];
      int n = NetNative.recvFrom(h, p.getData(), p.getOffset(), p.bufLength, timeout, src, srcPort, false);
      if (isClosed()) {
        throw new SocketException("Socket closed");
      }
      p.length = n;
      p.address = Socket.toAddress(src[0]);
      p.port = srcPort[0];
    } finally {
      net.end();
    }
  }

  public InetAddress getLocalAddress() {
    if (isClosed()) {
      return null;
    }
    return localAddress != null ? localAddress : InetAddress.anyLocalAddress();
  }

  public int getLocalPort() {
    if (isClosed()) {
      return -1;
    }
    return isBound() ? localPort : 0;
  }

  public synchronized void setSoTimeout(int timeout) throws SocketException {
    if (timeout < 0) {
      throw new IllegalArgumentException("timeout < 0");
    }
    if (isClosed()) {
      throw new SocketException("Socket is closed");
    }
    this.timeout = timeout;
  }

  public synchronized int getSoTimeout() throws SocketException {
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

  public synchronized void setSendBufferSize(int size) throws SocketException {
    if (size <= 0) {
      throw new IllegalArgumentException("negative send size");
    }
    setIntOption(NetNative.OPT_SNDBUF, size);
  }

  public synchronized int getSendBufferSize() throws SocketException {
    return getIntOption(NetNative.OPT_SNDBUF);
  }

  public synchronized void setReceiveBufferSize(int size) throws SocketException {
    if (size <= 0) {
      throw new IllegalArgumentException("invalid receive size");
    }
    setIntOption(NetNative.OPT_RCVBUF, size);
  }

  public synchronized int getReceiveBufferSize() throws SocketException {
    return getIntOption(NetNative.OPT_RCVBUF);
  }

  public synchronized void setReuseAddress(boolean on) throws SocketException {
    setIntOption(NetNative.OPT_REUSEADDR, on ? 1 : 0);
  }

  public synchronized boolean getReuseAddress() throws SocketException {
    return getIntOption(NetNative.OPT_REUSEADDR) != 0;
  }

  public synchronized void setBroadcast(boolean on) throws SocketException {
    setIntOption(NetNative.OPT_BROADCAST, on ? 1 : 0);
  }

  public synchronized boolean getBroadcast() throws SocketException {
    return getIntOption(NetNative.OPT_BROADCAST) != 0;
  }

  public synchronized void setTrafficClass(int tc) throws SocketException {
    if (tc < 0 || tc > 255) {
      throw new IllegalArgumentException("tc is not in range 0 -- 255");
    }
    setIntOption(NetNative.OPT_TOS, tc);
  }

  public synchronized int getTrafficClass() throws SocketException {
    return getIntOption(NetNative.OPT_TOS);
  }

  @Override
  public void close() {
    net.close();
  }

  public boolean isClosed() {
    return net.isClosed();
  }

  public DatagramChannel getChannel() {
    return null;
  }

  public static synchronized void setDatagramSocketImplFactory(DatagramSocketImplFactory fac) throws IOException {
    throw new SocketException("factory already defined");
  }

  public <T> DatagramSocket setOption(SocketOption<T> name, T value) throws IOException {
    if (name == null) {
      throw new NullPointerException();
    }
    if (isClosed()) {
      throw new SocketException("Socket is closed");
    }
    if (name == StandardSocketOptions.SO_SNDBUF) {
      setSendBufferSize((Integer) value);
    } else if (name == StandardSocketOptions.SO_RCVBUF) {
      setReceiveBufferSize((Integer) value);
    } else if (name == StandardSocketOptions.SO_REUSEADDR) {
      setReuseAddress((Boolean) value);
    } else if (name == StandardSocketOptions.SO_REUSEPORT) {
      setIntOption(NetNative.OPT_REUSEPORT, ((Boolean) value) ? 1 : 0);
    } else if (name == StandardSocketOptions.SO_BROADCAST) {
      setBroadcast((Boolean) value);
    } else if (name == StandardSocketOptions.IP_TOS) {
      setTrafficClass((Integer) value);
    } else if (name == StandardSocketOptions.IP_MULTICAST_TTL) {
      setIntOption(NetNative.OPT_MULTICAST_TTL, (Integer) value);
    } else if (name == StandardSocketOptions.IP_MULTICAST_LOOP) {
      setIntOption(NetNative.OPT_MULTICAST_LOOP, ((Boolean) value) ? 1 : 0);
    } else if (name == StandardSocketOptions.IP_MULTICAST_IF) {
      NetworkInterface nif = (NetworkInterface) value;
      if (nif == null) {
        throw new IllegalArgumentException("Invalid null network interface");
      }
      setIntOption(NetNative.OPT_MULTICAST_IF, nif.getIndex());
    } else if (SocketOptionSupport.supported(SocketOptionSupport.kUdp).contains(name)
        && SocketOptionSupport.isExtended(name)) {
      SocketOptionSupport.setExtended(net, name, value);
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
    if (name == StandardSocketOptions.SO_SNDBUF) {
      return (T) Integer.valueOf(getSendBufferSize());
    } else if (name == StandardSocketOptions.SO_RCVBUF) {
      return (T) Integer.valueOf(getReceiveBufferSize());
    } else if (name == StandardSocketOptions.SO_REUSEADDR) {
      return (T) Boolean.valueOf(getReuseAddress());
    } else if (name == StandardSocketOptions.SO_REUSEPORT) {
      return (T) Boolean.valueOf(getIntOption(NetNative.OPT_REUSEPORT) != 0);
    } else if (name == StandardSocketOptions.SO_BROADCAST) {
      return (T) Boolean.valueOf(getBroadcast());
    } else if (name == StandardSocketOptions.IP_TOS) {
      return (T) Integer.valueOf(getTrafficClass());
    } else if (name == StandardSocketOptions.IP_MULTICAST_TTL) {
      return (T) Integer.valueOf(getIntOption(NetNative.OPT_MULTICAST_TTL));
    } else if (name == StandardSocketOptions.IP_MULTICAST_LOOP) {
      return (T) Boolean.valueOf(getIntOption(NetNative.OPT_MULTICAST_LOOP) != 0);
    } else if (name == StandardSocketOptions.IP_MULTICAST_IF) {
      return (T) NetworkInterface.getByIndex(getIntOption(NetNative.OPT_MULTICAST_IF));
    }
    if (SocketOptionSupport.supported(SocketOptionSupport.kUdp).contains(name)
        && SocketOptionSupport.isExtended(name)) {
      return SocketOptionSupport.getExtended(net, name);
    }
    throw new UnsupportedOperationException("'" + name + "' not supported");
  }

  public Set<SocketOption<?>> supportedOptions() {
    return SocketOptionSupport.supported(SocketOptionSupport.kUdp);
  }

  public void joinGroup(SocketAddress mcastaddr, NetworkInterface netIf) throws IOException {
    membership(mcastaddr, netIf, true);
  }

  public void leaveGroup(SocketAddress mcastaddr, NetworkInterface netIf) throws IOException {
    membership(mcastaddr, netIf, false);
  }

  private void membership(SocketAddress mcastaddr, NetworkInterface netIf, boolean join) throws IOException {
    if (isClosed()) {
      throw new SocketException("Socket is closed");
    }
    if (mcastaddr == null || !(mcastaddr instanceof InetSocketAddress)) {
      throw new IllegalArgumentException("Unsupported address type");
    }
    InetAddress group = ((InetSocketAddress) mcastaddr).getAddress();
    if (!group.isMulticastAddress()) {
      throw new SocketException("Not a multicast address");
    }
    long h = net.begin();
    try {
      NetNative.membership(h, group.getAddress(), netIf == null ? 0 : netIf.getIndex(), join);
    } finally {
      net.end();
    }
  }
}
