/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 * SPDX-License-Identifier: MIT
 */

package shim;

import java.io.IOException;
import java.net.SocketException;
import java.net.SocketOption;
import java.net.StandardSocketOptions;
import java.nio.file.attribute.GroupPrincipal;
import java.nio.file.attribute.UserPrincipal;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import jdk.net.ExtendedSocketOptions;
import jdk.net.UnixDomainPrincipal;

public final class SocketOptionSupport {
  public static final int kTcpClient = 0;
  public static final int kTcpServer = 1;
  public static final int kUdp = 2;
  public static final int kChannelClient = 3;
  public static final int kChannelServer = 4;
  public static final int kUnixClient = 5;
  public static final int kUnixServer = 6;
  public static final int kAsyncClient = 7;
  public static final int kAsyncServer = 8;

  public static final SocketOption<Boolean> OOBINLINE = new SocketOption<Boolean>() {
    @Override
    public String name() {
      return "SO_OOBINLINE";
    }

    @Override
    public Class<Boolean> type() {
      return Boolean.class;
    }

    @Override
    public String toString() {
      return "SO_OOBINLINE";
    }
  };

  private SocketOptionSupport() { }

  public static boolean isExtended(SocketOption<?> name) {
    return name == ExtendedSocketOptions.TCP_KEEPIDLE || name == ExtendedSocketOptions.TCP_KEEPINTERVAL
        || name == ExtendedSocketOptions.TCP_KEEPCOUNT || name == ExtendedSocketOptions.TCP_QUICKACK
        || name == ExtendedSocketOptions.SO_INCOMING_NAPI_ID || name == ExtendedSocketOptions.IP_DONTFRAGMENT
        || name == ExtendedSocketOptions.SO_PEERCRED;
  }

  private static void addKeepAlive(Set<SocketOption<?>> options) {
    options.add(ExtendedSocketOptions.SO_INCOMING_NAPI_ID);
    options.add(ExtendedSocketOptions.TCP_KEEPCOUNT);
    options.add(ExtendedSocketOptions.TCP_KEEPIDLE);
    options.add(ExtendedSocketOptions.TCP_KEEPINTERVAL);
    options.add(ExtendedSocketOptions.TCP_QUICKACK);
  }

  public static Set<SocketOption<?>> supported(int kind) {
    Set<SocketOption<?>> options = new HashSet<SocketOption<?>>();
    switch (kind) {
    case kTcpClient:
    case kChannelClient:
      options.add(StandardSocketOptions.IP_TOS);
      options.add(StandardSocketOptions.SO_KEEPALIVE);
      options.add(StandardSocketOptions.SO_LINGER);
      options.add(StandardSocketOptions.SO_RCVBUF);
      options.add(StandardSocketOptions.SO_REUSEADDR);
      options.add(StandardSocketOptions.SO_REUSEPORT);
      options.add(StandardSocketOptions.SO_SNDBUF);
      options.add(StandardSocketOptions.TCP_NODELAY);
      addKeepAlive(options);
      if (kind == kChannelClient) {
        options.add(OOBINLINE);
      }
      break;
    case kTcpServer:
      options.add(StandardSocketOptions.IP_TOS);
      options.add(StandardSocketOptions.SO_RCVBUF);
      options.add(StandardSocketOptions.SO_REUSEADDR);
      options.add(StandardSocketOptions.SO_REUSEPORT);
      addKeepAlive(options);
      break;
    case kChannelServer:
    case kAsyncServer:
      options.add(StandardSocketOptions.SO_RCVBUF);
      options.add(StandardSocketOptions.SO_REUSEADDR);
      options.add(StandardSocketOptions.SO_REUSEPORT);
      addKeepAlive(options);
      break;
    case kUdp:
      options.add(StandardSocketOptions.IP_MULTICAST_IF);
      options.add(StandardSocketOptions.IP_MULTICAST_LOOP);
      options.add(StandardSocketOptions.IP_MULTICAST_TTL);
      options.add(StandardSocketOptions.IP_TOS);
      options.add(StandardSocketOptions.SO_BROADCAST);
      options.add(StandardSocketOptions.SO_RCVBUF);
      options.add(StandardSocketOptions.SO_REUSEADDR);
      options.add(StandardSocketOptions.SO_REUSEPORT);
      options.add(StandardSocketOptions.SO_SNDBUF);
      options.add(ExtendedSocketOptions.SO_INCOMING_NAPI_ID);
      options.add(ExtendedSocketOptions.IP_DONTFRAGMENT);
      break;
    case kUnixClient:
      options.add(StandardSocketOptions.SO_LINGER);
      options.add(StandardSocketOptions.SO_RCVBUF);
      options.add(StandardSocketOptions.SO_SNDBUF);
      options.add(ExtendedSocketOptions.SO_PEERCRED);
      break;
    case kUnixServer:
      options.add(StandardSocketOptions.SO_RCVBUF);
      break;
    case kAsyncClient:
      options.add(StandardSocketOptions.SO_KEEPALIVE);
      options.add(StandardSocketOptions.SO_RCVBUF);
      options.add(StandardSocketOptions.SO_REUSEADDR);
      options.add(StandardSocketOptions.SO_REUSEPORT);
      options.add(StandardSocketOptions.SO_SNDBUF);
      options.add(StandardSocketOptions.TCP_NODELAY);
      addKeepAlive(options);
      break;
    default:
      break;
    }
    return Collections.unmodifiableSet(options);
  }

