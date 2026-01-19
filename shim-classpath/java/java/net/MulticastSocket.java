/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package java.net;

import java.io.IOException;

public class MulticastSocket extends DatagramSocket {
  public MulticastSocket() throws IOException {
    this(new InetSocketAddress(0));
  }

  public MulticastSocket(int port) throws IOException {
    this(new InetSocketAddress(port));
  }

  public MulticastSocket(SocketAddress bindaddr) throws IOException {
    super((SocketAddress) null);
    setReuseAddress(true);
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

  @Deprecated
  public void setTTL(byte ttl) throws IOException {
    setTimeToLive(ttl & 0xff);
  }

  public void setTimeToLive(int ttl) throws IOException {
    if (ttl < 0 || ttl > 255) {
      throw new IllegalArgumentException("Invalid TTL/hop value");
    }
    setOption(StandardSocketOptions.IP_MULTICAST_TTL, ttl);
  }

  @Deprecated
  public byte getTTL() throws IOException {
    return (byte) getTimeToLive();
  }

  public int getTimeToLive() throws IOException {
    return getOption(StandardSocketOptions.IP_MULTICAST_TTL);
  }

  @Deprecated
  public void joinGroup(InetAddress mcastaddr) throws IOException {
    joinGroup(new InetSocketAddress(mcastaddr, 0), null);
  }

  @Deprecated
  public void leaveGroup(InetAddress mcastaddr) throws IOException {
    leaveGroup(new InetSocketAddress(mcastaddr, 0), null);
  }

  public void setLoopbackMode(boolean disable) throws SocketException {
    try {
      setOption(StandardSocketOptions.IP_MULTICAST_LOOP, !disable);
    } catch (SocketException e) {
      throw e;
    } catch (IOException e) {
      throw new SocketException(e.getMessage());
    }
  }

  public boolean getLoopbackMode() throws SocketException {
    try {
      return !getOption(StandardSocketOptions.IP_MULTICAST_LOOP);
    } catch (SocketException e) {
      throw e;
    } catch (IOException e) {
      throw new SocketException(e.getMessage());
    }
  }

  public void setInterface(InetAddress inf) throws SocketException {
    if (isClosed()) {
      throw new SocketException("Socket is closed");
    }
    if (inf == null) {
      throw new SocketException("bad argument for IP_MULTICAST_IF: address null or not bound to any interface");
    }
    NetworkInterface ni = NetworkInterface.getByInetAddress(inf);
    if (ni == null) {
      throw new SocketException("bad argument for IP_MULTICAST_IF: address not bound to any interface");
    }
    try {
      setOption(StandardSocketOptions.IP_MULTICAST_IF, ni);
    } catch (SocketException e) {
      throw e;
    } catch (IOException e) {
      throw new SocketException(e.getMessage());
    }
  }

  public InetAddress getInterface() throws SocketException {
    if (isClosed()) {
      throw new SocketException("Socket is closed");
    }
    try {
      NetworkInterface ni = getOption(StandardSocketOptions.IP_MULTICAST_IF);
      if (ni != null) {
        java.util.Enumeration<InetAddress> addrs = ni.getInetAddresses();
        if (addrs.hasMoreElements()) {
          return addrs.nextElement();
        }
      }
      return InetAddress.anyLocalAddress();
    } catch (SocketException e) {
      throw e;
    } catch (IOException e) {
      throw new SocketException(e.getMessage());
    }
  }

  public void setNetworkInterface(NetworkInterface netIf) throws SocketException {
    if (isClosed()) {
      throw new SocketException("Socket is closed");
    }
    try {
      setOption(StandardSocketOptions.IP_MULTICAST_IF, netIf);
    } catch (SocketException e) {
      throw e;
    } catch (IOException e) {
      throw new SocketException(e.getMessage());
    }
  }

  public NetworkInterface getNetworkInterface() throws SocketException {
    if (isClosed()) {
      throw new SocketException("Socket is closed");
    }
    try {
      NetworkInterface ni = getOption(StandardSocketOptions.IP_MULTICAST_IF);
      if (ni == null) {
        return NetworkInterface.anyInterface();
      }
      return ni;
    } catch (SocketException e) {
      throw e;
    } catch (IOException e) {
      throw new SocketException(e.getMessage());
    }
  }

  @Deprecated
  public void send(DatagramPacket p, byte ttl) throws IOException {
    if (isClosed()) {
      throw new SocketException("Socket is closed");
    }
    int previous = getTimeToLive();
    try {
      setTimeToLive(ttl & 0xff);
      send(p);
    } finally {
      setTimeToLive(previous);
    }
  }
}
