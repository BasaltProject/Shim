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
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.URI;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.charset.CharsetDecoder;
import java.nio.charset.CodingErrorAction;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.Executor;

final class WebSocketClient {
  private WebSocketClient() { }

  private static final String kGuid = "258EAFA5-E914-47DA-95CA-C5AB0DC85B11";
  private static final SecureRandom random = new SecureRandom();

  static final class BuilderImpl implements WebSocket.Builder {
    private final HttpClient client;
    private final Executor executor;
    private final List<String[]> headers = new ArrayList<String[]>();
    private Duration connectTimeout;
    private final List<String> subprotocols = new ArrayList<String>();

    BuilderImpl(HttpClient client, Executor executor) {
      this.client = client;
      this.executor = executor;
      this.connectTimeout = client.connectTimeout().orElse(null);
    }

    @Override
    public WebSocket.Builder header(String name, String value) {
      Objects.requireNonNull(name, "name");
      Objects.requireNonNull(value, "value");
      headers.add(new String[] {name, value});
      return this;
    }

    @Override
    public WebSocket.Builder connectTimeout(Duration timeout) {
      Objects.requireNonNull(timeout, "timeout");
      this.connectTimeout = timeout;
      return this;
    }

    @Override
    public WebSocket.Builder subprotocols(String mostPreferred, String... lesserPreferred) {
      Objects.requireNonNull(mostPreferred, "mostPreferred");
      Objects.requireNonNull(lesserPreferred, "lesserPreferred");
      List<String> all = new ArrayList<String>();
      all.add(mostPreferred);
      for (String s : lesserPreferred) {
        all.add(Objects.requireNonNull(s, "subprotocol"));
      }
      subprotocols.clear();
      subprotocols.addAll(all);
      return this;
    }

    private static String validate(URI uri, List<String[]> headers, List<String> protocols) {
      String scheme = uri.getScheme();
      if (scheme == null || !(scheme.equalsIgnoreCase("ws") || scheme.equalsIgnoreCase("wss"))) {
        return "invalid URI scheme: " + scheme;
      }
      if (uri.getHost() == null) {
        return "Invalid URI: " + uri;
      }
      if (uri.getRawFragment() != null) {
        return "URI has a fragment: " + uri;
      }
      for (String[] h : headers) {
        String lower = h[0].toLowerCase(Locale.ROOT);
        if (lower.isEmpty() || lower.equals("connection") || lower.equals("upgrade") || lower.startsWith("sec-websocket-")
            || lower.equals("host") || lower.equals("content-length") || lower.equals("expect")
            || lower.equals("transfer-encoding")) {
          return "Restricted header: " + h[0];
        }
        for (int i = 0; i < h[0].length(); ++i) {
          char c = h[0].charAt(i);
          if (c <= 0x20 || c >= 0x7f || ":()<>@,;\\\"/[]?={}".indexOf(c) >= 0) {
            return "Illegal header name: " + h[0];
          }
        }
        if (h[1].indexOf('\r') >= 0 || h[1].indexOf('\n') >= 0) {
          return "Illegal header value";
        }
      }
      for (String sp : protocols) {
        if (sp.isEmpty()) {
          return "Illegal subprotocol: " + sp;
        }
        for (int i = 0; i < sp.length(); ++i) {
          char c = sp.charAt(i);
          if (c <= 0x20 || c >= 0x7f || "()<>@,;:\\\"/[]?={}".indexOf(c) >= 0) {
            return "Illegal subprotocol: " + sp;
          }
        }
        if (protocols.indexOf(sp) != protocols.lastIndexOf(sp)) {
          return "Duplicating subprotocol: " + sp;
        }
      }
      return null;
    }