  private static int code(SocketOption<?> name) {
    if (name == ExtendedSocketOptions.TCP_KEEPIDLE) return NetNative.OPT_TCP_KEEPIDLE;
    if (name == ExtendedSocketOptions.TCP_KEEPINTERVAL) return NetNative.OPT_TCP_KEEPINTERVAL;
    if (name == ExtendedSocketOptions.TCP_KEEPCOUNT) return NetNative.OPT_TCP_KEEPCOUNT;
    if (name == ExtendedSocketOptions.TCP_QUICKACK) return NetNative.OPT_TCP_QUICKACK;
    if (name == ExtendedSocketOptions.SO_INCOMING_NAPI_ID) return NetNative.OPT_INCOMING_NAPI_ID;
    if (name == ExtendedSocketOptions.IP_DONTFRAGMENT) return NetNative.OPT_DONTFRAGMENT;
    return -1;
  }

  public static <T> void setExtended(NetHandle net, SocketOption<T> name, T value) throws IOException {
    if (value == null || !name.type().isInstance(value)) {
      throw new IllegalArgumentException("Invalid value '" + value + "'");
    }
    if (name == ExtendedSocketOptions.SO_INCOMING_NAPI_ID) {
      throw new SocketException("Attempt to set read only option " + name);
    }
    if (name == ExtendedSocketOptions.SO_PEERCRED) {
      throw new SocketException("Attempt to set read only option " + name);
    }
    int v = value instanceof Boolean ? (((Boolean) value) ? 1 : 0) : (Integer) value;
    long h = net.begin();
    try {
      NetNative.setOption(h, code(name), v);
    } finally {
      net.end();
    }
  }

  @SuppressWarnings("unchecked")
  public static <T> T getExtended(NetHandle net, SocketOption<T> name) throws IOException {
    long h = net.begin();
    try {
      if (name == ExtendedSocketOptions.SO_PEERCRED) {
        long[] ids = NetNative.peerCredentials(h);
        if (ids == null) {
          throw new SocketException("get SO_PEERCRED failed");
        }
        UserPrincipal user = sun.nio.fs.UnixUserPrincipals.fromUid((int) ids[0]);
        GroupPrincipal group = sun.nio.fs.UnixUserPrincipals.fromGid((int) ids[1]);
        return (T) new UnixDomainPrincipal(user, group);
      }
      int v = NetNative.getOption(h, code(name));
      return name.type() == Boolean.class ? (T) Boolean.valueOf(v != 0) : (T) Integer.valueOf(v);
    } finally {
      net.end();
    }
  }
}
