/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 * SPDX-License-Identifier: MIT
 */

package sun.net.httpserver;

import com.sun.net.httpserver.Authenticator;
import com.sun.net.httpserver.Filter;
import com.sun.net.httpserver.Headers;
import com.sun.net.httpserver.HttpContext;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;
import com.sun.net.httpserver.HttpsConfigurator;
import com.sun.net.httpserver.HttpsParameters;
import java.io.BufferedInputStream;
import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.BindException;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executor;
import java.util.concurrent.RejectedExecutionException;
import javax.net.ssl.SSLParameters;
import javax.net.ssl.SSLSession;
import javax.net.ssl.SSLSocket;

class ServerImpl {
  static final long kIdleMillis = 30000;
  static final int kChunkSize = 4096;
  static final long kDrainAmount = 64 * 1024;
  private static final System.Logger kLogger = System.getLogger("com.sun.net.httpserver");

  private final HttpServer wrapper;
  private final String protocol;
  private final boolean https;
  private final List<HttpContextImpl> contexts = new ArrayList<HttpContextImpl>();
  private final Set<Connection> connections = ConcurrentHashMap.newKeySet();
  private final Object stateLock = new Object();
  private ServerSocket serverSocket;
  private volatile Executor executor;
  private volatile HttpsConfigurator configurator;
  private Thread dispatcher;
  private boolean bound;
  private boolean started;
  private volatile boolean terminating;
  private boolean finished;
  private int active;

  ServerImpl(HttpServer wrapper, String protocol, InetSocketAddress addr, int backlog) throws IOException {
    this.wrapper = wrapper;
    this.protocol = protocol;
    this.https = protocol.equalsIgnoreCase("https");
    if (addr != null) {
      bind(addr, backlog);
    }
  }

  HttpServer getWrapper() {
    return wrapper;
  }

  void bind(InetSocketAddress addr, int backlog) throws IOException {
    synchronized (stateLock) {
      if (bound) {
        throw new BindException("HttpServer already bound");
      }
      if (addr == null) {
        throw new NullPointerException("null address");
      }
      ServerSocket ss = new ServerSocket();
      try {
        ss.setReuseAddress(true);
        ss.bind(addr, backlog);
      } catch (IOException e) {
        ss.close();
        throw e;
      }
      serverSocket = ss;
      bound = true;
    }
  }

  void start() {
    synchronized (stateLock) {
      if (!bound || started || finished) {
        throw new IllegalStateException("server in wrong state");
      }
      started = true;
      dispatcher = new Thread(this::acceptLoop, "HTTP-Dispatcher");
      dispatcher.start();
    }
  }

  void setExecutor(Executor executor) {
    synchronized (stateLock) {
      if (started) {
        throw new IllegalStateException("server already started");
      }
      this.executor = executor;
    }
  }

  Executor getExecutor() {
    return executor;
  }

  void setHttpsConfigurator(HttpsConfigurator config) {
    if (config == null) {
      throw new NullPointerException("null HttpsConfigurator");
    }
    if (started) {
      throw new IllegalStateException("server already started");
    }
    configurator = config;
  }

  HttpsConfigurator getHttpsConfigurator() {
    return configurator;
  }

  InetSocketAddress getAddress() {
    synchronized (stateLock) {
      if (!bound) {
        return null;
      }
      return (InetSocketAddress) serverSocket.getLocalSocketAddress();
    }
  }

  void stop(int delay) {
    if (delay < 0) {
      throw new IllegalArgumentException("negative delay parameter");
    }
    Thread toJoin;
    synchronized (stateLock) {
      terminating = true;
      if (serverSocket != null) {
        try {
          serverSocket.close();
        } catch (IOException ignore) { }
      }
      toJoin = dispatcher;
    }
    long deadline = System.nanoTime() + delay * 1000000000L;
    synchronized (stateLock) {
      while (active > 0 && System.nanoTime() < deadline) {
        try {
          stateLock.wait(50);
        } catch (InterruptedException e) {
          Thread.currentThread().interrupt();
          break;
        }
      }
    }
    for (Connection c : connections) {
      c.close();
    }
    if (toJoin != null && toJoin != Thread.currentThread()) {
      try {
        toJoin.join(1000);
      } catch (InterruptedException e) {
        Thread.currentThread().interrupt();
      }
    }
    synchronized (stateLock) {
      finished = true;
    }
  }

