/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package java.net;

public class Proxy {
  public enum Type {
    DIRECT, HTTP, SOCKS
  }

  public static final Proxy NO_PROXY = new Proxy();

  private final Type type;
  private final SocketAddress sa;

  private Proxy() {
    this.type = Type.DIRECT;
    this.sa = null;
  }

  public Proxy(Type type, SocketAddress sa) {
    if (type == Type.DIRECT || !(sa instanceof InetSocketAddress)) {
      throw new IllegalArgumentException("type " + type + " is not compatible with address " + sa);
    }
    this.type = type;
    this.sa = sa;
  }

  public Type type() {
    return type;
  }

  public SocketAddress address() {
    return sa;
  }

  @Override
  public String toString() {
    if (type() == Type.DIRECT) {
      return "DIRECT";
    }
    return type() + " @ " + address();
  }

  @Override
  public final boolean equals(Object obj) {
    if (!(obj instanceof Proxy)) return false;
    Proxy p = (Proxy) obj;
    if (p.type() != type()) return false;
    if (address() == null) return p.address() == null;
    return address().equals(p.address());
  }

  @Override
  public final int hashCode() {
    if (address() == null) return type().hashCode();
    return type().hashCode() + address().hashCode();
  }
}
