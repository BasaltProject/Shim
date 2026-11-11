/*
 * Copyright (c) 2026 BasaltMC.
 *
 * Licensed under the MIT License; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package java.net.http;

import java.io.Closeable;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Authenticator;
import java.net.CookieHandler;
import java.net.HttpURLConnection;
import java.net.Proxy;
import java.net.ProxySelector;
import java.net.URI;
import java.net.URL;
import java.net.URLConnection;
import java.nio.ByteBuffer;
import java.time.Duration;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.concurrent.Flow;
import java.util.concurrent.ForkJoinPool;

public abstract class HttpClient implements AutoCloseable {

  protected HttpClient() {}

  public enum Version {
    HTTP_1_1, HTTP_2
  }

  public enum Redirect {
    NEVER, ALWAYS, NORMAL
  }

  public interface Builder {
    public static final ProxySelector NO_PROXY = ProxySelector.of(null);

    default Builder localAddress(java.net.InetAddress localAddr) {
      throw new UnsupportedOperationException();
    }

    Builder sslContext(javax.net.ssl.SSLContext sslContext);
    Builder sslParameters(javax.net.ssl.SSLParameters sslParameters);
    Builder cookieHandler(CookieHandler cookieHandler);
    Builder connectTimeout(Duration duration);
    Builder followRedirects(Redirect policy);
    Builder version(Version version);
    Builder priority(int priority);
    Builder proxy(ProxySelector proxySelector);
    Builder authenticator(Authenticator authenticator);
    Builder executor(Executor executor);
    HttpClient build();
  }

  public static HttpClient newHttpClient() {
    return newBuilder().build();
  }

  public static Builder newBuilder() {
    return new BuilderImpl();
  }

  public abstract javax.net.ssl.SSLContext sslContext();
  public abstract javax.net.ssl.SSLParameters sslParameters();
  public abstract Optional<CookieHandler> cookieHandler();
  public abstract Optional<Duration> connectTimeout();
  public abstract Redirect followRedirects();
  public abstract Optional<ProxySelector> proxy();
  public abstract Optional<Authenticator> authenticator();
  public abstract Version version();
  public abstract Optional<Executor> executor();

  public abstract <T> HttpResponse<T> send(HttpRequest request, HttpResponse.BodyHandler<T> responseBodyHandler)
      throws IOException, InterruptedException;

  public abstract <T> CompletableFuture<HttpResponse<T>> sendAsync(
      HttpRequest request, HttpResponse.BodyHandler<T> responseBodyHandler);

  public abstract <T> CompletableFuture<HttpResponse<T>> sendAsync(
      HttpRequest request, HttpResponse.BodyHandler<T> responseBodyHandler, HttpResponse.PushPromiseHandler<T> pushPromiseHandler);

  public WebSocket.Builder newWebSocketBuilder() {
    return new WebSocketClient.BuilderImpl(this, executor().orElse(null));
  }

  @Override
  public void close() {
    shutdown();
  }

  public void shutdown() {}

  public void shutdownNow() {}

  public boolean awaitTermination(Duration duration) throws InterruptedException {
    return true;
  }

  public boolean isTerminated() {
    return true;
  }

  private static class BuilderImpl implements Builder {
    private CookieHandler cookieHandler;
    private Duration connectTimeout;
    private Redirect followRedirects = Redirect.NEVER;
    private Version version = Version.HTTP_2;
    private int priority;
    private ProxySelector proxySelector;
    private Authenticator authenticator;
    private Executor executor;
    private javax.net.ssl.SSLContext sslContext;
    private javax.net.ssl.SSLParameters sslParameters;
    private java.net.InetAddress localAddress;

    @Override
    public Builder localAddress(java.net.InetAddress localAddr) {
      this.localAddress = localAddr;
      return this;
    }

    @Override
    public Builder sslContext(javax.net.ssl.SSLContext sslContext) {
      this.sslContext = Objects.requireNonNull(sslContext);
      return this;
    }

    @Override
    public Builder sslParameters(javax.net.ssl.SSLParameters sslParameters) {
      this.sslParameters = Objects.requireNonNull(sslParameters);
      return this;
    }

    @Override
    public Builder cookieHandler(CookieHandler cookieHandler) {
      this.cookieHandler = Objects.requireNonNull(cookieHandler);
      return this;
    }

    @Override
    public Builder connectTimeout(Duration duration) {
      this.connectTimeout = Objects.requireNonNull(duration);
      return this;
    }

    @Override
    public Builder followRedirects(Redirect policy) {
      this.followRedirects = Objects.requireNonNull(policy);
      return this;
    }

    @Override
    public Builder version(Version version) {
      this.version = Objects.requireNonNull(version);
      return this;
    }

    @Override
    public Builder priority(int priority) {
      if (priority < 1 || priority > 256) throw new IllegalArgumentException();
      this.priority = priority;
      return this;
    }

    @Override
    public Builder proxy(ProxySelector proxySelector) {
      this.proxySelector = Objects.requireNonNull(proxySelector);
      return this;
    }

    @Override
    public Builder authenticator(Authenticator authenticator) {
      this.authenticator = Objects.requireNonNull(authenticator);
      return this;
    }

    @Override
    public Builder executor(Executor executor) {
      this.executor = Objects.requireNonNull(executor);
      return this;
    }

    @Override
    public HttpClient build() {
      return new HttpClientImpl(this);
    }
  }

  private static class HttpClientImpl extends HttpClient {
    private final Optional<CookieHandler> cookieHandler;
    private final Optional<Duration> connectTimeout;
    private final Redirect followRedirects;
    private final Version version;
    private final Optional<ProxySelector> proxy;
    private final Optional<Authenticator> authenticator;
    private final Optional<Executor> executor;
    private final javax.net.ssl.SSLContext sslContext;
    private final javax.net.ssl.SSLParameters sslParameters;

    HttpClientImpl(BuilderImpl b) {
      javax.net.ssl.SSLContext context = b.sslContext;
      if (context == null) {
        try {
          context = javax.net.ssl.SSLContext.getDefault();
        } catch (java.security.NoSuchAlgorithmException e) {
          throw new UncheckedIOException(new IOException(e));
        }
      }
      this.sslContext = context;
      this.sslParameters = b.sslParameters != null ? b.sslParameters : new javax.net.ssl.SSLParameters();
      this.cookieHandler = Optional.ofNullable(b.cookieHandler);
      this.connectTimeout = Optional.ofNullable(b.connectTimeout);
      this.followRedirects = b.followRedirects;
      this.version = b.version;
      this.proxy = Optional.ofNullable(b.proxySelector);
      this.authenticator = Optional.ofNullable(b.authenticator);
      this.executor = Optional.ofNullable(b.executor);
    }

    @Override
    public javax.net.ssl.SSLContext sslContext() {
      return sslContext;
    }

    @Override
    public javax.net.ssl.SSLParameters sslParameters() {
      javax.net.ssl.SSLParameters p = sslParameters;
      javax.net.ssl.SSLParameters copy = new javax.net.ssl.SSLParameters(p.getCipherSuites(), p.getProtocols());
      copy.setWantClientAuth(p.getWantClientAuth());
      copy.setNeedClientAuth(p.getNeedClientAuth());
      copy.setEndpointIdentificationAlgorithm(p.getEndpointIdentificationAlgorithm());
      copy.setUseCipherSuitesOrder(p.getUseCipherSuitesOrder());
      copy.setApplicationProtocols(p.getApplicationProtocols());
      if (p.getServerNames() != null) {
        copy.setServerNames(p.getServerNames());
      }
      return copy;
    }

    @Override
    public Optional<CookieHandler> cookieHandler() {
      return cookieHandler;
    }

    @Override
    public Optional<Duration> connectTimeout() {
      return connectTimeout;
    }

    @Override
    public Redirect followRedirects() {
      return followRedirects;
    }

    @Override
    public Optional<ProxySelector> proxy() {
      return proxy;
    }

    @Override
    public Optional<Authenticator> authenticator() {
      return authenticator;
    }

    @Override
    public Version version() {
      return version;
    }

    @Override
    public Optional<Executor> executor() {
      return executor;
    }

    @Override
    public <T> HttpResponse<T> send(HttpRequest request, HttpResponse.BodyHandler<T> responseBodyHandler)
        throws IOException, InterruptedException
    {
      try {
        return send0(request, responseBodyHandler);
      } catch (java.net.SocketTimeoutException e) {
        String m = e.getMessage();
        if (m != null && m.startsWith("connect")) {
          throw new HttpConnectTimeoutException("HTTP connect timed out");
        }
        throw new HttpTimeoutException("request timed out");
      }
    }

    private <T> HttpResponse<T> send0(HttpRequest request, HttpResponse.BodyHandler<T> responseBodyHandler)
        throws IOException, InterruptedException
    {
      Objects.requireNonNull(request, "request");
      Objects.requireNonNull(responseBodyHandler, "responseBodyHandler");

      URI uri = request.uri();
      URL url = uri.toURL();
      Proxy p = null;
      if (proxy.isPresent()) {
        List<Proxy> proxies = proxy.get().select(uri);
        if (proxies != null && !proxies.isEmpty()) {
          p = proxies.get(0);
        }
      }

      Version used = Version.HTTP_1_1;
      Http2Exchange.Result h2 = null;
      if (request.version().orElse(version) == Version.HTTP_2 && "https".equalsIgnoreCase(uri.getScheme())
          && (p == null || p == Proxy.NO_PROXY)) {
        h2 = sendHttp2(request, uri);
      }
      if (h2 != null) {
        return deliverHttp2(request, uri, h2, responseBodyHandler);
      }

      if ((p == null || p == Proxy.NO_PROXY) && ("http".equalsIgnoreCase(uri.getScheme())
          || "https".equalsIgnoreCase(uri.getScheme()))) {
        return http1(request, uri, responseBodyHandler, 0, null);
      }
      URLConnection connection = (p != null && p != Proxy.NO_PROXY) ? url.openConnection(p) : url.openConnection();
      if (connectTimeout.isPresent()) {
        connection.setConnectTimeout((int) connectTimeout.get().toMillis());
      }
      if (request.timeout().isPresent()) {
        connection.setReadTimeout((int) request.timeout().get().toMillis());
      }

      if (connection instanceof javax.net.ssl.HttpsURLConnection) {
        ((javax.net.ssl.HttpsURLConnection) connection).setSSLSocketFactory(sslContext.getSocketFactory());
      }
      if (connection instanceof HttpURLConnection) {
        HttpURLConnection http = (HttpURLConnection) connection;
        http.setRequestMethod(request.method());
        if (followRedirects == Redirect.ALWAYS) {
          http.setInstanceFollowRedirects(true);
        } else if (followRedirects == Redirect.NEVER) {
          http.setInstanceFollowRedirects(false);
        } else {
          http.setInstanceFollowRedirects(true);
        }
      }

      for (Map.Entry<String, List<String>> e : request.headers().map().entrySet()) {
        for (String val : e.getValue()) {
          connection.addRequestProperty(e.getKey(), val);
        }
      }

      if (cookieHandler.isPresent()) {
        Map<String, List<String>> cookies = cookieHandler.get().get(uri, request.headers().map());
        if (cookies != null) {
          for (Map.Entry<String, List<String>> e : cookies.entrySet()) {
            for (String val : e.getValue()) {
              connection.addRequestProperty(e.getKey(), val);
            }
          }
        }
      }

      Optional<HttpRequest.BodyPublisher> pub = request.bodyPublisher();
      if (pub.isPresent() && !request.method().equals("GET") && !request.method().equals("HEAD")) {
        connection.setDoOutput(true);
        try (OutputStream out = connection.getOutputStream()) {
          pub.get().subscribe(new Flow.Subscriber<ByteBuffer>() {
            @Override
            public void onSubscribe(Flow.Subscription subscription) {
              if (subscription != null) {
                subscription.request(Long.MAX_VALUE);
              }
            }

            @Override
            public void onNext(ByteBuffer item) {
              try {
                if (item.hasArray()) {
                  out.write(item.array(), item.arrayOffset() + item.position(), item.remaining());
                } else {
                  byte[] b = new byte[item.remaining()];
                  item.get(b);
                  out.write(b);
                }
              } catch (IOException e) {
                throw new RuntimeException(e);
              }
            }

            @Override
            public void onError(Throwable throwable) {}

            @Override
            public void onComplete() {}
          });
        }
      }

      int statusCode = 200;
      if (connection instanceof HttpURLConnection) {
        statusCode = ((HttpURLConnection) connection).getResponseCode();
        if ((statusCode == 401 || statusCode == 407) && authenticator.isPresent()
            && request.headers().firstValue(statusCode == 401 ? "Authorization" : "Proxy-Authorization").isEmpty()) {
          HttpResponse<T> retry = retryWithCredentials(request, responseBodyHandler, (HttpURLConnection) connection,
              statusCode);
          if (retry != null) {
            return retry;
          }
        }
      }

      Map<String, List<String>> headerFields = connection.getHeaderFields();
      if (headerFields == null) {
        headerFields = Collections.emptyMap();
      }
      HttpHeaders respHeaders = HttpHeaders.of(headerFields, (k, v) -> true);

      if (cookieHandler.isPresent()) {
        cookieHandler.get().put(uri, respHeaders.map());
      }

      final int code = statusCode;
      HttpResponse.ResponseInfo info = new HttpResponse.ResponseInfo() {
        @Override
        public int statusCode() {
          return code;
        }

        @Override
        public HttpHeaders headers() {
          return respHeaders;
        }

        @Override
        public Version version() {
          return used;
        }
      };

      HttpResponse.BodySubscriber<T> subscriber = responseBodyHandler.apply(info);
      subscriber.onSubscribe(null);

      InputStream in = null;
      try {
        in = connection.getInputStream();
      } catch (IOException e) {
        if (connection instanceof HttpURLConnection) {
          in = ((HttpURLConnection) connection).getErrorStream();
        }
      }

      if (in != null) {
        try (InputStream is = in) {
          byte[] buf = new byte[8192];
          int read;
          while ((read = is.read(buf)) != -1) {
            byte[] chunk = new byte[read];
            System.arraycopy(buf, 0, chunk, 0, read);
            subscriber.onNext(Collections.singletonList(ByteBuffer.wrap(chunk)));
          }
          subscriber.onComplete();
        }
      } else {
        subscriber.onComplete();
      }

      T body;
      try {
        body = subscriber.getBody().toCompletableFuture().get();
      } catch (Exception e) {
        Throwable cause = e.getCause() != null ? e.getCause() : e;
        if (cause instanceof IOException) throw (IOException) cause;
        throw new IOException(cause);
      }

      URI finalUri = uri;
      try {
        finalUri = connection.getURL().toURI();
      } catch (java.net.URISyntaxException ignore) { }
      return new HttpResponseImpl<T>(code, request, respHeaders, body, finalUri, used);
    }

    private final Http1Exchange.Pool pool = new Http1Exchange.Pool();

    private static final String kH2cSettings = "AAEAAEAAAAIAAAAAAAMAAAAAAAQBAAAAAAUAAEAAAAYABgAA";

    private byte[] collectBody(HttpRequest.BodyPublisher pub) throws IOException, InterruptedException {
      java.io.ByteArrayOutputStream bytes = new java.io.ByteArrayOutputStream();
      CompletableFuture<Void> done = new CompletableFuture<Void>();
      pub.subscribe(new Flow.Subscriber<ByteBuffer>() {
        @Override
        public void onSubscribe(Flow.Subscription subscription) {
          subscription.request(Long.MAX_VALUE);
        }

        @Override
        public void onNext(ByteBuffer item) {
          byte[] b = new byte[item.remaining()];
          item.get(b);
          bytes.write(b, 0, b.length);
        }

        @Override
        public void onError(Throwable throwable) {
          done.completeExceptionally(throwable);
        }

        @Override
        public void onComplete() {
          done.complete(null);
        }
      });
      try {
        done.get();
      } catch (java.util.concurrent.ExecutionException e) {
        Throwable cause = e.getCause();
        if (cause instanceof IOException) {
          throw (IOException) cause;
        }
        throw new IOException(cause);
      }
      return bytes.toByteArray();
    }

    private Http1Exchange.Connection connect(URI uri, long deadline) throws IOException {
      boolean secure = "https".equalsIgnoreCase(uri.getScheme());
      int port = uri.getPort() < 0 ? (secure ? 443 : 80) : uri.getPort();
      String host = uri.getHost();
      String key = uri.getScheme().toLowerCase(java.util.Locale.ROOT) + "://" + host + ":" + port;
      int ct = connectTimeout.isPresent() ? (int) connectTimeout.get().toMillis() : 0;
      java.net.Socket plain;
      try {
        plain = Http1Exchange.plainSocket(host, port, ct);
      } catch (java.net.ConnectException e) {
        java.net.ConnectException refused = new java.net.ConnectException();
        refused.initCause(e);
        throw refused;
      }
      if (!secure) {
        return new Http1Exchange.Connection(plain, key);
      }
      javax.net.ssl.SSLSocket ssl = (javax.net.ssl.SSLSocket) sslContext.getSocketFactory().createSocket(plain, host,
          port, true);
      javax.net.ssl.SSLParameters sp = ssl.getSSLParameters();
      if (sslParameters.getCipherSuites() != null) {
        sp.setCipherSuites(sslParameters.getCipherSuites());
      }
      if (sslParameters.getProtocols() != null) {
        sp.setProtocols(sslParameters.getProtocols());
      }
      sp.setEndpointIdentificationAlgorithm(sslParameters.getEndpointIdentificationAlgorithm() != null
          ? sslParameters.getEndpointIdentificationAlgorithm() : "HTTPS");
      sp.setApplicationProtocols(new String[] {"http/1.1"});
      ssl.setSSLParameters(sp);
      ssl.startHandshake();
      return new Http1Exchange.Connection(ssl, key);
    }

    private static boolean idempotent(String m) {
      return m.equals("GET") || m.equals("HEAD") || m.equals("PUT") || m.equals("DELETE") || m.equals("OPTIONS")
          || m.equals("TRACE");
    }

    private <T> HttpResponse<T> http1(HttpRequest request, URI uri, HttpResponse.BodyHandler<T> handler,
        int redirects, HttpResponse<T> previous) throws IOException, InterruptedException {
      String method = request.method();
      Optional<HttpRequest.BodyPublisher> pub = request.bodyPublisher();
      byte[] body = pub.isPresent() ? collectBody(pub.get()) : new byte[0];
      long declared = pub.isPresent() ? pub.get().contentLength() : 0;
      boolean chunked = pub.isPresent() && declared < 0;
      boolean secure = "https".equalsIgnoreCase(uri.getScheme());
      boolean upgrade = !secure && request.version().orElse(version) == Version.HTTP_2;
      int port = uri.getPort();

      Map<String, List<String>> sys = new java.util.TreeMap<String, List<String>>(String.CASE_INSENSITIVE_ORDER);
      Map<String, List<String>> user = request.headers().map();
      if (pub.isPresent()) {
        if (chunked) {
          sys.put("Transfer-encoding", Collections.singletonList("chunked"));
        } else {
          sys.put("Content-Length", Collections.singletonList(Integer.toString(body.length)));
        }
      }
      String host = uri.getHost();
      if (host != null && host.indexOf(':') >= 0 && !host.startsWith("[")) {
        host = "[" + host + "]";
      }
      sys.put("Host", Collections.singletonList(port < 0 ? host : host + ":" + port));
      boolean userAgent = false;
      for (String k : user.keySet()) {
        userAgent |= k.equalsIgnoreCase("User-Agent");
      }
      if (!userAgent) {
        sys.put("User-Agent", Collections.singletonList("Java-http-client/" + System.getProperty("java.version")));
      }
      if (request.expectContinue()) {
        sys.put("Expect", Collections.singletonList("100-Continue"));
      }
      if (upgrade) {
        sys.put("Connection", Collections.singletonList("Upgrade, HTTP2-Settings"));
        sys.put("Upgrade", Collections.singletonList("h2c"));
        sys.put("HTTP2-Settings", Collections.singletonList(kH2cSettings));
      }
      List<String[]> headers = new java.util.ArrayList<String[]>();
      for (Map.Entry<String, List<String>> e : sys.entrySet()) {
        for (String v : e.getValue()) {
          headers.add(new String[] {e.getKey(), v});
        }
      }
      for (Map.Entry<String, List<String>> e : user.entrySet()) {
        if (sys.containsKey(e.getKey())) {
          continue;
        }
        for (String v : e.getValue()) {
          headers.add(new String[] {e.getKey(), v});
        }
      }
      if (cookieHandler.isPresent()) {
        Map<String, List<String>> cookies = cookieHandler.get().get(uri, user);
        if (cookies != null) {
          for (Map.Entry<String, List<String>> e : cookies.entrySet()) {
            for (String v : e.getValue()) {
              headers.add(new String[] {e.getKey(), v});
            }
          }
        }
      }
      String target = uri.getRawPath() == null || uri.getRawPath().isEmpty() ? "/" : uri.getRawPath();
      if (uri.getRawQuery() != null) {
        target += "?" + uri.getRawQuery();
      }

      long deadline = request.timeout().isPresent() ? System.nanoTime() + request.timeout().get().toNanos() : 0;
      Http1Exchange.Connection conn = null;
      Http1Exchange.Head head = null;
      boolean retry = true;
      IOException writeFailure = null;
      while (head == null) {
        writeFailure = null;
        conn = pool.take(uri.getScheme().toLowerCase(java.util.Locale.ROOT) + "://" + uri.getHost() + ":"
            + (port < 0 ? (secure ? 443 : 80) : port));
        if (conn == null) {
          conn = connect(uri, deadline);
        }
        try {
          if (deadline != 0) {
            long left = (deadline - System.nanoTime()) / 1000000L;
            if (left <= 0) {
              throw new java.net.SocketTimeoutException("read timed out");
            }
            conn.socket.setSoTimeout((int) left);
          } else {
            conn.socket.setSoTimeout(0);
          }
          Http1Exchange.writeRequest(conn, method, target, headers);
          boolean sentBody = false;
          if (request.expectContinue() && body.length > 0) {
            int old = conn.socket.getSoTimeout();
            conn.socket.setSoTimeout(old == 0 ? 3000 : Math.min(old, 3000));
            try {
              head = Http1Exchange.readHead(conn);
              while (head.status == 100) {
                Http1Exchange.writeBody(conn, body, chunked);
                sentBody = true;
                head = Http1Exchange.readHead(conn);
              }
            } catch (java.net.SocketTimeoutException e) {
              head = null;
            }
            conn.socket.setSoTimeout(old);
            if (head != null && !sentBody && head.status < 200) {
              head = null;
            }
            if (head == null) {
              Http1Exchange.writeBody(conn, body, chunked);
              sentBody = true;
            }
          } else if (pub.isPresent()) {
            try {
              Http1Exchange.writeBody(conn, body, chunked);
            } catch (IOException e) {
              writeFailure = e;
            }
          }
          if (head == null) {
            try {
              head = Http1Exchange.readHead(conn);
            } catch (IOException e) {
              throw writeFailure != null ? writeFailure : e;
            }
          }
          while (head.status >= 100 && head.status < 200 && head.status != 101) {
            head = Http1Exchange.readHead(conn);
          }
        } catch (IOException e) {
          boolean wasReused = conn.reused;
          conn.close();
          head = null;
          if (e instanceof java.net.SocketTimeoutException) {
            throw e;
          }
          if (wasReused && retry && idempotent(method)) {
            retry = false;
            continue;
          }
          throw e;
        }
      }
      final Http1Exchange.Connection c = conn;
      Http1Exchange.Head h = head;
      Map<String, List<String>> respMap = h.headers;
      HttpHeaders respHeaders = HttpHeaders.of(respMap, (k, v) -> true);

      if (h.status == 101 && upgrade && "h2c".equalsIgnoreCase(String.valueOf(h.first("upgrade")))) {
        Http2Exchange.Result r2 = Http2Exchange.upgraded(c.in, c.out);
        c.close();
        return deliverHttp2(request, uri, r2, handler);
      }
      if (cookieHandler.isPresent()) {
        cookieHandler.get().put(uri, respHeaders.map());
      }
      final int code = h.status;
      String location = h.first("location");
      if (followRedirects != Redirect.NEVER && location != null && (code == 301 || code == 302 || code == 303
          || code == 307 || code == 308)) {
        URI target2;
        try {
          target2 = uri.resolve(location);
        } catch (IllegalArgumentException e) {
          throw new IOException("Invalid redirection");
        }
        boolean allowed = redirects < 5 && (followRedirects == Redirect.ALWAYS || !(secure && "http".equalsIgnoreCase(
            target2.getScheme())));
        if (allowed) {
          drain(c, method, h);
          HttpRequest.Builder b = HttpRequest.newBuilder(target2);
          boolean toGet = code == 303 ? !method.equals("HEAD") : (code == 301 || code == 302) && method.equals("POST");
          if (toGet) {
            b.GET();
          } else {
            b.method(method, pub.orElse(null) == null ? HttpRequest.BodyPublishers.noBody() : pub.get());
          }
          for (Map.Entry<String, List<String>> e : user.entrySet()) {
            for (String v : e.getValue()) {
              b.header(e.getKey(), v);
            }
          }
          request.timeout().ifPresent(b::timeout);
          b.version(request.version().orElse(version));
          HttpRequest next = b.build();
          HttpResponseImpl<T> prior = new HttpResponseImpl<T>(code, request, respHeaders, null, uri,
              Version.HTTP_1_1).chain(previous, null);
          return http1(next, target2, handler, redirects + 1, prior);
        }
      }
      if ((code == 401 || code == 407) && authenticator.isPresent()
          && request.headers().firstValue(code == 401 ? "Authorization" : "Proxy-Authorization").isEmpty()) {
        String challenge = h.first(code == 401 ? "www-authenticate" : "proxy-authenticate");
        HttpRequest retryRequest = credentialRequest(request, uri, challenge, code);
        if (retryRequest != null) {
          drain(c, method, h);
          return http1(retryRequest, uri, handler, redirects, previous);
        }
      }

      HttpResponse.ResponseInfo info = new HttpResponse.ResponseInfo() {
        @Override
        public int statusCode() {
          return code;
        }

        @Override
        public HttpHeaders headers() {
          return respHeaders;
        }

        @Override
        public Version version() {
          return Version.HTTP_1_1;
        }
      };
      boolean reusable = Http1Exchange.keepAlive(h) && !Http1Exchange.framedToEnd(h, method);
      Runnable onEnd = () -> {
        if (reusable) {
          pool.give(c);
        } else {
          c.close();
        }
      };
      HttpResponse.BodySubscriber<T> subscriber = handler.apply(info);
      subscriber.onSubscribe(null);
      InputStream in = Http1Exchange.bodyStream(c, method, h, onEnd);
      try {
        byte[] buf = new byte[8192];
        int read;
        while ((read = in.read(buf)) != -1) {
          byte[] chunk = new byte[read];
          System.arraycopy(buf, 0, chunk, 0, read);
          subscriber.onNext(Collections.singletonList(ByteBuffer.wrap(chunk)));
        }
        subscriber.onComplete();
      } catch (IOException e) {
        c.close();
        subscriber.onError(e);
        throw e;
      }
      T result;
      try {
        result = subscriber.getBody().toCompletableFuture().get();
      } catch (Exception e) {
        Throwable cause = e.getCause() != null ? e.getCause() : e;
        if (cause instanceof IOException) {
          throw (IOException) cause;
        }
        throw new IOException(cause);
      }
      javax.net.ssl.SSLSession session = c.socket instanceof javax.net.ssl.SSLSocket
          ? ((javax.net.ssl.SSLSocket) c.socket).getSession() : null;
      return new HttpResponseImpl<T>(code, request, respHeaders, result, uri, Version.HTTP_1_1)
          .chain(previous, session);
    }

    private void drain(Http1Exchange.Connection c, String method, Http1Exchange.Head h) {
      boolean reusable = Http1Exchange.keepAlive(h) && !Http1Exchange.framedToEnd(h, method);
      try {
        InputStream in = Http1Exchange.bodyStream(c, method, h, reusable ? () -> pool.give(c) : c::close);
        byte[] buf = new byte[4096];
        while (in.read(buf) != -1) {
          continue;
        }
      } catch (IOException e) {
        c.close();
      }
    }

    private HttpRequest credentialRequest(HttpRequest request, URI uri, String challenge, int status)
        throws IOException {
      if (challenge == null) {
        return null;
      }
      int space = challenge.indexOf(' ');
      String scheme = space < 0 ? challenge : challenge.substring(0, space);
      if (!"Basic".equalsIgnoreCase(scheme)) {
        return null;
      }
      String prompt = null;
      int r = challenge.toLowerCase().indexOf("realm=\"");
      if (r >= 0) {
        int end = challenge.indexOf('"', r + 7);
        prompt = end > r ? challenge.substring(r + 7, end) : null;
      }
      int port = uri.getPort() < 0 ? ("https".equalsIgnoreCase(uri.getScheme()) ? 443 : 80) : uri.getPort();
      java.net.PasswordAuthentication pa = authenticator.get().requestPasswordAuthenticationInstance(uri.getHost(),
          null, port, uri.getScheme(), prompt, "Basic", uri.toURL(),
          status == 401 ? Authenticator.RequestorType.SERVER : Authenticator.RequestorType.PROXY);
      if (pa == null) {
        return null;
      }
      String token = java.util.Base64.getEncoder().encodeToString(
          (pa.getUserName() + ":" + new String(pa.getPassword())).getBytes(java.nio.charset.StandardCharsets.UTF_8));
      HttpRequest.Builder b = HttpRequest.newBuilder(uri).method(request.method(), request.bodyPublisher()
          .orElse(HttpRequest.BodyPublishers.noBody()));
      request.headers().map().forEach((k, vs) -> vs.forEach(v -> b.header(k, v)));
      b.header(status == 401 ? "Authorization" : "Proxy-Authorization", "Basic " + token);
      request.timeout().ifPresent(b::timeout);
      return b.build();
    }

    private <T> HttpResponse<T> retryWithCredentials(HttpRequest request, HttpResponse.BodyHandler<T> handler,
        HttpURLConnection failed, int status) throws IOException, InterruptedException
    {
      String challenge = failed.getHeaderField(status == 401 ? "WWW-Authenticate" : "Proxy-Authenticate");
      if (challenge == null) {
        return null;
      }
      int space = challenge.indexOf(' ');
      String scheme = space < 0 ? challenge : challenge.substring(0, space);
      if (!"Basic".equalsIgnoreCase(scheme)) {
        return null;
      }
      String prompt = null;
      int r = challenge.toLowerCase().indexOf("realm=\"");
      if (r >= 0) {
        int end = challenge.indexOf('"', r + 7);
        prompt = end > r ? challenge.substring(r + 7, end) : null;
      }
      URI uri = request.uri();
      int port = uri.getPort() < 0 ? ("https".equalsIgnoreCase(uri.getScheme()) ? 443 : 80) : uri.getPort();
      java.net.PasswordAuthentication pa = authenticator.get().requestPasswordAuthenticationInstance(uri.getHost(),
          null, port, uri.getScheme(), prompt, "Basic", uri.toURL(),
          status == 401 ? Authenticator.RequestorType.SERVER : Authenticator.RequestorType.PROXY);
      if (pa == null) {
        return null;
      }
      String token = java.util.Base64.getEncoder().encodeToString(
          (pa.getUserName() + ":" + new String(pa.getPassword())).getBytes(java.nio.charset.StandardCharsets.UTF_8));
      HttpRequest.Builder b = HttpRequest.newBuilder(uri).method(request.method(), request.bodyPublisher()
          .orElse(HttpRequest.BodyPublishers.noBody()));
      request.headers().map().forEach((k, vs) -> vs.forEach(v -> b.header(k, v)));
      b.header(status == 401 ? "Authorization" : "Proxy-Authorization", "Basic " + token);
      request.timeout().ifPresent(b::timeout);
      return send0(b.build(), handler);
    }

    private Http2Exchange.Result sendHttp2(HttpRequest request, URI uri) throws IOException, InterruptedException {
      java.io.ByteArrayOutputStream bodyBytes = new java.io.ByteArrayOutputStream();
      Optional<HttpRequest.BodyPublisher> pub = request.bodyPublisher();
      if (pub.isPresent() && !request.method().equals("GET") && !request.method().equals("HEAD")) {
        pub.get().subscribe(new Flow.Subscriber<ByteBuffer>() {
          @Override
          public void onSubscribe(Flow.Subscription subscription) {
            subscription.request(Long.MAX_VALUE);
          }

          @Override
          public void onNext(ByteBuffer item) {
            byte[] b = new byte[item.remaining()];
            item.get(b);
            bodyBytes.write(b, 0, b.length);
          }

          @Override
          public void onError(Throwable throwable) { }

          @Override
          public void onComplete() { }
        });
      }
      List<String[]> headers = new java.util.ArrayList<String[]>();
      boolean agent = false;
      for (Map.Entry<String, List<String>> e : request.headers().map().entrySet()) {
        String name = e.getKey().toLowerCase(java.util.Locale.ROOT);
        agent |= name.equals("user-agent");
        for (String v : e.getValue()) {
          headers.add(new String[] {name, v});
        }
      }
      if (!agent) {
        headers.add(new String[] {"user-agent", "Java-http-client/" + System.getProperty("java.version")});
      }
      if (cookieHandler.isPresent()) {
        Map<String, List<String>> cookies = cookieHandler.get().get(uri, request.headers().map());
        if (cookies != null) {
          for (Map.Entry<String, List<String>> e : cookies.entrySet()) {
            for (String v : e.getValue()) {
              headers.add(new String[] {e.getKey().toLowerCase(java.util.Locale.ROOT), v});
            }
          }
        }
      }
      byte[] body = bodyBytes.toByteArray();
      if (body.length > 0) {
        headers.add(new String[] {"content-length", Integer.toString(body.length)});
      }
      int connect = connectTimeout.isPresent() ? (int) connectTimeout.get().toMillis() : 0;
      int read = request.timeout().isPresent() ? (int) request.timeout().get().toMillis() : 0;
      return h2Request(uri, request.method(), headers, body, connect, read);
    }

    private final Map<String, Http2Connection> h2Pool = new java.util.HashMap<String, Http2Connection>();
    private final java.util.Set<String> noH2 = new java.util.HashSet<String>();

    private Http2Exchange.Result h2Request(URI uri, String method, List<String[]> headers, byte[] body, int connect,
                                           int read) throws IOException, InterruptedException {
      String key = uri.getHost().toLowerCase(java.util.Locale.ROOT) + ":" + (uri.getPort() == -1 ? 443 : uri.getPort());
      for (int attempt = 0; ; attempt++) {
        Http2Connection c;
        boolean reused;
        synchronized (h2Pool) {
          if (noH2.contains(key)) {
            return null;
          }
          c = h2Pool.get(key);
          if (c != null && !c.usable()) {
            h2Pool.remove(key);
            c = null;
          }
          reused = c != null;
          if (c == null) {
            c = Http2Connection.open(sslContext.getSocketFactory(), sslParameters, uri, connect);
            if (c == null) {
              noH2.add(key);
              return null;
            }
            h2Pool.put(key, c);
          }
        }
        try {
          Http2Exchange.Result r = c.request(uri, method, headers, body, read);
          r.session = c.session();
          return r;
        } catch (HttpTimeoutException e) {
          throw e;
        } catch (IOException e) {
          synchronized (h2Pool) {
            if (h2Pool.get(key) == c) {
              h2Pool.remove(key);
            }
          }
          c.close();
          if (!reused || attempt > 0) {
            throw e;
          }
        }
      }
    }

    private <T> HttpResponse<T> deliverHttp2(HttpRequest request, URI uri, Http2Exchange.Result r,
                                             HttpResponse.BodyHandler<T> handler)
        throws IOException, InterruptedException
    {
      Map<String, List<String>> h2Headers = new java.util.LinkedHashMap<String, List<String>>();
      h2Headers.put(":status", Collections.singletonList(Integer.toString(r.status)));
      h2Headers.putAll(r.headers);
      HttpHeaders respHeaders = HttpHeaders.of(h2Headers, (k, v) -> true);
      if (cookieHandler.isPresent()) {
        cookieHandler.get().put(uri, respHeaders.map());
      }
      String location = respHeaders.firstValue("location").orElse(null);
      boolean redirect = r.status >= 301 && r.status <= 308 && r.status != 304 && r.status != 306 && location != null;
      if (redirect && followRedirects != Redirect.NEVER) {
        URI target = uri.resolve(location);
        boolean secure = "https".equalsIgnoreCase(target.getScheme());
        if (followRedirects == Redirect.ALWAYS || secure) {
          HttpRequest.Builder b = HttpRequest.newBuilder(target);
          boolean toGet = r.status == 303 || ((r.status == 301 || r.status == 302) && request.method().equals("POST"));
          if (toGet) {
            b.GET();
          } else {
            b.method(request.method(), request.bodyPublisher().orElse(HttpRequest.BodyPublishers.noBody()));
          }
          request.headers().map().forEach((k, vs) -> vs.forEach(v -> {
            if (!k.equalsIgnoreCase("content-length")) {
              b.header(k, v);
            }
          }));
          return send(b.build(), handler);
        }
      }
      final int code = r.status;
      HttpResponse.ResponseInfo info = new HttpResponse.ResponseInfo() {
        @Override
        public int statusCode() {
          return code;
        }

        @Override
        public HttpHeaders headers() {
          return respHeaders;
        }

        @Override
        public Version version() {
          return Version.HTTP_2;
        }
      };
      HttpResponse.BodySubscriber<T> subscriber = handler.apply(info);
      subscriber.onSubscribe(null);
      if (r.body.length > 0) {
        subscriber.onNext(Collections.singletonList(ByteBuffer.wrap(r.body)));
      }
      subscriber.onComplete();
      T body;
      try {
        body = subscriber.getBody().toCompletableFuture().get();
      } catch (Exception e) {
        Throwable cause = e.getCause() != null ? e.getCause() : e;
        if (cause instanceof IOException) throw (IOException) cause;
        throw new IOException(cause);
      }
      return new HttpResponseImpl<T>(code, request, respHeaders, body, uri, Version.HTTP_2).chain(null, r.session);
    }

    @Override
    public <T> CompletableFuture<HttpResponse<T>> sendAsync(
        HttpRequest request, HttpResponse.BodyHandler<T> responseBodyHandler,
        HttpResponse.PushPromiseHandler<T> pushPromiseHandler)
    {
      return sendAsync(request, responseBodyHandler);
    }

    @Override
    public <T> CompletableFuture<HttpResponse<T>> sendAsync(
        HttpRequest request, HttpResponse.BodyHandler<T> responseBodyHandler)
    {
      Executor exec = executor.orElse(ForkJoinPool.commonPool());
      return CompletableFuture.supplyAsync(() -> {
        try {
          return send(request, responseBodyHandler);
        } catch (InterruptedException e) {
          Thread.currentThread().interrupt();
          throw new java.util.concurrent.CompletionException(e);
        } catch (Exception e) {
          throw new java.util.concurrent.CompletionException(e);
        }
      }, exec);
    }
  }

  private static class HttpResponseImpl<T> implements HttpResponse<T> {
    private final int statusCode;
    private final HttpRequest request;
    private final HttpHeaders headers;
    private final T body;
    private final URI uri;
    private final Version version;
    private HttpResponse<T> previous;
    private javax.net.ssl.SSLSession session;

    HttpResponseImpl<T> chain(HttpResponse<T> previous, javax.net.ssl.SSLSession session) {
      this.previous = previous;
      this.session = session;
      return this;
    }

    HttpResponseImpl(int statusCode, HttpRequest request, HttpHeaders headers, T body, URI uri, Version version) {
      this.statusCode = statusCode;
      this.request = request;
      this.headers = headers;
      this.body = body;
      this.uri = uri;
      this.version = version;
    }

    @Override
    public int statusCode() {
      return statusCode;
    }

    @Override
    public HttpRequest request() {
      return request;
    }

    @Override
    public Optional<HttpResponse<T>> previousResponse() {
      return Optional.ofNullable(previous);
    }

    @Override
    public HttpHeaders headers() {
      return headers;
    }

    @Override
    public T body() {
      return body;
    }

    @Override
    public Optional<javax.net.ssl.SSLSession> sslSession() {
      return Optional.ofNullable(session);
    }

    @Override
    public URI uri() {
      return uri;
    }

    @Override
    public Version version() {
      return version;
    }

    @Override
    public String toString() {
      return "(" + request.method() + " " + uri + ") " + statusCode;
    }
  }
}