  HttpContext createContext(String path, HttpHandler handler) {
    if (handler == null || path == null) {
      throw new NullPointerException("null handler, or path parameter");
    }
    HttpContextImpl context = new HttpContextImpl(protocol, path, handler, this);
    addContext(context);
    return context;
  }

  HttpContext createContext(String path) {
    if (path == null) {
      throw new NullPointerException("null path parameter");
    }
    HttpContextImpl context = new HttpContextImpl(protocol, path, null, this);
    addContext(context);
    return context;
  }

  private void addContext(HttpContextImpl context) {
    synchronized (contexts) {
      for (HttpContextImpl c : contexts) {
        if (c.getProtocol().equals(context.getProtocol()) && c.getPath().equals(context.getPath())) {
          throw new IllegalArgumentException("cannot add context to list");
        }
      }
      contexts.add(context);
    }
  }

  void removeContext(String path) {
    if (path == null) {
      throw new NullPointerException("null path parameter");
    }
    synchronized (contexts) {
      for (HttpContextImpl c : contexts) {
        if (c.getProtocol().equals(protocol) && c.getPath().equals(path)) {
          contexts.remove(c);
          return;
        }
      }
    }
    throw new IllegalArgumentException("cannot remove element from list");
  }

  void removeContext(HttpContext context) {
    if (!(context instanceof HttpContextImpl)) {
      throw new IllegalArgumentException("wrong HttpContext type");
    }
    synchronized (contexts) {
      if (!contexts.remove(context)) {
        throw new IllegalArgumentException("cannot remove element from list");
      }
    }
  }

  private HttpContextImpl findContext(String path) {
    HttpContextImpl best = null;
    synchronized (contexts) {
      for (HttpContextImpl c : contexts) {
        if (c.getProtocol().equals(protocol) && path.startsWith(c.getPath())
            && (best == null || c.getPath().length() > best.getPath().length())) {
          best = c;
        }
      }
    }
    return best;
  }

  private void acceptLoop() {
    ServerSocket ss = serverSocket;
    while (!terminating) {
      Socket socket;
      try {
        socket = ss.accept();
      } catch (IOException e) {
        if (terminating || ss.isClosed()) {
          break;
        }
        kLogger.log(System.Logger.Level.TRACE, "accept failed", e);
        continue;
      }
      try {
        socket.setTcpNoDelay(true);
        Connection c = new Connection(socket);
        connections.add(c);
        Thread t = new Thread(c, "HTTP-Dispatcher");
        t.setDaemon(true);
        t.start();
      } catch (IOException e) {
        try {
          socket.close();
        } catch (IOException ignore) { }
      }
    }
  }

  private void exchangeStarted() {
    synchronized (stateLock) {
      active++;
    }
  }

  private void exchangeEnded() {
    synchronized (stateLock) {
      active--;
      stateLock.notifyAll();
    }
  }

  final class Connection implements Runnable {
    private Socket socket;
    private InputStream socketIn;
    private OutputStream socketOut;
    private RequestInput input;
    private final Socket plain;
    private volatile boolean closed;
    private SSLSocket ssl;

    Connection(Socket plain) {
      this.plain = plain;
      this.socket = plain;
    }

    InetSocketAddress remoteAddress() {
      return (InetSocketAddress) plain.getRemoteSocketAddress();
    }

    InetSocketAddress localAddress() {
      return (InetSocketAddress) plain.getLocalSocketAddress();
    }

    SSLSession sslSession() {
      return ssl == null ? null : ssl.getSession();
    }

    synchronized void writeRaw(String text) throws IOException {
      byte[] bytes = text.getBytes(StandardCharsets.ISO_8859_1);
      write(bytes, 0, bytes.length);
      flush();
    }

    synchronized void write(byte[] b, int off, int len) throws IOException {
      if (closed) {
        throw new IOException("connection closed");
      }
      socketOut.write(b, off, len);
    }

    synchronized void flush() throws IOException {
      if (!closed) {
        socketOut.flush();
      }
    }

    void close() {
      closed = true;
      try {
        socket.close();
      } catch (IOException ignore) { }
      try {
        plain.close();
      } catch (IOException ignore) { }
      connections.remove(this);
    }