    @Override
    public CompletableFuture<WebSocket> buildAsync(final URI uri, final WebSocket.Listener listener) {
      Objects.requireNonNull(uri, "uri");
      Objects.requireNonNull(listener, "listener");
      String problem = validate(uri, headers, subprotocols);
      if (problem != null) {
        CompletableFuture<WebSocket> failed = new CompletableFuture<WebSocket>();
        failed.completeExceptionally(new IllegalArgumentException(problem));
        return failed;
      }
      final List<String[]> extra = new ArrayList<String[]>(headers);
      final List<String> protocols = new ArrayList<String>(subprotocols);
      final Duration timeout = connectTimeout;
      final CompletableFuture<WebSocket> result = new CompletableFuture<WebSocket>();
      Runnable task = new Runnable() {
        public void run() {
          try {
            result.complete(connect(client, uri, extra, protocols, timeout, listener));
          } catch (Throwable t) {
            result.completeExceptionally(t);
          }
        }
      };
      if (executor != null) {
        executor.execute(task);
      } else {
        Thread t = new Thread(task, "WebSocket-connect");
        t.setDaemon(true);
        t.start();
      }
      return result;
    }
  }

  private static WebSocket connect(HttpClient client, URI uri, List<String[]> extra, List<String> protocols,
                                   Duration timeout, WebSocket.Listener listener) throws IOException {
    boolean secure = uri.getScheme().equalsIgnoreCase("wss");
    int port = uri.getPort() >= 0 ? uri.getPort() : (secure ? 443 : 80);
    Socket socket;
    if (secure) {
      socket = client.sslContext().getSocketFactory().createSocket();
    } else {
      socket = new Socket();
    }
    try {
      int millis = timeout == null ? 0 : (int) Math.min(Integer.MAX_VALUE, timeout.toMillis());
      socket.connect(new InetSocketAddress(uri.getHost(), port), millis);
      socket.setTcpNoDelay(true);
      byte[] keyBytes = new byte[16];
      random.nextBytes(keyBytes);
      String key = Base64.getEncoder().encodeToString(keyBytes);
      String path = uri.getRawPath() == null || uri.getRawPath().isEmpty() ? "/" : uri.getRawPath();
      if (uri.getRawQuery() != null) {
        path += "?" + uri.getRawQuery();
      }
      StringBuilder request = new StringBuilder();
      request.append("GET ").append(path).append(" HTTP/1.1\r\n");
      request.append("Host: ").append(uri.getHost());
      if (uri.getPort() >= 0) {
        request.append(':').append(uri.getPort());
      }
      request.append("\r\nUpgrade: websocket\r\nConnection: Upgrade\r\n");
      request.append("Sec-WebSocket-Key: ").append(key).append("\r\n");
      request.append("Sec-WebSocket-Version: 13\r\n");
      if (!protocols.isEmpty()) {
        request.append("Sec-WebSocket-Protocol: ");
        for (int i = 0; i < protocols.size(); ++i) {
          request.append(i == 0 ? "" : ", ").append(protocols.get(i));
        }
        request.append("\r\n");
      }
      for (String[] h : extra) {
        request.append(h[0]).append(": ").append(h[1]).append("\r\n");
      }
      request.append("\r\n");
      OutputStream out = socket.getOutputStream();
      out.write(request.toString().getBytes(StandardCharsets.ISO_8859_1));
      out.flush();
      InputStream in = socket.getInputStream();
      String statusLine = readLine(in);
      if (statusLine == null || !statusLine.startsWith("HTTP/1.1 101")) {
        throw new WebSocketHandshakeException(null);
      }
      String accept = null;
      String subprotocol = null;
      String line;
      while ((line = readLine(in)) != null && !line.isEmpty()) {
        int colon = line.indexOf(':');
        if (colon > 0) {
          String name = line.substring(0, colon).trim().toLowerCase(Locale.ROOT);
          String value = line.substring(colon + 1).trim();
          if (name.equals("sec-websocket-accept")) {
            accept = value;
          } else if (name.equals("sec-websocket-protocol")) {
            subprotocol = value;
          }
        }
      }
      MessageDigest sha1 = MessageDigest.getInstance("SHA-1");
      String expected = Base64.getEncoder().encodeToString(sha1.digest((key + kGuid).getBytes(StandardCharsets.ISO_8859_1)));
      if (!expected.equals(accept)) {
        throw new WebSocketHandshakeException(null);
      }
      if (subprotocol != null && !protocols.contains(subprotocol)) {
        throw new WebSocketHandshakeException(null);
      }
      Connection connection = new Connection(socket, in, out, listener, subprotocol == null ? "" : subprotocol);
      connection.start();
      return connection;
    } catch (java.security.NoSuchAlgorithmException e) {
      closeQuietly(socket);
      throw new IOException(e);
    } catch (IOException | RuntimeException e) {
      closeQuietly(socket);
      throw e;
    }
  }

