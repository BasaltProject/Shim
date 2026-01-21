/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 * SPDX-License-Identifier: MIT
 * Minimum release: 21
 */

import com.sun.net.httpserver.Authenticator;
import com.sun.net.httpserver.BasicAuthenticator;
import com.sun.net.httpserver.Filter;
import com.sun.net.httpserver.Headers;
import com.sun.net.httpserver.HttpContext;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpHandlers;
import com.sun.net.httpserver.HttpPrincipal;
import com.sun.net.httpserver.HttpServer;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.concurrent.Callable;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class HttpServerModern {
  static final List<String> kEvents = new CopyOnWriteArrayList<>();

  static void t(String name, Callable<Object> c) {
    try {
      System.out.println(name + " = " + c.call());
    } catch (Throwable e) {
      System.out.println(name + " ! " + e.getClass().getName() + (e instanceof NullPointerException ? "" : ": " + e.getMessage()));
    }
  }

  static byte[] readAll(InputStream in) throws IOException {
    ByteArrayOutputStream out = new ByteArrayOutputStream();
    byte[] buf = new byte[1024];
    int n;
    while ((n = in.read(buf)) > 0) {
      out.write(buf, 0, n);
    }
    return out.toByteArray();
  }

  static String readLine(InputStream in) throws IOException {
    StringBuilder sb = new StringBuilder();
    int c;
    while ((c = in.read()) != -1) {
      if (c == '\n') {
        break;
      }
      sb.append((char) c);
    }
    if (c == -1 && sb.length() == 0) {
      return null;
    }
    int n = sb.length();
    if (n > 0 && sb.charAt(n - 1) == '\r') {
      sb.setLength(n - 1);
    }
    return sb.toString();
  }

  static String normalizeHeader(String line) {
    if (line.startsWith("Date: ")) {
      return "Date: <date>";
    }
    return line;
  }

  static String readResponse(InputStream in, boolean head) throws IOException {
    String status = readLine(in);
    if (status == null) {
      return "<eof>";
    }
    TreeMap<String, String> headers = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);
    List<String> order = new ArrayList<>();
    String line;
    while ((line = readLine(in)) != null && !line.isEmpty()) {
      order.add(normalizeHeader(line));
      int colon = line.indexOf(':');
      headers.put(line.substring(0, colon), line.substring(colon + 1).trim());
    }
    StringBuilder sb = new StringBuilder(status);
    for (String h : order) {
      sb.append(" | ").append(h);
    }
    byte[] body = new byte[0];
    int code = Integer.parseInt(status.split(" ")[1]);
    if (head || code == 204 || code == 304 || (code >= 100 && code < 200)) {
      body = new byte[0];
    } else if ("chunked".equalsIgnoreCase(headers.get("Transfer-Encoding"))) {
      ByteArrayOutputStream out = new ByteArrayOutputStream();
      for (;;) {
        String size = readLine(in);
        int n = Integer.parseInt(size.trim(), 16);
        if (n == 0) {
          readLine(in);
          break;
        }
        byte[] chunk = in.readNBytes(n);
        out.write(chunk);
        readLine(in);
      }
      body = out.toByteArray();
    } else if (headers.containsKey("Content-length")) {
      body = in.readNBytes(Integer.parseInt(headers.get("Content-length")));
    } else if ("close".equalsIgnoreCase(headers.get("Connection"))) {
      body = readAll(in);
    }
    return sb + " || " + new String(body, StandardCharsets.ISO_8859_1).replace("\r", "\\r").replace("\n", "\\n");
  }

  static String exchange(int port, String request) throws Exception {
    try (Socket s = new Socket(InetAddress.getLoopbackAddress(), port)) {
      s.setSoTimeout(2000);
      OutputStream out = s.getOutputStream();
      out.write(request.getBytes(StandardCharsets.ISO_8859_1));
      out.flush();
      boolean head = request.startsWith("HEAD ");
      return readResponse(s.getInputStream(), head);
    }
  }

  static String get(int port, String path, String... headers) throws Exception {
    StringBuilder sb = new StringBuilder("GET " + path + " HTTP/1.1\r\nHost: localhost\r\nConnection: close\r\n");
    for (String h : headers) {
      sb.append(h).append("\r\n");
    }
    return exchange(port, sb.append("\r\n").toString());
  }

  static void send(HttpExchange ex, int code, String body) throws IOException {
    byte[] bytes = body.getBytes(StandardCharsets.UTF_8);
    ex.sendResponseHeaders(code, bytes.length);
    try (OutputStream os = ex.getResponseBody()) {
      os.write(bytes);
    }
  }

  public static void main(String[] args) throws Exception {
    HttpServer server = HttpServer.create(new InetSocketAddress(InetAddress.getLoopbackAddress(), 0), 0);
    int port = server.getAddress().getPort();
    t("address-loopback", () -> server.getAddress().getAddress().isLoopbackAddress());
    t("executor-default", () -> server.getExecutor());

    server.createContext("/hello", ex -> send(ex, 200, "hello"));
    server.createContext("/chunked", ex -> {
      ex.getResponseHeaders().add("Content-Type", "text/plain");
      ex.sendResponseHeaders(200, 0);
      try (OutputStream os = ex.getResponseBody()) {
        os.write("abc".getBytes());
        os.write("defgh".getBytes());
      }
    });
    server.createContext("/headok", ex -> {
      if (ex.getRequestMethod().equals("HEAD")) {
        ex.getResponseHeaders().set("X-Head", "yes");
        ex.sendResponseHeaders(200, -1);
        ex.close();
      } else {
        send(ex, 200, "body");
      }
    });
    server.createContext("/empty", ex -> {
      ex.sendResponseHeaders(204, -1);
      ex.close();
    });
    server.createContext("/nobody", ex -> {
      ex.sendResponseHeaders(200, -1);
      ex.close();
    });
    server.createContext("/echo", ex -> {
      byte[] body = readAll(ex.getRequestBody());
      ex.getResponseHeaders().set("X-Method", ex.getRequestMethod());
      ex.getResponseHeaders().add("x-multi", "one");
      ex.getResponseHeaders().add("X-MULTI", "two");
      ex.sendResponseHeaders(200, body.length);
      try (OutputStream os = ex.getResponseBody()) {
        os.write(body);
      }
    });
    server.createContext("/info", ex -> {
      Headers h = ex.getRequestHeaders();
      String body = ex.getRequestMethod() + "|" + ex.getRequestURI() + "|" + ex.getProtocol() + "|"
          + h.getFirst("Host") + "|" + h.getFirst("x-custom") + "|" + h.get("X-Multi") + "|"
          + ex.getHttpContext().getPath() + "|" + ex.getRemoteAddress().getAddress().isLoopbackAddress() + "|"
          + (ex.getLocalAddress().getPort() > 0) + "|" + (ex.getPrincipal() == null) + "|" + ex.getResponseCode();
      send(ex, 200, body);
    });
    server.createContext("/a", ex -> send(ex, 200, "a"));
    server.createContext("/a/b", ex -> send(ex, 200, "a/b"));
    server.createContext("/", ex -> send(ex, 404, "root fallback"));
    server.createContext("/io", ex -> {
      throw new IOException("handler failure");
    });
    server.createContext("/rt", ex -> {
      throw new IllegalStateException("handler runtime failure");
    });
    server.createContext("/silent", ex -> {
      ex.close();
    });
    server.createContext("/toomany", ex -> {
      ex.sendResponseHeaders(200, 3);
      try (OutputStream os = ex.getResponseBody()) {
        try {
          os.write("abcdef".getBytes());
        } catch (IOException e) {
          kEvents.add("toomany: " + e.getMessage());
          os.write("abc".getBytes());
        }
      }
    });
    server.createContext("/toofew", ex -> {
      ex.sendResponseHeaders(200, 10);
      OutputStream os = ex.getResponseBody();
      os.write("abc".getBytes());
      try {
        os.close();
      } catch (IOException e) {
        kEvents.add("toofew: " + e.getMessage());
      }
    });
    server.createContext("/twice", ex -> {
      ex.sendResponseHeaders(200, 0);
      try {
        ex.sendResponseHeaders(200, 0);
      } catch (IOException e) {
        kEvents.add("twice: " + e.getMessage());
      }
      ex.getResponseBody().close();
    });
    server.createContext("/nohdr", ex -> {
      try {
        ex.getResponseBody().write(1);
      } catch (IOException e) {
        kEvents.add("nohdr: " + e.getMessage());
      }
      send(ex, 200, "late");
    });
    server.createContext("/code", ex -> {
      int code = Integer.parseInt(ex.getRequestURI().getQuery());
      ex.sendResponseHeaders(code, -1);
      ex.close();
    });
    server.createContext("/attr", ex -> {
      ex.setAttribute("k", "v");
      send(ex, 200, ex.getAttribute("k") + "|" + ex.getAttribute("missing") + "|"
          + ex.getHttpContext().getAttributes().get("ctx"));
    }).getAttributes().put("ctx", "ctxvalue");

    HttpContext filtered = server.createContext("/filtered", ex -> send(ex, 200, "filtered-body"));
    filtered.getFilters().add(Filter.beforeHandler("before", ex -> kEvents.add("before1")));
    filtered.getFilters().add(new Filter() {
      @Override
      public void doFilter(HttpExchange exchange, Chain chain) throws IOException {
        kEvents.add("filter2-in");
        exchange.getResponseHeaders().add("X-Filter", "2");
        chain.doFilter(exchange);
        kEvents.add("filter2-out");
      }

      @Override
      public String description() {
        return "second";
      }
    });
    filtered.getFilters().add(Filter.afterHandler("after", ex -> kEvents.add("after:" + ex.getResponseCode())));

    HttpContext auth = server.createContext("/auth", ex -> send(ex, 200, "secret for " + ex.getPrincipal().getName()));
    BasicAuthenticator basic = new BasicAuthenticator("realm1") {
      @Override
      public boolean checkCredentials(String user, String password) {
        return user.equals("alice") && password.equals("pw");
      }
    };
    t("auth-set-returns-old", () -> auth.setAuthenticator(basic));
    t("auth-get", () -> auth.getAuthenticator() == basic);

    HttpContext custom = server.createContext("/custom", ex -> send(ex, 200, "custom"));
    custom.setAuthenticator(new Authenticator() {
      @Override
      public Result authenticate(HttpExchange exchange) {
        String v = exchange.getRequestHeaders().getFirst("X-Auth");
        if (v == null) {
          return new Failure(403);
        }
        if (v.equals("retry")) {
          exchange.getResponseHeaders().add("WWW-Authenticate", "Custom");
          return new Retry(401);
        }
        return new Success(new HttpPrincipal(v, "customRealm"));
      }
    });

    server.createContext("/factory", HttpHandlers.of(201, Headers.of("X-Fixed", "yes"), "fixed body"));

    t("start", () -> {
      server.start();
      return "started";
    });

    t("hello", () -> get(port, "/hello"));
    t("chunked", () -> get(port, "/chunked"));
    t("empty", () -> get(port, "/empty"));
    t("nobody", () -> get(port, "/nobody"));
    t("head", () -> exchange(port, "HEAD /headok HTTP/1.1\r\nHost: localhost\r\nConnection: close\r\n\r\n"));
    t("get-headok", () -> get(port, "/headok"));
    t("head-nobody", () -> exchange(port, "HEAD /nobody HTTP/1.1\r\nHost: localhost\r\nConnection: close\r\n\r\n"));
    t("post-echo", () -> exchange(port, "POST /echo HTTP/1.1\r\nHost: localhost\r\nConnection: close\r\n"
        + "Content-Length: 11\r\n\r\nhello world"));
    t("chunked-request", () -> exchange(port, "POST /echo HTTP/1.1\r\nHost: localhost\r\nConnection: close\r\n"
        + "Transfer-Encoding: chunked\r\n\r\n5\r\nhello\r\n6\r\n world\r\n0\r\n\r\n"));
    t("put-empty", () -> exchange(port, "PUT /echo HTTP/1.1\r\nHost: localhost\r\nConnection: close\r\n\r\n"));
    t("info", () -> get(port, "/info/x/y?q=1&r=a%20b", "X-Custom: custom value", "X-Multi: m1", "X-MULTI: m2"));
    t("info-absolute-uri", () -> exchange(port, "GET http://localhost/info/abs?z=1 HTTP/1.1\r\nHost: other\r\n"
        + "Connection: close\r\n\r\n"));
    t("ctx-a", () -> get(port, "/a"));
    t("ctx-a-slash", () -> get(port, "/a/"));
    t("ctx-a-b", () -> get(port, "/a/b"));
    t("ctx-a-b-c", () -> get(port, "/a/b/c"));
    t("ctx-a-x", () -> get(port, "/a/x"));
    t("ctx-ab", () -> get(port, "/ab"));
    t("ctx-root", () -> get(port, "/unknown"));
    t("io-failure", () -> get(port, "/io"));
    t("rt-failure", () -> get(port, "/rt"));
    t("silent", () -> get(port, "/silent"));
    t("toomany", () -> get(port, "/toomany"));
    t("toofew", () -> get(port, "/toofew"));
    t("twice", () -> get(port, "/twice"));
    t("nohdr", () -> get(port, "/nohdr"));
    t("code-100", () -> get(port, "/code?100"));
    t("code-304", () -> get(port, "/code?304"));
    t("code-404", () -> get(port, "/code?404"));
    t("code-500", () -> get(port, "/code?500"));
    t("code-999", () -> get(port, "/code?999"));
    t("attr", () -> get(port, "/attr"));
    t("filtered", () -> get(port, "/filtered"));
    t("auth-none", () -> get(port, "/auth"));
    t("auth-bad", () -> get(port, "/auth", "Authorization: Basic "
        + java.util.Base64.getEncoder().encodeToString("alice:nope".getBytes())));
    t("auth-good", () -> get(port, "/auth", "Authorization: Basic "
        + java.util.Base64.getEncoder().encodeToString("alice:pw".getBytes())));
    t("auth-malformed", () -> get(port, "/auth", "Authorization: Basic !!!"));
    t("auth-wrong-scheme", () -> get(port, "/auth", "Authorization: Bearer token"));
    t("custom-none", () -> get(port, "/custom"));
    t("custom-retry", () -> get(port, "/custom", "X-Auth: retry"));
    t("custom-ok", () -> get(port, "/custom", "X-Auth: bob"));
    t("factory", () -> get(port, "/factory"));
    t("bad-request-line", () -> exchange(port, "BROKEN\r\n\r\n"));
    t("bad-version", () -> exchange(port, "GET /hello HTTP/9.9\r\nHost: localhost\r\nConnection: close\r\n\r\n"));
    t("http10", () -> exchange(port, "GET /hello HTTP/1.0\r\n\r\n"));
    t("http10-chunked", () -> exchange(port, "GET /chunked HTTP/1.0\r\n\r\n"));
    t("no-host-11", () -> exchange(port, "GET /hello HTTP/1.1\r\nConnection: close\r\n\r\n"));
    t("lowercase-method", () -> exchange(port, "get /hello HTTP/1.1\r\nHost: localhost\r\nConnection: close\r\n\r\n"));
    t("bad-content-length", () -> exchange(port, "POST /echo HTTP/1.1\r\nHost: localhost\r\nConnection: close\r\n"
        + "Content-Length: abc\r\n\r\n"));
    t("unknown-method", () -> exchange(port, "FROB /hello HTTP/1.1\r\nHost: localhost\r\nConnection: close\r\n\r\n"));
    t("expect-continue", () -> {
      try (Socket s = new Socket(InetAddress.getLoopbackAddress(), port)) {
        s.setSoTimeout(2000);
        OutputStream out = s.getOutputStream();
        out.write(("POST /echo HTTP/1.1\r\nHost: localhost\r\nConnection: close\r\nContent-Length: 4\r\n"
            + "Expect: 100-continue\r\n\r\n").getBytes());
        out.flush();
        String first = readResponse(s.getInputStream(), false);
        out.write("body".getBytes());
        out.flush();
        return first + " ## " + readResponse(s.getInputStream(), false);
      }
    });
    t("keep-alive", () -> {
      try (Socket s = new Socket(InetAddress.getLoopbackAddress(), port)) {
        s.setSoTimeout(2000);
        OutputStream out = s.getOutputStream();
        InputStream in = s.getInputStream();
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < 3; i++) {
          out.write("GET /hello HTTP/1.1\r\nHost: localhost\r\n\r\n".getBytes());
          out.flush();
          sb.append(readResponse(in, false)).append(" ## ");
        }
        return sb;
      }
    });
    t("keep-alive-unread-body", () -> {
      try (Socket s = new Socket(InetAddress.getLoopbackAddress(), port)) {
        s.setSoTimeout(2000);
        OutputStream out = s.getOutputStream();
        InputStream in = s.getInputStream();
        out.write("POST /hello HTTP/1.1\r\nHost: localhost\r\nContent-Length: 5\r\n\r\nabcde".getBytes());
        out.write("GET /hello HTTP/1.1\r\nHost: localhost\r\n\r\n".getBytes());
        out.flush();
        return readResponse(in, false) + " ## " + readResponse(in, false);
      }
    });
    t("pipelined", () -> {
      try (Socket s = new Socket(InetAddress.getLoopbackAddress(), port)) {
        s.setSoTimeout(2000);
        OutputStream out = s.getOutputStream();
        InputStream in = s.getInputStream();
        out.write(("GET /hello HTTP/1.1\r\nHost: localhost\r\n\r\nGET /a HTTP/1.1\r\nHost: localhost\r\n\r\n")
            .getBytes());
        out.flush();
        return readResponse(in, false) + " ## " + readResponse(in, false);
      }
    });

    t("events", () -> new ArrayList<>(kEvents));

    t("create-context-bad-path", () -> server.createContext("nopath", ex -> { }));
    t("create-context-empty-path", () -> server.createContext("", ex -> { }));
    t("create-context-null-path", () -> server.createContext(null, ex -> { }));
    t("create-context-dup", () -> server.createContext("/hello", ex -> { }));
    t("create-context-no-handler", () -> {
      HttpContext c = server.createContext("/nohandler");
      return c.getHandler() + " " + c.getPath() + " " + (c.getServer() == server) + " " + c.getFilters().size();
    });
    t("nohandler-request", () -> get(port, "/nohandler"));
    t("set-handler", () -> {
      HttpContext c = server.createContext("/sethandler");
      HttpHandler h = ex -> send(ex, 200, "set");
      c.setHandler(h);
      return c.getHandler() == h;
    });
    t("set-handler-null", () -> server.createContext("/sethandler").getHandler());
    t("remove-context-string", () -> {
      server.createContext("/removeme", ex -> send(ex, 200, "x"));
      server.removeContext("/removeme");
      return get(port, "/removeme");
    });
    t("remove-context-missing", () -> {
      server.removeContext("/never");
      return "unreachable";
    });
    t("remove-context-object", () -> {
      HttpContext c = server.createContext("/removeobj", ex -> send(ex, 200, "x"));
      server.removeContext(c);
      return get(port, "/removeobj");
    });
    t("headers-normalize", () -> {
      Headers h = new Headers();
      h.add("content-TYPE", "a");
      h.add("Content-Type", "b");
      h.set("x-y-z", "1");
      h.add("ALLCAPS", "2");
      return h.get("CONTENT-type") + " " + h.getFirst("x-Y-z") + " " + h.containsKey("allcaps") + " "
          + new TreeMap<>(h) + " " + h.size();
    });
    t("headers-null-key", () -> {
      new Headers().add(null, "x");
      return "added";
    });
    t("headers-null-value", () -> {
      Headers h = new Headers();
      h.add("a", null);
      return h.get("a");
    });
    t("headers-bad-key", () -> {
      new Headers().add("bad key\n", "x");
      return "added";
    });
    t("headers-bad-value", () -> {
      new Headers().add("ok", "bad\nvalue");
      return "added";
    });
    t("headers-of", () -> new TreeMap<>(Headers.of("a", "1", "B", "2")));
    t("headers-of-odd", () -> Headers.of("a"));
    t("headers-of-map", () -> new TreeMap<>(Headers.of(Map.of("k", List.of("v1", "v2")))));
    t("headers-unmodifiable", () -> {
      Headers.of("a", "1").add("b", "2");
      return "added";
    });
    t("headers-equals", () -> {
      Headers a = new Headers();
      a.add("x", "1");
      Headers b = new Headers();
      b.set("X", "1");
      return a.equals(b) + " " + (a.hashCode() == b.hashCode());
    });
    t("headers-put-all", () -> {
      Headers h = new Headers();
      h.putAll(Map.of("abc", List.of("1")));
      return h.getFirst("ABC");
    });
    t("headers-remove", () -> {
      Headers h = new Headers();
      h.add("Foo", "1");
      return h.remove("fOO") + " " + h.isEmpty();
    });
    t("headers-first-empty", () -> new Headers().getFirst("none"));
    t("headers-to-string", () -> {
      Headers h = new Headers();
      h.add("one", "1");
      return h;
    });

    t("set-executor-after-start", () -> {
      server.setExecutor(Executors.newSingleThreadExecutor());
      return "no error";
    });
    t("pooled", () -> {
      HttpServer pooled = HttpServer.create(new InetSocketAddress(InetAddress.getLoopbackAddress(), 0), 0);
      ExecutorService pool = Executors.newFixedThreadPool(4);
      pooled.setExecutor(pool);
      pooled.createContext("/threaded", ex -> send(ex, 200, Thread.currentThread().getName().startsWith("pool-")
          ? "pool" : "other"));
      pooled.createContext("/hello", ex -> send(ex, 200, "hello"));
      pooled.start();
      int pport = pooled.getAddress().getPort();
      try {
        Object same = pooled.getExecutor() == pool;
        Object body = get(pport, "/threaded");
        CountDownLatch done = new CountDownLatch(8);
        List<String> results = new CopyOnWriteArrayList<>();
        for (int i = 0; i < 8; i++) {
          new Thread(() -> {
            try {
              results.add(get(pport, "/hello"));
            } catch (Exception e) {
              results.add("ERR " + e);
            } finally {
              done.countDown();
            }
          }).start();
        }
        done.await(20, TimeUnit.SECONDS);
        return same + " " + body + " " + results.size() + " " + new java.util.HashSet<>(results).size() + " "
            + results.get(0);
      } finally {
        pooled.stop(0);
        pool.shutdown();
      }
    });
    t("start-twice", () -> {
      server.start();
      return "no error";
    });
    t("bind-after-start", () -> {
      server.bind(new InetSocketAddress(0), 0);
      return "bound";
    });
    t("stop-negative", () -> {
      HttpServer other = HttpServer.create(new InetSocketAddress(InetAddress.getLoopbackAddress(), 0), 0);
      other.stop(-1);
      return "no";
    });
    t("unbound-address", () -> HttpServer.create().getAddress());
    t("create-with-handler", () -> {
      HttpServer s = HttpServer.create(new InetSocketAddress(InetAddress.getLoopbackAddress(), 0), 0, "/x",
          ex -> send(ex, 200, "initial"));
      s.start();
      try {
        return get(s.getAddress().getPort(), "/x");
      } finally {
        s.stop(0);
      }
    });
    t("stop", () -> {
      server.stop(0);
      return "stopped";
    });
    t("after-stop", () -> {
      try {
        return get(port, "/hello");
      } catch (IOException e) {
        return "refused";
      }
    });
    t("restart-after-stop", () -> {
      server.start();
      return "restarted";
    });
    System.exit(0);
  }
}
