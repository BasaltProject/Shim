/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package java.net;

import java.io.ObjectStreamException;
import java.util.Objects;

public class InetSocketAddress extends SocketAddress {
  private static final long serialVersionUID = 5076001401234631237L;

  private final String hostname;
  private final InetAddress addr;
  private final int port;

  private static int checkPort(int port) {
    if (port < 0 || port > 0xFFFF) {
      throw new IllegalArgumentException("port out of range:" + port);
    }
    return port;
  }

  private static String checkHost(String hostname) {
    if (hostname == null) {
      throw new IllegalArgumentException("hostname can't be null");
    }
    return hostname;
  }

  public InetSocketAddress(int port) {
    this(InetAddress.anyLocalAddress(), port);
  }

  public InetSocketAddress(InetAddress addr, int port) {
    this.hostname = null;
    this.addr = addr == null ? InetAddress.anyLocalAddress() : addr;
    this.port = checkPort(port);
  }

  public InetSocketAddress(String hostname, int port) {
    checkHost(hostname);
    InetAddress resolved = null;
    String host = null;
    try {
      resolved = InetAddress.getByName(hostname);
    } catch (UnknownHostException e) {
      host = hostname;
    }
    this.hostname = host;
    this.addr = resolved;
    this.port = checkPort(port);
  }

  private InetSocketAddress(int port, String hostname) {
    this.hostname = hostname;
    this.addr = null;
    this.port = port;
  }

  public static InetSocketAddress createUnresolved(String host, int port) {
    return new InetSocketAddress(checkPort(port), checkHost(host));
  }

  public final int getPort() {
    return port;
  }

  public final InetAddress getAddress() {
    return addr;
  }

  public final String getHostName() {
    if (hostname != null) {
      return hostname;
    }
    if (addr != null) {
      return addr.getHostName();
    }
    return null;
  }

  public final String getHostString() {
    if (hostname != null) {
      return hostname;
    }
    if (addr != null) {
      if (addr.holderHostName != null) {
        return addr.holderHostName;
      }
      return addr.getHostAddress();
    }
    return null;
  }

  public final boolean isUnresolved() {
    return addr == null;
  }

  @Override
  public String toString() {
    String formatted;
    if (isUnresolved()) {
      formatted = hostname + "/<unresolved>";
    } else {
      formatted = addr.toString();
      if (addr instanceof Inet6Address) {
        int i = formatted.lastIndexOf("/");
        formatted = formatted.substring(0, i + 1) + "[" + formatted.substring(i + 1) + "]";
      }
    }
    return formatted + ":" + port;
  }

  @Override
  public final boolean equals(Object obj) {
    if (!(obj instanceof InetSocketAddress)) {
      return false;
    }
    InetSocketAddress that = (InetSocketAddress) obj;
    boolean sameIP;
    if (addr != null) {
      sameIP = addr.equals(that.addr);
    } else if (hostname != null) {
      sameIP = (that.addr == null) && hostname.equalsIgnoreCase(that.hostname);
    } else {
      sameIP = (that.addr == null) && (that.hostname == null);
    }
    return sameIP && (port == that.port);
  }

  @Override
  public final int hashCode() {
    if (addr != null) {
      return addr.hashCode() + port;
    }
    if (hostname != null) {
      return hostname.toLowerCase().hashCode() + port;
    }
    return port;
  }
}
