/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 * SPDX-License-Identifier: MIT
 * Minimum release: 21
 */

import java.io.*;
import java.net.*;
import java.util.*;
import java.util.function.Supplier;

public class NetworkingModern {
  static void probe(String name, Supplier<Object> body) {
    String out;
    try {
      out = String.valueOf(body.get());
    } catch (Throwable t) {
      out = "!" + t.getClass().getName() + ":" + t.getMessage();
    }
    System.out.println(name + "=" + out.replaceAll("Nombre o servicio desconocido|Name or service not known", "NSK").replaceAll("La direcci\u00f3n ya se est\u00e1 usando|Address already in use", "AIU").replaceAll("Conexi\u00f3n rehusada|Connection refused", "CR").replaceAll("\\b(?:localport|port)=\\d{4,5}", "port=P").replaceAll(":\\d{4,5}\\b", ":P"));
  }

  interface IoBody { Object run() throws Exception; }

  static void io(String name, IoBody body) {
    probe(name, () -> {
      try { return body.run(); } catch (Exception e) { return "!" + e.getClass().getName() + ":" + e.getMessage(); }
    });
  }

  static Thread start(Runnable r) { Thread t = new Thread(r); t.setDaemon(true); t.start(); return t; }

  public static void main(String[] args) throws Exception {
    String[] literals = {"127.0.0.1", "0.0.0.0", "255.255.255.255", "10.1.2.3", "172.16.0.1", "172.32.0.1", "192.168.1.1", "169.254.1.1", "224.0.0.1", "224.0.1.1", "239.255.0.1", "239.192.0.1", "238.1.1.1", "::1", "::", "fe80::1", "fec0::1", "ff02::1", "ff0e::1", "ff05::1", "ff01::1", "ff08::1", "2001:db8::1", "::ffff:1.2.3.4", "::1.2.3.4", "1:2:3:4:5:6:7:8", "1::", "[::1]", "fe80::1%1", "1.2.3", "1.2.3.4.5", "256.1.1.1", "01.2.3.4", "1.2.3.", ":::", "12345::", "gggg::1"};
    for (String l : literals) {
      probe("literal[" + l + "]", () -> {
        try {
          InetAddress a = InetAddress.getByName(l);
          return a.getClass().getSimpleName() + " " + a + " " + a.getHostAddress() + " " + Arrays.toString(a.getAddress()) + " " + a.isAnyLocalAddress() + a.isLoopbackAddress() + a.isLinkLocalAddress() + a.isSiteLocalAddress() + a.isMulticastAddress() + a.isMCGlobal() + a.isMCNodeLocal() + a.isMCLinkLocal() + a.isMCSiteLocal() + a.isMCOrgLocal() + " " + a.hashCode();
        } catch (UnknownHostException e) { return "UHE " + e.getMessage(); }
      });
      probe("ofLiteral[" + l + "]", () -> InetAddress.ofLiteral(l).getHostAddress());
    }
    io("by-address", () -> InetAddress.getByAddress(new byte[] {1, 2, 3, 4}) + " " + InetAddress.getByAddress("host", new byte[] {1, 2, 3, 4}) + " " + InetAddress.getByAddress("v6", new byte[16]) + " " + InetAddress.getByAddress(new byte[] {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, (byte) 255, (byte) 255, 9, 8, 7, 6}).getClass().getSimpleName());
    io("by-address-bad", () -> InetAddress.getByAddress(new byte[3]));
    io("by-address-null", () -> InetAddress.getByAddress((byte[]) null));
    io("by-address-brackets", () -> InetAddress.getByAddress("[h]", new byte[] {1, 1, 1, 1}).getHostName());
    io("loopback", () -> InetAddress.getLoopbackAddress() + " " + InetAddress.getByName(null) + " " + InetAddress.getByName("") + " " + InetAddress.getByName("localhost").isLoopbackAddress());
    io("all-by-name-literal", () -> Arrays.toString(InetAddress.getAllByName("10.0.0.1")));
    io("resolve-localhost", () -> { InetAddress[] all = InetAddress.getAllByName("localhost"); boolean has4 = false; for (InetAddress a : all) has4 |= a instanceof Inet4Address; return has4 + " " + all[0].getHostName() + " " + all[0].getHostAddress().equals("127.0.0.1"); });
    io("resolve-bad", () -> InetAddress.getByName("no-such-host.invalid"));
    io("resolve-bad-bracket", () -> InetAddress.getByName("[1.2.3.4]"));
    io("resolve-bad-bracket2", () -> InetAddress.getByName("[::1"));
    io("equals-hash", () -> { InetAddress a = InetAddress.getByName("1.2.3.4"), b = InetAddress.getByAddress("named", new byte[] {1, 2, 3, 4}); InetAddress c = InetAddress.getByName("::1"); return a.equals(b) + " " + (a.hashCode() == b.hashCode()) + " " + a.equals(c) + " " + a.equals(null) + " " + c.equals(InetAddress.getByName("0:0:0:0:0:0:0:1")); });
    io("to-string", () -> InetAddress.getByAddress("named", new byte[] {1, 2, 3, 4}) + " " + InetAddress.getByName("::1") + " " + InetAddress.getByAddress("six", new byte[] {0x20, 1, 0xd, (byte) 0xb8, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1}));
    io("inet6-scope", () -> { Inet6Address a = Inet6Address.getByAddress("h", new byte[] {(byte) 0xfe, (byte) 0x80, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1}, 3); return a.getScopeId() + " " + a.getHostAddress() + " " + a.getScopedInterface() + " " + a.isIPv4CompatibleAddress() + " " + a; });
    io("inet6-compat", () -> ((Inet6Address) InetAddress.getByName("::1.2.3.4")).isIPv4CompatibleAddress() + " " + ((Inet6Address) InetAddress.getByName("2001::1")).isIPv4CompatibleAddress());
    io("socket-address", () -> new InetSocketAddress(80) + " " + new InetSocketAddress("1.2.3.4", 80) + " " + new InetSocketAddress(InetAddress.getByName("::1"), 80) + " " + InetSocketAddress.createUnresolved("example.org", 443) + " " + new InetSocketAddress((InetAddress) null, 5) + " " + new InetSocketAddress("no-such-host.invalid", 6));
    io("socket-address-props", () -> { InetSocketAddress a = new InetSocketAddress("1.2.3.4", 80); InetSocketAddress u = InetSocketAddress.createUnresolved("x.y", 9); return a.getHostString() + " " + a.getPort() + " " + a.isUnresolved() + " " + u.isUnresolved() + " " + u.getHostName() + " " + u.getAddress() + " " + a.getAddress() + " " + u.getHostString(); });
    io("socket-address-eq", () -> { InetSocketAddress a = new InetSocketAddress("1.2.3.4", 80), b = new InetSocketAddress(InetAddress.getByName("1.2.3.4"), 80); InetSocketAddress u1 = InetSocketAddress.createUnresolved("A.B", 1), u2 = InetSocketAddress.createUnresolved("a.b", 1); return a.equals(b) + " " + (a.hashCode() == b.hashCode()) + " " + u1.equals(u2) + " " + (u1.hashCode() == u2.hashCode()) + " " + a.equals(u1) + " " + new InetSocketAddress(1).equals(new InetSocketAddress(2)); });
    io("socket-address-bad", () -> new InetSocketAddress(70000));
    io("socket-address-bad2", () -> new InetSocketAddress("h", -1));
    io("socket-address-bad3", () -> InetSocketAddress.createUnresolved(null, 80));
    io("socket-address-bad4", () -> new InetSocketAddress((String) null, 80));
    io("tcp-echo", () -> {
      try (ServerSocket server = new ServerSocket(0, 5, InetAddress.getLoopbackAddress())) {
        int port = server.getLocalPort();
        Thread t = start(() -> { try (Socket c = server.accept()) { InputStream in = c.getInputStream(); OutputStream out = c.getOutputStream(); byte[] buf = new byte[64]; int n; while ((n = in.read(buf)) > 0) { out.write(buf, 0, n); } } catch (IOException e) { } });
        try (Socket s = new Socket(InetAddress.getLoopbackAddress(), port)) {
          s.getOutputStream().write("hello world".getBytes());
          byte[] buf = new byte[11];
          int got = 0;
          while (got < 11) got += s.getInputStream().read(buf, got, 11 - got);
          String r = new String(buf) + " " + s.isConnected() + " " + s.isBound() + " " + s.isClosed() + " " + (s.getPort() == port) + " " + s.getInetAddress() + " " + s.getLocalAddress() + " " + (s.getLocalPort() > 0) + " " + s.isInputShutdown() + s.isOutputShutdown();
          s.shutdownOutput();
          int eof = s.getInputStream().read();
          t.join(2000);
          return r + " " + eof + " " + s.isOutputShutdown();
        }
      }
    });
    io("tcp-to-string", () -> { try (ServerSocket server = new ServerSocket(0, 5, InetAddress.getLoopbackAddress())) { String a = server.toString(); try (Socket s = new Socket(InetAddress.getLoopbackAddress(), server.getLocalPort())) { String b = s.toString(); return a + " | " + b + " | " + new Socket() + " | " + new ServerSocket().toString(); } } });
    io("server-props", () -> { try (ServerSocket server = new ServerSocket()) { String a = server.isBound() + " " + server.getInetAddress() + " " + server.getLocalPort() + " " + server.getLocalSocketAddress() + " " + server.isClosed(); server.bind(new InetSocketAddress(InetAddress.getLoopbackAddress(), 0)); return a + " | " + server.isBound() + " " + server.getInetAddress() + " " + (server.getLocalPort() > 0) + " " + server.getReuseAddress() + " " + server.getSoTimeout() + " " + (server.getReceiveBufferSize() > 0); } });
    io("server-wildcard", () -> { try (ServerSocket server = new ServerSocket(0)) { return server.getInetAddress() + " " + server.getLocalSocketAddress().toString().replaceAll(":\\d+", ":P"); } });
    io("server-bind-twice", () -> { try (ServerSocket server = new ServerSocket(0)) { server.bind(new InetSocketAddress(0)); return "no"; } });
    io("server-port-in-use", () -> { try (ServerSocket a = new ServerSocket(0, 5, InetAddress.getLoopbackAddress())) { new ServerSocket(a.getLocalPort(), 5, InetAddress.getLoopbackAddress()); return "no"; } });
    io("server-bad-port", () -> new ServerSocket(70000));
    io("accept-timeout", () -> { try (ServerSocket server = new ServerSocket(0)) { server.setSoTimeout(100); long t = System.nanoTime(); try { server.accept(); return "no"; } catch (SocketTimeoutException e) { return e.getMessage() + " " + ((System.nanoTime() - t) / 1000000 >= 90) + " " + server.getSoTimeout(); } } });
    io("accept-closed", () -> { ServerSocket server = new ServerSocket(0); server.close(); server.accept(); return "no"; });
    io("accept-unbound", () -> new ServerSocket().accept());
    io("accept-interrupted-by-close", () -> { ServerSocket server = new ServerSocket(0); String[] r = new String[1]; Thread t = start(() -> { try { server.accept(); r[0] = "accepted"; } catch (IOException e) { r[0] = e.getClass().getSimpleName() + ":" + e.getMessage(); } }); Thread.sleep(100); server.close(); t.join(2000); return r[0]; });
    io("read-timeout", () -> { try (ServerSocket server = new ServerSocket(0, 5, InetAddress.getLoopbackAddress()); Socket s = new Socket(InetAddress.getLoopbackAddress(), server.getLocalPort()); Socket peer = server.accept()) { s.setSoTimeout(100); try { s.getInputStream().read(); return "no"; } catch (SocketTimeoutException e) { return e.getMessage() + " " + s.getSoTimeout() + " " + s.isClosed(); } } });
    io("read-blocked-then-close", () -> { ServerSocket server = new ServerSocket(0, 5, InetAddress.getLoopbackAddress()); Socket s = new Socket(InetAddress.getLoopbackAddress(), server.getLocalPort()); Socket peer = server.accept(); String[] r = new String[1]; Thread t = start(() -> { try { r[0] = "read " + s.getInputStream().read(); } catch (IOException e) { r[0] = e.getClass().getSimpleName() + ":" + e.getMessage(); } }); Thread.sleep(100); s.close(); t.join(2000); peer.close(); server.close(); return r[0]; });
    io("peer-close-eof", () -> { try (ServerSocket server = new ServerSocket(0, 5, InetAddress.getLoopbackAddress()); Socket s = new Socket(InetAddress.getLoopbackAddress(), server.getLocalPort()); Socket peer = server.accept()) { peer.getOutputStream().write(new byte[] {1, 2, 3}); peer.close(); byte[] b = new byte[10]; int n = s.getInputStream().read(b); int eof = s.getInputStream().read(b); return n + " " + eof + " " + s.getInputStream().available(); } });
    io("available", () -> { try (ServerSocket server = new ServerSocket(0, 5, InetAddress.getLoopbackAddress()); Socket s = new Socket(InetAddress.getLoopbackAddress(), server.getLocalPort()); Socket peer = server.accept()) { int a = s.getInputStream().available(); peer.getOutputStream().write(new byte[100]); Thread.sleep(100); int b = s.getInputStream().available(); s.getInputStream().read(new byte[40]); return a + " " + b + " " + s.getInputStream().available(); } });
    io("connect-refused", () -> { int port; try (ServerSocket tmp = new ServerSocket(0, 5, InetAddress.getLoopbackAddress())) { port = tmp.getLocalPort(); } new Socket(InetAddress.getLoopbackAddress(), port); return "no"; });
    io("connect-timeout-arg", () -> { Socket s = new Socket(); try { s.connect(new InetSocketAddress("127.0.0.1", 1), -1); return "no"; } catch (IllegalArgumentException e) { return e.getMessage(); } });
    io("connect-unresolved", () -> { Socket s = new Socket(); s.connect(InetSocketAddress.createUnresolved("no-such-host.invalid", 80)); return "no"; });
    io("connect-null", () -> { new Socket().connect(null); return "no"; });
    io("connect-twice", () -> { try (ServerSocket server = new ServerSocket(0, 5, InetAddress.getLoopbackAddress()); Socket s = new Socket()) { s.connect(server.getLocalSocketAddress()); s.connect(server.getLocalSocketAddress()); return "no"; } });
    io("connect-closed", () -> { Socket s = new Socket(); s.close(); s.connect(new InetSocketAddress("127.0.0.1", 1)); return "no"; });
    io("unconnected-streams", () -> { Socket s = new Socket(); String r = ""; try { s.getInputStream(); } catch (SocketException e) { r += e.getMessage(); } try { s.getOutputStream(); } catch (SocketException e) { r += "|" + e.getMessage(); } return r + "|" + s.getInetAddress() + " " + s.getPort() + " " + s.getLocalPort() + " " + s.getLocalAddress() + " " + s.getRemoteSocketAddress() + " " + s.getLocalSocketAddress(); });
    io("closed-streams", () -> { try (ServerSocket server = new ServerSocket(0, 5, InetAddress.getLoopbackAddress())) { Socket s = new Socket(InetAddress.getLoopbackAddress(), server.getLocalPort()); InputStream in = s.getInputStream(); OutputStream out = s.getOutputStream(); s.close(); String r = ""; try { in.read(); } catch (SocketException e) { r += e.getMessage(); } try { out.write(1); } catch (SocketException e) { r += "|" + e.getMessage(); } try { s.getInputStream(); } catch (SocketException e) { r += "|" + e.getMessage(); } return r + "|" + s.isClosed() + " " + s.isConnected() + " " + s.getInetAddress() + " " + (s.getPort() > 0); } });
    io("shutdown-states", () -> { try (ServerSocket server = new ServerSocket(0, 5, InetAddress.getLoopbackAddress()); Socket s = new Socket(InetAddress.getLoopbackAddress(), server.getLocalPort()); Socket peer = server.accept()) { s.shutdownOutput(); String r = s.isOutputShutdown() + ""; try { s.shutdownOutput(); } catch (SocketException e) { r += "|" + e.getMessage(); } try { s.getOutputStream(); } catch (SocketException e) { r += "|" + e.getMessage(); } s.shutdownInput(); r += "|" + s.isInputShutdown() + " " + s.getInputStream().getClass().getSimpleName().isEmpty(); return r; } catch (SocketException e) { return e.getMessage(); } });
    io("options", () -> { try (ServerSocket server = new ServerSocket(0, 5, InetAddress.getLoopbackAddress()); Socket s = new Socket(InetAddress.getLoopbackAddress(), server.getLocalPort())) { boolean d0 = s.getTcpNoDelay(); s.setTcpNoDelay(true); boolean k0 = s.getKeepAlive(); s.setKeepAlive(true); s.setSoLinger(true, 5); int l = s.getSoLinger(); s.setSoLinger(false, 0); int l2 = s.getSoLinger(); s.setReuseAddress(true); s.setSoTimeout(1234); s.setReceiveBufferSize(65536); s.setSendBufferSize(65536); s.setOOBInline(true); return d0 + " " + s.getTcpNoDelay() + " " + k0 + " " + s.getKeepAlive() + " " + l + " " + l2 + " " + s.getReuseAddress() + " " + s.getSoTimeout() + " " + (s.getReceiveBufferSize() >= 65536) + " " + (s.getSendBufferSize() >= 65536) + " " + s.getOOBInline(); } });
    io("options-bad", () -> { Socket s = new Socket(); String r = ""; try { s.setSoTimeout(-1); } catch (IllegalArgumentException e) { r += e.getMessage(); } try { s.setSendBufferSize(0); } catch (IllegalArgumentException e) { r += "|" + e.getMessage(); } try { s.setReceiveBufferSize(-1); } catch (IllegalArgumentException e) { r += "|" + e.getMessage(); } try { s.setTrafficClass(300); } catch (IllegalArgumentException e) { r += "|" + e.getMessage(); } try { s.setSoLinger(true, -1); } catch (IllegalArgumentException e) { r += "|" + e.getMessage(); } return r; });
    io("socket-options-api", () -> { try (Socket s = new Socket()) { s.setOption(StandardSocketOptions.SO_KEEPALIVE, true); s.setOption(StandardSocketOptions.TCP_NODELAY, true); s.setOption(StandardSocketOptions.SO_REUSEADDR, true); List<String> names = new ArrayList<>(); for (SocketOption<?> o : s.supportedOptions()) if (!o.name().startsWith("TCP_KEEP") && !o.name().equals("TCP_QUICKACK") && !o.name().equals("SO_INCOMING_NAPI_ID")) names.add(o.name()); Collections.sort(names); return s.getOption(StandardSocketOptions.SO_KEEPALIVE) + " " + s.getOption(StandardSocketOptions.TCP_NODELAY) + " " + names; } });
    io("socket-option-unsupported", () -> new Socket().setOption(StandardSocketOptions.SO_BROADCAST, true));
    io("urgent", () -> { try (ServerSocket server = new ServerSocket(0, 5, InetAddress.getLoopbackAddress()); Socket s = new Socket(InetAddress.getLoopbackAddress(), server.getLocalPort())) { s.sendUrgentData(1); return "sent"; } });
    io("bind-explicit", () -> { try (Socket s = new Socket()) { s.bind(new InetSocketAddress(InetAddress.getLoopbackAddress(), 0)); return s.isBound() + " " + s.isConnected() + " " + s.getLocalAddress() + " " + (s.getLocalPort() > 0) + " " + s.getLocalSocketAddress().toString().replaceAll(":\\d+", ":P"); } });
    io("bind-twice", () -> { try (Socket s = new Socket()) { s.bind(new InetSocketAddress(0)); s.bind(new InetSocketAddress(0)); return "no"; } });
    io("bind-null-socket", () -> { try (Socket s = new Socket()) { s.bind(null); return s.isBound() + " " + (s.getLocalPort() > 0) + " " + s.getLocalAddress(); } });
    io("socket-ctor-host", () -> { try (ServerSocket server = new ServerSocket(0, 5, InetAddress.getLoopbackAddress()); Socket s = new Socket("127.0.0.1", server.getLocalPort()); Socket t = new Socket((String) null, server.getLocalPort())) { return s.isConnected() + " " + t.isConnected() + " " + t.getInetAddress(); } });
    io("socket-ctor-local", () -> { try (ServerSocket server = new ServerSocket(0, 5, InetAddress.getLoopbackAddress()); Socket s = new Socket(InetAddress.getLoopbackAddress(), server.getLocalPort(), InetAddress.getLoopbackAddress(), 0)) { return s.getLocalAddress() + " " + s.isBound(); } });
    io("socket-ctor-proxy", () -> new Socket(Proxy.NO_PROXY).isConnected() + " " + new Socket(new Proxy(Proxy.Type.DIRECT, null)).isBound());
    io("socket-ctor-proxy-bad", () -> new Socket((Proxy) null));
    io("write-after-peer-close", () -> { try (ServerSocket server = new ServerSocket(0, 5, InetAddress.getLoopbackAddress()); Socket s = new Socket(InetAddress.getLoopbackAddress(), server.getLocalPort())) { Socket peer = server.accept(); peer.close(); Thread.sleep(50); OutputStream o = s.getOutputStream(); String r = ""; try { for (int i = 0; i < 100; i++) { o.write(new byte[8192]); } } catch (SocketException e) { r = e.getClass().getSimpleName(); } return !r.isEmpty(); } });
    io("large-transfer", () -> { try (ServerSocket server = new ServerSocket(0, 5, InetAddress.getLoopbackAddress())) { int total = 3_000_000; long[] sum = {0}; Thread t = start(() -> { try (Socket c = server.accept()) { byte[] buf = new byte[65536]; int n; while ((n = c.getInputStream().read(buf)) > 0) for (int i = 0; i < n; i++) sum[0] += buf[i] & 0xff; } catch (IOException e) { } }); try (Socket s = new Socket(InetAddress.getLoopbackAddress(), server.getLocalPort())) { byte[] chunk = new byte[10000]; Arrays.fill(chunk, (byte) 3); for (int i = 0; i < total / chunk.length; i++) s.getOutputStream().write(chunk); } t.join(10000); return sum[0]; } });
    io("many-connections", () -> { try (ServerSocket server = new ServerSocket(0, 50, InetAddress.getLoopbackAddress())) { int[] served = {0}; Thread t = start(() -> { for (int i = 0; i < 20; i++) { try (Socket c = server.accept()) { c.getOutputStream().write(i); served[0]++; } catch (IOException e) { return; } } }); int sum = 0; for (int i = 0; i < 20; i++) { try (Socket s = new Socket(InetAddress.getLoopbackAddress(), server.getLocalPort())) { sum += s.getInputStream().read(); } } t.join(5000); return sum + " " + served[0]; } });
    io("udp-roundtrip", () -> {
      try (DatagramSocket a = new DatagramSocket(0, InetAddress.getLoopbackAddress()); DatagramSocket b = new DatagramSocket(0, InetAddress.getLoopbackAddress())) {
        byte[] msg = "ping".getBytes();
        a.send(new DatagramPacket(msg, msg.length, InetAddress.getLoopbackAddress(), b.getLocalPort()));
        DatagramPacket p = new DatagramPacket(new byte[100], 100);
        b.setSoTimeout(2000);
        b.receive(p);
        String r = new String(p.getData(), p.getOffset(), p.getLength()) + " " + p.getLength() + " " + p.getAddress() + " " + (p.getPort() == a.getLocalPort()) + " " + p.getSocketAddress().toString().replaceAll(":\\d+", ":P");
        byte[] reply = "pong!".getBytes();
        b.send(new DatagramPacket(reply, reply.length, p.getSocketAddress()));
        DatagramPacket q = new DatagramPacket(new byte[3], 3);
        a.setSoTimeout(2000);
        a.receive(q);
        return r + " | " + new String(q.getData(), 0, q.getLength()) + " " + q.getLength() + " " + q.getData().length;
      }
    });
    io("udp-timeout", () -> { try (DatagramSocket s = new DatagramSocket(0, InetAddress.getLoopbackAddress())) { s.setSoTimeout(100); try { s.receive(new DatagramPacket(new byte[10], 10)); return "no"; } catch (SocketTimeoutException e) { return e.getMessage() + " " + s.getSoTimeout(); } } });
    io("udp-props", () -> { try (DatagramSocket s = new DatagramSocket(0, InetAddress.getLoopbackAddress())) { String a = s.isBound() + " " + s.isConnected() + " " + s.getInetAddress() + " " + s.getPort() + " " + s.getLocalAddress() + " " + (s.getLocalPort() > 0) + " " + s.getRemoteSocketAddress() + " " + s.isClosed() + " " + s.getBroadcast() + " " + s.getChannel(); s.close(); return a + " | " + s.isClosed() + " " + s.getLocalPort() + " " + s.getLocalAddress() + " " + s.getLocalSocketAddress(); } });
    io("udp-unbound", () -> { try (DatagramSocket s = new DatagramSocket(null)) { return s.isBound() + " " + s.getLocalPort() + " " + s.getLocalSocketAddress() + " " + s.getLocalAddress(); } });
    io("udp-wildcard", () -> { try (DatagramSocket s = new DatagramSocket()) { return s.isBound() + " " + s.getLocalAddress() + " " + (s.getLocalPort() > 0); } });
    io("udp-connect", () -> { try (DatagramSocket a = new DatagramSocket(0, InetAddress.getLoopbackAddress()); DatagramSocket b = new DatagramSocket(0, InetAddress.getLoopbackAddress())) { a.connect(InetAddress.getLoopbackAddress(), b.getLocalPort()); String r = a.isConnected() + " " + a.getInetAddress() + " " + (a.getPort() == b.getLocalPort()) + " " + a.getRemoteSocketAddress().toString().replaceAll(":\\d+", ":P"); a.send(new DatagramPacket("x".getBytes(), 1)); DatagramPacket p = new DatagramPacket(new byte[4], 4); b.setSoTimeout(2000); b.receive(p); a.disconnect(); return r + " " + new String(p.getData(), 0, p.getLength()) + " " + a.isConnected() + " " + a.getInetAddress() + " " + a.getPort(); } });
    io("udp-send-no-address", () -> { try (DatagramSocket s = new DatagramSocket()) { s.send(new DatagramPacket(new byte[1], 1)); return "no"; } });
    io("udp-send-connected-mismatch", () -> { try (DatagramSocket a = new DatagramSocket(0, InetAddress.getLoopbackAddress())) { a.connect(InetAddress.getLoopbackAddress(), 9); a.send(new DatagramPacket(new byte[1], 1, InetAddress.getLoopbackAddress(), 10)); return "no"; } });
    io("udp-truncate", () -> { try (DatagramSocket a = new DatagramSocket(0, InetAddress.getLoopbackAddress()); DatagramSocket b = new DatagramSocket(0, InetAddress.getLoopbackAddress())) { a.send(new DatagramPacket(new byte[100], 100, InetAddress.getLoopbackAddress(), b.getLocalPort())); DatagramPacket p = new DatagramPacket(new byte[10], 10); b.setSoTimeout(2000); b.receive(p); return p.getLength(); } });
    io("udp-offset", () -> { try (DatagramSocket a = new DatagramSocket(0, InetAddress.getLoopbackAddress()); DatagramSocket b = new DatagramSocket(0, InetAddress.getLoopbackAddress())) { byte[] buf = "..HELLO..".getBytes(); a.send(new DatagramPacket(buf, 2, 5, InetAddress.getLoopbackAddress(), b.getLocalPort())); byte[] rb = new byte[20]; DatagramPacket p = new DatagramPacket(rb, 4, 10); b.setSoTimeout(2000); b.receive(p); return new String(rb, 4, p.getLength()) + " " + p.getOffset() + " " + p.getLength(); } });
    io("udp-close-unblocks", () -> { DatagramSocket s = new DatagramSocket(0, InetAddress.getLoopbackAddress()); String[] r = new String[1]; Thread t = start(() -> { try { s.receive(new DatagramPacket(new byte[4], 4)); r[0] = "got"; } catch (IOException e) { r[0] = e.getClass().getSimpleName() + ":" + e.getMessage(); } }); Thread.sleep(100); s.close(); t.join(2000); return r[0]; });
    io("udp-options", () -> { try (DatagramSocket s = new DatagramSocket()) { s.setBroadcast(false); s.setReuseAddress(true); s.setReceiveBufferSize(65536); s.setSendBufferSize(65536); s.setTrafficClass(8); return s.getBroadcast() + " " + s.getReuseAddress() + " " + (s.getReceiveBufferSize() >= 65536) + " " + (s.getSendBufferSize() >= 65536) + " " + s.getTrafficClass(); } });
    io("udp-options-api", () -> { try (DatagramSocket s = new DatagramSocket()) { List<String> names = new ArrayList<>(); for (SocketOption<?> o : s.supportedOptions()) if (!o.name().startsWith("SO_INCOMING") && !o.name().equals("IP_DONTFRAGMENT")) names.add(o.name()); Collections.sort(names); s.setOption(StandardSocketOptions.SO_BROADCAST, false); return names + " " + s.getOption(StandardSocketOptions.SO_BROADCAST); } });
    io("udp-ctor-bad", () -> new DatagramSocket(70000));
    io("udp-bind-twice", () -> { try (DatagramSocket s = new DatagramSocket()) { s.bind(new InetSocketAddress(0)); return "no"; } });
    io("udp-closed-ops", () -> { DatagramSocket s = new DatagramSocket(); s.close(); String r = ""; try { s.send(new DatagramPacket(new byte[1], 1, InetAddress.getLoopbackAddress(), 9)); } catch (IOException e) { r += e.getMessage(); } try { s.setSoTimeout(1); } catch (IOException e) { r += "|" + e.getMessage(); } return r; });
    io("packet-api", () -> { DatagramPacket p = new DatagramPacket(new byte[10], 2, 5); String r = p.getOffset() + " " + p.getLength() + " " + p.getAddress() + " " + p.getPort(); p.setLength(3); p.setPort(80); p.setAddress(InetAddress.getByName("1.2.3.4")); p.setData(new byte[4]); return r + " | " + p.getOffset() + " " + p.getLength() + " " + p.getPort() + " " + p.getAddress() + " " + p.getSocketAddress() + " " + p.getData().length; });
    io("packet-bad", () -> new DatagramPacket(new byte[4], 5));
    io("packet-bad2", () -> new DatagramPacket(new byte[4], 3, 2));
    io("packet-bad3", () -> new DatagramPacket(new byte[4], 4, InetAddress.getLoopbackAddress(), 70000));
    io("packet-bad4", () -> { new DatagramPacket(new byte[4], 4).setLength(9); return "no"; });
    io("packet-bad5", () -> new DatagramPacket(new byte[4], 4, InetSocketAddress.createUnresolved("x", 1)));
    io("packet-bad6", () -> new DatagramPacket(new byte[4], 4, (SocketAddress) null));
    io("packet-sockaddr-unset", () -> new DatagramPacket(new byte[4], 4).getSocketAddress());
    io("packet-set-data-null", () -> { new DatagramPacket(new byte[4], 4).setData(null); return "no"; });
    io("multicast-props", () -> { try (MulticastSocket m = new MulticastSocket()) { m.setTimeToLive(5); return m.getTimeToLive() + " " + m.getLoopbackMode() + " " + m.isBound() + " " + (m.getLocalPort() > 0); } });
    io("multicast-ttl-bad", () -> { try (MulticastSocket m = new MulticastSocket()) { m.setTimeToLive(256); return "no"; } });
    io("multicast-join-bad", () -> { try (MulticastSocket m = new MulticastSocket()) { m.joinGroup(new InetSocketAddress(InetAddress.getByName("1.2.3.4"), 0), null); return "no"; } });
    io("multicast-loopback-mode", () -> { try (MulticastSocket m = new MulticastSocket()) { m.setLoopbackMode(true); boolean a = m.getLoopbackMode(); m.setLoopbackMode(false); return a + " " + m.getLoopbackMode(); } });
    io("network-interfaces", () -> { Enumeration<NetworkInterface> e = NetworkInterface.getNetworkInterfaces(); boolean lo = false; int count = 0; while (e.hasMoreElements()) { NetworkInterface n = e.nextElement(); count++; if (n.isLoopback()) { lo = true; boolean has = false; for (InetAddress a : Collections.list(n.getInetAddresses())) has |= a.isLoopbackAddress(); if (!has) return "loopback without address"; } } return (count > 0) + " " + lo; });
    io("network-interface-lookup", () -> { NetworkInterface lo = NetworkInterface.getByInetAddress(InetAddress.getLoopbackAddress()); return lo.isLoopback() + " " + lo.isUp() + " " + (lo.getIndex() > 0) + " " + (NetworkInterface.getByName(lo.getName()).equals(lo)) + " " + (NetworkInterface.getByIndex(lo.getIndex()).getName().equals(lo.getName())) + " " + lo.getName().equals(lo.getDisplayName()) + " " + lo.getParent() + " " + lo.getSubInterfaces().hasMoreElements() + " " + lo.getMTU() + " " + lo.isPointToPoint() + " " + lo.isVirtual() + " " + lo.toString().startsWith("name:"); });
    io("network-interface-missing", () -> NetworkInterface.getByName("no-such-iface0") + " " + NetworkInterface.getByIndex(99999));
    io("network-interface-bad", () -> NetworkInterface.getByName(null));
    io("network-interface-bad2", () -> NetworkInterface.getByIndex(-1));
    io("interface-addresses", () -> { NetworkInterface lo = NetworkInterface.getByInetAddress(InetAddress.getLoopbackAddress()); for (InterfaceAddress ia : lo.getInterfaceAddresses()) if (ia.getAddress() instanceof Inet4Address) return ia.getAddress() + " " + ia.getNetworkPrefixLength() + " " + ia.equals(ia) + " " + (ia.hashCode() == ia.hashCode()); return "none"; });
    io("local-host", () -> { InetAddress h = InetAddress.getLocalHost(); return (h.getHostName() != null) + " " + (h.getAddress().length > 0); });
    io("reachable", () -> InetAddress.getLoopbackAddress().isReachable(500) + " " + InetAddress.getByName("0.0.0.0").isReachable(1));
    io("reachable-bad", () -> InetAddress.getLoopbackAddress().isReachable(-1));
    io("canonical", () -> InetAddress.getByName("127.0.0.1").getCanonicalHostName().isEmpty());
    io("reverse-lookup-name", () -> { InetAddress a = InetAddress.getByAddress("given", new byte[] {127, 0, 0, 1}); return a.getHostName() + " " + a; });
    io("unix-address", () -> { UnixDomainSocketAddress a = UnixDomainSocketAddress.of("/tmp/x.sock"); return a + " " + a.getPath() + " " + a.equals(UnixDomainSocketAddress.of("/tmp/x.sock")) + " " + (a.hashCode() == UnixDomainSocketAddress.of("/tmp/x.sock").hashCode()) + " " + a.equals(UnixDomainSocketAddress.of("/tmp/y")); });
  }
}
