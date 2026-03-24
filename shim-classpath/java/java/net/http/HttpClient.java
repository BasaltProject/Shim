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

  public <T> CompletableFuture<HttpResponse<T>> sendAsync(
      HttpRequest request, HttpResponse.BodyHandler<T> responseBodyHandler, HttpResponse.PushPromiseHandler<T> pushPromiseHandler)
  {
    return sendAsync(request, responseBodyHandler);
  }

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

    private Http2Exchange.Result sendHttp2(HttpRequest request, URI uri) throws IOException {
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
      return Http2Exchange.run(sslContext.getSocketFactory(), sslParameters, uri, request.method(), headers, body,
          connect, read);
    }

    private <T> HttpResponse<T> deliverHttp2(HttpRequest request, URI uri, Http2Exchange.Result r,
                                             HttpResponse.BodyHandler<T> handler)
        throws IOException, InterruptedException
    {
      HttpHeaders respHeaders = HttpHeaders.of(r.headers, (k, v) -> true);
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
      return new HttpResponseImpl<T>(code, request, respHeaders, body, uri, Version.HTTP_2);
    }

    @Override
    public <T> CompletableFuture<HttpResponse<T>> sendAsync(
        HttpRequest request, HttpResponse.BodyHandler<T> responseBodyHandler)
    {
      Executor exec = executor.orElse(ForkJoinPool.commonPool());
      return CompletableFuture.supplyAsync(() -> {
        try {
          return send(request, responseBodyHandler);
        } catch (Exception e) {
          throw new RuntimeException(e);
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
      return Optional.empty();
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
      return Optional.empty();
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