    private void setUpStreams() throws IOException {
      if (https) {
        HttpsConfigurator cfg = configurator;
        if (cfg == null) {
          throw new IOException("no HttpsConfigurator set");
        }
        SSLSocket s = (SSLSocket) cfg.getSSLContext().getSocketFactory().createSocket(plain, null,
            plain.getPort(), true);
        s.setUseClientMode(false);
        final Connection self = this;
        HttpsParameters params = new HttpsParameters() {
          private SSLParameters sslParams;

          @Override
          public com.sun.net.httpserver.HttpsConfigurator getHttpsConfigurator() {
            return cfg;
          }

          @Override
          public InetSocketAddress getClientAddress() {
            return self.remoteAddress();
          }

          @Override
          public void setSSLParameters(SSLParameters p) {
            sslParams = p;
          }

          SSLParameters finish() {
            return sslParams;
          }
        };
        cfg.configure(params);
        SSLParameters p = new SSLParameters();
        if (params.getCipherSuites() != null) {
          p.setCipherSuites(params.getCipherSuites());
        }
        if (params.getProtocols() != null) {
          p.setProtocols(params.getProtocols());
        }
        p.setNeedClientAuth(params.getNeedClientAuth());
        p.setWantClientAuth(params.getWantClientAuth());
        s.setSSLParameters(p);
        ssl = s;
        socket = s;
      }
      socketIn = socket.getInputStream();
      socketOut = new java.io.BufferedOutputStream(socket.getOutputStream(), 8192);
      input = new RequestInput(new BufferedInputStream(socketIn, 8192));
    }

    @Override
    public void run() {
      try {
        setUpStreams();
        while (!closed && !terminating) {
          if (!serveOne()) {
            break;
          }
        }
      } catch (Throwable t) {
        kLogger.log(System.Logger.Level.TRACE, "connection failed", t);
      } finally {
        close();
      }
    }

    private void reject(int code, String message) {
      String body = "<h1>" + code + " " + Responses.get(code) + "</h1>" + message;
      try {
        writeRaw("HTTP/1.1 " + code + " " + Responses.get(code) + "\r\nContent-Length: " + body.length()
            + "\r\nContent-Type: text/html\r\nConnection: close\r\n\r\n" + body);
      } catch (IOException ignore) { }
      close();
    }

    /** Serves one request; returns whether the connection can be reused. */
    private boolean serveOne() throws IOException, InterruptedException {
      String line;
      try {
        socket.setSoTimeout((int) kIdleMillis);
        line = input.readLine();
        while (line != null && line.isEmpty()) {
          line = input.readLine();
        }
        socket.setSoTimeout(0);
      } catch (SocketTimeoutException | EOFException e) {
        return false;
      } catch (SocketException e) {
        return false;
      }
      if (line == null) {
        return false;
      }
      int first = line.indexOf(' ');
      int second = first < 0 ? -1 : line.indexOf(' ', first + 1);
      if (first < 0 || second < 0) {
        reject(400, "Bad request line");
        return false;
      }
      String method = line.substring(0, first);
      String uriString = line.substring(first + 1, second);
      String version = line.substring(second + 1);
      Headers headers = new Headers();
      String lastKey = null;
      for (;;) {
        String h = input.readLine();
        if (h == null) {
          return false;
        }
        if (h.isEmpty()) {
          break;
        }
        char c0 = h.charAt(0);
        if ((c0 == ' ' || c0 == '\t') && lastKey != null) {
          List<String> values = headers.get(lastKey);
          int last = values.size() - 1;
          values.set(last, values.get(last) + " " + h.trim());
          continue;
        }
        int colon = h.indexOf(':');
        if (colon <= 0) {
          reject(400, "Header key contains illegal characters");
          return false;
        }
        String key = h.substring(0, colon);
        for (int i = 0; i < key.length(); i++) {
          if (!Utils.isTokenChar(key.charAt(i))) {
            reject(400, "Header key contains illegal characters");
            return false;
          }
        }
        String value = h.substring(colon + 1).trim();
        try {
          headers.add(key, value);
        } catch (IllegalArgumentException e) {
          reject(400, "Header key contains illegal characters");
          return false;
        }
        lastKey = normalizedKey(headers, key);
      }
      long length = 0;
      boolean chunked = false;
      String te = headers.getFirst("Transfer-Encoding");
      List<String> lengths = headers.get("Content-Length");
      if (te != null) {
        if (lengths != null) {
          reject(400, "Conflicting or malformed headers detected");
          return false;
        }
        if (te.equalsIgnoreCase("chunked")) {
          chunked = true;
        } else {
          reject(501, "Unsupported Transfer-Encoding value");
          return false;
        }
      } else if (lengths != null) {
        if (lengths.size() > 1) {
          reject(400, "Conflicting or malformed headers detected");
          return false;
        }
        try {
          length = Long.parseLong(lengths.get(0).trim());
        } catch (NumberFormatException e) {
          reject(400, "NumberFormatException thrown");
          return false;
        }
        if (length < 0) {
          reject(400, "Illegal Content-Length value");
          return false;
        }
      }
      URI uri;
      try {
        uri = new URI(uriString);
      } catch (URISyntaxException e) {
        reject(400, "Bad request URI");
        return false;
      }
      if (uri.getScheme() == null && uri.getRawAuthority() != null) {
        reject(400, "Bad request URI");
        return false;
      }
      if (uri.getPath() == null) {
        return false;
      }
      String path = uri.getPath();
      HttpContextImpl context = findContext(path);
      if (context == null) {
        reject(404, "No context found for request");
        return false;
      }
      if (context.getHandler() == null) {
        reject(500, "No handler for context");
        return false;
      }
      ExchangeImpl impl = new ExchangeImpl(this, method, uri, version, headers, context, length, chunked, input);
      HttpExchange exchange = https ? new HttpsExchangeImpl(impl) : impl;
      exchangeStarted();
      try {
        Runnable task = () -> runExchange(impl, exchange, context);
        Executor ex = executor;
        if (ex == null) {
          task.run();
        } else {
          try {
            ex.execute(task);
          } catch (RejectedExecutionException e) {
            impl.markConnectionClosed();
          }
        }
        impl.awaitDone();
      } finally {
        exchangeEnded();
      }
      return !impl.shouldCloseConnection() && !closed;
    }

