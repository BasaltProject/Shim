/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 * SPDX-License-Identifier: MIT
 * Minimum release: 21
 */

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;
import com.sun.net.httpserver.HttpsConfigurator;
import com.sun.net.httpserver.HttpsExchange;
import com.sun.net.httpserver.HttpsServer;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.URI;
import java.net.URL;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.security.KeyStore;
import java.security.MessageDigest;
import java.util.Base64;
import java.util.HexFormat;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.Executors;
import java.util.concurrent.ExecutorService;
import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.TrustManagerFactory;

public class HttpServerClientModern {
  static final String kKeystore = "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";

  static void t(String name, Callable<Object> c) {
    try {
      System.out.println(name + " = " + c.call());
    } catch (Throwable e) {
      System.out.println(name + " ! " + e.getClass().getName() + (e instanceof NullPointerException ? "" : ": " + e.getMessage()));
    }
  }

  static String sha(byte[] b) throws Exception {
    return HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256").digest(b)).substring(0, 16);
  }

  static void write(HttpExchange ex, int code, byte[] body) throws IOException {
    ex.sendResponseHeaders(code, body.length == 0 ? -1 : body.length);
    if (body.length > 0) {
      try (OutputStream os = ex.getResponseBody()) {
        os.write(body);
      }
    }
    ex.close();
  }

