/*
 * Copyright (c) 2026 BasaltMC.
 *
 * Licensed under the MIT License; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package java.net.http;

import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.net.ssl.SSLParameters;
import javax.net.ssl.SSLSession;
import javax.net.ssl.SSLSocket;
import javax.net.ssl.SSLSocketFactory;

/** A multiplexed HTTP/2 connection over TLS negotiated with ALPN; requests from several threads share it. */
final class Http2Connection {
  private static final byte[] kPreface = "PRI * HTTP/2.0\r\n\r\nSM\r\n\r\n".getBytes(StandardCharsets.ISO_8859_1);
  private static final int kData = 0, kHeaders = 1, kRstStream = 3, kSettings = 4, kPing = 6, kGoaway = 7,
      kWindowUpdate = 8, kContinuation = 9;
  private static final int kEndStream = 1, kAck = 1, kEndHeaders = 4, kPadded = 8, kPriority = 0x20;

  private static final class Stream {
    final int id;
    final Http2Exchange.Result result = new Http2Exchange.Result();
    final ByteArrayOutputStream data = new ByteArrayOutputStream();
    ByteArrayOutputStream headerBlock;
    boolean headersDone;
    boolean ended;
    IOException error;
    long window;

    Stream(int id, long window) {
      this.id = id;
      this.window = window;
    }
  }

  private final SSLSocket socket;
  private final DataInputStream in;
  private final OutputStream out;
  private final Hpack.Decoder decoder = new Hpack.Decoder();
  private final Object writeLock = new Object();
  private final Map<Integer, Stream> streams = new HashMap<Integer, Stream>();
  private int nextId = 1;
  private long connectionWindow = 65535;
  private int initialWindow = 65535;
  private int maxFrame = 16384;
  private int maxConcurrent = Integer.MAX_VALUE;
  private boolean closed;
  private int lastGoaway = Integer.MAX_VALUE;

  private Http2Connection(SSLSocket socket) throws IOException {
    this.socket = socket;
    this.in = new DataInputStream(socket.getInputStream());
    this.out = socket.getOutputStream();
  }

  /** Returns null when the server did not select h2. */
  static Http2Connection open(SSLSocketFactory factory, SSLParameters base, java.net.URI uri, int connectTimeout)
      throws IOException {
    int port = uri.getPort() == -1 ? 443 : uri.getPort();
    Socket plain = new Socket();
    SSLSocket socket = null;
    try {
      plain.connect(new InetSocketAddress(uri.getHost(), port), connectTimeout);
      socket = (SSLSocket) factory.createSocket(plain, uri.getHost(), port, true);
      SSLParameters p = socket.getSSLParameters();
      if (base != null) {
        p.setCipherSuites(base.getCipherSuites() == null ? p.getCipherSuites() : base.getCipherSuites());
        p.setProtocols(base.getProtocols() == null ? p.getProtocols() : base.getProtocols());
        p.setEndpointIdentificationAlgorithm(base.getEndpointIdentificationAlgorithm());
      }
      p.setApplicationProtocols(new String[] {"h2", "http/1.1"});
      socket.setSSLParameters(p);
      socket.startHandshake();
      if (!"h2".equals(socket.getApplicationProtocol())) {
        socket.close();
        return null;
      }
      Http2Connection c = new Http2Connection(socket);
      c.start();
      return c;
    } catch (IOException | RuntimeException e) {
      try {
        (socket != null ? socket : plain).close();
      } catch (IOException ignored) {
      }
      throw e;
    }
  }

  SSLSession session() {
    return socket.getSession();
  }

  synchronized boolean usable() {
    return !closed && nextId > 0 && lastGoaway == Integer.MAX_VALUE;
  }

  void close() {
    fail(new IOException("connection closed"));
  }

  private void start() throws IOException {
    synchronized (writeLock) {
      out.write(kPreface);
      byte[] settings = {0, 2, 0, 0, 0, 0, 0, 4, 0, (byte) 0xff, (byte) 0xff, (byte) 0xff};
      frame(kSettings, 0, 0, settings, 0, settings.length);
      frame(kWindowUpdate, 0, 0, ints(0x7fff0000), 0, 4);
    }
    Thread reader = new Thread(this::readLoop, "Shim-HTTP2-Reader");
    reader.setDaemon(true);
    reader.start();
  }

  private void frame(int type, int flags, int stream, byte[] payload, int off, int len) throws IOException {
    byte[] h = {(byte) (len >> 16), (byte) (len >> 8), (byte) len, (byte) type, (byte) flags,
        (byte) (stream >> 24), (byte) (stream >> 16), (byte) (stream >> 8), (byte) stream};
    out.write(h);
    out.write(payload, off, len);
    out.flush();
  }

  private void send(int type, int flags, int stream, byte[] payload) throws IOException {
    synchronized (writeLock) {
      frame(type, flags, stream, payload, 0, payload.length);
    }
  }

