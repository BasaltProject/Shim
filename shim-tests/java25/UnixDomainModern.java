/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 * SPDX-License-Identifier: MIT
 * Minimum release: 21
 */
import java.net.*; import java.nio.channels.*; import java.nio.*; import java.nio.file.*; import java.util.*;
public class UnixDomainModern { static void t(String n, java.util.concurrent.Callable<Object> c){ try { Object r=c.call(); System.out.println(n+" = "+String.valueOf(r).replaceAll("/tmp/uds[0-9]+","TMP")); } catch(Throwable e){ System.out.println(n+" ! "+e.getClass().getName()+": "+String.valueOf(e.getMessage()).replaceAll("/tmp/uds[0-9]+","TMP")); } }
 public static void main(String[] a) throws Exception { Path dir=Files.createTempDirectory("uds"); Path p=dir.resolve("s.sock"); UnixDomainSocketAddress addr=UnixDomainSocketAddress.of(p);
  ServerSocketChannel s=ServerSocketChannel.open(StandardProtocolFamily.UNIX);
  t("server-unbound-local", ()->s.getLocalAddress()); t("server-str-unbound", ()->s.toString().replaceAll("/tmp/uds[0-9]+","TMP")); t("server-opts", ()->new TreeSet<>(s.supportedOptions().stream().map(Object::toString).toList()));
  t("server-socket", ()->s.socket()); t("server-bind-inet", ()->s.bind(new InetSocketAddress(0)));
  t("server-bind", ()->s.bind(addr)==s); t("server-local", ()->s.getLocalAddress().equals(addr)+" "+s.getLocalAddress().getClass().getSimpleName()); t("server-str", ()->s.toString().replaceAll("/tmp/uds[0-9]+","TMP"));
  t("bind-long", ()->{ ServerSocketChannel s4=ServerSocketChannel.open(StandardProtocolFamily.UNIX); return s4.bind(UnixDomainSocketAddress.of(dir.resolve("x".repeat(200)))); });
  SocketChannel c=SocketChannel.open(StandardProtocolFamily.UNIX);
  t("client-connect-inet", ()->c.connect(new InetSocketAddress("127.0.0.1",1))); t("client-connect-missing", ()->{ SocketChannel c2=SocketChannel.open(StandardProtocolFamily.UNIX); return c2.connect(UnixDomainSocketAddress.of(dir.resolve("nope"))); });
  t("client-connect", ()->c.connect(addr)); SocketChannel peer=s.accept();
  t("client-str-connected", ()->c.toString().replaceAll("/tmp/uds[0-9]+","TMP")); t("client-local2", ()->c.getLocalAddress()); t("client-remote2", ()->c.getRemoteAddress().equals(addr)); t("peer-local", ()->((UnixDomainSocketAddress)peer.getLocalAddress()).getPath().equals(p)); t("peer-remote", ()->peer.getRemoteAddress()); t("peer-str", ()->peer.toString().replaceAll("/tmp/uds[0-9]+","TMP"));
  t("io", ()->{ c.write(ByteBuffer.wrap("ping".getBytes())); ByteBuffer b=ByteBuffer.allocate(8); peer.read(b); peer.write(ByteBuffer.wrap("pong".getBytes())); c.read(b); b.flip(); return new String(b.array(),0,b.limit()); });
  t("opt-rcvbuf", ()->c.getOption(StandardSocketOptions.SO_RCVBUF)>0); t("opt-tcp", ()->c.getOption(StandardSocketOptions.TCP_NODELAY)); t("opt-linger", ()->c.getOption(StandardSocketOptions.SO_LINGER));
  t("shutdown", ()->{ c.shutdownOutput(); ByteBuffer b=ByteBuffer.allocate(4); return peer.read(b); });
  t("nonblocking-accept", ()->{ s.configureBlocking(false); return s.accept(); });
  t("selector", ()->{ Selector sel=Selector.open(); s.register(sel,SelectionKey.OP_ACCEPT); SocketChannel c3=SocketChannel.open(StandardProtocolFamily.UNIX); c3.connect(addr); int n=sel.select(2000); return n; });
  t("stream-class", ()->c.getClass().getSimpleName().isEmpty());
  t("family-null", ()->SocketChannel.open((ProtocolFamily)null)); t("family-inet", ()->SocketChannel.open(StandardProtocolFamily.INET).getLocalAddress()); t("server-family-inet6", ()->ServerSocketChannel.open(StandardProtocolFamily.INET6).getLocalAddress());
  t("dgram-unix", ()->DatagramChannel.open(StandardProtocolFamily.UNIX));
  t("provider", ()->java.nio.channels.spi.SelectorProvider.provider().openSocketChannel(StandardProtocolFamily.UNIX).isOpen());
  t("path-too-long", ()->UnixDomainSocketAddress.of("x".repeat(300)));
  t("addr-eq", ()->UnixDomainSocketAddress.of("/a/b").equals(UnixDomainSocketAddress.of(Path.of("/a/b")))+" "+UnixDomainSocketAddress.of("/a/b"));
 } }
