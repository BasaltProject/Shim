/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 * SPDX-License-Identifier: MIT
 * Minimum release: 21
 */

import java.io.*;
import java.net.*;
import java.nio.*;
import java.nio.channels.*;
import java.util.*;
import java.util.function.Supplier;

public class NioNetworkChannelsModern {
  static void probe(String name, Supplier<Object> body) {
    String out;
    try {
      out = String.valueOf(body.get());
    } catch (Throwable t) {
      out = "!" + t.getClass().getName() + ":" + t.getMessage();
    }
    System.out.println(name + "=" + out.replaceAll("(?<=[:=])\\d{4,5}\\b", "P").replaceAll("\\b(?:localport|port)=\\d{4,5}", "port=P").replaceAll("Nombre o servicio desconocido|Name or service not known", "NSK").replaceAll("Conexión rehusada|Connection refused", "CR"));
  }

  interface IoBody { Object run() throws Exception; }

  static void io(String name, IoBody body) {
    probe(name, () -> {
      try { return body.run(); } catch (Exception e) { return "!" + e.getClass().getName() + ":" + e.getMessage(); }
    });
  }

  static Thread start(Runnable r) { Thread t = new Thread(r); t.setDaemon(true); t.start(); return t; }

  static InetSocketAddress lo(int port) { return new InetSocketAddress(InetAddress.getLoopbackAddress(), port); }

