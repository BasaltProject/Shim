/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 * SPDX-License-Identifier: MIT
 */

package jdk.net;

import java.io.IOException;
import java.net.DatagramSocket;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketOption;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;
import shim.SocketOptionSupport;

public class Sockets {
  private Sockets() { }

  public static <T> void setOption(Socket s, SocketOption<T> name, T value) throws IOException {
    Objects.requireNonNull(s).setOption(name, value);
  }

  public static <T> T getOption(Socket s, SocketOption<T> name) throws IOException {
    return Objects.requireNonNull(s).getOption(name);
  }

  public static <T> void setOption(ServerSocket s, SocketOption<T> name, T value) throws IOException {
    Objects.requireNonNull(s).setOption(name, value);
  }

  public static <T> T getOption(ServerSocket s, SocketOption<T> name) throws IOException {
    return Objects.requireNonNull(s).getOption(name);
  }

  public static <T> void setOption(DatagramSocket s, SocketOption<T> name, T value) throws IOException {
    Objects.requireNonNull(s).setOption(name, value);
  }

  public static <T> T getOption(DatagramSocket s, SocketOption<T> name) throws IOException {
    return Objects.requireNonNull(s).getOption(name);
  }

  @Deprecated(since = "17", forRemoval = true)
  public static Set<SocketOption<?>> supportedOptions(Class<?> socketType) {
    if (socketType == Socket.class) {
      return SocketOptionSupport.supported(SocketOptionSupport.kTcpClient);
    }
    if (socketType == ServerSocket.class) {
      return SocketOptionSupport.supported(SocketOptionSupport.kTcpServer);
    }
    if (socketType == DatagramSocket.class) {
      Set<SocketOption<?>> options = new HashSet<SocketOption<?>>();
      options.add(java.net.StandardSocketOptions.SO_SNDBUF);
      options.add(java.net.StandardSocketOptions.SO_RCVBUF);
      options.add(java.net.StandardSocketOptions.SO_REUSEADDR);
      options.add(java.net.StandardSocketOptions.SO_REUSEPORT);
      options.add(java.net.StandardSocketOptions.IP_TOS);
      options.add(ExtendedSocketOptions.SO_INCOMING_NAPI_ID);
      return java.util.Collections.unmodifiableSet(options);
    }
    throw new IllegalArgumentException("unknown socket type");
  }
}
