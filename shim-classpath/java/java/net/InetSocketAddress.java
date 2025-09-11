/*
 * Based on Avian (https://github.com/ReadyTalk/avian).
 * Original work Copyright (c) 2008-2015, Avian Contributors.
 * Original work licensed under ISC; see LICENSE.txt:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE.txt
 *
 * Modifications Copyright (c) 2026 BasaltMC.
 * Modifications licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: ISC AND MIT
 */

/* Copyright (c) 2008-2015, Avian Contributors

   Permission to use, copy, modify, and/or distribute this software
   for any purpose with or without fee is hereby granted, provided
   that the above copyright notice and this permission notice appear
   in all copies.

   There is NO WARRANTY for this software.  See license.txt for
   details. */

package java.net;

public class InetSocketAddress extends SocketAddress {
  private final String host;
  private final InetAddress address;
  private final int port;

  public InetSocketAddress(String host, int port) {
    InetAddress address;
    try {
      address = InetAddress.getByName(host);
      host = address.getHostName();
    } catch (UnknownHostException e) {
      address = null;
    }
    this.host = host;
    this.address = address;
    this.port = port;
  }
  
  public InetSocketAddress(InetAddress address, int port) {
    this.host = address.getHostName();
    this.address = address;
    this.port = port;
  }

  public static InetSocketAddress createUnresolved(String host, int port) {
    return new InetSocketAddress(host, port, false);
  }

  private InetSocketAddress(String host, int port, boolean resolve) {
    if (port < 0 || port > 0xFFFF) throw new IllegalArgumentException("port out of range:" + port);
    if (host == null) throw new IllegalArgumentException("hostname can't be null");
    this.host = host;
    this.address = null;
    this.port = port;
  }

  public InetAddress getAddress() {
    return address;
  }
  
  public String getHostName() {
    return host;
  }

  public final String getHostString() {
    return host;
  }

  public final boolean isUnresolved() {
    return address == null;
  }

  public int getPort() {
    return port;
  }

  public boolean equals(Object o) {
    if (o instanceof InetSocketAddress) {
      InetSocketAddress a = (InetSocketAddress) o;
      return a.address.equals(address) && a.port == port;
    } else {
      return false;
    }
  }

  public int hashCode() {
    return port ^ address.hashCode();
  }

  public String toString() {
    return getHostName() + ":" + port;
  }
}