  private static void closeQuietly(Socket socket) {
    try {
      socket.close();
    } catch (IOException ignore) { }
  }

  private static String readLine(InputStream in) throws IOException {
    StringBuilder sb = new StringBuilder();
    int c;
    while ((c = in.read()) >= 0) {
      if (c == '\n') {
        int n = sb.length();
        if (n > 0 && sb.charAt(n - 1) == '\r') {
          sb.setLength(n - 1);
        }
        return sb.toString();
      }
      sb.append((char) c);
    }
    return sb.length() == 0 ? null : sb.toString();
  }

  private static final class Connection implements WebSocket {
    private final Socket socket;
    private final InputStream in;
    private final OutputStream out;
    private final Listener listener;
    private final String subprotocol;
    private final Object writeLock = new Object();
    private final Object demandLock = new Object();
    private long demand;
    private volatile boolean outputClosed;
    private volatile boolean inputClosed;
    private volatile boolean aborted;
    private boolean sendingFragments;
    private boolean receivingText;
    private boolean receivingBinary;
    private final CharsetDecoder decoder = StandardCharsets.UTF_8.newDecoder()
        .onMalformedInput(CodingErrorAction.REPORT).onUnmappableCharacter(CodingErrorAction.REPORT);
    private ByteBuffer pendingBytes = ByteBuffer.allocate(0);

    Connection(Socket socket, InputStream in, OutputStream out, Listener listener, String subprotocol) {
      this.socket = socket;
      this.in = in;
      this.out = out;
      this.listener = listener;
      this.subprotocol = subprotocol;
    }

    void start() {
      Thread reader = new Thread(new Runnable() {
        public void run() {
          readLoop();
        }
      }, "WebSocket-reader");
      reader.setDaemon(true);
      reader.start();
    }

    @Override
    public CompletableFuture<WebSocket> sendText(CharSequence message, boolean last) {
      Objects.requireNonNull(message, "message");
      return send(sendingFragments ? 0 : 1, message.toString().getBytes(StandardCharsets.UTF_8), last);
    }

    @Override
    public CompletableFuture<WebSocket> sendBinary(ByteBuffer message, boolean last) {
      Objects.requireNonNull(message, "message");
      byte[] data = new byte[message.remaining()];
      message.get(data);
      return send(sendingFragments ? 0 : 2, data, last);
    }

    @Override
    public CompletableFuture<WebSocket> sendPing(ByteBuffer message) {
      Objects.requireNonNull(message, "message");
      byte[] data = new byte[message.remaining()];
      message.get(data);
      if (outputClosed) {
        return failed(new IOException("Output closed"));
      }
      if (data.length > 125) {
        return failed(new IllegalArgumentException("Message too long"));
      }
      return control(9, data);
    }

    @Override
    public CompletableFuture<WebSocket> sendPong(ByteBuffer message) {
      Objects.requireNonNull(message, "message");
      byte[] data = new byte[message.remaining()];
      message.get(data);
      if (outputClosed) {
        return failed(new IOException("Output closed"));
      }
      if (data.length > 125) {
        return failed(new IllegalArgumentException("Message too long"));
      }
      return control(10, data);
    }

    @Override
    public CompletableFuture<WebSocket> sendClose(int statusCode, String reason) {
      Objects.requireNonNull(reason, "reason");
      byte[] reasonBytes = reason.getBytes(StandardCharsets.UTF_8);
      if (reasonBytes.length > 123) {
        return failed(new IllegalArgumentException("Reason is too long"));
      }
      if (!(statusCode == 1000 || statusCode == 1001 || statusCode == 1011
            || (statusCode >= 3000 && statusCode <= 4999))) {
        return failed(new IllegalArgumentException("Bad status code: " + statusCode));
      }
      byte[] payload = new byte[2 + reasonBytes.length];
      payload[0] = (byte) (statusCode >> 8);
      payload[1] = (byte) statusCode;
      System.arraycopy(reasonBytes, 0, payload, 2, reasonBytes.length);
      CompletableFuture<WebSocket> result = control(8, payload);
      outputClosed = true;
      return result;
    }