    private String normalizedKey(Headers headers, String key) {
      for (String k : headers.keySet()) {
        if (k.equalsIgnoreCase(key)) {
          return k;
        }
      }
      return key;
    }

    private void runExchange(ExchangeImpl impl, HttpExchange exchange, HttpContextImpl context) {
      try {
        HttpHandler terminal = new HttpHandler() {
          @Override
          public void handle(HttpExchange e) throws IOException {
            Authenticator auth = context.getAuthenticator();
            if (auth != null) {
              Authenticator.Result r = auth.authenticate(e);
              if (r instanceof Authenticator.Failure) {
                e.sendResponseHeaders(((Authenticator.Failure) r).getResponseCode(), -1);
                e.close();
                return;
              } else if (r instanceof Authenticator.Retry) {
                e.sendResponseHeaders(((Authenticator.Retry) r).getResponseCode(), -1);
                e.close();
                return;
              } else if (r instanceof Authenticator.Success) {
                impl.setPrincipal(((Authenticator.Success) r).getPrincipal());
              }
            }
            context.getHandler().handle(e);
          }
        };
        Filter.Chain chain = new Filter.Chain(new ArrayList<Filter>(context.getFilters()), terminal);
        chain.doFilter(exchange);
      } catch (Throwable t) {
        kLogger.log(System.Logger.Level.TRACE, "exchange failed", t);
        impl.markConnectionClosed();
        close();
      }
    }
  }

  static final class RequestInput {
    private final BufferedInputStream in;

    RequestInput(BufferedInputStream in) {
      this.in = in;
    }

    String readLine() throws IOException {
      StringBuilder sb = new StringBuilder();
      boolean sawCr = false;
      int c;
      while ((c = in.read()) != -1) {
        if (sawCr && c == '\n') {
          sb.setLength(sb.length() - 1);
          return sb.toString();
        }
        sawCr = c == '\r';
        sb.append((char) c);
        if (sb.length() > 512 * 1024) {
          throw new IOException("header line too long");
        }
      }
      if (sb.length() == 0) {
        return null;
      }
      throw new EOFException("connection closed in the middle of a line");
    }

    int read() throws IOException {
      return in.read();
    }

    int read(byte[] b, int off, int len) throws IOException {
      return in.read(b, off, len);
    }