  private static byte[] ints(int... values) {
    byte[] b = new byte[values.length * 4];
    for (int i = 0; i < values.length; i++) {
      b[i * 4] = (byte) (values[i] >> 24);
      b[i * 4 + 1] = (byte) (values[i] >> 16);
      b[i * 4 + 2] = (byte) (values[i] >> 8);
      b[i * 4 + 3] = (byte) values[i];
    }
    return b;
  }

  private static int word(byte[] b, int o) {
    return ((b[o] & 0xff) << 24) | ((b[o + 1] & 0xff) << 16) | ((b[o + 2] & 0xff) << 8) | (b[o + 3] & 0xff);
  }

  private void fail(IOException e) {
    List<Stream> pending;
    synchronized (this) {
      if (closed) {
        return;
      }
      closed = true;
      pending = new ArrayList<Stream>(streams.values());
      streams.clear();
      notifyAll();
    }
    for (Stream s : pending) {
      synchronized (s) {
        if (!s.ended) {
          s.error = e;
          s.notifyAll();
        }
      }
    }
    try {
      socket.close();
    } catch (IOException ignored) {
    }
  }

  private synchronized Stream stream(int id) {
    return streams.get(id);
  }

  private void readLoop() {
    try {
      while (true) {
        byte[] h = new byte[9];
        in.readFully(h);
        int len = ((h[0] & 0xff) << 16) | ((h[1] & 0xff) << 8) | (h[2] & 0xff);
        int type = h[3] & 0xff;
        int flags = h[4] & 0xff;
        int id = ((h[5] & 0x7f) << 24) | ((h[6] & 0xff) << 16) | ((h[7] & 0xff) << 8) | (h[8] & 0xff);
        byte[] payload = new byte[len];
        in.readFully(payload);
        handle(type, flags, id, payload);
      }
    } catch (IOException e) {
      fail(e);
    } catch (RuntimeException e) {
      fail(new IOException(e));
    }
  }

  private void handle(int type, int flags, int id, byte[] payload) throws IOException {
    switch (type) {
    case kSettings:
      if ((flags & kAck) == 0) {
        synchronized (this) {
          for (int i = 0; i + 6 <= payload.length; i += 6) {
            int sid = ((payload[i] & 0xff) << 8) | (payload[i + 1] & 0xff);
            int value = word(payload, i + 2);
            if (sid == 4) {
              for (Stream s : streams.values()) {
                s.window += value - initialWindow;
              }
              initialWindow = value;
            } else if (sid == 5) {
              maxFrame = value;
            } else if (sid == 3) {
              maxConcurrent = value < 0 ? Integer.MAX_VALUE : value;
            }
          }
          notifyAll();
        }
        send(kSettings, kAck, 0, new byte[0]);
      }
      return;
    case kPing:
      if ((flags & kAck) == 0) {
        send(kPing, kAck, 0, payload);
      }
      return;
    case kGoaway: {
      int last = word(payload, 0) & 0x7fffffff;
      List<Stream> dropped = new ArrayList<Stream>();
      synchronized (this) {
        lastGoaway = last;
        for (Stream s : streams.values()) {
          if (s.id > last) {
            dropped.add(s);
          }
        }
        for (Stream s : dropped) {
          streams.remove(s.id);
        }
        notifyAll();
      }
      for (Stream s : dropped) {
        synchronized (s) {
          s.error = new IOException("received GOAWAY");
          s.notifyAll();
        }
      }
      return;
    }
    case kWindowUpdate: {
      long inc = word(payload, 0) & 0x7fffffffL;
      synchronized (this) {
        if (id == 0) {
          connectionWindow += inc;
        } else {
          Stream s = streams.get(id);
          if (s != null) {
            s.window += inc;
          }
        }
        notifyAll();
      }
      return;
    }
    default:
      break;
    }
    Stream s = stream(id);
    if (s == null) {
      if (type == kData && payload.length > 0) {
        send(kWindowUpdate, 0, 0, ints(payload.length));
      }
      return;
    }
    if (type == kHeaders || type == kContinuation) {
      onHeaders(s, type, flags, payload);
    } else if (type == kData) {
      int off = 0;
      int end = payload.length;
      if ((flags & kPadded) != 0) {
        end -= payload[0] & 0xff;
        off = 1;
      }
      s.data.write(payload, off, end - off);
      if ((flags & kEndStream) != 0) {
        finish(s);
      }
      if (payload.length > 0) {
        try {
          send(kWindowUpdate, 0, 0, ints(payload.length));
          if ((flags & kEndStream) == 0) {
            send(kWindowUpdate, 0, id, ints(payload.length));
          }
        } catch (IOException ignored) {
          // a peer that closed after its last frame still delivered the response
        }
      }
    } else if (type == kRstStream) {
      synchronized (this) {
        streams.remove(id);
      }
      synchronized (s) {
        s.error = new IOException("stream reset: " + word(payload, 0));
        s.notifyAll();
      }
    }
  }