    private CompletableFuture<WebSocket> send(int opcode, byte[] data, boolean last) {
      if (outputClosed) {
        return failed(new IOException("Output closed"));
      }
      try {
        writeFrame(opcode, last, data);
        sendingFragments = !last;
        return CompletableFuture.completedFuture((WebSocket) this);
      } catch (IOException e) {
        return failed(e);
      }
    }

    private CompletableFuture<WebSocket> control(int opcode, byte[] data) {
      if (outputClosed) {
        return failed(new IOException("Output closed"));
      }
      try {
        writeFrame(opcode, true, data);
        return CompletableFuture.completedFuture((WebSocket) this);
      } catch (IOException e) {
        return failed(e);
      }
    }

    private CompletableFuture<WebSocket> failed(Throwable t) {
      CompletableFuture<WebSocket> f = new CompletableFuture<WebSocket>();
      f.completeExceptionally(t);
      return f;
    }

    private void writeFrame(int opcode, boolean fin, byte[] data) throws IOException {
      byte[] mask = new byte[4];
      random.nextBytes(mask);
      ByteArrayOutputStream frame = new ByteArrayOutputStream(data.length + 14);
      frame.write((fin ? 0x80 : 0) | opcode);
      if (data.length < 126) {
        frame.write(0x80 | data.length);
      } else if (data.length < 65536) {
        frame.write(0x80 | 126);
        frame.write(data.length >> 8);
        frame.write(data.length);
      } else {
        frame.write(0x80 | 127);
        long n = data.length;
        for (int shift = 56; shift >= 0; shift -= 8) {
          frame.write((int) (n >> shift));
        }
      }
      frame.write(mask, 0, 4);
      byte[] masked = new byte[data.length];
      for (int i = 0; i < data.length; ++i) {
        masked[i] = (byte) (data[i] ^ mask[i & 3]);
      }
      frame.write(masked, 0, masked.length);
      synchronized (writeLock) {
        out.write(frame.toByteArray());
        out.flush();
      }
    }

    @Override
    public void request(long n) {
      if (n <= 0) {
        throw new IllegalArgumentException("Request must be positive: " + n);
      }
      synchronized (demandLock) {
        demand += n;
        if (demand < 0) {
          demand = Long.MAX_VALUE;
        }
        demandLock.notifyAll();
      }
    }

    @Override
    public String getSubprotocol() {
      return subprotocol;
    }

    @Override
    public boolean isOutputClosed() {
      return outputClosed;
    }

    @Override
    public boolean isInputClosed() {
      return inputClosed;
    }

    @Override
    public void abort() {
      aborted = true;
      outputClosed = true;
      inputClosed = true;
      closeQuietly(socket);
      synchronized (demandLock) {
        demandLock.notifyAll();
      }
    }

    private boolean awaitDemand() {
      synchronized (demandLock) {
        while (demand == 0 && !aborted) {
          try {
            demandLock.wait();
          } catch (InterruptedException e) {
            return false;
          }
        }
        if (aborted) {
          return false;
        }
        if (demand != Long.MAX_VALUE) {
          --demand;
        }
        return true;
      }
    }

    private void awaitStage(CompletionStage<?> stage) {
      if (stage != null) {
        try {
          stage.toCompletableFuture().join();
        } catch (RuntimeException e) {
          fail(e);
        }
      }
    }

    private void fail(Throwable t) {
      if (aborted) {
        return;
      }
      inputClosed = true;
      try {
        listener.onError(this, t);
      } catch (Throwable ignore) { }
    }