    int available() throws IOException {
      return in.available();
    }
  }

  abstract static class LeftOverInput extends InputStream {
    protected final RequestInput in;
    protected final ExchangeImpl exchange;
    private boolean closed;
    private boolean continued;

    LeftOverInput(RequestInput in, ExchangeImpl exchange) {
      this.in = in;
      this.exchange = exchange;
    }

    protected final void checkOpen() throws IOException {
      if (closed) {
        throw new IOException("stream is closed");
      }
      if (!continued) {
        continued = true;
        exchange.sendContinueIfNeeded();
      }
    }

    abstract boolean drain(long limit) throws IOException;

    @Override
    public void close() throws IOException {
      if (closed) {
        return;
      }
      closed = true;
      if (!drain(kDrainAmount)) {
        exchange.closeAfterResponse();
      }
    }

    @Override
    public int read() throws IOException {
      byte[] one = new byte[1];
      int n = read(one, 0, 1);
      return n <= 0 ? -1 : one[0] & 0xFF;
    }
  }

  static final class FixedInput extends LeftOverInput {
    private long remaining;

    FixedInput(RequestInput in, long length, ExchangeImpl exchange) {
      super(in, exchange);
      this.remaining = length;
    }

    @Override
    public int read(byte[] b, int off, int len) throws IOException {
      checkOpen();
      if (len == 0) {
        return 0;
      }
      if (remaining == 0) {
        return -1;
      }
      int n = in.read(b, off, (int) Math.min(len, remaining));
      if (n < 0) {
        throw new IOException("connection closed before all data received");
      }
      remaining -= n;
      return n;
    }

    @Override
    public int available() throws IOException {
      return (int) Math.min(remaining, in.available());
    }

    @Override
    boolean drain(long limit) throws IOException {
      if (remaining > limit) {
        return false;
      }
      byte[] buf = new byte[1024];
      while (remaining > 0) {
        int n = in.read(buf, 0, (int) Math.min(buf.length, remaining));
        if (n < 0) {
          return false;
        }
        remaining -= n;
      }
      return true;
    }
  }

  static final class ChunkedInput extends LeftOverInput {
    private long chunkRemaining;
    private boolean eof;
    private boolean started;

    ChunkedInput(RequestInput in, ExchangeImpl exchange) {
      super(in, exchange);
    }

    private void nextChunk() throws IOException {
      if (started) {
        String crlf = in.readLine();
        if (crlf == null || !crlf.isEmpty()) {
          throw new IOException("missing chunk terminator");
        }
      }
      started = true;
      String size = in.readLine();
      if (size == null) {
        throw new IOException("connection closed before all data received");
      }
      long n;
      try {
        n = Long.parseLong(size.trim(), 16);
      } catch (NumberFormatException e) {
        throw new IOException("invalid chunk length: " + size);
      }
      if (n < 0) {
        throw new IOException("invalid chunk length: " + size);
      }
      if (n == 0) {
        eof = true;
        String trailer;
        while ((trailer = in.readLine()) != null && !trailer.isEmpty()) {
          continue;
        }
        return;
      }
      chunkRemaining = n;
    }

    @Override
    public int read(byte[] b, int off, int len) throws IOException {
      checkOpen();
      if (len == 0) {
        return 0;
      }
      if (eof) {
        return -1;
      }
      if (chunkRemaining == 0) {
        nextChunk();
        if (eof) {
          return -1;
        }
      }
      int n = in.read(b, off, (int) Math.min(len, chunkRemaining));
      if (n < 0) {
        throw new IOException("connection closed before all data received");
      }
      chunkRemaining -= n;
      if (chunkRemaining == 0 && !eof) {
        String crlf = in.readLine();
        if (crlf == null || !crlf.isEmpty()) {
          throw new IOException("missing chunk terminator");
        }
        started = false;
      }
      return n;
    }

    @Override
    boolean drain(long limit) throws IOException {
      byte[] buf = new byte[1024];
      long total = 0;
      while (!eof) {
        if (chunkRemaining == 0) {
          nextChunk();
          continue;
        }
        int n = in.read(buf, 0, (int) Math.min(buf.length, chunkRemaining));
        if (n < 0) {
          return false;
        }
        chunkRemaining -= n;
        total += n;
        if (chunkRemaining == 0) {
          String crlf = in.readLine();
          if (crlf == null) {
            return false;
          }
          started = false;
        }
        if (total > limit) {
          return false;
        }
      }
      return true;
    }
  }