  public static void main(String[] args) throws Exception {
    io("provider", () -> java.nio.channels.spi.SelectorProvider.provider() == java.nio.channels.spi.SelectorProvider.provider());
    io("server-basic", () -> { try (ServerSocketChannel ssc = ServerSocketChannel.open()) { String a = ssc.isOpen() + " " + ssc.isBlocking() + " " + ssc.getLocalAddress() + " " + ssc.validOps() + " " + ssc.isRegistered() + " " + ssc.toString().replaceAll("[0-9.:/]+$", ""); ssc.bind(lo(0)); String b = ssc.getLocalAddress().toString().replaceAll(":\\d+", ":P"); return a + " | " + b + " " + ssc.socket().isBound() + " " + (ssc.socket().getLocalPort() > 0) + " " + ssc.socket().getChannel().equals(ssc); } });
    io("server-bind-twice", () -> { try (ServerSocketChannel ssc = ServerSocketChannel.open()) { ssc.bind(lo(0)); ssc.bind(lo(0)); return "no"; } });
    io("server-accept-unbound", () -> { try (ServerSocketChannel ssc = ServerSocketChannel.open()) { ssc.accept(); return "no"; } });
    io("server-closed", () -> { ServerSocketChannel ssc = ServerSocketChannel.open(); ssc.close(); String r = ssc.isOpen() + ""; try { ssc.bind(null); } catch (ClosedChannelException e) { r += " bind"; } try { ssc.accept(); } catch (ClosedChannelException e) { r += " accept"; } try { ssc.getLocalAddress(); } catch (ClosedChannelException e) { r += " addr"; } return r; });
    io("server-nonblocking-accept", () -> { try (ServerSocketChannel ssc = ServerSocketChannel.open()) { ssc.bind(lo(0)); ssc.configureBlocking(false); return ssc.accept() + " " + ssc.isBlocking(); } });
    io("server-options", () -> { try (ServerSocketChannel ssc = ServerSocketChannel.open()) { ssc.setOption(StandardSocketOptions.SO_REUSEADDR, true); ssc.setOption(StandardSocketOptions.SO_RCVBUF, 65536); List<String> n = new ArrayList<>(); for (SocketOption<?> o : ssc.supportedOptions()) if (o.name().equals("SO_REUSEADDR") || o.name().equals("SO_RCVBUF") || o.name().equals("SO_REUSEPORT")) n.add(o.name()); Collections.sort(n); return ssc.getOption(StandardSocketOptions.SO_REUSEADDR) + " " + (ssc.getOption(StandardSocketOptions.SO_RCVBUF) >= 65536) + " " + n; } });
    io("server-option-unsupported", () -> { try (ServerSocketChannel ssc = ServerSocketChannel.open()) { ssc.setOption(StandardSocketOptions.TCP_NODELAY, true); return "no"; } });
    io("socket-basic", () -> { try (SocketChannel sc = SocketChannel.open()) { return sc.isOpen() + " " + sc.isConnected() + " " + sc.isConnectionPending() + " " + sc.isBlocking() + " " + sc.validOps() + " " + sc.getRemoteAddress() + " " + sc.getLocalAddress() + " " + sc.socket().isConnected() + " " + sc.socket().isClosed() + " " + sc.toString().replaceAll("@.*", ""); } });
    io("socket-unconnected-ops", () -> { try (SocketChannel sc = SocketChannel.open()) { String r = ""; try { sc.read(ByteBuffer.allocate(1)); } catch (NotYetConnectedException e) { r += "R"; } try { sc.write(ByteBuffer.allocate(1)); } catch (NotYetConnectedException e) { r += "W"; } try { sc.finishConnect(); } catch (NoConnectionPendingException e) { r += "F"; } try { sc.shutdownInput(); } catch (NotYetConnectedException e) { r += "I"; } try { sc.shutdownOutput(); } catch (NotYetConnectedException e) { r += "O"; } return r; } });
    io("socket-connect-bad-address", () -> { try (SocketChannel sc = SocketChannel.open()) { String r = ""; try { sc.connect(InetSocketAddress.createUnresolved("x", 1)); } catch (UnresolvedAddressException e) { r += "U"; } try { sc.connect(UnixDomainSocketAddress.of("/tmp/x")); } catch (UnsupportedAddressTypeException e) { r += "T"; } try { sc.connect(null); } catch (NullPointerException e) { r += "N"; } return r; } });
    io("echo-blocking", () -> {
      try (ServerSocketChannel ssc = ServerSocketChannel.open()) {
        ssc.bind(lo(0));
        int port = ((InetSocketAddress) ssc.getLocalAddress()).getPort();
        Thread t = start(() -> { try (SocketChannel c = ssc.accept()) { ByteBuffer buf = ByteBuffer.allocate(64); while (c.read(buf) > 0) { buf.flip(); while (buf.hasRemaining()) c.write(buf); buf.clear(); } } catch (IOException e) { } });
        try (SocketChannel sc = SocketChannel.open(lo(port))) {
          String a = sc.isConnected() + " " + sc.isConnectionPending() + " " + sc.finishConnect() + " " + sc.getRemoteAddress().toString().replaceAll(":\\d+", ":P") + " " + sc.socket().isConnected() + " " + (sc.socket().getPort() == port) + " " + sc.socket().getChannel().equals(sc);
          ByteBuffer out = ByteBuffer.wrap("hello channel".getBytes());
          int w = sc.write(out);
          ByteBuffer in = ByteBuffer.allocate(13);
          while (in.hasRemaining()) if (sc.read(in) < 0) break;
          sc.shutdownOutput();
          int eof = sc.read(ByteBuffer.allocate(1));
          t.join(2000);
          return a + " | " + w + " " + new String(in.array()) + " " + eof;
        }
      }
    });
    io("scatter-gather", () -> {
      try (ServerSocketChannel ssc = ServerSocketChannel.open()) {
        ssc.bind(lo(0));
        Thread t = start(() -> { try (SocketChannel c = ssc.accept()) { ByteBuffer a = ByteBuffer.allocate(3), b = ByteBuffer.allocate(5); long n = c.read(new ByteBuffer[] {a, b}); c.write(ByteBuffer.wrap(("n=" + n + " " + new String(a.array()) + "|" + new String(b.array(), 0, b.position())).getBytes())); } catch (IOException e) { } });
        try (SocketChannel sc = SocketChannel.open(ssc.getLocalAddress())) {
          long w = sc.write(new ByteBuffer[] {ByteBuffer.wrap("abc".getBytes()), ByteBuffer.wrap("defgh".getBytes())});
          sc.shutdownOutput();
          ByteBuffer in = ByteBuffer.allocate(64);
          while (sc.read(in) > 0) { }
          t.join(2000);
          in.flip();
          return w + " " + new String(in.array(), 0, in.limit());
        }
      }
    });
    io("nonblocking-connect", () -> {
      try (ServerSocketChannel ssc = ServerSocketChannel.open()) {
        ssc.bind(lo(0));
        try (SocketChannel sc = SocketChannel.open()) {
          sc.configureBlocking(false);
          boolean immediate = sc.connect(ssc.getLocalAddress());
          String r = "";
          try { sc.connect(ssc.getLocalAddress()); } catch (ConnectionPendingException | AlreadyConnectedException e) { r = e.getClass().getSimpleName(); }
          int tries = 0;
          while (!sc.finishConnect() && tries++ < 1000) Thread.sleep(2);
          return (immediate || sc.isConnected()) + " " + sc.isConnected() + " " + sc.isConnectionPending() + " " + sc.finishConnect() + " " + r;
        }
      }
    });
    io("nonblocking-read-empty", () -> { try (ServerSocketChannel ssc = ServerSocketChannel.open()) { ssc.bind(lo(0)); try (SocketChannel sc = SocketChannel.open(ssc.getLocalAddress()); SocketChannel peer = ssc.accept()) { sc.configureBlocking(false); int n = sc.read(ByteBuffer.allocate(8)); peer.write(ByteBuffer.wrap("z".getBytes())); Thread.sleep(50); ByteBuffer b = ByteBuffer.allocate(8); int m = sc.read(b); peer.close(); Thread.sleep(50); int e = sc.read(ByteBuffer.allocate(8)); return n + " " + m + " " + b.position() + " " + e; } } });
    io("selector-basic", () -> { try (Selector sel = Selector.open()) { return sel.isOpen() + " " + sel.keys().size() + " " + sel.selectedKeys().size() + " " + sel.selectNow() + " " + (sel.provider() == java.nio.channels.spi.SelectorProvider.provider()); } });
    io("selector-closed", () -> { Selector sel = Selector.open(); sel.close(); String r = sel.isOpen() + ""; try { sel.keys(); } catch (ClosedSelectorException e) { r += " keys"; } try { sel.selectNow(); } catch (ClosedSelectorException e) { r += " select"; } sel.close(); return r; });
    io("selector-wakeup", () -> { try (Selector sel = Selector.open()) { Thread t = start(() -> { try { Thread.sleep(100); } catch (InterruptedException e) { } sel.wakeup(); }); long s = System.nanoTime(); int n = sel.select(); t.join(); sel.wakeup(); int m = sel.select(); return n + " " + m + " " + ((System.nanoTime() - s) / 1000000 >= 90); } });
    io("selector-timeout", () -> { try (Selector sel = Selector.open()) { long s = System.nanoTime(); int n = sel.select(100); long ms = (System.nanoTime() - s) / 1000000; return n + " " + (ms >= 90 && ms < 1000); } });
    io("selector-bad-timeout", () -> { try (Selector sel = Selector.open()) { return sel.select(-1); } });
    io("register-blocking", () -> { try (Selector sel = Selector.open(); ServerSocketChannel ssc = ServerSocketChannel.open()) { ssc.register(sel, SelectionKey.OP_ACCEPT); return "no"; } });
    io("register-bad-ops", () -> { try (Selector sel = Selector.open(); ServerSocketChannel ssc = ServerSocketChannel.open()) { ssc.configureBlocking(false); ssc.register(sel, SelectionKey.OP_READ); return "no"; } });
    io("register-closed", () -> { try (Selector sel = Selector.open()) { ServerSocketChannel ssc = ServerSocketChannel.open(); ssc.configureBlocking(false); ssc.close(); ssc.register(sel, SelectionKey.OP_ACCEPT); return "no"; } });
    io("key-basics", () -> { try (Selector sel = Selector.open(); ServerSocketChannel ssc = ServerSocketChannel.open()) { ssc.configureBlocking(false); ssc.bind(lo(0)); SelectionKey k = ssc.register(sel, SelectionKey.OP_ACCEPT, "att"); String a = k.isValid() + " " + k.interestOps() + " " + k.attachment() + " " + (k.channel() == ssc) + " " + (k.selector() == sel) + " " + (ssc.keyFor(sel) == k) + " " + ssc.isRegistered() + " " + sel.keys().size(); SelectionKey k2 = ssc.register(sel, 0, "att2"); String b = (k == k2) + " " + k.interestOps() + " " + k.attachment() + " " + k.attach("x") + " " + k.attachment(); k.cancel(); String c = k.isValid() + " " + sel.keys().size(); sel.selectNow(); String d = sel.keys().size() + " " + ssc.isRegistered() + " " + (ssc.keyFor(sel) == null); try { k.interestOps(1); } catch (CancelledKeyException e) { d += " cancelled"; } return a + " | " + b + " | " + c + " | " + d; } });
    io("key-interest-ops", () -> { try (Selector sel = Selector.open(); SocketChannel sc = SocketChannel.open()) { sc.configureBlocking(false); SelectionKey k = sc.register(sel, SelectionKey.OP_CONNECT); k.interestOps(SelectionKey.OP_READ | SelectionKey.OP_WRITE); int before = k.interestOpsOr(SelectionKey.OP_CONNECT); int mid = k.interestOpsAnd(~SelectionKey.OP_WRITE); String bad = ""; try { k.interestOps(SelectionKey.OP_ACCEPT); } catch (IllegalArgumentException e) { bad = "IAE"; } return before + " " + mid + " " + k.interestOps() + " " + bad + " " + k.readyOps(); } });
    io("accept-select", () -> {
      try (Selector sel = Selector.open(); ServerSocketChannel ssc = ServerSocketChannel.open()) {
        ssc.configureBlocking(false);
        ssc.bind(lo(0));
        SelectionKey k = ssc.register(sel, SelectionKey.OP_ACCEPT);
        int none = sel.selectNow();
        try (SocketChannel client = SocketChannel.open(ssc.getLocalAddress())) {
          int n = sel.select(2000);
          String r = none + " " + n + " " + sel.selectedKeys().size() + " " + k.isAcceptable() + " " + k.isReadable() + " " + (k.readyOps() == SelectionKey.OP_ACCEPT);
          SocketChannel accepted = ssc.accept();
          r += " " + (accepted != null) + " " + (accepted == null ? "" : accepted.isBlocking() + " " + accepted.isConnected());
          sel.selectedKeys().clear();
          int again = sel.selectNow();
          if (accepted != null) accepted.close();
          return r + " " + again;
        }
      }
    });
    io("read-write-select", () -> {
      try (Selector sel = Selector.open(); ServerSocketChannel ssc = ServerSocketChannel.open()) {
        ssc.bind(lo(0));
        try (SocketChannel client = SocketChannel.open(ssc.getLocalAddress()); SocketChannel server = ssc.accept()) {
          client.configureBlocking(false);
          server.configureBlocking(false);
          SelectionKey ck = client.register(sel, SelectionKey.OP_READ | SelectionKey.OP_WRITE);
          int n = sel.select(1000);
          String r = n + " " + ck.isWritable() + " " + ck.isReadable();
          sel.selectedKeys().clear();
          ck.interestOps(SelectionKey.OP_READ);
          server.write(ByteBuffer.wrap("data".getBytes()));
          int m = sel.select(2000);
          ByteBuffer b = ByteBuffer.allocate(16);
          client.read(b);
          sel.selectedKeys().clear();
          return r + " | " + m + " " + ck.isReadable() + " " + new String(b.array(), 0, b.position());
        }
      }
    });
    io("connect-select", () -> {
      try (Selector sel = Selector.open(); ServerSocketChannel ssc = ServerSocketChannel.open()) {
        ssc.bind(lo(0));
        try (SocketChannel sc = SocketChannel.open()) {
          sc.configureBlocking(false);
          boolean done = sc.connect(ssc.getLocalAddress());
          SelectionKey k = sc.register(sel, SelectionKey.OP_CONNECT);
          int n = sel.select(2000);
          boolean connectable = k.isConnectable();
          boolean fin = sc.finishConnect();
          return (done || n == 1) + " " + (done || connectable) + " " + fin + " " + sc.isConnected();
        }
      }
    });
    io("select-action", () -> { try (Selector sel = Selector.open(); ServerSocketChannel ssc = ServerSocketChannel.open()) { ssc.configureBlocking(false); ssc.bind(lo(0)); ssc.register(sel, SelectionKey.OP_ACCEPT); try (SocketChannel c = SocketChannel.open(ssc.getLocalAddress())) { int[] seen = {0}; int n = sel.select(k -> seen[0]++, 2000); return n + " " + seen[0] + " " + sel.selectedKeys().size(); } } });
    io("close-selector-cancels", () -> { Selector sel = Selector.open(); ServerSocketChannel ssc = ServerSocketChannel.open(); ssc.configureBlocking(false); SelectionKey k = ssc.register(sel, SelectionKey.OP_ACCEPT); sel.close(); String r = k.isValid() + " " + ssc.isRegistered(); ssc.close(); return r; });
    io("close-channel-cancels", () -> { try (Selector sel = Selector.open()) { ServerSocketChannel ssc = ServerSocketChannel.open(); ssc.configureBlocking(false); SelectionKey k = ssc.register(sel, SelectionKey.OP_ACCEPT); ssc.close(); String r = k.isValid() + " " + sel.keys().size(); sel.selectNow(); return r + " " + sel.keys().size(); } });
    io("blocking-after-register", () -> { try (Selector sel = Selector.open(); ServerSocketChannel ssc = ServerSocketChannel.open()) { ssc.configureBlocking(false); ssc.register(sel, SelectionKey.OP_ACCEPT); ssc.configureBlocking(true); return "no"; } });
    io("socket-adaptor", () -> { try (ServerSocketChannel ssc = ServerSocketChannel.open()) { ssc.bind(lo(0)); try (SocketChannel sc = SocketChannel.open(ssc.getLocalAddress()); SocketChannel peer = ssc.accept()) { Socket s = sc.socket(); s.setTcpNoDelay(true); s.setKeepAlive(true); s.setSoLinger(true, 3); peer.socket().getOutputStream().write(new byte[] {7, 8, 9}); byte[] b = new byte[3]; int n = s.getInputStream().read(b); return s.getTcpNoDelay() + " " + s.getKeepAlive() + " " + s.getSoLinger() + " " + n + Arrays.toString(b) + " " + s.isBound() + " " + s.getInetAddress().isLoopbackAddress() + " " + (s.getLocalPort() > 0); } } });
    io("socket-adaptor-nonblocking", () -> { try (ServerSocketChannel ssc = ServerSocketChannel.open()) { ssc.bind(lo(0)); try (SocketChannel sc = SocketChannel.open(ssc.getLocalAddress()); SocketChannel peer = ssc.accept()) { sc.configureBlocking(false); sc.socket().getInputStream().read(); return "no"; } } });
    io("server-adaptor", () -> { try (ServerSocketChannel ssc = ServerSocketChannel.open()) { ServerSocket ss = ssc.socket(); String a = ss.isBound() + " " + ss.getLocalPort() + " " + ss.getInetAddress() + " " + ss; ss.bind(lo(0)); Thread t = start(() -> { try (Socket c = new Socket(InetAddress.getLoopbackAddress(), ss.getLocalPort())) { c.getOutputStream().write(5); } catch (IOException e) { } }); try (Socket accepted = ss.accept()) { int v = accepted.getInputStream().read(); t.join(2000); return a + " | " + ss.isBound() + " " + v + " " + (accepted.getChannel() != null) + " " + ss.getChannel().equals(ssc); } } });
    io("udp-channel-basic", () -> { try (DatagramChannel dc = DatagramChannel.open()) { return dc.isOpen() + " " + dc.isConnected() + " " + dc.isBlocking() + " " + dc.validOps() + " " + dc.getLocalAddress() + " " + dc.getRemoteAddress() + " " + dc.socket().isBound(); } });
    io("udp-channel-roundtrip", () -> {
      try (DatagramChannel a = DatagramChannel.open().bind(lo(0)); DatagramChannel b = DatagramChannel.open().bind(lo(0))) {
        int sent = a.send(ByteBuffer.wrap("datagram".getBytes()), b.getLocalAddress());
        ByteBuffer buf = ByteBuffer.allocate(32);
        SocketAddress from = b.receive(buf);
        String r = sent + " " + buf.position() + " " + new String(buf.array(), 0, buf.position()) + " " + from.toString().replaceAll(":\\d+", ":P") + " " + from.equals(a.getLocalAddress());
        b.send(ByteBuffer.wrap("back".getBytes()), from);
        ByteBuffer buf2 = ByteBuffer.allocate(4);
        a.receive(buf2);
        return r + " | " + new String(buf2.array());
      }
    });
    io("udp-channel-nonblocking", () -> { try (DatagramChannel a = DatagramChannel.open().bind(lo(0))) { a.configureBlocking(false); ByteBuffer buf = ByteBuffer.allocate(8); SocketAddress none = a.receive(buf); return none + " " + buf.position(); } });
    io("udp-channel-connected", () -> {
      try (DatagramChannel a = DatagramChannel.open().bind(lo(0)); DatagramChannel b = DatagramChannel.open().bind(lo(0))) {
        a.connect(b.getLocalAddress());
        b.connect(a.getLocalAddress());
        String r = a.isConnected() + " " + a.getRemoteAddress().equals(b.getLocalAddress());
        int w = a.write(ByteBuffer.wrap("conn".getBytes()));
        ByteBuffer buf = ByteBuffer.allocate(8);
        int n = b.read(buf);
        a.disconnect();
        String after = a.isConnected() + " " + a.getRemoteAddress();
        String err = "";
        try { a.write(ByteBuffer.wrap(new byte[1])); } catch (NotYetConnectedException e) { err = "NYC"; }
        try { a.connect(b.getLocalAddress()); a.connect(b.getLocalAddress()); } catch (AlreadyConnectedException e) { err += " ACE"; }
        return r + " " + w + " " + n + " " + new String(buf.array(), 0, n) + " | " + after + " " + err;
      }
    });
    io("udp-channel-select", () -> { try (Selector sel = Selector.open(); DatagramChannel a = DatagramChannel.open().bind(lo(0)); DatagramChannel b = DatagramChannel.open().bind(lo(0))) { a.configureBlocking(false); SelectionKey k = a.register(sel, SelectionKey.OP_READ); int none = sel.selectNow(); b.send(ByteBuffer.wrap("x".getBytes()), a.getLocalAddress()); int n = sel.select(2000); return none + " " + n + " " + k.isReadable(); } });
    io("udp-channel-options", () -> { try (DatagramChannel dc = DatagramChannel.open()) { dc.setOption(StandardSocketOptions.SO_BROADCAST, true); dc.setOption(StandardSocketOptions.SO_REUSEADDR, true); dc.setOption(StandardSocketOptions.IP_MULTICAST_TTL, 4); return dc.getOption(StandardSocketOptions.SO_BROADCAST) + " " + dc.getOption(StandardSocketOptions.SO_REUSEADDR) + " " + dc.getOption(StandardSocketOptions.IP_MULTICAST_TTL); } });
    io("udp-channel-bind-twice", () -> { try (DatagramChannel dc = DatagramChannel.open()) { dc.bind(lo(0)); dc.bind(lo(0)); return "no"; } });
    io("udp-channel-adaptor", () -> { try (DatagramChannel dc = DatagramChannel.open().bind(lo(0)); DatagramSocket other = new DatagramSocket(0, InetAddress.getLoopbackAddress())) { DatagramSocket s = dc.socket(); other.send(new DatagramPacket("via".getBytes(), 3, InetAddress.getLoopbackAddress(), s.getLocalPort())); DatagramPacket p = new DatagramPacket(new byte[10], 10); s.receive(p); return s.isBound() + " " + new String(p.getData(), 0, p.getLength()) + " " + (p.getPort() == other.getLocalPort()) + " " + (s.getChannel() == dc); } });
    io("udp-channel-family", () -> { try (DatagramChannel v4 = DatagramChannel.open(StandardProtocolFamily.INET)) { return v4.isOpen(); } });
    io("udp-channel-family-bad", () -> DatagramChannel.open(StandardProtocolFamily.UNIX));
    io("udp-channel-family-null", () -> DatagramChannel.open(null));
    io("pipe-basic", () -> { Pipe p = Pipe.open(); Pipe.SinkChannel sink = p.sink(); Pipe.SourceChannel source = p.source(); String a = sink.validOps() + " " + source.validOps() + " " + sink.isBlocking() + " " + source.isOpen(); sink.write(ByteBuffer.wrap("pipe data".getBytes())); ByteBuffer b = ByteBuffer.allocate(20); int n = source.read(b); sink.close(); int eof = source.read(ByteBuffer.allocate(4)); source.close(); return a + " | " + n + " " + new String(b.array(), 0, n) + " " + eof + " " + source.isOpen(); });
    io("pipe-nonblocking", () -> { Pipe p = Pipe.open(); p.source().configureBlocking(false); int none = p.source().read(ByteBuffer.allocate(4)); p.sink().write(ByteBuffer.wrap(new byte[] {1, 2})); Thread.sleep(20); ByteBuffer b = ByteBuffer.allocate(8); int n = p.source().read(b); p.sink().close(); p.source().close(); return none + " " + n; });
    io("pipe-select", () -> { Pipe p = Pipe.open(); try (Selector sel = Selector.open()) { p.source().configureBlocking(false); SelectionKey k = p.source().register(sel, SelectionKey.OP_READ); int none = sel.selectNow(); p.sink().write(ByteBuffer.wrap(new byte[] {9})); int n = sel.select(2000); p.sink().close(); p.source().close(); return none + " " + n + " " + k.isReadable(); } });
    io("pipe-scatter-gather", () -> { Pipe p = Pipe.open(); long w = p.sink().write(new ByteBuffer[] {ByteBuffer.wrap("ab".getBytes()), ByteBuffer.wrap("cd".getBytes())}); ByteBuffer x = ByteBuffer.allocate(3), y = ByteBuffer.allocate(3); long r = p.source().read(new ByteBuffer[] {x, y}); p.sink().close(); p.source().close(); return w + " " + r + " " + x.position() + " " + y.position(); });
    io("many-keys", () -> {
      try (Selector sel = Selector.open(); ServerSocketChannel ssc = ServerSocketChannel.open()) {
        ssc.configureBlocking(false);
        ssc.bind(lo(0));
        ssc.register(sel, SelectionKey.OP_ACCEPT);
        List<SocketChannel> clients = new ArrayList<>();
        for (int i = 0; i < 10; i++) { SocketChannel c = SocketChannel.open(ssc.getLocalAddress()); clients.add(c); }
        int accepted = 0;
        for (int round = 0; round < 20 && accepted < 10; round++) {
          sel.select(200);
          for (Iterator<SelectionKey> it = sel.selectedKeys().iterator(); it.hasNext();) {
            SelectionKey k = it.next(); it.remove();
            if (k.isAcceptable()) { SocketChannel c; while ((c = ssc.accept()) != null) { accepted++; c.close(); } }
          }
        }
        for (SocketChannel c : clients) c.close();
        return accepted;
      }
    });
    io("echo-server-selector", () -> {
      try (Selector sel = Selector.open(); ServerSocketChannel ssc = ServerSocketChannel.open()) {
        ssc.configureBlocking(false);
        ssc.bind(lo(0));
        ssc.register(sel, SelectionKey.OP_ACCEPT);
        int port = ((InetSocketAddress) ssc.getLocalAddress()).getPort();
        boolean[] stop = {false};
        Thread loop = start(() -> {
          try {
            while (!stop[0]) {
              if (sel.select(100) == 0) continue;
              for (Iterator<SelectionKey> it = sel.selectedKeys().iterator(); it.hasNext();) {
                SelectionKey k = it.next(); it.remove();
                if (k.isAcceptable()) { SocketChannel c = ssc.accept(); if (c != null) { c.configureBlocking(false); c.register(sel, SelectionKey.OP_READ); } }
                else if (k.isReadable()) { SocketChannel c = (SocketChannel) k.channel(); ByteBuffer b = ByteBuffer.allocate(64); int n = c.read(b); if (n < 0) { k.cancel(); c.close(); } else { b.flip(); c.write(b); } }
              }
            }
          } catch (IOException e) { }
        });
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < 3; i++) {
          try (Socket s = new Socket(InetAddress.getLoopbackAddress(), port)) {
            s.getOutputStream().write(("msg" + i).getBytes());
            byte[] buf = new byte[4];
            int got = 0;
            while (got < 4) got += s.getInputStream().read(buf, got, 4 - got);
            sb.append(new String(buf)).append(' ');
          }
        }
        stop[0] = true;
        sel.wakeup();
        loop.join(2000);
        return sb;
      }
    });
    io("channel-factory-methods", () -> { try (SocketChannel a = java.nio.channels.spi.SelectorProvider.provider().openSocketChannel(); ServerSocketChannel b = java.nio.channels.spi.SelectorProvider.provider().openServerSocketChannel(); DatagramChannel c = java.nio.channels.spi.SelectorProvider.provider().openDatagramChannel(); Selector d = java.nio.channels.spi.SelectorProvider.provider().openSelector()) { return a.isOpen() + " " + b.isOpen() + " " + c.isOpen() + " " + d.isOpen(); } });
    io("close-twice", () -> { SocketChannel sc = SocketChannel.open(); sc.close(); sc.close(); ServerSocketChannel ssc = ServerSocketChannel.open(); ssc.close(); ssc.close(); DatagramChannel dc = DatagramChannel.open(); dc.close(); dc.close(); return sc.isOpen() + " " + ssc.isOpen() + " " + dc.isOpen(); });
    io("read-closed", () -> { SocketChannel sc = SocketChannel.open(); sc.close(); String r = ""; try { sc.read(ByteBuffer.allocate(1)); } catch (ClosedChannelException e) { r += "R"; } try { sc.write(ByteBuffer.allocate(1)); } catch (ClosedChannelException e) { r += "W"; } try { sc.connect(lo(1)); } catch (ClosedChannelException e) { r += "C"; } try { sc.configureBlocking(false); } catch (ClosedChannelException e) { r += "B"; } try { sc.getLocalAddress(); } catch (ClosedChannelException e) { r += "L"; } return r; });
    io("connect-refused", () -> { int port; try (ServerSocketChannel t = ServerSocketChannel.open()) { t.bind(lo(0)); port = ((InetSocketAddress) t.getLocalAddress()).getPort(); } try (SocketChannel sc = SocketChannel.open()) { sc.connect(lo(port)); return "no"; } });
    io("write-shutdown", () -> { try (ServerSocketChannel ssc = ServerSocketChannel.open()) { ssc.bind(lo(0)); try (SocketChannel sc = SocketChannel.open(ssc.getLocalAddress()); SocketChannel peer = ssc.accept()) { sc.shutdownOutput(); sc.shutdownOutput(); try { sc.write(ByteBuffer.wrap(new byte[1])); return "no"; } catch (ClosedChannelException e) { return "CCE " + peer.read(ByteBuffer.allocate(1)); } } } });
    io("read-shutdown", () -> { try (ServerSocketChannel ssc = ServerSocketChannel.open()) { ssc.bind(lo(0)); try (SocketChannel sc = SocketChannel.open(ssc.getLocalAddress()); SocketChannel peer = ssc.accept()) { sc.shutdownInput(); return sc.read(ByteBuffer.allocate(1)) + ""; } } });
    io("large-transfer", () -> { try (ServerSocketChannel ssc = ServerSocketChannel.open()) { ssc.bind(lo(0)); long[] sum = {0}; Thread t = start(() -> { try (SocketChannel c = ssc.accept()) { ByteBuffer b = ByteBuffer.allocate(65536); while (c.read(b) >= 0) { byte[] arr = b.array(); for (int i = 0; i < b.position(); i++) sum[0] += arr[i]; b.clear(); } } catch (IOException e) { } }); try (SocketChannel sc = SocketChannel.open(ssc.getLocalAddress())) { ByteBuffer chunk = ByteBuffer.allocate(10000); for (int i = 0; i < chunk.capacity(); i++) chunk.put((byte) 2); for (int i = 0; i < 20; i++) { chunk.flip(); while (chunk.hasRemaining()) sc.write(chunk); } } t.join(10000); return sum[0]; } });
    io("direct-buffers", () -> { try (ServerSocketChannel ssc = ServerSocketChannel.open()) { ssc.bind(lo(0)); try (SocketChannel sc = SocketChannel.open(ssc.getLocalAddress()); SocketChannel peer = ssc.accept()) { ByteBuffer out = ByteBuffer.allocateDirect(8); out.put("direct!!".getBytes()).flip(); sc.write(out); ByteBuffer in = ByteBuffer.allocateDirect(8); while (in.hasRemaining()) peer.read(in); in.flip(); byte[] arr = new byte[8]; in.get(arr); return new String(arr); } } });
  }
}
