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
import java.nio.file.*;
import java.util.*;
import java.util.concurrent.*;
import java.util.function.Supplier;

public class AsyncChannelsModern {
  static void probe(String name, Supplier<Object> body) {
    String out;
    try {
      out = String.valueOf(body.get());
    } catch (Throwable t) {
      out = "!" + t.getClass().getName() + ":" + t.getMessage();
    }
    System.out.println(name + "=" + out.replaceAll("(?<=[:=])\\d{4,5}\\b", "P").replaceAll("Connection refused|Conexión rehusada", "CR"));
  }

  interface IoBody { Object run() throws Exception; }

  static void io(String name, IoBody body) {
    probe(name, () -> {
      try { return body.run(); } catch (Exception e) { return "!" + e.getClass().getName() + ":" + e.getMessage(); }
    });
  }

  static InetSocketAddress lo(int port) { return new InetSocketAddress(InetAddress.getLoopbackAddress(), port); }

  public static void main(String[] args) throws Exception {
    io("server-basic", () -> { try (AsynchronousServerSocketChannel s = AsynchronousServerSocketChannel.open()) { String a = s.isOpen() + " " + s.getLocalAddress(); s.bind(lo(0)); return a + " " + (((InetSocketAddress) s.getLocalAddress()).getPort() > 0) + " " + (s.provider() != null); } });
    io("server-accept-unbound", () -> { try (AsynchronousServerSocketChannel s = AsynchronousServerSocketChannel.open()) { s.accept(); return "no"; } });
    io("server-bind-twice", () -> { try (AsynchronousServerSocketChannel s = AsynchronousServerSocketChannel.open()) { s.bind(lo(0)); s.bind(lo(0)); return "no"; } });
    io("server-options", () -> { try (AsynchronousServerSocketChannel s = AsynchronousServerSocketChannel.open()) { s.setOption(StandardSocketOptions.SO_REUSEADDR, true); return s.getOption(StandardSocketOptions.SO_REUSEADDR); } });
    io("socket-basic", () -> { try (AsynchronousSocketChannel c = AsynchronousSocketChannel.open()) { return c.isOpen() + " " + c.getRemoteAddress() + " " + c.getLocalAddress(); } });
    io("echo-future", () -> {
      try (AsynchronousServerSocketChannel server = AsynchronousServerSocketChannel.open().bind(lo(0)); AsynchronousSocketChannel client = AsynchronousSocketChannel.open()) {
        Future<AsynchronousSocketChannel> acceptF = server.accept();
        client.connect(server.getLocalAddress()).get(5, TimeUnit.SECONDS);
        try (AsynchronousSocketChannel peer = acceptF.get(5, TimeUnit.SECONDS)) {
          int w = client.write(ByteBuffer.wrap("hello async".getBytes())).get(5, TimeUnit.SECONDS);
          ByteBuffer in = ByteBuffer.allocate(32);
          int r = peer.read(in).get(5, TimeUnit.SECONDS);
          peer.write(ByteBuffer.wrap(in.array(), 0, r)).get();
          ByteBuffer back = ByteBuffer.allocate(32);
          int r2 = client.read(back).get(5, TimeUnit.SECONDS);
          client.shutdownOutput();
          int eof = peer.read(ByteBuffer.allocate(4)).get(5, TimeUnit.SECONDS);
          return w + " " + r + " " + new String(in.array(), 0, r) + " " + r2 + " " + new String(back.array(), 0, r2) + " " + eof + " " + peer.getRemoteAddress().equals(client.getLocalAddress());
        }
      }
    });
    io("echo-handlers", () -> {
      try (AsynchronousServerSocketChannel server = AsynchronousServerSocketChannel.open().bind(lo(0)); AsynchronousSocketChannel client = AsynchronousSocketChannel.open()) {
        CountDownLatch done = new CountDownLatch(1);
        String[] result = new String[1];
        server.accept("acc", new CompletionHandler<AsynchronousSocketChannel, String>() {
          public void completed(AsynchronousSocketChannel ch, String att) {
            ByteBuffer buf = ByteBuffer.allocate(16);
            ch.read(buf, att, new CompletionHandler<Integer, String>() {
              public void completed(Integer n, String a2) { result[0] = a2 + ":" + n + ":" + new String(buf.array(), 0, n); done.countDown(); try { ch.close(); } catch (IOException e) { } }
              public void failed(Throwable t, String a2) { result[0] = "read failed " + t; done.countDown(); }
            });
          }
          public void failed(Throwable t, String att) { result[0] = "accept failed " + t; done.countDown(); }
        });
        client.connect(server.getLocalAddress(), null, new CompletionHandler<Void, Object>() {
          public void completed(Void v, Object a) { client.write(ByteBuffer.wrap("via handler".getBytes()), null, new CompletionHandler<Integer, Object>() { public void completed(Integer n, Object x) { } public void failed(Throwable t, Object x) { } }); }
          public void failed(Throwable t, Object a) { result[0] = "connect failed " + t; done.countDown(); }
        });
        boolean ok = done.await(5, TimeUnit.SECONDS);
        return ok + " " + result[0];
      }
    });
    io("pending-exceptions", () -> {
      try (AsynchronousServerSocketChannel server = AsynchronousServerSocketChannel.open().bind(lo(0)); AsynchronousSocketChannel client = AsynchronousSocketChannel.open()) {
        server.accept();
        String r = "";
        try { server.accept(); } catch (AcceptPendingException e) { r += "A"; }
        client.connect(server.getLocalAddress()).get(5, TimeUnit.SECONDS);
        Future<Integer> pending = client.read(ByteBuffer.allocate(4));
        try { client.read(ByteBuffer.allocate(4)); } catch (ReadPendingException e) { r += "R"; }
        return r;
      }
    });
    io("connect-states", () -> {
      try (AsynchronousServerSocketChannel server = AsynchronousServerSocketChannel.open().bind(lo(0)); AsynchronousSocketChannel client = AsynchronousSocketChannel.open()) {
        client.connect(server.getLocalAddress()).get(5, TimeUnit.SECONDS);
        try { client.connect(server.getLocalAddress()); return "no"; } catch (AlreadyConnectedException e) { return "ACE " + client.getRemoteAddress().equals(server.getLocalAddress()); }
      }
    });
    io("unconnected-ops", () -> { try (AsynchronousSocketChannel c = AsynchronousSocketChannel.open()) { String r = ""; try { c.read(ByteBuffer.allocate(1)); } catch (NotYetConnectedException e) { r += "R"; } try { c.write(ByteBuffer.allocate(1)); } catch (NotYetConnectedException e) { r += "W"; } return r; } });
    io("connect-refused", () -> { int port; try (ServerSocketChannel t = ServerSocketChannel.open()) { t.bind(lo(0)); port = ((InetSocketAddress) t.getLocalAddress()).getPort(); } try (AsynchronousSocketChannel c = AsynchronousSocketChannel.open()) { try { c.connect(lo(port)).get(5, TimeUnit.SECONDS); return "no"; } catch (ExecutionException e) { return e.getCause().getClass().getName() + ":" + e.getCause().getMessage(); } } });
    io("read-timeout", () -> {
      try (AsynchronousServerSocketChannel server = AsynchronousServerSocketChannel.open().bind(lo(0)); AsynchronousSocketChannel client = AsynchronousSocketChannel.open()) {
        Future<AsynchronousSocketChannel> acc = server.accept();
        client.connect(server.getLocalAddress()).get(5, TimeUnit.SECONDS);
        AsynchronousSocketChannel peer = acc.get(5, TimeUnit.SECONDS);
        CountDownLatch done = new CountDownLatch(1);
        Throwable[] err = new Throwable[1];
        client.read(ByteBuffer.allocate(4), 100, TimeUnit.MILLISECONDS, null, new CompletionHandler<Integer, Object>() {
          public void completed(Integer r, Object a) { done.countDown(); }
          public void failed(Throwable t, Object a) { err[0] = t; done.countDown(); }
        });
        boolean ok = done.await(5, TimeUnit.SECONDS);
        peer.close();
        return ok + " " + (err[0] == null ? null : err[0].getClass().getName());
      }
    });
    io("close-pending-read", () -> {
      try (AsynchronousServerSocketChannel server = AsynchronousServerSocketChannel.open().bind(lo(0)); AsynchronousSocketChannel client = AsynchronousSocketChannel.open()) {
        Future<AsynchronousSocketChannel> acc = server.accept();
        client.connect(server.getLocalAddress()).get(5, TimeUnit.SECONDS);
        AsynchronousSocketChannel peer = acc.get(5, TimeUnit.SECONDS);
        Future<Integer> pending = client.read(ByteBuffer.allocate(4));
        Thread.sleep(100);
        client.close();
        try { pending.get(5, TimeUnit.SECONDS); return "no"; } catch (ExecutionException e) { peer.close(); return e.getCause().getClass().getName(); }
      }
    });
    io("op-on-closed", () -> { AsynchronousSocketChannel c = AsynchronousSocketChannel.open(); c.close(); try { c.connect(lo(1)).get(5, TimeUnit.SECONDS); return "no"; } catch (ExecutionException e) { return e.getCause().getClass().getName() + " " + c.isOpen(); } });
    io("accept-on-closed", () -> { AsynchronousServerSocketChannel s = AsynchronousServerSocketChannel.open().bind(lo(0)); s.close(); try { s.accept().get(5, TimeUnit.SECONDS); return "no"; } catch (ExecutionException e) { return e.getCause().getClass().getName(); } });
    io("gather-scatter", () -> {
      try (AsynchronousServerSocketChannel server = AsynchronousServerSocketChannel.open().bind(lo(0)); AsynchronousSocketChannel client = AsynchronousSocketChannel.open()) {
        Future<AsynchronousSocketChannel> acc = server.accept();
        client.connect(server.getLocalAddress()).get(5, TimeUnit.SECONDS);
        try (AsynchronousSocketChannel peer = acc.get(5, TimeUnit.SECONDS)) {
          CompletableFuture<Long> w = new CompletableFuture<>();
          client.write(new ByteBuffer[] {ByteBuffer.wrap("ab".getBytes()), ByteBuffer.wrap("cd".getBytes())}, 0, 2, 0, TimeUnit.MILLISECONDS, null, new CompletionHandler<Long, Object>() { public void completed(Long r, Object a) { w.complete(r); } public void failed(Throwable t, Object a) { w.completeExceptionally(t); } });
          ByteBuffer x = ByteBuffer.allocate(4);
          CompletableFuture<Long> r = new CompletableFuture<>();
          peer.read(new ByteBuffer[] {x}, 0, 1, 0, TimeUnit.MILLISECONDS, null, new CompletionHandler<Long, Object>() { public void completed(Long n, Object a) { r.complete(n); } public void failed(Throwable t, Object a) { r.completeExceptionally(t); } });
          return w.get(5, TimeUnit.SECONDS) + " " + r.get(5, TimeUnit.SECONDS) + " " + new String(x.array(), 0, x.position());
        }
      }
    });
    io("custom-group", () -> {
      AsynchronousChannelGroup group = AsynchronousChannelGroup.withFixedThreadPool(2, r -> { Thread t = new Thread(r, "async-test"); t.setDaemon(true); return t; });
      String a = group.isShutdown() + " " + group.isTerminated();
      try (AsynchronousServerSocketChannel server = AsynchronousServerSocketChannel.open(group).bind(lo(0)); AsynchronousSocketChannel client = AsynchronousSocketChannel.open(group)) {
        CompletableFuture<String> thread = new CompletableFuture<>();
        server.accept(null, new CompletionHandler<AsynchronousSocketChannel, Object>() { public void completed(AsynchronousSocketChannel ch, Object x) { thread.complete(Thread.currentThread().getName()); try { ch.close(); } catch (IOException e) { } } public void failed(Throwable t, Object x) { thread.completeExceptionally(t); } });
        client.connect(server.getLocalAddress()).get(5, TimeUnit.SECONDS);
        String name = thread.get(5, TimeUnit.SECONDS);
        return a + " " + name;
      } finally {
        group.shutdown();
      }
    });
    io("group-shutdown", () -> { AsynchronousChannelGroup g = AsynchronousChannelGroup.withCachedThreadPool(Executors.newCachedThreadPool(), 1); g.shutdown(); return g.isShutdown() + " " + g.awaitTermination(5, TimeUnit.SECONDS) + " " + g.isTerminated(); });
    io("group-bad", () -> AsynchronousChannelGroup.withFixedThreadPool(0, Executors.defaultThreadFactory()));
    io("file-basic", () -> {
      Path p = Files.createTempFile("asyncfile", ".dat");
      try (AsynchronousFileChannel fc = AsynchronousFileChannel.open(p, StandardOpenOption.READ, StandardOpenOption.WRITE)) {
        int w = fc.write(ByteBuffer.wrap("0123456789".getBytes()), 0).get(5, TimeUnit.SECONDS);
        int w2 = fc.write(ByteBuffer.wrap("XY".getBytes()), 4).get(5, TimeUnit.SECONDS);
        ByteBuffer b = ByteBuffer.allocate(10);
        int r = fc.read(b, 0).get(5, TimeUnit.SECONDS);
        ByteBuffer eof = ByteBuffer.allocate(4);
        int e = fc.read(eof, 100).get(5, TimeUnit.SECONDS);
        long size = fc.size();
        fc.truncate(5);
        return w + " " + w2 + " " + r + " " + new String(b.array()) + " " + e + " " + size + " " + fc.size() + " " + fc.isOpen();
      } finally {
        Files.deleteIfExists(p);
      }
    });
    io("file-handlers", () -> {
      Path p = Files.createTempFile("asyncfile", ".dat");
      try (AsynchronousFileChannel fc = AsynchronousFileChannel.open(p, StandardOpenOption.READ, StandardOpenOption.WRITE)) {
        CountDownLatch l = new CountDownLatch(2);
        String[] r = new String[2];
        fc.write(ByteBuffer.wrap("handler data".getBytes()), 0, "w", new CompletionHandler<Integer, String>() { public void completed(Integer n, String a) { r[0] = a + n; l.countDown(); } public void failed(Throwable t, String a) { r[0] = "fail"; l.countDown(); } });
        Thread.sleep(100);
        ByteBuffer b = ByteBuffer.allocate(20);
        fc.read(b, 0, "r", new CompletionHandler<Integer, String>() { public void completed(Integer n, String a) { r[1] = a + n + new String(b.array(), 0, n); l.countDown(); } public void failed(Throwable t, String a) { r[1] = "fail"; l.countDown(); } });
        boolean ok = l.await(5, TimeUnit.SECONDS);
        return ok + " " + r[0] + " " + r[1];
      } finally {
        Files.deleteIfExists(p);
      }
    });
    io("file-lock", () -> {
      Path p = Files.createTempFile("asyncfile", ".dat");
      try (AsynchronousFileChannel fc = AsynchronousFileChannel.open(p, StandardOpenOption.READ, StandardOpenOption.WRITE)) {
        FileLock lock = fc.lock().get(5, TimeUnit.SECONDS);
        String a = lock.isValid() + " " + lock.isShared() + " " + lock.position() + " " + (lock.size() == Long.MAX_VALUE);
        lock.release();
        FileLock t = fc.tryLock(0, 10, true);
        return a + " " + lock.isValid() + " " + (t != null && t.isShared());
      } finally {
        Files.deleteIfExists(p);
      }
    });
    io("file-nonreadable", () -> { Path p = Files.createTempFile("asyncfile", ".dat"); try (AsynchronousFileChannel fc = AsynchronousFileChannel.open(p, StandardOpenOption.WRITE)) { String r = ""; try { fc.read(ByteBuffer.allocate(1), 0); } catch (NonReadableChannelException e) { r += "R"; } return r + fc.write(ByteBuffer.wrap("x".getBytes()), 0).get(); } finally { Files.deleteIfExists(p); } });
    io("file-nonwritable", () -> { Path p = Files.createTempFile("asyncfile", ".dat"); try (AsynchronousFileChannel fc = AsynchronousFileChannel.open(p, StandardOpenOption.READ)) { String r = ""; try { fc.write(ByteBuffer.wrap(new byte[1]), 0); } catch (NonWritableChannelException e) { r += "W"; } try { fc.truncate(0); } catch (NonWritableChannelException e) { r += "T"; } return r; } finally { Files.deleteIfExists(p); } });
    io("file-bad-args", () -> { Path p = Files.createTempFile("asyncfile", ".dat"); try (AsynchronousFileChannel fc = AsynchronousFileChannel.open(p, StandardOpenOption.READ, StandardOpenOption.WRITE)) { String r = ""; try { fc.read(ByteBuffer.allocate(1), -1); } catch (IllegalArgumentException e) { r += e.getMessage(); } try { fc.truncate(-1); } catch (IllegalArgumentException e) { r += "|" + e.getMessage(); } try { fc.lock(-1, 1, false); } catch (IllegalArgumentException e) { r += "|" + e.getMessage(); } return r; } finally { Files.deleteIfExists(p); } });
    io("file-closed", () -> { Path p = Files.createTempFile("asyncfile", ".dat"); try { AsynchronousFileChannel fc = AsynchronousFileChannel.open(p, StandardOpenOption.READ); fc.close(); try { fc.read(ByteBuffer.allocate(1), 0).get(); return "no"; } catch (ExecutionException e) { return e.getCause().getClass().getName(); } } finally { Files.deleteIfExists(p); } });
    io("file-append", () -> AsynchronousFileChannel.open(Paths.get("/tmp/x"), StandardOpenOption.WRITE, StandardOpenOption.APPEND));
    io("file-missing", () -> AsynchronousFileChannel.open(Paths.get("/tmp/no-such-dir-xyz/f"), StandardOpenOption.READ));
  }
}