  static final class SwitchableOutput extends OutputStream {
    private OutputStream target;

    synchronized void set(OutputStream out) {
      target = out;
    }

    synchronized void finish() throws IOException {
      if (target != null) {
        target.close();
      }
    }

    private synchronized OutputStream target() throws IOException {
      if (target == null) {
        throw new IOException("response headers not sent yet");
      }
      return target;
    }

    @Override
    public void write(int b) throws IOException {
      target().write(b);
    }

    @Override
    public void write(byte[] b, int off, int len) throws IOException {
      target().write(b, off, len);
    }

    @Override
    public void flush() throws IOException {
      OutputStream t;
      synchronized (this) {
        t = target;
      }
      if (t != null) {
        t.flush();
      }
    }

    @Override
    public void close() throws IOException {
      OutputStream t;
      synchronized (this) {
        t = target;
      }
      if (t != null) {
        t.close();
      }
    }
  }

  abstract static class ResponseOutput extends OutputStream {
    protected final Connection connection;
    protected final ExchangeImpl exchange;
    protected boolean closed;

    ResponseOutput(Connection connection, ExchangeImpl exchange) {
      this.connection = connection;
      this.exchange = exchange;
    }

    protected final void checkOpen() throws IOException {
      if (closed) {
        throw new IOException("stream closed");
      }
    }

    @Override
    public void write(int b) throws IOException {
      write(new byte[] {(byte) b}, 0, 1);
    }

    @Override
    public void flush() throws IOException {
      if (!closed) {
        connection.flush();
      }
    }
  }

  static final class FixedOutput extends ResponseOutput {
    private long remaining;

    FixedOutput(Connection connection, ExchangeImpl exchange, long length) {
      super(connection, exchange);
      this.remaining = length;
    }

    @Override
    public void write(byte[] b, int off, int len) throws IOException {
      checkOpen();
      if (len == 0) {
        return;
      }
      if (len > remaining) {
        throw new IOException("too many bytes to write to stream");
      }
      connection.write(b, off, len);
      remaining -= len;
    }

    @Override
    public void close() throws IOException {
      if (closed) {
        return;
      }
      closed = true;
      if (remaining > 0) {
        throw new IOException("insufficient bytes written to stream");
      }
      connection.flush();
      exchange.complete();
    }
  }

  static final class ChunkedOutput extends ResponseOutput {
    private final byte[] buffer = new byte[kChunkSize];
    private int count;

    ChunkedOutput(Connection connection, ExchangeImpl exchange) {
      super(connection, exchange);
    }

    private void writeChunk() throws IOException {
      if (count > 0) {
        connection.write((Integer.toHexString(count) + "\r\n").getBytes(StandardCharsets.ISO_8859_1), 0,
            Integer.toHexString(count).length() + 2);
        connection.write(buffer, 0, count);
        connection.write(new byte[] {'\r', '\n'}, 0, 2);
        count = 0;
      }
    }

    @Override
    public void write(byte[] b, int off, int len) throws IOException {
      checkOpen();
      while (len > 0) {
        int n = Math.min(len, buffer.length - count);
        System.arraycopy(b, off, buffer, count, n);
        count += n;
        off += n;
        len -= n;
        if (count == buffer.length) {
          writeChunk();
        }
      }
    }

    @Override
    public void flush() throws IOException {
      if (!closed) {
        writeChunk();
        connection.flush();
      }
    }

    @Override
    public void close() throws IOException {
      if (closed) {
        return;
      }
      writeChunk();
      closed = true;
      connection.write(new byte[] {'0', '\r', '\n', '\r', '\n'}, 0, 5);
      connection.flush();
      exchange.complete();
    }
  }

  static final class UndefLengthOutput extends ResponseOutput {
    UndefLengthOutput(Connection connection, ExchangeImpl exchange) {
      super(connection, exchange);
    }

    @Override
    public void write(byte[] b, int off, int len) throws IOException {
      checkOpen();
      connection.write(b, off, len);
    }

    @Override
    public void close() throws IOException {
      if (closed) {
        return;
      }
      closed = true;
      connection.flush();
      exchange.markConnectionClosed();
    }
  }
}
