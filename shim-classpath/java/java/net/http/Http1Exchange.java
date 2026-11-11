/*
 * Copyright (c) 2026 BasaltMC.
 *
 * Licensed under the MIT License; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package java.net.http;

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.TreeMap;

/** HTTP/1.1 message framing and a keep-alive connection pool. */
final class Http1Exchange {
  static final class Connection {
    final Socket socket;
    final BufferedInputStream in;
    final OutputStream out;
    final String key;
    boolean reused;

    Connection(Socket socket, String key) throws IOException {
      this.socket = socket;
      this.in = new BufferedInputStream(socket.getInputStream(), 8192);
      this.out = socket.getOutputStream();
      this.key = key;
    }

    void close() {
      try {
        socket.close();
      } catch (IOException ignored) {
      }
    }
  }

  static final class Head {
    int status;
    Map<String, List<String>> headers = new TreeMap<String, List<String>>(String.CASE_INSENSITIVE_ORDER);

    String first(String name) {
      List<String> v = headers.get(name);
      return v == null || v.isEmpty() ? null : v.get(0);
    }
  }

  static final class Pool {
    private final Map<String, ArrayDeque<Connection>> idle = new HashMap<String, ArrayDeque<Connection>>();

    synchronized Connection take(String key) {
      ArrayDeque<Connection> q = idle.get(key);
      while (q != null && !q.isEmpty()) {
        Connection c = q.pollLast();
        if (!c.socket.isClosed() && !c.socket.isInputShutdown() && !c.socket.isOutputShutdown() && stillOpen(c)) {
          c.reused = true;
          return c;
        }
        c.close();
      }
      return null;
    }

    private static boolean stillOpen(Connection c) {
      try {
        int old = c.socket.getSoTimeout();
        c.socket.setSoTimeout(1);
        try {
          c.in.mark(1);
          int b = c.in.read();
          if (b < 0) {
            return false;
          }
          c.in.reset();
          return false;
        } catch (java.net.SocketTimeoutException e) {
          return true;
        } finally {
          c.socket.setSoTimeout(old);
        }
      } catch (IOException e) {
        return false;
      }
    }

    synchronized void give(Connection c) {
      ArrayDeque<Connection> q = idle.get(c.key);
      if (q == null) {
        q = new ArrayDeque<Connection>();
        idle.put(c.key, q);
      }
      q.addLast(c);
    }
  }

  private Http1Exchange() {
  }

  static void writeRequest(Connection c, String method, String target, List<String[]> headers) throws IOException {
    StringBuilder head = new StringBuilder(256);
    head.append(method).append(' ').append(target).append(" HTTP/1.1\r\n");
    for (String[] h : headers) {
      head.append(h[0]).append(": ").append(h[1]).append("\r\n");
    }
    head.append("\r\n");
    c.out.write(head.toString().getBytes(StandardCharsets.ISO_8859_1));
    c.out.flush();
  }

  static void writeBody(Connection c, byte[] body, boolean chunked) throws IOException {
    if (body.length > 0) {
      if (chunked) {
        c.out.write((Integer.toHexString(body.length) + "\r\n").getBytes(StandardCharsets.ISO_8859_1));
        c.out.write(body);
        c.out.write('\r');
        c.out.write('\n');
      } else {
        c.out.write(body);
      }
    }
    if (chunked) {
      c.out.write("0\r\n\r\n".getBytes(StandardCharsets.ISO_8859_1));
    }
    c.out.flush();
  }

  private static String readLine(InputStream in) throws IOException {
    ByteArrayOutputStream line = new ByteArrayOutputStream(64);
    int b;
    while ((b = in.read()) >= 0) {
      if (b == '\n') {
        byte[] a = line.toByteArray();
        int n = a.length;
        if (n > 0 && a[n - 1] == '\r') {
          n--;
        }
        return new String(a, 0, n, StandardCharsets.ISO_8859_1);
      }
      line.write(b);
    }
    if (line.size() == 0) {
      return null;
    }
    throw new EOFException("EOF reached while reading");
  }

  static Head readHead(Connection c) throws IOException {
    String status = readLine(c.in);
    if (status == null) {
      throw new IOException("HTTP/1.1 header parser received no bytes");
    }
    String[] parts = status.split(" ", 3);
    if (parts.length < 2 || !parts[0].startsWith("HTTP/")) {
      throw new IOException("invalid HTTP status line: " + status);
    }
    Head h = new Head();
    try {
      h.status = Integer.parseInt(parts[1].trim());
    } catch (NumberFormatException e) {
      throw new IOException("invalid HTTP status line: " + status);
    }
    String line;
    String last = null;
    while ((line = readLine(c.in)) != null && !line.isEmpty()) {
      if ((line.charAt(0) == ' ' || line.charAt(0) == '\t') && last != null) {
        List<String> vs = h.headers.get(last);
        vs.set(vs.size() - 1, vs.get(vs.size() - 1) + " " + line.trim());
        continue;
      }
      int colon = line.indexOf(':');
      if (colon <= 0) {
        throw new IOException("Invalid header: " + line);
      }
      last = line.substring(0, colon).trim().toLowerCase(Locale.ROOT);
      List<String> vs = h.headers.get(last);
      if (vs == null) {
        vs = new ArrayList<String>();
        h.headers.put(last, vs);
      }
      vs.add(line.substring(colon + 1).trim());
    }
    return h;
  }

