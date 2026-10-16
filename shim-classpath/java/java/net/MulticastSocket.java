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
}