    private void readLoop() {
      try {
        listener.onOpen(this);
        while (!inputClosed && !aborted) {
          int b0 = in.read();
          if (b0 < 0) {
            throw new IOException("Connection closed");
          }
          int b1 = in.read();
          if (b1 < 0) {
            throw new IOException("Connection closed");
          }
          boolean fin = (b0 & 0x80) != 0;
          int opcode = b0 & 0x0f;
          boolean masked = (b1 & 0x80) != 0;
          long length = b1 & 0x7f;
          if (length == 126) {
            length = (readByte() << 8) | readByte();
          } else if (length == 127) {
            length = 0;
            for (int i = 0; i < 8; ++i) {
              length = (length << 8) | readByte();
            }
          }
          byte[] mask = null;
          if (masked) {
            mask = new byte[4];
            readFully(mask);
          }
          if (length > Integer.MAX_VALUE - 8) {
            throw new IOException("Frame too large");
          }
          byte[] payload = new byte[(int) length];
          readFully(payload);
          if (mask != null) {
            for (int i = 0; i < payload.length; ++i) {
              payload[i] ^= mask[i & 3];
            }
          }
          if (!dispatch(opcode, fin, payload)) {
            break;
          }
        }
      } catch (IOException e) {
        fail(e);
      } catch (Throwable t) {
        fail(t);
      } finally {
        inputClosed = true;
        if (!aborted && outputClosed) {
          closeQuietly(socket);
        }
      }
    }

    private int readByte() throws IOException {
      int b = in.read();
      if (b < 0) {
        throw new IOException("Connection closed");
      }
      return b;
    }

    private void readFully(byte[] buffer) throws IOException {
      int off = 0;
      while (off < buffer.length) {
        int n = in.read(buffer, off, buffer.length - off);
        if (n < 0) {
          throw new IOException("Connection closed");
        }
        off += n;
      }
    }

    private boolean dispatch(int opcode, boolean fin, byte[] payload) throws IOException {
      switch (opcode) {
      case 1:
      case 2:
      case 0: {
        boolean text = opcode == 1 || (opcode == 0 && receivingText);
        if (opcode == 1) {
          receivingText = !fin;
        } else if (opcode == 2) {
          receivingBinary = !fin;
        } else if (fin) {
          receivingText = false;
          receivingBinary = false;
        }
        if (!awaitDemand()) {
          return false;
        }
        CompletionStage<?> stage;
        if (text) {
          ByteBuffer input = ByteBuffer.allocate(pendingBytes.remaining() + payload.length);
          input.put(pendingBytes).put(payload).flip();
          CharBuffer chars = CharBuffer.allocate(input.remaining() + 1);
          java.nio.charset.CoderResult decoded = decoder.decode(input, chars, fin);
          if (decoded.isError()) {
            throw new IOException("Malformed text");
          }
          if (fin) {
            decoder.flush(chars);
            decoder.reset();
          }
          chars.flip();
          pendingBytes = ByteBuffer.allocate(input.remaining());
          pendingBytes.put(input).flip();
          stage = listener.onText(this, chars.toString(), fin);
        } else {
          stage = listener.onBinary(this, ByteBuffer.wrap(payload), fin);
        }
        awaitStage(stage);
        return true;
      }
      case 8: {
        int status = 1005;
        String reason = "";
        if (payload.length >= 2) {
          status = ((payload[0] & 0xff) << 8) | (payload[1] & 0xff);
          reason = new String(payload, 2, payload.length - 2, StandardCharsets.UTF_8);
        }
        inputClosed = true;
        if (!awaitDemandOrSkip()) {
          return false;
        }
        CompletionStage<?> stage = listener.onClose(this, status, reason);
        awaitStage(stage);
        if (!outputClosed) {
          outputClosed = true;
          try {
            byte[] reply = payload.length >= 2 ? new byte[] {payload[0], payload[1]} : new byte[0];
            writeFrame(8, true, reply);
          } catch (IOException ignore) { }
        }
        closeQuietly(socket);
        return false;
      }
      case 9: {
        if (!awaitDemand()) {
          return false;
        }
        try {
          if (!outputClosed) {
            writeFrame(10, true, payload);
          }
        } catch (IOException ignore) { }
        awaitStage(listener.onPing(this, ByteBuffer.wrap(payload)));
        return true;
      }
      case 10: {
        if (!awaitDemand()) {
          return false;
        }
        awaitStage(listener.onPong(this, ByteBuffer.wrap(payload)));
        return true;
      }
      default:
        throw new IOException("Unexpected opcode " + opcode);
      }
    }

    private boolean awaitDemandOrSkip() {
      return !aborted;
    }

    @Override
    public String toString() {
      return super.toString() + "[" + (inputClosed ? "input closed" : "open") + "]";
    }
  }
}
