/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 * SPDX-License-Identifier: MIT
 * Minimum release: 21
 */

import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketOption;
import java.net.StandardProtocolFamily;
import java.net.StandardSocketOptions;
import java.net.UnixDomainSocketAddress;
import java.nio.channels.AsynchronousServerSocketChannel;
import java.nio.channels.AsynchronousSocketChannel;
import java.nio.channels.DatagramChannel;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Set;
import java.util.TreeSet;
import java.util.concurrent.Callable;
import jdk.net.ExtendedSocketOptions;
import jdk.net.NetworkPermission;
import jdk.net.Sockets;
import jdk.net.UnixDomainPrincipal;

public class ExtendedSocketOptionsModern {
  static void t(String name, Callable<Object> c) {
    try {
      System.out.println(name + " = " + c.call());
    } catch (Throwable e) {
      System.out.println(name + " ! " + e.getClass().getName() + (e instanceof NullPointerException ? "" : ": "
          + e.getMessage()));
    }
  }

  static String names(Set<SocketOption<?>> options) {
    TreeSet<String> sorted = new TreeSet<>();
    for (SocketOption<?> o : options) {
      sorted.add(o.name() + ":" + o.type().getSimpleName());
    }
    return sorted.toString();
  }

  public static void main(String[] args) throws Exception {
    t("socket", () -> names(new Socket().supportedOptions()));
    t("server-socket", () -> names(new ServerSocket().supportedOptions()));
    t("datagram-socket", () -> names(new DatagramSocket(null).supportedOptions()));
    t("socket-channel", () -> names(SocketChannel.open().supportedOptions()));
    t("server-channel", () -> names(ServerSocketChannel.open().supportedOptions()));
    t("datagram-channel", () -> names(DatagramChannel.open().supportedOptions()));
    t("unix-channel", () -> names(SocketChannel.open(StandardProtocolFamily.UNIX).supportedOptions()));
    t("unix-server-channel", () -> names(ServerSocketChannel.open(StandardProtocolFamily.UNIX).supportedOptions()));
    t("async-socket", () -> names(AsynchronousSocketChannel.open().supportedOptions()));
    t("async-server", () -> names(AsynchronousServerSocketChannel.open().supportedOptions()));
    t("sockets-supported-socket", () -> names(Sockets.supportedOptions(Socket.class)));
    t("sockets-supported-server", () -> names(Sockets.supportedOptions(ServerSocket.class)));
    t("sockets-supported-datagram", () -> names(Sockets.supportedOptions(DatagramSocket.class)));
    t("sockets-supported-bad", () -> names(Sockets.supportedOptions(String.class)));
    t("option-names", () -> ExtendedSocketOptions.TCP_KEEPIDLE + " " + ExtendedSocketOptions.TCP_KEEPINTERVAL + " "
        + ExtendedSocketOptions.TCP_KEEPCOUNT + " " + ExtendedSocketOptions.TCP_QUICKACK + " "
        + ExtendedSocketOptions.SO_INCOMING_NAPI_ID + " " + ExtendedSocketOptions.IP_DONTFRAGMENT + " "
        + ExtendedSocketOptions.SO_PEERCRED);
    t("option-types", () -> ExtendedSocketOptions.TCP_KEEPIDLE.type() + " " + ExtendedSocketOptions.TCP_QUICKACK.type()
        + " " + ExtendedSocketOptions.SO_PEERCRED.type());

    t("tcp-keepalive-roundtrip", () -> {
      try (Socket s = new Socket()) {
        s.setOption(ExtendedSocketOptions.TCP_KEEPIDLE, 61);
        s.setOption(ExtendedSocketOptions.TCP_KEEPINTERVAL, 12);
        s.setOption(ExtendedSocketOptions.TCP_KEEPCOUNT, 4);
        return s.getOption(ExtendedSocketOptions.TCP_KEEPIDLE) + " " + s.getOption(ExtendedSocketOptions.TCP_KEEPINTERVAL)
            + " " + s.getOption(ExtendedSocketOptions.TCP_KEEPCOUNT);
      }
    });
    t("tcp-keepidle-zero", () -> {
      try (Socket s = new Socket()) {
        s.setOption(ExtendedSocketOptions.TCP_KEEPIDLE, 0);
        return "ok";
      }
    });
    t("tcp-keepidle-negative", () -> {
      try (Socket s = new Socket()) {
        s.setOption(ExtendedSocketOptions.TCP_KEEPIDLE, -1);
        return "ok";
      }
    });
    t("tcp-keepidle-null", () -> {
      try (Socket s = new Socket()) {
        s.setOption(ExtendedSocketOptions.TCP_KEEPIDLE, null);
        return "ok";
      }
    });
    t("tcp-keepcount-large", () -> {
      try (Socket s = new Socket()) {
        s.setOption(ExtendedSocketOptions.TCP_KEEPCOUNT, 100000);
        return s.getOption(ExtendedSocketOptions.TCP_KEEPCOUNT);
      }
    });
    t("quickack", () -> {
      try (Socket s = new Socket()) {
        s.setOption(ExtendedSocketOptions.TCP_QUICKACK, true);
        Object first = s.getOption(ExtendedSocketOptions.TCP_QUICKACK);
        s.setOption(ExtendedSocketOptions.TCP_QUICKACK, false);
        return first + " " + s.getOption(ExtendedSocketOptions.TCP_QUICKACK);
      }
    });
    t("napi-id-get", () -> {
      try (Socket s = new Socket()) {
        return s.getOption(ExtendedSocketOptions.SO_INCOMING_NAPI_ID) >= 0;
      }
    });
    t("napi-id-set", () -> {
      try (Socket s = new Socket()) {
        s.setOption(ExtendedSocketOptions.SO_INCOMING_NAPI_ID, 1);
        return "ok";
      }
    });
    t("peercred-on-tcp", () -> {
      try (Socket s = new Socket()) {
        return s.getOption(ExtendedSocketOptions.SO_PEERCRED);
      }
    });
    t("dontfragment-on-tcp", () -> {
      try (Socket s = new Socket()) {
        return s.getOption(ExtendedSocketOptions.IP_DONTFRAGMENT);
      }
    });
    t("dontfragment-udp", () -> {
      try (DatagramSocket d = new DatagramSocket(null)) {
        Object before = d.getOption(ExtendedSocketOptions.IP_DONTFRAGMENT);
        d.setOption(ExtendedSocketOptions.IP_DONTFRAGMENT, true);
        Object after = d.getOption(ExtendedSocketOptions.IP_DONTFRAGMENT);
        d.setOption(ExtendedSocketOptions.IP_DONTFRAGMENT, false);
        return before + " " + after + " " + d.getOption(ExtendedSocketOptions.IP_DONTFRAGMENT);
      }
    });
    t("keepalive-on-udp", () -> {
      try (DatagramSocket d = new DatagramSocket(null)) {
        d.setOption(ExtendedSocketOptions.TCP_KEEPIDLE, 5);
        return "ok";
      }
    });
    t("server-keepalive", () -> {
      try (ServerSocket s = new ServerSocket()) {
        s.setOption(ExtendedSocketOptions.TCP_KEEPIDLE, 33);
        s.setOption(ExtendedSocketOptions.TCP_QUICKACK, true);
        return s.getOption(ExtendedSocketOptions.TCP_KEEPIDLE) + " " + s.getOption(ExtendedSocketOptions.TCP_QUICKACK);
      }
    });
    t("channel-keepalive", () -> {
      try (SocketChannel c = SocketChannel.open()) {
        c.setOption(ExtendedSocketOptions.TCP_KEEPIDLE, 44);
        c.setOption(ExtendedSocketOptions.TCP_KEEPINTERVAL, 9);
        c.setOption(ExtendedSocketOptions.TCP_KEEPCOUNT, 3);
        return c.getOption(ExtendedSocketOptions.TCP_KEEPIDLE) + " " + c.getOption(ExtendedSocketOptions.TCP_KEEPINTERVAL)
            + " " + c.getOption(ExtendedSocketOptions.TCP_KEEPCOUNT);
      }
    });
    t("server-channel-keepalive", () -> {
      try (ServerSocketChannel c = ServerSocketChannel.open()) {
        c.setOption(ExtendedSocketOptions.TCP_KEEPCOUNT, 7);
        return c.getOption(ExtendedSocketOptions.TCP_KEEPCOUNT);
      }
    });
    t("async-keepalive", () -> {
      try (AsynchronousSocketChannel c = AsynchronousSocketChannel.open()) {
        c.setOption(ExtendedSocketOptions.TCP_KEEPIDLE, 55);
        return c.getOption(ExtendedSocketOptions.TCP_KEEPIDLE);
      }
    });
    t("sockets-set-get", () -> {
      try (Socket s = new Socket()) {
        Sockets.setOption(s, ExtendedSocketOptions.TCP_KEEPIDLE, 77);
        return Sockets.getOption(s, ExtendedSocketOptions.TCP_KEEPIDLE);
      }
    });
    t("sockets-server", () -> {
      try (ServerSocket s = new ServerSocket()) {
        Sockets.setOption(s, StandardSocketOptions.SO_REUSEADDR, true);
        return Sockets.getOption(s, StandardSocketOptions.SO_REUSEADDR);
      }
    });
    t("sockets-datagram", () -> {
      try (DatagramSocket s = new DatagramSocket(null)) {
        Sockets.setOption(s, StandardSocketOptions.SO_BROADCAST, false);
        return Sockets.getOption(s, StandardSocketOptions.SO_BROADCAST);
      }
    });
    t("sockets-null", () -> {
      Sockets.setOption((Socket) null, ExtendedSocketOptions.TCP_KEEPIDLE, 1);
      return "ok";
    });
    t("closed-socket", () -> {
      Socket s = new Socket();
      s.close();
      return s.getOption(ExtendedSocketOptions.TCP_KEEPIDLE);
    });
    t("network-permission", () -> {
      NetworkPermission p = new NetworkPermission("setOption.SO_FLOW_SLA");
      return p.getName() + " " + p.getActions().isEmpty() + " " + p.implies(new NetworkPermission("setOption.SO_FLOW_SLA"))
          + " " + p.implies(new NetworkPermission("other"));
    });
    t("network-permission-wild", () -> new NetworkPermission("setOption.*").implies(new NetworkPermission(
        "setOption.SO_FLOW_SLA")));
    t("network-permission-null", () -> new NetworkPermission(null));
    t("network-permission-empty", () -> new NetworkPermission(""));

    t("unix-peercred", () -> {
      Path dir = Files.createTempDirectory("peer");
      Path file = dir.resolve("s.sock");
      UnixDomainSocketAddress address = UnixDomainSocketAddress.of(file);
      try (ServerSocketChannel server = ServerSocketChannel.open(StandardProtocolFamily.UNIX)) {
        server.bind(address);
        try (SocketChannel client = SocketChannel.open(StandardProtocolFamily.UNIX)) {
          client.connect(address);
          try (SocketChannel peer = server.accept()) {
            UnixDomainPrincipal fromPeer = peer.getOption(ExtendedSocketOptions.SO_PEERCRED);
            UnixDomainPrincipal fromClient = client.getOption(ExtendedSocketOptions.SO_PEERCRED);
            String user = System.getProperty("user.name");
            return fromPeer.user().getName().equals(user) + " " + fromClient.user().getName().equals(user) + " "
                + (fromPeer.group().getName() != null) + " " + fromPeer.equals(fromClient) + " "
                + (fromPeer.hashCode() == fromClient.hashCode()) + " "
                + fromPeer.toString().startsWith("UnixDomainPrincipal[user=");
          }
        }
      } finally {
        Files.deleteIfExists(file);
        Files.deleteIfExists(dir);
      }
    });
    t("unix-peercred-set", () -> {
      try (SocketChannel client = SocketChannel.open(StandardProtocolFamily.UNIX)) {
        client.setOption(ExtendedSocketOptions.SO_PEERCRED, null);
        return "ok";
      }
    });
    t("unix-peercred-unconnected", () -> {
      try (SocketChannel client = SocketChannel.open(StandardProtocolFamily.UNIX)) {
        UnixDomainPrincipal p = client.getOption(ExtendedSocketOptions.SO_PEERCRED);
        return p.user().getName().equals(System.getProperty("user.name"));
      }
    });
    t("principal-ctor", () -> {
      Path dir = Files.createTempDirectory("owner");
      try {
        java.nio.file.attribute.UserPrincipal user = Files.getOwner(dir);
        java.nio.file.attribute.GroupPrincipal group = Files.readAttributes(dir,
            java.nio.file.attribute.PosixFileAttributes.class).group();
        UnixDomainPrincipal p = new UnixDomainPrincipal(user, group);
        return p.user().equals(user) + " " + p.group().equals(group) + " " + p.equals(new UnixDomainPrincipal(user,
            group));
      } finally {
        Files.deleteIfExists(dir);
      }
    });
    t("principal-null", () -> new UnixDomainPrincipal(null, null));
  }
}
