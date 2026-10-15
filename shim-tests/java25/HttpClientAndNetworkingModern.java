/*
 * Copyright (c) 2026 BasaltMC.
 *
 * Licensed under the MIT License; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

// Minimum release: 17

import java.net.Authenticator;
import java.net.CookieManager;
import java.net.CookiePolicy;
import java.net.HttpCookie;
import java.net.InetSocketAddress;
import java.net.PasswordAuthentication;
import java.net.Proxy;
import java.net.ProxySelector;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpHeaders;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.net.http.HttpTimeoutException;
import java.net.http.HttpConnectTimeoutException;
import java.net.http.WebSocketHandshakeException;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.OptionalLong;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Flow;

public class HttpClientAndNetworkingModern {

  public static void main(String[] args) throws Exception {
    testExceptions();
    testHttpClientConfiguration();
    testHttpRequestBuilding();
    testHttpHeadersOperations();
    testBodyPublishersAndSubscribers();
    testCookieSystem();
    testInetSocketAddressModern();
    testProxySelectorAndAuthenticator();
    System.out.println("ALL HTTP CLIENT AND NETWORKING TESTS COMPLETED");
  }

  private static void testExceptions() {
    HttpTimeoutException hte = new HttpTimeoutException("timeout");
    if (!(hte instanceof java.io.IOException)) throw new AssertionError();

    HttpConnectTimeoutException hcte = new HttpConnectTimeoutException("connect timeout");
    if (!(hcte instanceof HttpTimeoutException)) throw new AssertionError();

    WebSocketHandshakeException wshe = new WebSocketHandshakeException(null);
    if (!(wshe instanceof java.io.IOException) || wshe.getResponse() != null) throw new AssertionError();

    System.out.println("Exceptions: OK");
  }

  private static void testHttpClientConfiguration() throws Exception {
    HttpClient defaultClient = HttpClient.newHttpClient();
    if (defaultClient.version() != HttpClient.Version.HTTP_2) throw new AssertionError();
    if (defaultClient.followRedirects() != HttpClient.Redirect.NEVER) throw new AssertionError();
    if (defaultClient.connectTimeout().isPresent()) throw new AssertionError();

    CookieManager cm = new CookieManager();
    HttpClient customClient = HttpClient.newBuilder()
        .version(HttpClient.Version.HTTP_1_1)
        .followRedirects(HttpClient.Redirect.ALWAYS)
        .connectTimeout(Duration.ofSeconds(7))
        .cookieHandler(cm)
        .build();

    if (customClient.version() != HttpClient.Version.HTTP_1_1) throw new AssertionError();
    if (customClient.followRedirects() != HttpClient.Redirect.ALWAYS) throw new AssertionError();
    if (!customClient.connectTimeout().isPresent() || customClient.connectTimeout().get().toSeconds() != 7) {
      throw new AssertionError();
    }
    if (!customClient.cookieHandler().isPresent() || customClient.cookieHandler().get() != cm) {
      throw new AssertionError();
    }

    try {
      java.lang.reflect.Method closeMethod = HttpClient.class.getMethod("close");
      closeMethod.invoke(customClient);
    } catch (NoSuchMethodException ignored) {
      // close() is standard in HttpClient starting in Java 21
    }
    System.out.println("HttpClient Configuration: OK");
  }

  private static void testHttpRequestBuilding() {
    URI uri = URI.create("http://example.com:8080/api/v1/resource");
    HttpRequest req = HttpRequest.newBuilder(uri)
        .GET()
        .header("User-Agent", "BasaltShim-Test/1.0")
        .header("Accept", "application/json")
        .header("Accept", "text/plain")
        .headers("X-Trace-Id", "12345", "X-Span-Id", "67890")
        .timeout(Duration.ofMillis(3500))
        .expectContinue(true)
        .version(HttpClient.Version.HTTP_2)
        .build();

    if (!req.uri().equals(uri)) throw new AssertionError();
    if (!req.method().equals("GET")) throw new AssertionError();
    if (!req.expectContinue()) throw new AssertionError();
    if (!req.version().isPresent() || req.version().get() != HttpClient.Version.HTTP_2) throw new AssertionError();
    if (!req.timeout().isPresent() || req.timeout().get().toMillis() != 3500) throw new AssertionError();

    HttpHeaders h = req.headers();
    if (!h.firstValue("user-agent").isPresent() || !h.firstValue("user-agent").get().equals("BasaltShim-Test/1.0")) {
      throw new AssertionError();
    }
    List<String> accepts = h.allValues("accept");
    if (accepts.size() != 2 || !accepts.get(0).equals("application/json") || !accepts.get(1).equals("text/plain")) {
      throw new AssertionError();
    }

    // POST with body
    HttpRequest postReq = HttpRequest.newBuilder(uri)
        .POST(HttpRequest.BodyPublishers.ofString("payload-data"))
        .setHeader("Content-Type", "text/plain")
        .build();

    if (!postReq.method().equals("POST")) throw new AssertionError();
    if (!postReq.bodyPublisher().isPresent()) throw new AssertionError();
    if (postReq.bodyPublisher().get().contentLength() != 12) throw new AssertionError();

    System.out.println("HttpRequest Building: OK");
  }

  private static void testHttpHeadersOperations() {
    Map<String, List<String>> raw = new HashMap<String, List<String>>();
    raw.put("Content-Length", Collections.singletonList("4096"));
    raw.put("X-Custom", Arrays.asList("alpha", "beta"));
    raw.put("x-ignored", Collections.singletonList("drop"));

    HttpHeaders headers = HttpHeaders.of(raw, (k, v) -> !k.equalsIgnoreCase("x-ignored"));

    Optional<String> len = headers.firstValue("content-length");
    if (!len.isPresent() || !len.get().equals("4096")) throw new AssertionError();

    OptionalLong lenLong = headers.firstValueAsLong("CONTENT-LENGTH");
    if (!lenLong.isPresent() || lenLong.getAsLong() != 4096L) throw new AssertionError();

    if (headers.firstValue("x-ignored").isPresent()) throw new AssertionError();

    List<String> customs = headers.allValues("X-CUSTOM");
    if (customs.size() != 2 || !customs.get(0).equals("alpha") || !customs.get(1).equals("beta")) {
      throw new AssertionError();
    }

    System.out.println("HttpHeaders Operations: OK");
  }

  private static void testBodyPublishersAndSubscribers() throws Exception {
    // BodyPublishers
    HttpRequest.BodyPublisher p1 = HttpRequest.BodyPublishers.ofString("Hello");
    HttpRequest.BodyPublisher p2 = HttpRequest.BodyPublishers.ofString(" World!");
    HttpRequest.BodyPublisher concat = HttpRequest.BodyPublishers.concat(p1, p2);
    if (concat.contentLength() != 12) throw new AssertionError("Concat length: " + concat.contentLength());

    // BodySubscribers
    Flow.Subscription dummySub = new Flow.Subscription() {
      @Override public void request(long n) {}
      @Override public void cancel() {}
    };

    HttpResponse.BodySubscriber<String> strSub = HttpResponse.BodySubscribers.ofString(StandardCharsets.UTF_8);
    strSub.onSubscribe(dummySub);
    strSub.onNext(Collections.singletonList(ByteBuffer.wrap("Streamed ".getBytes("UTF-8"))));
    strSub.onNext(Collections.singletonList(ByteBuffer.wrap("Body".getBytes("UTF-8"))));
    strSub.onComplete();

    String result = strSub.getBody().toCompletableFuture().get();
    if (!result.equals("Streamed Body")) throw new AssertionError("Got: " + result);

    // BodySubscribers.replacing
    HttpResponse.BodySubscriber<Integer> intSub = HttpResponse.BodySubscribers.replacing(999);
    intSub.onSubscribe(dummySub);
    intSub.onComplete();
    if (intSub.getBody().toCompletableFuture().get() != 999) throw new AssertionError();

    System.out.println("BodyPublishers and Subscribers: OK");
  }

  private static void testCookieSystem() throws Exception {
    List<HttpCookie> cookies = HttpCookie.parse(
        "auth_token=xyz987; Path=/secure; Domain=example.com; Max-Age=3600; Secure; HttpOnly; Version=1");
    if (cookies.size() != 1) throw new AssertionError();
    HttpCookie c = cookies.get(0);
    if (!c.getName().equals("auth_token")) throw new AssertionError();
    if (!c.getValue().equals("xyz987")) throw new AssertionError();
    if (!c.getPath().equals("/secure")) throw new AssertionError();
    if (!c.getDomain().equals("example.com")) throw new AssertionError();
    if (c.getMaxAge() != 3600) throw new AssertionError();
    if (!c.getSecure()) throw new AssertionError();
    if (!c.isHttpOnly()) throw new AssertionError();
    if (c.getVersion() != 1) throw new AssertionError();

    CookieManager manager = new CookieManager(null, CookiePolicy.ACCEPT_ALL);
    URI uri = URI.create("https://example.com/secure/page");
    Map<String, List<String>> setCookie = new HashMap<String, List<String>>();
    setCookie.put("Set-Cookie", Collections.singletonList("user=alice; Path=/secure; Domain=example.com"));
    manager.put(uri, setCookie);

    Map<String, List<String>> retrieved = manager.get(uri, Collections.emptyMap());
    List<String> cookieHeaders = retrieved.get("Cookie");
    if (cookieHeaders == null || cookieHeaders.isEmpty()) throw new AssertionError();
    if (!cookieHeaders.get(0).contains("user=alice")) throw new AssertionError("Got: " + cookieHeaders.get(0));

    System.out.println("Cookie System: OK");
  }

  private static void testInetSocketAddressModern() {
    InetSocketAddress addr = InetSocketAddress.createUnresolved("proxy.internal", 3128);
    if (!addr.isUnresolved()) throw new AssertionError();
    if (!addr.getHostString().equals("proxy.internal")) throw new AssertionError();
    if (addr.getPort() != 3128) throw new AssertionError();
    if (addr.getAddress() != null) throw new AssertionError();

    System.out.println("InetSocketAddress: OK");
  }

  private static void testProxySelectorAndAuthenticator() {
    InetSocketAddress proxyAddr = InetSocketAddress.createUnresolved("127.0.0.1", 8888);
    ProxySelector selector = ProxySelector.of(proxyAddr);
    List<Proxy> proxies = selector.select(URI.create("http://example.org"));
    if (proxies == null || proxies.isEmpty()) throw new AssertionError();
    Proxy p = proxies.get(0);
    if (p.type() != Proxy.Type.HTTP) throw new AssertionError();

    PasswordAuthentication pa = new PasswordAuthentication("bot", "pass123".toCharArray());
    if (!pa.getUserName().equals("bot")) throw new AssertionError();
    if (pa.getPassword().length != 7) throw new AssertionError();

    System.out.println("ProxySelector and Authenticator: OK");
  }
}