  /** Body stream positioned after the head; the callback runs once when the message is fully consumed. */
  static InputStream bodyStream(Connection c, String method, Head h, Runnable onEnd) throws IOException {
    boolean none = method.equals("HEAD") || h.status == 204 || h.status == 304 || h.status < 200;
    String te = h.first("transfer-encoding");
    String cl = h.first("content-length");
    if (none) {
      onEnd.run();
      return InputStream.nullInputStream();
    }
    if (te != null && te.toLowerCase(Locale.ROOT).contains("chunked")) {
      return new Chunked(c.in, onEnd);
    }
    if (cl != null) {
      long n;
      try {
        n = Long.parseLong(cl.trim());
      } catch (NumberFormatException e) {
        throw new IOException("Invalid content-length: " + cl);
      }
      if (n == 0) {
        onEnd.run();
        return InputStream.nullInputStream();
      }
      return new Bounded(c.in, n, onEnd);
    }
    return new ToEnd(c.in, onEnd);
  }

  static boolean keepAlive(Head h) {
    String conn = h.first("connection");
    return conn == null || !conn.toLowerCase(Locale.ROOT).contains("close");
  }

  static boolean framedToEnd(Head h, String method) {
    return !(method.equals("HEAD") || h.status == 204 || h.status == 304 || h.status < 200)
        && h.first("transfer-encoding") == null && h.first("content-length") == null;
  }

  private abstract static class Body extends InputStream {
    final InputStream in;
    private final Runnable onEnd;
    private boolean finished;

    Body(InputStream in, Runnable onEnd) {
      this.in = in;
      this.onEnd = onEnd;
    }

    void done() {
      if (!finished) {
        finished = true;
        onEnd.run();
      }
    }

    @Override
    public int read() throws IOException {
      byte[] one = new byte[1];
      int n = read(one, 0, 1);
      return n < 0 ? -1 : one[0] & 0xff;
    }
  }

  private static final class Bounded extends Body {
    private long remaining;

    Bounded(InputStream in, long length, Runnable onEnd) {
      super(in, onEnd);
      this.remaining = length;
    }

    @Override
    public int read(byte[] b, int off, int len) throws IOException {
      if (remaining <= 0) {
        done();
        return -1;
      }
      int n = in.read(b, off, (int) Math.min(len, remaining));
      if (n < 0) {
        throw new EOFException("fixed content-length: premature end of stream");
      }
      remaining -= n;
      if (remaining == 0) {
        done();
      }
      return n;
    }
  }

  private static final class ToEnd extends Body {
    ToEnd(InputStream in, Runnable onEnd) {
      super(in, onEnd);
    }

    @Override
    public int read(byte[] b, int off, int len) throws IOException {
      int n = in.read(b, off, len);
      if (n < 0) {
        done();
      }
      return n;
    }
  }

  private static final class Chunked extends Body {
    private long left;
    private boolean eof;

    Chunked(InputStream in, Runnable onEnd) {
      super(in, onEnd);
    }

    @Override
    public int read(byte[] b, int off, int len) throws IOException {
      if (eof) {
        return -1;
      }
      if (left == 0) {
        String size = readLine(in);
        if (size == null) {
          throw new EOFException("unexpected end of chunked stream");
        }
        int semi = size.indexOf(';');
        if (semi >= 0) {
          size = size.substring(0, semi);
        }
        try {
          left = Long.parseLong(size.trim(), 16);
        } catch (NumberFormatException e) {
          throw new IOException("Invalid chunk size: " + size);
        }
        if (left == 0) {
          String t;
          while ((t = readLine(in)) != null && !t.isEmpty()) {
            continue;
          }
          eof = true;
          done();
          return -1;
        }
      }
      int n = in.read(b, off, (int) Math.min(len, left));
      if (n < 0) {
        throw new EOFException("unexpected end of chunked stream");
      }
      left -= n;
      if (left == 0) {
        readLine(in);
      }
      return n;
    }
  }

  static Socket plainSocket(String host, int port, int connectTimeout) throws IOException {
    Socket s = new Socket();
    s.setTcpNoDelay(true);
    s.connect(new InetSocketAddress(host, port), Math.max(connectTimeout, 0));
    return s;
  }
}
