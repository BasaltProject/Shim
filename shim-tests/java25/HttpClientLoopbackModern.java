/*
 * Copyright (c) 2026 BasaltMC.
 *
 * Licensed under the MIT License; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

import com.sun.net.httpserver.HttpServer;
import java.net.InetSocketAddress;
import java.net.URI;
import java.net.http.*;
import java.nio.charset.StandardCharsets;

public class HttpClientLoopbackModern {
  public static void main(String[] a) throws Exception {
    HttpServer s = HttpServer.create(new InetSocketAddress("127.0.0.1", 0), 0);
    s.createContext("/echo", x -> {
      byte[] body = x.getRequestBody().readAllBytes();
      String r = x.getRequestMethod() + " " + new String(body, StandardCharsets.UTF_8) + " " + x.getRequestHeaders().getFirst("X-Test");
      byte[] out = r.getBytes(StandardCharsets.UTF_8);
      x.getResponseHeaders().add("X-Reply", "ok");
      x.sendResponseHeaders(200, out.length);
      x.getResponseBody().write(out);
      x.close();
    });
    s.createContext("/redir", x -> {
      x.getResponseHeaders().add("Location", "/echo");
      x.sendResponseHeaders(302, -1);
      x.close();
    });
    s.start();
    String base = "http://127.0.0.1:" + s.getAddress().getPort();
    HttpClient c = HttpClient.newBuilder().followRedirects(HttpClient.Redirect.NORMAL).build();
    HttpResponse<String> r = c.send(HttpRequest.newBuilder(URI.create(base + "/echo")).header("X-Test", "1")
        .POST(HttpRequest.BodyPublishers.ofString("hello")).build(), HttpResponse.BodyHandlers.ofString());
    System.out.println(r.statusCode() + " " + r.body() + " " + r.headers().firstValue("x-reply").orElse("-") + " " + r.version());
    r = c.send(HttpRequest.newBuilder(URI.create(base + "/redir")).GET().build(), HttpResponse.BodyHandlers.ofString());
    System.out.println(r.statusCode() + " " + r.body() + " " + r.uri().getPath());
    System.out.println(c.sendAsync(HttpRequest.newBuilder(URI.create(base + "/echo")).GET().build(),
        HttpResponse.BodyHandlers.ofString()).get().body());
    s.stop(0);
  }
}