  private void onHeaders(Stream s, int type, int flags, byte[] payload) throws IOException {
    int off = 0;
    int end = payload.length;
    if (type == kHeaders) {
      if ((flags & kPadded) != 0) {
        end -= payload[0] & 0xff;
        off = 1;
      }
      if ((flags & kPriority) != 0) {
        off += 5;
      }
      s.headerBlock = new ByteArrayOutputStream();
    }
    s.headerBlock.write(payload, off, end - off);
    if ((flags & kEndHeaders) != 0) {
      boolean informational = false;
      for (String[] h : decoder.decode(s.headerBlock.toByteArray())) {
        if (h[0].equals(":status")) {
          int status = Integer.parseInt(h[1]);
          informational = status >= 100 && status < 200;
          if (!informational) {
            s.result.status = status;
          }
        } else if (!informational) {
          List<String> list = s.result.headers.get(h[0]);
          if (list == null) {
            list = new ArrayList<String>();
            s.result.headers.put(h[0], list);
          }
          list.add(h[1]);
        }
      }
      s.headersDone |= !informational;
      s.headerBlock = null;
    }
    if ((flags & kEndStream) != 0) {
      finish(s);
    }
  }

  private void finish(Stream s) {
    synchronized (this) {
      streams.remove(s.id);
      notifyAll();
    }
    synchronized (s) {
      if (!s.headersDone) {
        s.error = new IOException("no response headers received");
      }
      s.result.body = s.data.toByteArray();
      s.ended = true;
      s.notifyAll();
    }
  }

  Http2Exchange.Result request(java.net.URI uri, String method, List<String[]> userHeaders, byte[] body,
                               long timeoutMillis) throws IOException, InterruptedException {
    long deadline = timeoutMillis > 0 ? System.nanoTime() + timeoutMillis * 1000000L : 0;
    String path = uri.getRawPath() == null || uri.getRawPath().isEmpty() ? "/" : uri.getRawPath();
    if (uri.getRawQuery() != null) {
      path += "?" + uri.getRawQuery();
    }
    String authority = uri.getHost() + (uri.getPort() == -1 ? "" : ":" + uri.getPort());
    List<String[]> block = new ArrayList<String[]>();
    block.add(new String[] {":method", method});
    block.add(new String[] {":scheme", "https"});
    block.add(new String[] {":authority", authority});
    block.add(new String[] {":path", path});
    block.addAll(userHeaders);
    byte[] encoded = Hpack.encode(block);
    boolean noBody = body == null || body.length == 0;
    Stream s;
    synchronized (this) {
      while (!closed && streams.size() >= maxConcurrent) {
        wait(remaining(deadline));
      }
      if (closed || lastGoaway != Integer.MAX_VALUE || nextId < 0) {
        throw new IOException("connection closed");
      }
      s = new Stream(nextId, initialWindow);
      nextId += 2;
      streams.put(s.id, s);
    }
    try {
      synchronized (writeLock) {
        int first = Math.min(encoded.length, maxFrame);
        int f = (noBody ? kEndStream : 0) | (first == encoded.length ? kEndHeaders : 0);
        frame(kHeaders, f, s.id, encoded, 0, first);
        int pos = first;
        while (pos < encoded.length) {
          int n = Math.min(encoded.length - pos, maxFrame);
          frame(kContinuation, pos + n == encoded.length ? kEndHeaders : 0, s.id, encoded, pos, n);
          pos += n;
        }
      }
      if (!noBody) {
        sendBody(s, body, deadline);
      }
      synchronized (s) {
        while (!s.ended && s.error == null) {
          s.wait(remaining(deadline));
        }
        if (s.error != null) {
          throw s.error;
        }
      }
      return s.result;
    } catch (HttpTimeoutException e) {
      cancel(s);
      throw e;
    } catch (InterruptedException | IOException | RuntimeException e) {
      synchronized (this) {
        streams.remove(s.id);
      }
      throw e;
    }
  }

  private long remaining(long deadline) throws HttpTimeoutException {
    if (deadline == 0) {
      return 0;
    }
    long ms = (deadline - System.nanoTime()) / 1000000L;
    if (ms <= 0) {
      throw new HttpTimeoutException("request timed out");
    }
    return ms;
  }

  private void cancel(Stream s) {
    synchronized (this) {
      streams.remove(s.id);
    }
    try {
      send(kRstStream, 0, s.id, ints(8));
    } catch (IOException ignored) {
    }
  }

  private void sendBody(Stream s, byte[] body, long deadline) throws IOException, InterruptedException {
    int sent = 0;
    while (sent < body.length) {
      int n;
      synchronized (this) {
        long room;
        while ((room = Math.min(connectionWindow, s.window)) <= 0) {
          if (closed || s.error != null) {
            throw s.error != null ? s.error : new IOException("connection closed");
          }
          wait(remaining(deadline));
        }
        n = (int) Math.min(Math.min(body.length - sent, maxFrame), room);
        connectionWindow -= n;
        s.window -= n;
      }
      synchronized (writeLock) {
        frame(kData, sent + n == body.length ? kEndStream : 0, s.id, body, sent, n);
      }
      sent += n;
    }
  }
}
