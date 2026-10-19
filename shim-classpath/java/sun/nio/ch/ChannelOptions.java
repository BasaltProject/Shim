/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package sun.nio.ch;

import java.io.IOException;
import java.net.NetworkInterface;
import java.net.SocketOption;
import java.net.StandardSocketOptions;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import shim.NetHandle;
import shim.NetNative;

final class ChannelOptions {
  static final int kStream = 0;
  static final int kServer = 1;
  static final int kDatagram = 2;
  static final int kUnixStream = 3;
  static final int kUnixServer = 4;

  private ChannelOptions() { }

  static Set<SocketOption<?>> supported(int kind) {
    Set<SocketOption<?>> options = new HashSet<SocketOption<?>>();
    if (kind == kUnixStream || kind == kUnixServer) {
      options.add(StandardSocketOptions.SO_RCVBUF);
      if (kind == kUnixStream) {
        options.add(StandardSocketOptions.SO_SNDBUF);
        options.add(StandardSocketOptions.SO_LINGER);
      }
      return Collections.unmodifiableSet(options);
    }
    options.add(StandardSocketOptions.SO_RCVBUF);
    options.add(StandardSocketOptions.SO_REUSEADDR);
    options.add(StandardSocketOptions.SO_REUSEPORT);
    if (kind != kServer) {
      options.add(StandardSocketOptions.SO_SNDBUF);
      options.add(StandardSocketOptions.IP_TOS);
    }
    if (kind == kStream) {
      options.add(StandardSocketOptions.SO_KEEPALIVE);
      options.add(StandardSocketOptions.SO_LINGER);
      options.add(StandardSocketOptions.TCP_NODELAY);
    }
    if (kind == kDatagram) {
      options.add(StandardSocketOptions.SO_BROADCAST);
      options.add(StandardSocketOptions.IP_MULTICAST_TTL);
      options.add(StandardSocketOptions.IP_MULTICAST_LOOP);
      options.add(StandardSocketOptions.IP_MULTICAST_IF);
    }
    return Collections.unmodifiableSet(options);
  }

  private static int code(SocketOption<?> name) {
    if (name == StandardSocketOptions.SO_RCVBUF) return NetNative.OPT_RCVBUF;
    if (name == StandardSocketOptions.SO_SNDBUF) return NetNative.OPT_SNDBUF;
    if (name == StandardSocketOptions.SO_REUSEADDR) return NetNative.OPT_REUSEADDR;
    if (name == StandardSocketOptions.SO_REUSEPORT) return NetNative.OPT_REUSEPORT;
    if (name == StandardSocketOptions.SO_KEEPALIVE) return NetNative.OPT_KEEPALIVE;
    if (name == StandardSocketOptions.SO_LINGER) return NetNative.OPT_LINGER;
    if (name == StandardSocketOptions.TCP_NODELAY) return NetNative.OPT_TCP_NODELAY;
    if (name == StandardSocketOptions.SO_BROADCAST) return NetNative.OPT_BROADCAST;
    if (name == StandardSocketOptions.IP_TOS) return NetNative.OPT_TOS;
    if (name == StandardSocketOptions.IP_MULTICAST_TTL) return NetNative.OPT_MULTICAST_TTL;
    if (name == StandardSocketOptions.IP_MULTICAST_LOOP) return NetNative.OPT_MULTICAST_LOOP;
    if (name == StandardSocketOptions.IP_MULTICAST_IF) return NetNative.OPT_MULTICAST_IF;
    return -1;
  }

  static <T> void set(NetHandle net, SocketOption<T> name, T value, int kind) throws IOException {
    if (!supported(kind).contains(name)) {
      throw new UnsupportedOperationException("'" + name + "' not supported");
    }
    if (value == null || !name.type().isInstance(value)) {
      throw new IllegalArgumentException("Invalid value '" + value + "'");
    }
    int v;
    if (value instanceof Boolean) {
      v = ((Boolean) value) ? 1 : 0;
    } else if (value instanceof NetworkInterface) {
      v = ((NetworkInterface) value).getIndex();
    } else {
      v = (Integer) value;
    }
    if (name == StandardSocketOptions.SO_RCVBUF || name == StandardSocketOptions.SO_SNDBUF) {
      if (v < 0) {
        throw new IllegalArgumentException("Invalid send/receive buffer size");
      }
    } else if (name == StandardSocketOptions.IP_TOS && (v < 0 || v > 255)) {
      throw new IllegalArgumentException("Invalid IP_TOS value");
    } else if (name == StandardSocketOptions.IP_MULTICAST_TTL && (v < 0 || v > 255)) {
      throw new IllegalArgumentException("Invalid TTL/hop value");
    } else if (name == StandardSocketOptions.SO_LINGER && v > 65535) {
      v = 65535;
    }
    long h = net.begin();
    try {
      NetNative.setOption(h, code(name), v);
    } finally {
      net.end();
    }
  }

  @SuppressWarnings("unchecked")
  static <T> T get(NetHandle net, SocketOption<T> name, int kind) throws IOException {
    if (!supported(kind).contains(name)) {
      throw new UnsupportedOperationException("'" + name + "' not supported");
    }
    long h = net.begin();
    try {
      int v = NetNative.getOption(h, code(name));
      Class<T> type = name.type();
      if (type == Boolean.class) {
        return (T) Boolean.valueOf(v != 0);
      }
      if (type == NetworkInterface.class) {
        return (T) NetworkInterface.getByIndex(v);
      }
      return (T) Integer.valueOf(v);
    } finally {
      net.end();
    }
  }
}