  public static void main(String[] args) throws Exception {
    HttpServer server = HttpServer.create(new InetSocketAddress(InetAddress.getLoopbackAddress(), 0), 0);
    ExecutorService pool = Executors.newFixedThreadPool(4);
    server.setExecutor(pool);
    server.createContext("/echo", ex -> {
      byte[] in = ex.getRequestBody().readAllBytes();
      ex.getResponseHeaders().set("X-Seen-Method", ex.getRequestMethod());
      ex.getResponseHeaders().set("X-Seen-Length", String.valueOf(in.length));
      ex.getResponseHeaders().set("Content-Type", "application/octet-stream");
      write(ex, 200, in);
    });
    server.createContext("/stream", ex -> {
      ex.sendResponseHeaders(200, 0);
      try (OutputStream os = ex.getResponseBody()) {
        for (int i = 0; i < 50; i++) {
          os.write(("line " + i + "\n").getBytes());
          if (i % 10 == 9) {
            os.flush();
          }
        }
      }
    });
    server.createContext("/redirect", ex -> {
      ex.getResponseHeaders().set("Location", "/echo");
      write(ex, 302, new byte[0]);
    });
    server.createContext("/status", ex -> write(ex, Integer.parseInt(ex.getRequestURI().getQuery()), new byte[0]));
    server.createContext("/headers", ex -> {
      StringBuilder sb = new StringBuilder();
      List<String> names = new java.util.ArrayList<>(ex.getRequestHeaders().keySet());
      java.util.Collections.sort(names);
      for (String n : names) {
        if (n.startsWith("X-")) {
          sb.append(n).append('=').append(ex.getRequestHeaders().get(n)).append(';');
        }
      }
      write(ex, 200, sb.toString().getBytes());
    });
    server.start();
    int port = server.getAddress().getPort();
    String base = "http://127.0.0.1:" + port;

    byte[] big = new byte[1 << 20];
    new java.util.Random(7).nextBytes(big);

    t("urlconn-get", () -> {
      HttpURLConnection c = (HttpURLConnection) new URL(base + "/echo").openConnection();
      int code = c.getResponseCode();
      return code + " " + c.getHeaderField("X-Seen-Method") + " " + c.getHeaderField("Content-Type") + " "
          + new String(c.getInputStream().readAllBytes());
    });
    t("urlconn-post", () -> {
      HttpURLConnection c = (HttpURLConnection) new URL(base + "/echo").openConnection();
      c.setRequestMethod("POST");
      c.setDoOutput(true);
      c.getOutputStream().write("posted body".getBytes());
      return c.getResponseCode() + " " + c.getHeaderField("X-Seen-Length") + " "
          + new String(c.getInputStream().readAllBytes());
    });
    t("urlconn-post-big", () -> {
      HttpURLConnection c = (HttpURLConnection) new URL(base + "/echo").openConnection();
      c.setRequestMethod("POST");
      c.setDoOutput(true);
      c.setFixedLengthStreamingMode(big.length);
      c.getOutputStream().write(big);
      byte[] back = c.getInputStream().readAllBytes();
      return c.getResponseCode() + " " + back.length + " " + sha(back).equals(sha(big));
    });
    t("urlconn-post-chunked", () -> {
      HttpURLConnection c = (HttpURLConnection) new URL(base + "/echo").openConnection();
      c.setRequestMethod("POST");
      c.setDoOutput(true);
      c.setChunkedStreamingMode(1000);
      c.getOutputStream().write(big, 0, 100000);
      byte[] back = c.getInputStream().readAllBytes();
      return c.getResponseCode() + " " + back.length + " " + c.getHeaderField("X-Seen-Length");
    });
    t("urlconn-stream", () -> {
      HttpURLConnection c = (HttpURLConnection) new URL(base + "/stream").openConnection();
      String body = new String(c.getInputStream().readAllBytes());
      return c.getResponseCode() + " " + body.split("\n").length + " " + c.getHeaderField("Transfer-encoding");
    });
    t("urlconn-redirect", () -> {
      HttpURLConnection c = (HttpURLConnection) new URL(base + "/redirect").openConnection();
      return c.getResponseCode() + " " + c.getURL().getPath();
    });
    t("urlconn-404", () -> {
      HttpURLConnection c = (HttpURLConnection) new URL(base + "/nope").openConnection();
      return c.getResponseCode() + " " + c.getResponseMessage();
    });
    t("urlconn-status-204", () -> {
      HttpURLConnection c = (HttpURLConnection) new URL(base + "/status?204").openConnection();
      return c.getResponseCode() + " " + c.getContentLength();
    });
    t("urlconn-keepalive-sequence", () -> {
      StringBuilder sb = new StringBuilder();
      for (int i = 0; i < 5; i++) {
        HttpURLConnection c = (HttpURLConnection) new URL(base + "/echo").openConnection();
        sb.append(c.getResponseCode()).append(new String(c.getInputStream().readAllBytes()).length()).append(' ');
      }
      return sb;
    });
    t("urlconn-request-headers", () -> {
      HttpURLConnection c = (HttpURLConnection) new URL(base + "/headers").openConnection();
      c.setRequestProperty("X-One", "1");
      c.addRequestProperty("X-Two", "a");
      c.addRequestProperty("X-Two", "b");
      return new String(c.getInputStream().readAllBytes());
    });

    HttpClient client = HttpClient.newBuilder().version(HttpClient.Version.HTTP_1_1).build();
    t("client-get", () -> {
      HttpResponse<String> r = client.send(HttpRequest.newBuilder(URI.create(base + "/echo")).build(),
          HttpResponse.BodyHandlers.ofString());
      return r.statusCode() + " " + r.headers().firstValue("x-seen-method").orElse("?") + " [" + r.body() + "] "
          + r.version();
    });
    t("client-post", () -> {
      HttpResponse<String> r = client.send(HttpRequest.newBuilder(URI.create(base + "/echo"))
          .POST(HttpRequest.BodyPublishers.ofString("client body")).build(), HttpResponse.BodyHandlers.ofString());
      return r.statusCode() + " " + r.headers().firstValue("x-seen-length").orElse("?") + " " + r.body();
    });
    t("client-put-big", () -> {
      HttpResponse<byte[]> r = client.send(HttpRequest.newBuilder(URI.create(base + "/echo"))
          .PUT(HttpRequest.BodyPublishers.ofByteArray(big)).build(), HttpResponse.BodyHandlers.ofByteArray());
      return r.statusCode() + " " + r.body().length + " " + sha(r.body()).equals(sha(big));
    });
    t("client-stream-lines", () -> {
      HttpResponse<List<String>> r = client.send(HttpRequest.newBuilder(URI.create(base + "/stream")).build(),
          HttpResponse.BodyHandlers.ofLines()).statusCode() == 200
              ? null : null;
      return "skipped";
    });
    t("client-follow", () -> {
      HttpClient follow = HttpClient.newBuilder().version(HttpClient.Version.HTTP_1_1)
          .followRedirects(HttpClient.Redirect.NORMAL).build();
      HttpResponse<String> r = follow.send(HttpRequest.newBuilder(URI.create(base + "/redirect")).build(),
          HttpResponse.BodyHandlers.ofString());
      return r.statusCode() + " " + r.uri().getPath();
    });
    t("client-async-many", () -> {
      List<CompletableFuture<Integer>> futures = new java.util.ArrayList<>();
      for (int i = 0; i < 12; i++) {
        futures.add(client.sendAsync(HttpRequest.newBuilder(URI.create(base + "/status?" + (200 + i))).build(),
            HttpResponse.BodyHandlers.discarding()).thenApply(HttpResponse::statusCode));
      }
      StringBuilder sb = new StringBuilder();
      for (CompletableFuture<Integer> f : futures) {
        sb.append(f.get()).append(' ');
      }
      return sb;
    });
    t("client-custom-headers", () -> {
      HttpResponse<String> r = client.send(HttpRequest.newBuilder(URI.create(base + "/headers"))
          .header("X-A", "1").header("X-B", "2").build(), HttpResponse.BodyHandlers.ofString());
      return r.body();
    });
    t("client-parallel-threads", () -> {
      CountDownLatch done = new CountDownLatch(10);
      List<String> results = new CopyOnWriteArrayList<>();
      for (int i = 0; i < 10; i++) {
        final int n = i;
        new Thread(() -> {
          try {
            HttpResponse<String> r = client.send(HttpRequest.newBuilder(URI.create(base + "/echo"))
                .POST(HttpRequest.BodyPublishers.ofString("t" + n)).build(), HttpResponse.BodyHandlers.ofString());
            results.add(r.body());
          } catch (Exception e) {
            results.add("ERR " + e);
          } finally {
            done.countDown();
          }
        }).start();
      }
      done.await();
      java.util.Collections.sort(results);
      return results;
    });

    HttpsServer https = HttpsServer.create(new InetSocketAddress(InetAddress.getLoopbackAddress(), 0), 0);
    KeyStore ks = KeyStore.getInstance("PKCS12");
    ks.load(new ByteArrayInputStream(Base64.getDecoder().decode(kKeystore)), "changeit".toCharArray());
    KeyManagerFactory kmf = KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm());
    kmf.init(ks, "changeit".toCharArray());
    TrustManagerFactory tmf = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
    tmf.init(ks);
    SSLContext server_ctx = SSLContext.getInstance("TLS");
    server_ctx.init(kmf.getKeyManagers(), tmf.getTrustManagers(), null);
    https.setHttpsConfigurator(new HttpsConfigurator(server_ctx));
    https.createContext("/secure", ex -> {
      HttpsExchange sx = (HttpsExchange) ex;
      write(ex, 200, ("secure " + sx.getSSLSession().getProtocol().startsWith("TLS") + " "
          + (sx.getSSLSession().getCipherSuite() != null)).getBytes());
    });
    https.start();
    int sport = https.getAddress().getPort();
    t("https-client", () -> {
      SSLContext client_ctx = SSLContext.getInstance("TLS");
      client_ctx.init(null, tmf.getTrustManagers(), null);
      HttpClient hc = HttpClient.newBuilder().sslContext(client_ctx).version(HttpClient.Version.HTTP_1_1).build();
      HttpResponse<String> r = hc.send(HttpRequest.newBuilder(URI.create("https://localhost:" + sport + "/secure"))
          .build(), HttpResponse.BodyHandlers.ofString());
      return r.statusCode() + " " + r.body();
    });
    t("https-urlconn", () -> {
      SSLContext client_ctx = SSLContext.getInstance("TLS");
      client_ctx.init(null, tmf.getTrustManagers(), null);
      javax.net.ssl.HttpsURLConnection c = (javax.net.ssl.HttpsURLConnection) new URL("https://localhost:" + sport
          + "/secure").openConnection();
      c.setSSLSocketFactory(client_ctx.getSocketFactory());
      return c.getResponseCode() + " " + new String(c.getInputStream().readAllBytes());
    });
    t("https-untrusted", () -> {
      HttpURLConnection c = (HttpURLConnection) new URL("https://localhost:" + sport + "/secure").openConnection();
      return c.getResponseCode();
    });
    t("https-configurator-null", () -> HttpsServer.create().getHttpsConfigurator());

    server.stop(0);
    https.stop(0);
    pool.shutdown();
    System.exit(0);
  }
}
